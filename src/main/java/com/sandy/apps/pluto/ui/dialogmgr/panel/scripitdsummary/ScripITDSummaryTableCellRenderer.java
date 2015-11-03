/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 18, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.scripitdsummary;
import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.dto.Symbol ;
import com.sandy.apps.pluto.ui.UIHelper ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.scripeodsummary.LastNPctChangeLabel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.scripeodsummary.ScripEODValueCache ;

/**
 * This class acts as the cell renderer for the task summary columns.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ScripITDSummaryTableCellRenderer extends DefaultTableCellRenderer {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 561684807893431129L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ScripITDSummaryTableCellRenderer.class ) ;

    /** The decimal format used to render price values. */
    protected static final DecimalFormat PRICE_DF = new DecimalFormat( "###0.00" ) ;

    /**
     * The model whose data this rendering is rendering. A reference to the
     * model is used for inferring cross column data which might affect
     * rendering.
     */
    private ScripITDSummaryTableModel tableModel = null ;

    /**
     * A reference to the ScripEODValueCache which will hold the symbol definition
     * for all the registered symbols.
     */
    private final ScripEODValueCache eodCache = ScripEODValueCache.getInstance() ;

    /** Public constructor. */
    public ScripITDSummaryTableCellRenderer( final ScripITDSummaryTableModel model ) {
        super() ;
        this.tableModel = model ;
    }

    /** Returns the renderer component for the specified table cell. */
    @Override
    public Component getTableCellRendererComponent( final JTable table, final Object value,
            final boolean isSelected, final boolean hasFocus, final int row, final int column ) {

        final int modelCol = table.convertColumnIndexToModel( column ) ;
        final int modelRow = table.convertRowIndexToModel( row ) ;

        // If we are dealing with the last N pct change history column,
        // there is no need to delegate the call to the super class.
        if( modelCol == ScripITDValueCache.COL_PCT_CHG_HIST ) {
            return new LastNPctChangeLabel( ( Double[] )value ) ;
        }

        // Let the super class have the first stab at rendering the cell.
        final JLabel label = ( JLabel )super.getTableCellRendererComponent(
                             table, value, isSelected, hasFocus, row, column ) ;

        // Do the basic rendering.
        label.setOpaque( true ) ;
        label.setToolTipText( null ) ;
        label.setBackground( Color.WHITE ) ;
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

        if( modelCol == ScripITDValueCache.COL_PCT_E ||
            modelCol == ScripITDValueCache.COL_PCT_O ) {
            final double val = ( Double )value ;
            label.setBackground( UIHelper.getProfitLossHighlight( val ) ) ;
        }
        else if( modelCol == ScripITDValueCache.COL_SCRIP ) {
            final Double pctChange = ( Double )this.tableModel.getValueAt(
                                           modelRow,
                                           ScripITDValueCache.COL_PCT_E ) ;
            label.setBackground( UIHelper.getProfitLossHighlight( pctChange ) ) ;
        }
    }

    /**
     * Sets the background color of the label.
     */
    private void setBackgroundColor( final boolean isSelected, final int row,
                                     final JLabel label, final int modelCol ) {

        if( isSelected ) {
            label.setBackground( Color.CYAN ) ;
            if( modelCol == ScripITDValueCache.COL_SCRIP ) {
                label.setBorder( BorderFactory.createLoweredBevelBorder() ) ;
            }
        }
        else if( row % 2 == 0 ) {
            label.setBackground( UIHelper.EVEN_ROW_COLOR ) ;
        }
        else {
            label.setBackground( UIHelper.ODD_ROW_COLOR ) ;
        }
    }

    /**
     * Sets the alignment of the contents inside the label, based on the
     * column being rendered.
     */
    private void setLabelAlignment( final JLabel label, final int modelCol ) {
        if( modelCol == ScripITDValueCache.COL_SCRIP ) {
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
        if( value instanceof Double ) {
            label.setText( PRICE_DF.format( value ) ) ;
        }
        else {
            if( modelCol == ScripITDValueCache.COL_SCRIP ) {
                final String symbId = value.toString().trim() ;
                final Symbol symbol = this.eodCache.getSymbol( symbId ) ;

                if( symbol != null ) {
                    String tooltip = null ;
                    tooltip = "[" + symbol.getIciciCode() + "]" ;
                    tooltip+= " " + symbol.getDescription() ;
                    label.setToolTipText( tooltip ) ;
                }
            }
            label.setText( value.toString() ) ;
        }
    }
}
