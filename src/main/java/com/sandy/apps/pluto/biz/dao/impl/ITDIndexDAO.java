/**
 * Creation Date: Jul 27, 2008
 */

package com.sandy.apps.pluto.biz.dao.impl;
import java.util.Collections ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.dao.IITDIndexDAO ;
import com.sandy.apps.pluto.biz.dao.ISymbolDAO ;
import com.sandy.apps.pluto.shared.STUtils ;
import com.sandy.apps.pluto.shared.dto.ScripITD ;
import com.sandy.apps.pluto.shared.dto.Symbol ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * The implementation of {@link IITDIndexDAO}.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ITDIndexDAO extends AbstractBaseDAO implements IITDIndexDAO {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ITDIndexDAO.class ) ;

    /**
     * INJECTABLE: This variable should be injected with a reference to the
     * implementation of ISymbolDAO interface.
     */
    private ISymbolDAO symbolDAO = null ;

    /** Public no argument constructor. */
    public ITDIndexDAO() {
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
     * Inserts a list of itdIndices into the persistent storage. This method
     * filters the invalid ITD entries in the called array - for example if
     * an entry is found with an invalid date, it is removed from the input
     * collection.
     *
     * @param itdIndices A list of {@link ScripITD} instances.
     *
     * @return The number of successful inserts.
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the data access operation.
     */
    public int insert( final List<ScripITD> itdIndices )
        throws DataAccessException {

        logger.debug( "Inserting ScripITD instances into DB" ) ;
        // If we have nothing to delete, just return
        if( itdIndices == null || itdIndices.isEmpty() ) {
            return 0 ;
        }

        // The symbol we are dealing with.
        final String QUERY_ID = "ScripITD.insert" ;

        // Insert elements one by one and disregard PK violations. Now is
        // the current time. Any ITD value which is not before the current
        // time will be removed.
        final Date now = new Date() ;
        for( final Iterator<ScripITD> iter = itdIndices.iterator(); iter.hasNext(); ) {

            final ScripITD scrip = iter.next() ;
            boolean retry = true ;
            while( retry ) {
                try {
                    // Filter out the erroneous entries. There are times when
                    // it is observed that NSE gives us entries pointing to the
                    // future :) and way in the past like 2000 BC.
                    if( scrip.getTime().before( now ) ||
                        scrip.getTime().equals( now ) ) {
                        super.daMgr.createRecord( QUERY_ID, scrip ) ;
                    }
                    else {
                        iter.remove() ;
                        logger.debug( "Found a ITD in the future, ignoring..." ) ;
                        logger.debug( "Scrip ITD = " + scrip ) ;
                    }
                    retry = false ;
                }
                catch ( final DataAccessException e ) {
                    if( PostGresUtil.isPKViolation( e ) ) {
                        // Ignore a PK violation
                        logger.debug( "PK violation while inserting Scrip ITD " + scrip ) ;
                        retry = false ;
                    }
                    else if ( PostGresUtil.isFKViolation( e ) ) {
                        // Foreign key exceptions imply that the symbol is not yet
                        // registered - we employ fail safe measure by inserting
                        // the symbol at runtime and retrying the insert operation
                        final Symbol symbol = new Symbol() ;
                        symbol.setSymbol( scrip.getSymbolId() ) ;
                        symbol.setSeries( "EQ" ) ;
                        symbol.setMarketType( "N" ) ;
                        symbol.setDescription( "Dynamically added symbol" ) ;
                        this.symbolDAO.addSymbol( symbol ) ;
                    }
                    else {
                        logger.error( "Unable to insert ITD data. Msg:" + e.getMessage() ) ;
                        logger.debug( "Unable to insert ITD data.", e ) ;
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
     * Inserts an instance of ScripITD into the persistent storage.
     *
     * @param itdIndices A list of {@link ScripITD} instances.
     *
     * @return The number of successful inserts.
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the data access operation.
     */
    public int insert( final ScripITD scrip )
        throws DataAccessException {

        logger.debug( "Inserting ScripITD instances into DB" ) ;
        // If we have nothing to insert, just return
        if( scrip == null ) {
            return 0 ;
        }

        // The symbol we are dealing with.
        final String QUERY_ID = "ScripITD.insert" ;

        // insert all the elements in batch mode.
        boolean retry = true ;
        while( retry ) {
            try {
                // Filter out the erroneous entries. There are times when
                // it is observed that NSE gives us entries pointing to the
                // future :) and way in the past like 2000 BC.
                final Date now = new Date() ;
                if( scrip.getTime().before( now ) ) {
                    super.daMgr.createRecord( QUERY_ID, scrip ) ;
                }
                else {
                    logger.error( "Found a ITD in the future, ignoring..." ) ;
                    logger.error( "Scrip ITD = " + scrip ) ;
                }
                retry = false ;
            }
            catch ( final DataAccessException e ) {
                if( PostGresUtil.isPKViolation( e ) ) {
                    // Ignore a PK violation
                    logger.debug( "PK violation while inserting Scrip ITD = " + scrip ) ;
                    retry = false ;
                }
                else if ( PostGresUtil.isFKViolation( e ) ) {
                    // Foreign key exceptions imply that the symbol is not yet
                    // registered - we employ fail safe measure by inserting
                    // the symbol at runtime and retrying the insert operation
                    final Symbol symbol = new Symbol() ;
                    symbol.setSymbol( scrip.getSymbolId() ) ;
                    symbol.setSeries( "EQ" ) ;
                    symbol.setMarketType( "N" ) ;
                    symbol.setDescription( "Dynamically added symbol" ) ;
                    this.symbolDAO.addSymbol( symbol ) ;
                }
                else {
                    logger.error( "Unable to insert ITD data. Msg:" + e.getMessage() ) ;
                    logger.debug( "Unable to insert ITD data.", e ) ;
                    retry = false ;
                }
            }
        }

        // With the new version of PRISM, we will get the value to return
        // Till such time this is a place holder.
        return 0 ;
    }

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
    public List<ScripITD> getScripITD( final String symbol, final Date date )
        throws DataAccessException {

        return getScripITD( symbol, STUtils.getStartOfDay( date ),
                            STUtils.getEndOfDay( date ) ) ;
    }

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
    @SuppressWarnings("unchecked")
    public List<ScripITD> getLatestScripITD() throws DataAccessException {

        final String QUERY_ID = "ScripITD.getLatestScripITD" ;
        List<ScripITD> retVal = super.daMgr.searchRecords(
                                                        QUERY_ID, null ) ;
        if( retVal == null ) {
            retVal = Collections.emptyList() ;
        }

        return retVal ;
    }

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
    @SuppressWarnings("unchecked")
    public List<ScripITD> getScripITD( final String symbol, final Date start,
                                       final Date end )
        throws DataAccessException {

        final String QUERY_ID = "ScripITD.getITDForDateRange" ;
        final Map<String, Object> paramMap = new HashMap<String, Object>() ;

        paramMap.put( "scrip",    symbol ) ;
        paramMap.put( "fromDate", start ) ;
        paramMap.put( "toDate",   end ) ;

        List<ScripITD> retVal = super.daMgr.searchRecords(
                                                        QUERY_ID, paramMap ) ;
        if( retVal == null ) {
            retVal = Collections.emptyList() ;
        }

        return retVal ;
    }

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
    public void archiveLiveRecords( final Date boundary )
        throws DataAccessException {

        final String QUERY_ID = "ScripITD.archiveLiveRecords" ;
        super.daMgr.createRecord( QUERY_ID, boundary ) ;
    }

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
    public void deleteLiveRecords( final Date boundary )
        throws DataAccessException {

        final String QUERY_ID = "ScripITD.deleteLiveRecords" ;
        super.daMgr.deleteRecord( QUERY_ID, boundary ) ;
    }
}
