/**
 * Creation Date: Aug 7, 2008
 */

package com.sandy.apps.pluto.biz.svc.job;
import java.awt.TrayIcon.MessageType ;

import org.apache.log4j.Logger ;
import org.quartz.JobExecutionException ;

import com.sandy.apps.pluto.StockTracker ;
import com.sandy.apps.pluto.biz.dao.impl.RSSDAO ;
import com.sandy.apps.pluto.biz.svc.IRSSSvc ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.JobConfig ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;

/**
 * This job imports the active news from the Internet. The details of the
 * news sources can be obtained from the {@link RSSDAO#getNewsSources(boolean)}
 * operation.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class NewsImportJob extends AbstractBaseJob {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( NewsImportJob.class ) ;

    /** Public constructor. */
    public NewsImportJob() {
        super() ;
    }

    /**
     * Imports the latest and oldest bhavcopy data
     */
    @Override
    public void executeJob( final JobConfig jobCfg )
            throws JobExecutionException {

        logger.debug( "Executing News Import Job" ) ;
        try {
            final IRSSSvc rssSvc = ServiceMgr.getRSSSvc() ;
            final int numItems = rssSvc.importActiveNews() ;

            // If the number of items imported are greater than zero, notify
            // the user via a task bar message
            if( numItems > 0 ) {
                final String msg = numItems + " news items imported" ;
                StockTracker.SYS_TRAY.showMessage( "News", msg, MessageType.INFO ) ;
            }
        }
        catch( final Exception e ) {
            logger.error( "Error importing news", e ) ;
            LogMsg.error( "Error importing news" ) ;
        }
    }
}
