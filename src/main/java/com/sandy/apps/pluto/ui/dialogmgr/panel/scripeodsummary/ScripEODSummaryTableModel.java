/**
 * 
 * 
 * 
 *
 * Creation Date: Sep 8, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.scripeodsummary;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.CacheListener ;
import com.sandy.apps.pluto.shared.dto.ScripEOD ;
import com.sandy.apps.pluto.shared.dto.ScripITD ;
import com.sandy.apps.pluto.shared.dto.SymbolPctChange ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.scripitdsummary.ScripITDValueCache ;

/**
 * This class provides the table model required for displaying the Scrip EOD
 * summary information.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ScripEODSummaryTableModel extends AbstractTableModel
    implements CacheListener {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 9850964192L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ScripEODSummaryTableModel.class ) ;

    /**
     * The column properties for this table. Each row contains a row description
     * where the 0th column is the column header and the 1st column is the
     * class of object representing the type of object representing the column
     * data.
     */
    public static final Object[][] COL_PROPERTIES = {
        { "", Boolean.class },
        { "Last 10 %E", Double[].class },
        { "Symbol", String.class },
        { "%E", Double.class },
        { "%O", Double.class },
        { "Price", Double.class },
        { "Quantity", Long.class },
        { "ICICI", String.class },
        { "Name", String.class }
    } ;

    public static final int COL_INDEX_LINKED = 0 ;
    public static final int COL_PCT_CHG_HIST = 1 ;
    public static final int COL_SYMBOL       = 2 ;
    public static final int COL_PCT_E        = 3 ;
    public static final int COL_PCT_O        = 4 ;
    public static final int COL_PRICE        = 5 ;
    public static final int COL_QTY          = 6 ;
    public static final int COL_ICICI        = 7 ;
    public static final int COL_NAME         = 8 ;

    /**
     * A reference to the {@link ScripEODValueCache} from which this model
     * derives the last N percentage change data.
     */
    private final ScripEODValueCache eodCache = ScripEODValueCache.getInstance() ;

    /** A reference to the ScripITDValueCache for checking the index linked status. */
    private final ScripITDValueCache itdCache = ScripITDValueCache.getInstance() ;

    /** Public no argument constructor. */
    public ScripEODSummaryTableModel() {
    }

    /** Returns the number of columns supported by the ITD summary panel. */
    @Override
    public int getColumnCount() {
        return COL_PROPERTIES.length ;
    }

    /** Returns the number of rows for the table. */
    @Override
    public int getRowCount() {
        return this.eodCache.getScripEODList().size() ;
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
        final ScripEOD eod = getScripEODForRow( rowIndex ) ;
        Object value = null ;

        if( eod != null ) {

            final String symbol    = eod.getSymbolId() ;
            final double close     = eod.getClosingPrice() ;
            final double open      = eod.getOpeningPrice() ;
            final double prevClose = eod.getPrevClosePrice() ;

            switch( columnIndex ) {
                case COL_INDEX_LINKED :
                    final ScripITD itd = this.itdCache.getScripITDForSymbol( eod.getSymbolId() ) ;
                    value = ( itd == null ) ? Boolean.FALSE : Boolean.TRUE ;
                    break ;
                case COL_PCT_CHG_HIST:
                    List<SymbolPctChange> list = null ;
                    list = this.eodCache.getSymbolPctChangeList( symbol ) ;
                    if( list != null && !list.isEmpty() ) {
                        // Interesting - the cache holds the EOD pct changes
                        // starting yesterday. Now since in the EOD summary
                        // panel, we show the symbol name highlighted with
                        // the latest %change, we remove the first element in
                        // the list.
                        final List<SymbolPctChange> modList = new ArrayList<SymbolPctChange>() ;
                        modList.addAll( list ) ;
                        modList.remove( 0 ) ;

                        final Double[] changes = new Double[modList.size()] ;
                        for( int i=0; i<modList.size(); i++ ) {
                            changes[i] = modList.get( i ).getPctChange() ;
                        }
                        value = changes ;
                    }
                    break ;
                case COL_SYMBOL :
                    value = eod.getSymbolId() ;
                    break ;
                case COL_ICICI  :
                    value = eod.getSymbol().getIciciCode() ;
                    break ;
                case COL_PRICE  :
                    value = close ;
                    break ;
                case COL_PCT_E  :
                    value = new Double( (( close - prevClose )/prevClose)*100 ) ;
                    break ;
                case COL_PCT_O  :
                    value = new Double( (( close - open )/open)*100 ) ;
                    break ;
                case COL_QTY    :
                    value = new Long( eod.getTotalTradeQty() ) ;
                    break ;
                case COL_NAME   :
                    value = eod.getSymbol().getDescription() ;
                    break ;
            }
        }

        return value ;
    }

    /** Returns the class of the column at the specified column index. */
    @Override
    public Class<?> getColumnClass( final int columnIndex ) {
        return ( Class<?> )COL_PROPERTIES[columnIndex][1] ;
    }

    /**
     * Returns the last time the data in the model was refreshed.
     */
    public Date getDate() {
        return this.eodCache.getDate() ;
    }

    /** Returns the ScripITD instance for the specified row. */
    public ScripEOD getScripEODForRow( final int row ) {
        ScripEOD eod = null ;
        final List<ScripEOD> eodList = this.eodCache.getScripEODList() ;
        if( row < eodList.size() ) {
            eod = eodList.get( row ) ;
        }
        return eod ;
    }

    /**
     * This method is invoked when the data in the ITD cache has changed. This
     * is a queue for us to fire a table changed event will will result in
     * the UI being refreshed with the new data in the cache.
     */
    public void cacheDataChanged() {
        fireTableDataChanged() ;
    }
}
