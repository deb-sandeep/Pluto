/**
 * 
 * 
 * 
 *
 * Creation Date: Jan 17, 2009
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.portfolio ;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.biz.dao.ITradeDAO ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.Trade ;

/**
 * This class encapsulates the entire portfolio details and provides multiple
 * views on the data it contains. PortfolioManager is a singleton class.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class PortfolioManager implements Serializable {

    /** The serial version UID. */
    private static final long serialVersionUID = 4836605762181044617L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( PortfolioManager.class ) ;

    /** A list of active cash stocks. */
    private final List<StockTradeGrouping> activeCashTrades =
                                     new ArrayList<StockTradeGrouping>() ;

    /** A list of all cash stocks (including active and passive) */
    private final List<StockTradeGrouping> allCashTrades =
                                     new ArrayList<StockTradeGrouping>() ;


    /** The singleton instance for this class. */
    private static PortfolioManager INSTANCE = null ;

    /** A reference to the trade DAO. This will be set during initialization. */
    private static ITradeDAO tradeDAO = null ;

    /**
     * Private constructor for enforcing the singleton pattern.
     *
     * @param allTrades A list of all trades sorted on the ascending order
     *        of their trade date.
     */
    private PortfolioManager() {
        super() ;
    }

    /**
     * Singleton accessor method.
     */
    public static PortfolioManager getInstance() {
        if( INSTANCE == null ) {
            try {
                INSTANCE = new PortfolioManager() ;
                tradeDAO = ServiceMgr.getTradeDAO() ;
                INSTANCE.refresh() ;
            }
            catch ( final Exception e ) {
                logger.error( "Portfolio manager could not be loaded", e ) ;
            }
        }
        return INSTANCE ;
    }

    /**
     * Initializes the internal state of this instance based on the allTrades that
     * this portfolio has been created with.
     */
    public void refresh() throws STException {

        StockTradeGrouping stockTrades = null ;
        String symbol = null ;

        // A list of all the allTrades that this portfolio represents.
        List<Trade> allTrades = null ;

        // A map of symbols versus their {@link StockTradeGrouping} instances. */
        final Map<String, StockTradeGrouping> stockTradesMap =
                                     new HashMap<String, StockTradeGrouping>() ;

        this.activeCashTrades.clear() ;
        this.allCashTrades.clear() ;

        allTrades = tradeDAO.getAllTrades() ;
        if( allTrades == null ) {
            logger.warn( "No trades found in the system." ) ;
            return ;
        }

        for( final Trade trade : allTrades ) {
            symbol = trade.getSymbol() ;
            stockTrades = stockTradesMap.get( symbol ) ;
            if( stockTrades == null ) {
                stockTrades = new StockTradeGrouping( symbol ) ;
                stockTradesMap.put( symbol, stockTrades ) ;
            }
            // Now that we have a valid stock trade instance, let's add the
            // current trade to it.
            logger.debug( "Adding trade :" + trade ) ;
            stockTrades.addTrade( trade ) ;
        }

        // Initialize active stocks
        for( final StockTradeGrouping stTrades : stockTradesMap.values() ) {
            stTrades.initialize() ;
            this.allCashTrades.add( stTrades ) ;
            if( stTrades.getNumActiveCashUnits() > 0 ) {
                this.activeCashTrades.add( stTrades ) ;
            }
        }
    }

    /** Returns a list of stock trades for stocks whose quantity is active. */
    public List<StockTradeGrouping> getActiveTrades() {
        return this.activeCashTrades ;
    }

    /** Returns a list of all stock trades. */
    public List<StockTradeGrouping> getAllTrades() {
        return this.allCashTrades ;
    }
}
