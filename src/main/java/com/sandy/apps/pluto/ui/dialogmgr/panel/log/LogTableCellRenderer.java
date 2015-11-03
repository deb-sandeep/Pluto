/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 18, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.log;
import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.dto.LogMsg.Sev ;
import com.sandy.apps.pluto.ui.UIConstant ;

/**
 * This class acts as the cell renderer for the log table columns. For the
 * severity column, it replaces the severity with an equivalent visual icon,
 * for the time column it replaces the date with a HH:mm:ss formatted string,
 * while for the message column, the as is value is substituted.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class LogTableCellRenderer extends DefaultTableCellRenderer {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 561684807893431129L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( LogTableCellRenderer.class ) ;

    /** The date format used to render the time for the message. */
    private static final DateFormat TIME_DF = new SimpleDateFormat( "HH:mm:ss" ) ;

    /** Public constructor. */
    public LogTableCellRenderer() {
        super() ;
    }

    /** Returns the renderer component for the specified table cell. */
    @Override
    public Component getTableCellRendererComponent( final JTable table, final Object value,
            final boolean isSelected, final boolean hasFocus, final int row, final int column ) {

        // Let the super class have the first stab at rendering the cell.
        final JLabel label = ( JLabel )super.getTableCellRendererComponent(
                             table, value, isSelected, hasFocus, row, column ) ;

        // If this is the 0th column the value is the severity of the message
        if( value instanceof Sev ) {
            renderMsgSeverity( ( Sev )value, label ) ;
        }
        else if( value instanceof Date ) {
            renderMsgTime( ( Date )value, label ) ;
        }
        else {
            // Do nothing, the super class has already done the formatting.
        }

        return label ;
    }

    /** Renders the time of the message in HH:mm:ss format. */
    private void renderMsgTime( final Date value, final JLabel label ) {
        label.setFont( UIConstant.LOG_FONT ) ;
        label.setText( TIME_DF.format( value ) ) ;
    }

    /** Depending upon the severity, sets the proper icon in the label. */
    private void renderMsgSeverity( final Sev value, final JLabel label ) {
        label.setText( null ) ;
        if( value == Sev.INFO ) {
            label.setIcon( new ImageIcon( UIConstant.IMG_LOG_INFO ) ) ;
        }
        else if( value == Sev.WARN ) {
            label.setIcon( new ImageIcon( UIConstant.IMG_LOG_WARN ) ) ;
        }
        else if( value == Sev.ERROR ) {
            label.setIcon( new ImageIcon( UIConstant.IMG_LOG_ERROR ) ) ;
        }
    }
}
