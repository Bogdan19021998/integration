package ai.distil.integration.job;

import ai.distil.integration.constants.JobPriority;
import ai.distil.integration.job.sync.request.IJobRequest;
import ai.distil.integration.job.sync.request.SyncConnectionRequest;
import ai.distil.integration.job.sync.request.SyncDataSourceRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.scheduling.quartz.QuartzJobBean;

@AllArgsConstructor
public enum JobDefinitionEnum {
    SYNC_DATASOURCE("SYNC_DATASOURCE", true, JobPriority.LOW, "0 0 0 * * ?", SyncDataSourceJob.class, SyncDataSourceRequest.class),
    SYNC_CONNECTION("SYNC_CONNECTION", true, JobPriority.HIGH, "0 0 0 * * ?", SyncConnectionJob.class, SyncConnectionRequest.class);

    private String name;
    @Getter
    private boolean durable;
    @Getter
    private int priority;
    @Getter
    private String defaultCronExpression;
    @Getter
    private Class<? extends QuartzJobBean> job;

    @Getter
    private Class<? extends IJobRequest> jobRequestClazz;

    public String getJobKey(IJobRequest request) {
        return request.getKey() == null ? name : String.format("%s_%s", name, request.getKey());
    }

    public String getTriggerKey(IJobRequest request) {
        return String.format("%s_TRIGGER_%s", this.name, request.getKey());
    }

    public String getGroup() {
        return String.format("%s_GROUP", this.name);
    }

    public <T extends IJobRequest> boolean isRequestTypeCorrect(T request) {
        return this.jobRequestClazz.isInstance(request);
    }
}
