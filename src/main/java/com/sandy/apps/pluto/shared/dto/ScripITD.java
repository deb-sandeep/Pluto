/**
 * Creation Date: Jul 27, 2008
 */

package com.sandy.apps.pluto.shared.dto;
import java.io.Serializable ;
import java.text.DateFormat ;
import java.text.SimpleDateFormat ;
import java.util.Date ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.shared.util.util.StringUtil ;

/**
 * This class encapsulates an intra day price index data for a symbol.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ScripITD extends AbstractITDValue
    implements Comparable<ScripITD>, Serializable {

    /** Generated serial version UID. This class will be serialized by Pluto. */
    private static final long serialVersionUID = -3777821100438402350L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ScripITD.class ) ;

    private String symbolId     = null ;
    private double price        = 0.0D ;
    private Date   time         = null ;
    private double high         = 0.0D ;
    private double low          = 0.0D ;
    private double prevClose    = 0.0D ;
    private double openingPrice = 0.0D ;
    private double change       = 0.0D ;
    private double pctChange    = 0.0D ;
    private long   totalTradeQty= -1 ;

    /* A boolean flag indicate if this instance is interpolated or live. */
    private boolean interpolated = false ;

    /** Public no argument constructor. */
    public ScripITD() {
        super() ;
    }

    /**
     * @return the symbolId
     */
    public String getSymbolId() {
        return this.symbolId ;
    }

    /**
     * @param symbolId the symbolId to set
     */
    public void setSymbolId( final String symbolId ) {
        this.symbolId = symbolId ;
    }

    /**
     * @return the price
     */
    public double getPrice() {
        return this.price ;
    }

    /**
     * @param price the price to set
     */
    public void setPrice( final double price ) {
        this.price = price ;
    }

    /**
     * @param time the time to set
     */
    public void setTime( final Date time ) {
        this.time = time;
    }

    /**
     * @return the time
     */
    public Date getTime() {
        return this.time;
    }

    /**
     * @return the high
     */
    public double getHigh() {
        return this.high ;
    }

    /**
     * @param high the high to set
     */
    public void setHigh( final double high ) {
        this.high = high ;
    }

    /**
     * @return the low
     */
    public double getLow() {
        return this.low ;
    }

    /**
     * @param low the low to set
     */
    public void setLow( final double low ) {
        this.low = low ;
    }

    /**
     * @return the prevClose
     */
    public double getPrevClose() {
        return this.prevClose ;
    }

    /**
     * @param prevClose the prevClose to set
     */
    public void setPrevClose( final double prevClose ) {
        this.prevClose = prevClose ;
    }

    /**
     * @return the change
     */
    public double getChange() {
        return this.change ;
    }

    /**
     * @param change the change to set
     */
    public void setChange( final double change ) {
        this.change = change ;
    }

    /**
     * @return the pctChange
     */
    public double getPctChange() {
        return this.pctChange ;
    }

    /**
     * @return the percentage change relative to the opening price.
     */
    public double getPctChangeO() {
        final double pct = (( this.price - this.openingPrice )/this.openingPrice)*100 ;
        return pct ;
    }

    /**
     * @param pctChange the pctChange to set
     */
    public void setPctChange( final double pctChange ) {
        this.pctChange = pctChange ;
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
     * @return the interpolated
     */
    public boolean isInterpolated() {
        boolean retVal = this.interpolated ;

        // If an interpolated Scrip ITD is loaded from the database, the
        // following condition will hold.
        if( !retVal ) {
            if( ( this.openingPrice == 0.00 ) ||
                ( this.totalTradeQty == -1.00 ) ||
                ( this.high == 0.00 ) ||
                ( this.low == 0.00 ) ) {
                retVal = true ;
            }
        }
        return retVal ;
    }

    /**
     * @param interpolated the interpolated to set
     */
    public void setInterpolated( final boolean interpolated ) {
        this.interpolated = interpolated ;
    }

    /**
     * A string representation of this DTO.
     */
    public String toString() {
        final DateFormat DF = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" ) ;
        final StringBuffer buffer = new StringBuffer() ;
        buffer.append( StringUtil.rightPad( this.symbolId, 20 ) ) ;
        buffer.append( StringUtil.rightPad( DF.format( this.time ), 20 ) ) ;
        buffer.append( StringUtil.rightPad( "" + this.openingPrice, 20 ) ) ;
        buffer.append( StringUtil.rightPad( "" + this.high, 20 ) ) ;
        buffer.append( StringUtil.rightPad( "" + this.low, 20 ) ) ;
        buffer.append( StringUtil.rightPad( "" + this.price, 20 ) ) ;
        buffer.append( StringUtil.rightPad( "" + this.prevClose, 20 ) ) ;
        buffer.append( StringUtil.rightPad( "" + this.pctChange, 20 ) ) ;
        buffer.append( StringUtil.rightPad( "" + this.totalTradeQty, 20 ) ) ;
        return buffer.toString() ;
    }

    /**
     * Compares this instance with another script ITD and returns +1, 0 or -1
     * depending upon whether this instance should be post, equal or prior to
     * the parameter.
     *
     * If the ITD value being compared is of another symbol, then the ordering
     * is based on the natural ordering of the symbols, else on time.
     */
    @Override
    public int compareTo( final ScripITD o ) {
        int retVal = 0 ;
        if( !this.symbolId.equals( o.symbolId ) ) {
            retVal = this.symbolId.compareTo( o.symbolId ) ;
        }
        else {
            retVal = this.time.compareTo( o.time ) ;
        }
        return retVal ;
    }

    /**
     * Returns the hash code of this instance.
     */
    @Override
    public int hashCode() {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + ((this.symbolId == null) ? 0 : this.symbolId.hashCode()) ;
        result = prime * result + ((this.time == null) ? 0 : this.time.hashCode()) ;
        return result ;
    }

    /**
     * Two instances of {@link ScripITD} are equal if they are of the
     * same symbol and time.
     */
    @Override
    public boolean equals( final Object obj ) {
        if ( this == obj ) return true ;
        if ( obj == null ) return false ;
        if (!(obj instanceof ScripITD)) return false ;

        final ScripITD other = ( ScripITD ) obj ;
        if ( !this.symbolId.equals( other.symbolId ) ) return false ;
        if ( this.time == null ) {
            if ( other.time != null ) return false ;
        }
        else if (!this.time.equals( other.time ))
            return false ;

        return true ;
    }
}
