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
 * However, these lists don't capture all the scrips - for scrips which are
 * not a part of the above indexes, the data has to be fetched individually
 * from the following URL:
 * http://www.nseindia.com/marketinfo/equities/cmquote_tab.jsp
 * via a HTTP post operation. This interface exposes the services of an operation
 * which helps in downloading ITD scrip values of stocks which are not a part
 * of the exposed indexes. If you are interested in getting the values of
 * exposed indexes, please use the IITDBulkImportSvc implementation.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface IITDImportSvc {

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
    void importNSESymbol( final String symbol,
                          final boolean ignoreError )
        throws STException ;

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
    void importHighResNSESymbol( final String symbol, final boolean ignoreError )
        throws STException ;
}
