package ai.distil.integration.job.sync.http.sf.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SfField {
    private Boolean aggregatable;
    private Boolean aiPredictionField;
    private Boolean autoNumber;
    private Long byteLength;
    private Boolean calculated;
    private String calculatedFormula;
    private Boolean cascadeDelete;
    private Boolean caseSensitive;
    private String compoundFieldName;
    private String controllerName;
    private Boolean createable;
    private Boolean custom;
    private String defaultValue;
    private String defaultValueFormula;
    private Boolean defaultedOnCreate;
    private Boolean dependentPicklist;
    private Boolean deprecatedAndHidden;
    private Integer digits;
    private Boolean displayLocationInDecimal;
    private Boolean encrypted;
    private Boolean externalId;
    private String extraTypeInfo;
    private Boolean filterable;
    private Map<String, Object> filteredLookupInfo;
    private Boolean formulaTreatNullNumberAsZero;
    private Boolean groupable;
    private Boolean highScaleNumber;
    private Boolean htmlFormatted;
    private Boolean idLookup;
    private String inlineHelpText;
    private String label;
    private Integer length;
    private String mask;
//  not sure what is this
    private Object maskType;
    private String name;
    private Boolean nameField;
    private Boolean namePointing;
    private Boolean nillable;
    private Boolean permissionable;
//  list of custom objects, we don't need it for now 
    private List<Object> picklistValues;
    private Boolean polymorphicForeignKey;
    private Integer precision;
    private Boolean queryByDistance;
    private String referenceTargetField;
    private List<String> referenceTo;
    private String relationshipName;
//    not sure what is this
    private Object relationshipOrder;
    private Boolean restrictedDelete;
    private Boolean restrictedPicklist;
    private Integer scale;
    private Boolean searchPrefilterable;
    private String soapType;
    private Boolean sortable;
    private String type;
    private Boolean unique;
    private Boolean updateable;
    private Boolean writeRequiresMasterRead;
}
