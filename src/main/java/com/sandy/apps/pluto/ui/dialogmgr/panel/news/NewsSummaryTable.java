/**
 * 
 * 
 * 
 *
 * Creation Date: Dec 4, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.news;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.biz.svc.IRSSSvc ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.dto.RSSNewsItem ;
import com.sandy.apps.pluto.shared.util.util.StringUtil ;
import com.sandy.apps.pluto.ui.UIConstant ;

/**
 * This class encapsulates the table which displays the news items. This class
 * also handles the user interations on the table, for example row selections
 * and asks the panel to mediate the actions with other panel components.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class NewsSummaryTable extends JTable
    implements UIConstant {

    /** Default serial version UID. This class will never be serialized. */
    private static final long serialVersionUID = 1L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( NewsSummaryTable.class ) ;

    /**
     * This anonymous internal class implements the logic of handling double
     * clicks on any row of the table. When a user double clicks on any row,
     * it is a que for us to open up a chart of that index. This is a convenient
     * way for the user to investigate any particular index instead of using
     * the normal drag and drop feature or using the chart button.
     */
    private final MouseListener mouseListener = new MouseAdapter() {

        /**
         * Traps the mouse click events and triggers a graph once a double click
         * is detected.
         */
        @Override
        public void mouseClicked( final MouseEvent e ) {
            if( e.getClickCount() == 2 ) {
                NewsSummaryTable.this.openNewsDetailsInBrowser() ;
            }
        }
    } ;

    /**
     * A reference to the news panel which holds this tree. This tree will use
     * this reference to call back methods on the panel to relay the user
     * initiated actions on the tree. The panel acts as a mediator which
     * translates calls inititated by this class to actions on other panel
     * participants.
     */
    private final NewsPanel newsPanel ;

    /** The model used for the job summary summaryTable. */
    private final NewsSummaryTableModel tableModel = new NewsSummaryTableModel() ;

    /** The summaryTable row sorter to be used to filter the rows of the summaryTable. */
    private final TableRowSorter<NewsSummaryTableModel> sorter =
                   new TableRowSorter<NewsSummaryTableModel>( this.tableModel ) ;

    /**
     * Public constructor.
     *
     * @param newsMap The pre loaded news map which contains past news items.
     * @param newsPanel The parent panel of which this tree is a part.
     */
    public NewsSummaryTable( final Map<String, Object> newsMap,
                             final NewsPanel newsPanel ) {
        this.newsPanel = newsPanel ;
    }

    /** A helper method to initialize the summary table. */
    public void initialize() throws STException {

        // Add the summaryTable model, summaryTable column model etc for the summaryTable.
        setModel( this.tableModel ) ;
        setDefaultRenderer( String.class,  new NewsSummaryTableCellRenderer( this.tableModel ) ) ;
        setDefaultRenderer( Boolean.class, new NewsSummaryTableCellRenderer( this.tableModel ) ) ;
        setDefaultRenderer( Date.class,    new NewsSummaryTableCellRenderer( this.tableModel ) ) ;
        setAutoCreateRowSorter( true ) ;
        setGridColor( new Color( 243, 243, 243 ) ) ;
        setFont( NEWS_SUMMARY_FONT ) ;
        getTableHeader().setFont( LOG_FONT_BOLD ) ;
        setRowHeight( 18 ) ;
        setDoubleBuffered( true ) ;
        setRowSorter( this.sorter ) ;
        setRowSelectionAllowed( true ) ;
        setColumnSelectionAllowed( false ) ;
        setSelectionMode( ListSelectionModel.SINGLE_SELECTION ) ;
        getSelectionModel().addListSelectionListener( this ) ;
        addMouseListener( this.mouseListener ) ;

        // Set the initial sort filter to null. This will cause all the
        // rows in the summaryTable to be visible
        this.sorter.setRowFilter( null ) ;

        setColumnProperties( NewsSummaryTableModel.COL_READ,  10  ) ;
        setColumnProperties( NewsSummaryTableModel.COL_TIME,  100 ) ;
        setColumnProperties( NewsSummaryTableModel.COL_TITLE, 330  ) ;
    }

    /**
     * A tiny helper method to set the properties of the columns in the ITD
     * summaryTable.
     *
     * @param colId The identifier of the column
     * @param width The preferred width
     */
    private void setColumnProperties( final int colId, final int width ) {
        final TableColumnModel colModel = getColumnModel() ;
        final TableColumn col = colModel.getColumn( colId ) ;
        col.setPreferredWidth( width ) ;
        col.setResizable( true ) ;
    }

    /**
     * Sets the filter query on the table. This method accepts a string, which
     * the user has entered as the search query, converts the query into
     * appropriate row filters and applies the row filters on the table.
     */
    public void setFilterCriteria( final String filterQuery ) {

        if( StringUtil.isNotEmptyOrNull( filterQuery ) ) {
            RowFilter<Object, Object> rowFilter = null ;
            rowFilter = RowFilter.regexFilter( filterQuery.trim(), NewsSummaryTableModel.COL_TITLE ) ;
            this.sorter.setRowFilter( rowFilter ) ;
        }
        else {
            this.sorter.setRowFilter( null ) ;
        }
    }

    /**
     * This method opens the selected news details in system dependent browser.
     * If there is no row selected, this method silently returns.
     */
    public void openNewsDetailsInBrowser() {
        final int selIndex = getSelectedRow() ;
        if( selIndex != -1 ) {
            final int modelRow = convertRowIndexToModel( selIndex ) ;
            final RSSNewsItem item = this.tableModel.getNewsItemAtRow( modelRow ) ;

            try {
                if( StringUtil.isNotEmptyOrNull( item.getUrl() ) ) {
                    Desktop.getDesktop().browse( new URI( item.getUrl() ) ) ;
                }
            }
            catch ( final Exception e1 ) {
                logger.error( "Could not open browser for URL " + item.getUrl(), e1 ) ;
                LogMsg.error( "Could not open browser. Msg = " + e1.getMessage() ) ;
            }
        }
    }

    /**
     * This method is invoked by Swing framework when the table row selection
     * changes. This is a trigger for us to change the long description of the
     * selected job in the description text box.
     */
    @Override
    public void valueChanged( final ListSelectionEvent evt ) {

        try {
            final int newIndex = getSelectedRow() ;
            if( evt.getValueIsAdjusting() || newIndex < 0 ) {
                return ;
            }

            super.repaint() ;
            this.newsPanel.repaintTree() ;

            final int modelRow = convertRowIndexToModel( newIndex ) ;
            final IRSSSvc rssSvc = ServiceMgr.getRSSSvc() ;

            final RSSNewsItem item         = this.tableModel.getNewsItemAtRow( modelRow ) ;
            final RSSNewsItem enrichedItem = rssSvc.getNewsDetails( item ) ;

            final String title = "<html><body><font color='blue'>[ " +
                                 item.getSite() + " - " +
                                 NewsSummaryTableCellRenderer.TITLE_DATE_TIME_FMT.format( item.getPublishDate() ) +
                                 " ]</font><b> " + item.getTitle() +
                                 "</b></body></html>" ;
            // Set the new detailed description for the selected task
            this.newsPanel.setDescription( enrichedItem.getDescription() ) ;
            this.newsPanel.setNewsTitle( title ) ;

            // Now mark the item in the ovewview table as read, so that it
            // is displayed properly
            if( item.isNewItem() ) {
                item.setNewItem( false ) ;
            }

            // Enable or disable the browser action button depending upon whether
            // this news item has a valid URL associated with it.
            final boolean enable = StringUtil.isNotEmptyOrNull( item.getUrl() ) ;
            this.newsPanel.getDialogManager().enablePanelBtn( this.newsPanel, AC_SHOW_BROWSER, enable ) ;
        }
        catch ( final STException e ) {
            LogMsg.error( "Could not fetch details for news item" ) ;
            logger.debug( "News details could not be fetched", e ) ;
        }
    }

    /** Sets the given list of news items as the new table content. */
    void setNewsItemsInTable( final List<RSSNewsItem> items ) {
        this.tableModel.setNewsItems( items ) ;
    }
}
