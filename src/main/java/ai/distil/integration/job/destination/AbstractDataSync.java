package ai.distil.integration.job.destination;

import ai.distil.api.internal.model.dto.datasource.DTODataSourceAttributeExtended;
import ai.distil.api.internal.model.dto.destination.DestinationDTO;
import ai.distil.api.internal.model.dto.destination.DestinationIntegrationDTO;
import ai.distil.api.internal.model.dto.destination.HyperPersonalizedDestinationDTO;
import ai.distil.integration.job.destination.vo.CustomAttributeDefinition;
import ai.distil.integration.job.destination.vo.SendSubscribersResult;
import ai.distil.integration.job.destination.vo.UtmData;
import ai.distil.integration.job.sync.AbstractSubscriber;
import ai.distil.integration.job.sync.http.AbstractHttpConnection;
import ai.distil.integration.job.sync.http.sync.SyncSettings;
import ai.distil.integration.controller.dto.destination.SyncDestinationProgressData;
import ai.distil.integration.utils.ListUtils;
import ai.distil.model.org.CustomerRecord;
import ai.distil.model.org.destination.IntegrationSettings;
import ai.distil.model.types.DataSourceAttributeType;
import ai.distil.model.types.DataSourceSchemaAttributeTag;
import ai.distil.model.types.DataSourceType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ai.distil.integration.job.sync.AbstractSubscriber.HASH_CODE_FRIENDLY_NAME;
import static ai.distil.integration.utils.HashHelper.STRING_FUNNEL;
import static ai.distil.model.types.DataSourceSchemaAttributeTag.*;


/**
 * S - means subscriber (member, etc..)
 */

@Slf4j
@AllArgsConstructor
public abstract class AbstractDataSync<C extends AbstractHttpConnection, S extends AbstractSubscriber> {
    private static final DecimalFormat DEFAULT_NUMBER_FORMAT = new DecimalFormat("#,##0.00");

    private static final Integer DEFAULT_BATCH_SIZE = 100;
    private static final String LIST_NAME_TEMPLATE = "DISTIL-%s";
    protected static final long DEFAULT_HASH_CODE_FIELD_ID = -1000_000L;
    protected static final String HASH_CODE_FIELD_NAME = buildFieldId(DEFAULT_HASH_CODE_FIELD_ID);

    protected DestinationDTO destination;
    protected DestinationIntegrationDTO destinationIntegration;
    @Getter
    private List<DTODataSourceAttributeExtended> attributes;
    protected SyncSettings syncSettings;
    protected C httpConnection;

    protected UtmData utmData;


    private static final Set<DataSourceSchemaAttributeTag> UTM_URLS_ATTRIBUTES = Sets.newHashSet(PRODUCT_SHOP_URL,
            PRODUCT_IMAGE_URL, PRODUCT_THUMBNAIL_URL, CONTENT_URL, CONTENT_IMAGE_URL);

    public AbstractDataSync(DestinationDTO destination, DestinationIntegrationDTO destinationIntegration,
                            List<DTODataSourceAttributeExtended> attributes, SyncSettings syncSettings, C httpConnection) {
        this.destination = destination;
        this.destinationIntegration = destinationIntegration;
        this.attributes = attributes;
        this.syncSettings = syncSettings;
        this.httpConnection = httpConnection;

        this.utmData = buildUtmData(destination);
    }

    public abstract IntegrationSettings findIntegrationSettings();

    /**
     * @return list id
     */
    public abstract String createListIfNotExists();

    public abstract List<CustomAttributeDefinition> syncCustomAttributesSchema(String listId);

    protected abstract Map<String, String> retrieveCurrentUsersAndHashes(String listId);

    protected String buildListName(String destinationTitle) {
        return String.format(LIST_NAME_TEMPLATE, destinationTitle).trim();
    }

    protected static String buildFieldId(Long id) {
        return String.format("D%s", id);
    }

    protected List<DTODataSourceAttributeExtended> retrieveAllAttributesBySettings() {
        Integer defaultProductsCount = this.syncSettings.getDefaultProductsCount();

        return Stream.concat(this.attributes.stream()
                        .filter(attr -> !Optional.ofNullable(attr.getAutoGenerated()).orElse(false)
                                || Optional.ofNullable(attr.getPosition()).orElse(0) <= defaultProductsCount),
                Stream.of(getDefaultHashField()))
                .collect(Collectors.toList());
    }

