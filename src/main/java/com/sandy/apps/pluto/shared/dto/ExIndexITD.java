/**
 * Creation Date: Aug 9, 2008
 */

package com.sandy.apps.pluto.shared.dto;

import java.io.Serializable ;
import java.util.Date ;

/**
 * This DTO encapsulates data related to an exchange's ITD information.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ExIndexITD extends AbstractITDValue
    implements Comparable<ExIndexITD>, Serializable {

    /** Generated serial version UID. */
    private static final long serialVersionUID = -3343809909342196889L ;

    private Date   date       = null ;
    private double currentVal = 0.0D ;
    private double prevClose  = 0.0D ;
    private double open       = 0.0D ;
    private String index      = null ;

    /** Public constructor. */
    public ExIndexITD() {
        super() ;
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
     * @return the currentVal
     */
    public double getCurrentVal() {
        return this.currentVal ;
    }

    /**
     * @param currentVal the currentVal to set
     */
    public void setCurrentVal( final double currentVal ) {
        this.currentVal = currentVal ;
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
     * Hash code for this instance.
     */
    @Override
    public int hashCode() {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + ((this.date == null) ? 0 : this.date.hashCode()) ;
        result = prime * result + ((this.index == null) ? 0 : this.index.hashCode()) ;
        return result ;
    }

    /**
     * Equals for this instance.
     */
    @Override
    public boolean equals( final Object obj ) {

        if (this == obj) return true ;
        if (obj == null) return false ;
        if (!(obj instanceof ExIndexITD)) return false ;

        final ExIndexITD other = ( ExIndexITD ) obj ;

        if (this.date == null) {
            if (other.date != null) return false ;
        }
        else if (!this.date.equals( other.date )) return false ;

        if (this.index == null) {
            if (other.index != null) return false ;
        }
        else if (!this.index.equals( other.index )) return false ;

        return true ;
    }

    /**
     * Compares two instances of {@link ExIndexITD}. If both the instances are
     * belong to the same index, they are compared based on their date values.
     * Else they are compared based on the natural ordering of their index
     * names.
     */
    @Override
    public int compareTo( final ExIndexITD o ) {
        if( this.index.equals( o.index ) ) {
            return this.date.compareTo( o.date ) ;
        }
        else {
            return this.index.compareTo( o.index ) ;
        }
    }
}
