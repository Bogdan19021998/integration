package ai.distil.integration.ssh;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SshPortForwardConfig {
    private SshConnectionParameters connectionParameters;
    private String destinationAddress;
    private int destinationPort;
}