/**
 * Creation Date: Oct 29, 2008
 */

package com.sandy.apps.pluto.biz.svc.impl.scraper;
import java.text.DateFormat ;
import java.text.DecimalFormat ;
import java.text.ParseException ;
import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.Calendar ;
import java.util.Date ;
import java.util.List ;
import java.util.TimeZone ;

import org.apache.commons.lang.StringEscapeUtils ;
import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.svc.INetworkSvc ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.dto.WorldIndex ;

/**
 * This class encapsulates the logic of parsing the screen scraped data for
 * world indexes as exposed by Bloomberg.
 * <p>
 * http://www.bloomberg.com/markets/stocks/wei_region1.html
 * http://www.bloomberg.com/markets/stocks/wei_region2.html
 * http://www.bloomberg.com/markets/stocks/wei_region3.html
 * </p>
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class BloombergWIParser {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( BloombergWIParser.class ) ;

    private static final DecimalFormat DECIMAL_FMT     = new DecimalFormat( "###,###,###.##" ) ;
    private static final DecimalFormat DECIMAL_FMT_PCT = new DecimalFormat( "###.##%" ) ;

    // Bloomberg publishes all its data in eastern standard time and in the format
    // specified below. We will use these to parse the update page time.
    private static final TimeZone   TZ_ET  = TimeZone.getTimeZone ( "America/New_York" ) ;
    private static final DateFormat UPD_DF = new SimpleDateFormat( "MMM dd HH:mm" ) ;

    /**
     * The URLs where bloomberg exposes the world indexes. On the bloomberg
     * site, world markets are dividided into three regions. There is a URL for
     * each region. The page at the URL displays indexes categorized by
     * countries.
     */
    private static final String BBERG_WI_URL_LIST[] = {
        "http://www.bloomberg.com/markets/stocks/wei_region1.html",
        "http://www.bloomberg.com/markets/stocks/wei_region2.html",
        "http://www.bloomberg.com/markets/stocks/wei_region3.html"
    } ;

    // String fragments to identify parse regions
    private static final String INDEX_MARKER1    = "<td bgcolor=\"#FFFFFF\" align=\"left\" width=\"44%\"><a HREF=\"/apps/quote?ticker=" ;
    private static final String INDEX_MARKER2    = "<td bgcolor=\"#D2E1E8\" align=\"left\" width=\"44%\"><a HREF=\"/apps/quote?ticker=" ;
    private static final String COUNTRY_MARKER   = "<div class=\"roundtop\">" ;
    private static final String TAG_P            = "<p>" ;
    private static final String TAG_P_END        = "</p>" ;
    private static final String INDEX_NAME_START = ":IND\">" ;
    private static final String INDEX_NAME_END   = "</a>" ;
    private static final String VALUE_START      = "width=\"14%\">" ;
    private static final String VALUE_END        = "</td>" ;
    private static final String CHG_START1       = "<span class=\"changedown\">" ;
    private static final String CHG_START2       = "<span class=\"changeup\">" ;
    private static final String CHG_END          = "</span>" ;
    private static final String UPD_TIME_START   = "<b>Updated:&nbsp;&nbsp;</b><b>New York</b>," ;
    private static final String UPD_TIME_END     = "</div>" ;
    private static final String IDX_TIME_START   = "align=\"right\" width=\"11%\">" ;
    private static final String IDX_TIME_END     = "</td>" ;

    // The token type to identify what is the next type of token in the file
    private static enum TOKEN_TYPE { INDEX, COUNTRY, EOI } ;

    /** The network service reference. */
    private final INetworkSvc networkSvc ;

    /** Public no argument constructor. */
    public BloombergWIParser( final INetworkSvc netSvc ) {
        super() ;
        this.networkSvc = netSvc ;
    }

    /**
     * Scrapes the world index pages from Bloomberg and extracts {@link WorldIndex}
     * instances from the page contents. Note that bloomberg has multiple pages
     * for publishing world indexes. This method collates the index values from
     * all the pages. In case of a problem with any of the pages, the remaining
     * pages are parsed.
     *
     * @return A list of {@link WorldIndex} instances.
     *
     * @throws STException In case an unanticipated exception was encountered
     *         while fetching the ITD contents.
     */
    public List<WorldIndex> getWorldIndexValues() throws STException {

        logger.debug( "Importing world indexes from bloomberg" ) ;
        final List<WorldIndex> indexes = new ArrayList<WorldIndex>() ;

        // Parse all the URL pages and collate information before returning.
        // In case of an exception with any of the pages, ignore and move
        // forward.
        for( int i=0; i<BBERG_WI_URL_LIST.length; i++ ) {
            try {
                indexes.addAll( parsePage( BBERG_WI_URL_LIST[i] ) ) ;
            }
            catch ( final Exception e ) {
                logger.error( "Exception parsing " + BBERG_WI_URL_LIST[i], e ) ;
            }
        }

        return indexes ;
    }

    /**
     * Parses the specified URL and returns a list of world index instances.
     * The returned list is empty in case no indexes are found.
     *
     * @param url The bloomberg world index page to parse.
     * @return A list of world index instances or an empty list if no indexes
     *         are found. The returned value is never null.
     *
     * @throws STException In case of an unanticipated exception.
     */
    private List<WorldIndex> parsePage( final String url )
        throws STException {

        final List<WorldIndex> indexes  = new ArrayList<WorldIndex>() ;
        final String           contents = this.networkSvc.getGETResult( url ) ;
        final ParsingContext   context  = new ParsingContext( contents ) ;

        TOKEN_TYPE nextTok    = null ;
        String     country    = null ;
        Calendar   updateTime = null ;

        updateTime = getPageUpdateTime( context ) ;
        if( updateTime != null ) {
            while( ( nextTok = getNextTokenType( context ) ) != TOKEN_TYPE.EOI ) {
                if( nextTok == TOKEN_TYPE.COUNTRY ) {
                    country = parseCountry( context ) ;
                    logger.debug( country ) ;
                }
                else if( nextTok == TOKEN_TYPE.INDEX ) {
                    final WorldIndex index = parseIndex( context, country,
                                                         updateTime ) ;
                    logger.debug( "Index = " + index ) ;
                    indexes.add( index ) ;
                }
            }
        }

        return indexes ;
    }

    /**
     * Returns the next token type in the file from the current parse position.
     * If we have reached the end of file, this method returns a token type
     * of EOI. Note that this method does not change the state of the parse
     * context.
     *
     * @param context The parsing context
     * @return The token type of the next parse token
     */
    private TOKEN_TYPE getNextTokenType( final ParsingContext context ) {

        int countryPos = 0 ;
        int indexPos1  = 0 ;
        int indexPos2  = 0 ;
        int indexPos   = 0 ;

        TOKEN_TYPE nextTok = TOKEN_TYPE.EOI ;

        // Get the next position of country header and index rows.
        countryPos = context.contents.indexOf( COUNTRY_MARKER, context.parsePos ) ;
        indexPos1  = context.contents.indexOf( INDEX_MARKER1,  context.parsePos ) ;
        indexPos2  = context.contents.indexOf( INDEX_MARKER2,  context.parsePos ) ;

        // The minimum of valid positions provide an indication of the next
        // type of token. If both the index positions are -1, it implies that
        // there are no more index rows. This is the same as reaching the end of
        // input.
        if( indexPos1 != -1 || indexPos2 != -1 ) {
            if( indexPos1 != -1 && indexPos2 != -1 ) {
                indexPos = Math.min( indexPos1, indexPos2 ) ;
            }
            else {
                indexPos = ( indexPos1 != -1 )? indexPos1 : indexPos2 ;
            }

            if( countryPos != -1 ) {
                nextTok = ( countryPos < indexPos ) ? TOKEN_TYPE.COUNTRY :
                                                      TOKEN_TYPE.INDEX ;
            }
            else {
                nextTok = TOKEN_TYPE.INDEX ;
            }
        }

        return nextTok ;
    }

    /**
     * Parses the next country from the parsing context and returns the name of
     * the country. This method returns a null if no more country headers exist
     * from the current parse position.
     */
    private String parseCountry( final ParsingContext context ) {

        String countryName = null ;
        int    countryPos  = 0 ;
        int    startIndex  = 0 ;
        int    endIndex    = 0 ;

        countryPos = context.contents.indexOf( COUNTRY_MARKER, context.parsePos ) ;
        if( countryPos != -1 ) {
            startIndex = context.contents.indexOf( TAG_P, context.parsePos ) ;
            if( startIndex != -1 ) {
                startIndex += TAG_P.length() ;
                endIndex = context.contents.indexOf( TAG_P_END, startIndex ) ;
                if( endIndex != -1 ) {
                    countryName = context.contents.substring( startIndex, endIndex ) ;
                    context.parsePos = endIndex + TAG_P_END.length() ;
                }
            }
        }

        return countryName ;
    }

    /**
     * Parses the next index from the parsing context and returns an instance
     * of {@link WorldIndex}. This method returns a null if no more index
     * rows are found from the current parse position.
     */
    private WorldIndex parseIndex( final ParsingContext context,
                                   final String country,
                                   final Calendar pageUpdateTime ) {

        WorldIndex index   = null ;
        int indexPos1   = 0 ;
        int indexPos2   = 0 ;
        int indexPos    = 0 ;

        indexPos1  = context.contents.indexOf( INDEX_MARKER1,  context.parsePos ) ;
        indexPos2  = context.contents.indexOf( INDEX_MARKER2,  context.parsePos ) ;

        // If both the index positions are -1, implies we have reached the
        // end of file.
        if( indexPos1 == -1 && indexPos2 == -1 ) return null ;

        // Find the index of the closest index row
        if( indexPos1 != -1 && indexPos2 != -1 ) {
            indexPos = Math.min( indexPos1, indexPos2 ) ;
        }
        else {
            indexPos = ( indexPos1 != -1 )? indexPos1 : indexPos2 ;
        }

        // Increment the parse position. Market1 and 2 are both of the same size
        context.parsePos = indexPos + INDEX_MARKER1.length() ;
        index = new WorldIndex() ;

        index.setCountry( country ) ;
        index.setIndexName( parseIndexName( context ) ) ;
        index.setValue( parseValue( context ) ) ;
        index.setChange( parseChange( context ) ) ;
        index.setPctChange( parsePctChange( context ) ) ;

        // Parse the time and populate the ITD/EOD flag and local time
        // If the parsing is not successful, return a null
        if( !parseIndexUpdateTime( context, pageUpdateTime, index ) ) {
            return null ;
        }

        return index ;
    }

    /**
     * Parses the index name from the current parse position.
     */
    private String parseIndexName( final ParsingContext context ) {

        String indexName  = null ;
        int    startIndex = 0 ;
        int    endIndex   = 0 ;

        startIndex = context.contents.indexOf( INDEX_NAME_START, context.parsePos ) ;
        if( startIndex != -1 ) {
            startIndex += INDEX_NAME_START.length() ;
            endIndex  = context.contents.indexOf( INDEX_NAME_END, startIndex ) ;
            indexName = context.contents.substring( startIndex, endIndex ) ;
            indexName = StringEscapeUtils.unescapeHtml( indexName ) ;

            context.parsePos = endIndex + INDEX_NAME_END.length() ;
        }

        return indexName ;
    }

    /**
     * Parses the index value from the parsing context
     */
    private float parseValue( final ParsingContext context ) {

        String tmpStr     = null ;
        float  value      = 0.0F ;
        int    startIndex = 0 ;
        int    endIndex   = 0 ;

        startIndex = context.contents.indexOf( VALUE_START, context.parsePos ) ;
        if( startIndex != -1 ) {
            startIndex += VALUE_START.length() ;
            endIndex    = context.contents.indexOf( VALUE_END, startIndex ) ;
            tmpStr      = context.contents.substring( startIndex, endIndex ) ;

            try {
                value = DECIMAL_FMT.parse( tmpStr ).floatValue() ;
            }
            catch ( final ParseException e ) {
                logger.warn( "Could not parse number " + tmpStr ) ;
            }

            context.parsePos = endIndex + VALUE_END.length() ;
        }

        return value ;
    }

    /**
     * Parses the index change value from the parsing context
     */
    private float parseChange( final ParsingContext context ) {

        String tmpStr = null ;
        float  value  = Float.MIN_VALUE ;

        tmpStr = getChangeStrStartIndex( context ) ;
        try {
            if( tmpStr != null ) {
                value = DECIMAL_FMT.parse( tmpStr ).floatValue() ;
            }
        }
        catch ( final ParseException e ) {
            logger.warn( "Could not parse number " + tmpStr ) ;
        }
        return value ;
    }

    /**
     * Parses the index change value from the parsing context
     */
    private float parsePctChange( final ParsingContext context ) {

        String tmpStr = null ;
        float  value  = Float.MIN_VALUE ;

        tmpStr = getChangeStrStartIndex( context ) ;
        try {
            if( tmpStr != null ) {
                value = DECIMAL_FMT_PCT.parse( tmpStr ).floatValue() ;
                value *= 100 ;
            }
        }
        catch ( final ParseException e ) {
            logger.warn( "Could not parse number " + tmpStr ) ;
        }
        return value ;
    }

    /**
     * Returns the change string (change & %change) from the current parse
     * position. Why such big song and dance? That's because - the change
     * string, (both change and % change) have two qualifiers "changeup" and
     * "changedown". Depending upon which one comes first we decide to position
     * our start index. Since this logic is reused for both change and %change,
     * we have refactoried it out.
     *
     * @param context The parsing context
     *
     * @return The start index from which the change string can be extracted.
     *         null if the change string can not be found.
     */
    private String getChangeStrStartIndex( final ParsingContext context ) {

        String changeStr  = null ;
        int    start1     = 0 ;
        int    start2     = 0 ;
        int    startIndex = Integer.MIN_VALUE ;
        int    endIndex   = 0 ;

        start1 = context.contents.indexOf( CHG_START1, context.parsePos ) ;
        start2 = context.contents.indexOf( CHG_START2, context.parsePos ) ;

        if( start1 == -1 ) {
            startIndex = start2 + CHG_START2.length() ; ;
        }
        else if( start2 == -1 ) {
            startIndex = start1 + CHG_START1.length() ;
        }
        else if( start1 < start2 ) {
            startIndex = start1 + CHG_START1.length() ;
        }
        else if( start2 < start1 ){
            startIndex = start2 + CHG_START2.length() ;
        }

        if( startIndex != Integer.MIN_VALUE ) {
            endIndex   = context.contents.indexOf( CHG_END, startIndex ) ;
            changeStr  = context.contents.substring( startIndex, endIndex ) ;
            context.parsePos = endIndex + CHG_END.length() ;
            changeStr = changeStr.trim() ;
        }

        return changeStr ;
    }

    /**
     * Parses the time at which the index value was updated. Bloomberg publishes
     * the index update time in either of the two formats HH:mm or MM/dd.
     * HH:mm implies that the market is opening and this value is an intraday
     * value, while MM/dd implies that the market is closed and this is an end
     * of day value.
     *
     * We update the attributes of the input {@link WorldIndex} instance with
     * the knowledge derived from the above logic.
     *
     * @param context The parsing context
     * @param pageUpdateTime The page update time
     * @param wIndex The instance of {@link WorldIndex} which needs to be
     *        updated.
     *
     * @return true if successfully parsed, false otherwise.
     */
    private boolean parseIndexUpdateTime( final ParsingContext context,
                                          final Calendar pageUpdateTime,
                                          final WorldIndex wIndex ) {
        boolean status     = false ;
        int     startIndex = -1 ;
        int     endIndex   = -1 ;
        String  tmpStr     = null ;
        Date    time       = null ;

        try {
            startIndex = context.contents.indexOf( IDX_TIME_START, context.parsePos ) ;
            if( startIndex != -1 ) {
                startIndex += IDX_TIME_START.length() ;
                endIndex = context.contents.indexOf( IDX_TIME_END, startIndex ) ;
                if( endIndex != -1 ) {
                    tmpStr = context.contents.substring( startIndex, endIndex ) ;
                    tmpStr = StringEscapeUtils.unescapeHtml( tmpStr ).trim() ;

                    if( tmpStr.indexOf( '/' ) != -1 ) {
                        // This is a EOD value
                        wIndex.setItd( false ) ;

                        // Convert the time into MM/dd/YYYY format so that we can
                        // parse it easily.
                        final int year = pageUpdateTime.get( Calendar.YEAR ) ;
                        tmpStr = tmpStr + "/" + year ;
                        time = new SimpleDateFormat( "MM/dd/yyyy" ).parse( tmpStr ) ;

                        wIndex.setTime( time ) ;
                    }
                    else {
                        wIndex.setItd( true ) ;
                        final String[] parts = tmpStr.split( ":" ) ;
                        pageUpdateTime.set( Calendar.HOUR_OF_DAY,
                                            Integer.parseInt( parts[0] ) ) ;
                        pageUpdateTime.set( Calendar.MINUTE,
                                            Integer.parseInt( parts[1] ) ) ;

                        wIndex.setTime( pageUpdateTime.getTime() ) ;
                    }

                    context.parsePos = endIndex + IDX_TIME_END.length() ;
                    status = true ;
                }
            }
        }
        catch ( final ParseException e ) {
            logger.error( "Could not parse date " + tmpStr ) ;
        }

        return status ;
    }

    /**
     * Returns the time at which the page was updated by Bloomberg. The update
     * time is published as a HTML fragment on the top of the page. This
     * function should be the first one to be called while parsing a page.
     *
     * @param context The parsing context
     *
     * @return The publish time or null if the publish time is not found.
     */
    private Calendar getPageUpdateTime( final ParsingContext context ) {

        int     startIndex = -1 ;
        int     endIndex   = -1 ;
        String  tmpStr     = null ;

        final Calendar cal = Calendar.getInstance() ;

        try {
            startIndex = context.contents.indexOf( UPD_TIME_START, context.parsePos ) ;
            if( startIndex != -1 ) {
                startIndex += UPD_TIME_START.length() ;
                endIndex = context.contents.indexOf( UPD_TIME_END, startIndex ) ;
                if( endIndex != -1 ) {
                    tmpStr = context.contents.substring( startIndex, endIndex ) ;
                    tmpStr = StringEscapeUtils.unescapeHtml( tmpStr ).trim() ;
                    tmpStr = tmpStr.replace( (char)0xA0, (char)0x20 ) ;

                    final Date date = UPD_DF.parse( tmpStr ) ;

                    cal.setTime( date ) ;
                    cal.set( Calendar.YEAR, Calendar.getInstance().get( Calendar.YEAR ) ) ;
                    cal.setTimeZone( TZ_ET ) ;

                    context.parsePos = endIndex + UPD_TIME_END.length() ;
                }
            }
        }
        catch ( final ParseException e ) {
            logger.error( "Could not parse date " + tmpStr, e ) ;
            return null ;
        }

        return cal ;
    }
}
