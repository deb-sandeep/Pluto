/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 18, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.scripeodsummary;
import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.dto.ScripEOD ;
import com.sandy.apps.pluto.shared.util.util.StringUtil ;
import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.UIHelper ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.scripitdsummary.ScripITDValueCache ;

/**
 * This class acts as the cell renderer for the task summary columns.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ScripEODSummaryTableCellRenderer extends DefaultTableCellRenderer {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 561684807893431129L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ScripEODSummaryTableCellRenderer.class ) ;

    /** The decimal format used to render price values. */
    protected static final DecimalFormat PRICE_DF = new DecimalFormat( "###0.00" ) ;

    /**
     * The model whose data this rendering is rendering. A reference to the
     * model is used for inferring cross column data which might affect
     * rendering.
     */
    private ScripEODSummaryTableModel tableModel = null ;

    /** Public constructor. */
    public ScripEODSummaryTableCellRenderer( final ScripEODSummaryTableModel model ) {
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
        if( modelCol == ScripEODSummaryTableModel.COL_PCT_CHG_HIST ) {
            return new LastNPctChangeLabel( ( Double[] )value ) ;
        }

        // Let the super class have the first stab at rendering the cell.
        final JLabel label = ( JLabel )super.getTableCellRendererComponent(
                             table, value, isSelected, hasFocus, row, column ) ;

        // Do the basic rendering.
        label.setText( "" ) ;
        label.setOpaque( true ) ;
        label.setBackground( Color.WHITE ) ;
        label.setBorder( BorderFactory.createEmptyBorder() ) ;

        setBackgroundColor( isSelected, row, label, modelCol ) ;
        if( modelCol == ScripEODSummaryTableModel.COL_INDEX_LINKED ) {
            final Boolean b = ( Boolean )value ;
            if( b!= null && b.booleanValue() == true ) {
                label.setIcon( new ImageIcon( UIConstant.IMG_INDEX_LINKED ) ) ;
            }
            else {
                label.setIcon( null ) ;
            }
        }
        else {
            setLabelText( value, label, modelRow, modelCol ) ;
            setLabelAlignment( label, modelCol ) ;
            setColorGradation( value, label, modelCol, modelRow ) ;
        }

        return label ;
    }

    /**
     * Depending upon the percentage change, sets the color gradation of the row.
     */
    private void setColorGradation( final Object value, final JLabel label,
                                    final int modelCol, final int modelRow ) {

        if( modelCol == ScripEODSummaryTableModel.COL_PCT_E ||
            modelCol == ScripEODSummaryTableModel.COL_PCT_O ) {
            final double val = ( Double )value ;
            label.setBackground( UIHelper.getProfitLossHighlight( val ) ) ;
        }
        else if( modelCol == ScripEODSummaryTableModel.COL_SYMBOL ) {
            final Double pctChange = ( Double )this.tableModel.getValueAt(
                                modelRow, ScripEODSummaryTableModel.COL_PCT_E ) ;
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
        if( modelCol == ScripEODSummaryTableModel.COL_ICICI ||
            modelCol == ScripEODSummaryTableModel.COL_SYMBOL ||
            modelCol == ScripEODSummaryTableModel.COL_NAME ) {
            label.setHorizontalAlignment( JLabel.LEFT ) ;
            label.setHorizontalTextPosition( JLabel.LEFT ) ;
        }
        else {
            label.setHorizontalAlignment( JLabel.RIGHT ) ;
            label.setHorizontalTextPosition( JLabel.RIGHT ) ;
        }
    }

    /**
     * Sets the text of the label.
     */
    private void setLabelText( final Object value, final JLabel label,
                               final int modelRow, final int modelCol ) {

        if( value instanceof Double ) {
            label.setText( PRICE_DF.format( value ) ) ;
        }
        else {
            if( value != null ) {
                if( modelCol == ScripEODSummaryTableModel.COL_SYMBOL ) {
                    final ScripEOD eod = this.tableModel.getScripEODForRow( modelRow ) ;
                    final String name  = eod.getSymbol().getDescription() ;
                    final String icici = eod.getSymbol().getIciciCode() ;

                    if( StringUtil.isNotEmptyOrNull( name ) ) {
                        String tooltip = null ;
                        tooltip = "[" + icici + "]" ;
                        tooltip+= " " + name ;
                        label.setToolTipText( tooltip ) ;
                    }
                }

                if( modelCol == ScripEODSummaryTableModel.COL_ICICI  ) {
                    label.setText( " " + value.toString() ) ;
                }
                else {
                    label.setText( value.toString() ) ;
                }
            }
            else {
                label.setText( "" ) ;
            }
        }
    }
}