    protected DTODataSourceAttributeExtended getDefaultHashField() {
        DTODataSourceAttributeExtended attr = new DTODataSourceAttributeExtended(true);
        attr.setAttributeDisplayName(HASH_CODE_FRIENDLY_NAME);
        attr.setId(DEFAULT_HASH_CODE_FIELD_ID);
        attr.setAttributeDataTag(DataSourceSchemaAttributeTag.NONE);
        attr.setPosition(1);

        return attr;
    }

    protected abstract S buildBaseSubscriber();

    protected abstract void addCustomField(S subscriber, String fieldName, String value);

    protected abstract SendSubscribersResult sendSubscribers(String listId, List<S> subscribers);

    protected abstract void removeSubscribers(String listId, Collection<String> subscribersIds);


    public SyncDestinationProgressData ingestData(String listId, List<CustomAttributeDefinition> attributes, List<CustomerRecord> data) {
        SyncDestinationProgressData progressData = new SyncDestinationProgressData();

//        sort attributes first, it's required for current products logic
        attributes.sort(Comparator.comparing(cad -> Optional.ofNullable(cad).map(CustomAttributeDefinition::getPosition).orElse(-1)));

        Map<DataSourceSchemaAttributeTag, Set<String>> attributesToBackfill = ListUtils.groupByToLinkedSet(this.attributes.stream()
                        .filter(a -> Optional.ofNullable(a.getAutoGenerated()).orElse(false))
                        .sorted(Comparator.comparingInt(a -> Optional.ofNullable(a.getPosition()).orElse(0))).collect(Collectors.toList()),
                DTODataSourceAttributeExtended::getAttributeDataTag, dsa -> String.valueOf(dsa.getId()));

        Map<String, String> currentSubscribers = retrieveCurrentUsersAndHashes(listId);
        progressData.setBeforeRowsCount(currentSubscribers.size());

        List<S> subscribers = new ArrayList<>();

        data.forEach(record -> {
            S subscriber = generateSubscriberData(attributes, record, attributesToBackfill);
            if (subscriber == null) {
                progressData.incrementExcludedCounter();
            } else {

                Optional<String> existingHashOptional = Optional.ofNullable(currentSubscribers.remove(subscriber.getEmailAddress()));

                if (existingHashOptional.isPresent()) {
                    String hash = existingHashOptional.get();
                    if (subscriber.getHashCode().equals(hash)) {
                        progressData.incrementNotChangedCounter();
                    } else {
                        subscribers.add(subscriber);
                        progressData.incrementUpdatesCounter();
                    }
                } else {
                    progressData.incrementCreatesCounter();
                    subscribers.add(subscriber);
                }
            }

            if (subscribers.size() > DEFAULT_BATCH_SIZE) {
                log.info("Sending next subscribers batch - {} subscribers", DEFAULT_BATCH_SIZE);
                sendSubscribersAndTrackStats(listId, progressData, subscribers);
                subscribers.clear();
            }
        });

        if (subscribers.size() > 0) {
            log.info("Sending last subscribers batch - {} subscribers", subscribers.size());
            sendSubscribersAndTrackStats(listId, progressData, subscribers);
            subscribers.clear();
        }

        log.info("Subscribers to delete -> {}", currentSubscribers.size());

        progressData.setDeleted(currentSubscribers.size());

        removeSubscribers(listId, currentSubscribers.keySet());

//      todo newsfeed card?
        log.info("Successfully finished data sync for the integration - result {}", progressData);

        return progressData;

    }

    private void sendSubscribersAndTrackStats(String listId, SyncDestinationProgressData progressData, List<S> subscribers) {
        SendSubscribersResult sendSubscribersResult = sendSubscribers(listId, subscribers);

        progressData.addFailedEmails(sendSubscribersResult.getFailedSubscribers());
        progressData.updateErrorsCount(sendSubscribersResult.getFailedSubscribers().size());
    }

