/**
 * 
 * 
 * 
 *
 * Creation Date: Oct 17, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.scripitdsummary;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTable;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.StockTracker ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.dto.ScripITD ;
import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoFrameType ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoInternalFrame ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.ChartingPanel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartEntityConfig ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartEntity.EntityType ;

/**
 * This is a special extension to the Swing's JTable, providing drag operations
 * on the table rows. In Pluto, the user can drag one or more of the table
 * rows onto the desktop or an existing charting window, causing the dragged
 * scrip to be included in an existing scrip (later) or create a new charting
 * window of it's own (former drag case).
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ScripITDSummaryTable extends JTable implements DragGestureListener {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ScripITDSummaryTable.class ) ;

    /**
     * This anonymous internal class implements the logic of handling double
     * clicks on any row of the table. When a user double clicks on any row,
     * it is a que for us to open up a chart of that scrip. This is a convenient
     * way for the user to investigate any particular scrip instead of using
     * the normal drag and drop feature or using the chart button.
     */
    private final MouseListener mouseListener = new MouseAdapter() {

        /**
         * Traps the mouse click events and triggers a graph once a double click
         * is detected.
         */
        @Override
        public void mouseClicked( final MouseEvent e ) {

            List<ChartEntityConfig> cfgList = null ;
            ChartEntityConfig       cfg     = null ;

            if( e.getClickCount() == 2 ) {
                final int row = ScripITDSummaryTable.this.rowAtPoint( e.getPoint() ) ;
                if( row != -1 ) {
                    final String scripName = null ;
                    try {
                        // Convert the row Id to model specific row identifier.
                        final int modelRowId = convertRowIndexToModel( row ) ;
                        final ScripITDSummaryTableModel model = ( ScripITDSummaryTableModel )getModel() ;
                        final ScripITD scripITD = model.getScripITDForRow( modelRowId ) ;

                        final ChartingPanel panel  = new ChartingPanel() ;
                        final PlutoInternalFrame dialog = new PlutoInternalFrame( "Charting",
                                                  JFrame.DISPOSE_ON_CLOSE,
                                                  PlutoFrameType.CHART_FRAME, panel ) ;

                        dialog.setSize( ChartingPanel.DEFAULT_WIDTH,
                                        ChartingPanel.DEFAULT_HEIGHT ) ;
                        dialog.setResizable( true ) ;

                        cfgList = new ArrayList<ChartEntityConfig>() ;
                        cfg = new ChartEntityConfig( scripITD.getSymbolId(),
                                           EntityType.SCRIP, scripITD.getTime() ) ;
                        cfgList.add( cfg ) ;
                        panel.addChartEntities( cfgList ) ;

                        StockTracker.MAIN_FRAME.addInternalFrame( dialog, true ) ;
                    }
                    catch ( final STException ste ) {
                        LogMsg.error( "Error opening graph for " + scripName ) ;
                    }
                }
            }
        }
    } ;

    /** Public no argument constructor. */
    public ScripITDSummaryTable() {
        super() ;
        super.addMouseListener( this.mouseListener ) ;
    }

    /**
     * This function is invoked when the user tries to initiate a drag operation
     * from within the table.
     *
     * @param dge The drag gesture event, which encapsulates information
     *        regarding the start of drag.
     */
    @Override
    public void dragGestureRecognized( final DragGestureEvent dge ) {

        // Get the selected row index in the table.
        final int rowId = getSelectedRow() ;
        if( rowId == -1 ) {
            // If there are no rows selected, there is nothing to drag. Ignore.
            return ;
        }

        // Convert the row Id to model specific row identifier.
        final int modelRowId = convertRowIndexToModel( rowId ) ;

        final ScripITDSummaryTableModel model = ( ScripITDSummaryTableModel )getModel() ;
        final ScripITD scripITD = model.getScripITDForRow( modelRowId ) ;
        final ChartEntityConfig cfg = new ChartEntityConfig(
                scripITD.getSymbolId(), EntityType.SCRIP, scripITD.getTime() ) ;

        // Create the transferable. The means of data transfer is much cleaner
        // than the temptation to use local variables for tracking the data.
        final Transferable trans = new Transferable() {

            public Object getTransferData( final DataFlavor flavor )
                throws UnsupportedFlavorException, IOException {
                return cfg ;
            }

            public DataFlavor[] getTransferDataFlavors() {
                final DataFlavor[] flavors = new DataFlavor[1] ;
                flavors[0] = UIConstant.CHART_ENTITY_DATA_FLAVOR ;
                return flavors ;
            }

            public boolean isDataFlavorSupported( final DataFlavor flavor ) {
                final String accMimeType = DataFlavor.stringFlavor.getMimeType() ;
                if( flavor.getMimeType().equals( accMimeType ) ) {
                    return true ;
                }
                return false ;
            }
        } ;

        // Start dragging our transferable color object
        dge.startDrag( DragSource.DefaultMoveDrop, trans, new DragSourceAdapter(){} ) ;
    }
}
