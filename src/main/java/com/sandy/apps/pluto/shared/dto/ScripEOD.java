/**
 * Creation Date: Jul 27, 2008
 */

package com.sandy.apps.pluto.shared.dto;
import java.util.Calendar ;
import java.util.Date ;

import org.apache.commons.lang.time.DateUtils ;
import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.shared.STConstant ;

/**
 * This class encapsulates an end of day price index data for a symbol.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ScripEOD implements Comparable<ScripEOD> {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ScripEOD.class ) ;

    private String  symbolId        = null ;
    private Date    date            = null ;
    private double  openingPrice    = 0.0D ;
    private double  closingPrice    = 0.0D ;
    private double  highestPrice    = 0.0D ;
    private double  lowestPrice     = 0.0D ;
    private long    totalTradeQty   = 0 ;
    private double  prevClosePrice  = 0.0D ;
    private Symbol  symbol          = null ;
    private final boolean hasSymbolDetails = false ;

    /** Public no argument constructor. */
    public ScripEOD() {
        super() ;
    }

    /**
     * @return the symbol
     */
    public String getSymbolId() {
        return this.symbolId ;
    }

    /**
     * @param symbol the symbol to set
     */
    public void setSymbolId( final String id ) {
        this.symbolId = id ;
    }

    /**
     * @return the date
     */
    public Date getDate() {
        return this.date ;
    }

    /**
     * @param date the date to set
     */
    public void setDate( final Date date ) {
        this.date = date ;

        // For EOD date doesn't have any significance for hours minutes and seconds
        DateUtils.truncate( this.date, Calendar.HOUR ) ;
        DateUtils.truncate( this.date, Calendar.MINUTE ) ;
        DateUtils.truncate( this.date, Calendar.SECOND ) ;
        DateUtils.truncate( this.date, Calendar.MILLISECOND ) ;
    }

    /**
     * @return the openingPrice
     */
    public double getOpeningPrice() {
        return this.openingPrice ;
    }

    /**
     * @param openingPrice the openingPrice to set
     */
    public void setOpeningPrice( final double openingPrice ) {
        this.openingPrice = openingPrice ;
    }

    /**
     * @return the closingPrice
     */
    public double getClosingPrice() {
        return this.closingPrice ;
    }

    /**
     * @param closingPrice the closingPrice to set
     */
    public void setClosingPrice( final double closingPrice ) {
        this.closingPrice = closingPrice ;
    }

    /**
     * @return the highestPrice
     */
    public double getHighestPrice() {
        return this.highestPrice ;
    }

    /**
     * @param highestPrice the highestPrice to set
     */
    public void setHighestPrice( final double highestPrice ) {
        this.highestPrice = highestPrice ;
    }

    /**
     * @return the lowestPrice
     */
    public double getLowestPrice() {
        return this.lowestPrice ;
    }

    /**
     * @param lowestPrice the lowestPrice to set
     */
    public void setLowestPrice( final double lowestPrice ) {
        this.lowestPrice = lowestPrice ;
    }

    /**
     * @return the totalTradeQty
     */
    public long getTotalTradeQty() {
        return this.totalTradeQty ;
    }

    /**
     * @param totalTradeQty the totalTradeQty to set
     */
    public void setTotalTradeQty( final long totalTradeQty ) {
        this.totalTradeQty = totalTradeQty ;
    }

    /**
     * @return the prevClosePrice
     */
    public double getPrevClosePrice() {
        return this.prevClosePrice ;
    }

    /**
     * @param prevClosePrice the prevClosePrice to set
     */
    public void setPrevClosePrice( final double prevClosePrice ) {
        this.prevClosePrice = prevClosePrice ;
    }

    /**
     * @return the symbol
     */
    public Symbol getSymbol() {
        return this.symbol ;
    }

    /**
     * @param symbol the symbol to set
     */
    public void setSymbol( final Symbol symbol ) {
        this.symbol = symbol ;
    }

    /**
     * @return the hasSymbolDetails
     */
    public boolean hasSymbolDetails() {
        return this.hasSymbolDetails ;
    }

    /**
     * Compares this instance with another script EOD and returns +1, 0 or -1
     * depending upon whether this instance should be post, equal or prior to
     * the parameter.
     */
    @Override
    public int compareTo( final ScripEOD o ) {
        return this.date.compareTo( o.date ) ;
    }

    /**
     * toString method: creates a String representation of the object
     * @return the String representation
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer() ;
        buffer.append( "ScripEOD[" ) ;
        buffer.append( "symbolId        = " ).append( this.symbolId ) ;
        buffer.append( ", date          = " ).append( STConstant.DATE_FMT.format( this.date ) ) ;
        buffer.append( ", prevClose     = " ).append( this.prevClosePrice ) ;
        buffer.append( ", openingPrice  = " ).append( this.openingPrice ) ;
        buffer.append( ", closingPrice  = " ).append( this.closingPrice ) ;
        buffer.append( ", highestPrice  = " ).append( this.highestPrice ) ;
        buffer.append( ", lowestPrice   = " ).append( this.lowestPrice ) ;
        buffer.append( ", totalTradeQty = " ).append( this.totalTradeQty ) ;
        buffer.append( ", symbol        = " ).append( this.symbol ) ;
        buffer.append( "]" ) ;
        return buffer.toString() ;
    }
}
