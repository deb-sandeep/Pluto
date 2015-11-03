/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 18, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.log;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.dto.LogMsg.Sev ;

/**
 * This class represents the table model for the log display panel. This model
 * operates upon a list of LogMsg instances and notifies the display of
 * change of data.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
class LogTableModel extends AbstractTableModel {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 561684807893431129L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( LogTableModel.class ) ;

    /** A reference to the list of log messages. */
    private final List<LogMsg> logMsgs ;

    /**
     * Public constructor.
     * @param logMessages A reference to a list of {@link LogMsg} instances.
     *        Note that messages in this list are managed by some external
     *        party and this model is notified when the data in the list has
     *        changed. This model is just a read only operator on the list of
     *        log messages.
     */
    public LogTableModel( final List<LogMsg> logMessages ) {
        this.logMsgs = logMessages ;
    }

    /** Returns the number of columns in the log display panel. */
    @Override
    public int getColumnCount() {
        return LogDisplayPanel.COL_NAMES.length ;
    }

    /** Returns the number of rows in the log display panel. */
    @Override
    public int getRowCount() {
        return this.logMsgs.size() ;
    }

    /**
     * Returns the value of the column at the specified row index. Note that
     * for the log display table, column 0 is the severity column, column 1
     * is the time column while column 2 is the message column.
     * <p>
     * The following object types are returned for values at different columns
     * <ul>
     *  <li>Column 0 - {@link LogMsg.Sev}</li>
     *  <li>Column 1 - java.util.Date</li>
     *  <li>Column 2 - java.lang.String</li>
     * </ul>
     */
    @Override
    public Object getValueAt( final int rowIndex, final int columnIndex ) {
        Object retVal = null ;
        final LogMsg msg = this.logMsgs.get( rowIndex ) ;
        if( columnIndex == 0 ) {
            retVal = msg.getSev() ;
        }
        else if( columnIndex == 1 ) {
            retVal = msg.getTime() ;
        }
        else if( columnIndex == 2 ) {
            retVal = msg.getMessage() ;
        }
        return retVal ;
    }

    /** Returns the class of the column at the specified column index. */
    @Override
    public Class<?> getColumnClass( final int columnIndex ) {
        Class<?> cls = null ;
        switch( columnIndex ) {
            case 0:
                cls = Sev.class ;
                break ;
            case 1:
                cls = Date.class ;
                break ;
            case 2:
                cls = String.class ;
                break ;
        }
        return cls ;
    }

    /** Returns the name of the column at the specified column index. */
    @Override
    public String getColumnName( final int column ) {
        return LogDisplayPanel.COL_NAMES[column] ;
    }

    /**
     * This method should be invoked by an external entity to let the table
     * model know that the internal data has changed and it should notify the
     * view of this model of the same.
     */
    public void tableDataChanged() {
        super.fireTableDataChanged() ;
    }

    /** None of this table cells are editable. */
    @Override
    public boolean isCellEditable( final int rowIndex, final int columnIndex ) {
        return false ;
    }
}
