/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 15, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.charting;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.swing.JLabel;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.dialogmgr.AbstractPlutoFramePanel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartEntity ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartEntityConfig ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartModel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.EODValue ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ITDValue ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartModel.ModelEvent ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartModel.ModelEventType ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartModel.ModelListener ;
import com.sandy.apps.pluto.ui.svc.STViewService ;

/**
 * This class is the concrete implementation of the graph plotting panel.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ChartingPanel extends AbstractPlutoFramePanel
    implements UIConstant, ActionListener, ModelListener {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 561684807893431129L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ChartingPanel.class ) ;

    /** Default size of a chart panel. */
    public static final int DEFAULT_WIDTH = 450 ;
    public static final int DEFAULT_HEIGHT = 300 ;

    /** The decimal format for showing numeric values in the title. */
    private static DecimalFormat NUMBER_FMT = new DecimalFormat( "###.##" ) ;

    /**
     * The base title of the panel. The complete title will be formed with this
     * string as the base string.
     */
    private static final String BASE_TITLE = "Chart" ;

    /** The set of colors which we can use for chart entities. */
    private static final Color[] CURVE_COLORS = {
       Color.green.brighter(),
       Color.cyan.brighter(),
       Color.magenta.brighter(),
       Color.lightGray.brighter(),
       Color.red.brighter(),
       Color.orange.brighter(),
       Color.pink.brighter(),
    } ;

    /** The current color index. This will cycle through the set of available colors.*/
    private static int colorIndex = 0 ;

    /** The model for this charting UI. */
    private final ChartModel model ;

    /** The canvas which will render the chart curves. */
    private final ChartCanvas canvas ;

    /** The chart control panel which lies on the NORTH of the chart. */
    private final ChartControlPanel controlPanel ;

    /** The listener which will listen to drops on this class. */
    private final DropTargetListener dropTgtListener = new DropTargetAdapter() {

        /**
         * This method is invoked by Swing when the user drop some dragable
         * item on the component for which this listener is engaged to
         * listen to drops.
         */
        @Override
        public void drop( final DropTargetDropEvent dtde ) {

            try {
                final Transferable tfr     = dtde.getTransferable() ;
                final DataFlavor[] flavors = tfr.getTransferDataFlavors() ;
                ChartEntityConfig entityConfig = null ;

                if( flavors[0] == CHART_ENTITY_DATA_FLAVOR ) {

                    entityConfig = ( ChartEntityConfig )tfr.getTransferData( CHART_ENTITY_DATA_FLAVOR ) ;
                    final List<ChartEntityConfig> cfgList = new ArrayList<ChartEntityConfig>() ;
                    cfgList.add( entityConfig ) ;
                    ChartingPanel.this.addChartEntities( cfgList ) ;
                }
            }
            catch ( final Exception e ) {
                // Ignore the drop operation if it is not supported.
                logger.error( "Unanticipated exception", e ) ;
            }
        }
    } ;

    /** Public constructor. */
    public ChartingPanel( final boolean showControlPanel ) {
        super( "Charting" ) ;

        // Set up the model and controller relationship for this charting UI.
        this.model              = new ChartModel() ;
        this.canvas             = new ChartCanvas( this.model, this ) ;
        this.controlPanel       = new ChartControlPanel( this.model, this.canvas ) ;

        if( !showControlPanel ) {
            this.controlPanel.setVisible( false ) ;
        }

        this.model.addModelListener( this.canvas ) ;
        this.model.addModelListener( this.controlPanel ) ;
        this.model.addModelListener( this ) ;

        // Add this panel as a drop target. Scrips or charting elements can
        // be dropped by the user by dragging them from somewhere else.
        new DropTarget( this, this.dropTgtListener ) ;
    }

    /** Public constructor. */
    public ChartingPanel() {
        this( true ) ;
    }

    /**
     * This method can be called upon the charting panel to add an intra day
     * scrip. The parameter helps the chart decide on the time and day for
     * plotting the intra day values.
     *
     * @param scripName A comma separated list of one or more scrip names.
     */
    public void addChartEntities( final List<ChartEntityConfig> cfgList ) {

        if( cfgList == null || cfgList.isEmpty() ) {
            return ;
        }

        final List<ChartEntity> entities = new ArrayList<ChartEntity>() ;
        for( final ChartEntityConfig config : cfgList ) {

            if( !this.model.containsEntity( config.getName() ) ) {
                // TODO: This is where we need to associate the UI characteristics
                // with the scrip - color, thickness, stroke. Maybe we can maintain
                // a user preference for the scrip symbol and load it.
                final ChartEntity entity = new ChartEntity( config ) ;
                if( ChartingPanel.colorIndex > CURVE_COLORS.length-1 ) {
                    ChartingPanel.colorIndex = 0 ;
                }
                entity.setColor( CURVE_COLORS[ChartingPanel.colorIndex] ) ;
                ChartingPanel.colorIndex++ ;

                final Stroke stroke = new BasicStroke( 0.4f, BasicStroke.CAP_ROUND,
                                                       BasicStroke.JOIN_ROUND ) ;
                entity.setStroke( stroke ) ;

                entities.add( entity ) ;
            }
        }

        // If we have one or more entities which are not yet in the panel,
        // let's add them to the model.
        if( !entities.isEmpty() ) {
            this.model.addPrimaryEntities( entities ) ;
            this.controlPanel.enableButton( this.model.getFixedTimeRange() ) ;
        }
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
            { AC_SHOW_ITD_PANEL, IMG_SHOW_ITD_PANEL, IMG_SHOW_ITD_PANEL_PRESSED, "ITD Panel" },
            { AC_SHOW_CONTROL,   IMG_SHOW_CONTROL,   IMG_SHOW_CONTROL_PRESSED,   "Show/Hide controls" },
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
        if( actCmd.equals( AC_SHOW_ITD_PANEL ) ) {
            final STViewService viewSvc = ServiceMgr.getSTViewService() ;
            try {
                viewSvc.showITDPanel() ;
            }
            catch ( final STException e1 ) {
                LogMsg.error( "Exception while showing the ITD summary panel" ) ;
                logger.error( "Exception ITD summary panel", e1 ) ;
            }
        }
        else if( actCmd.equals( AC_SHOW_CONTROL ) ) {
            final boolean isVisible = this.controlPanel.isVisible() ;
            this.controlPanel.setVisible( !isVisible ) ;
        }
    }

    /**
     * Initializes the nature of this panel, including setting up appropriate
     * listeners, controller and model of the charting engine.
     */
    @Override
    public void initializeData() throws STException {

        setLayout( new BorderLayout() ) ;
        add( this.controlPanel,     BorderLayout.NORTH ) ;
        add( this.canvas,           BorderLayout.CENTER ) ;

        getDialogManager().setHeaderBackground( Color.BLACK ) ;

        final JLabel headerLabel = getDialogManager().getHeaderPanel( this ) ;
        headerLabel.setFont( CHART_PANEL_HDR_FONT ) ;
        headerLabel.setForeground( Color.LIGHT_GRAY ) ;
    }

    /**
     * This method is invoked on the child panels when the dialog is being
     * disposed as a result of a cancel operation chosen by the user. Subclasses
     * can implement this function to perform cleanup logic like de-registering
     * from the event bus etc.
     */
    public void destroy() {
        this.model.destroy() ;
    }

    /**
     * This method is invoked when the underlying chart model changes. The
     * chart panel takes this opportunity to change the chart title to a more
     * meaningful name, which relates closely to the entities that are managed
     * by the chart.
     */
    @Override
    public void modelChanged( final ModelEvent event ) {

        final ModelEventType eventType = event.getType() ;
        // If a primary entity has been added or removed, modify the title
        // accordingly.
        if( eventType == ModelEventType.PRIMARY_SERIES_ADDED ||
            eventType == ModelEventType.PRIMARY_SERIES_REMOVED ) {

            final String[] entityNames = this.model.getPrimaryEntityNames() ;
            final StringBuffer title   = new StringBuffer() ;
            if( entityNames != null && entityNames.length > 0 ) {
                for( int i=0; i<entityNames.length; i++ ) {
                    title.append( entityNames[i] ) ;
                    if( i < entityNames.length-1 ) {
                        title.append( " / " ) ;
                    }
                }
            }
            else {
                title.append( BASE_TITLE ) ;
            }
            super.getDialogManager().setPanelTitle( this, title.toString() ) ;
        }
        else if ( eventType == ModelEventType.MODEL_DATA_CHANGED ) {
            // If the data has changed and this chart is displaying a single
            // entity, print the latest value statistics (value, percentage
            // change and change amount) in the title.
            if( this.model.getNumPrimaryEntities() == 1 ) {
                String title = "<html><body>" ;
                title += this.model.getPrimaryEntityNames()[0] ;
                title += " " + getSingleEntityStats() ;
                title += "</body></html>" ;
                super.getDialogManager().setPanelTitle( this, title ) ;
            }
        }
    }

    /** Updates the latest values of all the entities being supported by this chart. */
    private String getSingleEntityStats() {

        final Map<ChartEntity, List<EODValue>> renderData = this.model.getRenderData() ;
        final StringBuffer buffer = new StringBuffer() ;

        if( renderData.size() == 1 ) {
            final List<EODValue> eodValues = renderData.values().iterator().next() ;
            if( !eodValues.isEmpty() ) {
                final EODValue eodVal = eodValues.get( eodValues.size()-1 ) ;
                final SortedSet<ITDValue> itdValues = eodVal.getITDValues() ;

                double value     = 0.0 ;
                double pctChange = 0.0 ;
                double change    = 0 ;

                if( itdValues.isEmpty() ) {
                    value = eodVal.getClose() ;
                }
                else {
                    value = itdValues.last().getValue() ;
                }

                pctChange = (( value - eodVal.getPrevClose() )/eodVal.getPrevClose())*100 ;
                change    = value - eodVal.getPrevClose() ;

                buffer.append( "[ " ) ;
                buffer.append( NUMBER_FMT.format( value ) ) ;
                buffer.append( ", " ) ;
                if( change < 0 ) {
                    buffer.append( "<font color=\"red\">" ) ;
                }
                else {
                    buffer.append( "<font color=\"yellow\">" ) ;
                }
                buffer.append( NUMBER_FMT.format( change ) ) ;
                buffer.append( "</font>" ) ;
                buffer.append( ", " ) ;

                if( pctChange < 0 ) {
                    buffer.append( "<font color=\"red\">" ) ;
                }
                else {
                    buffer.append( "<font color=\"yellow\">" ) ;
                }
                buffer.append( NUMBER_FMT.format( pctChange ) ) ;
                buffer.append( "%" ) ;
                buffer.append( "</font>" ) ;
                buffer.append( " ]" ) ;
            }
        }

        return buffer.toString() ;
    }
}
