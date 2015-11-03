/**
 * Creation Date: Aug 9, 2008
 */

package com.sandy.apps.pluto.biz.svc.impl;

import java.io.IOException ;
import java.text.ParseException ;
import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;
import java.util.Vector ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

import org.apache.log4j.Logger ;

import com.csvreader.CsvReader ;
import com.sandy.apps.pluto.biz.dao.IExIndexDAO ;
import com.sandy.apps.pluto.biz.svc.IExIndexSvc ;
import com.sandy.apps.pluto.biz.svc.INetworkSvc ;
import com.sandy.apps.pluto.biz.svc.impl.scraper.NSEIndexITDScreenParser ;
import com.sandy.apps.pluto.shared.ConfigKey ;
import com.sandy.apps.pluto.shared.ErrorCode ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.STUtils ;
import com.sandy.apps.pluto.shared.dto.ChartData ;
import com.sandy.apps.pluto.shared.dto.ExIndexEOD ;
import com.sandy.apps.pluto.shared.dto.ExIndexITD ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;
import com.sandy.apps.pluto.shared.util.util.ConfigManager ;

/**
 * Implementation of the {@link IExIndexSvc} interface.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ExIndexSvc implements IExIndexSvc {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ExIndexSvc.class ) ;

    /** The URL which emits historical NSE index data. */
    private static final String NSE_HIST_INDEX_URL =
        "http://www.nseindia.com/marketinfo/indices/histdata/historicalindices.jsp" ;

    /** The NSE homepage where the NSE index intra day values are published. */
    private static final String NSE_ITD_CHARTDATA_URL = "http://www.nseindia.com/chartdata" ;

    /** A pattern which identifies the CSV link to be downloaded. */
    private static final String CSV_LINK_PATTERN_STR =
        "<a href=\"/content/indices/histdata/(.*)\" target=\"_blank\">Download file in csv format</a>" ;

    /** The URL prefix to prepend to the CSV download file. */
    private static final String CSV_DOWNLOAD_URL_PREFIX =
        "http://www.nseindia.com/content/indices/histdata/" ;

    /** A regexp pattern for the CSV link pattern. */
    public static final Pattern CSV_LINK_PATTERN = Pattern.compile( CSV_LINK_PATTERN_STR ) ;

    /** The date format used for posting date range for historic EOD data. */
    private static final SimpleDateFormat NSE_POST_DF = new SimpleDateFormat( "dd-MM-yyyy" ) ;

    /** The date format used in CSV files. */
    private static final SimpleDateFormat CSV_DF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;

    /**
     * INJECTABLE: This variable should be injected with a reference to the
     * implementation of IExIndexDAO interface
     */
    private IExIndexDAO exIndexDAO = null ;

    /**
     * INJECTABLE: This reference to the network service
     */
    private INetworkSvc networkSvc = null ;

    /** Public constructor. */
    public ExIndexSvc() {
        super() ;
    }

    /**
     * Imports EOD index data for the specified index for the range of dates
     * specified.
     *
     * @param indexName The name of the index
     * @param fromDate The start of the date range.
     * @param toDate The end of the date range
     *
     * @throws STException If an exception is encountered during the process
     *         of import. If a primary key violation results from the import,
     *         the exception is silently gobbled. Any other exception gets
     *         translated as an instance of STException with the underlying
     *         exception wrapped as the root cause of the exception.
     */
    public void importEODIndices( final String indexName, final Date fromDate,
                                  final Date toDate )
        throws STException {

        String response = null ;

        logger.debug( "Downloading EOD index data for " + indexName +
                      " from " + fromDate + " to " + toDate ) ;
        try {
            // NSE historic index download is a two page transition, the first
            // page results after the post, which gives a link to the CSV
            // file download. First post the data...
            final Map<String, String> data = new HashMap<String, String>() ;
            data.put( "indexType",   indexName ) ;
            data.put( "fromDate",    NSE_POST_DF.format( fromDate ) ) ;
            data.put( "toDate",      NSE_POST_DF.format( toDate ) ) ;
            data.put( "check",       "new" ) ;

            response = this.networkSvc.getPOSTResult( NSE_HIST_INDEX_URL, data ) ;
            // From the post response - extract the path of the CSV file
            final Matcher matcher = CSV_LINK_PATTERN.matcher( response ) ;
            if ( matcher.find() ) {
                final String fileName = matcher.group( 1 ).replace( " ", "%20" ) ;
                response = this.networkSvc.getPOSTResult( CSV_DOWNLOAD_URL_PREFIX + fileName ) ;

                final List<ExIndexEOD> exIndexEODList = parseIndexEOD( response, indexName ) ;

                // Sort the collection such that the EOD instances are in ascending
                // order of their time value. Note that all the instances in
                // this case belong to the same index name.
                Collections.sort( exIndexEODList ) ;

                // The data exported by NSE does not contain the previous closing
                // values explicitly. We will have to derive them from the
                // data we already have
                populatePrevCloseValues( exIndexEODList ) ;

                for( final ExIndexEOD index : exIndexEODList ) {
                    this.exIndexDAO.addEODData( index ) ;
                }
            }
            else {
                if( response.indexOf( "<font class=header3> No Records </font>" ) != -1 ) {
                    logger.debug( "No EOD Index data for index " + indexName +
                                  " for the range " + NSE_POST_DF.format( fromDate ) +
                                  " to " + NSE_POST_DF.format( toDate ) ) ;
                }
                else {
                    throw new STException( "Unanticipated response from server",
                                           ErrorCode.DOWNLOAD_FAILURE ) ;
                }
            }
        }
        catch( final STException ste ) {
            throw ste ;
        }
        catch( final IOException ioe ) {
            throw new STException( "Parsing of CSV contents failed", ioe,
                                   ErrorCode.EOD_IMPORT_FAILURE ) ;
        }
        catch( final Exception e ) {
            throw new STException( e, ErrorCode.DOWNLOAD_FAILURE ) ;
        }
    }

    /** Populates the EOD instances with the previous closing values. */
    private void populatePrevCloseValues( final List<ExIndexEOD> eodList ) {

        int index = 0 ;
        double prevClose = 0 ;
        for( final ExIndexEOD eod : eodList ) {
            if( index == 0 ) {
                // Get the previous close from the database
                final ExIndexEOD lastEod = this.exIndexDAO.getLatestEOD( eod.getIndex() ) ;
                if( lastEod != null ) {
                    eod.setPrevClose( lastEod.getClose() ) ;
                }
                prevClose = eod.getClose() ;
            }
            else {
                eod.setPrevClose( prevClose ) ;
                prevClose = eod.getClose() ;
            }
            index++ ;
        }
    }

    /**
     * Parses the contents of exchange EOD and returns a list of {@link ExIndexEOD}
     * instances.
     *
     * @param response The CSV contents of EOD index data
     * @param indexName The name of the index for whom the index data has been
     *        downloaded.
     *
     * @return A list of {@link ExIndexEOD} instances.
     */
    private List<ExIndexEOD> parseIndexEOD( final String response,
                                            final String indexName )
       throws IOException {

        final List<ExIndexEOD> eodIndices = new ArrayList<ExIndexEOD>() ;
        ExIndexEOD  eodIndex  = null ;

        final CsvReader csvReader = CsvReader.parse( response ) ;

        csvReader.readHeaders() ;

        while( csvReader.readRecord() ) {
            final String[] colVals = csvReader.getValues() ;
            try {
                eodIndex = new ExIndexEOD() ;
                eodIndex.setIndex( indexName ) ;

                // NSE CSV has the following format
                // Column - 1: Date
                eodIndex.setDate( CSV_DF.parse( colVals[0].trim() ) ) ;

                // Column - 2: Open price
                eodIndex.setOpen( Double.parseDouble( colVals[1].trim() ) ) ;

                // Column - 3: High price
                eodIndex.setHigh( Double.parseDouble( colVals[2].trim() ) ) ;

                // Column - 4: Low price
                eodIndex.setLow( Double.parseDouble( colVals[3].trim() ) ) ;

                // Column - 5: Close
                eodIndex.setClose( Double.parseDouble( colVals[4].trim() ) ) ;

                eodIndices.add( eodIndex ) ;
            }
            catch ( final ParseException e ) {
                // If we have an exception - we ignore this EOD index.
                logger.error( "Parse exception. Msg=" + e.getMessage() ) ;
                logger.debug( "Parse exception.", e ) ; ;
            }
        }
        return eodIndices ;
    }

    /**
     * Imports ITD index data for the specified NSE index name. All the
     * available indexes since the last index time will be imported into the
     * persistent storage. This operation uses the high resolution (Chart)
     * data feed and hence is costly in terms of network usage. Typically this
     * method will fetch around 100K of data from server for one index. If we
     * use this for fetching ITD values at 30 sec intervals - it will cost us
     * around 60 MB of data per day, per index. On the other hand, this operation
     * is capable of backfilling the index values in case of outages. Ideally,
     * this operation should be run at 2 hour intervals to ensure that the
     * gaps are eradicated. For real time ITD values, the usage of
     * importLowResITDIndices is recommended.
     *
     * @param indexName The name of the index
     * @param lastIndexTime The latest time for which we have the ITD data
     *        for this index. If the last index time is passed as null, this
     *        function refreshes all the intra day index values.
     *
     * @event {@link EventType#EVT_NSE_INDEX_ITD_INSERT} This event is published
     *        when one or more Index ITD values have been inserted during this
     *        import operation. The value of the event is a list of Index ITD
     *        values in the form of {@link ExIndexITD} instances.
     *
     * @return The last imported index time.
     *
     * @throws STException If an exception is encountered during the process
     *         of import. If a primary key violation results from the import,
     *         the exception is silently gobbled. Any other exception gets
     *         translated as an instance of STException with the underlying
     *         exception wrapped as the root cause of the exception.
     */
    @SuppressWarnings("unchecked")
    public synchronized Date importHiResITDIndices( final String indexName, final Date lastIndexTime )
        throws STException {

        // Keep the formatted last time with us for comparison
        String lastTimeStr = null ;
        if( lastIndexTime != null ) {
            lastTimeStr = ITD_DF.format( lastIndexTime ) ;
        }

        // Get the ITD data resolution in milliseconds. If we receive two data
        // points with more resolution than the specified configuration, we
        // ignore the latest data point.
        final ConfigManager cfgMgr = ConfigManager.getInstance() ;
        final int resolution = cfgMgr.getInt( ConfigKey.NSE_INDEX_ITD_RESOLUTION, 30000 ) ;

        // Create the parameters to be sent to the server
        final Map<String, String> params = new HashMap<String, String>() ;
        params.put( "indexname", indexName ) ;
        params.put( "charttype", "ONLINE_INDEX" ) ;

        // Get the chart data from the server.
        LogMsg.info( "Downloading hi res ITD data for " + indexName ) ;
        final ChartData chartData = this.networkSvc.getChartDataGET(
                                               NSE_ITD_CHARTDATA_URL, params ) ;

        final Vector<String> timeSeries = chartData.getIndexData()[0] ;
        final Vector<String> valueSeries= chartData.getIndexData()[1] ;
        final double         prevClose  = Double.parseDouble( chartData.getPrevClose() ) ;

        ExIndexITD itdIndex = null ;
        String timeStr          = null ;
        String valStr           = null ;
        Date   latestITDTime    = null ;
        Date   lastITDTime      = null ;

        final List<ExIndexITD> itdValues = new ArrayList<ExIndexITD>() ;
        try {
            // Deduce the opening price. Opening price is the value of the first
            // time entry. Note that the chart data does not give us explictly
            // the value of the opening price.
            final double open = Double.parseDouble( valueSeries.get( 0 ) ) ;

            // There is a problem with NSE's way of exposing data over the web
            // There are cases when the date is inaccurate, causing the ITD value
            // to represent a time either in the past or the future. This causes
            // malicious entries and hinders data analysis. We try to filter out
            // the bad entries before persisting them.
            final Date startOfDay    = STUtils.getStartOfDay( new Date() ) ;
            final long beginingOfBiz = startOfDay.getTime() + (9*60*60*1000) ;
            final long endOfBiz      = startOfDay.getTime() + (17*60*60*1000) ;

            // Iterate backwards to catch the ITD indexes since the last save time
            LogMsg.info( "Saving hi res ITD data for " + indexName ) ;
            for( int i=timeSeries.size()-1; i>=0; i-- ) {

                timeStr = timeSeries.get( i ) ;
                valStr  = valueSeries.get( i ) ;

                // Save the latest ITD time. This will be returned back.
                if( i == timeSeries.size()-1 ) {
                    latestITDTime = ITD_DF.parse( timeStr ) ;
                }

                // If we have already processed the ITD indexes from this
                // point till back in the past - we break out of the loop.
                if( lastTimeStr != null && timeStr.equals( lastTimeStr ) ) {
                    break ;
                }

                itdIndex = new ExIndexITD() ;
                itdIndex.setDate( ITD_DF.parse( timeStr ) ) ;
                itdIndex.setIndex( chartData.getIndexName() ) ;
                itdIndex.setCurrentVal( Double.parseDouble( valStr ) ) ;
                itdIndex.setPrevClose( prevClose ) ;
                itdIndex.setOpen( open ) ;

                // Save the index value only if it is received outside the
                // resolution period
                if( ( lastITDTime == null ) ||
                    ( lastITDTime.getTime() - itdIndex.getDate().getTime() ) > resolution ) {

                    // Save the data if and only if it lies within the business
                    // day we are operating against. A safeguard to prevent
                    // vague entries from entering into our data model.
                    if( itdIndex.getDate().getTime() > beginingOfBiz &&
                        itdIndex.getDate().getTime() < endOfBiz ) {

                        this.exIndexDAO.addITDData( itdIndex ) ;
                        itdValues.add( itdIndex ) ;
                        lastITDTime = itdIndex.getDate() ;
                    }
                }
            }

            // We publish an event intimating that high resolution data has been
            // downloaded for the index. The value of this event is the index
            // name. Note that this is not the same as the EVT_INDEX_ITD_INSERT
            // which is generated for bulk imports and low resolution imports.
            // Subscribers are expected to fetch the data from the database, if
            // required.
            EventBus.publish( EventType.EVT_HI_RES_NSE_INDEX_ITD_INSERT, indexName ) ;
        }
        catch ( final Exception e ) {
            logger.error( "Could not parse the date string " + timeStr ) ;
            logger.debug( "Date parse error for " + timeStr, e ) ;
        }

        return latestITDTime ;
    }

    /**
     * Imports low resolution ITD index data for the specified NSE index name.
     * This operation uses the low resolution (Screen Scraping) data feed and hence
     * is cheap in terms of network usage. Typically this method will fetch around
     * 10K of data from server for all the fundamental indexes. If we
     * use this for fetching ITD values at 30 sec intervals - it will cost us
     * around 6 MB of data per day, for all indexes. On the other hand, this
     * operation is NOT capable of backfilling the index values in case of outages.
     *
     * @event {@link EventType#EVT_NSE_INDEX_ITD_INSERT} This event is published
     *        when one or more Index ITD values have been inserted during this
     *        import operation. The value of the event is a list of Index ITD
     *        values in the form of {@link ExIndexITD} instances.
     *
     * @throws STException If an exception is encountered during the process
     *         of import. If a primary key violation results from the import,
     *         the exception is silently gobbled. Any other exception gets
     *         translated as an instance of STException with the underlying
     *         exception wrapped as the root cause of the exception.
     */
    public void importLowResITDIndices()
        throws STException {

        final NSEIndexITDScreenParser parser = new NSEIndexITDScreenParser( this.networkSvc ) ;
        try {
            final List<ExIndexITD> itdValues = parser.getExIndexITDValues() ;
            final List<ExIndexITD> insertedList = new ArrayList<ExIndexITD>() ;

            // Add all the values one by one and ignore individual exceptions.
            if( !itdValues.isEmpty() ) {
                // There is a problem with NSE's way of exposing data over the web
                // There are cases when the date is inaccurate, causing the ITD value
                // to represent a time either in the past or the future. This causes
                // malicious entries and hinders data analysis. We try to filter out
                // the bad entries before persisting them.
                final Date startOfDay    = STUtils.getStartOfDay( new Date() ) ;
                final long beginingOfBiz = startOfDay.getTime() + (9*60*60*1000) ;
                final long endOfBiz      = startOfDay.getTime() + (17*60*60*1000) ;

                for( final ExIndexITD itdVal : itdValues ) {
                    boolean success = false ;
                    try {
                        // Save the data if and only if it lies within the business
                        // day we are operating against. A safeguard to prevent
                        // vague entries from entering into our data model.
                        if( itdVal.getDate().getTime() > beginingOfBiz &&
                            itdVal.getDate().getTime() < endOfBiz ) {

                            success = this.exIndexDAO.addITDData( itdVal ) ;
                            if( success ) {
                                insertedList.add( itdVal ) ;
                            }
                        }
                    }
                    catch ( final Exception e ) {
                        logger.error( "NSE ITD Index data import failure", e ) ;
                    }
                }

                // Publish the event that ITD index values have been inserted.
                // The value of this event is a list of index ITD values that
                // have been added in this iteration.
                if( !insertedList.isEmpty() ) {
                    EventBus.publish( EventType.EVT_NSE_INDEX_ITD_INSERT, itdValues ) ;
                }
            }
        }
        catch ( final STException e ) {
            final String errMsg = "Low resolution index ITD fetch failed. " +
                                  "Msg = " + e.getMessage()  ;
            // Log the exception only if the exception in unanticipated.
            if( e instanceof STException ) {
                logger.info( errMsg ) ;
            }
            else {
                logger.info( errMsg, e ) ;
            }
            LogMsg.error( errMsg ) ;
        }
    }

    /**
     * Returns a list of all the EOD index data for the specified index in
     * a sorted fashion in the ascending order of their dates.
     *
     * @param index The name of the index for which the EOD data is required.
     *
     * @return A list of {@link ExIndexEOD} instances sorted in the ascending
     *         order of their dates.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         process of data access
     */
    @Override
    public List<ExIndexEOD> getExIndexEODList( final String index )
        throws DataAccessException {
        return this.exIndexDAO.getExIndexEODList( index ) ;
    }

    /**
     * Returns a list of the EOD index data for the date range specified
     * for the specified index in a sorted fashion in the ascending order of their
     * dates.
     *
     * @param index The name of the index for which the EOD data is required.
     * @param fromDate The start of the date range
     * @param toDate The end of the date range
     *
     * @return A list of {@link ExIndexEOD} instances sorted in the ascending
     *         order of their dates.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         process of data access
     */
    @Override
    public List<ExIndexEOD> getExIndexEODList( final String index,
                                               final Date fromDate,
                                               final Date toDate )
        throws DataAccessException {
        return this.exIndexDAO.getExIndexEODList( index, fromDate, toDate ) ;
    }

    /**
     * Returns a list of the ITD index data for the date range specified
     * for the specified index in a sorted fashion in the ascending order of their
     * dates.
     *
     * @param index The name of the index for which the EOD data is required.
     * @param fromDate The start of the date range
     * @param toDate The end of the date range
     *
     * @return A list of {@link ExIndexITD} instances sorted in the ascending
     *         order of their dates.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         process of data access
     */
    public List<ExIndexITD> getExIndexITDList( final String name,
                                               final Date startTime,
                                               final Date endTime ) {
        return this.exIndexDAO.getExIndexITDList( name, startTime, endTime ) ;
    }

    /**
     * Returns a list of all the indexes for a given exchange.
     *
     * @param exchangeName The name of the exchange
     *
     * @return A list containing the names of the indexes for the given
     *         exchange
     *
     * @throws DataAccessException If an exception is encountered during the
     *         process of data access
     */
    @Override
    public List<String> getIndexNames( final String exchangeName )
            throws DataAccessException {
        return this.exIndexDAO.getIndexNames( exchangeName ) ;
    }

    /**
     * @return the exIndexDAO
     */
    public IExIndexDAO getExIndexDAO() {
        return this.exIndexDAO ;
    }

    /**
     * @param exIndexDAO the exIndexDAO to set
     */
    public void setExIndexDAO( final IExIndexDAO exIndexDAO ) {
        this.exIndexDAO = exIndexDAO ;
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
}
