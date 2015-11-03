/**
 * Creation Date: Aug 7, 2008
 */

package com.sandy.apps.pluto.biz.svc.job;
import java.text.ParseException ;
import java.util.Calendar ;
import java.util.Date ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;
import org.quartz.JobExecutionException ;

import com.sandy.apps.pluto.biz.svc.IEODImportSvc ;
import com.sandy.apps.pluto.biz.svc.IUserPreferenceSvc ;
import com.sandy.apps.pluto.shared.STUtils ;
import com.sandy.apps.pluto.shared.dto.JobAttribute ;
import com.sandy.apps.pluto.shared.dto.JobConfig ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.util.util.BizObjectFactory ;
import com.sandy.apps.pluto.shared.util.util.StringUtil ;

/**
 * This job downloads the bhavcopy reports and imports them into the database.
 * This job has two parts to it:
 * <ul>
 * <li>a) Every time it is triggered, it checks for the <b>latest.bhavcopy.date</b>
 *    job attribute and determines if it needs to download any
 *    latest (nearest to the present) bhavcopy.</li>
 * <li>b) It checks for the <b>oldest.bhavcopy.date</b> and downloads 'batchSize'
 *    number of bhavcopy before it relinquishes control.</li>
 * </ul>
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class NSEBhavcopyImportJob extends AbstractBaseJob {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( NSEBhavcopyImportJob.class ) ;

    /**
     * Configuration key for this job, which indicates the number of bhavcopies
     * to be downloaded in one iteration. The value of this key should be an integer
     * value.
     */
    private static String KEY_BATCH_SIZE = "batchSize" ;

    /**
     * A job attribute key to store the latest (nearest to present) date
     * for which the bhavcopy has been downloaded. The value of this key
     * should be in the dd-MM-yyyy format.
     */
    private static String KEY_LATEST_BHAVCOPY_DATE = "latest.bhavcopy.date" ;

    /**
     * A job attribute key to store the oldest (farthest in past) date
     * for which the bhavcopy has been downloaded. The value of this key
     * should be in the dd-MM-yyyy format.
     */
    private static String KEY_OLDEST_BHAVCOPY_DATE = "oldest.bhavcopy.date" ;

    /** Public constructor. */
    public NSEBhavcopyImportJob() {
        super() ;
    }

    /**
     * Imports the latest and oldest bhavcopy data. Latest implies newest data
     * which is in the future of the KEY_LATEST_BHAVCOPY_DATE key value. Oldest
     * implies older bhavcopy data which is in the past of the
     * KEY_OLDEST_BHAVCOPY_DATE key value.
     */
    @Override
    public void executeJob( final JobConfig jobCfg )
            throws JobExecutionException {

        logger.debug( "Executing bhavcopy download Job" ) ;

        try {
            importLatestBhavcopies( jobCfg ) ;
        }
        catch( final Exception e ) {
            logger.error( "Error downloading latest bhavcopy", e ) ;
            LogMsg.error( "Error downloading latest bhavcopy" ) ;
        }

        try {
            importOldestBhavcopies( jobCfg ) ;
        }
        catch( final Exception e ) {
            logger.error( "Error downloading oldest bhavcopy", e ) ;
            LogMsg.error( "Error downloading oldest bhavcopy" ) ;
        }
    }

    /**
     * Downloads at max numBhavcopyDwnload number of latest bhavcopies and
     * imports them into the persistent storage.
     *
     * @param latestBhavcopyDwnDt The last date (closest to present) when a
     *        bhavcopy was downloaded.
     *
     * @param numBhavcopyDwnload The number of bhavcopies to download in this
     *        iteration.
     *
     * @param jobCfg The attributes associated with this job
     */
    private void importLatestBhavcopies( final JobConfig jobCfg )
        throws ParseException {

        final JobAttribute latestDwnDtAttr = jobCfg.getAttributeValue( KEY_LATEST_BHAVCOPY_DATE ) ;
        final JobAttribute batchSizeAttr   = jobCfg.getAttributeValue( KEY_BATCH_SIZE ) ;

        // The number of bhavcopies to download in one go. By default it is 30
        int  numBhavcopyDwnload  = 30 ;
        if( StringUtil.isNotEmptyOrNull( batchSizeAttr.getValue() ) ) {
            numBhavcopyDwnload = Integer.parseInt( batchSizeAttr.getValue().trim() ) ;
        }

        final BizObjectFactory   objectFact  = ( BizObjectFactory )BizObjectFactory.getInstance() ;
        final IEODImportSvc      eodSvc      = ( IEODImportSvc )objectFact.getBean( "EODImportSvc" ) ;

        // Get today's date and truncate it such that it becomes 12:00:00 AM
        final Calendar today = STUtils.getToday() ;

        // From the latest date, coming towards today download the specified
        // number of bhavcopy. If the latest download date is not specified as
        // a job attribute, we set it to yesterday
        Date latestDwnDt = getDate( latestDwnDtAttr ) ;
        if( latestDwnDt == null ) {
            latestDwnDt = STUtils.getToday().getTime() ;
            latestDwnDt = DateUtils.addDays( latestDwnDt, -1 ) ;
        }

        logger.debug( "Importing latest bhavcopies from date = " +
                        IUserPreferenceSvc.DF.format( latestDwnDt ) + " for " +
                        numBhavcopyDwnload + " days " ) ;

        Date   date    = latestDwnDt ;
        int    numDwn  = 0 ;
        String dateStr = null ;

        try {
            while( numDwn < numBhavcopyDwnload && date.before( today.getTime() )) {
                dateStr = DF.format( date ) ;
                if( !isWeekend( date ) ) {
                    logger.info( "Importing bhavcopy for date " + dateStr ) ;
                    eodSvc.importBhavcopyEODData( date ) ;
                    numDwn ++ ;
                    LogMsg.info( "Bhavcopy downloaded for date " + dateStr ) ;
                }
                else {
                    logger.debug( "Skipping bhavcopy for date " + dateStr +
                                  " since it is a weekend") ;
                }

                date = DateUtils.addDays( date, 1 ) ;

                // Update the job attribute
                latestDwnDtAttr.setValue( DF.format( date ) ) ;
                super.updateJobAttribute( latestDwnDtAttr ) ;
            }
        }
        catch ( final Exception e ) {
            LogMsg.error( "Bhavcopy downloaded failure for date " + dateStr
                          + ". Msg " + e.getMessage() ) ;

            // If any of the iteration attempts fail, abort this job iteration
            // We don't want holes in the end of the day. The next iteration
            // of the job schedule will try to pick this up and if not manual
            // intervention would be required to set this job straight.
            logger.error( "Could not import latest bhavcopy data for date " +
                          dateStr + ". Msg = " + e.getMessage() ) ;
            logger.debug( "Latest bhavcopy import failure for date " + dateStr, e ) ;
        }
    }

    /**
     * Downloads at max numBhavcopyDwnload number of oldest bhavcopies and
     * imports them into the persistent storage.
     *
     * @param jobCfg The attributes associated with this job
     */
    private void importOldestBhavcopies(final JobConfig jobCfg )
        throws ParseException {

        final JobAttribute oldestDwnDtAttr = jobCfg.getAttributeValue( KEY_OLDEST_BHAVCOPY_DATE ) ;
        final Date         oldestDwnDt     = getDate( oldestDwnDtAttr ) ;

        // If we don't have an oldest date specified, we don't have to download
        // any pre historic bhavcopies, just return.
        if( oldestDwnDt == null ) {
            logger.info( "Not downloading any pre-historic data since the " +
                         "oldest.bhavcopy.date job attribute value is null" ) ;
            return ;
        }

        final JobAttribute batchSizeAttr = jobCfg.getAttributeValue( KEY_BATCH_SIZE ) ;
        int  numDownloads  = 30 ;
        if( StringUtil.isNotEmptyOrNull( batchSizeAttr.getValue() ) ) {
            numDownloads = Integer.parseInt( batchSizeAttr.getValue().trim() ) ;
        }

        logger.debug( "Importing oldest bhavcopies from date = " +
                      DF.format( oldestDwnDt ) + " for " + numDownloads + " days " ) ;

        final BizObjectFactory objectFact = ( BizObjectFactory )BizObjectFactory.getInstance() ;
        final IEODImportSvc    eodSvc     = ( IEODImportSvc )objectFact.getBean( "EODImportSvc" ) ;

        // From the oldest date, going backwards for the specified number of
        // downloads, keep downloading and importing the bhavcopy.
        Date date   = oldestDwnDt ;
        int  numDwn = 0 ;
        String dateStr = null ;

        try {
            while( numDwn < numDownloads ) {
                dateStr = DF.format( date ) ;
                if( !isWeekend( date ) ) {
                    logger.info( "Importing old bhavcopy for date " + dateStr ) ;
                    eodSvc.importBhavcopyEODData( date ) ;
                    numDwn ++ ;
                }
                else {
                    logger.debug( "Skipping old bhavcopy for date " + dateStr +
                                  " since it is a weekend") ;
                }

                // Move back one day
                date = DateUtils.addDays( date, -1 ) ;

                // Save the oldest bhavcopy date job attribute
                oldestDwnDtAttr.setValue( IUserPreferenceSvc.DF.format( date ) ) ;
                super.updateJobAttribute( oldestDwnDtAttr ) ;
            }
        }
        catch ( final Exception e ) {
            // If any of the iteration attempts fail, abort this job iteration
            // We don't want holes in the end of the day. The next iteration
            // of the job schedule will try to pick this up and if not manual
            // intervention would be required to set this job straight.
            logger.error( "Could not import oldest bhavcopy data for date " +
                          date + ". Msg = " + e.getMessage() ) ;
            logger.debug( "Oldest bhavcopy import failure for date " + date, e ) ;
        }
    }
}
