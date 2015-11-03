/**
 * 
 * 
 * 
 *
 * Creation Date: Oct 14, 2008
 */

package com.sandy.stocktracker.test.poc;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.biz.dao.IEODIndexDAO ;
import com.sandy.apps.pluto.biz.dao.ISymbolDAO ;
import com.sandy.apps.pluto.biz.svc.IJobSvc ;
import com.sandy.apps.pluto.biz.svc.IScripSvc ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.ScripEOD ;
import com.sandy.apps.pluto.shared.dto.Symbol ;
import com.sandy.apps.pluto.shared.util.bootstrap.Bootstrap ;
import com.sandy.apps.pluto.shared.util.util.BizObjectFactory ;

/**
 * A class is written to enrich the old data (both ITD and EOD) for previous
 * closing price and other derived entities like open, close, high, low,
 * percentage change etc. Since we had not taken these into consideration
 * earlier, we have to retrospectively correct the data.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ScripDataModification {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ScripDataModification.class ) ;

    static BizObjectFactory OF = null ;

    public ScripDataModification() {
    }

    public void updateActiveEODForSymbol( final String scrip ) throws Exception {

        final IScripSvc eodScripSvc = ServiceMgr.getScripSvc() ;
        final List<ScripEOD> activeEOD = eodScripSvc.getEODData( scrip ) ;
        double lastClosing = -1 ;
        for( final ScripEOD eod : activeEOD ) {
            if( lastClosing != -1 ) {
                eod.setPrevClosePrice( lastClosing ) ;
            }
            lastClosing = eod.getClosingPrice() ;
        }

        final IEODIndexDAO dao = ( IEODIndexDAO )OF.getBean( "EODIndexDAO" ) ;
        dao.update( activeEOD ) ;
    }

    public void updateArchiveEODForSymbol( final String scrip ) throws Exception {

        final IScripSvc eodScripSvc = ServiceMgr.getScripSvc() ;
        final List<ScripEOD> activeEOD = eodScripSvc.getArchivedEODData( scrip ) ;
        double lastClosing = -1 ;
        for( final ScripEOD eod : activeEOD ) {
            if( lastClosing != -1 ) {
                eod.setPrevClosePrice( lastClosing ) ;
            }
            lastClosing = eod.getClosingPrice() ;
        }

        final IEODIndexDAO dao = ( IEODIndexDAO )OF.getBean( "EODIndexDAO" ) ;
        dao.updateArchive( activeEOD ) ;
    }

    public void updateActiveEOD() throws Exception {

        final ISymbolDAO symbDAO = ( ISymbolDAO )OF.getBean( "SymbolDAO" ) ;
        final Map<String, Symbol> symbMap = symbDAO.getAllSymbolsMap() ;

        int i = 0 ;
        for( final String symbol : symbMap.keySet() ) {
            logger.debug( symbMap.size()-i + " left. Updating Active symbol = " + symbol ) ;
            updateActiveEODForSymbol( symbol ) ;
            i++ ;
        }

        i = 0 ;
        for( final String symbol : symbMap.keySet() ) {
            logger.debug( symbMap.size()-i + " left. Updating Archive symbol = " + symbol ) ;
            updateArchiveEODForSymbol( symbol ) ;
            i++ ;
        }
    }

    public static void main( final String[] args )
        throws Exception {

        final String cfgPath = "/com/sandy/stocktracker/bootstrap-config.xml" ;
        new Bootstrap().initialize( ScripDataModification.class.getResource( cfgPath ) ) ;
        OF = ( BizObjectFactory )BizObjectFactory.getInstance() ;

        try {
            new ScripDataModification().updateActiveEOD() ;
        }
        finally {
            final IJobSvc svc = ServiceMgr.getJobSvc() ;
            svc.shutdownScheduler() ;
        }
    }
}
