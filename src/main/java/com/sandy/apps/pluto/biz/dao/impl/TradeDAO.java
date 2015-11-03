/**
 * Creation Date: Jan 13, 2009
 */

package com.sandy.apps.pluto.biz.dao.impl;
import java.util.Collections ;
import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.dao.ITradeDAO ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.dto.Trade ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * An implementation of the {@link ITradeDAO} interface.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class TradeDAO extends AbstractBaseDAO
    implements ITradeDAO {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( TradeDAO.class ) ;

    /** Public no argument constructor. */
    public TradeDAO() {
    }

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
    @Override
    public Trade add( final Trade trade ) {

        final String INSERT_QUERY_ID = "Trade.insert" ;

        try {
            logger.debug( "Inserting trade " + trade.toString() ) ;
            if( trade.getTradeId() == -1 ) {
                trade.setTradeId( getNextTradeId() ) ;
                super.daMgr.createRecord( INSERT_QUERY_ID, trade ) ;
                EventBus.publish( EventType.TRADE_ADDED, trade ) ;
            }
            else {
                update( trade ) ;
            }
        }
        catch( final DataAccessException dae ) {
            if( PostGresUtil.isPKViolation( dae ) ) {
                update( trade ) ;
            }
            else {
                // Set the trade id of the trade to -1. If this is not done
                // the instance would have been assigned a trade id. The good
                // practice is that the caller would have relinquished control
                // of the incoming trade (tier separation), but then.. best
                // practices are recommendations after all :D
                trade.setTradeId( -1 ) ;
                throw dae ;
            }
        }

        return trade ;
    }

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
    @Override
    public Trade update( final Trade trade ) {

        final String UPDATE_QUERY_ID = "Trade.update" ;
        logger.debug( "Updating trade " + trade.toString() ) ;
        if( trade.getTradeId() == -1 ) {
            return add( trade ) ;
        }
        else {
            super.daMgr.updateRecord( UPDATE_QUERY_ID, trade ) ;
            EventBus.publish( EventType.TRADE_UPDATED, trade ) ;
        }
        return trade ;
    }

    /**
     * Removes the trade with the specified id from the database. In case the
     * specified trade was successfully removed from the persistent storage,
     * the event TRADE_DELETED will be generated, the value of which will
     * be a {@link Trade} instance encapsulating the details of the trade
     * which was removed.
     *
     * @param tradeId The identifier of the trade.
     */
    @Override
    public void remove( final int tradeId ) {

        final String QUERY_ID = "Trade.delete" ;

        logger.debug( "Deleting trade " + tradeId ) ;
        final Trade trade = getTrade( tradeId ) ;
        if( trade != null ) {
            super.daMgr.deleteRecord( QUERY_ID, new Integer( tradeId ) ) ;
            EventBus.publish( EventType.TRADE_DELETED, trade ) ;
        }
    }

    /**
     * Returns all the trades registered in the database in ascending order
     * of their trade dates.
     *
     * @return A list of all the trades registered in the system. If no trades
     *         are found, an empty list is returned. Never null.
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Trade> getAllTrades() {

        final String QUERY_ID = "Trade.getAll" ;

        logger.debug( "Getting all trades" ) ;
        List<Trade> trades = super.daMgr.searchRecords( QUERY_ID, null ) ;
        if( trades == null ) {
            trades = Collections.emptyList() ;
        }
        return trades ;
    }

    /**
     * Returns the next available unique trade identifier which can be allocated
     * to a new trade.
     */
    private Integer getNextTradeId() {
        final String QUERY_ID = "Trade.getNextTradeId" ;
        Integer nextId = null ;

        nextId = ( Integer ) super.daMgr.retrieveRecord( QUERY_ID, null ) ;
        if( nextId == null ) {
            nextId = Integer.valueOf( 0 ) ;
        }

        return nextId ;
    }

    /**
     * Returns a trade instance for the given trade id or null if no such
     * trade could be found registered in the system.
     *
     * @param tradeId The trade identifier
     * @return The trade instance corresponding to the given trade id or null
     *         if no such trade is found.
     */
    private Trade getTrade( final int tradeId ) {
        final String QUERY_ID = "Trade.getTrade" ;
        return ( Trade ) super.daMgr.retrieveRecord( QUERY_ID, tradeId ) ;
    }
}
