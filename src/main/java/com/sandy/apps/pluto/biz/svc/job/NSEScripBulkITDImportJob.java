/**
 * Creation Date: Aug 21, 2008
 */

package com.sandy.apps.pluto.biz.svc.job;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.quartz.JobExecutionException ;

import com.sandy.apps.pluto.biz.svc.IAsyncExecutorSvc ;
import com.sandy.apps.pluto.biz.svc.IAsyncExecutorSvc.AsyncTask ;
import com.sandy.apps.pluto.biz.svc.IITDBulkImportSvc ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.JobAttribute ;
import com.sandy.apps.pluto.shared.dto.JobConfig ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;

/**
 * This class helps import NSE intraday quotes for the registered scrips in
 * the indexes published by NSE. The supported indexes are listed as follows
 * and are statically defined in the {@link IITDBulkImportSvc}'s INDEX_
 * constants. One or more indexes can be registered against an instance of this
 * job, by using an indexed job configuration variable named 'index'. Each of the
 * index scrip values will be fetched asynchronously at the scheduled time of this
 * job.
 * <p>
 * Please note that this job does not enrich the data with past high resolution
 * values. So, if a cycle is missed or on an exception, the value for that
 * instant will be lost.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class NSEScripBulkITDImportJob extends AbstractBaseJob {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( NSEScripBulkITDImportJob.class ) ;

    /** The attribute key against which the indexes are stored. */
    public static final String INDEX_ATTR_VAL = "index" ;

    // ------START-- Asynchronous task for scrip ITD leeching ------------------
    /**
     * This class represents the asynchronous task which will import the ITD
     * value for the specified scrip. This task is hardned such that it will
     * not propagate the exception beyond itself.
     */
    private class AsyncScripBulkITDImportTask implements AsyncTask {

        private JobAttribute index = null ;

        /** Constructor. */
        public AsyncScripBulkITDImportTask( final JobAttribute index ) {
            this.index = index ;
        }

        @Override
        public String getName() {
            return "NSE index scrip ITD import for '" + this.index.getValue() + "'" ;
        }

        @Override
        public void run() {
            try {
                final String indexName  = this.index.getValue() ;
                final IITDBulkImportSvc svc = ServiceMgr.getITDBulkImportSvc() ;

                LogMsg.info( "Importing ITD scrip value for index '" + indexName + "'") ;
                svc.importNSEIndexScrips( indexName, true ) ;
                LogMsg.info( "ITD scrip value for index '" + indexName +
                             "' successfully imported." ) ;
            }
            catch ( final Throwable e ) {
                LogMsg.error( "Error " + getName() + ". Msg = " + e.getMessage() ) ;
                logger.error( "Error in " + getName(), e ) ;
            }
        }
    }
    // ------END---- Asynchronous task for scrip ITD leeching ------------------

    /** Public constructor. */
    public NSEScripBulkITDImportJob() {
        super() ;
    }

    /**
     * Retrieves the list of registered index for intra day scrip leeching and
     * schedules them for asynchronous execution. The list of registered scrips is
     * determined by the indexed values for the job attribute named 'index'. If
     * there are no indexes registered, this function does nothing.
     *
     * @param jobConfig The job configuration for this task.
     */
    @Override
    protected void executeJob( final JobConfig jobCfg ) throws JobExecutionException {

        // Get the registered scrips.
        final List<JobAttribute> indices = jobCfg.getAttributeValues( INDEX_ATTR_VAL ) ;
        if( indices == null || indices.isEmpty() ) {
            logger.warn( "No index registered for ITD leeching" ) ;
            LogMsg.error( "No index found for intraday leeching" ) ;
        }
        else {
            // Schedule each script as asynchronous tasks
            final IAsyncExecutorSvc executor = ServiceMgr.getAsyncExecutorSvc() ;
            final List<AsyncScripBulkITDImportTask> tasks = new ArrayList<AsyncScripBulkITDImportTask>() ;

            for( final JobAttribute index : indices ) {
                tasks.add( new AsyncScripBulkITDImportTask( index ) ) ;
            }

            executor.submitAndWait( tasks ) ;
        }
    }
}
