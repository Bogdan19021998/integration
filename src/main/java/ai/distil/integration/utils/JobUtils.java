package ai.distil.integration.utils;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import java.util.Date;
import java.util.Map;

public class JobUtils {

    /**
     * Create a Single trigger.
     *
     * @param triggerName        Trigger name.
     * @param startTime          Trigger start time.
     * @param misFireInstruction Misfire instruction (what to do in case of misfire happens).
     * @return Trigger
     */
    public static Trigger createSingleTrigger(String triggerName, Date startTime, int misFireInstruction, int priority) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setName(triggerName);
        factoryBean.setStartTime(startTime);
        factoryBean.setMisfireInstruction(misFireInstruction);
        factoryBean.setRepeatCount(0);
        factoryBean.setPriority(priority);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    /**
     * Create Quartz Job.
     *
     * @param jobClass  Class whose executeInternal() method needs to be called.
     * @param isDurable Job needs to be persisted even after completion. if true, job will be persisted, not otherwise.
     * @param context   Spring application context.
     * @param jobName   Job name.
     * @param jobGroup  Job group.
     * @return JobDetail object
     */
    public static JobDetail createJob(Class<? extends QuartzJobBean> jobClass, boolean isDurable,
                                      ApplicationContext context, String jobName, String jobGroup, Map<String, Object> data) {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(jobClass);
        factoryBean.setDurability(isDurable);
        factoryBean.setApplicationContext(context);
        factoryBean.setName(jobName);
        factoryBean.setGroup(jobGroup);

        // set job data map
        JobDataMap jobDataMap = new JobDataMap(data);
        factoryBean.setJobDataMap(jobDataMap);

        factoryBean.afterPropertiesSet();

        return factoryBean.getObject();
    }

}
