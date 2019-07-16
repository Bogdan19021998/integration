package ai.distil.integration.service.sync;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.SshConnection;
import ai.distil.integration.job.sync.http.campmon.CampaignMonitorHttpConnection;
import ai.distil.integration.job.sync.http.campmon.holder.CampaignMonitorFieldsHolder;
import ai.distil.integration.job.sync.http.mailchimp.MailChimpHttpConnection;
import ai.distil.integration.job.sync.http.mailchimp.holder.MailChimpMembersFieldsHolder;
import ai.distil.integration.job.sync.jdbc.MsSqlJdbcConnection;
import ai.distil.integration.job.sync.jdbc.MySqlJdbcConnection;
import ai.distil.integration.job.sync.jdbc.PostgreSqlJdbcConnection;
import ai.distil.integration.job.sync.jdbc.RedshiftSqlJdbcConnection;
import ai.distil.integration.mapper.ConnectionMapper;
import ai.distil.integration.service.RestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConnectionFactory {

    private final ConnectionMapper connectionMapper;
    private final RestService restService;
    private final MailChimpMembersFieldsHolder mailChimpMembersFieldsHolder;
    private final CampaignMonitorFieldsHolder campaignMonitorFieldsHolder;

    public AbstractConnection buildConnection(DTOConnection dtoConnection) {
        AbstractConnection abstractConnection = buildSimpleConnection(dtoConnection);

        if (dtoConnection.getConnectionSettings().isSsh_enabled()) {
            return new SshConnection(abstractConnection, connectionMapper);
        }

        return abstractConnection;
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
            default:
                throw new UnsupportedOperationException();
        }
    }
}
