/**
 * 
 * 
 * 
 *
 * Creation Date: Sep 8, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.indexitdsummary;
import java.util.Date;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.CacheListener ;
import com.sandy.apps.pluto.shared.dto.ExIndexITD ;

/**
 * This class provides the table model required for displaying the job
 * summary information.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class IndexITDSummaryTableModel extends AbstractTableModel
    implements CacheListener {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 9850964192L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( IndexITDSummaryTableModel.class ) ;

    /** The cache from which the ITD data is actually fetched for display. */
    private final IndexITDValueCache valueCache ;

    /** Public no argument constructor. */
    public IndexITDSummaryTableModel() {
        this.valueCache = IndexITDValueCache.getInstance() ;
    }

    /** Returns the number of columns supported by the ITD summary panel. */
    @Override
    public int getColumnCount() {
        return this.valueCache.getColumnCount() ;
    }

    /** Returns the number of rows for the table. */
    @Override
    public int getRowCount() {
        return this.valueCache.getRowCount() ;
    }

    /** Returns the name of the column at the specified column index. */
    @Override
    public String getColumnName( final int column ) {
        return this.valueCache.getColumnName( column ) ;
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
        return this.valueCache.getValueAt( rowIndex, columnIndex ) ;
    }

    /** Returns the class of the column at the specified column index. */
    @Override
    public Class<?> getColumnClass( final int columnIndex ) {
        return this.valueCache.getColumnClass( columnIndex ) ;
    }

    /**
     * Returns the last time the data in the model was refreshed.
     */
    public Date getLastRefreshTime() {
        return this.valueCache.getLastRefreshTime() ;
    }

    /** Returns the {@link ExIndexITD} instance for the specified row. */
    public ExIndexITD getIndexITDForRow( final int row ) {
        return this.valueCache.getExIndexITDForRow( row ) ;
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
