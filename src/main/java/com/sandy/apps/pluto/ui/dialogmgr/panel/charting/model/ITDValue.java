/**
 * Creation Date: Oct 19, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model;
import org.apache.log4j.Logger ;

/**
 *
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ITDValue implements Comparable<ITDValue>{

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ITDValue.class ) ;

    private long  time  = 0 ;
    private float value = 0 ;
    private long  volume= 0 ;

    private boolean interpolated = false ;

    /**
     */
    public ITDValue() {
        super() ;
    }

    public long getTime() { return this.time ; }
    public void setTime( final long time ) { this.time = time ; }

    public float getValue() { return this.value ; }
    public void setValue( final float value ) { this.value = value ; }

    public boolean isInterpolated() { return this.interpolated ; }
    public void setInterpolated( final boolean interp ) { this.interpolated = interp ; }

    public long getVolume() { return this.volume ; }
    public void setVolume( final long vol ) { this.volume = vol ; }

    /**
     * Returns a new ITDValue instance containing a percentage relative value
     * of this instance. The base against which the percentage relative values
     * are calculated is passes as the parameter.
     *
     * @param base The base for computing relative percentage values.
     *
     * @return A new instance of {@link ITDValue}
     */
    public ITDValue getPctChangeValue( final float base ) {

        final ITDValue newVal = new ITDValue() ;
        newVal.interpolated = this.interpolated ;
        newVal.setTime( this.time ) ;
        newVal.value = (( this.value - base )/base)*100 ;
        return newVal ;
    }

    @Override
    public int compareTo( final ITDValue o ) {
        return (int)(this.time - o.time) ;
    }

    /** ITD values are same if they represent values at the same time instant. */
    @Override
    public boolean equals( final Object obj ) {
        return this.time == (( ITDValue )obj).time ;
    }
}
