/**
 * Creation Date: Oct 19, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model;
import java.util.Date ;
import java.util.SortedSet ;
import java.util.TreeSet ;

import org.apache.log4j.Logger ;

/**
 * This class is a simple data holder for end of day values. Each end of
 * day value will have an opening, high, low and closing value. Please note that
 * it is not mandatory for an EOD value to have the OHLC values - for example,
 * if we are dealing with intra day data for today, the EOD value instance for
 * today will not have a closing value, although it will have a opening,
 * high and low value reflecting the state of the day data till the point of time.
 *
 * This class also contains an aggregation of ITD value instances, each of
 * which represents ITD values and points of time in the day.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class EODValue implements Comparable<EODValue>{

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( EODValue.class ) ;

    private float open      = -1 ;
    private float high      = Float.MIN_VALUE ;
    private float low       = Float.MAX_VALUE ;
    private float close     = -1 ;
    private float prevClose = -1 ;
    private long  volume    = -1 ;

    private boolean isITD = false ;

    private final SortedSet<ITDValue> itdValues = new TreeSet<ITDValue>() ;

    /** The date for which this instance represents the EOD value. */
    private final Date date ;

    /**
     * Constructor which takes in the date for which this instance represents
     * the EOD value.
     */
    public EODValue( final Date date ) {
        this.date = date ;
    }

    /**
     * Constructor which takes in the date for which this instance represents
     * the EOD value. It also takes a flag indicating that this EOD value is
     * not complete yet.
     */
    public EODValue( final Date date, final boolean isITD ) {
        this.date = date ;
        this.isITD = isITD ;
    }

    /**
     * Returns a new EODValue instance containing a percentage relative value
     * of this instance. The base against which the percentage relative values
     * are calculated is taken as the base value parameter passed.
     *
     * @param base The base for computing relative percentage values.
     *
     * @return A new instance of {@link ITDValue}
     */
    public EODValue getPctChangeValue( final float baseValue ) {

        final EODValue newVal = new EODValue( this.date, this.isITD ) ;

        newVal.open = (( getOpen() - baseValue )/baseValue)*100 ;
        newVal.high = (( getHigh() - baseValue )/baseValue)*100 ;
        newVal.low  = (( getLow()  - baseValue )/baseValue)*100 ;
        newVal.close= (( getClose()- baseValue )/baseValue)*100 ;

        for( final ITDValue itdVal : this.itdValues ) {
            newVal.itdValues.add( itdVal.getPctChangeValue( baseValue ) ) ;
        }

        return newVal ;
    }

    public float getOpen() { return this.open ; }
    public void setOpen( final float open ) { this.open = open ; }

    public float getHigh() { return this.high ; }
    public void setHigh( final float high ) { this.high = high ; }

    public float getLow() { return this.low ; }
    public void setLow( final float low ) { this.low = low ; }

    /**
     * Does special handling of the closing value. If this instance is a
     * ITD day marker, the closing is taken as the value of the last ITD price
     * range.
     */
    public float getClose() {
        float closeVal = -1 ;
        if( this.isITD ) {
            closeVal = this.itdValues.last().getValue() ;
        }
        else {
            closeVal = this.close ;
        }
        return closeVal ;
    }

    public void setClose( final float close ) { this.close = close ; }

    public float getPrevClose() { return this.prevClose ; }
    public void setPrevClose( final float close ) { this.prevClose = close ; }

    public Date getDate() { return this.date ; }

    public long getVolume() { return this.volume ; }
    public void setVolume( final long vol ) { this.volume = vol ; }

    /** The relative ordering of EOD values is based on the date. */
    @Override
    public int compareTo( final EODValue eod ) {
        return this.date.compareTo( eod.date ) ;
    }

    /** EOD values are same if they represent values at the same date. */
    @Override
    public boolean equals( final Object obj ) {
        return this.date.equals( ((EODValue)obj).date ) ;
    }

    /** Associates the given ITD value with this EOD value. */
    public void addITDValue( final ITDValue itdValue ) {

        // Synchronize on the collection of ITD values before adding, since the
        // itd values can be added to during the time the volue is being painted.
        synchronized ( this.itdValues ) {
            // Add the itd value to the list of values. Adding will automatically
            // place this itd value in a sorted order within the collection.
            this.itdValues.add( itdValue ) ;
        }

        // If we are dealing with an ITD value, compute the high and low values.
        // Also compute the volume of the day.
        if( this.isITD ) {
            if( itdValue.getValue() > this.high ) {
                this.high = itdValue.getValue() ;
            }

            if( itdValue.getValue() < this.low ) {
                this.low = itdValue.getValue() ;
            }

            if( itdValue.getVolume() > getVolume() ) {
                setVolume( itdValue.getVolume() ) ;
            }
        }

        // Check for the opening and closing price - Note that the values in
        // the itdValues list is sorted in order of time. So the first entry
        // in the list should be the opening price and the last entity in the
        // list should decide the closing price.
        this.open  = this.itdValues.first().getValue() ;
        this.close = this.itdValues.last().getValue() ;
    }

    /** Returns a sorted collection of ITD values for this day. */
    public SortedSet<ITDValue> getITDValues() {
        return this.itdValues ;
    }
}
