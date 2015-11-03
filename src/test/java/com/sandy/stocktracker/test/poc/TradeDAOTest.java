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

import com.sandy.apps.pluto.biz.dao.ITradeDAO ;
import com.sandy.apps.pluto.biz.svc.IJobSvc ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.Trade ;
import com.sandy.apps.pluto.shared.util.bootstrap.Bootstrap ;
import com.sandy.apps.pluto.shared.util.util.BizObjectFactory ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.portfolio.PortfolioManager ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.portfolio.StockTradeGrouping ;

/**
 * A POC test class to test the Trade DAO and it's exposed operations.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class TradeDAOTest {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( TradeDAOTest.class ) ;

    static BizObjectFactory OF = null ;

    public TradeDAOTest() {
        super() ;
    }

    private Trade getTrade() {
        final Trade trade = new Trade() ;
        trade.setSymbol( "ICICIBANK" ) ;
        trade.setBuy( true ) ;
        trade.setBrokerage( 100.0 ) ;
        trade.setUnitPrice( 100.0 ) ;
        trade.setUnits( 100 ) ;
        trade.setTradeType( Trade.CASH ) ;
        trade.setDate( new Date() ) ;
        return trade ;
    }

    public void test() throws Exception {

        final ITradeDAO dao = ( ITradeDAO )OF.getBean( "TradeDAO" ) ;
        final Trade trade   = getTrade() ;

        logger.debug( "Adding a new trade" ) ;
        dao.add( trade ) ;

        logger.debug( "Listing all trades" ) ;
        List<Trade> allTrades = dao.getAllTrades() ;
        for( final Trade t : allTrades ) {
            logger.debug( t ) ;
        }

        logger.debug( "Remove the last trade" ) ;
        dao.remove( allTrades.get( allTrades.size()-1 ).getTradeId() ) ;

        logger.debug( "Listing all trades" ) ;
        allTrades = dao.getAllTrades() ;
        for( final Trade t : allTrades ) {
            logger.debug( t ) ;
        }
    }

    public void testPortfolio() throws Exception {

        final PortfolioManager portfolio = PortfolioManager.getInstance() ;

        final List<StockTradeGrouping> allTrades = portfolio.getAllTrades() ;
        for( final StockTradeGrouping stockTrade: allTrades ) {
            logger.debug( "STOCK : " + stockTrade ) ;
        }
    }

    public static void main( final String[] args )
        throws Exception {

        final String cfgPath = "/com/sandy/stocktracker/bootstrap-config.xml" ;
        new Bootstrap().initialize( TradeDAOTest.class.getResource( cfgPath ) ) ;
        OF = ( BizObjectFactory )BizObjectFactory.getInstance() ;

        try {
            final TradeDAOTest test = new TradeDAOTest() ;
            //test.test() ;
            test.testPortfolio() ;
        }
        finally {
            final IJobSvc svc = ServiceMgr.getJobSvc() ;
            svc.shutdownScheduler() ;
        }
    }
}
