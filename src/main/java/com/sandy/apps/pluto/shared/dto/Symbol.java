/**
 * Creation Date: Jul 27, 2008
 */

package com.sandy.apps.pluto.shared.dto;
import org.apache.log4j.Logger ;

/**
 * A class encapsulating the information regarding a symbol.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class Symbol {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( Symbol.class ) ;

    private String symbol = null ;
    private String series  = null ;
    private String marketType = null ;
    private String description = null ;
    private String niftyGroup = null ;
    private String segment = null ;
    private String segmentCat = null ;
    private String iciciCode = null ;
    private String website = null ;

    /** Public no argument constructor. */
    public Symbol() {
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
     * @return the series
     */
    public String getSeries() {
        return this.series ;
    }

    /**
     * @param series the series to set
     */
    public void setSeries( final String series ) {
        this.series = series ;
    }

    /**
     * @return the marketType
     */
    public String getMarketType() {
        return this.marketType ;
    }

    /**
     * @param marketType the marketType to set
     */
    public void setMarketType( final String marketType ) {
        this.marketType = marketType ;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return this.description ;
    }

    /**
     * @param description the description to set
     */
    public void setDescription( final String description ) {
        this.description = description ;
    }
    /**
     * @return the niftyGroup
     */
    public String getNiftyGroup() {
        return this.niftyGroup ;
    }
    /**
     * @param niftyGroup the niftyGroup to set
     */
    public void setNiftyGroup( final String niftyGroup ) {
        this.niftyGroup = niftyGroup ;
    }
    /**
     * @return the segment
     */
    public String getSegment() {
        return this.segment ;
    }
    /**
     * @param segment the segment to set
     */
    public void setSegment( final String segment ) {
        this.segment = segment ;
    }

    /**
     * @return the iciciCode
     */
    public String getIciciCode() {
        return this.iciciCode ;
    }

    /**
     * @param iciciCode the iciciCode to set
     */
    public void setIciciCode( final String iciciCode ) {
        this.iciciCode = iciciCode ;
    }

    /**
     * @return the segmentCat
     */
    public String getSegmentCat() {
        return this.segmentCat ;
    }
    /**
     * @param segmentCat the segmentCat to set
     */
    public void setSegmentCat( final String segmentCat ) {
        this.segmentCat = segmentCat ;
    }
    /**
     * @return the website
     */
    public String getWebsite() {
        return this.website ;
    }
    /**
     * @param website the website to set
     */
    public void setWebsite( final String website ) {
        this.website = website ;
    }
    /**
     * Two symbols are considered equal if their symbol is the same.
     */
    @Override
    public boolean equals( final Object obj ) {
        boolean retVal = false ;
        if( obj instanceof Symbol ) {
            final Symbol sbl = ( Symbol )obj ;
            if( sbl.getSymbol().equals( this.symbol ) ) {
                retVal = true ;
            }
        }
        return retVal ;
    }

    /**
     * @return The hash code of the symbol code.
     */
    @Override
    public int hashCode() {
        return this.symbol.hashCode() ;
    }
}
