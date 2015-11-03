/**
 * Creation Date: Jul 27, 2008
 */

package com.sandy.apps.pluto.biz.svc.impl;
import java.io.BufferedReader ;
import java.io.File ;
import java.io.IOException ;
import java.io.StringReader ;
import java.text.DateFormat ;
import java.text.ParseException ;
import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.Calendar ;
import java.util.Collections ;
import java.util.Date ;
import java.util.List ;
import java.util.StringTokenizer ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.dao.IEODIndexDAO ;
import com.sandy.apps.pluto.biz.dao.ISymbolDAO ;
import com.sandy.apps.pluto.biz.svc.IEODImportSvc ;
import com.sandy.apps.pluto.biz.svc.INetworkSvc ;
import com.sandy.apps.pluto.shared.ConfigKey ;
import com.sandy.apps.pluto.shared.ErrorCode ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.dto.ScripEOD ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.util.util.ConfigManager ;
import com.sandy.apps.pluto.shared.util.util.StringUtil ;
import com.sandy.apps.pluto.shared.util.util.ZipUtils ;
import com.sandy.common.util.FileUtils ;
import com.sandy.common.util.IOUtils ;

/**
 * Implementation of {@link IEODImportSvc}.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class EODImportSvc implements IEODImportSvc {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( EODImportSvc.class ) ;

    /** URL prefix for downloading daily bhavcopy. */
    private static final String URL_BHAVCOPY_PREFIX =
                        "http://www.nseindia.com/content/historical/EQUITIES/" ;

    /**
     * A string constant indicating that the downloaded content is not a valid
     * bhavcopy.
     */
    private static final String BHAVCOPY_NOT_FOUND = "<H1>Not Found</H1>" ;

    /**
     * INJECTABLE: This reference should be injected with the IEODIndexDAO
     * implementation.
     */
    private IEODIndexDAO eodIndexDAO = null ;

    /**
     * INJECTABLE: This reference should be injected with the ISymbolDAO
     * implementation.
     */
    private ISymbolDAO symbolDAO = null ;

    /**
     * INJECTABLE: This reference to the network service
     */
    private INetworkSvc networkSvc = null ;

    /** Public no argument constructor. */
    public EODImportSvc() {
        super() ;
    }

    /**
     * @param eodIndexDAO the eodIndexDAO to set
     */
    public void setEodIndexDAO( final IEODIndexDAO eodIndexDAO ) {
        this.eodIndexDAO = eodIndexDAO;
    }

    /**
     * @return the eodIndexDAO
     */
    public IEODIndexDAO getEodIndexDAO() {
        return this.eodIndexDAO;
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
     * This operation bhavcopy data for the specified date. The raw bhavcopy
     * data is first downloaded and saved onto the HISTORIC_EOD directory and
     * then the raw data is imported from the hard disk.
     *
     * @param date The date for which the bhavcopy is to be imported
     *
     * @throws STException If an exception is encountered during the import
     *         process.
     */
    public void importBhavcopyEODData( final Date date )
        throws STException {

        // First download the bhavcopy contents of this date
        final String savePath = downloadNSEEquityBhavcopy( date ) ;

        // Now import the downloaded contents.
        if( importBhavcopyEODData( savePath ) ) {
            // If we have successfully imported the bhavcopy for the given date,
            // let us publish the notification on the system event bus. This will
            // allow any interested party to process the latest bhavcopy data
            EventBus.publish( EventType.EVT_BHAVCOPY_IMPORT_SUCCESS, date ) ;

            logger.info( "Imported Bhavcopy for date " + NSE_CSV_DF.format( date ) ) ;
        }
        else {
            logger.warn( "Bhavcopy not found for date " +
                          IEODImportSvc.NSE_CSV_DF.format( date ) ) ;
        }
    }

    /**
     * Download the historic Bhavcopy Equity data for the date provided.
     * This function checks for the existence of the configuration
     * parameter 'nse.eod.historic.download.location'. If this key is not
     * found, this function stores the downloaded file in the user's home
     * directory, else it downloads the file into a folder named "BHAVCOPY"
     * inside the folder specified. Example of a URL for downloading the
     * bhavcopy is as follows:
     * <p>
     * http://www.nseindia.com/content/historical/EQUITIES/2008/AUG/cm04AUG2008bhav.csv.zip
     *
     * @param date The date for which the bhavcopy needs to be downloaded
     *
     * @return The absolute path of the file where the data has been downloaded.
     *
     * @throws STException If an exception is encountered during the import
     *         process.
     */
    private String downloadNSEEquityBhavcopy( final Date date )
        throws STException {

        logger.debug( "Downloading bhavcopy for date " + NSE_CSV_DF.format( date ) ) ;

        final DateFormat DMF    = new SimpleDateFormat( "MMM" ) ;
        final String     month  = DMF.format( date ).toUpperCase() ;
        final String     urlFmt = "yyyy/'" + month +"'/'cm'dd'" + month + "'yyyy'bhav.csv.zip'" ;
        final DateFormat DF     = new SimpleDateFormat( urlFmt ) ;

        // Create the URL based on the date
        final StringBuffer url = new StringBuffer( URL_BHAVCOPY_PREFIX ) ;
        url.append( DF.format( date ) ) ;

        // Now download the contents of the URL into a String - note that
        // the bhavcopy is never huge, approximately 70KB worth of data. We
        // can directly load it into memory
        logger.debug( "Getting bhavcopy from URL " + url.toString() ) ;
        final byte[] bhavcopyContent = this.networkSvc.getRawGETResult( url.toString() ) ;

        final ConfigManager cfgMgr = ConfigManager.getInstance() ;
        final String saveDir = cfgMgr.getString( ConfigKey.EOD_DOWNLOAD_DIR,
                                                 System.getProperty( "user.dir" ) ) ;

        final File dir = new File( saveDir ) ;
        if( !dir.exists() ) {
            if( !dir.mkdirs() ) {
                throw new STException( "Could not create directory " + dir,
                                       ErrorCode.DOWNLOAD_FAILURE ) ;
            }
        }
        else if( !dir.isDirectory() ) {
            throw new STException( "Path is not a directory - " + dir,
                                   ErrorCode.DOWNLOAD_FAILURE ) ;
        }

        final String fileName = "BHAVCOPY_" + NSE_CSV_DF.format( date ) + ".csv" ;
        final File file = new File( dir, fileName ) ;
        try {
            final byte[] data = ZipUtils.unpackContentsUsingZip( bhavcopyContent ) ;
            IOUtils.writeFile( data, file.getAbsolutePath() ) ;
        }
        catch ( final Exception e ) {
            throw new STException( "Could not write contents to file " +
                                   file.getAbsolutePath(), e,
                                   ErrorCode.DOWNLOAD_FAILURE ) ;
        }

        return file.getAbsolutePath() ;
    }

    /**
     * This operation imports the data as contained in the file specified. The
     * format of the data is conveyed through the format parameter. The data
     * is expected in the NSE bhavcopy format.
     *
     * @param filePath The absolute file path from where to load the data.
     *
     * @param format The format of data contained in the file.
     *
     * @return true if the bhavcopy for the specified date was imported
     *         successfully, false otherwise.
     *
     * @throws STException If an exception is encountered during the import
     *         process.
     */
    private boolean importBhavcopyEODData( final String filePath )
        throws STException {

        boolean result = false ;
        try {
            logger.debug( "Importing bhavcopy from " + filePath ) ;
            final String fileContents = FileUtils.readFileToString( new File( filePath ) ) ;
            if( StringUtil.isEmptyOrNull( fileContents ) ||
                fileContents.contains( BHAVCOPY_NOT_FOUND ) ||
                fileContents.length() < 100 ) {
                // This implies that the downloaded bhavopy did not have any
                // records. This can happen if we have encountered an exchange
                // holiday. In this case, just skip population and return a
                // false, indicating no records were imported.
                logger.debug( "No records found in " + filePath ) ;
            }
            else {
                final List<ScripEOD> eodIndices = parseBhavcopy( fileContents ) ;
                this.eodIndexDAO.insert( eodIndices ) ;
                result = true ;
            }
        }
        catch( final NumberFormatException ne ) {
            // This will result when the file could not be parsed. Typical
            // cases are when the stock exchange was closed e.g. Saturday,
            // Sunday or national holidays. We simply ignore this error
            logger.error( "Could not parse file " + filePath ) ;
        }
        catch ( final IOException e ) {
            throw new STException( "File read failure - " + filePath,
                                   ErrorCode.EOD_IMPORT_FAILURE ) ;
        }
        return result ;
    }

    /**
     * Parses the contents as EOD indices in the NSE Bhavcopy format.
     *
     * @param contents The CSV file contents of NSE Bhavcopy export.
     *
     * @return A list of {@link ScripEOD} DTO instances.
     */
    private List<ScripEOD> parseBhavcopy( final String contents )
        throws IOException {

        // If the contents are not valid bhavcopy contents, return an empty
        // list. This can happen if we are trying to fetch bhavcopy for a holiday
        // in which case the server would return a not found page.
        if( !checkBhavcopyValidity( contents ) ) {
            return Collections.emptyList() ;
        }

        final List<ScripEOD> eodIndices = new ArrayList<ScripEOD>() ;
        final BufferedReader reader = new BufferedReader( new StringReader( contents ) ) ;
        String          line      = null ;
        StringTokenizer tokenizer = null ;
        boolean         firstLine = true ;
        ScripEOD        eodIndex  = null ;
        while( ( line = reader.readLine() ) != null ) {
            // Skip the first line in the file - this is the header line.
            if( firstLine ) {
                firstLine = false ;
                continue ;
            }

            try {
                eodIndex = new ScripEOD() ;
                tokenizer = new StringTokenizer( line, "," ) ;
                // NSE CSV has the following format
                // Column - 1: Symbol
                eodIndex.setSymbolId( tokenizer.nextToken() ) ;

                // Column - 2: Series (We ignore this)
                tokenizer.nextToken() ;

                // Column - 3: Open Price
                eodIndex.setOpeningPrice( Double.parseDouble( tokenizer.nextToken() ) ) ;

                // Column - 4: High Price
                eodIndex.setHighestPrice( Double.parseDouble( tokenizer.nextToken() ) ) ;

                // Column - 5: Low Price
                eodIndex.setLowestPrice( Double.parseDouble( tokenizer.nextToken() ) ) ;

                // Column - 6: Close Price
                eodIndex.setClosingPrice( Double.parseDouble( tokenizer.nextToken() ) ) ;

                // Column - 7: Last Price - we ignore this
                tokenizer.nextToken() ;

                // Column - 8: Previous close price - we ignore this
                eodIndex.setPrevClosePrice( Double.parseDouble( tokenizer.nextToken() ) ) ;

                // Column - 9: Total trade quantity
                eodIndex.setTotalTradeQty( Long.parseLong( tokenizer.nextToken() ) ) ;

                // Column - 10: Total trade value - we ignore this
                tokenizer.nextToken() ;

                // Column - 11: Date
                eodIndex.setDate( NSE_CSV_DF.parse( tokenizer.nextToken() ) ) ;

                // Ignore the rest of the line.

                eodIndices.add( eodIndex ) ;
            }
            catch ( final ParseException e ) {
                // If we have an exception - we ignore this EOD index.
                logger.error( "Parse exception. Msg=" + e.getMessage() ) ;
                logger.debug( "Parse exception", e ) ;
            }
        }
        return eodIndices ;
    }

    /**
     * Checks if the contents resemble a valid bhavcopy content. A valid bhavcopy
     * CSV content string always starts with the headers and hence we check
     * if the contents start with a piece of known header information.
     *
     * @param contents The contents of the bhavcopy
     * @return true if the contents are of a valid bhavcopy, false otherwise.
     */
    private boolean checkBhavcopyValidity( final String contents ) {
        return contents.startsWith( "SYMBOL,SERIES,OPEN,HIGH," ) ;
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
        final int threshold = cfgMgr.getInt( CFG_KEY_ARCHIVAL_THRESHOLD,
                                             DEF_ARCHIVAL_THRESHOLD ) ;

        final Calendar boundaryDate = Calendar.getInstance() ;
        boundaryDate.add( Calendar.DATE, -1*threshold ) ;
        final Date boundary = boundaryDate.getTime() ;
        logger.debug( "Archiving all records prior to " + NSE_CSV_DF.format( boundary ) ) ;

        // Ask the DAO to copy the data from live to archive table with one
        // query operation (insert with nested select)
        logger.debug( "Copying records to archive table" ) ;
        this.eodIndexDAO.archiveLiveRecords( boundary ) ;

        // Ask the DAO to delete all the data past the calculated date from the
        // live table
        logger.debug( "Deleting records from live table" ) ;
        this.eodIndexDAO.deleteLiveRecords( boundary ) ;
    }
}
