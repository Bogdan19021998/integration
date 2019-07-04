package ai.distil.integration.job.sync.request;

public interface IJobRequest {
    String DEFAULT_KEY_SEPARATOR = "_";

    String getKey();
}
