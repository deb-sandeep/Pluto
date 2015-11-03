/**
 * Creation Date: Jul 31, 2008
 */

package com.sandy.apps.pluto.biz.dao;

import java.util.List ;
import java.util.Map ;

import com.sandy.apps.pluto.shared.dto.Symbol ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * This DAO exposes operations to store and retrieve Symbol details from the
 * persistent storage.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface ISymbolDAO {

    /** Static constant to indicate symbols belonging to NIFTY 500 category. */
    String NIFTY_500 = "NIFTY500" ;

    /** Static constant to indicate symbols belonging to NIFTY 200 category. */
    String NIFTY_200 = "NIFTY200" ;

    /** Static constant to indicate symbols belonging to NIFTY 100 category. */
    String NIFTY_100 = "NIFTY100" ;

    /** Static constant to indicate symbols belonging to NIFTY 50 category. */
    String NIFTY = "NIFTY" ;

    /**
     * Returns a map of symbol codes versus an object representation of the
     * symbol.
     *
     * @return A map of symbols, qualified by their symbol key.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         operation
     */
    Map<String, Symbol> getAllSymbolsMap()
        throws DataAccessException ;

    /**
     * Adds the specified symbol to the existing set of symbols. Inserts the
     * symbol in the database, if such a symbol already exists, the symbol
     * information is overridden with the one provided and hence this method can
     * be safely used for an update operation.
     *
     * @param symbol The symbol which needs to be added to the persistent storage.
     *        If such a symbol already exists, this method will throw an exception.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         operation or if such a symbol already exists.
     */
    void addSymbol( final Symbol symbol )
        throws DataAccessException ;

    /**
     * Updates the specified symbol to the existing set of symbols. If no
     * such symbol already exists, this method does nothing.
     *
     * @param symbol The symbol which needs to be updated in the persistent storage.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         operation or if such a symbol already exists.
     */
    void updateSymbol( final Symbol symbol )
        throws DataAccessException ;

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
    Symbol getSymbol( final String symbol )
        throws DataAccessException ;

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
    List<Symbol> getSymbolsForNiftyCat( final String niftyCat ) ;
}
