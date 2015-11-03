/**
 * Creation Date: Aug 20, 2008
 */

package com.sandy.apps.pluto.biz.svc;

import java.util.List ;

import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.dto.JobAttribute ;
import com.sandy.apps.pluto.shared.dto.JobConfig ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * This interface exposes operations for operating upon the scheduled jobs
 * in the application. The application can have many scheduled jobs for the
 * registered job types, for example scheduled intraday price leeching, EOD
 * bhavcopy leeching etc. The background scheduled job free the user from
 * having to keep track of repetitive tasks.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface IJobSvc {

    /** A set of enumerated values identifying the current state of a Job. */
    public static enum JobState {
        /** The job is in the process of being scheduled for execution. */
        STARTING,

        /** The job has been successfully scheduled for execution. */
        STARTED,

        /** The job is in the process of being stopped and unscheduled. */
        STOPPING,

        /** The job has been removed from the scheduler. */
        STOPPED,

        /** The job is currently executing. */
        EXECUTING
    } ;

    /**
     * A static constant identifying the key against which the job identifier
     * is stored in the scheduled jobs data map. The value of this attribute
     * is an Integer instance
     */
    String KEY_JOB_ID = "jobId" ;

    /**
     * Retrieves the state of the given job. If such a job is not registered
     * with the system, this method throws an instance of {@link STException}.
     *
     * @param jobId The job identifier, whose state is requested for
     * @return The state of the given job
     */
    JobState getJobState( final Integer jobId ) ;

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
    JobConfig getJobConfig( final Integer jobId )
        throws DataAccessException ;

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
    List<JobConfig> getJobConfigs() throws DataAccessException ;

    /**
     * Updates the specified attribute in the persistent storage.
     *
     * @param attribute The attribute to update.
     *
     * @throws DataAccessException In case of a data access exception.
     */
    void updateJobAttribute( final JobAttribute attribute )
        throws DataAccessException ;

    /**
     * Returns the number of job instances registered with the application.
     */
    int getNumRegisteredJobs() throws DataAccessException ;

    /**
     * Gets the execution state of the job whose identifier is passed as
     * parameter. This function returns a true if the job with the specified
     * identifier is currently active, false otherwise.
     */
    boolean isRunning( final Integer jobId ) ;

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
    void startJob( final Integer jobId ) ;

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
    void stopJob( final Integer jobId ) ;

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
    void restartJob( final Integer jobId ) ;

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
     * @param jobId The job to stop
     */
    void executeNow( Integer jobId ) ;

    /**
     * Shuts down the scheduler
     */
    void shutdownScheduler() throws STException ;
}
