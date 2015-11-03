/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 18, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.jobsummary;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;

/**
 * This class acts as the cell renderer for the ITD summary columns.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class JobSummaryTableCellRenderer extends DefaultTableCellRenderer {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 561684807893431129L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( JobSummaryTableCellRenderer.class ) ;

    /** Public constructor. */
    public JobSummaryTableCellRenderer() {
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
        if( value instanceof Icon ) {
            label.setText( "" ) ;
            label.setIcon( ( Icon ) value ) ;
        }
        else if( value instanceof String ) {
            label.setText( ( String ) value ) ;
        }
        else {
            // Do nothing, the super class has already done the formatting.
        }

        return label ;
    }
}
