package ai.distil.integration.ssh;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class KeySshConnectionParameters extends SshConnectionParameters {
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String keyPassphrase;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String key;

    public KeySshConnectionParameters(String serverAddress, int serverPort, String username, String keyPassphrase, String key) {
        super(serverAddress, serverPort);
        this.username = username;
        this.keyPassphrase = keyPassphrase;
        this.key = key;
    }

}