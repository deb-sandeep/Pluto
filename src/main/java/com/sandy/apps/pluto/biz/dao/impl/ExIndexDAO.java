/**
 * Creation Date: Aug 9, 2008
 */

package com.sandy.apps.pluto.biz.dao.impl;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.dao.IExIndexDAO ;
import com.sandy.apps.pluto.biz.svc.impl.EODImportSvc ;
import com.sandy.apps.pluto.shared.dto.ExIndexEOD ;
import com.sandy.apps.pluto.shared.dto.ExIndexITD ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * Implementation of {@link IExIndexDAO} interface.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ExIndexDAO extends AbstractBaseDAO implements IExIndexDAO {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ExIndexDAO.class ) ;

    /** Public constructor. */
    public ExIndexDAO() {
        super() ;
    }

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
    @SuppressWarnings("unchecked")
    public List<String> getIndexNames( final String exchangeName )
        throws DataAccessException {

        final String QUERY_ID = "ExIndex.getExIndexes" ;
        return super.daMgr.searchRecords( QUERY_ID, exchangeName ) ;
    }

    /**
     * Adds the given {@link ExIndexEOD} instance to the persistent storage.
     *
     * @param data The EOD data that needs to be saved in the database.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         process of data access
     */
    public void addEODData( final ExIndexEOD data )
        throws DataAccessException {

        // The symbol we are dealing with.
        final String QUERY_ID = "ExIndex.insertEOD" ;

        try {
            if( logger.isDebugEnabled() ) {
                logger.debug( "Inserting EOD for index " + data.getIndex() +
                " date " + EODImportSvc.NSE_POST_DF.format( data.getDate() ) ) ;
            }
            super.daMgr.createRecord( QUERY_ID, data ) ;
        }
        catch ( final DataAccessException e ) {
            // If this is a PK violation, we ignore the exception
            if( PostGresUtil.isPKViolation( e ) ) {
                // Ignore primary key violations.
            }
            else {
                throw e ;
            }
        }
    }

    /**
     * Adds the given {@link ExIndexITD} instance to the persistent storage.
     *
     * @param data The ITD data that needs to be saved in the database.
     *
     * @return true if teh data was successfully added to the persistent storage
     *         false otherwise.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         process of data access
     */
    public boolean addITDData( final ExIndexITD data )
        throws DataAccessException {

        boolean retVal = false ;
        // The symbol we are dealing with.
        final String QUERY_ID = "ExIndex.insertITD" ;

        try {
            super.daMgr.createRecord( QUERY_ID, data ) ;
            retVal = true ;
            if( logger.isDebugEnabled() ) {
                logger.debug( "Inserting ITD for index " + data.getIndex() +
                " date " + data.getDate() ) ;
            }
        }
        catch ( final DataAccessException e ) {
            // If this is a PK violation, we ignore the exception
            if( PostGresUtil.isPKViolation( e ) ) {
                // Ignore primary key violations.
            }
            else {
                throw e ;
            }
        }
        return retVal ;
    }

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
    public int updateEOD( final List<ExIndexEOD> eodIndices )
        throws DataAccessException {

        // If we have nothing to delete, just return
        if( eodIndices == null || eodIndices.isEmpty() ) {
            return 0 ;
        }

        // The symbol we are dealing with.
        final String QUERY_ID = "ExIndex.updateEOD" ;

        for( final ExIndexEOD index : eodIndices ) {
            if( logger.isDebugEnabled() ) {
                logger.debug( "Updating EOD for " + index.getIndex() +
                " date " + EODImportSvc.NSE_POST_DF.format( index.getDate() ) ) ;
            }
            super.daMgr.updateRecord( QUERY_ID, index ) ;
        }

        // With the new version of PRISM, we will get the value to return
        // Till such time this is a place holder.
        return 0 ;
    }

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
    @SuppressWarnings("unchecked")
    public List<ExIndexEOD> getExIndexEODList( final String index )
        throws DataAccessException {

        final String QUERY_ID = "ExIndex.getExIndexEODForIndex" ;
        return super.daMgr.searchRecords( QUERY_ID, index ) ;
    }

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
    @SuppressWarnings("unchecked")
    public List<ExIndexEOD> getExIndexEODList( final String index,
                                               final Date fromDate,
                                               final Date toDate )
        throws DataAccessException {

        final String QUERY_ID = "ExIndex.getExIndexEODForIndexWithRange" ;
        final Map<String, Object> paramMap = new HashMap<String, Object>() ;

        paramMap.put( "indexName", index ) ;
        paramMap.put( "fromDate",  fromDate ) ;
        paramMap.put( "toDate",    toDate ) ;

        return super.daMgr.searchRecords( QUERY_ID, paramMap ) ;
    }

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
    @SuppressWarnings("unchecked")
    public List<ExIndexITD> getExIndexITDList( final String index,
                                               final Date fromDate,
                                               final Date toDate )
        throws DataAccessException {

        final String QUERY_ID = "ExIndex.getExIndexITDForIndexWithRange" ;
        final Map<String, Object> paramMap = new HashMap<String, Object>() ;

        paramMap.put( "indexName", index ) ;
        paramMap.put( "fromDate",  fromDate ) ;
        paramMap.put( "toDate",    toDate ) ;

        return super.daMgr.searchRecords( QUERY_ID, paramMap ) ;
    }

    /**
     * Gets the latest (last inserted) EOD value for the index name specified.
     *
     * @param index The name of the index.
     *
     * @return An instance of {@link ExIndexEOD} or null if no value is found.
     */
    public ExIndexEOD getLatestEOD( final String index ) {

        final String QUERY_ID = "ExIndex.getLatestEOD" ;
        return ( ExIndexEOD )super.daMgr.retrieveRecord( QUERY_ID, index ) ;
    }
}
