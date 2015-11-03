/**
 * 
 * 
 * 
 *
 * Creation Date: Nov 21, 2008
 */

package com.sandy.apps.pluto.ui.statusbar;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.StockTracker ;
import com.sandy.apps.pluto.shared.CacheListener ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.dto.ExIndexITD ;
import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.UIHelper ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoFrameType ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoInternalFrame ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.ChartingPanel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartEntityConfig ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartEntity.EntityType ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.indexitdsummary.IndexITDValueCache ;

/**
 * This status bar component tracks the index values. This component provides
 * the following UI units:
 * <ul>
 * <li>Index choice button : A button which when clicks allows the user to
 *     choose one of the indexes which are being tracked by Pluto. The
 *     default value will be 'S&P CNX NIFTY"</li>
 * <li>Index name display : A label displaying the currently chosen index name</li>
 * <li>Current value : A label displaying the current value of the index</li>
 * <li>Points : A label displaying the number of points that this index is
 *     riding relative to the last closing value</li>
 * <li>Percentage : A label displaying the percentage variation as compared
 *     to the last closing value</li>
 * </ul>
 *
 * Depending upon whether the index is in the +ve or -ve range, the values will
 * be shown in green or red font, providing a visual marker of the direction.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class IndexValueSBComponent extends AbstractSBComponent
    implements CacheListener, ActionListener {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( IndexValueSBComponent.class ) ;

    /** The index selection button, clicking on which will pop op a selection menu.*/
    private JButton indexSelBtn = null ;

    /** The label which will display the index name. */
    private JButton indexNameButton = null ;

    /** The label which will display the current value of the index. */
    private final JLabel valueLabel = new JLabel() ;

    /** The label which will display the difference in points value. */
    private final JLabel pointsLabel = new JLabel() ;

    /** The label which will display the percentage difference value. */
    private final JLabel percentageLabel = new JLabel() ;

    /** The current index name we are dealing with. */
    private String currIndexName = "S&P CNX NIFTY" ;

    /** The decimal format used to render decimal values. */
    private static final DecimalFormat DF = new DecimalFormat( "##.##" ) ;

    /** The popup menu which will enable the user to switch the index name.*/
    private JPopupMenu popupMenu = null ;

    /**
     * The number of indexes that we have. This is required to position the
     * popup menu.
     */
    private int numIndexes = 0 ;

    /** Public no argument constructor. */
    public IndexValueSBComponent() {
        super() ;
    }

    /**
     * This method is called by the parent status bar component before this
     * component is added to the status bar. This is an opportunity for us
     * to initialize ourself and setup our own UI.
     */
    @Override
    public void initialize() throws STException {
        // Setup our UI
        super.setBackground( Color.BLACK ) ;
        super.setLayout( new FlowLayout() ) ;

        add( getIndexSelButton() ) ;
        add( getIndexChartButton() ) ;
        add( formatLabel( this.valueLabel,      Color.YELLOW,     Font.BOLD,  14 ) ) ;
        add( formatLabel( this.pointsLabel,     Color.LIGHT_GRAY, Font.BOLD,  12 ) ) ;
        add( formatLabel( this.percentageLabel, Color.LIGHT_GRAY, Font.ITALIC,12 ) ) ;

        // Now refresh the values - this refresh will determine the initial
        // values that are displayed on the labels.
        this.indexNameButton.setText( this.currIndexName ) ;
        refreshValues() ;

        // Register ourself as a listener to the ITD value cache. We deal with
        // the cache instead of the backend services because, the cache always
        // has a persistent backup of the latest index value. If we don't deal
        // with the cache, we will have to implement the whole logic of figuring
        // out which is the last refresh time etc etc. - big headache.
        IndexITDValueCache.getInstance().addITDValueCacheListener( this ) ;
    }

    /** Configures a button for use in this panel. A refactored method. */
    private JButton getIndexSelButton() {

        if( this.indexSelBtn == null ) {
            this.indexSelBtn = new JButton() ;
            this.indexSelBtn.setIcon( UIHelper.getIcon( "index_choose.png" ) ) ;
            this.indexSelBtn.setPressedIcon( UIHelper.getIcon( "index_choose_pressed.png" ) ) ;
            this.indexSelBtn.setBackground( Color.black ) ;
            this.indexSelBtn.addActionListener( this ) ;
            this.indexSelBtn.setContentAreaFilled( false ) ;
            this.indexSelBtn.setBorderPainted( false ) ;
            this.indexSelBtn.setFocusPainted( false ) ;
            this.indexSelBtn.setIconTextGap( 0 ) ;
            this.indexSelBtn.setPreferredSize( new Dimension( 16, 16 ) ) ;
            this.indexSelBtn.setMargin( new Insets(0,0,0,0) ) ;
            this.indexSelBtn.setBorder( UIConstant.EMPTY_BORDER ) ;
            this.indexSelBtn.setActionCommand( UIConstant.AC_SHOW_INDEX_SEL ) ;
        }
        return this.indexSelBtn ;
    }

    /**
     * Configures a button for use as the index name. The user can click on the
     * button to pop up an index chart for the current index.
     */
    private JButton getIndexChartButton() {

        if( this.indexNameButton == null ) {
            this.indexNameButton = new JButton() ;
            this.indexNameButton.setBackground( Color.black ) ;
            this.indexNameButton.addActionListener( this ) ;
            this.indexNameButton.setContentAreaFilled( true ) ;
            this.indexNameButton.setBorderPainted( false ) ;
            this.indexNameButton.setFocusPainted( false ) ;
            this.indexNameButton.setIconTextGap( 0 ) ;
            this.indexNameButton.setMargin( new Insets(0,0,0,0) ) ;
            this.indexNameButton.setBorder( UIConstant.EMPTY_BORDER ) ;
            this.indexNameButton.setActionCommand( UIConstant.AC_SHOW_CHART ) ;
        }
        return this.indexNameButton ;
    }

    /** Prepares the supplied JLabel by setting its attributes. */
    private JLabel formatLabel( final JLabel label, final Color color,
                                final int style, final int size ) {

        label.setBackground( Color.BLACK ) ;
        label.setForeground( color ) ;
        label.setFont( new Font( "Tahoma", style, size ) ) ;

        return label ;
    }

    /**
     * This method is invoked on this instance when the Index ITD cache values
     * have changed. This is a trigger for us to update the display values
     * for the currently managed index.
     */
    public void cacheDataChanged() {
        refreshValues() ;
    }

    /**
     * This method is called when the values need to be refreshed with the
     * latest values in the cache. This method queries the cache for the latest
     * values of the currently displayed index and renders the values in
     * the various labels.
     */
    private void refreshValues() {
        final IndexITDValueCache cache = IndexITDValueCache.getInstance() ;
        final ExIndexITD itdVal = cache.getExIndexITDForIndex( this.currIndexName ) ;

        if( itdVal != null ) {
            final double prevClose = itdVal.getPrevClose() ;
            final double diffPts   = itdVal.getCurrentVal() - prevClose ;
            final double pctChange = ( diffPts / prevClose )*100 ;

            final Color fontColor = ( pctChange < 0 ) ? Color.RED : Color.GREEN ;
            this.pointsLabel.setForeground( fontColor ) ;
            this.percentageLabel.setForeground( fontColor ) ;

            this.valueLabel.setText( DF.format( itdVal.getCurrentVal() ) ) ;
            this.pointsLabel.setText( DF.format( diffPts ) ) ;
            this.percentageLabel.setText( DF.format( pctChange ) + "%" ) ;

            // Now construct the task bar tooltip text. We change the tooltip
            // of the task bar icon with every index refresh.
            final StringBuffer buffer = new StringBuffer() ;
            buffer.append( this.currIndexName ).append( " " ) ;
            buffer.append( this.valueLabel.getText() ).append( " " ) ;
            buffer.append( "(" + this.pointsLabel.getText() + ") " ) ;
            buffer.append( this.percentageLabel.getText() ) ;
            StockTracker.SYS_TRAY.setTooltip( buffer.toString() ) ;
        }
        else {
            logger.debug( "Index ITD value for index " + this.currIndexName +
                          " could not be found in the Index ITD cache" ) ;
        }
    }

    /**
     * This method is called when the index selection button is pressed or
     * the user selects any of the index names from the index selection popup
     * menu. We show up a popup menu containing all the indexes supported and let the
     * user select
     */
    @Override
    public void actionPerformed( final ActionEvent e ) {

        final String actCmd = e.getActionCommand() ;

        if( actCmd.equals( UIConstant.AC_SHOW_INDEX_SEL ) ) {
            final JPopupMenu popup = getPopupMenu() ;
            popup.show( this.indexSelBtn, 0, -this.numIndexes*16 ) ; ;
        }
        else if( actCmd.equals( UIConstant.AC_SHOW_CHART ) ) {
            plotChart() ;
        }
        else {
            // This is the name of an index. Switch the active index name
            this.currIndexName = actCmd ;
            this.indexNameButton.setText( this.currIndexName ) ;
            refreshValues() ;
        }
    }

    /** A helper method to open a chart for the selected symbols. */
    private void plotChart() {
        try {
            final ChartingPanel panel  = new ChartingPanel() ;
            final PlutoInternalFrame dialog = new PlutoInternalFrame( "Charting",
                                                JFrame.DISPOSE_ON_CLOSE,
                                                PlutoFrameType.CHART_FRAME,
                                                panel ) ;

            dialog.setSize( ChartingPanel.DEFAULT_WIDTH, ChartingPanel.DEFAULT_HEIGHT ) ;
            dialog.setResizable( true ) ;

            List<ChartEntityConfig> cfgList = null ;
            ChartEntityConfig       cfg     = null ;

            final ExIndexITD index = IndexITDValueCache.getInstance().getExIndexITDForIndex( this.currIndexName ) ;
            cfgList = new ArrayList<ChartEntityConfig>() ;
            cfg = new ChartEntityConfig( index.getIndex(), EntityType.INDEX,
                                         index.getDate() ) ;
            cfgList.add( cfg ) ;
            panel.addChartEntities( cfgList ) ;

            StockTracker.MAIN_FRAME.addInternalFrame( dialog, true ) ;
        }
        catch ( final STException e ) {
            logger.error( "Unable to open chart", e ) ;
        }
    }

    /**
     * Returns a JPopupMenu containing all the indexes supported by PLUTO.
     */
    private JPopupMenu getPopupMenu() {

        if( this.popupMenu == null ) {
            this.popupMenu = new JPopupMenu() ;
            this.popupMenu.setBackground( Color.BLACK ) ;
            this.popupMenu.setFont( UIConstant.LOG_FONT ) ;
            this.popupMenu.setForeground( Color.LIGHT_GRAY ) ;

            final Collection<String> indexNames = IndexITDValueCache.getInstance().getIndexNames() ;
            this.numIndexes = indexNames.size() ;
            for( final String indexName : indexNames ) {
                this.popupMenu.add( getJMenuItem( indexName ) ) ;
            }
        }
        return this.popupMenu ;
    }

    /**
     * Creates a menu item for the specified dialog manager. The active title
     * of the dialog manager is used to create the menu item. This menu item
     * will be the part of a transitent popup menu and when the user clicks
     * the menu item, the window associated with the menu item will be made
     * visible.
     */
    private JMenuItem getJMenuItem( final String indexName ) {

        final JMenuItem menuItem = new JMenuItem() ;
        menuItem.setBackground( Color.BLACK ) ;
        menuItem.setForeground( Color.WHITE ) ;
        menuItem.setFont( UIConstant.LOG_FONT ) ;
        menuItem.setText( indexName ) ;
        menuItem.addActionListener( this ) ;
        menuItem.setActionCommand( indexName ) ;
        return menuItem ;
    }
}
