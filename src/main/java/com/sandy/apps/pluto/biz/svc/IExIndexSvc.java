/**
 * Creation Date: Aug 9, 2008
 */

package com.sandy.apps.pluto.biz.svc;

import java.text.DateFormat ;
import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.List ;

import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.dto.ExIndexEOD ;
import com.sandy.apps.pluto.shared.dto.ExIndexITD ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * This service definition encapsulates methods which operate upon the data
 * for exchange indexes, like end of day and intraday information.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface IExIndexSvc {

    /** The date format used to specify the time for the ITD index values. */
    DateFormat ITD_DF = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" ) ;

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
    void importEODIndices( final String indexName, final Date fromDate,
                           final Date toDate )
        throws STException ;

    /**
     * Imports low resolution ITD index data for the specified NSE index name.
     * This operation uses the low resolution (Screen Scraping) data feed and hence
     * is cheap in terms of network usage. Typically this method will fetch around
     * 10K of data from server for all the fundamental indexes. If we
     * use this for fetching ITD values at 30 sec intervals - it will cost us
     * around 6 MB of data per day, for all indexes. On the other hand, this operation
     * is NOT capable of backfilling the index values in case of outages.
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
    void importLowResITDIndices()
        throws STException ;

    /**
     * Imports ITD index data for the specified NSE index name. All the
     * available indexes since the last index time will be imported into the
     * persistent storage. This operation uses the high resolution (Chart)
     * data feed and hence is costly in terms of network usage. Typically this
     * method will fetch around 100K of data from server for one index. If we
     * use this for fetching ITD values at 30 sec intervals - it will cost us
     * around 60 MB of data per day, per index. If we track 5 indexes, a 300 MB
     * overhead (eikes!) On the other hand, this operation is capable of
     * backfilling the index values in case of outages. Ideally, this operation
     * should be run at 2 hour intervals to ensure that the gaps are eradicated.
     * For real time ITD values, the usage of importLowResITDIndices is recommended.
     *
     * @param indexName The name of the index
     * @param lastIndexTime The latest time for which we have the ITD data
     *        for this index.
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
    Date importHiResITDIndices( final String indexName, final Date lastIndexTime )
        throws STException ;

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
    List<String> getIndexNames( final String exchangeName )
        throws DataAccessException ;

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
    List<ExIndexEOD> getExIndexEODList( final String index )
        throws DataAccessException ;

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
    List<ExIndexEOD> getExIndexEODList( final String index, final Date fromDate,
                                        final Date toDate )
        throws DataAccessException ;

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
    List<ExIndexITD> getExIndexITDList( String name, Date startTime,
            Date endTime ) ;
}
