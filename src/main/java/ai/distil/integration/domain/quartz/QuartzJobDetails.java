package ai.distil.integration.domain.quartz;

import lombok.Data;
import org.hibernate.annotations.Immutable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Immutable
@Table(name = "qrtz_job_details")
@Data
public class QuartzJobDetails {

    private String schedName;
    @Id
    private String jobName;
    private String jobGroup;
    private String description;
    private String jobClassName;
    private Boolean isDurable;
    private Boolean isNonconcurrent;
    private Boolean isUpdateData;
    private Boolean requestsRecovery;
    private String jobData;
}
