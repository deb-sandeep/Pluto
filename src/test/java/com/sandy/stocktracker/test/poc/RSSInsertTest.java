/**
 * 
 * 
 * 
 *
 * Creation Date: Oct 14, 2008
 */

package com.sandy.stocktracker.test.poc;
import java.util.List;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.biz.dao.IRSSDAO ;
import com.sandy.apps.pluto.biz.svc.IJobSvc ;
import com.sandy.apps.pluto.biz.svc.IRSSSvc ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.RSSNewsItem ;
import com.sandy.apps.pluto.shared.util.bootstrap.Bootstrap ;
import com.sandy.apps.pluto.shared.util.util.BizObjectFactory ;
import com.sandy.common.util.IOUtils;

/**
 * A POC test class to test the ITD Bulk intraday import service.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class RSSInsertTest {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( RSSInsertTest.class ) ;

    static BizObjectFactory OF = null ;

    public RSSInsertTest() {
        super() ;
    }

    public void test() throws Exception {

        final String contents = IOUtils.getPathContents( "c:\\temp\\temprss.xml" ) ;
        final IRSSSvc svc = ServiceMgr.getRSSSvc() ;
        final List<RSSNewsItem> newsItems = svc.parseRSSContent( "TEMP", contents ) ;

        final IRSSDAO dao = ( IRSSDAO )OF.getBean( "RSSDAO" ) ;
        for( final RSSNewsItem item : newsItems ) {
            dao.insertNewsItem( item ) ;
        }
    }

    public void testSource() throws Exception {

        final IRSSSvc svc = ServiceMgr.getRSSSvc() ;
        svc.importRSS( "Reuters", "India" ) ;
    }

    public static void main( final String[] args )
        throws Exception {

        final String cfgPath = "/com/sandy/stocktracker/bootstrap-config.xml" ;
        new Bootstrap().initialize( RSSInsertTest.class.getResource( cfgPath ) ) ;
        OF = ( BizObjectFactory )BizObjectFactory.getInstance() ;

        try {
            final RSSInsertTest test = new RSSInsertTest() ;
            //new RSSInsertTest().test() ;
            test.testSource() ;
        }
        finally {
            final IJobSvc svc = ServiceMgr.getJobSvc() ;
            svc.shutdownScheduler() ;
        }
    }
}
