/**
 * Creation Date: Aug 9, 2008
 */

package com.sandy.apps.pluto.biz.dao;

import java.util.Date ;
import java.util.List ;

import com.sandy.apps.pluto.shared.dto.ExIndexEOD ;
import com.sandy.apps.pluto.shared.dto.ExIndexITD ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * This DAO specifies the operations on the exchange index data from a perspective
 * of persistent storage and retrieval.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface IExIndexDAO {

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
     * Adds the given {@link ExIndexEOD} instance to the persistent storage.
     *
     * @param data The EOD data that needs to be saved in the database.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         process of data access
     */
    void addEODData( final ExIndexEOD data )
        throws DataAccessException ;

    /**
     * Updates a list of eodIndices into the persistent storage. The values
     * of the EOD index are updated based on their date and scrip name.
     *
     * @param eodIndices A list of {@link ExIndexEOD} instances.
     *
     * @return The number of successful inserts.
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the data access operation.
     */
    int updateEOD( final List<ExIndexEOD> eodIndices )
        throws DataAccessException ;

    /**
     * Adds the given {@link ExIndexITD} instance to the persistent storage.
     *
     * @param data The ITD data that needs to be saved in the database.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         process of data access
     */
    boolean addITDData( final ExIndexITD data )
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
    List<ExIndexITD> getExIndexITDList( final String index, final Date fromDate,
                                        final Date toDate ) ;

    /**
     * Gets the latest (last inserted) EOD value for the index name specified.
     *
     * @param index The name of the index.
     *
     * @return An instance of {@link ExIndexEOD} or null if no value is found.
     */
    ExIndexEOD getLatestEOD( final String index )
        throws DataAccessException ;

}
