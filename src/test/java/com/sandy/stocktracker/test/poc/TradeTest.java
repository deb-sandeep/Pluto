/**
 * 
 * 
 * 
 *
 * Creation Date: Jan 13, 2009
 */

package com.sandy.stocktracker.test.poc;
import java.util.Date;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.dto.Trade ;

/**
 * Basic tests for a trade.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class TradeTest {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( TradeTest.class ) ;

    private Trade getTrade() {
        final Trade trade = new Trade() ;

        trade.setSymbol( "ICICIBANK" ) ;
        trade.setDate( new Date() ) ;
        trade.setUnits( 100 ) ;
        trade.setUnitPrice( 240.00 ) ;
        trade.setBrokerage( 240.00 ) ;
        trade.setBuy( true ) ;
        trade.setTradeType( Trade.CASH ) ;

        return trade ;
    } ;

    public static void main( final String[] args ) {
        logger.debug( new TradeTest().getTrade() ) ;
    }
}
