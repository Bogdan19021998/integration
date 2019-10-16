package ai.distil.integration.job.destination;

import ai.distil.api.internal.model.dto.datasource.DTODataSourceAttributeExtended;
import ai.distil.api.internal.model.dto.destination.DestinationIntegrationDTO;
import ai.distil.integration.job.destination.vo.CustomAttributeDefinition;
import ai.distil.integration.job.sync.http.AbstractHttpConnection;
import ai.distil.integration.job.sync.http.sync.SyncSettings;
import ai.distil.integration.utils.ListUtils;
import ai.distil.model.org.CustomerRecord;
import ai.distil.model.types.DataSourceSchemaAttributeTag;
import ai.distil.model.types.DataSourceType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import static ai.distil.model.types.DataSourceSchemaAttributeTag.CUSTOMER_EMAIL_ADDRESS;


/**
 * S - means subscriber
 */

@Slf4j
@AllArgsConstructor
public abstract class AbstractDataSync<C extends AbstractHttpConnection, S> {
    private static final Integer DEFAULT_BATCH_SIZE = 100;

    private static final String LIST_NAME_TEMPLATE = "DISTIL-%s";

    protected DestinationIntegrationDTO destinationIntegration;
    protected List<DTODataSourceAttributeExtended> attributes;
    protected SyncSettings syncSettings;
    protected C httpConnection;

    /**
     * @return list id
     */
    public abstract String createListIfNotExists();

    public abstract List<CustomAttributeDefinition> syncCustomAttributesSchema(String listId);

    protected abstract Set<String> retrieveCurrentEmails(String listId);

    protected String buildListName(Long destinationId) {
        return String.format(LIST_NAME_TEMPLATE, destinationId).toUpperCase().trim();
    }

    protected String buildCustomFieldName(String name, Long id) {
        return String.format("%s-%s", name, id);
    }

    protected List<DTODataSourceAttributeExtended> retrieveAllAttributesBySettings() {
        Integer defaultProductsCount = this.syncSettings.getDefaultProductsCount();

        return this.attributes.stream()
                .filter(attr -> !attr.getAutoGenerated() || attr.getPosition() <= defaultProductsCount)
                .collect(Collectors.toList());
    }

    protected abstract S getSubscriber();

    //    warn impure
    protected abstract void setEmail(S subscriber, String value);

    protected abstract String getEmailAddress(S subscriber);

    protected abstract void addCustomField(S subscriber, String fieldName, String value);

    protected abstract void sendSubscribers(String listId, List<S> subscribers);

    protected abstract void removeSubscribers(String listId, Collection<String> subscribersIds);


    public void ingestData(String listId, List<CustomAttributeDefinition> attributes, List<CustomerRecord> data) {

//        sort attributes first
        attributes.sort(Comparator.comparing(cad -> Optional.ofNullable(cad).map(CustomAttributeDefinition::getPosition).orElse(-1)));

        Map<DataSourceSchemaAttributeTag, Set<String>> attributesToBackfill = ListUtils.groupByToLinkedSet(this.attributes.stream()
                        .filter(DTODataSourceAttributeExtended::getAutoGenerated)
                        .sorted(Comparator.comparingInt(DTODataSourceAttributeExtended::getPosition)).collect(Collectors.toList()),
                DTODataSourceAttributeExtended::getAttributeDataTag, dsa -> String.valueOf(dsa.getId()));

        Set<String> currentSubscribers = retrieveCurrentEmails(listId);

        List<S> subscribers = new ArrayList<>();

        data.forEach(record -> {
            Optional.ofNullable(generateSubscriberData(attributes, record, attributesToBackfill)).ifPresent(subscriber -> {
                subscribers.add(subscriber);
                currentSubscribers.remove(this.getEmailAddress(subscriber));
            });

            if (subscribers.size() > DEFAULT_BATCH_SIZE) {
                sendSubscribers(listId, subscribers);
                subscribers.clear();
            }
        });

        if (subscribers.size() > 0) {
            sendSubscribers(listId, subscribers);
            subscribers.clear();
        }

        log.info("Subscribers to delete -> {}", currentSubscribers);

        removeSubscribers(listId, currentSubscribers);

    }

    protected S generateSubscriberData(List<CustomAttributeDefinition> attributes, CustomerRecord data, Map<DataSourceSchemaAttributeTag, Set<String>> fieldToBackfillByTags) {
        Set<String> attributesProcessed = new HashSet<>();

        ObjectNode customerValues = data.getCustomerValues();

        S result = getSubscriber();

        for (CustomAttributeDefinition attr : attributes) {

            String distilAttributeId = String.valueOf(attr.getDistilAttributeId());

            attributesProcessed.add(distilAttributeId);

            if (CUSTOMER_EMAIL_ADDRESS.equals(attr.getTag())) {
                this.setEmail(result, customerValues.get(distilAttributeId).asText());
            } else {
                Optional<JsonNode> jsonNode = Optional.ofNullable(customerValues.get(distilAttributeId));

                if (DataSourceType.PRODUCT.equals(attr.getTag().getDataSourceType()) && jsonNode.map(JsonNode::isNull).orElse(true)
                        && Optional.ofNullable(attr.getAutoGeneratedAttribute()).orElse(false)) {
                    Set<String> fieldsToBackfill = new HashSet<>(fieldToBackfillByTags.getOrDefault(attr.getTag(), Collections.emptySet()));
                    fieldsToBackfill.removeAll(attributesProcessed);

                    for (String s : fieldsToBackfill) {
                        attributesProcessed.add(s);
                        Optional<JsonNode> node = Optional.ofNullable(customerValues.get(s));

                        if (node.isPresent() && !node.get().isNull()) {
                            this.addCustomField(result, attr.getId(), node.get().asText());
                            break;
                        }
//                      this means that some auto generated required column can't be backfilled that's why we need to skip this consumer
                        return null;
                    }
                } else {
                    jsonNode.ifPresent(node -> this.addCustomField(result, attr.getId(), node.asText()));
                }
            }
        }

        return result;

    }


}
