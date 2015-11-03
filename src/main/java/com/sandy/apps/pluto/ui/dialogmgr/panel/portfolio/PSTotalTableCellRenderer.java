/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 18, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.portfolio;
import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;

/**
 * This class acts as the cell renderer for the portfolio summary total columns.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class PSTotalTableCellRenderer extends DefaultTableCellRenderer {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 561684807893431129L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( PSTotalTableCellRenderer.class ) ;

    /** The decimal format used to render price values. */
    protected static final DecimalFormat PRICE_DF = new DecimalFormat( "###0.00" ) ;

    /** Public constructor. */
    public PSTotalTableCellRenderer() {
        super() ;
    }

    /** Returns the renderer component for the specified table cell. */
    @Override
    public Component getTableCellRendererComponent( final JTable table, final Object value,
            final boolean isSelected, final boolean hasFocus, final int row, final int column ) {

        final int modelCol = table.convertColumnIndexToModel( column ) ;
        final int modelRow = table.convertRowIndexToModel( row ) ;

        // Let the super class have the first stab at rendering the cell.
        final JLabel label = ( JLabel )super.getTableCellRendererComponent(
                             table, value, isSelected, hasFocus, row, column ) ;

        // Do the basic rendering.
        label.setOpaque( true ) ;
        label.setToolTipText( null ) ;
        label.setBackground( Color.WHITE ) ;
        label.setBackground( Color.DARK_GRAY ) ;
        label.setBorder( BorderFactory.createEmptyBorder() ) ;

        setLabelText( value, label, modelRow, modelCol ) ;
        setLabelAlignment( label, modelCol ) ;
        setBackgroundColor( isSelected, row, label, modelCol ) ;
        setColorGradation( value, label, modelCol, modelRow ) ;

        return label ;
    }

    /**
     * Depending upon the percentage change, sets the color gradation of the row.
     */
    private void setColorGradation( final Object value, final JLabel label,
                                    final int modelCol, final int modelRow ) {
    }

    /**
     * Sets the background color of the label.
     */
    private void setBackgroundColor( final boolean isSelected, final int row,
                                     final JLabel label, final int modelCol ) {
        label.setBackground( Color.BLACK ) ;
    }

    /**
     * Sets the alignment of the contents inside the label, based on the
     * column being rendered.
     */
    private void setLabelAlignment( final JLabel label, final int modelCol ) {
        if( modelCol == PSTableModel.COL_SYMBOL ) {
            label.setHorizontalAlignment( JLabel.LEFT ) ;
            label.setHorizontalTextPosition( JLabel.LEFT ) ;
        }
        else {
            label.setHorizontalAlignment( JLabel.RIGHT ) ;
            label.setHorizontalTextPosition( JLabel.RIGHT ) ;
        }
    }

    /**
     * Sets the text (display and tooltip) of the label.
     */
    private void setLabelText( final Object value, final JLabel label,
                               final int modelRow, final int modelCol ) {
        if( modelCol == PSTableModel.COL_LTP ||
            modelCol == PSTableModel.COL_PRICE ||
            modelCol == PSTableModel.COL_QTY ) {
            label.setText( "" ) ;
        }
        else if( value instanceof Double ) {
            if( modelCol == PSTableModel.COL_REALIZED ||
                modelCol == PSTableModel.COL_UNREALIZED ||
                modelCol == PSTableModel.COL_UNREALIZED_PCT ) {

                final double val = ( Double )value ;
                if( val >= 0 ) {
                    label.setForeground( Color.GREEN.brighter() ) ;
                }
                else {
                    label.setForeground( Color.RED.brighter() ) ;
                }
            }
            else {
                label.setForeground( Color.WHITE ) ;
            }

            label.setText( PRICE_DF.format( value ) ) ;
            if( modelCol == PSTableModel.COL_UNREALIZED_PCT ) {
                label.setText( label.getText() + " %" ) ;
            }
        }
        else {
            label.setText( value.toString() ) ;
        }
    }
}
