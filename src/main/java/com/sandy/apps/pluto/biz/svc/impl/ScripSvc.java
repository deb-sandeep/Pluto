/**
 * Creation Date: Oct 6, 2008
 */

package com.sandy.apps.pluto.biz.svc.impl;
import java.util.Date ;
import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.dao.IEODIndexDAO ;
import com.sandy.apps.pluto.biz.dao.IITDIndexDAO ;
import com.sandy.apps.pluto.biz.dao.ISymbolDAO ;
import com.sandy.apps.pluto.biz.svc.IScripSvc ;
import com.sandy.apps.pluto.biz.svc.impl.scraper.ICICIDirectCoSnapshotScreenParser ;
import com.sandy.apps.pluto.shared.ErrorCode ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.ScripEOD ;
import com.sandy.apps.pluto.shared.dto.ScripITD ;
import com.sandy.apps.pluto.shared.dto.Symbol ;
import com.sandy.apps.pluto.shared.dto.SymbolPctChange ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;
import com.sandy.apps.pluto.shared.util.util.StringUtil ;

/**
 * An implementation of {@link IScripSvc} interface.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ScripSvc implements IScripSvc {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ScripSvc.class ) ;

    /**
     * INJECTABLE: This variable should be injected with a reference to the
     * implementation of IEODIndexDAO interface.
     */
    private IEODIndexDAO eodIndexDAO = null ;

    /**
     * INJECTABLE: This variable should be injected with a reference to the
     * implementation of IITDIndexDAO interface.
     */
    private IITDIndexDAO itdIndexDAO = null ;

    /**
     * INJECTABLE: This variable should be injected with a reference to the
     * implementation of ISymbolDAO interface.
     */
    private ISymbolDAO symbolDAO = null ;

    /** Public no argument constructor. */
    public ScripSvc() {
        super() ;
    }

    /**
     * Returns a list of {@link ScripEOD} instances representing the EOD
     * data for the specified scrip for the last one year starting from today.
     *
     * @param scrip The scrip name
     * @return A list of {@link ScripEOD} instances
     *
     * @throws STException If the scrip is not registered.
     */
    @Override
    public List<ScripEOD> getEODData( final String scrip ) throws STException {
        final List<ScripEOD> data = this.eodIndexDAO.getScripEOD( scrip ) ;
        if( data == null ) {
            throw new STException( "Specified scrip '" + scrip + "' is not " +
                    "registered in Pluto", ErrorCode.SCRIP_NOT_REGISTERED ) ;
        }
        return data ;
    }

    /**
     * Retrieves a list of EOD values for the specified scrip and date range.
     *
     * @param scrip The NSE scrip symbol
     * @param start The start of the time range
     * @param end   The end of the time range.
     *
     * @return A list of ScripEOD instances or null if no EOD values could be
     *         found for the specified range.
     *
     * @throws STException In case the data could not be retrieved because
     *         of unanticipated reasons or if the Scrip is not registered in
     *         the system.
     */
    public List<ScripEOD> getEODData( final String scrip, final Date start,
                                      final Date end )
        throws STException {

        final List<ScripEOD> data = this.eodIndexDAO.getScripEOD(
                                    scrip, start, end ) ;
        return data ;
    }

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
    public List<ScripITD> getITDData( final String scrip, final Date start,
                                      final Date end )
        throws STException {

        final List<ScripITD> data = this.itdIndexDAO.getScripITD(
                                                           scrip, start, end ) ;
        return data ;
    }

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
    public List<ScripEOD> getArchivedEODData( final String scrip )
        throws STException {

        final List<ScripEOD> data = this.eodIndexDAO.getArchivedScripEOD( scrip ) ;
        if( data == null ) {
            throw new STException( "Specified scrip '" + scrip + "' is not " +
                    "registered in Pluto", ErrorCode.SCRIP_NOT_REGISTERED ) ;
        }
        return data ;
    }

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
     *         optionally embedded based on the flag passed. If no results
     *         are found, a null list is returned.
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the data access operation.
     */
    @Override
    public List<ScripEOD> getScripEOD( final Date date, final boolean includeSymbolDetails )
        throws DataAccessException {

        final List<ScripEOD> data = this.eodIndexDAO.getScripEOD( date, includeSymbolDetails ) ;
        return data ;
    }

    /**
     * Returns the latest date for which EOD data has been imported for Scrips.
     *
     * @return A {@link Date} instance representing the latest bhavcopy import
     *         date or null, if there are no EOD records in the database.
     *
     * @throws DataAccessException If an exception was encountered during the
     *         data access operation
     */
    public Date getLastScripEODDate() throws DataAccessException {
        return this.eodIndexDAO.getLastScripEODDate() ;
    }

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
    public Date getLastScripEODDate( final Date refDate ) throws DataAccessException {
        return this.eodIndexDAO.getLastScripEODDate( refDate ) ;
    }

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
    public Double[] getLastNPctEODChange( final String symbol, final Date refDate,
                                          final int numDays )
        throws DataAccessException {
        return this.eodIndexDAO.getLastNPctEODChange( symbol, refDate, numDays ) ;
    }

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
    public List<SymbolPctChange> getLastNPctEODChange( final Date refDate,
                                                       final int numDays )
        throws DataAccessException {

        return this.eodIndexDAO.getLastNPctEODChange( refDate, numDays ) ;
    }

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
    public void importScripDetails( final String symbolId, final boolean snapshot,
                                    final boolean history )
        throws STException {

        logger.debug( "Importing scrip details for " + symbolId ) ;

        // Lookup the symbol from the database
        final Symbol symbol = this.symbolDAO.getSymbol( symbolId ) ;
        if( symbol == null ) {
            throw new STException( "No symbol registered by the code " + symbolId,
                                   ErrorCode.INVALID_ARG ) ;
        }

        // Check if an ICICI code is asscoaited with the scrip - if not,
        // throw an exception.
        if( StringUtil.isEmptyOrNull( symbol.getIciciCode() ) ) {
            throw new STException( "No ICICIDirect code associated with " + symbolId,
                                   ErrorCode.INVALID_ARG ) ;
        }

        // If we have been asked to import the scrip snapshot - we proceed
        if( snapshot ) {
            importScripSnapshotDetails( symbol ) ;
        }
    }

    /**
     * Imports the scrip snap shot details.
     *
     * @param symbol The symbol for which snapshot details are to be imported
     *
     * @throws STException If an exception is encountered in the process.
     */
    private void importScripSnapshotDetails( final Symbol symbol )
            throws STException {

        logger.debug( "Importing scrip snapshot for " + symbol.getSymbol() ) ;

        // If we are good with all the pre-requisite checks, parse the
        // contents and enrich the Symbol before updating it back into the
        // database
        final ICICIDirectCoSnapshotScreenParser parser =
            new ICICIDirectCoSnapshotScreenParser( ServiceMgr.getNetworkSvc() ) ;

        final Symbol enrichedSymbol = parser.parse( symbol.getIciciCode() ) ;
        symbol.setWebsite( enrichedSymbol.getWebsite() ) ;
        symbol.setSegment( enrichedSymbol.getSegment() ) ;
        symbol.setSegmentCat( enrichedSymbol.getSegmentCat() ) ;

        // Now update the enriched symbol into the database.
        this.symbolDAO.updateSymbol( symbol ) ;
    }

    /**
     * @return the eodIndexDAO
     */
    public IEODIndexDAO getEodIndexDAO() {
        return this.eodIndexDAO ;
    }

    /**
     * @param eodIndexDAO the eodIndexDAO to set
     */
    public void setEodIndexDAO( final IEODIndexDAO eodIndexDAO ) {
        this.eodIndexDAO = eodIndexDAO ;
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
     * @param symbolDAO the symbolDAO to set
     */
    public void setSymbolDAO( final ISymbolDAO symbolDAO ) {
        this.symbolDAO = symbolDAO;
    }

    /**
     * @return the symbolDAO
     */
    public ISymbolDAO getSymbolDAO() {
        return this.symbolDAO;
    }
}
