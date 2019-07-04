package ai.distil.integration.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;
import java.util.UUID;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class SyncProgressTrackingDataEntity extends AbstractAuditEntity {
    private long currentTrackingTime;
    private long processed;
    private long created;
    private long updated;
    private long deleted;
    private long notChanged;
    private long currentRowsCount;
    private long beforeRowsCount;
    private Date startedDate;
    private Date finishedDate;
    @Id
    private UUID jobId;

}
