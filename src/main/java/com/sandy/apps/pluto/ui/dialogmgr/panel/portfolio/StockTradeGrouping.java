/**
 * 
 * 
 * 
 *
 * Creation Date: Jan 17, 2009
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.portfolio;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.ErrorCode ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.dto.ScripEOD ;
import com.sandy.apps.pluto.shared.dto.ScripITD ;
import com.sandy.apps.pluto.shared.dto.Trade ;
import com.sandy.apps.pluto.shared.util.util.StringUtil ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.scripeodsummary.ScripEODValueCache ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.scripitdsummary.ScripITDValueCache ;

/**
 * This class encapsulates the trades specific to a particular stock. The
 * PortfolioManager contains many instances of this class, qualified by the stock
 * that this class represents.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class StockTradeGrouping implements Serializable {

    /** The serial version UID. */
    private static final long serialVersionUID = 4836605762181044617L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( StockTradeGrouping.class ) ;

    /** The symbol (NSE), for which this instance encapsulates the trades. */
    final private String symbol ;

    /**
     * A list of all the trades for this symbol in ascending order of their
     * trade dates. This list includes both buy and sell trades for both
     * CASH and MARGIN trade types.
     */
    private final List<Trade> allTrades = new ArrayList<Trade>() ;

    /**
     * A list containing all buy trades irrespective of their holding status.
     * These buy trades will be associated with the sell trades if any.
     */
    private final List<Trade> allCashBuyTrades = new ArrayList<Trade>() ;

    /**
     * A list of all the buy orders with positive holdings. Positive holding
     * orders are trades (buy), whose number of active units are greater than 0.
     */
    private final List<Trade> posHldCashTrades = new ArrayList<Trade>() ;

    /** The average unit price for the active units of cash trades. */
    private double avgCashUnitPrice = 0 ;

    /**
     * The realized profit/loss for this stock. This includes the realized
     * profit for both the cash and margin trades.
     */
    private double realizedProfit = 0 ;

    /** A reference to the ITD value cache. */
    private ScripITDValueCache itdValueCache = null ;

    /** A reference to the EOD value cache. */
    private ScripEODValueCache eodValueCache = null ;

    /** Public no argument constructor. */
    public StockTradeGrouping( final String symbol ) {
        super() ;
        this.symbol = symbol ;
    }

    /**
     * Adds the given trade to the list of trades for this stock. Also computes
     * derived data. This method thrown a STException in case the trade order
     * does not match. For example if we are adding a sell trade for which
     * there is no corresponding buy trade, it is treated as an exception
     * condition.
     *
     * @param trade The trade to be added.
     */
    public void addTrade( final Trade trade ) throws STException {

        if( trade == null ) {
            throw new IllegalArgumentException( "Trade can't be null" ) ;
        }
        else if( !trade.getSymbol().equals( this.symbol ) ) {
            throw new IllegalArgumentException( "A trade for " + trade.getSymbol() +
                    " can't be added to stock trades for " + this.symbol ) ;
        }

        // Note that orders would be always added in ascending order of their
        // trade date.
        if( trade.isCashTrade() ) {
            // Cash trades have different characteristics as compared to margin
            // trades. In case of cash trades, we can't sell if we do not have
            // as many active units.
            if( trade.isBuy() ) {
                addBuyCashTrade( trade ) ;
            }
            else {
                try {
                    // It we are dealing with a sell trade. Handle it separately
                    addSellCashTrade( trade ) ;
                }
                catch ( final CloneNotSupportedException e ) {
                    throw new STException( e, ErrorCode.UNKNOWN_EXCEPTION ) ;
                }
            }
        }
        else if( trade.isMarginTrade() ) {
            // Margin trades show different characteristics as compared to
            // cash trades. The net active units at the end of a day is 0.
            // Also, in case of margin trades we can do short selling implying
            // we can sell even if we don't have as many active units.
        }
        else {
            throw new STException( "Trade type " + trade.getTradeType() +
                                   " not supported", ErrorCode.INVALID_ARG ) ;
        }

        this.allTrades.add( trade ) ;
    }

    /**
     * Adds a buy trade. Buy trades can be of either CASH or MARGIN types. Note
     * that we handle cash and margin separately since their brokerage and
     * trade characteristics are different.
     */
    private void addBuyCashTrade( final Trade trade ) {
        // Add the trade to the end of the list.
        this.posHldCashTrades.add( trade ) ;
        this.allCashBuyTrades.add( trade ) ;
    }

    /**
     * Adds a sell order to the buy trades for this stock. The sell order
     * will be matched against the active buy orders and registered with them.
     *
     * @param trade The sell trade.
     */
    private void addSellCashTrade( final Trade trade )
        throws CloneNotSupportedException {

        // Check if the number of active units is greater than or equal to
        // the number of units that have been sold. If not, it is an error
        // condition. We are not handling short sell scenarios.
        if( getNumActiveCashUnits() < trade.getUnits() ) {
            logger.error( "Selling more than active untis. Error condition" ) ;
            logger.error( "  SYMBOL       = " + trade.getSymbol() ) ;
            logger.error( "  active units = " + getNumActiveCashUnits() ) ;
            logger.error( "  sell units   = " + trade.getUnits() ) ;
            throw new IllegalArgumentException( "Trying to sell more than " +
                    "what we have. Bad bad.." ) ;
        }

        // Selling happens in FIFO order.
        int   qtyMatched    = 0 ;
        Trade buyTrade      = null ;
        int   buyActUnits   = 0 ;
        int   sellUnitsLeft = trade.getUnits() ;
        Trade sellTrade= null ;

        // While we have not quenched all the units of the sell trade, we
        // continue matching it against our buy orders. Assumption is that if
        // we receive a sell order, we must have enough buy orders already
        // registered to quench the sell order. Else we have an error condition
        while( qtyMatched != trade.getUnits() ) {

            // Clone the sell order. In case one sell order matches multiple
            // buy orders, we need to attach a clone of the sell order with
            // appropriate quantity matched to each buy order.
            sellTrade = ( Trade )trade.clone() ;

            // Get the first buy order which has still some active units left.
            buyTrade    = this.posHldCashTrades.get( 0 ) ;

            // Determine how many active units we are dealing with.
            buyActUnits = buyTrade.getNumActiveUnits() ;

            // If the buy active units are greater than the sell units left,
            // it implies that the complete sell order will be used up here.
            if( buyActUnits >= sellUnitsLeft ) {

                // Increase the total quantity matched by the sell units left.
                qtyMatched += sellUnitsLeft ;
                buyTrade.setMatchedUnits( buyTrade.getMatchedUnits() + sellUnitsLeft ) ;
                sellTrade.setMatchedUnits( sellUnitsLeft ) ;
                sellUnitsLeft = 0 ;
            }
            else {
                // If the sell units are more than the current buy units, it
                // implies that this sell order spans multiple buy orders.
                qtyMatched += buyActUnits ;

                // The buy order is completely matched. Hence the matched units
                // is equal to the number of buy units.
                buyTrade.setMatchedUnits( buyTrade.getUnits() ) ;

                // Set the number of units matched for this sell trade.
                sellTrade.setMatchedUnits( buyActUnits ) ;
                sellUnitsLeft -= buyActUnits ;
            }

            buyTrade.addSellTrade( sellTrade ) ;

            // If the buy order is completely matched, remove it from the list
            // of positive holdings and move it to the zero holdings list.
            if( buyTrade.getNumActiveUnits() == 0 ) {
                this.posHldCashTrades.remove( 0 ) ;
            }
        }
    }

    /**
     * This method is called after all the trades for this symbol has been
     * added and there are no more trades to be added immediately. This method
     * computes the average unit price for the active units held for this
     * stock. This method also computes the realized profit for this stock.
     */
    public void initialize() {

        // Calculate the average unit price.
        calculateAvgUnitPrice() ;

        // Calculate the realized profit/loss for this stock
        calculateRealizedProfit() ;

        this.itdValueCache = ScripITDValueCache.getInstance() ;
        this.eodValueCache = ScripEODValueCache.getInstance() ;
    }

    /** Calculates the realized profit for the trades of this symbol. */
    private void calculateRealizedProfit() {

        // Calculate the realized profit for this stock. Realized profit is
        // the sum of money already gained or lost for all the shares sold for
        // this stock.
        this.realizedProfit = 0 ;
        for( final Trade buyTrade : this.allCashBuyTrades ) {
            final List<Trade> sellTrades = buyTrade.getSellTrades() ;
            if( sellTrades != null ) {
                for( final Trade sellTrade : sellTrades ) {
                    this.realizedProfit += sellTrade.getMatchedUnits() *
                          ( sellTrade.getAvgPrice() - buyTrade.getAvgPrice() ) ;
                }
            }
        }
    }

    /** Calculates the average unit price for the active units. */
    private void calculateAvgUnitPrice() {

        // If we don't have any active units, the average unit price is 0.
        if( getNumActiveCashUnits() <= 0 ) {
            this.avgCashUnitPrice = 0 ;
            return ;
        }
        else {
            double totalCostPrice = 0 ;
            for( final Trade trade : this.posHldCashTrades ) {
                totalCostPrice += trade.getNumActiveUnits() * trade.getAvgPrice() ;
            }
            this.avgCashUnitPrice = totalCostPrice / getNumActiveCashUnits() ;
        }
    }

    /** Returns the symbol that this class represents. */
    public String getSymbol() {
        return this.symbol ;
    }

    /**
     * Returns the number of active units for this stock. Active units are
     * those units which are currently held by the user in his DMAT account
     * for this stock. For example, if the user has bought 100 shares and
     * sold 50 shares, the active units will be 100-50 = 50 shares.
     *
     * @return The number of active share units for this stock.
     */
    public int getNumActiveCashUnits() {
        int numActiveUnits = 0 ;
        for( final Trade buyTrade : this.posHldCashTrades ) {
            numActiveUnits += buyTrade.getNumActiveUnits() ;
        }
        return numActiveUnits ;
    }

    /**
     * Returns the average unit price for the active units. The average price
     * is inclusive of brokerage and taxes. If the number of active units are 0,
     * the average price will be zero.
     *
     * @return The average unit price for active stocks.
     */
    public double getAvgCashUnitPrice() {
        return this.avgCashUnitPrice ;
    }

    /**
     * Returns the realized profit for this stock. Realized profit/loss is
     * computed for already sold trades.
     *
     * @return The realized profit/loss for this symbol.
     */
    public double getRealizedProfit() {
        return this.realizedProfit ;
    }

    /**
     * Returns the unrealized profit for this stock. Realized profit/loss is
     * computed for active units and their current market price. Unrealized
     * profit represents the amount of money the user will make it s/he sells
     * the currently active units at the last traded price.
     *
     * @return The unrealized profit/loss for this symbol. The unrealized
     *         profit is 0 in case there are no active units.
     */
    public double getUnRealizedCashProfit() {
        double   profit = 0 ;
        double   lastTP = 0.0 ;
        ScripITD itdVal = null ;
        ScripEOD eodVal = null ;

        // We have unrealized profit if and only if we have positive active units
        if( getNumActiveCashUnits() > 0 ) {
            itdVal = this.itdValueCache.getScripITDForSymbol( this.symbol ) ;
            if( itdVal != null ) {
                lastTP = itdVal.getPrice() ;
            }
            else {
                eodVal = this.eodValueCache.getScripEOD( this.symbol ) ;
                if( eodVal != null ) {
                    lastTP = eodVal.getClosingPrice() ;
                }
                else {
                    throw new IllegalArgumentException( "Symbol " + this.symbol +
                                       " not registered for calculating LTP" ) ;
                }
            }

            profit = getNumActiveCashUnits() * ( lastTP - this.avgCashUnitPrice ) ;

            // Approximately 0.87% of the profit is taxes and brokerage.
            // Subtract that from the profit.
            profit -= (0.87/100)*profit ;
        }
        return profit ;
    }

    /**
     * Returns a string representation of this instance.
     */
    public String toString() {
        final StringBuilder buffer = new StringBuilder() ;
        final DecimalFormat df = new DecimalFormat( "#00.00" ) ;

        buffer.append( StringUtil.rightPad( this.symbol, 20 ) ) ;
        buffer.append( StringUtil.leftPad( "" + getNumActiveCashUnits(), 5 ) ) ;
        buffer.append( StringUtil.leftPad( df.format( this.avgCashUnitPrice ), 10 ) ) ;
        buffer.append( StringUtil.leftPad( df.format( this.realizedProfit ), 10 ) ) ;
        buffer.append( StringUtil.leftPad( df.format( getUnRealizedCashProfit() ), 10 ) ) ;

        return buffer.toString() ;
    }
}
