package ai.distil.integration.job.destination;

import ai.distil.integration.job.destination.vo.CustomAttributeDefinition;

import java.util.List;

public interface IDataSync {
    String LIST_NAME_TEMPLATE = "DISTIL-%s";


    /**
     * @return list id
     * */
    String createListIfNotExists();
    List<CustomAttributeDefinition> syncCustomAttributesSchema(String listId);
    void ingestData(String listId, List<CustomAttributeDefinition> attributes);

    default String buildListName(Long destinationId) {
        return String.format(LIST_NAME_TEMPLATE, destinationId).toUpperCase().trim();
    }

    default String buildCustomFieldName(String name, Long id) {
        return String.format("%s-%s", name, id);
    }
}
