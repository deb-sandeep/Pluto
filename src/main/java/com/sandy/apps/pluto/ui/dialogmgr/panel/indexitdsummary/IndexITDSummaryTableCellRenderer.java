/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 18, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.indexitdsummary;
import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.ui.dialogmgr.panel.scripitdsummary.ScripITDValueCache ;

/**
 * This class acts as the cell renderer for the task summary columns.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class IndexITDSummaryTableCellRenderer extends DefaultTableCellRenderer {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 561684807893431129L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( IndexITDSummaryTableCellRenderer.class ) ;

    /** The decimal format used to render price values. */
    protected static final DecimalFormat PRICE_DF = new DecimalFormat( "###0.00" ) ;

    /** Background color for the even rows. */
    private static final Color EVEN_ROW_COLOR = new Color( 240, 240, 240 ) ;

    /** Background color for the odd rows. */
    private static final Color ODD_ROW_COLOR = Color.white ;

    /** Grades of color for varying levels of profit or loss. */
    private static final Color LOSS_GRADE_0 = new Color( 255, 240, 251 ) ;
    private static final Color LOSS_GRADE_1 = new Color( 255, 210, 242 ) ;
    private static final Color LOSS_GRADE_2 = new Color( 255, 179, 232 ) ;
    private static final Color LOSS_GRADE_3 = new Color( 254, 139, 220 ) ;
    private static final Color LOSS_GRADE_4 = new Color( 254, 84,  203 ) ;
    private static final Color LOSS_GRADE_5 = new Color( 248, 1,   173 ) ;

    private static final Color PROFIT_GRADE_0 = new Color( 236, 255, 239 ) ;
    private static final Color PROFIT_GRADE_1 = new Color( 210, 255, 217 ) ;
    private static final Color PROFIT_GRADE_2 = new Color( 170, 255, 183 ) ;
    private static final Color PROFIT_GRADE_3 = new Color( 119, 255, 139 ) ;
    private static final Color PROFIT_GRADE_4 = new Color( 60,  255, 89  ) ;
    private static final Color PROFIT_GRADE_5 = new Color( 0,   253, 38  ) ;

    /**
     * The model whose data this rendering is rendering. A reference to the
     * model is used for inferring cross column data which might affect
     * rendering.
     */
    private IndexITDSummaryTableModel tableModel = null ;

    /** Public constructor. */
    public IndexITDSummaryTableCellRenderer( final IndexITDSummaryTableModel model ) {
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
        label.setBackground( Color.WHITE ) ;
        label.setBorder( BorderFactory.createEmptyBorder() ) ;

        setLabelText( value, label ) ;
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

        if( modelCol == IndexITDValueCache.COL_PCT_E ||
            modelCol == IndexITDValueCache.COL_PCT_O ) {
            final double val = ( Double )value ;
            label.setBackground( getProfitLossHighlight( val ) ) ;
        }
        else if( modelCol == IndexITDValueCache.COL_INDEX ) {
            final Double pctChange = ( Double )this.tableModel.getValueAt(
                                           modelRow,
                                           IndexITDValueCache.COL_PCT_E ) ;
            label.setBackground( getProfitLossHighlight( pctChange ) ) ;
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
            label.setBackground( EVEN_ROW_COLOR ) ;
        }
        else {
            label.setBackground( ODD_ROW_COLOR ) ;
        }
    }

    /**
     * Sets the alignment of the contents inside the label, based on the
     * column being rendered.
     */
    private void setLabelAlignment( final JLabel label, final int modelCol ) {
        if( modelCol == IndexITDValueCache.COL_INDEX ) {
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
    private void setLabelText( final Object value, final JLabel label ) {
        if( value instanceof Double ) {
            label.setText( PRICE_DF.format( value ) ) ;
        }
        else {
            label.setText( value.toString() ) ;
        }
    }

    /** Returns a color gradation of the percentage change. */
    private Color getProfitLossHighlight( final double pctChange ) {

        Color retVal = Color.white ;

        if( pctChange < 0 ) {

            if( pctChange < -10 ) {
                retVal = LOSS_GRADE_5 ;
            }
            else if( pctChange < -8 ) {
                retVal = LOSS_GRADE_4 ;
            }
            else if( pctChange < -6 ) {
                retVal = LOSS_GRADE_3 ;
            }
            else if( pctChange < -4 ) {
                retVal = LOSS_GRADE_2 ;
            }
            else if( pctChange < -2 ) {
                retVal = LOSS_GRADE_1 ;
            }
            else {
                retVal = LOSS_GRADE_0 ;
            }
        }
        else if( pctChange > 0 ) {

            if( pctChange > 10 ) {
                retVal = PROFIT_GRADE_5 ;
            }
            else if( pctChange > 8 ) {
                retVal = PROFIT_GRADE_4 ;
            }
            else if( pctChange > 6 ) {
                retVal = PROFIT_GRADE_3 ;
            }
            else if( pctChange > 4 ) {
                retVal = PROFIT_GRADE_2 ;
            }
            else if( pctChange > 1 ) {
                retVal = PROFIT_GRADE_1 ;
            }
            else {
                retVal = PROFIT_GRADE_0 ;
            }
        }

        return retVal ;
    }
}
