/**
 * Creation Date: Aug 7, 2008
 */

package com.sandy.apps.pluto.biz.svc.job;
import java.text.DateFormat ;
import java.text.ParseException ;
import java.text.SimpleDateFormat ;
import java.util.Calendar ;
import java.util.Date ;

import org.apache.log4j.Logger ;
import org.quartz.Job ;
import org.quartz.JobDataMap ;
import org.quartz.JobExecutionContext ;
import org.quartz.JobExecutionException ;

import com.sandy.apps.pluto.biz.svc.IJobSvc ;
import com.sandy.apps.pluto.shared.ErrorCode ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.JobAttribute ;
import com.sandy.apps.pluto.shared.dto.JobConfig ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.util.util.StringUtil ;

/**
 * A base class for all Jobs in the system to bring in the feature of working
 * bands. Note that although we use the cron settings to run the jobs, we
 * suffer from the fundamental fact that jobs need to start and stop at
 * certain times and the fact that the application is going to run on a desktop
 * which is not a high availability machine. For instance, if we schedule a job
 * to start at 9:45 AM in the morning and run every 2 seconds - what happens if
 * we boot the machine at 10:30 AM? The job does not run for that day because
 * the start time is already gone.
 * <p>
 * To mitigate the fact, we will make all the tasks repetitive and control the
 * time band when we will pass the execution control to the real logic. If
 * the job is triggered outside the time band, the logic will not be invoked.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public abstract class AbstractBaseJob implements Job {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( AbstractBaseJob.class ) ;

    /** The time format for upper and lower bands. */
    private static final DateFormat BAND_DF = new SimpleDateFormat( "HH:mm:ss" ) ;

    /** The date format used to store user preference dates. */
    protected static final DateFormat DF = new SimpleDateFormat( "dd-MM-yyyy" ) ;

    /** Public constructor. */
    public AbstractBaseJob() {
        super() ;
    }

    /**
     * A final execute method which traps all the job invocations in a central
     * fashion and let's only the invocations which fall within the allowed
     * band pass to the real logic. Note that the time banding of a job is not
     * mandatory and if not specified allows all the job triggers to be passed
     * on to the concrete job logic.
     */
    @Override
    public final void execute( final JobExecutionContext context )
            throws JobExecutionException {

        final JobDataMap dataMap = context.getMergedJobDataMap() ;
        final Integer    jobId   = dataMap.getInt( IJobSvc.KEY_JOB_ID ) ;
        final JobConfig  jobCfg  = ServiceMgr.getJobSvc().getJobConfig( jobId ) ;

        final Calendar now   = Calendar.getInstance() ;
        final Date lowerBand = getLowerTimeBand( jobCfg ) ;
        final Date upperBand = getUpperTimeBand( jobCfg ) ;
        final String name    = jobCfg.getName() ;

        if( now.getTime().after( lowerBand ) ) {
            if( now.getTime().before( upperBand ) ) {
                if( networkCheckPassed( jobCfg ) ) {
                    LogMsg.info( "Executing scheduled job '" + name + "'" ) ;
                    executeJob( jobCfg ) ;
                }
                else {
                    logger.debug( "Skipping job '" + name + "' since it did not " +
                                  "pass the network check" ) ;
                }
            }
            else {
                logger.debug( "Skipping job '" + name + "' since it is triggered " +
                		      "after the upper time band" ) ;
            }
        }
        else {
            logger.debug( "Skipping job '" + name + "' since it is triggered " +
                          "before the lower time band" ) ;
        }
    }

    /**
     * A final execute method which executes the given job immediately without
     * any check for valid bounds. This method can be invoked on a job without
     * Quartz scaffolding.
     */
    public final void executeNow( final Integer jobId )
            throws JobExecutionException {

        final JobConfig jobCfg = ServiceMgr.getJobSvc().getJobConfig( jobId ) ;

        LogMsg.info( "Executing scheduled job '" + jobCfg.getName() + "'" ) ;
        executeJob( jobCfg ) ;
    }

    /**
     * Concrete implementation should implement this method to implement the
     * logic of job execution. This operation will be called if and only if
     * the trigger time falls between the configured time bands.
     *
     * @param jobCfg The job configuration for the job to be executed.
     *
     * @throws JobExecutionException If an exception is encountered during the
     *         execution of the task.
     */
    protected abstract void executeJob( final JobConfig jobCfg )
        throws JobExecutionException ;

    /**
     * Updates the specified job attribute in the persistent storage. It is
     * assumed that the attribute is still liked to its mother configuration
     * instance and hence the update of the job configuration is not
     * considered in this operation.
     *
     * @param attribute The job attribute to update.
     */
    protected void updateJobAttribute( final JobAttribute attribute ) {
        final IJobSvc jobSvc = ServiceMgr.getJobSvc() ;
        jobSvc.updateJobAttribute( attribute ) ;
    }

    /**
     * Returns a false if and only if the network.required configuration is set
     * to true and the network is not available at the instant of job invocation.
     */
    private boolean networkCheckPassed( final JobConfig cfg ) {

        boolean retVal = true ;
        final boolean netRequired = cfg.getJobDef().isNetworkReq() ;

        if( netRequired ) {
            if( !ServiceMgr.getNetworkSvc().isOnline() ) {
                retVal = false ;
            }
        }
        return retVal ;
    }

    /**
     * Gets the lower time band of this job, if specified, else returns 00:00:00:00
     * hours of the current date.
     *
     * @param jobCfg The job's configuration.
     * @return A Date instance.
     *
     * @throws JobExecutionException If the date format is incorrectly specified.
     */
    private Date getLowerTimeBand( final JobConfig jobCfg )
        throws JobExecutionException {

        final String time = jobCfg.getLowerTimeBand() ;
        Date bandTime = null ;

        try {
            final Calendar today = Calendar.getInstance() ;
            if( StringUtil.isEmptyOrNull( time ) ) {
                today.set( Calendar.HOUR_OF_DAY, 0 ) ;
                today.set( Calendar.MINUTE, 0 ) ;
                today.set( Calendar.SECOND, 0 ) ;
                today.set( Calendar.MILLISECOND, 0 ) ;
                bandTime = today.getTime() ;
            }
            else {
                final Calendar cal = Calendar.getInstance() ;
                cal.setTime( BAND_DF.parse( time ) ) ;
                cal.set( Calendar.YEAR,  today.get( Calendar.YEAR ) ) ;
                cal.set( Calendar.MONTH, today.get( Calendar.MONTH ) ) ;
                cal.set( Calendar.DATE,  today.get( Calendar.DATE ) ) ;
                bandTime = cal.getTime() ;
            }
        }
        catch (final Exception e) {
            throw new JobExecutionException( "Invalid lower band time " + time, false ) ;
        }

        return bandTime ;
    }

    /**
     * Gets the upper time band of this job, if specified, else returns
     * 23:59:59:59 AM of the current date.
     *
     * @param jobCfg The job's configuration.
     * @return A Date instance.
     *
     * @throws JobExecutionException If the date format is incorrectly specified.
     */
    private Date getUpperTimeBand( final JobConfig jobCfg )
        throws JobExecutionException  {

        final String time = jobCfg.getUpperTimeBand() ;
        Date bandTime = null ;

        try {
            final Calendar today = Calendar.getInstance() ;
            if( StringUtil.isEmptyOrNull( time ) ) {
                today.set( Calendar.HOUR_OF_DAY, 23 ) ;
                today.set( Calendar.MINUTE, 59 ) ;
                today.set( Calendar.SECOND, 59 ) ;
                today.set( Calendar.MILLISECOND, 999 ) ;
                bandTime = today.getTime() ;
            }
            else {
                final Calendar cal = Calendar.getInstance() ;
                cal.setTime( BAND_DF.parse( time ) ) ;
                cal.set( Calendar.YEAR,  today.get( Calendar.YEAR ) ) ;
                cal.set( Calendar.MONTH, today.get( Calendar.MONTH ) ) ;
                cal.set( Calendar.DATE,  today.get( Calendar.DATE ) ) ;
                bandTime = cal.getTime() ;
            }
        }
        catch (final Exception e) {
            throw new JobExecutionException( "Invalid upper band time " + time, false ) ;
        }

        return bandTime ;
    }

    /**
     * Converts the given string in the format "dd-MM-yyyy" to a Date instance.
     * In case of parsing exceptions, an error is logged and the parse exception
     * is percolated to the caller.
     *
     * @param value The value of the data as a string in the format "dd-MM-yyyy"
     *
     * @return A date instance or null if no value exits for the job attribute
     */
    protected Date getDate( final JobAttribute value )
        throws ParseException {

        Date retVal = null ;
        if( StringUtil.isNotEmptyOrNull( value.getValue() ) ) {
            try {
                retVal = DF.parse( value.getValue().trim() ) ;
            }
            catch ( final ParseException e ) {
                logger.error( "Could not parse value " + value +
                              " to a valid date", e ) ;
                throw e ;
            }
        }
        return retVal ;
    }

    /**
     * A helper function which given a calendar instance checks if this date
     * falls on a weekend.
     */
    protected boolean isWeekend( final Date date ) {
        boolean retVal = false ;
        final Calendar cal = Calendar.getInstance() ;
        cal.setTime( date ) ;
        final int dayOfWeek = cal.get( Calendar.DAY_OF_WEEK ) ;
        if( dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY ) {
            retVal = true ;
        }
        return retVal ;
    }

    /**
     * This helper method logs the exception appropriately, trying to minimize
     * the clutter in the log files. For example, if we know it's a network
     * error, there is no need to print the entire stack trace - just the
     * cause will suffice, since we take ample care to put in a very verbose
     * exception cause.
     *
     * @param baseMessage The base message to which the jist of the cause
     *        needs to be added.
     * @param cause The cause of the error, which needs to be logged.
     */
    protected void logError( final String baseMessage, final Throwable cause ) {

        boolean logged = false ;
        if( cause instanceof STException ) {
            final STException stEx = ( STException )cause ;
            if( stEx.getErrorCode() == ErrorCode.NETWORK_CONNECTION_FAILURE ||
                stEx.getErrorCode() == ErrorCode.NETWORK_UNAVAILABLE ) {

                logger.error( baseMessage + ". Reason - " + stEx.getMessage()  ) ;
                if( stEx.getCause() != null ) {
                    logger.error( "\tRoot cause - " + stEx.getCause().getMessage() ) ;
                }
                logged = true ;
            }
        }

        if( !logged ) {
            logger.error( baseMessage + ". Reason - " + cause.getMessage(), cause ) ;
        }
    }
}
