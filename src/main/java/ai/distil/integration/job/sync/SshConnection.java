package ai.distil.integration.job.sync;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.iterator.IRowIterator;
import ai.distil.integration.job.sync.jdbc.SimpleDataSourceDefinition;
import ai.distil.integration.mapper.ConnectionMapper;
import ai.distil.integration.ssh.ConnectionParamsFactory;
import ai.distil.integration.ssh.SshConnectionParameters;
import ai.distil.integration.ssh.SshPortForwardConfig;
import ai.distil.integration.ssh.SshPortForwardThread;
import ai.distil.model.org.ConnectionSettings;

import java.util.List;

public class SshConnection extends AbstractConnection {

    private SshPortForwardThread sshPortForwardThread;

    private AbstractConnection connection;
    private DTOConnection sshConnectionData;
    private SshConnectionParameters sshConnectionParameters;

    public SshConnection(AbstractConnection connection, ConnectionMapper connectionMapper) {
        super(connection.getConnectionData());
        this.connection = connection;
        this.sshConnectionData = connectionMapper.copy(connection.getConnectionData());

        ConnectionSettings settings = this.sshConnectionData.getConnectionSettings();

        this.sshConnectionParameters = ConnectionParamsFactory.buildConnectionParams(settings);
        SshPortForwardConfig portForwardConfig = new SshPortForwardConfig(sshConnectionParameters,
                settings.getServerAddress(),
                Integer.parseInt(settings.getPort()));

//      setting up port forwarding, probably make sense to make it lazy
        this.sshPortForwardThread = new SshPortForwardThread(portForwardConfig);
        this.sshPortForwardThread.start();

//      override connection data after setting up the port
        sshConnectionData.getConnectionSettings().setServerAddress(this.sshPortForwardThread.getListenAddress());
        sshConnectionData.getConnectionSettings().setPort(String.valueOf(this.sshPortForwardThread.getListenPort()));

    }

    @Override
    public boolean isAvailable() {
        return connection.isAvailable();
    }

    @Override
    public List<DTODataSource> getAllDataSources() {
        return connection.getAllDataSources();
    }

    @Override
    public DTODataSource getDataSource(SimpleDataSourceDefinition dataSourceDefinition) {
        return connection.getDataSource(dataSourceDefinition);
    }

    @Override
    public IRowIterator getIterator(DataSourceDataHolder dataSources) {
        return connection.getIterator(dataSources);
    }

    @Override
    public boolean dataSourceExist(DataSourceDataHolder dataSource) {
        return connection.dataSourceExist(dataSource);
    }

    @Override
    public void close() throws Exception {
        this.connection.close();
        this.sshPortForwardThread.interrupt();
    }

    @Override
    public DTOConnection getConnectionData() {
        return this.sshConnectionData;
    }
}
