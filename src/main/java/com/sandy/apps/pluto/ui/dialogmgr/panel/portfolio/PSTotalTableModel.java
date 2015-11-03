/**
 * 
 * 
 * 
 *
 * Creation Date: Sep 8, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.portfolio;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * This class is the table model for the showing the total of portfolio summary
 * for all the stocks held by the user.
 * <p>
 * This model is backed by the singleton {@link PortfolioManager} instance,
 * from which the data is derived.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class PSTotalTableModel extends PSTableModel {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 9850964192L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( PSTotalTableModel.class ) ;

    /** Public no argument constructor. */
    public PSTotalTableModel() {
        super() ;
    }

    /** Returns the number of rows for the table. */
    @Override
    public int getRowCount() {
        return 1 ;
    }

    /**
     * Returns the value of the column at the specified row index.
     */
    @Override
    public Object getValueAt( final int rowIndex, final int columnIndex ) {

        Object value = null ;

        final List<StockTradeGrouping> stocks = getStocks() ;
        double val = 0 ;

        switch( columnIndex ) {
            case COL_SYMBOL:
                value = "Total" ;
                break ;
            case COL_QTY:
            case COL_PRICE:
            case COL_LTP:
                value = -1 ; ;
                break ;
            case COL_UNREALIZED:
                for( int i=0; i<stocks.size(); i++ ) {
                    val += stocks.get( i ).getUnRealizedCashProfit() ;
                }
                value = val ;
                break ;
            case COL_VAL_AT_COST:
                for( int i=0; i<stocks.size(); i++ ) {
                    val += stocks.get( i ).getAvgCashUnitPrice() *
                           stocks.get( i ).getNumActiveCashUnits() ;
                }
                value = val ;
                break ;
            case COL_VAL_AT_MKT:
                for( int i=0; i<stocks.size(); i++ ) {
                    val += getLTP( stocks.get( i ).getSymbol() ) *
                           stocks.get( i ).getNumActiveCashUnits() ;
                }
                value = val ;
                break ;
            case COL_UNREALIZED_PCT:
                double totalUnrealized = 0 ;
                for( int i=0; i<stocks.size(); i++ ) {
                    totalUnrealized += stocks.get( i ).getUnRealizedCashProfit() ;
                }

                double totalValAtCost = 0 ;
                for( int i=0; i<stocks.size(); i++ ) {
                    totalValAtCost += stocks.get( i ).getAvgCashUnitPrice() *
                                      stocks.get( i ).getNumActiveCashUnits() ;
                }
                value = ( totalUnrealized / totalValAtCost )*100 ;
                break ;
            case COL_REALIZED:
                for( int i=0; i<stocks.size(); i++ ) {
                    val += stocks.get( i ).getRealizedProfit() ;
                }
                value = val ;
                break ;
            default:
                value = "N/A" ;
                break ;
        }

        return value ;
    }
}
