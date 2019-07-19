package ai.distil.integration.job.sync.http.sf.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesforceListFields {
    private List<FieldsActionOverride> actionOverrides;
    private Boolean activateable;
    private List<ChildFieldRelation> childRelationships;

    private Boolean compactLayoutable;
    private Boolean createable;
    private Boolean custom;
    private Boolean customSetting;
    private Boolean deletable;
    private Boolean deprecatedAndHidden;
    private Boolean feedEnabled;

    private List<SfField> fields;

    private Boolean hasSubtypes;
    private Boolean isSubtype;
    private String keyPrefix;
    private String label;
    private String labelPlural;
    private Boolean layoutable;
    private Boolean listviewable;
    private Object lookupLayoutable;
    private Boolean mergeable;
    private Boolean mruEnabled;
    private String name;
    private List<Object> namedLayoutInfos;
    private String networkScopeFieldName;
    private Boolean queryable;

    private List<RecordTypeInfo> recordTypeInfos;
    private Boolean replicateable;
    private Boolean retrieveable;
    private Boolean searchLayoutable;
    private Boolean searchable;

    private List<Scope> supportedScopes;

    private Boolean triggerable;
    private Boolean undeletable;
    private Boolean updateable;

    private Map<String, String> urls;
}
