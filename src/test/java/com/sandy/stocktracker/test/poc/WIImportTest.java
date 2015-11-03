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

import com.sandy.apps.pluto.biz.svc.IJobSvc ;
import com.sandy.apps.pluto.biz.svc.impl.scraper.BloombergWIParser ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.WorldIndex ;
import com.sandy.apps.pluto.shared.util.bootstrap.Bootstrap ;
import com.sandy.apps.pluto.shared.util.util.BizObjectFactory ;

/**
 * A POC test class to test the World Index import service.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class WIImportTest {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( WIImportTest.class ) ;

    static BizObjectFactory OF = null ;

    public WIImportTest() {
        super() ;
    }

    public void test() throws Exception {

        final BloombergWIParser parser = new BloombergWIParser( ServiceMgr.getNetworkSvc() ) ;
        final List<WorldIndex> indexes = parser.getWorldIndexValues() ;
        for( final WorldIndex index : indexes ) {
            logger.debug( index ) ;
        }
    }

    public static void main( final String[] args )
        throws Exception {

        final String cfgPath = "/com/sandy/stocktracker/bootstrap-config.xml" ;
        new Bootstrap().initialize( WIImportTest.class.getResource( cfgPath ) ) ;
        OF = ( BizObjectFactory )BizObjectFactory.getInstance() ;

        try {
            final WIImportTest test = new WIImportTest() ;
            test.test() ;
        }
        finally {
            final IJobSvc svc = ServiceMgr.getJobSvc() ;
            svc.shutdownScheduler() ;
        }

        /*
        final TimeZone   est  = TimeZone.getTimeZone ( "America/New_York" ) ;
        final DateFormat df   = new SimpleDateFormat( "MMM dd HH:mm" ) ;

        String tmp = "Mar�06�11:23" ;
        tmp = tmp.replace( (char)0xA0, (char)0x20 ) ;

        final Date       date = df.parse( tmp ) ;
        final Calendar   cal  = Calendar.getInstance() ;

        cal.setTime( date ) ;
        cal.set( Calendar.YEAR, Calendar.getInstance().get( Calendar.YEAR ) ) ;
        cal.setTimeZone( est ) ;

        cal.set( Calendar.HOUR, 9 ) ;
        cal.set( Calendar.MINUTE, 22 ) ;

        logger.debug( cal.getTime() ) ;
        */
    }
}
