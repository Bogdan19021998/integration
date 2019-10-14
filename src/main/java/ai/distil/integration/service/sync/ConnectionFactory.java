package ai.distil.integration.service.sync;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.datasource.DTODataSourceAttributeExtended;
import ai.distil.api.internal.model.dto.destination.DestinationIntegrationDTO;
import ai.distil.integration.job.destination.IDataSync;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.SshConnection;
import ai.distil.integration.job.sync.http.campmon.CampaignMonitorDataSync;
import ai.distil.integration.job.sync.http.campmon.CampaignMonitorHttpConnection;
import ai.distil.integration.job.sync.http.campmon.holder.CampaignMonitorFieldsHolder;
import ai.distil.integration.job.sync.http.mailchimp.MailChimpDataSync;
import ai.distil.integration.job.sync.http.mailchimp.MailChimpHttpConnection;
import ai.distil.integration.job.sync.http.mailchimp.holder.MailChimpMembersFieldsHolder;
import ai.distil.integration.job.sync.http.sf.SalesforceHttpConnection;
import ai.distil.integration.job.sync.http.sf.holder.SalesforceFieldsHolder;
import ai.distil.integration.job.sync.http.sync.SyncSettings;
import ai.distil.integration.job.sync.jdbc.MsSqlJdbcConnection;
import ai.distil.integration.job.sync.jdbc.MySqlJdbcConnection;
import ai.distil.integration.job.sync.jdbc.PostgreSqlJdbcConnection;
import ai.distil.integration.job.sync.jdbc.RedshiftSqlJdbcConnection;
import ai.distil.integration.mapper.ConnectionMapper;
import ai.distil.integration.service.RestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConnectionFactory {

    private final ConnectionMapper connectionMapper;
    private final RestService restService;
    private final MailChimpMembersFieldsHolder mailChimpMembersFieldsHolder;
    private final CampaignMonitorFieldsHolder campaignMonitorFieldsHolder;
    private final SalesforceFieldsHolder salesforceFieldsHolder;

    public AbstractConnection buildConnection(DTOConnection dtoConnection) {
        AbstractConnection abstractConnection = buildSimpleConnection(dtoConnection);

        if (dtoConnection.getConnectionSettings().isSsh_enabled()) {
            return new SshConnection(abstractConnection, connectionMapper);
        }

        return abstractConnection;
    }


    public IDataSync buildDataSync(DTOConnection connection, DestinationIntegrationDTO integration, SyncSettings settings, List<DTODataSourceAttributeExtended> attributes) {
        switch (connection.getConnectionType()) {
            case CAMPAIGN_MONITOR:
                return new CampaignMonitorDataSync(connection, integration, restService, campaignMonitorFieldsHolder, attributes, settings);
            case MAILCHIMP:
                return new MailChimpDataSync(connection, integration, restService, mailChimpMembersFieldsHolder);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private AbstractConnection buildSimpleConnection(DTOConnection connection) {
        switch (connection.getConnectionType()) {
            case MYSQL:
                return new MySqlJdbcConnection(connection);
            case POSTGRESQL:
                return new PostgreSqlJdbcConnection(connection);
            case SQLSERVER:
                return new MsSqlJdbcConnection(connection);
            case REDSHIFT:
                return new RedshiftSqlJdbcConnection(connection);
            case MAILCHIMP:
                return new MailChimpHttpConnection(connection, restService, mailChimpMembersFieldsHolder);
            case CAMPAIGN_MONITOR:
                return new CampaignMonitorHttpConnection(connection, restService, campaignMonitorFieldsHolder);
            case SALESFORCE:
                return new SalesforceHttpConnection(connection, restService, salesforceFieldsHolder);
            default:
                throw new UnsupportedOperationException();
        }
    }
}
