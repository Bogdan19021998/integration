package ai.distil.integration.configuration;

import ai.distil.integration.configuration.vo.ConnectionProps;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Data
@Component
@ConfigurationProperties("ai.distil.integrations.connections")
public class ConnectionConfiguration {

    public static ConnectionProps MY_SQL;
    public static ConnectionProps POSTGRE_SQL;
    public static ConnectionProps MS_SQL;
    public static ConnectionProps REDSHIFT;

    public ConnectionProps mySql;
    public ConnectionProps postgreSql;
    public ConnectionProps msSql;
    public ConnectionProps redshift;


    @PostConstruct
    public void init() {
        MY_SQL = this.mySql;
        POSTGRE_SQL = this.postgreSql;
        MS_SQL = this.msSql;
        REDSHIFT = this.redshift;
    }

}
