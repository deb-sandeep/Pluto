/**
 * Creation Date: Jul 31, 2008
 */

package com.sandy.apps.pluto.biz.svc;

import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.dto.ScripITD ;

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
 *
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
public interface IITDBulkImportSvc {

    /* String constants identifying the supported indexes. */
    String INDEX_NIFTY           = "NIFTY" ;
    String INDEX_JR_NIFTY        = "JR. NIFTY" ;
    String INDEX_CNX_IT          = "CNX IT" ;
    String INDEX_BANK_NIFTY      = "BANK NIFTY" ;
    String INDEX_NIFTY_MIDCAP_50 = "NIFTY MIDCAP 50" ;
    String INDEX_IL              = "IL" ;


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
    void importNSEIndexScrips( final String indexName,
                               final boolean ignoreError )
        throws STException ;

    /**
     * Archives all the data from STOCK_ITD_DATA table which are older than
     * the configured interval 'scrip.itd.archive.days.threshold'. The archived
     * records would be moved to the STOCK_ITD_DATA_ARCHIVE table and deleted
     * from the live table.
     *
     * @throws STException In case an exception condition is encountered during
     *         the archival process.
     */
    void archive() throws STException ;
}
