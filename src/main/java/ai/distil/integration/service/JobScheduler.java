package ai.distil.integration.service;

import ai.distil.integration.constants.JobPriority;
import ai.distil.integration.job.JobDefinitionEnum;
import ai.distil.integration.job.sync.request.IJobRequest;
import ai.distil.integration.service.sync.RequestMapper;
import ai.distil.integration.utils.JobUtils;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static ai.distil.integration.constants.JobConstants.JOB_REQUEST;
import static ai.distil.integration.constants.JobConstants.TASK_ID_KEY;
import static org.quartz.TriggerBuilder.newTrigger;

@Slf4j
@Service
public class JobScheduler {

    @Autowired
    private ApplicationContext context;

    @Qualifier("fastScheduler")
    @Autowired
    private SchedulerFactoryBean fastSchedulerFactoryBean;
    @Qualifier("slowScheduler")
    @Autowired
    private SchedulerFactoryBean slowSchedulerFactoryBean;

    @Autowired
    private RequestMapper requestMapper;

    @PostConstruct
    public void init() throws SchedulerException {
        fastSchedulerFactoryBean.getScheduler().start();
        slowSchedulerFactoryBean.getScheduler().start();
    }

    public boolean scheduleJob(JobDefinitionEnum jobDefinition, IJobRequest jobRequest) {

        log.debug("Scheduling new job: {}", jobDefinition);

        if (!jobDefinition.isRequestTypeCorrect(jobRequest)) {
            throw new IllegalStateException("Job request data type is incorrect, can't schedule a job.");
        }

        String jobKey = jobDefinition.getJobKey(jobRequest);
        String triggerKey = jobDefinition.getTriggerKey(jobRequest);
        String groupKey = jobDefinition.getGroup();

        JobDetail jobDetail = JobUtils.createJob(jobDefinition.getJob(), jobDefinition.isDurable(), context, jobKey, groupKey,
                buildJobDataMap(jobRequest));

        //      creating a trigger for run task periodically
        CronTrigger cronTrigger = newTrigger().forJob(jobKey, groupKey).withIdentity(triggerKey, groupKey)
                .withPriority(jobDefinition.getPriority())
                .withSchedule(CronScheduleBuilder.cronSchedule(jobDefinition.getDefaultCronExpression()))
                .build();

        log.debug("Creating trigger for key : {}", jobKey);

        try {
            Scheduler scheduler = slowSchedulerFactoryBean.getScheduler();
            if (scheduler.checkExists(new JobKey(jobKey, groupKey))) {
                scheduler.rescheduleJob(new TriggerKey(triggerKey, groupKey), cronTrigger);
            } else {
                scheduler.scheduleJob(jobDetail, cronTrigger);
            }
            log.debug("Job with key jobKey: {} and group : {} scheduled successfully", jobKey, groupKey);
            return true;
        } catch (SchedulerException e) {
            log.error("SchedulerException while scheduling job with key : {}", jobKey, e);
            //todo choose more appropriate exception
            throw new RuntimeException(e);
        }
    }

    public boolean scheduleOneTimeJobNow(JobDefinitionEnum jobDefinition, IJobRequest jobRequest) {

        String jobKey = String.format("ONE_TIME_%s", jobDefinition.getJobKey(jobRequest));
        String triggerKey = String.format("ONE_TIME_%s", jobDefinition.getTriggerKey(jobRequest));
        String groupKey = jobDefinition.getGroup();

        JobDetail jobDetail = JobUtils.createJob(jobDefinition.getJob(), jobDefinition.isDurable(),
                context, jobKey, groupKey,
                buildJobDataMap(jobRequest));

        Trigger cronTriggerBean = JobUtils.createSingleTrigger(triggerKey, null, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW, JobPriority.HIGH);

        try {

            log.info("Scheduling one time job - {}", jobKey);

            Scheduler scheduler = fastSchedulerFactoryBean.getScheduler();
            if(scheduler.checkExists(jobDetail.getKey())) {
                scheduler.triggerJob(jobDetail.getKey());
            } else {
                scheduler.scheduleJob(jobDetail, cronTriggerBean);
            }

            return true;
        } catch (SchedulerException e) {
            log.error("Something happen while scheduling one time job - {}", jobKey);

            //todo choose more appropriate exception
            throw new RuntimeException(e);
        }

    }

    public boolean startJobNow(JobDefinitionEnum jobDefinition, IJobRequest jobRequest) {
        String jobKey = jobDefinition.getJobKey(jobRequest);
        JobKey jKey = new JobKey(jobKey, jobDefinition.getGroup());

        JobDataMap jobDataMap = new JobDataMap(buildJobDataMap(jobRequest));

        try {
            slowSchedulerFactoryBean.getScheduler().triggerJob(jKey, jobDataMap);
            log.info("Job with jobKey : {} started now successfully.", jobKey);
            return true;
        } catch (SchedulerException e) {
            log.error("SchedulerException while triggering a job with key : {}", jobKey, e);
            //todo choose more appropriate exception
            throw new RuntimeException(e);
        }
    }

    public boolean deleteJobs(JobDefinitionEnum jobDefinitionEnum, Collection<? extends IJobRequest> jobRequests) {
        List<JobKey> jobsToDelete = jobRequests.stream()
                .map(v -> new JobKey(jobDefinitionEnum.getJobKey(v), jobDefinitionEnum.getGroup()))
                .collect(Collectors.toList());

        try {
            slowSchedulerFactoryBean.getScheduler().deleteJobs(jobsToDelete);
            return true;
        } catch (SchedulerException e) {
            log.warn("Unable to remove scheduled jobs.", e);
            return false;
        }
    }

    private Map<String, Object> buildJobDataMap(IJobRequest jobRequest) {
        return ImmutableMap.of(
                JOB_REQUEST, requestMapper.serialize(jobRequest),
                TASK_ID_KEY, UUID.randomUUID().toString());
    }
}
