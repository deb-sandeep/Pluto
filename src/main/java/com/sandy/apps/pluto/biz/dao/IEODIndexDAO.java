/**
 * Creation Date: Jul 27, 2008
 */

package com.sandy.apps.pluto.biz.dao;

import java.util.Date ;
import java.util.List ;

import com.sandy.apps.pluto.shared.dto.ScripEOD ;
import com.sandy.apps.pluto.shared.dto.SymbolPctChange ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * This interface exposes the operations required to operate on the EOD indices
 * in the persistent storage.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface IEODIndexDAO {

    /**
     * Inserts a list of eodIndices into the persistent storage. If an index in
     * the list already exists in the database, it is deleted and replaced with
     * a new value. It is assumed that the elements in the index belong to one
     * symbol. If the input list contains EOD from multiple symbols, the result
     * of the operation is undefined.
     *
     * @param eodIndices A list of {@link ScripEOD} instances.
     *
     * @return The number of successful inserts.
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the data access operation.
     */
    int insert( final List<ScripEOD> eodIndices )
        throws DataAccessException ;

    /**
     * Updates a list of eodIndices into the persistent storage. The values
     * of the EOD index are updated based on their date and scrip name.
     *
     * @param eodIndices A list of {@link ScripEOD} instances.
     *
     * @return The number of successful inserts.
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the data access operation.
     */
    int update( final List<ScripEOD> eodIndices )
        throws DataAccessException ;

    /**
     * Updates a list of eodIndices into the persistent storage. The values
     * of the EOD index are updated based on their date and scrip name.
     *
     * @param eodIndices A list of {@link ScripEOD} instances.
     *
     * @return The number of successful inserts.
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the data access operation.
     */
    int updateArchive( final List<ScripEOD> eodIndices )
        throws DataAccessException ;

    /**
     * Deletes a list of eodIndices from the persistent storage. It is assumed
     * that the elements in the index belong to one symbol. If the input list
     * contains EOD from multiple symbols, the result of the operation is
     * undefined.
     *
     * @param eodIndices A list of {@link ScripEOD} instances.
     *
     * @return The number of successful inserts.
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the data access operation.
     */
    int delete( final List<ScripEOD> eodIndices ) ;

    /**
     * Returns the EOD data for the specified scrip for the date specified. If
     * a scrip EOD is not found, a null value is returned.
     *
     * @param symbol The symbol for which we need the EOD data fetched
     * @param date The date for which we need the data
     *
     * @return The {@link ScripEOD} instance or null if the scrip is not registered.
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the data access operation.
     */
    ScripEOD getScripEOD( final String symbol, final Date date )
        throws DataAccessException ;

    /**
     * Returns a list of EOD data for the specified scrip for the entire year's
     * range. If the scrip is not registered with the system, this method
     * returns a null.
     *
     * @param symbol The symbol for which we need the EOD data fetched
     *
     * @return A list of {@link ScripEOD} instances or null if the scrip is
     *         not registered.
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the data access operation.
     */
    List<ScripEOD> getScripEOD( final String symbol )
        throws DataAccessException ;

    /**
     * Returns a list of EOD data for the specified scrip for and the time
     * range. If the scrip is not registered with the system, this method
     * returns a null.
     *
     * @param symbol The symbol for which we need the EOD data fetched
     *
     * @return A list of {@link ScripEOD} instances or null if the scrip is
     *         not registered or if there are no EOD records for the range
     *         specified.
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the data access operation.
     */
    List<ScripEOD> getScripEOD( final String symbol, final Date start,
                                final Date end )
        throws DataAccessException ;

    /**
     * Returns a list of scrip EOD data for the specified date. This function
     * also accepts a flag which enables embedding of the symbol details in
     * the returned ScripEOD instances.
     *
     * @param date The date for which the list of ScripEOD instances need to
     *        be returned.
     * @param includeSymbolDetails A boolean flag indicating if the symbol
     *        details need to be associated with the {@link ScripEOD} instances.
     *
     * @return A list of {@link ScripEOD} instances, with the symbol details
     *         optionally embedded based on the flag passed.
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the data access operation.
     */
    List<ScripEOD> getScripEOD( final Date date, final boolean includeSymbolDetails )
        throws DataAccessException ;

    /**
     * Returns the latest date for which EOD data has been imported for Scrips.
     *
     * @return A {@link Date} instance representing the latest bhavcopy import
     *         date or null, if there are no EOD records in the database.
     *
     * @throws DataAccessException If an exception was encountered during the
     *         data access operation
     */
    Date getLastScripEODDate() throws DataAccessException ;

    /**
     * Returns the latest date for which EOD data has been imported for Scrips,
     * before the specified date.
     *
     * @param refDate A reference date from whose past the latest bhavcopy
     *        import data has to be fetched.
     *
     * @return A {@link Date} instance representing the latest bhavcopy import
     *         date or null, if there are no EOD records in the database.
     *
     * @throws DataAccessException If an exception was encountered during the
     *         data access operation
     */
    Date getLastScripEODDate( final Date refDate ) throws DataAccessException ;

    /**
     * Returns the last 'n' percentage change for EOD values for the given
     * symbol from the reference date specfied in descending order of their
     * dates.
     *
     * @param symbol The NSE symbol for which the percentage changes are needed
     * @param refDate The reference date prior to which the data is required
     * @param numDays The number of days for which percentage changes are required.
     *
     * @return An array of {@link Double} values representing changes for
     *         days in descending order.
     *
     * @throws DataAccessException If an unexpected database exception is
     *         encountered in the process of fetching the data.
     */
    Double[] getLastNPctEODChange( final String symbol, final Date refDate,
                                   final int numDays )
        throws DataAccessException ;

    /**
     * Returns the last 'n' percentage change for EOD values for all
     * symbols from the reference date specfied in descending order of their
     * dates.
     *
     * @param refDate The refe    e date prior to which the data is required
     * @param numDays The number of days for which percentage changes are required.
     *
     * @return An array of {@link SymbolPctChange} values representing changes
     *         for symbols in ascending and days in descending order.
     *
     * @throws DataAccessException If an unexpected database exception is
     *         encountered in the process of fetching the data.
     */
    List<SymbolPctChange> getLastNPctEODChange( final Date refDate,
                                                final int numDays )
        throws DataAccessException ;

    /**
     * Returns a list of all archived EOD data for the specified scrip. If the
     * scrip is not registered with the system, this method returns a null.
     *
     * @param symbol The symbol for which we need the EOD data fetched
     *
     * @return A list of {@link ScripEOD} instances or null if the scrip is
     *         not registered.
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the data access operation.
     */
    List<ScripEOD> getArchivedScripEOD( final String symbol )
        throws DataAccessException ;

    /**
     * Moves all records in the SCRIP_EOD_DATA table which have a date attribute
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
     * Deletes all records in the SCRIP_EOD_DATA table which have a date attribute
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
