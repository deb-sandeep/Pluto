/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 15, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.indexitdsummary;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.StockTracker ;
import com.sandy.apps.pluto.biz.svc.IExIndexSvc ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.ExIndexITD ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.dialogmgr.AbstractPlutoFramePanel ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoFrameType ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoInternalFrame ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.ChartingPanel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartEntityConfig ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartEntity.EntityType ;

/**
 * This class is the concrete implementation of the self refreshing ITD summary
 * panel. There can be multiple instances of this panel, each showing one or
 * more Scrips whose ITD data is being updated dynamically. The user can also
 * choose to add/remove the scrips that each panel can show.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class IndexITDSummaryPanel extends AbstractPlutoFramePanel
    implements UIConstant, ActionListener, TableModelListener {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 561684807893431129L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( IndexITDSummaryPanel.class ) ;

    /** The table which will display the task summary. */
    private final IndexITDSummaryTable table = new IndexITDSummaryTable() ;

    /** The model used for the job summary table. */
    private final IndexITDSummaryTableModel tableModel = new IndexITDSummaryTableModel() ;

    /** The table row sorter to be used to filter the rows of the table. */
    private final TableRowSorter<IndexITDSummaryTableModel> sorter =
                   new TableRowSorter<IndexITDSummaryTableModel>( this.tableModel ) ;

    /** The time format for rendering the time in the title. */
    final static SimpleDateFormat TITLE_DATE_TIME_FMT = new SimpleDateFormat( "dd-MMM HH:mm:ss" ) ;

    /** Public constructor. */
    public IndexITDSummaryPanel( final String name ) {
        super( name ) ;
    }

    /**
     * Returns the icons that need to be displayed in the wizard toolbar when
     * this panel is selected.
     * <ul>
     *  <li>Element 0 [String][M] - Action command for the button</li>
     *  <li>Element 1 [Image] [M] - Button image</li>
     *  <li>Element 2 [Image] [O] - Pressed image for button</li>
     *  <li>Element 3 [Image] [O] - Roll over description for button[</li>
     * </ul>
     * <p>
     *
     * @return A two dimensional array of Objects, with each row having two
     *         elements.
     */
    public Object[][] getPanelIcons() {
        return new Object[][] {
            { AC_SHOW_CHART,     IMG_SHOW_CHART,     IMG_SHOW_CHART_PRESSED,     "Plot" },
            { AC_FETCH,          IMG_FETCH,          IMG_FETCH_PRESSED,          "Fetch ITD data" }
        } ;
    }

    /**
     * This method is invoked when the user selects any of the panel specific
     * buttons. Depending upon the action initiated by the user, this method
     * delegates the processing to the appropriate method.
     */
    @Override
    public void actionPerformed( final ActionEvent e ) {
        final String actCmd = e.getActionCommand() ;
        if( actCmd.equals( AC_SHOW_CHART ) ) {
            plotChart() ;
        }
        else if( actCmd.equals( AC_FETCH ) ) {
            fetchHiResITDData() ;
        }
    }

    /** Fetches hi resolution ITD data for selected scrips. */
    private void fetchHiResITDData() {
        final int selRows[] = this.table.getSelectedRows() ;
        if( selRows.length > 0 ) {

            final IExIndexSvc svc = ServiceMgr.getAsyncExIndexSvc() ;
            for( int i=0; i<selRows.length; i++ ) {
                final int modelRow     = this.table.convertRowIndexToModel( selRows[i] ) ;
                final ExIndexITD index = this.tableModel.getIndexITDForRow( modelRow ) ;
                final String name      = index.getIndex() ;

                LogMsg.info( "Fetching high resolution ITD data for index " + name ) ;
                try {
                    svc.importHiResITDIndices( name, null ) ;
                }
                catch ( final Exception e ) {
                    final String errMsg = "Error fetching high resolution ITD " +
                                          "data for index " + name ;
                    LogMsg.error( errMsg ) ;
                    logger.debug( errMsg, e ) ;
                }
            }
        }
    }

    /** A helper method to open a chart for the selected symbols. */
    private void plotChart() {
        try {
            final int selRows[] = this.table.getSelectedRows() ;
            if( selRows.length > 0 ) {

                final ChartingPanel panel  = new ChartingPanel() ;
                final PlutoInternalFrame dialog = new PlutoInternalFrame( "Charting",
                                                    JFrame.DISPOSE_ON_CLOSE,
                                                    PlutoFrameType.CHART_FRAME,
                                                    panel ) ;

                dialog.setSize( ChartingPanel.DEFAULT_WIDTH, ChartingPanel.DEFAULT_HEIGHT ) ;
                dialog.setResizable( true ) ;

                final List<ChartEntityConfig> cfgList = new ArrayList<ChartEntityConfig>() ;
                ChartEntityConfig cfg = null ;

                for( int i=0; i<selRows.length; i++ ) {
                    final int modelRow   = this.table.convertRowIndexToModel( selRows[i] ) ;
                    final ExIndexITD index = this.tableModel.getIndexITDForRow( modelRow ) ;

                    cfg = new ChartEntityConfig( index.getIndex(), EntityType.INDEX, index.getDate() ) ;
                    cfgList.add( cfg ) ;
                }
                panel.addChartEntities( cfgList ) ;

                StockTracker.MAIN_FRAME.addInternalFrame( dialog, true ) ;
            }
        }
        catch ( final STException e ) {
            logger.error( "Unable to open chart", e ) ;
        }
    }

    /**
     * Initializes the nature of this panel, including setting up appropriate
     * listeners, controller and model of the charting engine.
     */
    @Override
    public void initializeData() throws STException {

        // Set up the table.
        final JScrollPane tableSP = new JScrollPane( this.table ) ;
        tableSP.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ) ;

        // Add the table model, table column model etc for the table.
        this.table.setModel( this.tableModel ) ;
        this.table.setDefaultRenderer( String.class, new IndexITDSummaryTableCellRenderer( this.tableModel ) ) ;
        this.table.setDefaultRenderer( Double.class, new IndexITDSummaryTableCellRenderer( this.tableModel ) ) ;
        this.table.setDefaultRenderer( Integer.class,new IndexITDSummaryTableCellRenderer( this.tableModel ) ) ;
        this.table.setAutoCreateRowSorter( true ) ;
        this.table.setGridColor( new Color( 243, 243, 243 ) ) ;
        this.table.setFont( LOG_FONT ) ;
        this.table.getTableHeader().setFont( LOG_FONT_BOLD ) ;
        this.table.setRowHeight( 15 ) ;
        this.table.setDoubleBuffered( true ) ;
        this.table.setRowSorter( this.sorter ) ;
        this.table.setRowSelectionAllowed( true ) ;
        this.table.setColumnSelectionAllowed( false ) ;
        this.table.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION ) ;

        // Set the table as a drag source
        // Set up a DragGestureRecognizer that will detect when the user
        // begins a drag.  When it detects one, it will notify us by calling
        // the dragGestureRecognized() method of the DragGestureListener
        // interface we implement below
        final DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer( this.table,
                      DnDConstants.ACTION_COPY_OR_MOVE, this.table );

        // Add this panel as a table data change listener. This will enable
        // us to change the panel title value as and when new data comes in
        this.tableModel.addTableModelListener( this ) ;
        if( this.tableModel.getLastRefreshTime() != null ) {
            final Date refreshTime = this.tableModel.getLastRefreshTime() ;
            final String newTitle  = " " + getName() + " at " +
                                     TITLE_DATE_TIME_FMT.format( refreshTime ) ;
            super.getDialogManager().setPanelTitle( this, newTitle ) ;
        }

        // Set the initial sort filter to null. This will cause all the
        // rows in the table to be visible
        this.sorter.setRowFilter( null ) ;

        setColumnProperties( IndexITDValueCache.COL_INDEX,       110 ) ;
        setColumnProperties( IndexITDValueCache.COL_VALUE,       50  ) ;
        setColumnProperties( IndexITDValueCache.COL_CHANGE,      40  ) ;
        setColumnProperties( IndexITDValueCache.COL_PCT_E,       40  ) ;
        setColumnProperties( IndexITDValueCache.COL_PCT_O,       40  ) ;

        setLayout( new BorderLayout() ) ;
        add( tableSP, BorderLayout.CENTER ) ;

        // The table model is a subscriber to the TID value cache
        IndexITDValueCache.getInstance().addITDValueCacheListener( this.tableModel ) ;
    }

    /**
     * A tiny helper method to set the properties of the columns in the ITD
     * table.
     *
     * @param colId The identifier of the column
     * @param width The preferred width
     */
    private void setColumnProperties( final int colId, final int width ) {
        final TableColumnModel colModel = this.table.getColumnModel() ;
        final TableColumn col = colModel.getColumn( colId ) ;
        col.setPreferredWidth( width ) ;
        col.setMinWidth( width ) ;
        col.setResizable( true ) ;
    }

    /**
     * This method is invoked on the child panels when the dialog is being
     * disposed as a result of a cancel operation chosen by the user. Subclasses
     * can implement this function to perform cleanup logic like de-registering
     * from the event bus etc.
     */
    public void destroy() {
        // The table model is a subscriber to the TID value cache
        IndexITDValueCache.getInstance().removeITDValueCacheListener( this.tableModel ) ;
    }

    /**
     * This method is called when the data in the table changes. This is an
     * opportunity for us to refresh time in the title bar.
     */
    @Override
    public void tableChanged( final TableModelEvent e ) {
        final Date refreshTime = this.tableModel.getLastRefreshTime() ;
        final String newTitle  = " " + getName() + " at " +
                                 TITLE_DATE_TIME_FMT.format( refreshTime ) ;
        super.getDialogManager().setPanelTitle( this, newTitle ) ;
    }
}
