/**
 * Creation Date: Aug 9, 2008
 */

package com.sandy.apps.pluto.shared.dto;

import java.util.Date ;

import com.sandy.apps.pluto.shared.STConstant ;
import com.sandy.apps.pluto.shared.util.util.StringUtil ;

/**
 * This DTO encapsulates data related to an exchange's EOD information. Note
 * that this class does not have a reference to the index name. It is assumed
 * that the association would be maintained separately.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ExIndexEOD implements Comparable<ExIndexEOD>{

    private double open = 0.0D ;
    private double high = 0.0D ;
    private double low  = 0.0D ;
    private double close= 0.0D ;
    private Date   date = null ;
    private String index= null ;
    private double prevClose = 0.0D ;

    /** Public constructor. */
    public ExIndexEOD() {
    }

    /** Returns a string form of this instance. */
    public String toString() {
        final StringBuffer buffer = new StringBuffer() ;
        buffer.append( StringUtil.rightPad( this.index, 30 ) ) ;
        buffer.append( StringUtil.rightPad( STConstant.DATE_FMT.format( this.date ), 15 ) ) ;
        buffer.append( StringUtil.rightPad( "" + this.open, 10 ) ) ;
        buffer.append( StringUtil.rightPad( "" + this.high, 10 ) ) ;
        buffer.append( StringUtil.rightPad( "" + this.low , 10 ) ) ;
        buffer.append( StringUtil.rightPad( "" + this.close, 10 ) ) ;
        buffer.append( StringUtil.rightPad( "" + this.prevClose, 10 ) ) ;
        return buffer.toString() ;
    }

    /**
     * @return The change between the closing and opening value for the day
     */
    public double getEODChange() {
        return this.close - this.open ;
    }

    /**
     * @return The change between the highest and the lowest value for the day
     */
    public double getITDChange() {
        return this.high - this.low ;
    }

    /**
     * @return the open
     */
    public double getOpen() {
        return this.open ;
    }

    /**
     * @param open the open to set
     */
    public void setOpen( final double open ) {
        this.open = open ;
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
     * @return the close
     */
    public double getClose() {
        return this.close ;
    }

    /**
     * @param close the close to set
     */
    public void setClose( final double close ) {
        this.close = close ;
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
    }

    /**
     * @return the index
     */
    public String getIndex() {
        return this.index ;
    }

    /**
     * @param index the index to set
     */
    public void setIndex( final String index ) {
        this.index = index ;
    }

    /**
     * Generates a hyper strong hash code - hahaha. Joys of generated code.
     */
    @Override
    public int hashCode() {

        final int prime = 31 ;
        int result = 1 ;
        long temp ;

        temp = Double.doubleToLongBits( this.close ) ;
        result = prime * result + ( int ) (temp ^ (temp >>> 32)) ;
        result = prime * result + ((this.date == null) ? 0 : this.date.hashCode()) ;

        temp = Double.doubleToLongBits( this.high ) ;
        result = prime * result + ( int ) (temp ^ (temp >>> 32)) ;

        temp = Double.doubleToLongBits( this.low ) ;
        result = prime * result + ( int ) (temp ^ (temp >>> 32)) ;

        temp = Double.doubleToLongBits( this.open ) ;
        result = prime * result + ( int ) (temp ^ (temp >>> 32)) ;

        result = prime * this.index.hashCode() ;

        return result ;
    }

    /**
     * Generates a hyper strong equals code - hahaha. Joys of generated code.
     */
    @Override
    public boolean equals( final Object obj ) {

        if (this == obj) return true ;
        if (obj == null) return false ;
        if (!(obj instanceof ExIndexEOD)) return false ;

        final ExIndexEOD other = ( ExIndexEOD ) obj ;

        if( other.index.equals( this.index ) ) return false ;

        if ( Double.doubleToLongBits( this.close ) !=
             Double.doubleToLongBits( other.close ))
            return false ;

        if (this.date == null) {
            if (other.date != null) return false ;
        }
        else if (!this.date.equals( other.date )) return false ;

        if ( Double.doubleToLongBits( this.high ) !=
             Double.doubleToLongBits( other.high ) )
            return false ;

        if ( Double.doubleToLongBits( this.low ) !=
             Double.doubleToLongBits( other.low ) )
            return false ;

        if ( Double.doubleToLongBits( this.open ) !=
             Double.doubleToLongBits( other.open ) )
            return false ;

        return true ;
    }

    /**
     * Compares two ExIndexEOD instances. If the instances belong to the same
     * index, the order of their time decides their comparison. Else the
     * index name comparision is returned.
     */
    @Override
    public int compareTo( final ExIndexEOD o ) {
        if( o.getIndex().equals( getIndex() ) ) {
            return this.date.compareTo( o.date ) ;
        }
        else {
            return this.index.compareTo( o.index ) ;
        }
    }
}
