/**
 * Creation Date: Dec 21, 2008
 */

package com.sandy.apps.pluto.shared.dto;
import java.util.Date ;

/**
 * A simple DTO which encapsulates information about a symbol and the EOD
 * percentage change for a given date. An aggregation of this DTO instances
 * gives a visual indicator of how the symbol has done over a period of time.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class SymbolPctChange {

    private String symbol = null ;
    private Date date = null ;
    private Double closingPrice = null ;
    private Double prevClosingPrice = null ;

    /** Public no argument constructor. */
    public SymbolPctChange() {
        super() ;
    }

    /**
     * @return the symbol
     */
    public String getSymbol() {
        return this.symbol ;
    }

    /**
     * @param symbol the symbol to set
     */
    public void setSymbol( final String symbol ) {
        this.symbol = symbol ;
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
     * @return the pctChange
     */
    public Double getPctChange() {
        double pctChange = 0 ;
        if( this.prevClosingPrice != 0 ) {
            pctChange = (( this.closingPrice - this.prevClosingPrice )/this.prevClosingPrice)*100 ;
        }
        return pctChange ;
    }

    /**
     * @return the closingPrice
     */
    public Double getClosingPrice() {
        return this.closingPrice ;
    }

    /**
     * @return the prevClosingPrice
     */
    public Double getPrevClosingPrice() {
        return this.prevClosingPrice ;
    }

    /**
     * @param closingPrice the closingPrice to set
     */
    public void setClosingPrice( final Double closingPrice ) {
        this.closingPrice = closingPrice ;
    }

    /**
     * @param prevClosingPrice the prevClosingPrice to set
     */
    public void setPrevClosingPrice( final Double prevClosingPrice ) {
        this.prevClosingPrice = prevClosingPrice ;
    }
}
