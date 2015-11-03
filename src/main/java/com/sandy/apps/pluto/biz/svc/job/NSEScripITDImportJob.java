/**
 * Creation Date: Aug 21, 2008
 */

package com.sandy.apps.pluto.biz.svc.job;
import java.text.DateFormat ;
import java.text.ParseException ;
import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.quartz.JobExecutionException ;

import com.sandy.apps.pluto.biz.svc.IAsyncExecutorSvc ;
import com.sandy.apps.pluto.biz.svc.IAsyncExecutorSvc.AsyncTask ;
import com.sandy.apps.pluto.biz.svc.IITDImportSvc ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.JobAttribute ;
import com.sandy.apps.pluto.shared.dto.JobConfig ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.util.util.StringUtil ;

/**
 * This class helps import NSE intraday quotes for the registered scrips. Many
 * scrips can be registered against an instance of this job, by using an
 * indexed job configuration variable named 'scrip'. Each of the scrip values
 * will be fetched asynchronously at the scheduled time of this job.
 * <p>
 * Please note that this job does not enrich the data with past high resolution
 * values. So, if a cycle is missed or on an exception, the value for that
 * instant will be lost.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class NSEScripITDImportJob extends AbstractBaseJob {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( NSEScripITDImportJob.class ) ;

    /** The attribute key against which the indexed scrips are stored. */
    public static final String SCRIP_ATTR_VAL = "scrip" ;

    /**
     * The key against which the minimum time interval for enriching ITD data
     * is stored. This job will try to enrich (fill in with high resolution)
     * ITD data after the value of this key. The value of this key should be
     * specified in seconds.
     */
    public static final String SCRIP_ENRICH_INTERVAL = "scrip.enrich.interval" ;

    /** The default enrich time interval of one hour. */
    private static final int DEF_ENRICH_INTERVAL = 60*60 ;

    // The timestamp format in which the last enrich time is stored.
    private static final DateFormat TIMESTAMP_FMT = new SimpleDateFormat( "dd-MM-yyyy HH:mm:ss" ) ;

    // ------START-- Asynchronous task for scrip ITD leeching ------------------
    /**
     * This class represents the asynchronous task which will import the ITD
     * value for the specified scrip. This task is hardned such that it will
     * not propagate the exception beyond itself.
     */
    private class AsyncScripITDImportTask implements AsyncTask {

        private JobAttribute symbol = null ;
        private int enrichInterval  = -1 ;
        private Date lastEnrichTime = null ;

        /** Constructor. */
        public AsyncScripITDImportTask( final JobAttribute symbol,
                                        final int enrichInterval ) {
            this.symbol = symbol ;
            this.enrichInterval = enrichInterval ;

            // The last time the symbol's ITD data was enriched
            try {
                final String tmp = symbol.getExtraData1() ;
                if( StringUtil.isNotEmptyOrNull( tmp ) ) {
                    this.lastEnrichTime = TIMESTAMP_FMT.parse( tmp.trim() ) ;
                }
                else {
                    // If the last enrich time is not set, set a time to
                    // one hour before the current time.
                    long currentTime = System.currentTimeMillis() ;
                    currentTime -= 3600*1000 ;
                    this.lastEnrichTime = new Date( currentTime ) ;
                }
            }
            catch ( final ParseException e ) {
                // If we have a date parse exception, not to worry - just
                // treat the latest time as the current time.
                long currentTime = System.currentTimeMillis() ;
                currentTime -= 3600*1000 ;
                this.lastEnrichTime = new Date( currentTime ) ;
            }
        }

        @Override
        public String getName() {
            return "NSE scrip ITD import for '" + this.symbol.getValue() + "'" ;
        }

        @Override
        public void run() {
            try {
                final String symbolName = this.symbol.getValue() ;
                final IITDImportSvc svc = ServiceMgr.getITDImportSvc() ;

                LogMsg.info( "Importing ITD value for scrip '" + symbolName + "'") ;
                svc.importNSESymbol( symbolName, false ) ;

                // Once we have imported the ITD data, see if its time to
                // enrich the ITD data with high resolution values
                final int interval = (int)( System.currentTimeMillis() - this.lastEnrichTime.getTime() ) / 1000 ;
                if( interval > this.enrichInterval ) {
                    svc.importHighResNSESymbol( symbolName, false ) ;

                    // Update the last enrich time stamp.
                    this.symbol.setExtraData1( TIMESTAMP_FMT.format( new Date() ) ) ;
                    updateJobAttribute( this.symbol ) ;
                }
            }
            catch ( final Throwable e ) {
                LogMsg.error( "Error " + getName() + ". Msg = " + e.getMessage() ) ;
                logger.error( "Error in " + getName(), e ) ;
            }
        }
    }
    // ------END---- Asynchronous task for scrip ITD leeching ------------------

    /** Public constructor. */
    public NSEScripITDImportJob() {
        super() ;
    }

    /**
     * Retrieves the list of registered scrips for intra day leeching and schedules
     * them for asynchronous execution. The list of registered scrips is
     * determined by the indexed values for the job attribute named 'scrip'. If
     * there are no scrips registered, this function does nothing.
     *
     * @param jobConfig The job configuration for this task.
     */
    @Override
    protected void executeJob( final JobConfig jobCfg ) throws JobExecutionException {

        // Get the registered scrips.
        final List<JobAttribute> scrips = jobCfg.getAttributeValues( SCRIP_ATTR_VAL ) ;
        if( scrips != null && !scrips.isEmpty() ) {
            // Schedule each script as asynchronous tasks
            final IAsyncExecutorSvc executor = ServiceMgr.getAsyncExecutorSvc() ;

            final JobAttribute enrichIntervalAttr = jobCfg.getAttributeValue( SCRIP_ENRICH_INTERVAL ) ;
            int enrichInterval = DEF_ENRICH_INTERVAL ;
            if( enrichIntervalAttr != null ) {
                enrichInterval = Integer.parseInt( enrichIntervalAttr.getValue().trim() ) ;
            }

            for( final JobAttribute scrip : scrips ) {
                executor.submit( new AsyncScripITDImportTask( scrip, enrichInterval ) ) ;
            }
        }
    }
}
