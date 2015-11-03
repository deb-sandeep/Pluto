/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 15, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.news;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.biz.svc.IRSSSvc ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.STConstant ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.STUtils ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.dto.RSSNewsItem ;
import com.sandy.apps.pluto.shared.event.Event ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.event.IEventSubscriber ;
import com.sandy.apps.pluto.shared.util.util.ConfigManager ;
import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.dialogmgr.AbstractPlutoFramePanel ;

/**
 * This class is the concrete implementation of the self refreshing news panel.
 * This panel has two parts, the overview summaryTable and the details panel. The
 * overview summaryTable shows rows of news summary with color coded rows for
 * read and unread news items. On selecting a row in the news overview summaryTable,
 * the details of the news item will be displayed in the description panel.
 * <p>
 * ADD MORE DETAILS ABOUT THE ACTIONS SUPPORTED.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class NewsPanel extends AbstractPlutoFramePanel
    implements UIConstant, ActionListener, IEventSubscriber  {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 561684807893431129L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( NewsPanel.class ) ;

    /** The summaryTable which will display the task summary. */
    private NewsSummaryTable summaryTable = null ;

    /** The text are in which the user can enter the news search query. */
    private final JTextField searchTF = new JTextField() ;

    /** The tree which shows the news categories. */
    private NewsCategoryTree tree = null ;

    /** The text area in which the news item details will be shown. */
    private final JTextArea descrTA = new JTextArea() ;

    /** This label is used to highlight the title of the selected news item. */
    private final JLabel titleLabel = new JLabel() ;

    /**
     * A map which stores the news items categorized on the basis of their site
     * and category. Any new news items added in transit will be moved down
     * to their proper root node and stored. If the value of a key is a map,
     * the key represents a non terminal node, else if the value is a list the
     * key is a terminal node.
     */
    private final Map<String, Object> newsMap = new HashMap<String, Object>() ;

    /** Public constructor. */
    public NewsPanel() {
        super( "Latest News" ) ;
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
            { AC_SHOW_BROWSER,     IMG_BROWSER,     IMG_BROWSER_PRESSED,     "Browse" },
            { AC_EXEC_NOW,         IMG_EXEC_NOW,    IMG_EXEC_NOW_PRESSED,    "Fetch now" }
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
        if( actCmd.equals( AC_SHOW_BROWSER ) ) {
            this.summaryTable.openNewsDetailsInBrowser() ;
        }
        else if( actCmd.equals( AC_NEWS_SEARCH ) ) {
            final String query = this.searchTF.getText() ;
            this.summaryTable.setFilterCriteria( query ) ;
        }
        else if( actCmd.equals( AC_EXEC_NOW ) ) {
            final int numItems = ServiceMgr.getRSSSvc().importActiveNews() ;
            LogMsg.info( numItems + " news items imported." ) ;
        }
    }

    /**
     * Initializes the nature of this panel, including setting up appropriate
     * listeners, controller and model of the charting engine.
     */
    @Override
    public void initializeData() throws STException {

        // Load the previous news items and setup the internal data structure
        loadPreviousNewsItems() ;

        // Initialize the news item overview table.
        this.summaryTable = new NewsSummaryTable( this.newsMap, this ) ;
        this.summaryTable.initialize() ;

        // Prepare the tree
        this.tree = new NewsCategoryTree( this.newsMap, this ) ;
        this.tree.initialize() ;

        // Set up the text area for the description information
        initializeTextArea() ;

        // Set up the text field in which the user can enter the search queries
        initializeTextField() ;

        final JScrollPane treeSP = new JScrollPane( this.tree ) ;
        treeSP.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ) ;
        treeSP.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ) ;

        final JScrollPane tableSP = new JScrollPane( this.summaryTable ) ;
        tableSP.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ) ;

        final JPanel tablePanel = new JPanel() ;
        tablePanel.setLayout( new BorderLayout() ) ;
        tablePanel.add( tableSP, BorderLayout.CENTER ) ;
        tablePanel.add( this.searchTF, BorderLayout.NORTH ) ;

        final JScrollPane textSP = new JScrollPane( this.descrTA ) ;
        textSP.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER ) ;

        // Create the panel holding the top right quadrant components, the
        // table and the search text field.
        final JPanel lowerPanel = new JPanel() ;
        lowerPanel.setLayout( new BorderLayout() ) ;
        lowerPanel.add( textSP, BorderLayout.CENTER ) ;
        lowerPanel.add( this.titleLabel, BorderLayout.NORTH ) ;

        // Create the top split pane, the left side of which is the tree and
        // the right side of which is the table and search text field.
        final JSplitPane topSplit = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT ) ;
        topSplit.setDividerLocation( 180 ) ;
        topSplit.setOneTouchExpandable( false ) ;
        topSplit.setDividerSize( 1 ) ;
        topSplit.add( treeSP ) ;
        topSplit.add( tablePanel ) ;

        // Create the overall split pane, the top of which is the tree and table
        // while the bottom is the description text area.
        final JSplitPane splitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT ) ;
        splitPane.setDividerLocation( 240 ) ;
        splitPane.setOneTouchExpandable( false ) ;
        splitPane.setDividerSize( 1 ) ;
        splitPane.add( topSplit ) ;
        splitPane.add( lowerPanel ) ;

        setLayout( new BorderLayout() ) ;
        add( splitPane, BorderLayout.CENTER ) ;

        // Register this instance as an event subscriber to the news events
        EventBus.instance().addSubscriberForEventTypes( this, EventType.RSS_NEWS_IMPORTED ) ;
    }

    /**
     * This method is called during the initialization of this model. For the
     * first time, we pre load the news items since the last seven days.
     */
    private void loadPreviousNewsItems() throws STException {

        final IRSSSvc svc = ServiceMgr.getRSSSvc() ;
        final Calendar cal = Calendar.getInstance() ;
        final Date endDate = STUtils.getEndOfDay( cal.getTime() ) ;

        final int numOldDays = ConfigManager.getInstance().getInt( STConstant.CFG_KEY_NUM_OLD_DAYS_NEWS, 7 ) ;
        cal.add( Calendar.DATE, -1*numOldDays ) ;
        final Date startDate = STUtils.getStartOfDay( cal.getTime() ) ;

        final List<RSSNewsItem> newsItems = svc.getNewsItems( startDate, endDate, true ) ;
        categorizeNewsItems( newsItems ) ;
    }

    /** A helper method to categorize a list of news items. */
    @SuppressWarnings("unchecked")
    private void categorizeNewsItems( final List<RSSNewsItem> newsItems ) {
        for( final RSSNewsItem item : newsItems ) {

            final String site = item.getSite() ;
            final String category = item.getCategory() ;

            Map<String, List<RSSNewsItem>> siteMap =
                    ( Map<String, List<RSSNewsItem>> )this.newsMap.get( site ) ;

            if( siteMap == null ) {
                siteMap = new HashMap<String, List<RSSNewsItem>>() ;
                this.newsMap.put( site, siteMap ) ;
            }

            List<RSSNewsItem> newsItemList = siteMap.get( category ) ;
            if( newsItemList == null ) {
                newsItemList = new ArrayList<RSSNewsItem>() ;
                siteMap.put( category, newsItemList ) ;
            }

            newsItemList.add( item ) ;
            Collections.sort( newsItemList ) ;
        }
    }

    /** A helper method to initialize the text field for search queries. */
    private void initializeTextField() {

        this.searchTF.setActionCommand( AC_NEWS_SEARCH ) ;
        this.searchTF.setFont( NEWS_SUMMARY_FONT ) ;
        this.searchTF.addActionListener( this ) ;
    }

    /** A helper method to initialize the text area. */
    private void initializeTextArea() {

        this.titleLabel.setFont( NEWS_TITLE_FONT ) ;
        this.titleLabel.setOpaque( true ) ;
        this.titleLabel.setForeground( Color.DARK_GRAY ) ;
        this.titleLabel.setPreferredSize( new Dimension( 1, 17 ) ) ;

        this.descrTA.setFont( NEWS_DETAIL_FONT ) ;
        this.descrTA.setWrapStyleWord( true ) ;
        this.descrTA.setEditable( false ) ;
        this.descrTA.setLineWrap( true ) ;
    }

    /**
     * This method is invoked on the child panels when the dialog is being
     * disposed as a result of a cancel operation chosen by the user. Subclasses
     * can implement this function to perform cleanup logic like de-registering
     * from the event bus etc.
     */
    public void destroy() {
        EventBus.instance().removeSubscriber( this, EventType.RSS_NEWS_IMPORTED ) ;
    }

    /**
     * This method is invoked when {@link EventType#RSS_NEWS_IMPORTED} events
     * are generated. The value of the event is a list of {@link RSSNewsItem}
     * instances. This method processes the event and updates the table model.
     * The update to the table model will trigger the table UI rendering via the
     * Swing event notification mechanism.
     *
     * @param event The RSS_NEWS_IMPORTED event. The value of the event is
     *        a List of {@link RSSNewsItem} instances.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void handleEvent( final Event event ) {

        final List<RSSNewsItem> items = (List<RSSNewsItem>)event.getValue() ;
        if( items != null && !items.isEmpty() ) {
            categorizeNewsItems( items ) ;

            // Notify the table and tree that we have added new items and they
            // need to repaint themselves.
            this.summaryTable.repaint() ;
            this.tree.repaint() ;
        }
    }

    /** Returns the list of news items for the specified site and category. */
    @SuppressWarnings("unchecked")
    public List<RSSNewsItem> getNewsItems( final String site, final String cat ) {

        List<RSSNewsItem> newsItems = null ;
        final Map<String, List<RSSNewsItem>> siteMap =
                   ( Map<String, List<RSSNewsItem>> ) this.newsMap.get( site ) ;
        if( siteMap != null ) {
            newsItems = siteMap.get( cat ) ;
        }

        if( newsItems == null ) {
            newsItems = new ArrayList<RSSNewsItem>() ;
        }

        return newsItems ;
    }

    /** Sets the given text in the description text area. */
    void setDescription( final String descr ) {
        this.descrTA.setText( descr ) ;
    }

    /** Sets the given list of news items as the new table content. */
    void setNewsItemsInTable( final List<RSSNewsItem> items ) {
        this.summaryTable.setNewsItemsInTable( items ) ;
    }

    /** Sets the news title. */
    void setNewsTitle( final String title ) {
        this.titleLabel.setText( title ) ;
    }

    /** Repaints the tree. */
    void repaintTree() {
        this.tree.repaint() ;
    }
}
