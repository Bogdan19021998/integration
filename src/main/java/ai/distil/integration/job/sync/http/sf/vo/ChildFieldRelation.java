package ai.distil.integration.job.sync.http.sf.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChildFieldRelation {
    private Boolean cascadeDelete;
    private String childSObject;
    private Boolean deprecatedAndHidden;
    private String field;
//    list without type expected, not sure what sf returns to us
    private List junctionIdListNames;
//    list without type expected, not sure what sf returns to us
    private List junctionReferenceTo;
    private String relationshipName;
    private Boolean restrictedDelete;



}
