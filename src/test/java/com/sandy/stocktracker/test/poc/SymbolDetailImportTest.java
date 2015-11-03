/**
 * 
 * 
 * 
 *
 * Creation Date: Oct 14, 2008
 */

package com.sandy.stocktracker.test.poc;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.biz.dao.ISymbolDAO ;
import com.sandy.apps.pluto.biz.svc.IJobSvc ;
import com.sandy.apps.pluto.biz.svc.IScripSvc ;
import com.sandy.apps.pluto.biz.svc.impl.scraper.ICICIDirectCoSnapshotScreenParser ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.Symbol ;
import com.sandy.apps.pluto.shared.util.bootstrap.Bootstrap ;
import com.sandy.apps.pluto.shared.util.util.BizObjectFactory ;

/**
 * A POC test class to test the ITD Bulk intraday import service.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class SymbolDetailImportTest {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( SymbolDetailImportTest.class ) ;

    static BizObjectFactory OF = null ;

    public SymbolDetailImportTest() {
        super() ;
    }

    public void test() throws Exception {
        ICICIDirectCoSnapshotScreenParser parser = null ;
        parser = new ICICIDirectCoSnapshotScreenParser( ServiceMgr.getNetworkSvc() ) ;

        final Symbol symbol = parser.parse( "INASEC" ) ;
        logger.debug( "ICICI Code = " + symbol.getIciciCode() ) ;
        logger.debug( "Website    = " + symbol.getWebsite() ) ;
        logger.debug( "Segment    = " + symbol.getSegment() ) ;
        logger.debug( "Segment Cat= " + symbol.getSegmentCat() ) ;
    }

    public void importForAll() throws Exception {

        final ISymbolDAO dao = ( ISymbolDAO )OF.getBean( "SymbolDAO" ) ;
        final Map<String, Symbol> symbolMap = dao.getAllSymbolsMap() ;
        final IScripSvc   scripSvc = ServiceMgr.getScripSvc() ;

        int totalSymbols = symbolMap.size() ;
        int success = 0 ;
        for( final Symbol symbol : symbolMap.values() ) {
            try {
                scripSvc.importScripDetails( symbol.getSymbol(), true, false ) ;
                logger.error( totalSymbols-- + " symbols left. Success = " + ++success +
                              " KB downloaded = " + ServiceMgr.getNetworkSvc().getNumBytesDownloaded()/(1024) ) ;
            }
            catch ( final STException e ) {
            }
            catch( final Exception ex ) {
                logger.error( "ERROR", ex ) ;
            }
        }
    }

    public static void main( final String[] args )
        throws Exception {

        final String cfgPath = "/com/sandy/stocktracker/bootstrap-config.xml" ;
        new Bootstrap().initialize( SymbolDetailImportTest.class.getResource( cfgPath ) ) ;
        OF = ( BizObjectFactory )BizObjectFactory.getInstance() ;

        try {
            final SymbolDetailImportTest test = new SymbolDetailImportTest() ;
            //test.test() ;
            test.importForAll() ;
        }
        finally {
            final IJobSvc svc = ServiceMgr.getJobSvc() ;
            svc.shutdownScheduler() ;
        }
    }
}
