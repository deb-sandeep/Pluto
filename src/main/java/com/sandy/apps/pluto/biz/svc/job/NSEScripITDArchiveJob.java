/**
 * Creation Date: Aug 7, 2008
 */

package com.sandy.apps.pluto.biz.svc.job;
import org.apache.log4j.Logger ;
import org.quartz.JobExecutionException ;

import com.sandy.apps.pluto.biz.svc.IEODImportSvc ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.JobConfig ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;

/**
 * This job archives scrip ITD data relative to the current date. The date
 * for archival is chosen based on the value of the configuration parameter
 * {@link IEODImportSvc#CFG_KEY_ARCHIVAL_THRESHOLD}. This job should be
 * scheduled very infrequently (maybe once a day or once every two days).
 * <p>
 * This job expects no job configuration parameters.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class NSEScripITDArchiveJob extends AbstractBaseJob {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( NSEScripITDArchiveJob.class ) ;

    /** Public constructor. */
    public NSEScripITDArchiveJob() {
        super() ;
    }

    /**
     * Imports the latest and oldest bhavcopy data
     */
    @Override
    public void executeJob( final JobConfig jobCfg )
            throws JobExecutionException {

        logger.debug( "Executing Scrip ITD archive Job" ) ;
        try {
            ServiceMgr.getITDBulkImportSvc().archive() ;
        }
        catch( final Exception e ) {
            logger.error( "Error archiving Scrip ITD", e ) ;
            LogMsg.error( "Error archiving scrip ITD data" ) ;
        }
    }
}
