/**
 * Creation Date: Jul 27, 2008
 */

package com.sandy.apps.pluto.biz.dao;

import java.util.Date ;
import java.util.List ;

import com.sandy.apps.pluto.shared.dto.ScripITD ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * This interface exposes the operations required to operate on the ITD indices
 * in the persistent storage.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface IITDIndexDAO {

    /**
     * Inserts a list of itdIndices into the persistent storage.
     *
     * @param itdIndices A list of {@link ScripITD} instances.
     *
     * @return The number of successful inserts.
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the data access operation.
     */
    int insert( final List<ScripITD> itdIndices )
        throws DataAccessException ;

    /**
     * Inserts an instance of ScripITD into the persistent storage.
     *
     * @param itdIndices A list of {@link ScripITD} instances.
     *
     * @return The number of successful inserts.
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the data access operation.
     */
    int insert( final ScripITD itdIndex )
        throws DataAccessException ;

    /**
     * Returns a list of the latest Scrip ITD instances for all the symbols
     * for whom the intra day data is available.
     *
     * @return A list of {@link ScripITD}. If there are no intra day data for
     *         the specified date, an empty list is returned, never null.
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the data access operation.
     */
    List<ScripITD> getLatestScripITD() throws DataAccessException ;

    /**
     * Returns a list of all the intra day ITD markers for the specified symbol
     * for the specified date.
     *
     * @param symbol The NSE symbol for which the ITD data has to be fetched
     * @param date The date for which the data has to be fetched.
     *
     * @return A list of {@link ScripITD}. If there are no intra day data for
     *         the specified date, an empty list is returned, never null.
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the data access operation.
     */
    List<ScripITD> getScripITD( final String symbol, final Date date )
        throws DataAccessException ;

    /**
     * Returns a list of all the intra day ITD markers for the specified symbol
     * and the date range specified.
     *
     * @param symbol The NSE symbol for which the ITD data has to be fetched
     * @param start  The start date of the date range
     * @param end    The end date of the date range
     *
     * @return A list of {@link ScripITD}. If there are no intra day data for
     *         the specified date, an empty list is returned, never null.
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the data access operation.
     */
    List<ScripITD> getScripITD( final String symbol, final Date start,
                                final Date end )
        throws DataAccessException ;

    /**
     * Moves all records in the SCRIP_ITD_DATA table which have a date attribute
     * which is in the past, relative to the boundary date passed as parameter.
     * Please note that this operation does not delete the data from the live
     * table. If you want to delete the live data which has been archived, you
     * need to call on the deleteLiveRecords operation within the same transaction
     * boundary.
     *
     * @param boundary The date which implies that any record with date in the
     *        past as compared to the boundary will be archived.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         archival process.
     */
    void archiveLiveRecords( final Date boundary )
        throws DataAccessException ;

    /**
     * Deletes all records in the SCRIP_ITD_DATA table which have a date attribute
     * which is in the past, relative to the boundary date passed as parameter.
     * Please note that this operation does not copy the data from the live
     * table to the archive table. If you want to copy the live data which is
     * about to be deleted, you need to call on the archiveLiveRecords
     * before this call, within the same transaction boundary.
     *
     * @param boundary The date which implies that any record with date in the
     *        past as compared to the boundary will be deleted.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         archival process.
     */
    void deleteLiveRecords( final Date boundary )
        throws DataAccessException ;
}
