package ai.distil.integration.ssh;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class SshConnectionParameters {
    private String serverAddress;
    private int serverPort;
}
