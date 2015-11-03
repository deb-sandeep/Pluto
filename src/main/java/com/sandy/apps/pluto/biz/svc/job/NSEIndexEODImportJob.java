/**
 * Creation Date: Aug 10, 2008
 */

package com.sandy.apps.pluto.biz.svc.job;
import java.util.Calendar ;
import java.util.Date ;
import java.util.List ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;
import org.quartz.JobExecutionException ;

import com.sandy.apps.pluto.biz.svc.IAsyncExecutorSvc.AsyncTask ;
import com.sandy.apps.pluto.biz.svc.IExIndexSvc ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.STUtils ;
import com.sandy.apps.pluto.shared.dto.JobAttribute ;
import com.sandy.apps.pluto.shared.dto.JobConfig ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.util.util.BizObjectFactory ;
import com.sandy.apps.pluto.shared.util.util.SpringObjectFactory ;
import com.sandy.apps.pluto.shared.util.util.StringUtil ;

/**
 * This job fetches the EOD values of the NSE indexes as configured in the
 * task configuration of this job as indexed keys with the format 'nse.index'
 * with the value as the index name. The last date of successful download is
 * specified in the 'dd-MM-yyyy' format in the extraData1 column. The
 * last successful date is updated after every successful download.
 * <p>
 * Note that each index EOD is downloaded asynchronously.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class NSEIndexEODImportJob extends AbstractBaseJob {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( NSEIndexEODImportJob.class ) ;

    /**
     * The job attribute key against which an indexed list of index names are
     * specified.
     */
    private static final String INDEX_NAME_KEY = "nse.index" ;

    // ------START-- Asynchronous task for Index EOD leeching ------------------
    /**
     * This class represents the asynchronous task which will import the EOD
     * value for the specified index. This task is hardened such that it will
     * not propagate the exception beyond itself. This task receives as its
     * input an instance of {@link JobAttribute}, the value of which is the
     * name of the index and the extra data 1 variable holds the last date
     * of successful index download.
     */
    private class AsyncIndexEODImportTask implements AsyncTask {

        private JobAttribute indexInfo = null ;

        /** Constructor. */
        public AsyncIndexEODImportTask( final JobAttribute info ) {
            this.indexInfo = info ;
        }

        @Override
        public String getName() {
            return "NSE Index EOD import for '" + this.indexInfo.getValue() + "'" ;
        }

        @Override
        public void run() {
            try {
                LogMsg.info( "Importing NSE index EOD for index " + this.indexInfo.getValue() ) ;
                final String indexName = this.indexInfo.getValue() ;
                final String dateStr   = this.indexInfo.getExtraData1() ;
                Date lastImportDate = null ;
                if( StringUtil.isNotEmptyOrNull( dateStr ) ) {
                    lastImportDate = DF.parse( dateStr ) ;
                    importEODData( indexName, lastImportDate ) ;
                }
                else {
                    throw new IllegalArgumentException( "Last import date not " +
                            "specified for index " + this.indexInfo.getValue() ) ;
                }
            }
            catch ( final Throwable e ) {
                LogMsg.error( "Error " + getName() + ". Msg = " + e.getMessage() ) ;
                logError( "Error in " + getName(), e ) ;
            }
        }

        /**
         * Imports the EOD data for the specified index from the day following the
         * last date till yesterday. If the number of days between lastDate and
         * today is less than 2 days, the import is ignored.
         *
         * @param indexName The name of the index for which we need to import the
         *        EOD data.
         *
         * @param lastDate The last date for which EOD data exists for this index.
         */
        private void importEODData( final String indexName, final Date lastDate )
            throws STException {

            final Calendar today = STUtils.getToday() ;

            final Date yesterday = DateUtils.addDays( today.getTime(), -1 ) ;
            final Date startDate = DateUtils.addDays( lastDate, 1 ) ;

            if( logger.isDebugEnabled() ) {
                logger.debug( "Initiating index EOD import for " + indexName ) ;
                logger.debug( "\tStart date = " + startDate ) ;
                logger.debug( "\tEnd   date = " + yesterday ) ;
            }

            if( startDate.getTime() <= yesterday.getTime() ) {
                // Okay we have some days missing - need to fetch the data
                final SpringObjectFactory of = BizObjectFactory.getInstance() ;
                final IExIndexSvc svc = ( IExIndexSvc )of.getBean( "ExIndexSvc" ) ;
                svc.importEODIndices( indexName, startDate, yesterday ) ;

                // Update the context attribute for this index's date downloaded
                // date. This will be updated in the persistent storage as soon
                // as this task returns.
                this.indexInfo.setExtraData1( DF.format( yesterday ) ) ;
                updateJobAttribute( this.indexInfo ) ;
            }
        }
    }
    // ------END---- Asynchronous task for scrip ITD leeching ------------------

    /** Public constructor. */
    public NSEIndexEODImportJob() {
        super() ;
    }

    /**
     * Downloads the NSE index EOD data for the registered indexes.
     */
    @Override
    protected void executeJob( final JobConfig jobCfg )
            throws JobExecutionException {

        try {
            // Get the list of registered indexes.
            final List<JobAttribute> indexes = jobCfg.getAttributeValues( INDEX_NAME_KEY ) ;
            if( indexes != null && !indexes.isEmpty() ) {
                for( final JobAttribute indexInfo : indexes ) {
                    AsyncIndexEODImportTask task = null ;
                    task = new AsyncIndexEODImportTask( indexInfo ) ;
                    task.run() ;
                }
            }
        }
        catch ( final Exception e ) {
            final String jobName = jobCfg.getName() ;
            LogMsg.error( "NSE index EOD data import failed. Msg =" + e.getMessage() ) ;
            logger.debug( "Job " + jobName + " aborted due to exception.", e ) ;
            logger.error( jobName + " aborted. Msg =" + e.getMessage() ) ;
            new JobExecutionException( e ) ;
        }
    }
}
