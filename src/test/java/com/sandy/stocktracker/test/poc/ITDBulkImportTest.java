/**
 * 
 * 
 * 
 *
 * Creation Date: Oct 14, 2008
 */

package com.sandy.stocktracker.test.poc;
import org.apache.log4j.Logger;

import com.sandy.apps.pluto.biz.svc.IITDBulkImportSvc ;
import com.sandy.apps.pluto.biz.svc.IJobSvc ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.ScripITD ;
import com.sandy.apps.pluto.shared.event.Event ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.event.IEventSubscriber ;
import com.sandy.apps.pluto.shared.util.bootstrap.Bootstrap ;
import com.sandy.apps.pluto.shared.util.util.BizObjectFactory ;

/**
 * A POC test class to test the ITD Bulk intraday import service.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ITDBulkImportTest implements IEventSubscriber {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ITDBulkImportTest.class ) ;

    static BizObjectFactory OF = null ;

    public ITDBulkImportTest() {
        EventBus.instance().addSubscriberForEventTypes( this, EventType.EVT_SCRIP_ITD_INSERT ) ;
    }

    public void test() throws Exception {

        final IITDBulkImportSvc importSvc = ServiceMgr.getITDBulkImportSvc() ;

        importSvc.importNSEIndexScrips( "NIFTY", false ) ;
    }

    public static void main( final String[] args )
        throws Exception {

        final String cfgPath = "/com/sandy/stocktracker/bootstrap-config.xml" ;
        new Bootstrap().initialize( ITDBulkImportTest.class.getResource( cfgPath ) ) ;
        OF = ( BizObjectFactory )BizObjectFactory.getInstance() ;

        try {
            new ITDBulkImportTest().test() ;
        }
        finally {
            final IJobSvc svc = ServiceMgr.getJobSvc() ;
            svc.shutdownScheduler() ;
        }
    }

    @Override
    public void handleEvent( final Event event ) {
        final ScripITD itd = ( ScripITD )event.getValue() ;
        logger.debug( itd ) ;
    }
}
