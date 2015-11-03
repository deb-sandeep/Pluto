/**
 * Creation Date: Aug 3, 2008
 */

package com.sandy.apps.pluto.biz.dao.impl;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.dao.ISymbolDAO ;
import com.sandy.apps.pluto.shared.dto.Symbol ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * An implementation of {@link ISymbolDAO} which helps manage the Symbols
 * stored in the database.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class SymbolDAO extends AbstractBaseDAO implements ISymbolDAO {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( SymbolDAO.class ) ;

    /** Public constructor. */
    public SymbolDAO() {
        super() ;
    }

    /**
     * OVERRIDDEN METHOD: Inserts the symbol in the database, if such a symbol
     * already exists, the symbol information is overridden with the one
     * provided and hence this method can be safely used for an update
     * operation.
     */
    @Override
    public void addSymbol( final Symbol symbol ) throws DataAccessException {
        final String INSERT_QUERY_ID = "Symbol.insert" ;

        try {
            logger.debug( "Inserting symbol " + symbol.getSymbol() ) ;
            super.daMgr.createRecord( INSERT_QUERY_ID, symbol ) ;
        }
        catch( final DataAccessException dae ) {
            if( PostGresUtil.isPKViolation( dae ) ) {
                updateSymbol( symbol ) ;
            }
            else {
                throw dae ;
            }
        }
    }

    /**
     * Updates the specified symbol to the existing set of symbols. If no
     * such symbol already exists, this method does nothing.
     *
     * @param symbol The symbol which needs to be updated in the persistent storage.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         operation or if such a symbol already exists.
     */
    public void updateSymbol( final Symbol symbol )
        throws DataAccessException {

        final String UPDATE_QUERY_ID = "Symbol.update" ;

        logger.debug( "Updating symbol " + symbol.getSymbol() ) ;
        super.daMgr.updateRecord( UPDATE_QUERY_ID, symbol ) ;
    }

    /**
     * Returns a map of symbol codes versus an object representation of the
     * symbol.
     *
     * @return A map of symbols, qualified by their symbol key.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         operation
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Symbol> getAllSymbolsMap() throws DataAccessException {

        final String QUERY_ID = "Symbol.getAll" ;
        final List<Symbol> symbolList = super.daMgr.searchRecords( QUERY_ID, null ) ;
        final Map<String, Symbol> symbolMap = new HashMap<String, Symbol>() ;
        for( final Symbol symbol : symbolList ) {
            symbolMap.put( symbol.getSymbol(), symbol ) ;
        }
        return symbolMap ;
    }

    /**
     * Returns a {@link Symbol} instance corresponding to the symbol key
     * specified.
     *
     * @param symbol The symbol key
     *
     * @return A symbol instance corresponding to the key specified.
     *
     * @throws DataAccessException If an exception is generated during the
     *         process of data retrieval.
     */
    public Symbol getSymbol( final String symbolName )
        throws DataAccessException {

        final String QUERY_ID = "Symbol.getSymbol" ;
        final Symbol symbol = ( Symbol )super.daMgr.retrieveRecord( QUERY_ID, symbolName ) ;
        return symbol ;
    }

    /**
     * Returns the symbols which fall into the the provided nifty category. The
     * nifty categories are as mentioned by the NIFTY* constants in the
     * {@link ISymbolDAO} interface.
     *
     * @param niftyCat The nifty category.
     *
     * @return A list of {@link Symbol} which belong to the nifty category
     *         provided.
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Symbol> getSymbolsForNiftyCat( final String niftyCat ) {
        final String QUERY_ID = "Symbol.getSymbolForCategory" ;
        final List<Symbol> symbolList = super.daMgr.searchRecords( QUERY_ID, niftyCat ) ;
        return symbolList ;
    }
}
