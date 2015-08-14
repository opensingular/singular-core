package br.net.mirante.singular.flow.schedule.quartz;

import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import br.net.mirante.singular.flow.schedule.IScheduleData;
import br.net.mirante.singular.flow.schedule.ScheduleDataBuilder;
import br.net.mirante.singular.flow.schedule.ScheduledJob;

import static org.junit.Assert.*;

public class QuartzSchedulerFactoryTest {

    private static final Log LOGGER = LogFactory.getLog(QuartzSchedulerFactoryTest.class);

    private static final String SCHEDULER_NAME = "SingularFlowScheduler";
    private static final String SCHEDULER_INSTANCE_ID = "TEST";
    private static final String SCHEDULER_JOB_STORE = "org.quartz.simpl.RAMJobStore";

    private static final String JOB_GROUP = "groupTest";
    private static final String JOB_NAME = "jobTest";
    private static final String JOB_ID = "jobTestID";

    private static QuartzSchedulerFactory quartzSchedulerFactory;
    private static String jobRunResult;

    @Before
    public void setUp() throws Exception {
        quartzSchedulerFactory = new QuartzSchedulerFactory();
        quartzSchedulerFactory.setSchedulerName(SCHEDULER_NAME);
        quartzSchedulerFactory.setConfigLocation(ResourceBundle.getBundle("quartz"));
        jobRunResult = null;
    }

    @After
    public void tearDown() throws Exception {
        if (quartzSchedulerFactory != null) {
            quartzSchedulerFactory.destroy();
        }
    }

    @Test
    public void testStartScheduler() throws Exception {
        try {
            quartzSchedulerFactory.initialize();
            assertFalse(quartzSchedulerFactory.isRunning());
            quartzSchedulerFactory.start();
            assertTrue(quartzSchedulerFactory.isRunning());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testInitialize() throws Exception {
        try {
            quartzSchedulerFactory.initialize();
            quartzSchedulerFactory.start();

            Scheduler scheduler = quartzSchedulerFactory.getScheduler();
            assertEquals(SCHEDULER_NAME, scheduler.getSchedulerName());
            assertEquals(SCHEDULER_INSTANCE_ID, scheduler.getMetaData().getSchedulerInstanceId());
            assertEquals(SCHEDULER_JOB_STORE, scheduler.getMetaData().getJobStoreClass().getName());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void testAddJob() throws Exception {
        try {
            quartzSchedulerFactory.initialize();
            quartzSchedulerFactory.start();

            JobDetail jobDetail = JobBuilder.newJob().ofType(QuartzScheduledJob.class)
                    .withIdentity(JOB_NAME, JOB_GROUP).storeDurably().build();
            IScheduleData scheduleData = ScheduleDataBuilder.buildDaily(0, 0);
            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(configureJob(jobDetail, scheduleData)).withSchedule(
                            CronScheduleBuilder.cronSchedule(scheduleData.getCronExpression()))
                    .startNow().build();
            quartzSchedulerFactory.addJob(jobDetail);
            quartzSchedulerFactory.addTrigger(trigger, jobDetail);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testAddTrigger() throws Exception {
        try {
            quartzSchedulerFactory.initialize();
            quartzSchedulerFactory.start();

            JobDetail jobDetail = JobBuilder.newJob().ofType(QuartzScheduledJob.class)
                    .withIdentity(JOB_NAME, JOB_GROUP).storeDurably().build();
            IScheduleData scheduleData = ScheduleDataBuilder.buildDaily(0, 0);
            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(configureJob(jobDetail, scheduleData)).withSchedule(
                            CronScheduleBuilder.cronSchedule(scheduleData.getCronExpression()))
                    .startNow().build();
            quartzSchedulerFactory.addTrigger(trigger, jobDetail);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRegisterTrigger() throws Exception {
        try {
            JobDetail jobDetail = JobBuilder.newJob().ofType(QuartzScheduledJob.class)
                    .withIdentity(JOB_NAME, JOB_GROUP).storeDurably().build();
            Trigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail)
                    .startNow().build();
            quartzSchedulerFactory.setJobDetails(configureJob(jobDetail, null));
            quartzSchedulerFactory.setTriggers(trigger);

            quartzSchedulerFactory.setWaitForJobsToCompleteOnShutdown(true);
            quartzSchedulerFactory.initialize();
            quartzSchedulerFactory.start();

            assertNull(jobRunResult);
            Thread.sleep(500);
            assertNotNull(jobRunResult);
            assertEquals(JOB_ID, jobRunResult);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private JobDetail configureJob(JobDetail jobDetail, IScheduleData scheduleData) {
        jobDetail.getJobDataMap().put(QuartzJobFactory.JOB_KEY, new ScheduledJob(JOB_ID, scheduleData, () -> {
            jobRunResult = JOB_ID;
            return JOB_ID;
        }));
        return jobDetail;
    }
}