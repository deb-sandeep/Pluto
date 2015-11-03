/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 15, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.portfolio ;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.biz.svc.IITDImportSvc ;
import com.sandy.apps.pluto.shared.CacheListener ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.event.Event ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.event.IEventSubscriber ;
import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.UIHelper ;
import com.sandy.apps.pluto.ui.dialogmgr.AbstractPlutoFramePanel ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoInternalFrame ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.scripeodsummary.ScripEODValueCache ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.scripitdsummary.ScripITDValueCache ;
import com.sandy.apps.pluto.ui.svc.STViewService ;

/**
 * TODO: Document this.
 *
 * a) Documentation/Formatting/Review
 * f) Sensitive to trade events, add/remove/update
 * d) DONE - Drag/Drop/Charting
 * b) DONE - Sensitive to ITD changes
 * c) DONE - Total row
 * e) DONE - Portfolio title shows latest time of update
 * g) DONE - Provision of manual update (data refresh)
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class PortfolioSummaryPanel extends AbstractPlutoFramePanel
    implements UIConstant, ActionListener, CacheListener, IEventSubscriber {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 561684807893431129L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( PortfolioSummaryPanel.class ) ;

    /** The model used for the portfolio summary table. */
    private final PSTableModel tableModel = new PSTableModel() ;

    /** The model used for the portfolio summary total row. */
    private final PSTotalTableModel totalTM = new PSTotalTableModel() ;

    /** The table which will display the portfolio summary. */
    private final PSTable table = new PSTable() ;

    /** The table representing the portfolio summary total row. */
    private final JTable summaryRow = new JTable() ;

    /** The time format for rendering the time in the title. */
    private final static SimpleDateFormat TITLE_DATE_TIME_FMT =
                                     new SimpleDateFormat( "dd-MMM HH:mm:ss" ) ;

    /** Public constructor. */
    public PortfolioSummaryPanel( final String name ) {
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
            { AC_EXEC_NOW, IMG_EXEC_NOW, IMG_EXEC_NOW_PRESSED, "Refresh portfolio" },
            { PlutoInternalFrame.BTN_TYPE_TOGGLE + AC_TOGGLE_PORTFOLIO, IMG_PORTFOLIO_CURRENT, IMG_PORTFOLIO_ALL, "Holdings" },
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
        if( actCmd.equals( AC_EXEC_NOW ) ) {
            try {
                PortfolioManager.getInstance().refresh() ;
                // If we have successfully refreshed the portfolio, update the
                // user interface.
                cacheDataChanged() ;
            }
            catch ( final STException e1 ) {
                logger.error( "Portfolio refresh failed", e1 ) ;
                LogMsg.error( "Portfolio refresh failed." ) ;
            }
        }
        else if( actCmd.equals( AC_TOGGLE_PORTFOLIO ) ) {
            final boolean showAllHoldings = getDialogManager().isBtnSelected(
                                                   this, AC_TOGGLE_PORTFOLIO ) ;
            this.tableModel.setShowZeroHoldingStocks( showAllHoldings ) ;
            this.totalTM.setShowZeroHoldingStocks( showAllHoldings ) ;
            this.tableModel.fireTableDataChanged() ;
            this.totalTM.fireTableDataChanged() ;
        }
        else if( actCmd.equals( AC_TRADE_ADD ) ) {
            showTradeEntryDialog() ;
        }
        else if( actCmd.equals( AC_FETCH ) ) {
            fetchHiResITDData() ;
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
                final StockTradeGrouping grp =
                              this.tableModel.getStockTradeGrouping( modelRow ) ;

                viewSvc.showEquityBuySellDialog( grp.getSymbol(), true ) ;
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
        this.table.setDefaultRenderer( String.class,  new PSTableCellRenderer( this.tableModel ) ) ;
        this.table.setDefaultRenderer( Integer.class, new PSTableCellRenderer( this.tableModel ) ) ;
        this.table.setDefaultRenderer( Double.class,  new PSTableCellRenderer( this.tableModel ) ) ;
        this.table.setAutoCreateRowSorter( true ) ;
        this.table.setGridColor( UIHelper.GRID_COLOR ) ;
        this.table.setFont( LOG_FONT ) ;
        this.table.getTableHeader().setFont( LOG_FONT_BOLD ) ;
        this.table.setRowHeight( 15 ) ;
        this.table.setDoubleBuffered( true ) ;
        this.table.setRowSelectionAllowed( true ) ;
        this.table.setColumnSelectionAllowed( false ) ;
        this.table.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION ) ;
        this.table.getTableHeader().setReorderingAllowed( false ) ;

        // Add the table model, table column model etc for the table.
        this.summaryRow.setModel( this.totalTM ) ;
        this.summaryRow.setDefaultRenderer( String.class,  new PSTotalTableCellRenderer() ) ;
        this.summaryRow.setDefaultRenderer( Integer.class, new PSTotalTableCellRenderer() ) ;
        this.summaryRow.setDefaultRenderer( Double.class,  new PSTotalTableCellRenderer() ) ;
        this.summaryRow.setGridColor( Color.BLACK ) ;
        this.summaryRow.setForeground( Color.WHITE ) ;
        this.summaryRow.setFont( LOG_FONT ) ;
        this.summaryRow.setRowHeight( 15 ) ;
        this.summaryRow.setDoubleBuffered( true ) ;
        this.summaryRow.setRowSelectionAllowed( true ) ;
        this.summaryRow.setColumnSelectionAllowed( false ) ;
        this.summaryRow.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION ) ;

        // Set the table as a drag source
        // Set up a DragGestureRecognizer that will detect when the user
        // begins a drag.  When it detects one, it will notify us by calling
        // the dragGestureRecognized() method of the DragGestureListener
        // interface we implement below
        final DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer( this.table,
                      DnDConstants.ACTION_COPY_OR_MOVE, this.table );

        setColumnProperties( PSTableModel.COL_SYMBOL,          100 ) ;
        setColumnProperties( PSTableModel.COL_QTY,              30 ) ;
        setColumnProperties( PSTableModel.COL_PRICE,            40 ) ;
        setColumnProperties( PSTableModel.COL_LTP,              40 ) ;
        setColumnProperties( PSTableModel.COL_UNREALIZED,       50 ) ;
        setColumnProperties( PSTableModel.COL_UNREALIZED_PCT,   45 ) ;
        setColumnProperties( PSTableModel.COL_VAL_AT_COST,      50 ) ;
        setColumnProperties( PSTableModel.COL_VAL_AT_MKT,       50 ) ;
        setColumnProperties( PSTableModel.COL_REALIZED,         50 ) ;

        setLayout( new BorderLayout() ) ;
        add( tableSP, BorderLayout.CENTER ) ;
        add( this.summaryRow, BorderLayout.SOUTH ) ;

        // The table model is a subscriber to the ITD & EOD value cache
        ScripITDValueCache.getInstance().addITDValueCacheListener( this ) ;
        ScripEODValueCache.getInstance().addEODValueCacheListener( this ) ;

        EventBus.instance().addSubscriberForEventPatterns( this, "TRADE_.*" ) ;
    }

    /**
     * A tiny helper method to set the properties of the columns in the ITD
     * table.
     *
     * @param colId The identifier of the column
     * @param width The preferred width
     */
    private void setColumnProperties( final int colId, final int width ) {
        TableColumnModel colModel = this.table.getColumnModel() ;
        TableColumn col = colModel.getColumn( colId ) ;
        col.setPreferredWidth( width ) ;
        col.setMinWidth( width ) ;
        col.setResizable( true ) ;

        colModel = this.summaryRow.getColumnModel() ;
        col = colModel.getColumn( colId ) ;
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
        // The table model is a subscriber to the ITD & EOD value cache
        ScripITDValueCache.getInstance().removeITDValueCacheListener( this ) ;
        ScripEODValueCache.getInstance().removeEODValueCacheListener( this ) ;

        EventBus.instance().removeSubscriber( this, EventType.TRADE_ADDED ) ;
        EventBus.instance().removeSubscriber( this, EventType.TRADE_DELETED ) ;
        EventBus.instance().removeSubscriber( this, EventType.TRADE_UPDATED ) ;
    }

    /**
     * Returns the preferred width of this panel.
     */
    public int getPreferredWidth() {
        int preferredWidth = 0 ;
        final int dispColId[] = {
            PSTableModel.COL_SYMBOL,
            PSTableModel.COL_QTY,
            PSTableModel.COL_PRICE,
            PSTableModel.COL_LTP,
            PSTableModel.COL_UNREALIZED,
            PSTableModel.COL_UNREALIZED_PCT,
            PSTableModel.COL_VAL_AT_COST,
            //PSTableModel.COL_VAL_AT_MKT,
            //PSTableModel.COL_REALIZED
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

    /**
     * This method is invoked when the data in the ITD or EOD cache has changed.
     * This is a cue for us to fire a table changed event will will result in
     * the UI being refreshed with the new data in the cache.
     */
    public void cacheDataChanged() {
        this.tableModel.fireTableDataChanged() ;
        this.totalTM.fireTableDataChanged() ;

        // Refresh the title of the dialog.
        final String newTitle = " " + getName() + " @ " +
                                TITLE_DATE_TIME_FMT.format( new Date() ) ;
        super.getDialogManager().setPanelTitle( this, newTitle ) ;
    }

    /** Fetches hi resolution ITD data for selected scrips. */
    private void fetchHiResITDData() {
        final int selRows[] = this.table.getSelectedRows() ;
        if( selRows.length > 0 ) {

            final IITDImportSvc svc = ServiceMgr.getAsyncITDImportSvc() ;
            for( int i=0; i<selRows.length; i++ ) {
                final int modelRow   = this.table.convertRowIndexToModel( selRows[i] ) ;
                final String   symbol= this.tableModel.getStockTradeGrouping( modelRow ).getSymbol() ;

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

    /** This method is called whenever a trade related event is published. */
    public void handleEvent( final Event event ) {
        try {
            PortfolioManager.getInstance().refresh() ;
            cacheDataChanged() ;
        }
        catch ( final STException e ) {
            final String msg = "Error refreshing portfolio. Msg=" + e.getMessage() ;
            LogMsg.error( msg ) ;
            logger.error( msg, e ) ;
        }
    }
}
