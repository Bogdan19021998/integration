package ai.distil.integration.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class JobConfiguration {

    private final ApplicationContext applicationContext;
    private final DataSource dataSource;

    /**
     * create scheduler for slow running tasks (scheduled updates)
     */
    @Bean(name = "slowScheduler")
    public SchedulerFactoryBean slowSchedulerFactoryBean() throws IOException {

        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setOverwriteExistingJobs(true);
        factory.setDataSource(dataSource);
        factory.setQuartzProperties(quartzProperties(true));

        ContextAwareSpringBeanJobFactory jobFactory = new ContextAwareSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        factory.setJobFactory(jobFactory);

        return factory;
    }

    /**
     * create scheduler for slow running tasks (on going synchronizations)
     */
    @Bean(name = "fastScheduler")
    public SchedulerFactoryBean fastSchedulerFactoryBean() throws IOException {

        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setOverwriteExistingJobs(true);
        factory.setDataSource(dataSource);
        factory.setQuartzProperties(quartzProperties(false));

        ContextAwareSpringBeanJobFactory jobFactory = new ContextAwareSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        factory.setJobFactory(jobFactory);

        return factory;
    }

    /**
     * Configure quartz using properties file
     */
    private Properties quartzProperties(boolean slow) throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource(slow ? "./quartz-slow.properties" : "./quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }
}
