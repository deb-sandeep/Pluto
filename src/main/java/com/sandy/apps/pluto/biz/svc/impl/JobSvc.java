/**
 * Creation Date: Aug 20, 2008
 */

package com.sandy.apps.pluto.biz.svc.impl;
import java.text.ParseException ;
import java.util.List ;
import java.util.Vector ;

import org.apache.log4j.Logger ;
import org.quartz.CronTrigger ;
import org.quartz.JobDataMap ;
import org.quartz.JobDetail ;
import org.quartz.JobExecutionContext ;
import org.quartz.JobExecutionException ;
import org.quartz.JobListener ;
import org.quartz.Scheduler ;
import org.quartz.SchedulerException ;
import org.quartz.impl.StdSchedulerFactory ;

import com.sandy.apps.pluto.biz.dao.IJobDAO ;
import com.sandy.apps.pluto.biz.svc.IJobSvc ;
import com.sandy.apps.pluto.biz.svc.job.AbstractBaseJob ;
import com.sandy.apps.pluto.shared.ErrorCode ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.Initializable ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.JobAttribute ;
import com.sandy.apps.pluto.shared.dto.JobConfig ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;
import com.sandy.apps.pluto.shared.util.util.ReflectionUtil ;

/**
 * Implementation of {@link IJobSvc} interface. This implementation class also
 * takes care of initializing and sequencing the scheduled jobs on startup.
 * This is a singleton class and is required to be loaded via the DI container.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class JobSvc implements IJobSvc, Initializable, JobListener {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( JobSvc.class ) ;

    /** The group name to which all the task jobs belong. */
    private static final String JOB_GROUP_NAME = "STJobGroup" ;

    /** The group name to which all the triggers belong. */
    private static final String TRIGGER_GROUP_NAME = "STTriggerGroup" ;

    /** The job name prefix. */
    private static final String JOB_NAME_PREFIX = "Job" ;

    /** The trigger name prefix. */
    private static final String TRIGGER_NAME_PREFIX = "Trigger" ;

    /** Automatic startup type.*/
    public static final String STARTUP_AUTO = "AUTO" ;

    /** Manual startup type */
    public static final String STARTUP_MANUAL = "MANUAL" ;

    /** The quartz scheduler to use. */
    private Scheduler scheduler = null ;

    /** A place holder for job identifiers which are being started. */
    private final Vector<Integer> startingJobIdList = new Vector<Integer>() ;

    /** A place holder for job identifiers which are being stopped. */
    private final Vector<Integer> stoppingJobIdList = new Vector<Integer>() ;

    /** A place holder for job identifiers which are executing. */
    private final Vector<Integer> executingJobIdList = new Vector<Integer>() ;

    /**
     * INJECTABLE. This variable should be injected with a reference to the
     * IJobDAO implementation before this instance is initialized.
     */
    private IJobDAO jobDAO = null ;
    public IJobDAO getJobDAO() { return this.jobDAO ; }
    public void setJobDAO( final IJobDAO jobDAO ) { this.jobDAO = jobDAO ; }

    /** Public constructor. */
    public JobSvc() {
        super() ;
    }

    // ---------------------- INITIALIZATION BEGINS --------------------------
    /**
     * Initializes the Job service by loading all the registered job configurations
     * and starting the job scheduler with those jobs whose startup type is
     * set to AUTO. This method is invoked by the container (Spring)
     *
     * @throws STException If an exception is encountered during the initialization
     *         process.
     */
    @Override
    public void initialize() throws STException {
        logger.info( "Initializing Job service..." ) ;
        try {
            // Create the scheduler and add this instance as the job listener
            // so that it can track the tasks being executed.
            this.scheduler = StdSchedulerFactory.getDefaultScheduler() ;
            this.scheduler.addGlobalJobListener( this ) ;

            // Get a list of all the registered task configurations and schedule
            // the auto startup jobs
            final List<JobConfig> jobCfgs = this.jobDAO.getJobConfigs() ;
            for( final JobConfig cfg : jobCfgs ) {
                if( cfg.getStartupType().trim().equals( STARTUP_AUTO ) ) {
                    scheduleJobConfig( cfg ) ;
                }
                else {
                    logger.debug( "Job " + cfg.getName() + " not scheduled " +
                                  "because it is of manual startup type" ) ;
                }
            }
            logger.info( "Starting the job scheduler after 5 seconds from now" ) ;
            this.scheduler.startDelayed( 5 ) ;
        }
        catch ( final Exception e ) {
            logger.error( "Could not start job scheduler", e ) ;
            throw new STException( "Job scheduler could not be started", e,
                                   ErrorCode.INIT_FAILURE ) ;
        }
    }

    /**
     * Schedules a job corresponding to the specified job configuration. It is
     * assumed that the specified job configuration is of a auto startup type.
     *
     * @param cfg The job configuration for which a Quartz job has to be scheduled.
     *
     * @throws SchedulerException If an exception is encountered while scheduling
     *         the job.
     * @throws ParseException
     */
    private void scheduleJobConfig( final JobConfig cfg )
        throws SchedulerException, ParseException {

        try {
            // Add the job id to the list of jobs in the process of being started.
            this.startingJobIdList.add( cfg.getJobId() ) ;

            logger.debug( "Scheduling job " + cfg.getName() ) ;
            final String jobName     = JOB_NAME_PREFIX + cfg.getJobId() ;
            final String triggerName = TRIGGER_NAME_PREFIX + cfg.getJobId() ;

            // Check if a job with the same name is already registered with the
            // scheduler. If so, delete it before scheduling a new job of the
            // same name
            JobDetail jobDetail = this.scheduler.getJobDetail( jobName, JOB_GROUP_NAME ) ;
            if( jobDetail != null ) {
                this.scheduler.pauseJob( jobName, JOB_GROUP_NAME ) ;
                this.scheduler.deleteJob( jobName, JOB_GROUP_NAME ) ;
            }

            // Create a new job detail and register it with the scheduler
            jobDetail = new JobDetail() ;
            jobDetail.setName( jobName ) ;
            jobDetail.setGroup( JOB_GROUP_NAME ) ;
            jobDetail.setJobClass( cfg.getJobDef().getClassType() ) ;
            jobDetail.setDescription( cfg.getName() ) ;

            // Create a data map for the Quartz task. Note that we put only the
            // Job identifier in the data map and nothing else. The jobs in this
            // system do not rely upon the data map values but rather on the
            // job configuration which they can directly fetch from the job ID.
            // This is so because Quartz plays dirty with the job data map attributes
            // and doesn't allow mutation of these values across invocations.
            final JobDataMap dataMap = jobDetail.getJobDataMap() ;
            dataMap.put( KEY_JOB_ID, cfg.getJobId() ) ;

            // Register the job and start it if the job is set to automatic.
            final String cron = cfg.getCron() ;
            final CronTrigger trigger = new CronTrigger( triggerName, TRIGGER_GROUP_NAME, cron ) ;
            trigger.setMisfireInstruction( CronTrigger.MISFIRE_INSTRUCTION_SMART_POLICY ) ;
            this.scheduler.scheduleJob( jobDetail, trigger ) ;
            logger.debug( "\tCron = " + cron ) ;

            // Publish an event notifying that the job has been scheduled
            EventBus.publish( EventType.JOB_STARTED, cfg ) ;
        }
        finally {
            // Remove the job id from the list of jobs being started. At this
            // State either the job has been successfully scheduled or stopped.
            this.startingJobIdList.remove( cfg.getJobId() ) ;
        }
    }
    // ---------------------- INITIALIZATION ENDS --------------------------

    /**
     * Retrieves the job configuration for the given job identifier.
     *
     * @param jobId The unique job identifier for the job.
     * @return A {@link JobConfig} instance or null if no job configuration
     *         exists for the given type.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         database operation.
     */
    public JobConfig getJobConfig( final Integer jobId )
        throws DataAccessException {
        return this.jobDAO.getJobConfig( jobId ) ;
    }

    /**
     * Retrieves a list of all {@link JobConfig} registered in the system.
     *
     * @return A list of {@link JobConfig} instances registered in the system.
     *         If no job configurations are registered, an empty list is
     *         returned, never null.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         database operation.
     */
    public List<JobConfig> getJobConfigs() throws DataAccessException {
        return this.jobDAO.getJobConfigs() ;
    }

    /**
     * Updates the specified attribute in the persistent storage.
     *
     * @param attribute The attribute to update.
     *
     * @throws DataAccessException In case of a data access exception.
     */
    public void updateJobAttribute( final JobAttribute attribute )
        throws DataAccessException {
        this.jobDAO.updateJobAttribute( attribute ) ;
    }

    /**
     * Returns the number of job instances registered with the application.
     */
    public int getNumRegisteredJobs() throws DataAccessException {
        return this.jobDAO.getJobConfigs().size() ;
    }

    /**
     * Retrieves the state of the given job. If such a job is not registered
     * with the system, this method throws an instance of {@link STException}.
     *
     * @param jobId The job identifier, whose state is requested for
     * @return The state of the given job
     */
    public JobState getJobState( final Integer jobId ) {

        JobState state = JobState.STOPPED ;

        if( this.startingJobIdList.contains( jobId ) ) {
            state = JobState.STARTING ;
        }
        else if( this.executingJobIdList.contains( jobId ) ) {
            state = JobState.EXECUTING ;
        }
        else if( this.stoppingJobIdList.contains( jobId ) ) {
            state = JobState.STOPPING ;
        }
        else if( isRunning( jobId ) ) {
            state = JobState.STARTED ;
        }
        else {
            state = JobState.STOPPED ;
        }

        return state ;
    }

    /**
     * Gets the execution state of the job whose identifier is passed as
     * parameter. This function returns a true if the job with the specified
     * identifier is currently active, false otherwise.
     */
    public boolean isRunning( final Integer jobId ) {
        boolean running = false ;
        try {
            final String[] jobNames = this.scheduler.getJobNames( JOB_GROUP_NAME ) ;
            for( final String jobName : jobNames ) {
                if( jobName.equals( JOB_NAME_PREFIX + jobId ) ) {
                    running = true ;
                    break ;
                }
            }
        }
        catch ( final SchedulerException e ) {
            logger.error( "Unanticipated exception", e ) ;
        }
        return running ;
    }

    /**
     * Starts the job whose identifier is passed as parameter. An automatic
     * startup job is started (scheduled) automatically during application
     * startup. However, the user can start/stop any job at runtime through
     * the task management UI. If the specified job is already running, this
     * method does nothing.
     * <p>
     * This method publishes the following events during the processing.
     * <ul>
     *  <li>{@link EventType#JOB_STARTING} This event is published as soon
     *      as the async job is submitted for execution</li>
     *  <li>{@link EventType#JOB_STARTED} This event is published after the
     *      job has been successfully scheduled for execution</li>
     *  <li>{@link EventType#JOB_STOPPED} This event is published if the job
     *      could not be scheduled for execution</li>
     * </ul>
     *
     * @param jobId The job to start
     */
    public void startJob( final Integer jobId ) {
        logger.debug( "Starting job " + jobId ) ;
        if( isRunning( jobId ) ) {
            logger.debug( "Job " + jobId + " is already running." ) ;
            return ;
        }

        final JobConfig config = this.jobDAO.getJobConfig( jobId ) ;

        // Publish the event, notifying that the job is starting.
        EventBus.publish( EventType.JOB_STARTING, config ) ;
        LogMsg.info( "Starting job " + config.getName() ) ;
        try {
            scheduleJobConfig( config ) ;
        }
        catch ( final Exception e ) {
            // If for some reason, we could not start the job, publish an
            // event notifying that the job was stopped
            logger.error( "Job " + jobId + " could not be started", e ) ;
            EventBus.publish( EventType.JOB_STOPPED, config ) ;
            LogMsg.error( "Job " + config.getName() + " could not be started" ) ;
        }
    }

    /**
     * Stops the job whose identifier is passed as parameter. If the specified
     * job is already not running, this method does nothing.
     * <p>
     * This method publishes the following events during the processing.
     * <ul>
     *  <li>{@link EventType#JOB_STOPPING} This event is published as soon
     *      as the async job is submitted for execution</li>
     *  <li>{@link EventType#JOB_STOPPED} This event is published after the
     *      job has been successfully removed from the scheduler</li>
     * </ul>
     *
     * @param jobId The job to stop
     */
    public void stopJob( final Integer jobId ) {

        if( getJobState( jobId ) == JobState.STOPPED ) {
            logger.debug( "Job " + jobId + " has been already stopped." ) ;
            return ;
        }

        final JobConfig config = this.jobDAO.getJobConfig( jobId ) ;

        // Publish the event, notifying that the job is starting.
        EventBus.publish( EventType.JOB_STOPPING, config ) ;

        // Add the job id to the list of jobs which are in the process of
        // being stopped. This is to track the state of the jobs effectively.
        this.stoppingJobIdList.add( jobId ) ;

        LogMsg.info( "Stopping job " + config.getName() ) ;
        try {
            // Check if a job with the same name is already registered with the
            // scheduler. If so, delete it before scheduling a new job of the
            // same name
            final String jobName = JOB_NAME_PREFIX + jobId ;
            final JobDetail jobDetail = this.scheduler.getJobDetail( jobName, JOB_GROUP_NAME ) ;
            if( jobDetail != null ) {
                this.scheduler.pauseJob( jobName, JOB_GROUP_NAME ) ;
                this.scheduler.deleteJob( jobName, JOB_GROUP_NAME ) ;
            }
            EventBus.publish( EventType.JOB_STOPPED, config ) ;
        }
        catch ( final Exception e ) {
            // If for some reason, we could not start the job, publish an
            // event notifying that the job was stopped
            logger.error( "Job " + jobId + " could not be stopped", e ) ;
            LogMsg.error( "Job " + config.getName() + " could not be stopped" ) ;
            EventBus.publish( EventType.JOB_STARTED, config ) ;
        }
        finally {
            // Remove the job from the stopping list.
            this.stoppingJobIdList.remove( jobId ) ;
        }
    }

    /**
     * Stops the and starts the job whose identifier is passed as parameter.
     * If the specified job is already not running, this method does nothing.
     * If the job is running, it is first stopped and then started again.
     * <p>
     * This method publishes the following events during the processing.
     * <ul>
     *  <li>{@link EventType#JOB_STOPPING} This event is published as soon
     *      as the async job is submitted for execution</li>
     *  <li>{@link EventType#JOB_STOPPED} This event is published after the
     *      job has been successfully removed from the scheduler</li>
     *  <li>{@link EventType#JOB_STARTING} This event is published as soon
     *      as the async job is submitted for execution</li>
     *  <li>{@link EventType#JOB_STARTED} This event is published after the
     *      job has been successfully scheduled for execution</li>
     *  <li>{@link EventType#JOB_STOPPED} This event is published if the job
     *      could not be scheduled for execution</li>
     * </ul>
     *
     * @param jobId The job to stop
     */
    public void restartJob( final Integer jobId ) {
        logger.debug( "Restarting job " + jobId ) ;
        stopJob( jobId ) ;
        startJob( jobId ) ;
    }

    /**
     * Executes the specified job once and immediately. The job is returned to
     * it's initial state as soon as it completes execution.
     * <p>
     * This method publishes the following events during the processing.
     * <ul>
     *  <li>{@link EventType#JOB_EXECUTING} This event is published as soon
     *      as the async job is submitted for execution</li>
     * </ul>
     *
     * @param jobId The job to execute immediately
     */
    public void executeNow( final Integer jobId ) {

        final JobConfig jobCfg = this.jobDAO.getJobConfig( jobId ) ;
        try {
            final String jobClass = jobCfg.getJobDef().getClassName() ;
            final AbstractBaseJob job = ( AbstractBaseJob )ReflectionUtil.createInstance( jobClass ) ;

            this.executingJobIdList.add( jobId ) ;
            EventBus.publish( EventType.JOB_EXECUTING, jobCfg ) ;
            job.executeNow( jobId ) ;
        }
        catch ( final Exception e ) {
            LogMsg.error( "Error executing job " + jobCfg.getName() +
                          ". Msg = " + e.getMessage() ) ;
        }
        finally {
            this.executingJobIdList.remove( jobId ) ;
            final JobState state = ServiceMgr.getJobSvc().getJobState( jobId ) ;
            if( state == JobState.STARTED ) {
                EventBus.publish( EventType.JOB_STARTED, jobCfg ) ;
            }
            else if( state == JobState.STARTING ) {
                EventBus.publish( EventType.JOB_STARTING, jobCfg ) ;
            }
            else if( state == JobState.STOPPING ) {
                EventBus.publish( EventType.JOB_STOPPING, jobCfg ) ;
            }
            else if( state == JobState.STOPPED ) {
                EventBus.publish( EventType.JOB_STOPPED, jobCfg ) ;
            }
            else if( state == JobState.EXECUTING ) {
                EventBus.publish( EventType.JOB_EXECUTING, jobCfg ) ;
            }
        }
    }

    /** Returns the name of this job listener. */
    @Override
    public String getName() {
        return "Pluto global Job Listener" ;
    }

    /**
     * This is called if the job was about to be executed, but some other
     * trigger listener vetoed its execution. Such a situation will not occur
     * in Pluto and hence this method is a NO OP.
     */
    @Override
    public void jobExecutionVetoed( final JobExecutionContext context ) {
        // NO OP
    }

    /**
     * This is invoked when the job is about to be executed. We take this
     * opportunity to add the job to the executing list, so that the state
     * can be successfully tracked.
     */
    @Override
    public void jobToBeExecuted( final JobExecutionContext context ) {
        final int jobId = context.getMergedJobDataMap().getInt( KEY_JOB_ID ) ;
        this.executingJobIdList.add( new Integer( jobId ) ) ;
        EventBus.publish( EventType.JOB_EXECUTING, this.jobDAO.getJobConfig( jobId ) ) ;
    }

    /**
     * This is invoked when the job has finished execution. We take this
     * opportunity to remove the job from the executing list, so that the state
     * can be successfully tracked.
     */
    @Override
    public void jobWasExecuted( final JobExecutionContext context,
                                final JobExecutionException jobException ) {
        final int jobId = context.getMergedJobDataMap().getInt( KEY_JOB_ID ) ;
        this.executingJobIdList.remove( new Integer( jobId ) ) ;
        EventBus.publish( EventType.JOB_STARTED, this.jobDAO.getJobConfig( jobId ) ) ;
    }

    /**
     * Shuts down the scheduler
     */
    public void shutdownScheduler() throws STException {
        try {
            this.scheduler.shutdown() ;
        }
        catch (final SchedulerException e) {
            throw new STException( "Unable to shutdown scheduler", e,
                                   ErrorCode.UNKNOWN_EXCEPTION ) ;
        }
    }
}
