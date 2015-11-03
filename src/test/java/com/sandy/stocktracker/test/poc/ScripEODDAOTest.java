/**
 * 
 * 
 * 
 *
 * Creation Date: Oct 14, 2008
 */

package com.sandy.stocktracker.test.poc;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.biz.dao.IEODIndexDAO ;
import com.sandy.apps.pluto.biz.svc.IJobSvc ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.ScripEOD ;
import com.sandy.apps.pluto.shared.util.bootstrap.Bootstrap ;
import com.sandy.apps.pluto.shared.util.util.BizObjectFactory ;

/**
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ScripEODDAOTest {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ScripEODDAOTest.class ) ;

    static BizObjectFactory OF = null ;

    public ScripEODDAOTest() {
    }

    public void test() throws Exception {

        final IEODIndexDAO dao = ( IEODIndexDAO )OF.getBean( "EODIndexDAO" ) ;
        final Date date = dao.getLastScripEODDate() ;
        logger.debug( date ) ;
        final List<ScripEOD> list = dao.getScripEOD( date, true ) ;
        for( final ScripEOD eod : list ) {
            logger.debug( eod ) ;
        }
    }

    public void testLastNPctChanges() {

        final IEODIndexDAO dao = ( IEODIndexDAO )OF.getBean( "EODIndexDAO" ) ;
        final Date date = dao.getLastScripEODDate() ;

        final long time = System.currentTimeMillis() ;
        dao.getLastNPctEODChange( date, 10 ) ;
        logger.debug( "Time taken = " + ( System.currentTimeMillis() - time ) ) ;
    }

    public static void main( final String[] args )
        throws Exception {

        final String cfgPath = "/com/sandy/stocktracker/bootstrap-config.xml" ;
        new Bootstrap().initialize( ScripEODDAOTest.class.getResource( cfgPath ) ) ;
        OF = ( BizObjectFactory )BizObjectFactory.getInstance() ;

        try {
            //new ScripEODDAOTest().test() ;
            new ScripEODDAOTest().testLastNPctChanges() ;
        }
        finally {
            final IJobSvc svc = ServiceMgr.getJobSvc() ;
            svc.shutdownScheduler() ;
        }
    }
}
