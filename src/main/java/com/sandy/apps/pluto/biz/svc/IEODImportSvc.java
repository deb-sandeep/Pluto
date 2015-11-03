/**
 * Creation Date: Jul 27, 2008
 */

package com.sandy.apps.pluto.biz.svc;

import java.text.SimpleDateFormat ;
import java.util.Date ;

import com.sandy.apps.pluto.shared.STException ;

/**
 * This service exposes operations to import EOD scrip data into the persistent
 * storage.
 * <p>
 * This interface also provides operations to archive Scrip EOD data
 * which are 'n' days old. The value of the number of days, which qualifies the
 * EOD data as eligible for archive can be specified by setting the application
 * configuration value for property 'scrip.eod.archive.days.threshold'. By
 * default a value of 360 days is considered, which implies that at any point
 * in time only one year's data will be considered live.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface IEODImportSvc {

    /** The date format used for NSE EOD dates in the CSV files. */
    SimpleDateFormat NSE_CSV_DF = new SimpleDateFormat( "dd-MMM-yyyy" ) ;

    /** The date format used for posting date range for historic EOD data. */
    SimpleDateFormat NSE_POST_DF = new SimpleDateFormat( "dd-MM-yyyy" ) ;

    /**
     * The configuration key against which the archival threshold is mentioned
     * in number of days relative to today.
     */
    String CFG_KEY_ARCHIVAL_THRESHOLD = "scrip.eod.archive.days.threshold" ;

    /** The default value of the archival threshold. */
    int DEF_ARCHIVAL_THRESHOLD = 360 ;

    /**
     * This operation bhavcopy data for the specified date. If the force
     * download option is set as false, this operation first looks for an
     * already downloaded file for the specified date, if not found it tries
     * to fetch it from the Internet.
     *
     * @param date The date for which the bhavcopy is to be imported
     *
     * @throws STException If an exception is encountered during the import
     *         process.
     */
    void importBhavcopyEODData( final Date date )
        throws STException ;

    /**
     * Archives all the data from STOCK_EOD_DATA table which are older than
     * the configured interval 'scrip.eod.archive.days.threshold'. The archived
     * records would be moved to the STOCK_EOD_DATA_ARCHIVE table and deleted
     * from the live table.
     *
     * @throws STException In case an exception condition is encountered during
     *         the archival process.
     */
    void archive() throws STException ;
}
