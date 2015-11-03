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

import com.sandy.apps.pluto.biz.dao.IExIndexDAO ;
import com.sandy.apps.pluto.biz.svc.IExIndexSvc ;
import com.sandy.apps.pluto.biz.svc.IJobSvc ;
import com.sandy.apps.pluto.shared.STConstant ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.ExIndexEOD ;
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
public class IndexDataModification {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( IndexDataModification.class ) ;

    static BizObjectFactory OF = null ;

    public void updateActiveEODForIndex( final String index ) throws Exception {

        final IExIndexSvc svc = ServiceMgr.getExIndexSvc() ;
        final List<ExIndexEOD> activeEOD = svc.getExIndexEODList( index ) ;
        final IExIndexDAO dao = ( IExIndexDAO )OF.getBean( "ExIndexDAO" ) ;
        double lastClosing = -1 ;
        for( final ExIndexEOD eod : activeEOD ) {
            if( lastClosing != -1 ) {
                eod.setPrevClose( lastClosing ) ;
                logger.debug( "Updating ITD: Index = " + index + " ::date " + STConstant.DATE_FMT.format( eod.getDate() ) ) ;
                //dao.updateITDForEOD( eod ) ;
            }
            lastClosing = eod.getClose() ;
        }

        logger.debug( "Updating EOD for index " + index ) ;
        dao.updateEOD( activeEOD ) ;
    }

    public static void main( final String[] args )
        throws Exception {

        final String cfgPath = "/com/sandy/stocktracker/bootstrap-config.xml" ;
        new Bootstrap().initialize( IndexDataModification.class.getResource( cfgPath ) ) ;
        OF = ( BizObjectFactory )BizObjectFactory.getInstance() ;

        try {
            final IndexDataModification driver = new IndexDataModification() ;
            driver.updateActiveEODForIndex( "BANK NIFTY" ) ;
            driver.updateActiveEODForIndex( "CNX 100" ) ;
            driver.updateActiveEODForIndex( "CNX IT" ) ;
            driver.updateActiveEODForIndex( "CNX MIDCAP" ) ;
            driver.updateActiveEODForIndex( "CNX MIDCAP 200" ) ;
            driver.updateActiveEODForIndex( "CNX NIFTY JUNIOR" ) ;
            driver.updateActiveEODForIndex( "NIFTY MIDCAP 50" ) ;
            driver.updateActiveEODForIndex( "S&P CNX 500" ) ;
            driver.updateActiveEODForIndex( "S&P CNX DEFTY" ) ;
            driver.updateActiveEODForIndex( "S&P CNX NIFTY" ) ;
            driver.updateActiveEODForIndex( "S&P ESG INDIA INDEX" ) ;
        }
        finally {
            final IJobSvc svc = ServiceMgr.getJobSvc() ;
            svc.shutdownScheduler() ;
        }
    }
}
