/**
 * Creation Date: Aug 10, 2008
 */

package com.sandy.apps.pluto.biz.svc.job;
import java.text.DateFormat ;
import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.quartz.JobExecutionException ;

import com.sandy.apps.pluto.biz.svc.IAsyncExecutorSvc.AsyncTask ;
import com.sandy.apps.pluto.biz.svc.IExIndexSvc ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.JobAttribute ;
import com.sandy.apps.pluto.shared.dto.JobConfig ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.util.util.StringUtil ;

/**
 * This job fetches the ITD values of the NIFY indexes. This job fetches both
 * real time (low resolution) and retrospective (high resolution) NSE ITD index
 * values. The hi resolution index values are fetched for the multiple indexes
 * defined against the composite attribute named 'nse.index'. The value of the
 * 'nse.index' attribute needs to match from the ones in "MARKET_INDEX" table.
 * The high resolution data fetching frequency can be controlled by defining the
 * 'index.enrich.interval' attribute, value of which is the time period in seconds.
 * If this attribute is not provided, a default value of 3600 (1 hour) is taken.
 * <p>
 * Along with the high resolution ITD index values, this job also fetches the
 * real time index values on every invocation. This requires no additional
 * attributes for the job.
 * <p>
 * Note that each index ITD is downloaded asynchronously.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class NSEIndexITDImportJob extends AbstractBaseJob {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( NSEIndexITDImportJob.class ) ;

    /**
     * The job attribute key against which an indexed list of index names are
     * specified.
     */
    private static final String INDEX_NAME_KEY = "nse.index" ;

    /**
     * The key against which the minimum time interval for enriching ITD data
     * is stored. This job will try to enrich (fill in with high resolution)
     * ITD data after the value of this key. The value of this key should be
     * specified in seconds.
     */
    public static final String INDEX_ENRICH_INTERVAL = "index.enrich.interval" ;

    /** The default enrich time interval of one hour. */
    private static final int DEF_ENRICH_INTERVAL = 60*60 ;

    // ------START-- Asynchronous task for Index EOD leeching ------------------
    /**
     * This class represents the asynchronous task which will import the EOD
     * value for the specified index. This task is hardned such that it will
     * not propagate the exception beyond itself. This task receives as its
     * input an instance of {@link JobAttribute}, the value of which is the
     * name of the index and the extra data 1 variable holds the last date
     * of successful index download.
     */
    private class AsyncIndexITDImportTask implements AsyncTask {

        private JobAttribute indexInfo = null ;
        private int enrichInterval = DEF_ENRICH_INTERVAL ;

        // The timestamp format in which the last ITD time is stored.
        private final DateFormat TIMESTAMP_FMT = new SimpleDateFormat( "dd-MM-yyyy HH:mm:ss" ) ;

        /** Constructor. */
        public AsyncIndexITDImportTask( final JobAttribute info,
                                        final int enrichInterval ) {
            this.indexInfo = info ;
            this.enrichInterval = enrichInterval ;
        }

        @Override
        public String getName() {
            return "NSE Index ITD import for '" + this.indexInfo.getValue() + "'" ;
        }

        @Override
        public void run() {
            try {

                final String indexName = this.indexInfo.getValue() ;
                final String timeStr   = this.indexInfo.getExtraData1() ;
                Date lastImportTime = null ;
                if( StringUtil.isNotEmptyOrNull( timeStr ) ) {
                    lastImportTime = this.TIMESTAMP_FMT.parse( timeStr.trim() ) ;
                }
                importITDData( indexName, lastImportTime ) ;
            }
            catch ( final Throwable e ) {
                LogMsg.error( "Error " + getName() + ". Msg = " + e.getMessage() ) ;
                logError( "Error in " + getName(), e ) ;
            }
        }

        /**
         * Imports the ITD data for the specified index from the last import
         * time.
         *
         * @param indexName The name of the index for which we need to import the
         *        EOD data.
         *
         * @param lastImportTime The last date for which ITD data exists for this index.
         */
        private void importITDData( final String indexName, final Date lastImportTime )
            throws STException {

            final IExIndexSvc svc = ServiceMgr.getExIndexSvc() ;

            // Enrich only if the elapsed time is greater than the enrich interval.
            boolean importHiRes = false ;
            if( lastImportTime == null ) {
                importHiRes = true ;
            }
            else {
                final long elapsedTime = System.currentTimeMillis() - lastImportTime.getTime() ;
                if( elapsedTime > this.enrichInterval*1000 ) {
                    importHiRes = true ;
                }
            }

            if( importHiRes ) {
                LogMsg.info( "Importing hi res NSE index ITD for index " + this.indexInfo.getValue() ) ;
                final Date lastTime = svc.importHiResITDIndices( indexName, lastImportTime ) ;
                LogMsg.info( "Success importing hi res NSE index ITD for index " + this.indexInfo.getValue() ) ;

                // Update the context attribute for this index's date downloaded
                // date. This will be updated in the persistent storage as soon
                // as this task returns.
                this.indexInfo.setExtraData1( this.TIMESTAMP_FMT.format( lastTime ) ) ;
                updateJobAttribute( this.indexInfo ) ;
            }
            else {
                logger.debug( "Skipping high resolution index ITD import " +
                              "since enrich interval is not breached" ) ;
            }
        }
    }
    // ------END---- Asynchronous task for scrip ITD leeching ------------------

    /** Public constructor. */
    public NSEIndexITDImportJob() {
        super() ;
    }

    /**
     * Downloads the NSE index ITD data for the registered indexes.
     */
    @Override
    protected void executeJob( final JobConfig jobCfg )
            throws JobExecutionException {

        try {
            // Get the enrich interval from the job configuration.
            final JobAttribute enrichIntervalAttr = jobCfg.getAttributeValue( INDEX_ENRICH_INTERVAL ) ;
            int enrichInterval = DEF_ENRICH_INTERVAL ;
            if( enrichIntervalAttr != null ) {
                enrichInterval = Integer.parseInt( enrichIntervalAttr.getValue().trim() ) ;
            }

            // Get the list of registered indexes.
            final List<JobAttribute> indexes = jobCfg.getAttributeValues( INDEX_NAME_KEY ) ;
            final List<AsyncIndexITDImportTask> tasks = new ArrayList<AsyncIndexITDImportTask>() ;

            if( indexes != null && !indexes.isEmpty() ) {
                for( final JobAttribute indexInfo : indexes ) {
                    AsyncIndexITDImportTask task = null ;
                    task = new AsyncIndexITDImportTask( indexInfo, enrichInterval ) ;
                    tasks.add( task ) ;
                }
            }

            if( !tasks.isEmpty() ) {
                ServiceMgr.getAsyncExecutorSvc().submitAndWait( tasks ) ;
            }

            // Get the real time ITD values
            final IExIndexSvc svc = ServiceMgr.getExIndexSvc() ;
            LogMsg.info( "Importing low resolution index values" ) ;
            svc.importLowResITDIndices() ;
        }
        catch ( final Throwable e ) {
            final String jobName = jobCfg.getName() ;
            LogMsg.error( "NSE index ITD data import failed. Msg =" + e.getMessage() ) ;
            logger.debug( "Job " + jobName + " aborted due to exception.", e ) ;
            logger.error( jobName + " aborted. Msg =" + e.getMessage() ) ;
            new JobExecutionException( e ) ;
        }
    }
}
