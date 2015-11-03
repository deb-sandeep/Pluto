/**
 * Creation Date: Oct 14, 2008
 */

package com.sandy.apps.pluto.biz.svc.impl;
import java.text.DateFormat ;
import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.Calendar ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.dao.IITDIndexDAO ;
import com.sandy.apps.pluto.biz.dao.impl.PostGresUtil ;
import com.sandy.apps.pluto.biz.svc.IITDBulkImportSvc ;
import com.sandy.apps.pluto.biz.svc.IJobSvc ;
import com.sandy.apps.pluto.biz.svc.IJobSvc.JobState ;
import com.sandy.apps.pluto.biz.svc.INetworkSvc ;
import com.sandy.apps.pluto.shared.ErrorCode ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.STUtils ;
import com.sandy.apps.pluto.shared.dto.JobAttribute ;
import com.sandy.apps.pluto.shared.dto.JobConfig ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.dto.ScripITD ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;
import com.sandy.apps.pluto.shared.util.util.ConfigManager ;

/**
 * Imports intra-day data from exchanges and saves into local persistent
 * storage.
 * <p>
 * Please note that NSE exposes scrip ITD data in two flavors. The
 * first one is index based, for example it gives a periodic tabular summary
 * of the scrips belonging to the NIFTY scrips [please refer the following URL]
 * (http://www.nseindia.com/content/equities/niftywatch.htm) etc. At present
 * scrips belonging to the following indexes are published :
 * <ul>
 *  <li>Nifty</li>
 *  <li>Jr. Nifty</li>
 *  <li>CNX IT</li>
 *  <li>Bank Nifty</li>
 *  <li>NIFTY MIDCAP 50</li>
 *  <li>IL</li>
 * </ul>
 * This interface exposes the services of an operation which helps in downloading
 * ITD scrip values of stocks which are a part of the exposed indexes. If you are
 * interested in getting the values of scrips which are not a part of these
 * indexes, please use the IITDImportSvc implementation.
 * <p>
 * This job ignores the Scrips which are being pulled as a part of the Scrip ITD
 * job. To achieve this, this job checks the state of the Scrip ITD job and if
 * found to be running, gets the list of scrip attribute values. These values
 * are used to filter any scrip that is being downloaded as a part of the normal
 * index based bulk import. This is done because Scrip ITD job based data is
 * more accurate (and more costly). It is assumed that the user will use the
 * Scrip ITD for fetching only the portfolio related scrips or those scrips
 * which are being monitored closely.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ITDBulkImportSvc implements IITDBulkImportSvc {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ITDBulkImportSvc.class ) ;

    private static final String TECH_PROBLEM_STR     = "<html><head><title>Technical Difficulties</title>" ;
    private static final String AS_ON_STR            = "<td class=\"smalllinks\" align=\"right\" nowrap>As on " ;
    private static final String HOURS_IST_STR        = " hours IST" ;
    private static final String HOURS_IST_STR_UC     = " Hours IST" ;
    private static final String PRE_SCRIP_TOKEN      = "&flag=0\">" ;
    private static final String POST_SCRIP_TOKEN     = "</a></td>" ;
    private static final String PRE_VAL_TOKEN        = "<td class=\"t1\">" ;
    private static final String POST_VAL_TOKEN       = "</td>" ;

    /** The job id for the Scrip ITD job. */
    private static final Integer SCRIP_ITD_JOB_ID = new Integer( 1 ) ;

    /**
     * The configuration key against which the archival threshold is mentioned
     * in number of days relative to today.
     */
    final String CFG_KEY_ARCHIVAL_THRESHOLD = "scrip.itd.archive.days.threshold" ;

    /** The default value of the archival threshold. */
    final int DEF_ARCHIVAL_THRESHOLD = 30 ;

    /** The date format in which the time is specified. 06-AUG-2008 14:52:33 */
    private static final DateFormat LOW_RES_ITD_DF = new SimpleDateFormat( "dd-MMM-yyyy HH:mm:ss" ) ;

    /** A static map containing a mapping of the NSE url versus the index name. */
    private static final HashMap<String, String> INDEX_URL_MAP = new HashMap<String, String>() ;
    static {
        INDEX_URL_MAP.put( INDEX_NIFTY,           "http://www.nseindia.com/content/equities/niftywatch.htm" ) ;
        INDEX_URL_MAP.put( INDEX_JR_NIFTY,        "http://www.nseindia.com/content/equities/jrniftywatch.htm" ) ;
        INDEX_URL_MAP.put( INDEX_BANK_NIFTY,      "http://www.nseindia.com/content/equities/cnxbankwatch.htm" ) ;
        INDEX_URL_MAP.put( INDEX_CNX_IT,          "http://www.nseindia.com/content/equities/cnxitwatch.htm" ) ;
        INDEX_URL_MAP.put( INDEX_NIFTY_MIDCAP_50, "http://www.nseindia.com/content/equities/niftymidcap50watch.htm" ) ;
        INDEX_URL_MAP.put( INDEX_IL,              "http://www.nseindia.com/content/equities/ILwatch.htm" ) ;
    }

    private static class ParsingContext {
        public String contents ;
        public int    parsePos ;
    }

    /**
     * INJECTABLE: This variable should be injected with a reference to the
     * implementation of INetworkSvc interface.
     */
    private INetworkSvc networkSvc = null ;

    /**
     * INJECTABLE: This variable should be injected with a reference to the
     * IJobSvc implementation
     */
    private IJobSvc jobSvc = null ;

    /**
     * INJECTABLE: This variable should be injected with a reference to the
     * implementation of IITDImportSvc interface.
     */
    private IITDIndexDAO itdIndexDAO = null ;

    /** Public no argument constructor for ease of DI loading. */
    public ITDBulkImportSvc() {
        super() ;
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
     * @return the jobSvc
     */
    public IJobSvc getJobSvc() {
        return this.jobSvc ;
    }

    /**
     * @param jobSvc the jobSvc to set
     */
    public void setJobSvc( final IJobSvc jobSvc ) {
        this.jobSvc = jobSvc ;
    }

    /**
     * Imports the latest available intra day data from NSE exchange for the
     * scrips of the given index.
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
     *        one or more {@link ScripITD} instances.
     *
     * @param indexName The NSE index name for which NSE exposes tabular
     *        intraday data for participating scrips. If the index name is
     *        not one of the INDEX_* defined constants (ignore equal case),
     *        an exception will be raised.
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
    public void importNSEIndexScrips( final String indexName, final boolean ignoreError )
            throws STException {

        final String upperCaseIndexName = indexName.toUpperCase() ;
        final String scrapeURL = INDEX_URL_MAP.get( upperCaseIndexName ) ;

        if( scrapeURL == null ) {
            final String errMsg = "Invalid index name = " + indexName ;
            if( !ignoreError ) {
                throw new STException( errMsg, ErrorCode.INVALID_INDEX_NAME ) ;
            }
            else {
                logger.info( errMsg ) ;
                return ;
            }
        }

        try {
            final String contents = this.networkSvc.getGETResult( scrapeURL ) ;

            // Extract the contents of the price information table
            final int startIndex = contents.indexOf( AS_ON_STR ) ;
            if( startIndex != -1 ) {
                final List<ScripITD> itdValues = parseScripITD( contents ) ;
                this.itdIndexDAO.insert( itdValues ) ;

                // Publish an event notifying that Scrip ITD indexes have been
                // added. Note that the collection of ITD values have been
                // cleansed of erroneous values while persisting.
                EventBus.publish( EventType.EVT_SCRIP_ITD_INSERT, itdValues ) ;
            }
            else if( contents.indexOf( TECH_PROBLEM_STR ) != -1 ) {
                final String errMsg = "Server faced technical difficulties " +
                                      "for fetching ITD data scrips in index " +
                                      indexName ;
                logger.info( errMsg ) ;
                LogMsg.info( errMsg ) ;
            }
            else {
                logger.info( "Invaid server response for bulk ITD import" ) ;
                if ( !ignoreError ) {
                    throw new STException( "Invalid server response",
                            ErrorCode.ITD_IMPORT_FAILURE ) ;
                }
            }
        }
        catch ( final DataAccessException dae ) {
            if( !PostGresUtil.isPKViolation( dae ) ) {
                final String errMsg = "Failure inserting ITD data for scrips " +
                                      "in index " + indexName ;
                logger.warn( errMsg, dae ) ;
                if( !ignoreError ) {
                    throw new STException( errMsg, dae,
                                           ErrorCode.ITD_IMPORT_FAILURE ) ;
                }
            }
        }
        catch ( final Exception e ) {
            final String errMsg = "Failure inserting ITD data for scrips " +
                                  "in index " + indexName +
                                  ".Msg=" + e.getMessage() ;

            // Log the exception only if it is unanticipated.
            if( e instanceof STException ) {
                logger.warn( errMsg ) ;
            }
            else {
                logger.warn( errMsg, e ) ;
            }

            if( !ignoreError ) {
                throw new STException( errMsg, e,
                                       ErrorCode.ITD_IMPORT_FAILURE ) ;
            }
        }
    }

    /**
     * Returns the time of the current ScripITD from the HTML contents.
     * @param contents The HTML contents
     * @return The date at which this report was generated
     */
    private Date getLowResTime( final ParsingContext ctx ) {

        final int beginIndex = ctx.contents.indexOf( AS_ON_STR ) + AS_ON_STR.length() - 1 ;
        int endIndex = ctx.contents.indexOf( HOURS_IST_STR, beginIndex ) ;
        if( endIndex < beginIndex ) {
            endIndex = ctx.contents.indexOf( HOURS_IST_STR_UC, beginIndex ) ;
        }

        final String tmp = ctx.contents.substring( beginIndex, endIndex ).trim() ;
        Date date = null ;

        try {
            date = LOW_RES_ITD_DF.parse( tmp ) ;
            ctx.parsePos += endIndex + HOURS_IST_STR.length() ;
        }
        catch ( final Exception e ) {
            logger.warn( "Could not parse date '" + tmp + "'" ) ;
            date = new Date() ;
        }

        return date ;
    }

    /**
     * Parses the contents of the scraped contents and returns a list of
     * populated {@link ScripITD} instances.
     *
     * @param contents The scraped html contents from the NSE site.
     *
     * @return A list of {@link ScripITD} instances.
     */
    private List<ScripITD> parseScripITD( final String contents ) {

        final List<ScripITD> list = new ArrayList<ScripITD>() ;

        String      scripName   = null ;
        ScripITD    itd         = null ;
        Date        time        = null ;
        final List<String> itdJobScrips = new ArrayList<String>() ;

        final ParsingContext ctx = new ParsingContext() ;
        ctx.contents = contents ;
        ctx.parsePos = 0 ;

        time = getLowResTime( ctx ) ;

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
            logger.debug( "Got an invalid ITD scrip time, ignoring" ) ;
            logger.debug( "Invalid time = " + time ) ;
            return list ;
        }

        // Get the list of Scrips being currently managed by the Scrip ITD
        // Job. We need to ignore these scrips from the bulk import process
        final JobState state = this.jobSvc.getJobState( SCRIP_ITD_JOB_ID ) ;
        if( state == JobState.STARTED || state == JobState.EXECUTING ) {
            final JobConfig config = this.jobSvc.getJobConfig( SCRIP_ITD_JOB_ID ) ;
            final List<JobAttribute> attributes = config.getAttributeValues( "scrip" ) ;
            for( final JobAttribute attr : attributes ) {
                itdJobScrips.add( attr.getValue().trim() ) ;
            }
        }

        while( true ) {

            scripName = getNextScripName( ctx ) ;
            if( scripName != null ) {

                itd = new ScripITD() ;
                itd.setSymbolId( scripName ) ;
                itd.setTime( time ) ;

                // Opening price
                itd.setOpeningPrice( getNextValue( ctx ) ) ;

                // High price
                itd.setHigh( getNextValue( ctx ) ) ;

                // Low price
                itd.setLow( getNextValue( ctx ) ) ;

                // Last traded price
                itd.setPrice( getNextValue( ctx ) ) ;

                // Previous closing price
                itd.setPrevClose( getNextValue( ctx ) ) ;

                // % change
                itd.setPctChange( getNextValue( ctx ) ) ;

                // Total traded quantity
                itd.setTotalTradeQty( (long)getNextValue( ctx ) ) ;

                // Set the change in amount
                itd.setChange( itd.getPrice() - itd.getPrevClose() ) ;

                if( !itdJobScrips.contains( scripName ) ) {
                    list.add( itd ) ;
                }
                else {
                    if( logger.isDebugEnabled() ) {
                        logger.debug( "Scrip " + scripName + " is already " +
                                "scheduled for leeching by the Scrip ITD job " +
                                "Ignoring from Scrip ITD bulk import." ) ;
                    }
                }
            }
            else {
                break ;
            }
        }

        return list ;
    }

    /**
     * Gets the next scrip name present in the scrapped HTML from the position
     * provided. If no scrip name is found, this method returns a null.
     */
    private String getNextScripName( final ParsingContext ctx ) {
        String scripName = null ;
        final int startIndex = ctx.contents.indexOf( PRE_SCRIP_TOKEN, ctx.parsePos ) ;
        if( startIndex >= 0 ) {
            final int endIndex = ctx.contents.indexOf( POST_SCRIP_TOKEN, startIndex ) ;
            if( endIndex >= 0 ) {
                scripName = ctx.contents.substring( startIndex + PRE_SCRIP_TOKEN.length(), endIndex ) ;
                ctx.parsePos = endIndex + POST_SCRIP_TOKEN.length() ;
            }
        }
        return scripName ;
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

    /**
     * Archives all the data from STOCK_EOD_DATA table which are older than
     * the configured interval 'scrip.eod.archive.days.threshold'. The archived
     * records would be moved to the STOCK_EOD_DATA_ARCHIVE table and deleted
     * from the live table.
     * <p>
     * Note that the data is moved from live to archive table and deleted from
     * live table within one transaction boundary and hence if either the insert
     * or the delete fails, the transaction is rolled back. The transaction
     * boundary is set by Spring AOP configuration - no special consideration
     * for transaction isolation is done inside the method implementation.
     *
     * @throws STException In case an exception condition is encountered during
     *         the archival process.
     */
    public void archive() throws STException {

        logger.debug( "Archiving scrip EOD data" ) ;

        // Determine the date which will mark the boundary and render all the
        // data past the date, qualified for archival.
        final ConfigManager cfgMgr = ConfigManager.getInstance() ;
        final int threshold = cfgMgr.getInt( this.CFG_KEY_ARCHIVAL_THRESHOLD,
                                             this.DEF_ARCHIVAL_THRESHOLD ) ;

        final Calendar boundaryDate = Calendar.getInstance() ;
        boundaryDate.add( Calendar.DATE, -1*threshold ) ;
        final Date boundary = boundaryDate.getTime() ;
        logger.debug( "Archiving all Scrip ITD records prior to " + boundary ) ;

        // Ask the DAO to copy the data from live to archive table with one
        // query operation (insert with nested select)
        logger.debug( "Copying records to archive table" ) ;
        this.itdIndexDAO.archiveLiveRecords( boundary ) ;

        // Ask the DAO to delete all the data past the calculated date from the
        // live table
        logger.debug( "Deleting records from live table" ) ;
        this.itdIndexDAO.deleteLiveRecords( boundary ) ;
    }
}
