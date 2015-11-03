/**
 * 
 * 
 * 
 *
 * Creation Date: Sep 8, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.portfolio;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.CacheListener ;
import com.sandy.apps.pluto.shared.dto.ScripEOD ;
import com.sandy.apps.pluto.shared.dto.ScripITD ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.scripeodsummary.ScripEODValueCache ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.scripitdsummary.ScripITDValueCache ;

/**
 * This class is the table model for the portfolio display table. The portfolio
 * display table shows aggregated trade values for all the symbols that the
 * user has traded in.
 * <p>
 * This model is backed by the singleton {@link PortfolioManager} instance,
 * from which the data is derived.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class PSTableModel extends AbstractTableModel
    implements CacheListener {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 9850964192L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( PSTableModel.class ) ;

    /**
     * The column properties for this table. Each row contains a row description
     * where the 0th column is the column header and the 1st column is the
     * class of object representing the type of object representing the column
     * data.
     */
    public static final Object[][] COL_PROPERTIES = {
        { "Stock", String.class },
        { "P",     Double.class },
        { "P%",    Double.class },
        { "Qty",   Integer.class },
        { "Price", Double.class },
        { "LTP",   Double.class },
        { "V@C",   Double.class },
        { "V@M",   Double.class },
        { "PTD",   Double.class }
    } ;

    // Column indentifiers
    public static final int COL_SYMBOL         = 0 ;
    public static final int COL_UNREALIZED     = 1 ;
    public static final int COL_UNREALIZED_PCT = 2 ;
    public static final int COL_QTY            = 3 ;
    public static final int COL_PRICE          = 4 ;
    public static final int COL_LTP            = 5 ;
    public static final int COL_VAL_AT_COST    = 6 ;
    public static final int COL_VAL_AT_MKT     = 7 ;
    public static final int COL_REALIZED       = 8 ;

    /**
     * A reference to the {@link PortfolioManager} from which this model derives
     * its values for display via the table user interface.
     */
    private final PortfolioManager portfolioMgr = PortfolioManager.getInstance() ;

    /** A reference to the ScripITDValueCache for checking the LTP. */
    private final ScripITDValueCache itdCache = ScripITDValueCache.getInstance() ;

    /** A reference to the ScripEODValueCache for checking the LTP. */
    private final ScripEODValueCache eodCache = ScripEODValueCache.getInstance() ;

    /** A boolean value indicating if the zero holding stocks are to be displayed.*/
    private boolean showZeroHoldingStocks = false ;

    /** Public no argument constructor. */
    public PSTableModel() {
    }

    /**
     * Gets the list of stocks from the portfolio manager based on whether to
     * show positive or zero holding stocks.
     */
    protected List<StockTradeGrouping> getStocks() {
        if( this.showZeroHoldingStocks ) {
            return this.portfolioMgr.getAllTrades() ;
        }
        else {
            return this.portfolioMgr.getActiveTrades() ;
        }
    }

    /** Returns the number of columns supported by the ITD summary panel. */
    @Override
    public int getColumnCount() {
        return COL_PROPERTIES.length ;
    }

    /** Returns the number of rows for the table. */
    @Override
    public int getRowCount() {
        return getStocks().size() ;
    }

    /** Returns the name of the column at the specified column index. */
    @Override
    public String getColumnName( final int column ) {
        return ( String )COL_PROPERTIES[column][0] ;
    }

    /** None of this table cells are editable. */
    @Override
    public boolean isCellEditable( final int rowIndex, final int columnIndex ) {
        return false ;
    }

    /**
     * Returns the value of the column at the specified row index.
     */
    @Override
    public Object getValueAt( final int rowIndex, final int columnIndex ) {

        Object value = null ;

        final List<StockTradeGrouping> stocks = getStocks() ;
        final StockTradeGrouping       stock  = stocks.get( rowIndex ) ;

        switch( columnIndex ) {
            case COL_SYMBOL:
                value = stock.getSymbol() ;
                break ;
            case COL_QTY:
                value = stock.getNumActiveCashUnits() ;
                break ;
            case COL_PRICE:
                value = stock.getAvgCashUnitPrice() ;
                break ;
            case COL_LTP:
                value = getLTP( stock.getSymbol() ) ;
                break ;
            case COL_UNREALIZED:
                value = stock.getUnRealizedCashProfit() ;
                break ;
            case COL_VAL_AT_COST:
                value = stock.getAvgCashUnitPrice() * stock.getNumActiveCashUnits() ;
                break ;
            case COL_VAL_AT_MKT:
                value = stock.getNumActiveCashUnits() * getLTP( stock.getSymbol() ) ;
                break ;
            case COL_UNREALIZED_PCT:
                double valAtCost = 0 ;
                if( stock.getNumActiveCashUnits() > 0 ) {
                    valAtCost = stock.getAvgCashUnitPrice() * stock.getNumActiveCashUnits() ;
                    value = ( stock.getUnRealizedCashProfit() / valAtCost ) * 100 ;
                }
                else {
                    value = 0.0d ;
                }
                break ;
            case COL_REALIZED:
                value = stock.getRealizedProfit() ;
                break ;
            default:
                value = "N/A" ;
                break ;
        }

        return value ;
    }

    /**
     * Returns the current price for a given symbol. First the intra day cache
     * is searched, if not found, the end of day cache is searched for the
     * lastest registered value. If none are found, the price is returned as 0
     */
    protected double getLTP( final String symbol ) {

        ScripITD itdVal = null ;
        ScripEOD eodVal = null ;
        double lastTradedPrice = 0 ;
        itdVal = this.itdCache.getScripITDForSymbol( symbol ) ;

        if( itdVal != null ) {
            lastTradedPrice = itdVal.getPrice() ;
        }
        else {
            eodVal = this.eodCache.getScripEOD( symbol ) ;
            if( eodVal != null ) {
                lastTradedPrice = eodVal.getPrevClosePrice() ;
            }
            else {
                lastTradedPrice = 0.0 ;
            }
        }
        return lastTradedPrice ;
    }

    /**
     * Returns the percentage change of the LTP against the previous EOD closing
     * value. The row index is mapped onto the ScripITD that the row represents
     * and the percentage EOD change is returned.
     *
     * @param rowIndex The row in the table for whose scrip the %EOD is required
     * @return The percentage change value.
     */
    double getPctEChange( final int rowIndex ) {
        double pctEChange = 0 ;
        final String symbol = getStocks().get( rowIndex ).getSymbol() ;
        final ScripITD itd  = this.itdCache.getScripITDForSymbol( symbol ) ;
        if( itd == null ) {
            final ScripEOD eod = this.eodCache.getScripEOD( symbol ) ;
            if( eod != null ) {
                pctEChange = ( ( eod.getClosingPrice() - eod.getPrevClosePrice() )/
                               eod.getPrevClosePrice() )*100 ;
            }
        }
        else {
            pctEChange = itd.getPctChange() ;
        }
        return pctEChange ;
    }

    /** Returns the class of the column at the specified column index. */
    @Override
    public Class<?> getColumnClass( final int columnIndex ) {
        return ( Class<?> )COL_PROPERTIES[columnIndex][1] ;
    }

    /**
     * This method is invoked when the data in the ITD cache has changed. This
     * is a queue for us to fire a table changed event will will result in
     * the UI being refreshed with the new data in the cache.
     */
    public void cacheDataChanged() {
        fireTableDataChanged() ;
    }

    /**
     * @param showZeroHoldingStocks the showZeroHoldingStocks to set
     */
    public void setShowZeroHoldingStocks( final boolean showZeroHoldingStocks ) {
        this.showZeroHoldingStocks = showZeroHoldingStocks ;
    }

    /**
     * Gets the {@link StockTradeGrouping} instance representing the row
     * whose index is passed as parameter.
     *
     * @param modelRowIndex The row index for the model
     * @return The {@link StockTradeGrouping} instance for the row.
     */
    public StockTradeGrouping getStockTradeGrouping( final int modelRowIndex ) {
        return getStocks().get( modelRowIndex ) ;
    }
}
