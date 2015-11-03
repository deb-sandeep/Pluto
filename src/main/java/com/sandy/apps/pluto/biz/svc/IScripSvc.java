/**
 * Creation Date: Oct 6, 2008
 */

package com.sandy.apps.pluto.biz.svc;

import java.util.Date ;
import java.util.List ;

import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.dto.ScripEOD ;
import com.sandy.apps.pluto.shared.dto.ScripITD ;
import com.sandy.apps.pluto.shared.dto.SymbolPctChange ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * This service exposes methods to retrieve Scrip specific data, including
 * EOD and ITD data.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface IScripSvc {

    /**
     * Imports the details of the specified scrip based on the boolean flags
     * passed as input parameters. This function depends upon the ICICI Direct
     * code for the given NSE symbol for fetching the scrip details. If an
     * associated ICICI Direct code is not found, this function raises an
     * exception with the error code as invalid argument.
     *
     * @param symbolId The NSE symbol id for the scrip.
     * @param snapshot A boolean flag indicating if the snapshot information
     *        is to be downloaded.
     * @param history A boolean flag indicating if the history of the company
     *        is to be downloaded.
     *
     * @throws STException An exception is raised under the following scenarios.
     *         <ul>
     *          <li>No associated ICICI Direct code is found associated with
     *              the symbol</li>
     *          <li>A network error occured while fetching the details</li>
     *          <li>A database exception was encountered during the process</li>
     *         </ul>
     */
    void importScripDetails( final String symbolId, final boolean snapshot,
                             final boolean history )
        throws STException ;

    /**
     * Retrieves a list of EOD data points for the specified scrip. This
     * method retrieves all the EOD data points from today till the last one
     * year.
     *
     * @param scrip The NSE scrip symbol
     * @return A list of {@link ScripEOD} instances.
     *
     * @throws STException In case the data could not be retrieved because
     *         of unanticipated reasons or if the Scrip is not registered in
     *         the system.
     */
    List<ScripEOD> getEODData( final String scrip ) throws STException ;

    /**
     * Retrieves a list of archived EOD data points for the specified scrip. This
     * method retrieves all the archived EOD data we have in the Pluto system.
     *
     * @param scrip The NSE scrip symbol
     * @return A list of {@link ScripEOD} instances.
     *
     * @throws STException In case the data could not be retrieved because
     *         of unanticipated reasons or if the Scrip is not registered in
     *         the system.
     */
    List<ScripEOD> getArchivedEODData( final String scrip ) throws STException ;

    /**
     * Retrieves a list of EOD values for the specified scrip and date range.
     *
     * @param scrip The NSE scrip symbol
     * @return A list of ScripEOD instances or null if no EOD values could be
     *         found for the specified range.
     *
     * @throws STException In case the data could not be retrieved because
     *         of unanticipated reasons or if the Scrip is not registered in
     *         the system.
     */
    List<ScripEOD> getEODData( final String scrip, final Date start, final Date end )
        throws STException ;

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
     * Retrieves a list of ITD values for the specified scrip and date range.
     *
     * @param scrip The NSE scrip symbol
     * @return A list of ScripITD instances or null if no ITD values could be
     *         found for the specified range.
     *
     * @throws STException In case the data could not be retrieved because
     *         of unanticipated reasons or if the Scrip is not registered in
     *         the system.
     */
    List<ScripITD> getITDData( final String scrip, final Date start, final Date end )
        throws STException ;

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
     * @param refDate The reference date prior to which the data is required
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
}
