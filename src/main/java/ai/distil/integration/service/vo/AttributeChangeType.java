package ai.distil.integration.service.vo;

import ai.distil.api.internal.model.dto.DTODataSourceAttribute;

public enum AttributeChangeType {
    NOT_CHANGED,
    ADDED,
    DELETED,
    TYPE_CHANGED;

    public static AttributeChangeType defineAttributeType(DTODataSourceAttribute oldAttribute, DTODataSourceAttribute newAttribute) {
        if (newAttribute == null) {
            return AttributeChangeType.DELETED;
        }

        if (oldAttribute.getAttributeType().equals(newAttribute.getAttributeType())) {
            return AttributeChangeType.NOT_CHANGED;
        } else {
            return AttributeChangeType.TYPE_CHANGED;
        }
    }
}
