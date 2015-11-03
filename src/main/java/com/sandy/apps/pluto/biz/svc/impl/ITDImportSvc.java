/**
 * Creation Date: Aug 6, 2008
 */

package com.sandy.apps.pluto.biz.svc.impl;
import java.text.DateFormat ;
import java.text.ParseException ;
import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;
import java.util.Vector ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.dao.IEODIndexDAO ;
import com.sandy.apps.pluto.biz.dao.IITDIndexDAO ;
import com.sandy.apps.pluto.biz.dao.ISymbolDAO ;
import com.sandy.apps.pluto.biz.dao.impl.PostGresUtil ;
import com.sandy.apps.pluto.biz.svc.IITDImportSvc ;
import com.sandy.apps.pluto.biz.svc.INetworkSvc ;
import com.sandy.apps.pluto.shared.ConfigKey ;
import com.sandy.apps.pluto.shared.ErrorCode ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.STUtils ;
import com.sandy.apps.pluto.shared.dto.ChartData ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.dto.ScripITD ;
import com.sandy.apps.pluto.shared.dto.Symbol ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;
import com.sandy.apps.pluto.shared.util.util.ConfigManager ;

/**
 * Implementation of {@link IITDImportSvc} interface.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ITDImportSvc implements IITDImportSvc {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ITDImportSvc.class ) ;

    /** The NSE homepage where the NSE index intra day values are published. */
    private static final String NSE_ITD_CHARTDATA_URL = "http://www.nseindia.com/chartdata" ;

    /**
     * INJECTABLE: This variable should be injected with a reference to the
     * implementation of ISymbolDAO interface.
     */
    private ISymbolDAO symbolDAO = null ;

    /**
     * INJECTABLE: This variable should be injected with a reference to the
     * implementation of INetworkSvc interface.
     */
    private INetworkSvc networkSvc = null ;

    /**
     * INJECTABLE: This variable should be injected with a reference to the
     * implementation of IITDImportSvc interface.
     */
    private IITDIndexDAO itdIndexDAO = null ;

    /**
     * INJECTABLE: This variable should be injected with a reference to the
     * implementation of IEODIndexDAO interface.
     */
    private IEODIndexDAO eodIndexDAO = null ;

    /** The URL to use for fetching NSE intra day data. */
    private static final String NSE_ITD_URL =
                 "http://www.nseindia.com/marketinfo/equities/cmquote_tab.jsp" ;

    // Some static constants to make parsing of the data simpler.
    private static final String TECH_PROBLEM_STR     = "<html><head><title>Technical Difficulties</title>" ;
    private static final String PRICE_INFO_TABLE_HDR = "<th class=specialhead colspan=2> Price Information</th>" ;
    private static final String PRICE_INFO_ATTR_HDR  = "<th class=specialhead2 align=\"LEFT\">" ;
    private static final String AS_ON_STR            = "<img src=\"/images/trans.gif\" height=\"1\" width=\"210\" />As on " ;
    private static final String HOURS_IST_STR        = " Hours IST" ;
    private static final String TABLE_END_TAG = "</table>" ;
    private static final String TH_END_TAG    = "</th>" ;
    private static final String B_START_TAG   = "<b>" ;
    private static final String B_END_TAG     = "</b>" ;

    // The column headers for price information attributes
    private static final String PI_OPEN             = "Open" ;
    private static final String PI_HIGH             = "High" ;
    private static final String PI_LOW              = "Low" ;
    private static final String PI_LAST_PRICE       = "Last Price" ;
    private static final String PI_PREV_CLOSE       = "Prev. Close" ;
    private static final String PI_CHANGE           = "Change" ;
    private static final String PI_PCT_CHANGE       = "% Change" ;
    private static final String PI_TOTAL_TRD_QTY    = "Total traded quantity" ;

    /** The date format in which the time is specified. 06-AUG-2008 14:52:33 */
    private static final DateFormat LOW_RES_ITD_DF = new SimpleDateFormat( "dd-MMM-yyyy HH:mm:ss" ) ;

    /** The date format used to specify the time for the ITD index values. */
    private static final DateFormat HI_RES_ITD_DF = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" ) ;

    /** Public constructor. */
    public ITDImportSvc() {
        super() ;
    }

    /**
     * @return the symbolDAO
     */
    public ISymbolDAO getSymbolDAO() {
        return this.symbolDAO ;
    }

    /**
     * @param symbolDAO the symbolDAO to set
     */
    public void setSymbolDAO( final ISymbolDAO symbolDAO ) {
        this.symbolDAO = symbolDAO ;
    }

    /**
     * @return the networkSvc
     */
    public INetworkSvc getNetworkSvc() {
        return this.networkSvc ;
    }

    /**
     * @param networkSvc the networkSvc to set
     */
    public void setNetworkSvc( final INetworkSvc networkSvc ) {
        this.networkSvc = networkSvc ;
    }

    /**
     * @return the itdIndexDAO
     */
    public IITDIndexDAO getItdIndexDAO() {
        return this.itdIndexDAO ;
    }

    /**
     * @param itdIndexDAO the itdIndexDAO to set
     */
    public void setItdIndexDAO( final IITDIndexDAO itdIndexDAO ) {
        this.itdIndexDAO = itdIndexDAO ;
    }

    /**
     * @return the eodIndexDAO
     */
    public IEODIndexDAO getEodIndexDAO() {
        return this.eodIndexDAO ;
    }

    /**
     * @param eodIndexDAO the eodIndexDAO to set
     */
    public void setEodIndexDAO( final IEODIndexDAO eodIndexDAO ) {
        this.eodIndexDAO = eodIndexDAO ;
    }

    /**
     * Imports the latest available intra day data from NSE exchange for the
     * given symbol. Note that NSE requires two parameters, symbol and key to
     * fetch the ticker details. For example, for Steel Authority of India,
     * the symbol is SAIL and the key is SAILEQN. The data regarding the key
     * and symbol is stored in the SYMBOL table.
     * <p>
     * NOTE: The following points are to be kept in mind while using this
     * operation:
     * <ul>
     *  <li>The ITD data which is published by NSE at the instant of invoking
     *      this operation is imported.</li>
     *  <li>This operation relies on screen scraping to pick up the data.</li>
     *  <li>Rich version of the intraday data is obtained, which includes
     *      price, volume, high, low etc.</li>
     *  <li>The network overhead of this operation is in the order of 0.5 KB</li>
     *  <li>NSE refreshes the data in intervals of 30 seconds and hence it
     *      is not economical to call this operation at a higher frequency
     *      than twice a minute</li>
     *  <li>This operation will not fetch past ITD information for the day</li>
     * </ul>
     *
     * @event An EVT_SCRIP_ITD_INSERT event is generated on successful insert.
     *        The value of the event will be a List containing instances of
     *        one or more {@link ScripITD} instances. In this case, it will
     *        be a list with only one element.
     *
     * @param sybmol The NSE symbol for which the intra day data needs to be
     *        fetched. The key will be constructed by this operation based on
     *        the series and market type of the scrip.
     *
     * @param ignoreError A boolean flag indicating if any errors encountered
     *        during the process have to be silently ignored.
     *
     * @throws STException If the operation encountered an exception while
     *         fetching the intra day data. If the ignoreError parameter is set
     *         to true, any exceptions encountered will be silently ignored
     *         with a log entry made at INFO level.
     */
    @Override
    public void importNSESymbol( final String symbol, final boolean ignoreError )
            throws STException {

        final Symbol symb = this.symbolDAO.getSymbol( symbol ) ;
        if( symb == null ) {
            logger.error( "Unknown symbol for ITD import " + symbol ) ;
            if( !ignoreError ) {
                throw new STException( "Unknown symbol " + symbol,
                                       ErrorCode.ITD_IMPORT_FAILURE ) ;
            }
            else {
                return ;
            }
        }

        // Try to get the contents of the page
        final Map<String, String> paramMap = new HashMap<String, String>() ;
        paramMap.put( "symbol", symb.getSymbol() ) ;
        paramMap.put( "key", symb.getSymbol() + symb.getSeries() + symb.getMarketType() ) ;
        paramMap.put( "flag", "0" ) ;

        String contents = null ;
        try {
            contents = new String( this.networkSvc.getRawGETResult( NSE_ITD_URL, paramMap ) ) ;

            // Extract the contents of the price information table
            final int startIndex = contents.indexOf( PRICE_INFO_TABLE_HDR ) ;
            if( startIndex != -1 ) {
                final int endIndex = contents.indexOf( TABLE_END_TAG, startIndex ) ;
                final String piContents = contents.substring(
                                      startIndex + PRICE_INFO_TABLE_HDR.length(),
                                      endIndex ) ;

                final ScripITD itdIndex = createLowResITDIndex( piContents, symbol ) ;

                if( itdIndex != null ) {
                    // Now we associate the time with the ScripITD instance.
                    final Date time = getLowResTime( contents ) ;

                    // There is a problem with NSE's way of exposing data over the web
                    // There are cases when the date is inaccurate, causing the ITD value
                    // to represent a time either in the past or the future. This causes
                    // malicious entries and hinders data analysis. We try to filter out
                    // the bad entries before persisting them.
                    final Date startOfDay    = STUtils.getStartOfDay( new Date() ) ;
                    final long beginingOfBiz = startOfDay.getTime() + (9*60*60*1000) ;
                    final long endOfBiz      = startOfDay.getTime() + (17*60*60*1000) ;

                    // Anything which is not of today, we ignore.
                    if( time.getTime() < beginingOfBiz ||
                        time.getTime() > endOfBiz ) {
                        logger.error( "Got an invalid ITD scrip time, ignoring" ) ;
                        logger.error( "Invalid time = " + time ) ;
                        return ;
                    }

                    itdIndex.setTime( time ) ;
                    this.itdIndexDAO.insert( itdIndex ) ;

                    final List<ScripITD> insertedVals = new ArrayList<ScripITD>() ;
                    insertedVals.add( itdIndex ) ;
                    EventBus.publish( EventType.EVT_SCRIP_ITD_INSERT, insertedVals ) ;
                }
            }
            else if( contents.indexOf( TECH_PROBLEM_STR ) != -1 ) {
                logger.debug( "Server faced technical difficulties with the request" ) ;
                LogMsg.info( "Server faced technical problems for Scrip ITD request - " + symb.getSymbol() ) ;
            }
            else {
                throw new Exception( "Invalid ITD data for symbol " + symbol ) ;
            }
        }
        catch ( final DataAccessException dae ) {
            if( !PostGresUtil.isPKViolation( dae ) ) {
                logger.warn( "Failure fetching ITD data for " + symbol, dae ) ;
                if( !ignoreError ) {
                    throw new STException( "ITD import failure for " + symbol, dae,
                                           ErrorCode.ITD_IMPORT_FAILURE ) ;
                }
            }
        }
        catch ( final Exception e ) {
            logger.warn( "Failure fetching ITD data for " + symbol +
                         ". Msg=" + e.getMessage() ) ;
            if( !ignoreError ) {
                throw new STException( "ITD import failure for " + symbol, e,
                                       ErrorCode.ITD_IMPORT_FAILURE ) ;
            }
        }
    }

    /**
     * Creates an instance of {@link ScripITD} from the HTML contents as
     * leeched from the NSE web site.
     *
     * @param contents The HTML contents leeched from the NSE site.
     * @param symbol The symbol for which this ScripITD is being created.
     *
     * @return An index of {@link ScripITD} class encapsulating the interested
     *         data in the screen scrape or null if the page is not a valid
     *         page.
     */
    private ScripITD createLowResITDIndex( final String contents, final String symbol ) {

        final ScripITD index = new ScripITD() ;
        index.setSymbolId( symbol ) ;

        index.setChange(        getLowResPriceInfo( PI_CHANGE, contents ) ) ;
        index.setHigh(          getLowResPriceInfo( PI_HIGH, contents ) ) ;
        index.setLow(           getLowResPriceInfo( PI_LOW, contents ) ) ;
        index.setPctChange(    getLowResPriceInfo( PI_PCT_CHANGE, contents ) ) ;
        index.setPrevClose(     getLowResPriceInfo( PI_PREV_CLOSE, contents ) ) ;
        index.setPrice(         getLowResPriceInfo( PI_LAST_PRICE, contents ) ) ;
        index.setTotalTradeQty( ( long )getLowResPriceInfo( PI_TOTAL_TRD_QTY, contents ) ) ;
        index.setOpeningPrice(  ( long )getLowResPriceInfo( PI_OPEN, contents ) ) ;

        return index ;
    }

    /**
     * Returns the time of the current ScripITD from the HTML contents.
     * @param contents The HTML contents
     * @return The date at which this report was generated
     */
    private Date getLowResTime( final String contents ) {

        final int beginIndex = contents.indexOf( AS_ON_STR ) + AS_ON_STR.length() - 1 ;
        final int endIndex   = contents.indexOf( HOURS_IST_STR, beginIndex ) ;
        final String tmp     = contents.substring( beginIndex, endIndex ) ;
        Date date = null ;

        try {
            date = LOW_RES_ITD_DF.parse( tmp ) ;
        }
        catch ( final Exception e ) {
            logger.warn( "Could not parse date '" + tmp + "'" ) ;
            date = new Date() ;
        }

        return date ;
    }

    /**
     * Returns the value of the price information attribute from the contents.
     *
     * @param attr The price info attribute whose value is required
     * @param content The HTML content of the price information table.
     * @return
     */
    private double getLowResPriceInfo( final String attr, final String content ) {

        final String colHdrStr = PRICE_INFO_ATTR_HDR + attr + TH_END_TAG ;
        int beginIndex  = 0 ;
        int endIndex    = 0 ;
        double val      = 0 ;

        if( attr.equals( PI_LAST_PRICE ) ) {
            beginIndex = content.indexOf( PI_LAST_PRICE ) ;
            beginIndex = content.indexOf( B_START_TAG, beginIndex ) + B_START_TAG.length() - 1;
            endIndex   = content.indexOf( B_END_TAG, beginIndex ) ;
        }
        else {
            beginIndex = content.indexOf( colHdrStr ) + colHdrStr.length() ;
            beginIndex = content.indexOf( '>', beginIndex ) ;
            endIndex   = content.indexOf( '<', beginIndex ) ;
        }

        final String tmp = content.substring( beginIndex+1, endIndex ) ;
        try {
            val = Double.parseDouble( tmp ) ;
        }
        catch (final Exception e) {
            val = 0.0D ;
        }
        return val ;
    }

    /**
     * Imports ITD values for the given symbol at a very high level of time
     * resolution. The high resolution claims its price in terms of the lack
     * of richness of the data. Only the price value and the time is obtained
     * from the server and the rest of the information is interpolated in
     * memory. Although the interpolated data is good for analysis - it should
     * be kept in mind that we are dealing with interpolated data. Whether an
     * instance of ScripITD is interpolated can be ascertained by calling
     * upon the isInterpolated operation.
     * <p>
     * Note that even if the data is interpolated, the time and price markers are
     * genuine. The resolution of the persisted data can be controlled via
     * the "nse.scrip.itd.resolution" configuration parameter.
     * <p>
     * NOTE: The following points are to be kept in mind while using this
     * operation:
     * <ul>
     *  <li>This operation imports the data from the start of the day and not
     *      just the instant data. Hence this operation can be invoked in
     *      a batch mode to import and refresh ITD data at the end of day
     *      with a high degree of resolution.</li>
     *  <li>This operation does not rely upon screen scraping.</li>
     *  <li>A less rich version of the intraday data is obtained, which includes
     *      price and time. Rest of the data is interpolated.</li>
     *  <li>The network overhead of this operation is in the order of 1-2 KB</li>
     *  <li>NSE provides high resolution intraday data in the order of 4-6
     *      seconds.</li>
     *  <li>This operation will fetch past ITD information for the day</li>
     * </ul>
     *
     * @event An EVT_SCRIP_ITD_INSERT event is generated on successful insert.
     *        The value of the event will be a List containing instances of
     *        one or more {@link ScripITD} instances.
     *
     * @param sybmol The NSE symbol for which the intra day data needs to be
     *        fetched. The key will be constructed by this operation based on
     *        the series and market type of the scrip.
     *
     * @param ignoreError A boolean flag indicating if any errors encountered
     *        during the process have to be silently ignored.
     *
     * @throws STException If the operation encountered an exception while
     *         fetching the intra day data. If the ignoreError parameter is set
     *         to true, any exceptions encountered will be silently ignored
     *         with a log entry made at INFO level.
     */
    @SuppressWarnings("unchecked")
    public synchronized void importHighResNSESymbol( final String symbol, final boolean ignoreError )
        throws STException {

        final Symbol symb = this.symbolDAO.getSymbol( symbol ) ;
        if( symb == null ) {
            logger.error( "Unknown symbol for high resolution ITD import " + symbol ) ;
            if( !ignoreError ) {
                throw new STException( "Unknown symbol " + symbol,
                                       ErrorCode.ITD_IMPORT_FAILURE ) ;
            }
            else {
                return ;
            }
        }

        // Try to get the contents of the page
        final Map<String, String> paramMap = new HashMap<String, String>() ;
        paramMap.put( "charttype", "ONLINE_STOCK" ) ;
        paramMap.put( "symbol",  symb.getSymbol() ) ;
        paramMap.put( "series",  symb.getSeries() ) ;
        paramMap.put( "mkttype", symb.getMarketType() ) ;

        try {
            LogMsg.info( "Downloading hi res ITD data for " + symbol ) ;
            final ChartData chartData = this.networkSvc.getChartDataGET(
                                             NSE_ITD_CHARTDATA_URL, paramMap ) ;
            // Do error checking of the data
            if( !checkHiResDataValidity( chartData, ignoreError ) ) {
                return ;
            }

            // Figure out for which date have we received the data. Note that
            // NSE preserves the ITD data over the weekend and holidays
            final Date date = getHiResDate( chartData ) ;

            // Get the list of ITD indexes for the date from the DAO
            final List<ScripITD> itdList = this.itdIndexDAO.getScripITD( symbol, date ) ;

            // Create a list of ScriptITD based on the high resolution data
            // we have received, filtering them on the basis of the resolution
            // configuration. Mark these instances as interpolated. Add the
            // newly created instances to the list of ITD scrip data received.
            final Vector<String> timeList = chartData.getStockData()[0] ;
            final Vector<String> priceList= chartData.getStockData()[1] ;
            ScripITD itdIndex = null ;
            for( int i=0; i<timeList.size(); i++ ) {
                itdIndex = new ScripITD() ;
                itdIndex.setSymbolId( symbol ) ;
                itdIndex.setTime( HI_RES_ITD_DF.parse( timeList.get( i ) ) ) ;
                itdIndex.setPrice( Double.parseDouble( priceList.get( i ) ) ) ;
                itdIndex.setInterpolated( true ) ;
                itdIndex.setPrevClose( Double.parseDouble( chartData.getPrevClose() ) ) ;
                itdIndex.setChange( itdIndex.getPrice() - itdIndex.getPrevClose() ) ;
                itdIndex.setPctChange( (itdIndex.getChange()/itdIndex.getPrevClose())*100 ) ;
                itdIndex.setTotalTradeQty( -1 ) ;

                // If we already have a data point, we don't add one again.
                if( !itdList.contains( itdIndex ) ) {
                    itdList.add( itdIndex ) ;
                }
            }

            // Sort the script list in the order of time
            Collections.sort( itdList ) ;

            // Enforce resolution. Knock out the items from the list which are
            // spaced closer than the configured ITD resolution interval.
            long lastTime = -1 ;
            final long resolution = ConfigManager.getInstance().getLong(
                                    ConfigKey.NSE_SCRIP_ITD_RESOLUTION, 6000 ) ;
            for( final Iterator<ScripITD> itdIter =itdList.iterator(); itdIter.hasNext(); ) {
                final ScripITD tmpScrip = itdIter.next() ;
                if( lastTime == -1 ) {
                    lastTime = tmpScrip.getTime().getTime() ;
                }
                else {
                    if( (tmpScrip.getTime().getTime() - lastTime) > resolution ) {
                        lastTime = tmpScrip.getTime().getTime() ;
                    }
                    else {
                        itdIter.remove() ;
                    }
                }
            }

            // Save the list and publish events while saving.
            LogMsg.info( "Saving hi res ITD data for " + symbol ) ;
            this.itdIndexDAO.insert( itdList ) ;

            // We publish an event intimating that high resolution data has been
            // downloaded for the scrip. The value of this event is the scrip
            // name. Note that this is not the same as the EVT_SCRIP_ITD_INSERT
            // which is generated for bulk imports and low resolution imports.
            // Subscribers are expected to fetch the data from the database, if
            // required.
            EventBus.publish( EventType.EVT_HI_RES_SCRIP_ITD_INSERT, symbol ) ;
        }
        catch ( final Exception e ) {
            logger.warn( "Failure fetching ITD data for " + symbol +
                         ". Msg=" + e.getMessage() ) ;
            if( !ignoreError ) {
                throw new STException( "ITD import failure for " + symbol, e,
                                       ErrorCode.ITD_IMPORT_FAILURE ) ;
            }
        }
    }

    /**
     * @return The date for which this high resolution chart data is received.
     *         The returned date is truncated to 12:00:00 AM of the date.
     */
    private Date getHiResDate( final ChartData data )
        throws STException {

        Date date = null ;
        final String firstTime = ( String )data.getStockData()[0].get( 0 ) ;
        try {
            date = HI_RES_ITD_DF.parse( firstTime ) ;
            date = STUtils.getStartOfDay( date ) ;
        }
        catch ( final ParseException e ) {
            logger.error( "Unable to parse date", e ) ;
            throw new STException( "Date parse failure", e, ErrorCode.ITD_IMPORT_FAILURE ) ;
        }

        return date ;
    }

    /**
     * Checks if the high resolution data received from NSE site is valid. If
     * not this method throws an exception if the ignore error parameter is false,
     * or returns a false.
     *
     * @param chartData The data received from the server
     * @param ignoreError A boolean flag indicating if an exception should
     *        be generated or a status value should be returned.
     *
     * @return true if the data is valid, false if the data is invalid and ignore
     *         error is set to true, false otherwise.
     *
     * @throws STException If the data is invalid and a request has been made
     *         not to ignore the error.
     */
    private boolean checkHiResDataValidity( final ChartData chartData,
                                            final boolean ignoreError )
        throws STException {

        boolean retVal = false ;

        if( chartData != null ) {
            if( chartData.getStockData() != null &&
                chartData.getStockData().length == 2 &&
                !chartData.getStockData()[0].isEmpty() ) {
                retVal = true ;
            }
        }
        else {
            if( !ignoreError ) {
                throw new STException( "Invalid high resolution data received",
                                       ErrorCode.ITD_IMPORT_FAILURE ) ;
            }
        }

        return retVal ;
    }
}
