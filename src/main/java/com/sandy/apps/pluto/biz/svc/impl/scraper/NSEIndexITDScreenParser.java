/**
 * Creation Date: Oct 29, 2008
 */

package com.sandy.apps.pluto.biz.svc.impl.scraper;
import java.text.DateFormat ;
import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.svc.INetworkSvc ;
import com.sandy.apps.pluto.shared.ErrorCode ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.dto.ExIndexITD ;

/**
 * This class encapsulates the logic of parsing the screen scraped data for
 * intra day NSE index values and returns a list of {@link ExIndexITD} instances
 * or appropriate error values. The rationale of refactoring this class is to
 * ensure that the screen scraping logic remains localized and resilient to
 * changes.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class NSEIndexITDScreenParser {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( NSEIndexITDScreenParser.class ) ;

    /** The NSE URL for low resolution ITD index data. */
    private static final String NSE_ITD_LOW_RES_URL = "http://www.nseindia.com/marketinfo/indices/indexwatch.jsp" ;

    // String constants used to parse the low resolution screen scrap
    private static final String LOW_RES_TIME_PREFIX = "<td valign=top align=right class=smalllinks>" ;
    private static final String AS_ON_STR           = "As on " ;
    private static final String HOURS_IST_STR       = " hours IST" ;
    private static final String ROW_PREFIX          = "<td class=t0><a href=\"/content/indices/" ;
    private static final String ROW_PREFIX_ALT      = "<td class=t0><a href=\" /content/indices/" ;
    private static final String ROW_SUFFIX          = "</a></td>" ;
    private static final String ROW_SUFFIX_ALT      = "</a> </td>" ;
    private static final String PRE_VAL_TOKEN       = "<td class=t1>" ;
    private static final String POST_VAL_TOKEN      = "</td>" ;


    /** The date format in which the time is specified. 06-AUG-2008 14:52:33 */
    private static final DateFormat LOW_RES_ITD_DF = new SimpleDateFormat( "dd-MMM-yyyy HH:mm:ss" ) ;

    /** The network sevice reference. */
    private final INetworkSvc networkSvc ;

    /** Public no argument constructor. */
    public NSEIndexITDScreenParser( final INetworkSvc netSvc ) {
        super() ;
        this.networkSvc = netSvc ;
    }

    /**
     * Scrapes the NSE site for ITD index values at the instant of invocation.
     * Returns a list of {@link ExIndexITD} instances or throws an exception
     * in case the data could not be retrieved.
     *
     * @return A list of {@link ExIndexITD} instances.
     *
     * @throws STException In case an unanticipated exception was encountered
     *         while fetching the ITD contents.
     */
    public List<ExIndexITD> getExIndexITDValues() throws STException {

        logger.debug( "Importing low resolution index ITD data" ) ;

        final String contents = this.networkSvc.getGETResult( NSE_ITD_LOW_RES_URL ) ;
        final List<ExIndexITD> itdValues = new ArrayList<ExIndexITD>() ;

        if( contents.indexOf( LOW_RES_TIME_PREFIX ) != -1 ) {

            final ParsingContext ctx = new ParsingContext( contents ) ;

            final Date itdTime = getLowResTime( ctx ) ;
            if( itdTime != null ) {
                parseLowResITDValues( ctx, itdTime, itdValues ) ;
            }
        }
        else {
            throw new STException( "Invalid server response", ErrorCode.ITD_IMPORT_FAILURE ) ;
        }

        return itdValues ;
    }

    /**
     * Returns the time of the current ScripITD from the HTML contents.
     * @param contents The HTML contents
     * @return The date at which this report was generated
     */
    private Date getLowResTime( final ParsingContext ctx ) {

        Date date = null ;
        int beginIndex = ctx.contents.indexOf( LOW_RES_TIME_PREFIX ) ;
        if( beginIndex != -1 ) {
            beginIndex = ctx.contents.indexOf( AS_ON_STR, beginIndex ) + AS_ON_STR.length() ;
            final int endIndex = ctx.contents.indexOf( HOURS_IST_STR, beginIndex ) ;

            final String tmp = ctx.contents.substring( beginIndex, endIndex ).trim() ;

            try {
                date = LOW_RES_ITD_DF.parse( tmp ) ;
                ctx.parsePos = ( endIndex + HOURS_IST_STR.length() ) ;
            }
            catch ( final Exception e ) {
                logger.warn( "Could not parse date '" + tmp + "'" ) ;
                date = new Date() ;
            }
        }

        return date ;
    }

    /** Parses the ITD index values and adds the individual elements to the list. */
    private void parseLowResITDValues( final ParsingContext ctx, final Date time,
                                       final List<ExIndexITD> itdValues )
        throws STException {

        try {
            String indexName = getNextIndexName( ctx ) ;
            while( indexName != null ) {
                final ExIndexITD itdVal = new ExIndexITD() ;
                itdVal.setIndex( indexName ) ;
                itdVal.setDate( time ) ;

                // The table has the columns in the order :
                // Index Name, Arrow Image, Prev close, Open, High, Low, Last, %change
                // We have already got the index name, the arrow image column has
                // a different prefix and will be ignored by the getNextValue method.
                // Rest we try to populate in the ITD instance.

                itdVal.setPrevClose( getNextValue( ctx ) ) ;
                itdVal.setOpen( getNextValue( ctx ) ) ;

                // Ignore the High value - don't need them as of now
                getNextValue( ctx ) ;

                // Ignore the Low value - don't need them as of now
                getNextValue( ctx ) ;

                itdVal.setCurrentVal( getNextValue( ctx ) ) ;

                // Ignore the percentage change value
                getNextValue( ctx ) ;

                // Add the ITD value to the list
                itdValues.add( itdVal ) ;

                // Get the next index name for the next iteration.
                indexName = getNextIndexName( ctx ) ;
            }
        }
        catch ( final Exception e ) {
            throw new STException( "Parse exception", e, ErrorCode.ITD_IMPORT_FAILURE ) ;
        }
    }

    /**
     * Parses the contents of the context starting from the parse position and
     * returns the name of the next index. This method also updates the context
     * with the new parse position.
     *
     * @param ctx The current parsing context.
     *
     * @return The name of the next index or null if no index are found.
     */
    private String getNextIndexName( final ParsingContext ctx ) {
        String name = null ;

        int beginIndex    = ctx.contents.indexOf( ROW_PREFIX, ctx.parsePos ) ;
        final int beginIndexAlt = ctx.contents.indexOf( ROW_PREFIX_ALT, ctx.parsePos ) ;

        if( beginIndexAlt != -1 ) {
            beginIndex = ( beginIndex == -1 ) ? beginIndexAlt : beginIndex ;
            beginIndex = ( beginIndex <= beginIndexAlt ) ? beginIndex : beginIndexAlt ;
        }

        if( beginIndex != -1 ) {

            beginIndex += ROW_PREFIX.length() ;
            beginIndex = ctx.contents.indexOf( '>', beginIndex ) + 1 ;

            int endIndex    = ctx.contents.indexOf( ROW_SUFFIX, beginIndex ) ;
            final int endIndexAlt = ctx.contents.indexOf( ROW_SUFFIX_ALT, beginIndex ) ;

            if( endIndexAlt != -1 ) {
                endIndex = ( endIndex == -1 ) ? endIndexAlt : endIndex ;
                endIndex = ( endIndex <= endIndexAlt ) ? endIndex : endIndexAlt ;
            }

            name = ctx.contents.substring( beginIndex, endIndex ).trim() ;

            ctx.parsePos = ( endIndex + ROW_SUFFIX.length() ) ;
        }

        return name ;
    }

    /**
     * Gets the next scrip name present in the scrapped HTML from the position
     * provided. If no scrip name is found, this method returns a null.
     */
    private double getNextValue( final ParsingContext ctx ) {

        String value = null ;
        double retVal= 0.0D ;
        final int startIndex = ctx.contents.indexOf( PRE_VAL_TOKEN, ctx.parsePos ) ;
        if( startIndex >= 0 ) {
            final int endIndex = ctx.contents.indexOf( POST_VAL_TOKEN, startIndex ) ;
            if( endIndex >= 0 ) {
                value = ctx.contents.substring( startIndex + PRE_VAL_TOKEN.length(), endIndex ) ;
                ctx.parsePos = endIndex + POST_VAL_TOKEN.length() ;
            }
        }

        try {
            retVal = Double.parseDouble( value ) ;
        }
        catch ( final Exception e ) {
            retVal = -1.0 ;
        }

        return retVal ;
    }
}
