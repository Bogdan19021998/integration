package ai.distil.integration.configuration;

import ai.distil.integration.configuration.vo.DbConnectionProps;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Data
@Component
@ConfigurationProperties("ai.distil.integrations.connections.jdbc")
public class DbConnectionConfiguration {

    public static DbConnectionProps MY_SQL;
    public static DbConnectionProps POSTGRE_SQL;
    public static DbConnectionProps MS_SQL;
    public static DbConnectionProps REDSHIFT;

    public DbConnectionProps mySql;
    public DbConnectionProps postgreSql;
    public DbConnectionProps msSql;
    public DbConnectionProps redshift;


    @PostConstruct
    public void init() {
        MY_SQL = this.mySql;
        POSTGRE_SQL = this.postgreSql;
        MS_SQL = this.msSql;
        REDSHIFT = this.redshift;
    }

}