    protected S generateSubscriberData(List<CustomAttributeDefinition> attributes, CustomerRecord data, Map<DataSourceSchemaAttributeTag, Set<String>> fieldToBackfillByTags) {
        Hasher hasher = Hashing.sha1().newHasher();

        Set<String> attributesProcessed = new HashSet<>();

        ObjectNode customerValues = data.getCustomerValues();

        S result = buildBaseSubscriber();

        for (CustomAttributeDefinition attr : attributes) {

            String distilAttributeId = String.valueOf(attr.getDistilAttributeId());
            attributesProcessed.add(distilAttributeId);

            Optional<String> valueOptional = getValueAndUpdateHashIfNotNull(attr.getTag(), customerValues.get(distilAttributeId), hasher);
            boolean isAutoGeneratedAttr = Boolean.TRUE.equals(attr.getAutoGeneratedAttribute());

//            handle value present
            valueOptional.ifPresent(value -> {

                switch (attr.getTag()) {
                    case CUSTOMER_EMAIL_ADDRESS:
                        result.setEmail(value);
                        return;
                    case CUSTOMER_FIRST_NAME:
                        result.setFirstName(value);
                        return;
                    case CUSTOMER_LAST_NAME:
                        result.setLastName(value);
                        return;
                }

            });

            if (isProductDataSource(attr) && !valueOptional.isPresent() && isAutoGeneratedAttr) {

                Set<String> fieldsToBackfill = new HashSet<>(fieldToBackfillByTags.getOrDefault(attr.getTag(), Collections.emptySet()));
                fieldsToBackfill.removeAll(attributesProcessed);

                for (String s : fieldsToBackfill) {
                    attributesProcessed.add(s);

                    getValueAndUpdateHashIfNotNull(attr.getTag(), customerValues.get(s), hasher)
                            .ifPresent(value -> this.addCustomField(result, attr.getId(), formatField(attr.getTag(), attr.getType(), value)));

//                      this means that some auto generated required column can't be backfilled that's why we need to skip this consumer
                    return null;
                }
            } else if (valueOptional.isPresent()) {
                String value = formatField(attr.getTag(), attr.getType(), valueOptional.get());
                this.addCustomField(result, attr.getId(), value);
            }
        }

        result.setHashCode(buildFieldId(DEFAULT_HASH_CODE_FIELD_ID), hasher.hash().toString());

        return result;
    }

    private boolean isProductDataSource(CustomAttributeDefinition attr) {
        return DataSourceType.PRODUCT.equals(attr.getTag().getDataSourceType());
    }

    /**
     * WARN: impure, modifying hash
     */
    private Optional<String> getValueAndUpdateHashIfNotNull(DataSourceSchemaAttributeTag tag, JsonNode node, Hasher hasher) {

        return Optional.ofNullable(node)
                .filter(((Predicate<JsonNode>) JsonNode::isNull).negate())
                .map((v) -> {
                    String result = UTM_URLS_ATTRIBUTES.contains(tag) ? this.utmData.fillUrl(v.asText()) : v.asText();

                    hasher.putObject(result, STRING_FUNNEL);
                    return result;
                });

    }

    private UtmData buildUtmData(DestinationDTO destination) {
        if (destination instanceof HyperPersonalizedDestinationDTO) {
            HyperPersonalizedDestinationDTO destinationPersonalized = (HyperPersonalizedDestinationDTO) destination;
            return new UtmData(destinationPersonalized);
        }

        return new UtmData();
    }

    private String formatField(DataSourceSchemaAttributeTag tag, DataSourceAttributeType type, String value) {

        if (type == null || value == null) {
            return value;
        }

        switch (type) {
            case DOUBLE:
            case INTEGER:
                return toDoubleIfCan(value);
            default:
                if (tag == null) {
                    return value;
                } else {
                    switch (tag) {
                        case PRODUCT_LIST_PRICE_EX_TAX:
                        case PRODUCT_LIST_PRICE_INC_TAX:
                            return toDoubleIfCan(value);
                        default:
                            return value;
                    }
                }
        }
    }

    private String toDoubleIfCan(String value) {
        try {
            return DEFAULT_NUMBER_FORMAT.format(Double.parseDouble(value));
        } catch (Exception e) {
            log.warn("Can't parse double value: {}", value);
        }
        return value;
    }

}
