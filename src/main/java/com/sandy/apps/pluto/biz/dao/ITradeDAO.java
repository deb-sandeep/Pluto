/**
 * Creation Date: Jan 13, 2009
 */

package com.sandy.apps.pluto.biz.dao;

import java.util.List ;

import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.dto.Trade ;

/**
 * This interface encapsulates and exposes the operations that can be performed
 * on persistent trade instances in the database.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface ITradeDAO {

    /**
     * Inserts the given trade into the database. Insertion will only be attempted
     * in case the trade identifier of the incoming message is -1. If not, this
     * method will attempt an update of the order. In case the incoming message's
     * trade identifier is -1, this method will try to inject an unique
     * trade identifier before inserting the trade into persistent storage.
     * <p>
     * Upon successful insertion, this method publishes the
     * {@link EventType#TRADE_ADDED} event. If this operation resulted in an
     * update, the TRADE_UPDATED event is published.
     *
     * @param trade The trade to add to persistent storage.
     * @return The input trade instance with a valid trade identifier populated
     */
    Trade add( final Trade trade ) ;

    /**
     * Updates the given trade into the database. Updation will only be attempted
     * in case the trade identifier of the incoming message is not -1. If not, this
     * method will attempt an insert the trade. In case the incoming message's
     * trade identifier is -1, this method will try to inject an unique
     * trade identifier before inserting the trade into persistent storage.
     * <p>
     * Upon successful insertion, this method publishes the
     * {@link EventType#TRADE_ADDED} event. If this operation resulted in an
     * update, the TRADE_UPDATED event is published.
     *
     * @param trade The trade that was updated.
     * @return The input trade instance with a valid trade identifier populated
     */
    Trade update( final Trade trade ) ;

    /**
     * Removes the trade with the specified id from the database. In case the
     * specified trade was successfully removed from the persistent storage,
     * the event TRADE_DELETED will be generated, the value of which will
     * be a {@link Trade} instance encapsulating the details of the trade
     * which was removed.
     *
     * @param tradeId The identifier of the trade.
     */
    void remove( final int tradeId ) ;

    /**
     * Returns all the trades registered in the database in ascending order
     * of their trade dates.
     *
     * @return A list of all the trades registered in the system. If no trades
     *         are found, an empty list is returned. Never null.
     */
    List<Trade> getAllTrades() ;
}
