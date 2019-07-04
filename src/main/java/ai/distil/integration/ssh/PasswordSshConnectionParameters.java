package ai.distil.integration.ssh;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PasswordSshConnectionParameters extends SshConnectionParameters {
    public String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String password;

    public PasswordSshConnectionParameters(String serverAddress, int serverPort, String username, String password) {
        super(serverAddress, serverPort);
        this.username = username;
        this.password = password;
    }

}
