/**
 * Creation Date: Jan 13, 2009
 */

package com.sandy.apps.pluto.shared.dto;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sandy.apps.pluto.shared.util.util.StringUtil ;

/**
 * This class encapsulates the data attributes required to represent a single
 * instance of trade (buy or sell) that the user has undertaken in the market.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class Trade implements Comparable<Trade>, Cloneable {

    // Static constants defining the trade type. The reason why these have
    // not been defined as enumeration because this class has direct mapping
    // to the database and string constants provide a more readable means.
    public static final String CASH   = "CASH" ;
    public static final String MARGIN = "MARGIN" ;
    public static final String SPOT   = "SPOT" ;

    /** A unique pluto wide trade identifier. For new orders this is -1. */
    private int tradeId = -1 ;

    /** The date on which the trade was performed. */
    private Date date = null ;

    /** The symbol (NSE) on which the trade was performed. */
    private String symbol = null ;

    /** If the trade is a buy, false if sell */
    private boolean buy = true ;

    /** The brokerage charges that have been levied on this trade. */
    private double brokerage = 0.0d ;

    /** The number of units that were traded. */
    private int units = 0 ;

    /** The market price (per unit) at which the trade was made. */
    private double unitPrice = 0.0d ;

    /**
     * The number of units matched. The value of this variable signifies the
     * number of units of this trade which has been quenched. For example,
     * if this is a buy trade for 100 units and 80 units are remaining (active),
     * it implies that 20 units have been matched. Similarly if this is a
     * sell order for 100 units and is used to quench a buy order of 20 units,
     * a clone of this sell order would be attached to the buy trade with a
     * matched unit of 20.
     */
    private int matchedUnits = 0 ;

    /**
     * A list of associated sell orders with this order. This is only valid
     * if this order is a buy order.
     */
    private List<Trade> sellOrders = null ;

    /**
     * The type of trade. Brokerage charges are typically computed based on the
     * type of trade.
     */
    private String tradeType = CASH ;

    /** Public no argument constructor. */
    public Trade() {
        this.tradeId = -1 ;
    }

    /** @return the date */
    public Date getDate() {
        return this.date ;
    }

    /** @param date the date to set */
    public void setDate( final Date date ) {
        this.date = date ;
    }

    /** @return the symbol */
    public String getSymbol() {
        return this.symbol ;
    }

    /** @param symbol the symbol to set */
    public void setSymbol( final String symbol ) {
        this.symbol = symbol ;
    }

    /** @return the buy */
    public boolean isBuy() {
        return this.buy ;
    }

    /** @param buy the buy to set */
    public void setBuy( final boolean buy ) {
        this.buy = buy ;
    }

    /** @return the brokerage */
    public double getBrokerage() {
        return this.brokerage ;
    }

    /** @param brokerage the brokerage to set */
    public void setBrokerage( final double brokerage ) {
        this.brokerage = brokerage ;
    }

    /** @return the units */
    public int getUnits() {
        return this.units ;
    }

    /** @param units the units to set */
    public void setUnits( final int units ) {
        this.units = units ;
    }

    /** @return the unitPrice */
    public double getUnitPrice() {
        return this.unitPrice ;
    }

    /** @param unitPrice the unitPrice to set */
    public void setUnitPrice( final double unitPrice ) {
        this.unitPrice = unitPrice ;
    }

    /** @return The average price per unit including brokerage and taxes */
    public double getAvgPrice() {

        double avgPrice = this.unitPrice ;
        final double avgBrok  = this.brokerage / this.units ;

        avgPrice += ( this.buy ) ? avgBrok : -1*avgBrok ;

        return avgPrice ;
    }

    /** @return the tradeType */
    public String getTradeType() {
        return this.tradeType ;
    }

    /** @param tradeType the tradeType to set */
    public void setTradeType( final String tradeType ) {
        this.tradeType = tradeType ;
    }

    /** @param id The trade id to set. */
    public void setTradeId( final int id ) {
        this.tradeId = id ;
    }

    /** @return The trade identifier. */
    public int getTradeId() {
        return this.tradeId ;
    }

    /** Returns true if this is a margin trade. */
    public boolean isMarginTrade() {
        return this.tradeType.equals( MARGIN ) ;
    }

    /** Returns true if this is a cash trade. */
    public boolean isCashTrade() {
        return this.tradeType.equals( CASH ) ;
    }

    /**
     * Returns he total price of this transaction from the perspective of the
     * user. For a sell trade, the user gets less because brokerage is
     * substracted from the sell price, while as for buy trade, the price is
     * more since the brokerage is also taken from the user.
     */
    public double getActualValue() {
        double value = this.unitPrice * this.units ;
        if( this.buy ) {
            value += this.brokerage ;
        }
        else {
            value -= this.brokerage ;
        }
        return value ;
    }

    /**
     * Returns the number of active units for this trade.
     */
    public int getNumActiveUnits() {
        return this.units - this.matchedUnits ;
    }

    /**
     * Sets the number of matched units for this trade. The number of matched
     * units should be less than the total number of units and greater
     * than 0.
     *
     * @param matchedUnits The number of matched units for this trade.
     *
     * @throws IllegalArgumentException In case matched units is more than the
     *         total units for this trade, or if the matched units is negative.
     */
    public void setMatchedUnits( final int matchedUnits ) {
        if( matchedUnits < 0 ) {
            throw new IllegalArgumentException( "Number of matched units can't" +
                    " be less than zero" ) ;
        }
        else if( matchedUnits > this.units ) {
            throw new IllegalArgumentException( "Num matched units can't be " +
                    " greater than the number of units" ) ;
        }
        else {
            this.matchedUnits = matchedUnits ;
        }
    }

    /** Returns the number of units that have been matched from this trade. */
    public int getMatchedUnits() {
        return this.matchedUnits ;
    }

    /** Adds a sell trade to this buy trade. */
    public void addSellTrade( final Trade trade ) {
        if( this.sellOrders == null ) {
            this.sellOrders = new ArrayList<Trade>() ;
        }
        this.sellOrders.add( trade ) ;
    }

    /**
     * Returns a list of sell trades associated with this trade. If this
     * trade is a sell trade, a null list is returned.
     */
    public List<Trade> getSellTrades() {
        return this.sellOrders ;
    }

    /**
     * Returns a string representation of this instance.
     */
    public String toString() {
        final StringBuilder buffer = new StringBuilder() ;
        final DecimalFormat df = new DecimalFormat( "###.00" ) ;
        final DateFormat dtFmt = new SimpleDateFormat( "MM/dd/yyyy HH:mm:ss" ) ;

        buffer.append( "[" + this.tradeId + "] " ) ;
        buffer.append( StringUtil.rightPad( dtFmt.format( this.date ), 20 ) ) ;
        if( this.buy ) {
            buffer.append( StringUtil.rightPad( "BUY", 5 ) ) ;
        }
        else {
            buffer.append( StringUtil.rightPad( "SELL", 5 ) ) ;
        }
        buffer.append( StringUtil.rightPad( this.tradeType, 5 ) ) ;
        buffer.append( StringUtil.rightPad( this.symbol, 10 ) ) ;
        buffer.append( StringUtil.leftPad( "" + this.units, 5 ) ) ;
        buffer.append( StringUtil.leftPad( df.format( this.unitPrice ), 7 ) ) ;
        buffer.append( StringUtil.leftPad( df.format( this.brokerage ), 7 ) ) ;
        buffer.append( StringUtil.leftPad( df.format( getAvgPrice() ), 7 ) ) ;
        buffer.append( StringUtil.leftPad( df.format( getActualValue() ), 10 ) ) ;

        return buffer.toString() ;
    }

    /**
     * Compares this trade with another trade based on their trade dates.
     */
    @Override
    public int compareTo( final Trade trade ) {
        int result = 0 ;
        result = this.date.compareTo( trade.date ) ;
        return result ;
    }

    /**
     * Returns true if the parameter represents a Trade and is equal to this
     * trade, false otherwise.
     */
    @Override
    public boolean equals( final Object obj ) {
        boolean retVal = false ;
        if( obj instanceof Trade ) {
            final Trade another = ( Trade )obj ;
            retVal = ( hashCode() == another.hashCode() ) ;
        }
        return retVal ;
    }

    /**
     * Clones this trade.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {

        final Trade trade  = new Trade() ;
        trade.tradeId      = this.tradeId ;
        trade.brokerage    = this.brokerage ;
        trade.buy          = this.buy ;
        trade.date         = this.date ;
        trade.matchedUnits = 0 ;
        trade.sellOrders   = null ;
        trade.symbol       = this.symbol ;
        trade.tradeType    = this.tradeType ;
        trade.unitPrice    = this.unitPrice ;
        trade.units        = this.units ;

        return trade ;
    }

    /**
     * Returns an unique integer value for this trade.
     */
    @Override
    public int hashCode() {
        return toString().hashCode() ;
    }
}
