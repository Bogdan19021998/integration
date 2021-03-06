package ai.distil.integration.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseDestinationIntegrationRequest {
    @NotNull(message = "Org Id must be set")
    private Long orgId;
    @NotNull(message = "Tenant Id must be set")
    private String tenantId;
    @NotNull(message = "Integration id must be set")
    private Long integrationId;
}
