/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 18, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.news;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.STUtils ;
import com.sandy.apps.pluto.shared.dto.RSSNewsItem ;
import com.sandy.apps.pluto.ui.util.ColorIcon ;

/**
 * This class acts as the cell renderer for the task summary columns.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class NewsSummaryTableCellRenderer extends DefaultTableCellRenderer {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 561684807893431129L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( NewsSummaryTableCellRenderer.class ) ;

    /** Background color for the even rows. */
    private static final Color EVEN_ROW_COLOR = new Color( 235, 235, 235 ) ;

    /** Background color for the odd rows. */
    private static final Color ODD_ROW_COLOR = Color.white ;

    /** The time format for rendering the time in the title. */
    public final static SimpleDateFormat TITLE_DATE_TIME_FMT = new SimpleDateFormat( "dd-MMM HH:mm:ss" ) ;

    /**
     * The model whose data this rendering is rendering. A reference to the
     * model is used for inferring cross column data which might affect
     * rendering.
     */
    private NewsSummaryTableModel tableModel = null ;

    /** Public constructor. */
    public NewsSummaryTableCellRenderer( final NewsSummaryTableModel model ) {
        super() ;
        this.tableModel = model ;
    }

    /** Returns the renderer component for the specified table cell. */
    @Override
    public Component getTableCellRendererComponent( final JTable table, final Object value,
            final boolean isSelected, final boolean hasFocus, final int row, final int column ) {

        // Let the super class have the first stab at rendering the cell.
        final JLabel label = ( JLabel )super.getTableCellRendererComponent(
                             table, value, isSelected, hasFocus, row, column ) ;

        final int modelCol = table.convertColumnIndexToModel( column ) ;
        final int modelRow = table.convertRowIndexToModel( row ) ;

        // Do the basic rendering.
        label.setOpaque( true ) ;
        label.setBorder( BorderFactory.createEmptyBorder() ) ;
        label.setHorizontalAlignment( JLabel.LEFT ) ;
        label.setHorizontalTextPosition( JLabel.LEFT ) ;

        if( modelCol == NewsSummaryTableModel.COL_READ ) {
            label.setText( null ) ;
            if( ((Boolean)value).booleanValue() ) {
                label.setIcon( new ColorIcon( Color.GREEN, 10, 10 ) ) ;
            }
            else {
                label.setIcon( new ColorIcon( Color.LIGHT_GRAY, 10, 10 ) ) ;
            }
        }
        else if( modelCol == NewsSummaryTableModel.COL_TIME ) {
            final String dateStr = TITLE_DATE_TIME_FMT.format( ( Date )value ) ;
            label.setText( dateStr ) ;
            setColor( isSelected, row, label, modelRow, modelCol ) ;
        }
        else {
            label.setIcon( null ) ;
            label.setText( value.toString() ) ;
            setColor( isSelected, row, label, modelRow, modelCol ) ;
        }

        return label ;
    }

    /**
     * Sets the background color of the label.
     */
    private void setColor( final boolean isSelected, final int row,
                                     final JLabel label, final int modelRow,
                                     final int modelCol ) {

        // If we are dealing with a selected row, the background is CYAN
        if( isSelected ) {
            label.setBackground( Color.CYAN ) ;
        }
        else {
            // Highlight even and odd rows
            if( row % 2 == 0 ) {
                label.setBackground( EVEN_ROW_COLOR ) ;
            }
            else {
                label.setBackground( ODD_ROW_COLOR ) ;
            }
        }

        // Set the foreground color of the cells.
        final RSSNewsItem newsItem = this.tableModel.getNewsItemAtRow( modelRow ) ;
        final Date startOfToday = STUtils.getStartOfDay( new Date() ) ;
        final Date endOfToday   = STUtils.getEndOfDay( new Date() ) ;

        // If we are dealing with a news item for today, the foreground is
        // blue, else the foreground is dark gray.
        if( newsItem.getPublishDate().after( startOfToday ) &&
            newsItem.getPublishDate().before( endOfToday ) ) {
            label.setForeground( Color.BLUE ) ;
        }
        else {
            label.setForeground( Color.DARK_GRAY ) ;
        }

        if( newsItem.isNewItem() ) {
            label.setFont( label.getFont().deriveFont( Font.BOLD ) ) ;
        }
        else {
            label.setFont( label.getFont().deriveFont( Font.PLAIN ) ) ;
        }
    }
}
