package ai.distil.integration.job.sync.http.campmon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscribers {
    List<Subscriber> subscribers;
}
