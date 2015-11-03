/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 15, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.scripeodsummary ;

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

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.StockTracker ;
import com.sandy.apps.pluto.biz.svc.IITDImportSvc ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.dto.ScripEOD ;
import com.sandy.apps.pluto.shared.util.util.StringUtil ;
import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.UIHelper ;
import com.sandy.apps.pluto.ui.dialogmgr.AbstractPlutoFramePanel ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoFrameType ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoInternalFrame ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.ChartingPanel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartEntityConfig ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartEntity.EntityType ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.scripeodsummary.filter.ScripEODFilterQueryParser ;
import com.sandy.apps.pluto.ui.svc.STViewService ;

/**
 * This class is the concrete implementation of the Scrip EOD summary panel.
 * There can be multiple instances of this panel, each showing one or more
 * Scrips showing the EOD data for the chosen date. This panel also enables
 * the user to view the details of a scrip, open up charts for one or more
 * selected scrips, etc. This panel can be thought of as being similar to the
 * Scrip ITD panel, except that the data for this panel does not track
 * intra day data and shows all the scrips which ITD panel does not. ITD panel
 * shows only the scrips which are index linked or for which ITD tracking is
 * enabled separately.
 * <p>
 * This panel also supports a search text field where the user can enter
 * custom search queries to filter the data set.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ScripEODSummaryPanel extends AbstractPlutoFramePanel
    implements UIConstant, ActionListener, TableModelListener {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 561684807893431129L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ScripEODSummaryPanel.class ) ;

    /** The text box for entering quick filter criteria. */
    private final JComboBox searchTF = new JComboBox() ;

    /** The model used for the job summary table. */
    private final ScripEODSummaryTableModel tableModel = new ScripEODSummaryTableModel() ;

    /** A panel which renders the profit/loss spectrum of the market. */
    private final ScripEODSpectrumPanel spectrumPanel = new ScripEODSpectrumPanel( this.tableModel ) ;

    /** The table which will display the task summary. */
    private final ScripEODSummaryTable table = new ScripEODSummaryTable() ;

    /** The table row sorter to be used to filter the rows of the table. */
    private final TableRowSorter<ScripEODSummaryTableModel> sorter =
                   new TableRowSorter<ScripEODSummaryTableModel>( this.tableModel ) ;

    /** The time format for rendering the time in the title. */
    final static SimpleDateFormat TITLE_DATE_FMT = new SimpleDateFormat( "dd-MMM-yy" ) ;

    /** The number of days for which the previos pct EOD change is shown. */
    public static final int LAST_N_PCT_CHANGE_DAYS = 10 ;

    /** Public constructor. */
    public ScripEODSummaryPanel( final String name ) {
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
            { AC_FETCH,          IMG_FETCH,          IMG_FETCH_PRESSED,          "Fetch ITD data" },
            { AC_TRADE_ADD,      IMG_TRADE_ADD,      IMG_TRADE_ADD_PRESSED,      "Add Trade" }
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
        else if( actCmd.equals( AC_TRADE_ADD ) ) {
            showTradeEntryDialog() ;
        }
        else if( actCmd.equals( AC_ITD_SEARCH_ENTRY ) ) {
            final String queryStr = this.searchTF.getSelectedItem().toString() ;
            if( StringUtil.isNotEmptyOrNull( queryStr ) ) {

                final ScripEODFilterQueryParser parser = new ScripEODFilterQueryParser( queryStr ) ;
                try {
                    final RowFilter<Object, Object> filter = parser.parse() ;
                    this.sorter.setRowFilter( filter ) ;
                    this.searchTF.setBackground( Color.white ) ;
                    this.searchTF.setToolTipText( "Enter filter query and enter" ) ;
                    this.searchTF.insertItemAt( queryStr, 0 ) ;
                }
                catch ( final Exception e2 ) {
                    this.searchTF.setBackground( Color.pink ) ;
                    this.searchTF.setToolTipText( "ERROR: " + e2.getMessage() ) ;
                }
            }
            else {
                this.sorter.setRowFilter( null ) ;
                this.searchTF.setBackground( Color.white ) ;
                this.searchTF.setToolTipText( "Enter filter query and enter" ) ;
            }
        }
    }

    /**
     * Opens up the trade entry dialog for the currently selected stock. If
     * rows are not selected, an unfilled trade dialog is popped up.
     */
    private void showTradeEntryDialog() {
        try {
            final int           selRow  = this.table.getSelectedRow() ;
            final STViewService viewSvc = ServiceMgr.getSTViewService() ;

            if( selRow != -1 ) {
                final int modelRow   = this.table.convertRowIndexToModel( selRow ) ;
                final ScripEOD scrip = this.tableModel.getScripEODForRow( modelRow ) ;

                viewSvc.showEquityBuySellDialog( scrip.getSymbolId(), true ) ;
            }
            else {
                viewSvc.showEquityBuySellDialog( null, true ) ;
            }
        }
        catch ( final STException bsEx ) {
            final String errMsg = "Failure showing dialog. msg = " + bsEx.getMessage() ;
            LogMsg.error( errMsg ) ;
            logger.error( errMsg, bsEx ) ;
        }
    }

    /** Fetches hi resolution ITD data for selected scrips. */
    private void fetchHiResITDData() {
        final int selRows[] = this.table.getSelectedRows() ;
        if( selRows.length > 0 ) {

            final IITDImportSvc svc = ServiceMgr.getAsyncITDImportSvc() ;
            for( int i=0; i<selRows.length; i++ ) {
                final int modelRow   = this.table.convertRowIndexToModel( selRows[i] ) ;
                final ScripEOD scrip = this.tableModel.getScripEODForRow( modelRow ) ;
                final String   symbol= scrip.getSymbolId() ;

                LogMsg.info( "Fetching high resolution ITD data for scrip " + symbol ) ;
                try {
                    svc.importHighResNSESymbol( symbol, true ) ;
                }
                catch ( final Exception e ) {
                    final String errMsg = "Error fetching high resolution ITD " +
                                          "data for scrip " + symbol ;
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
                                                      PlutoFrameType.CHART_FRAME, panel ) ;

                dialog.setSize( ChartingPanel.DEFAULT_WIDTH, ChartingPanel.DEFAULT_HEIGHT ) ;
                dialog.setResizable( true ) ;

                final List<ChartEntityConfig> cfgList = new ArrayList<ChartEntityConfig>() ;
                ChartEntityConfig cfg = null ;

                for( int i=0; i<selRows.length; i++ ) {
                    final int modelRow   = this.table.convertRowIndexToModel( selRows[i] ) ;
                    final ScripEOD scrip = this.tableModel.getScripEODForRow( modelRow ) ;

                    cfg = new ChartEntityConfig( scrip.getSymbolId(),
                                           EntityType.SCRIP, scrip.getDate() ) ;
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

        // Register the table model with the cache so that it can pick up
        // model change notifications.
        ScripEODValueCache.getInstance().addEODValueCacheListener( this.tableModel ) ;

        // Add the table model, table column model etc for the table.
        this.table.setModel( this.tableModel ) ;
        this.table.setDefaultRenderer( String.class,    new ScripEODSummaryTableCellRenderer( this.tableModel ) ) ;
        this.table.setDefaultRenderer( Double.class,    new ScripEODSummaryTableCellRenderer( this.tableModel ) ) ;
        this.table.setDefaultRenderer( Long.class,      new ScripEODSummaryTableCellRenderer( this.tableModel ) ) ;
        this.table.setDefaultRenderer( Boolean.class,   new ScripEODSummaryTableCellRenderer( this.tableModel ) ) ;
        this.table.setDefaultRenderer( Double[].class,  new ScripEODSummaryTableCellRenderer( this.tableModel ) ) ;
        this.table.setAutoCreateRowSorter( true ) ;
        this.table.setGridColor( UIHelper.GRID_COLOR ) ;
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
        if( this.tableModel.getDate() != null ) {
            final Date refreshTime = this.tableModel.getDate() ;
            final String newTitle  = " " + getName() + " @ " +
                                     TITLE_DATE_FMT.format( refreshTime ) ;
            super.getDialogManager().setPanelTitle( this, newTitle ) ;
        }

        // Set the initial sort filter to null. This will cause all the
        // rows in the table to be visible
        this.sorter.setRowFilter( null ) ;

        setColumnProperties( ScripEODSummaryTableModel.COL_INDEX_LINKED,15 ) ;
        setColumnProperties( ScripEODSummaryTableModel.COL_PCT_CHG_HIST,70 ) ;
        setColumnProperties( ScripEODSummaryTableModel.COL_SYMBOL,     100 ) ;
        setColumnProperties( ScripEODSummaryTableModel.COL_PCT_E,       40 ) ;
        setColumnProperties( ScripEODSummaryTableModel.COL_PCT_O,       40 ) ;
        setColumnProperties( ScripEODSummaryTableModel.COL_PRICE,       40 ) ;
        setColumnProperties( ScripEODSummaryTableModel.COL_QTY,         50 ) ;
        setColumnProperties( ScripEODSummaryTableModel.COL_ICICI,       60 ) ;
        setColumnProperties( ScripEODSummaryTableModel.COL_NAME,       150 ) ;

        // Set up the search entry text box and add it to the panel
        this.searchTF.setFont( LOG_FONT ) ;
        this.searchTF.addActionListener( this ) ;
        this.searchTF.setActionCommand( AC_ITD_SEARCH_ENTRY ) ;
        this.searchTF.setEditable( true ) ;

        final JPanel panel = new JPanel() ;
        panel.setLayout( new BorderLayout() ) ;
        panel.add( new JLabel( UIHelper.getIcon( "search.png" ) ), BorderLayout.WEST ) ;
        panel.add( this.searchTF, BorderLayout.CENTER ) ;
        panel.add( this.spectrumPanel, BorderLayout.SOUTH ) ;

        setLayout( new BorderLayout() ) ;
        add( tableSP, BorderLayout.CENTER ) ;
        add( panel, BorderLayout.NORTH ) ;
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
        // De-register the table model with the cache so that it can pick up
        // model change notifications.
        ScripEODValueCache.getInstance().removeEODValueCacheListener( this.tableModel ) ;
    }

    /**
     * This method is called when the data in the table changes. This is an
     * opportunity for us to refresh time in the title bar.
     */
    @Override
    public void tableChanged( final TableModelEvent e ) {
        final Date refreshTime = this.tableModel.getDate() ;
        final String newTitle  = " " + getName() + " @ " +
                                 TITLE_DATE_FMT.format( refreshTime ) ;
        super.getDialogManager().setPanelTitle( this, newTitle ) ;
        this.spectrumPanel.repaint() ;
    }

    /** No operation method. */
    public String getTitle() {

        String titleStr = "Scrip EOD" ;
        final Object selectedItem = this.searchTF.getSelectedItem() ;
        if( selectedItem != null ) {
            titleStr = selectedItem.toString() ;
            if( StringUtil.isNotEmptyOrNull( titleStr ) ) {
                titleStr = "EOD [" + titleStr + "]" ;
            }
            else {
                titleStr = "EOD" ;
            }
        }

        return titleStr ;
    }

    /**
     * Returns the preferred width of this panel.
     */
    public int getPreferredWidth() {
        int preferredWidth = 0 ;
        final int dispColId[] = {
                ScripEODSummaryTableModel.COL_INDEX_LINKED,
                ScripEODSummaryTableModel.COL_PCT_CHG_HIST,
                ScripEODSummaryTableModel.COL_SYMBOL,
                ScripEODSummaryTableModel.COL_PCT_E,
                ScripEODSummaryTableModel.COL_PCT_O,
                ScripEODSummaryTableModel.COL_PRICE,
                ScripEODSummaryTableModel.COL_QTY
        } ;

        final TableColumnModel colModel = this.table.getColumnModel() ;
        for( int colId=0; colId<dispColId.length; colId++ ) {
            final TableColumn col = colModel.getColumn( colId ) ;
            preferredWidth += col.getPreferredWidth() ;
        }

        // Account for the vertical scrollbar
        preferredWidth += 20 ;
        return preferredWidth ;
    }
}
