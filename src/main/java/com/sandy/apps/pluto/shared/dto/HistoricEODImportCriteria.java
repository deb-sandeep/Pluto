/**
 * Creation Date: Aug 3, 2008
 */

package com.sandy.apps.pluto.shared.dto;

import java.util.ArrayList ;
import java.util.Date ;
import java.util.List ;

/**
 * This DTO encapsulates the criteria for importing historic equity EOD
 * information.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class HistoricEODImportCriteria {

    private final List<Symbol> symbolList       = new ArrayList<Symbol>() ;
    private Date         fromDate         = null ;
    private Date         toDate           = null ;
    private boolean      abortOnException = false ;

    /** Public constructor. */
    public HistoricEODImportCriteria() {
        super() ;
    }

    /**
     * @return the symbolList
     */
    public List<Symbol> getSymbolList() {
        return this.symbolList ;
    }

    /**
     * @param symbolList the symbolList to set
     */
    public void setSymbolList( final List<Symbol> symbolList ) {
        this.symbolList.addAll( symbolList ) ;
    }

    /**
     * Adds a symbol to the existing list of symbols.
     */
    public void addSymbol( final Symbol symbol ) {
        this.symbolList.add( symbol ) ;
    }

    /**
     * @return the fromDate
     */
    public Date getFromDate() {
        return this.fromDate ;
    }

    /**
     * @param fromDate the fromDate to set
     */
    public void setFromDate( final Date fromDate ) {
        this.fromDate = fromDate ;
    }

    /**
     * @return the toDate
     */
    public Date getToDate() {
        return this.toDate ;
    }

    /**
     * @param toDate the toDate to set
     */
    public void setToDate( final Date toDate ) {
        this.toDate = toDate ;
    }

    /**
     * @return the abortOnException
     */
    public boolean isAbortOnException() {
        return this.abortOnException ;
    }

    /**
     * @param abortOnException the abortOnException to set
     */
    public void setAbortOnException( final boolean abortOnException ) {
        this.abortOnException = abortOnException ;
    }
}
