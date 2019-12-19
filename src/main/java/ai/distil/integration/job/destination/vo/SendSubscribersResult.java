package ai.distil.integration.job.destination.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendSubscribersResult {
    private Set<String> failedSubscribers;
    private Long successCount;
}
