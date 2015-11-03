/**
 * Creation Date: Jul 27, 2008
 */

package com.sandy.apps.pluto.biz.dao.impl;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.dao.IEODIndexDAO ;
import com.sandy.apps.pluto.biz.dao.ISymbolDAO ;
import com.sandy.apps.pluto.biz.svc.impl.EODImportSvc ;
import com.sandy.apps.pluto.shared.STConstant ;
import com.sandy.apps.pluto.shared.STUtils ;
import com.sandy.apps.pluto.shared.dto.ScripEOD ;
import com.sandy.apps.pluto.shared.dto.Symbol ;
import com.sandy.apps.pluto.shared.dto.SymbolPctChange ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * The implementation of {@link IEODIndexDAO}.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class EODIndexDAO extends AbstractBaseDAO implements IEODIndexDAO {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( EODIndexDAO.class ) ;

    /**
     * INJECTABLE: This variable should be injected with the ISymbolDAO instance
     * this will be used to insert symbols at runtime.
     */
    private ISymbolDAO symbolDAO = null ;

    /** Public no argument constructor. */
    public EODIndexDAO() {
        super() ;
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
    public int delete( final List<ScripEOD> eodIndices ) {

        logger.debug( "Deleting ScripEOD instances into DB" ) ;
        // If we have nothing to delete, just return
        if( eodIndices == null || eodIndices.isEmpty() ) {
            return 0 ;
        }

        // The symbol we are dealing with.
        final String QUERY_ID = "ScripEOD.delete" ;

        // Delete all the elements in batch mode.
        super.daMgr.deleteMultipleRecords( QUERY_ID, eodIndices, false ) ;

        // With the new version of PRISM, we will get the value to return
        // Till such time this is a place holder.
        return 0 ;
    }

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
    public int insert( final List<ScripEOD> eodIndices ) {

        // If we have nothing to delete, just return
        if( eodIndices == null || eodIndices.isEmpty() ) {
            return 0 ;
        }

        // The symbol we are dealing with.
        final String QUERY_ID = "ScripEOD.insert" ;

        for( final ScripEOD index : eodIndices ) {
            boolean retry = true ;
            while( retry ) {
                try {
                    if( logger.isDebugEnabled() ) {
                        logger.debug( "Importing EOD for " + index.getSymbolId() +
                        " date " + EODImportSvc.NSE_POST_DF.format( index.getDate() ) ) ;
                    }
                    super.daMgr.createRecord( QUERY_ID, index ) ;
                    retry = false ;
                }
                catch ( final DataAccessException e ) {
                    // If this is a PK violation, we ignore the exception
                    if( PostGresUtil.isPKViolation( e ) ) {
                        // Ignore primary key violations.
                        retry = false ;
                    }
                    else if ( PostGresUtil.isFKViolation( e ) ) {
                        // Foreign key exceptions imply that the symbol is not yet
                        // registered - we employ fail safe measure by inserting
                        // the symbol at runtime and retrying the insert operation
                        final Symbol symbol = new Symbol() ;
                        symbol.setSymbol( index.getSymbolId() ) ;
                        symbol.setSeries( "EQ" ) ;
                        symbol.setMarketType( "N" ) ;
                        symbol.setDescription( "Dynamically added symbol" ) ;
                        this.symbolDAO.addSymbol( symbol ) ;
                    }
                    else {
                        logger.error( "Unable to insert EOD data. Msg:" + e.getMessage() ) ;
                        logger.debug( "Unable to insert EOD data.", e ) ;
                        retry = false ;
                    }
                }
            }
        }

        // With the new version of PRISM, we will get the value to return
        // Till such time this is a place holder.
        return 0 ;
    }

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
    public int update( final List<ScripEOD> eodIndices )
        throws DataAccessException {

        // If we have nothing to delete, just return
        if( eodIndices == null || eodIndices.isEmpty() ) {
            return 0 ;
        }

        // The symbol we are dealing with.
        final String QUERY_ID = "ScripEOD.update" ;

        for( final ScripEOD index : eodIndices ) {
            if( logger.isDebugEnabled() ) {
                logger.debug( "Updating EOD for " + index.getSymbolId() +
                " date " + EODImportSvc.NSE_POST_DF.format( index.getDate() ) ) ;
            }
            super.daMgr.updateRecord( QUERY_ID, index ) ;
        }

        // With the new version of PRISM, we will get the value to return
        // Till such time this is a place holder.
        return 0 ;
    }

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
    public int updateArchive( final List<ScripEOD> eodIndices )
        throws DataAccessException {

        // If we have nothing to delete, just return
        if( eodIndices == null || eodIndices.isEmpty() ) {
            return 0 ;
        }

        // The symbol we are dealing with.
        final String QUERY_ID = "ScripEOD.updateArchive" ;

        for( final ScripEOD index : eodIndices ) {
            if( logger.isDebugEnabled() ) {
                logger.debug( "Updating Archive EOD for " + index.getSymbolId() +
                " date " + EODImportSvc.NSE_POST_DF.format( index.getDate() ) ) ;
            }
            super.daMgr.updateRecord( QUERY_ID, index ) ;
        }

        // With the new version of PRISM, we will get the value to return
        // Till such time this is a place holder.
        return 0 ;
    }

    /**
     * Returns the EOD data for the specified scrip for the date specified. If
     * a scrip EOD is not found, a null value is returned.
     *
     * @param symbol The symbol for which we need the EOD data fetched
     * @param date The date for which we need the data
     *
     * @return The number of successful inserts.
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the data access operation.
     */
    public ScripEOD getScripEOD( final String symbol, final Date date )
        throws DataAccessException {

        ScripEOD eodData = null ;
        final String QUERY_ID = "ScripEOD.getScripEODForDate" ;
        final Map<String, Object> paramMap = new HashMap<String, Object>() ;

        paramMap.put( "scrip", symbol ) ;
        paramMap.put( "date", STUtils.getStartOfDay( date ) ) ;

        eodData = ( ScripEOD )super.daMgr.retrieveRecord( QUERY_ID, paramMap ) ;
        return eodData ;
    }

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
    @SuppressWarnings("unchecked")
    public List<ScripEOD> getScripEOD( final String symbol, final Date start,
                                       final Date end )
        throws DataAccessException {

        List<ScripEOD> data = null ;

        final String QUERY_ID = "ScripEOD.getScripEODForDateRange" ;
        final Map<String, Object> paramMap = new HashMap<String, Object>() ;

        paramMap.put( "scrip", symbol ) ;
        paramMap.put( "startDate", STUtils.getStartOfDay( start ) ) ;
        paramMap.put( "endDate",   STUtils.getStartOfDay( end ) ) ;

        if( logger.isDebugEnabled() ) {
            logger.debug( "Loading Scrip EOD data for date range " +
                          "startDate = " + STConstant.DATE_FMT.format( start ) +
                          ", endDate = " + STConstant.DATE_FMT.format( end ) ) ;
        }

        data = super.daMgr.searchRecords( QUERY_ID, paramMap ) ;

        return data ;
    }

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
    @SuppressWarnings("unchecked")
    public List<ScripEOD> getScripEOD( final String symbol )
        throws DataAccessException {

        final String QUERY_ID = "ScripEOD.getActiveScripEOD" ;
        List<ScripEOD> data = null ;

        data = super.daMgr.searchRecords( QUERY_ID, symbol ) ;

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
     *         optionally embedded based on the flag passed.
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the data access operation.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<ScripEOD> getScripEOD( final Date date, final boolean includeSymbolDetails )
        throws DataAccessException {

        final String QUERY_ID = "ScripEOD.getAllScripEODForDate" ;
        final String QUERY_ID_WITH_DETAIL = "ScripEOD.getAllScripEODForDateWithDetail" ;
        List<ScripEOD> data = null ;

        final Date requestDate = STUtils.getStartOfDay( date ) ;
        if( includeSymbolDetails ) {
            data = super.daMgr.searchRecords( QUERY_ID_WITH_DETAIL,
                                                          requestDate ) ;
        }
        else {
            data = super.daMgr.searchRecords( QUERY_ID, requestDate ) ;
        }

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
        return getLastScripEODDate( new Date() ) ;
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
    public Date getLastScripEODDate( final Date refDate )
        throws DataAccessException {

        final String QUERY_ID = "ScripEOD.getLastScripEODDateBefore" ;
        Date date = null ;
        date = ( Date )super.daMgr.retrieveRecord( QUERY_ID, refDate ) ;
        return date ;
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
    @SuppressWarnings("unchecked")
    public Double[] getLastNPctEODChange( final String symbol,
                                          final Date refDate, final int numDays )
        throws DataAccessException {

        final String QUERY_ID = "ScripEOD.getLastNPctEODChange" ;
        final Map<String, Object> paramMap = new HashMap<String, Object>() ;

        paramMap.put( "date",   refDate ) ;
        paramMap.put( "symbol", symbol ) ;
        paramMap.put( "n",      numDays ) ;

        final List<Double> pctChanges = super.daMgr.searchRecords(
                                                          QUERY_ID, paramMap ) ;
        return pctChanges.toArray( new Double[numDays] ) ;
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
    @SuppressWarnings("unchecked")
    public List<SymbolPctChange> getLastNPctEODChange( final Date refDate,
                                                       final int numDays )
        throws DataAccessException {

        final String QUERY_ID = "ScripEOD.getLastNPctEODChangeForAllSymbols" ;
        final Map<String, Object> paramMap = new HashMap<String, Object>() ;

        paramMap.put( "date",   refDate ) ;
        paramMap.put( "n",      numDays ) ;

        List<SymbolPctChange> pctChanges = null ;
        pctChanges = super.daMgr.searchRecords( QUERY_ID, paramMap ) ;
        return pctChanges ;
    }

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
    @SuppressWarnings("unchecked")
    public List<ScripEOD> getArchivedScripEOD( final String symbol )
        throws DataAccessException {

        final String QUERY_ID = "ScripEOD.getArchiveScripEOD" ;
        List<ScripEOD> data = null ;

        data = super.daMgr.searchRecords( QUERY_ID, symbol ) ;

        return data ;
    }

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
    public void archiveLiveRecords( final Date boundary )
        throws DataAccessException {

        final String QUERY_ID = "ScripEOD.archiveLiveRecords" ;
        super.daMgr.createRecord( QUERY_ID, boundary ) ;
    }

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
    public void deleteLiveRecords( final Date boundary )
        throws DataAccessException {

        final String QUERY_ID = "ScripEOD.deleteLiveRecords" ;
        super.daMgr.deleteRecord( QUERY_ID, boundary ) ;
    }
}
