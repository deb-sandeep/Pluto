/**
 * 
 * 
 * 
 *
 * Creation Date: Oct 17, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.charting;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.util.util.StringUtil ;
import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartEntity ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartModel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartModel.ModelEvent ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartModel.ModelListener ;
import com.sandy.apps.pluto.ui.util.ColorIcon ;

/**
 * This panel is added to the top of the charting window and contains controls
 * for adding/removing/editing derived entities. This panel is called upon
 * by the model for any changes to the chart participants and this panel reacts
 * accordingly.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ChartControlPanel extends JPanel
    implements UIConstant, ModelListener, ActionListener {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 561684807893431129L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ChartControlPanel.class ) ;

    /** The font for fixed time range buttons. */
    private static final Font TIME_RANGE_FONT = new Font( "Lucidia Console", Font.PLAIN, 9 ) ;

    /** A reference to the chart model. */
    private final ChartModel model ;

    /** A reference to the chart canvas. */
    private final ChartCanvas canvas ;

    /** The JComboBox containing a listing of the primary chart entities. */
    private JComboBox primaryEntityCombo = null ;

    /** The button to remove the selected primary entity. */
    private JButton entityRemoveBtn = null ;

    /**
     * The toggle button with which the user chooses whether to show the
     * volume chart or not.
     */
    private JToggleButton showVolChartBtn = null ;

    /** The toggle buttons representing fixed range markers. */
    private JToggleButton btn1D = null ;
    private JToggleButton btn2D = null ;
    private JToggleButton btn3D = null ;
    private JToggleButton btn4D = null ;
    private JToggleButton btn5D = null ;
    private JToggleButton btn2W = null ;
    private JToggleButton btn1M = null ;
    private JToggleButton btn3M = null ;
    private JToggleButton btn6M = null ;
    private JToggleButton btn1Y = null ;
    private JToggleButton btn2Y = null ;
    private JToggleButton btn5Y = null ;
    private JToggleButton btn10Y = null ;

    /** A map to store the range toggle buttons to implement a button group. */
    private final Map<String, JToggleButton> toggleBtnMap = new HashMap<String, JToggleButton>() ;

    /** A map storing the mapping between the entity names and the entities. */
    private final Map<String, ChartEntity> entityMap = new HashMap<String, ChartEntity>() ;

    /**
     * Constructor which sets up the UI. This constructor also accepts a reference
     * to the chart model, which can be queried for specified data once a
     * model notification is received.
     */
    public ChartControlPanel( final ChartModel model,
                              final ChartCanvas canvas ) {
        super() ;
        this.model = model ;
        this.canvas = canvas ;
        setUpUI() ;
    }

    /** A private helper method to set up the user interface for this panel. */
    private void setUpUI() {

        setLayout( new BorderLayout( 0, 0 ) ) ;
        setBorder( BorderFactory.createEmptyBorder() ) ;

        final JPanel leftPanel = createLeftPanel() ;
        final JPanel rightPanel= createRightPanel() ;

        // Add the left and right panels to this panel.
        add( leftPanel, BorderLayout.CENTER ) ;
        add( rightPanel,BorderLayout.EAST ) ;
    }

    /** Creates the left control panel containing the combo boxes. */
    private JPanel createLeftPanel() {

        final JPanel leftPanel = new JPanel() ;

        leftPanel.setLayout( new FlowLayout( FlowLayout.LEFT, 5, 0 ) ) ;
        leftPanel.setBorder( BorderFactory.createEmptyBorder() ) ;
        leftPanel.setBackground( Color.black ) ;

        // Set up the primary entity combo box
        this.primaryEntityCombo = getComboBox() ;
        leftPanel.add( this.primaryEntityCombo ) ;

        // Set up the entity removal button
        this.entityRemoveBtn = createButton( IMG_DELETE, IMG_DELETE_PRESSED, AC_DELETE ) ;
        leftPanel.add( this.entityRemoveBtn ) ;

        // Add the volume hide show button
        this.showVolChartBtn = createToggleButton( UIConstant.IMG_SHOW_VOL_CHART,
                                                   UIConstant.AC_SHOW_HIDE_VOL_CHART ) ;
        this.showVolChartBtn.setEnabled( false ) ;
        this.showVolChartBtn.setSelectedIcon( new ImageIcon( UIConstant.IMG_HIDE_VOL_CHART ) ) ;
        this.showVolChartBtn.setToolTipText( "Show/Hide volume graph" ) ;

        leftPanel.add( this.showVolChartBtn ) ;

        return leftPanel ;
    }

    /** Creates the right control panel containing the range markers. */
    private JPanel createRightPanel() {

        final JPanel rightPanel = new JPanel() ;
        rightPanel.setLayout( new FlowLayout( FlowLayout.RIGHT, 0, 0 ) ) ;
        rightPanel.setBorder( BorderFactory.createEmptyBorder() ) ;
        rightPanel.setBackground( Color.black ) ;

        this.btn1D = createToggleButton( "1d", AC_TIME_RANGE_1D ) ;
        this.btn2D = createToggleButton( "2d", AC_TIME_RANGE_2D ) ;
        this.btn3D = createToggleButton( "3d", AC_TIME_RANGE_3D ) ;
        this.btn4D = createToggleButton( "4d", AC_TIME_RANGE_4D ) ;
        this.btn5D = createToggleButton( "5d", AC_TIME_RANGE_5D ) ;
        this.btn2W = createToggleButton( "2w", AC_TIME_RANGE_2W ) ;
        this.btn1M = createToggleButton( "1m", AC_TIME_RANGE_1M ) ;
        this.btn3M = createToggleButton( "3m", AC_TIME_RANGE_3M ) ;
        this.btn6M = createToggleButton( "6m", AC_TIME_RANGE_6M ) ;
        this.btn1Y = createToggleButton( "1y", AC_TIME_RANGE_1Y ) ;
        this.btn2Y = createToggleButton( "2y", AC_TIME_RANGE_2Y ) ;
        this.btn5Y = createToggleButton( "5y", AC_TIME_RANGE_5Y ) ;
        this.btn10Y= createToggleButton( "10y",AC_TIME_RANGE_10Y ) ;

        this.toggleBtnMap.put( RANGE_1D, this.btn1D ) ;
        this.toggleBtnMap.put( RANGE_2D, this.btn2D ) ;
        this.toggleBtnMap.put( RANGE_3D, this.btn3D ) ;
        this.toggleBtnMap.put( RANGE_4D, this.btn4D ) ;
        this.toggleBtnMap.put( RANGE_5D, this.btn5D ) ;
        this.toggleBtnMap.put( RANGE_2W, this.btn2W ) ;
        this.toggleBtnMap.put( RANGE_1M, this.btn1M ) ;
        this.toggleBtnMap.put( RANGE_3M, this.btn3M ) ;
        this.toggleBtnMap.put( RANGE_6M, this.btn6M ) ;
        this.toggleBtnMap.put( RANGE_1Y, this.btn1Y ) ;
        this.toggleBtnMap.put( RANGE_2Y, this.btn2Y ) ;
        this.toggleBtnMap.put( RANGE_5Y, this.btn5Y ) ;
        this.toggleBtnMap.put( RANGE_10Y,this.btn10Y ) ;

        // During creation of the panel all the buttons are disabled. They
        // are enabled only when one or more primary entities are being
        // managed by the chart model.
        setRangeButtonsEnabled( false ) ;

        rightPanel.add( this.btn1D ) ;
        rightPanel.add( this.btn2D ) ;
        rightPanel.add( this.btn3D ) ;
        rightPanel.add( this.btn4D ) ;
        rightPanel.add( this.btn5D ) ;
        rightPanel.add( this.btn2W ) ;
        rightPanel.add( this.btn1M ) ;
        rightPanel.add( this.btn3M ) ;
        rightPanel.add( this.btn6M ) ;
        rightPanel.add( this.btn1Y ) ;
        rightPanel.add( this.btn2Y ) ;
        rightPanel.add( this.btn5Y ) ;
        rightPanel.add( this.btn10Y ) ;

        // All charts are intra day to start with.
        enableButton( RANGE_1D ) ;

        return rightPanel ;
    }

    /**
     * Enables the toggle button with the given range identifier. This function
     * deselects all the other toggle buttons.
     *
     * @param rangeId The range identifier for which the toggle button needs
     *        to be selected.
     */
    public void enableButton( final String rangeId ) {

        for( final String id : this.toggleBtnMap.keySet() ) {
            final JToggleButton tBtn = this.toggleBtnMap.get( id ) ;
            if( id.equals( rangeId ) ) {
                tBtn.setSelected( true ) ;
            }
            else {
                tBtn.setSelected( false ) ;
            }
        }
    }

    /** Configures a button for use in this panel. A refactored method. */
    private void configureButton( final AbstractButton button ) {

        button.setBackground( Color.black ) ;
        button.addActionListener( this ) ;
        button.setContentAreaFilled( true ) ;
        button.setBorderPainted( false ) ;
        button.setFocusPainted( false ) ;
        button.setIconTextGap( 0 ) ;
        button.setPreferredSize( new Dimension( 20, 17 ) ) ;
        button.setMargin( new Insets(0,0,0,0) ) ;
        button.setBorder( EMPTY_BORDER ) ;
    }

    /**
     * Creates and returns a new button as per the UI requirements. The created
     * button delegates its actions to this class.
     */
    private JToggleButton createToggleButton( final String text, final String actCmd ) {
        final JToggleButton button = new JToggleButton() ;

        configureButton( button ) ;
        button.setText( text ) ;
        button.setForeground( Color.gray ) ;
        button.setFont( TIME_RANGE_FONT ) ;
        button.setActionCommand( actCmd ) ;

        return button ;
    }

    /**
     * Creates and returns a new button as per the UI requirements. The created
     * button delegates its actions to this class.
     */
    private JToggleButton createToggleButton( final Image image, final String actCmd ) {
        final JToggleButton button = new JToggleButton() ;

        configureButton( button ) ;
        button.setActionCommand( actCmd ) ;
        button.setIcon( new ImageIcon( image ) ) ;
        button.setPreferredSize( new Dimension( 17, 17 ) ) ;

        return button ;
    }

    /** Enables or disables all the range toggle buttons. */
    private void setRangeButtonsEnabled( final boolean enabled ) {
        for( final JToggleButton btn : this.toggleBtnMap.values() ) {
            btn.setEnabled( enabled ) ;
        }
    }

    /**
     * Creates and returns a new button as per the UI requirements. The created
     * button delegates its actions to this class.
     */
    private JButton createButton( final Image image, final Image pressedImg,
                                  final String actCmd ) {
        final JButton button = new JButton() ;

        configureButton( button ) ;
        button.setIcon( new ImageIcon( image ) ) ;
        button.setPressedIcon( new ImageIcon( pressedImg ) ) ;
        button.setActionCommand( actCmd ) ;

        return button ;
    }

    /**
     * Creates and returns a new combo box as per the UI requirements. Note that
     * the combo box that is returned is purely visual, implying that it has
     * no wirings to listeners or model. The caller function needs to take the
     * combo box and do the logical wiring.
     */
    private JComboBox getComboBox() {

        final JComboBox combo = new JComboBox() ;

        combo.setBackground( Color.DARK_GRAY ) ;
        combo.setForeground( Color.WHITE ) ;
        combo.setFont( LOG_FONT ) ;
        combo.setMaximumRowCount( 10 ) ;
        combo.setRenderer( new DefaultListCellRenderer() {

            private static final long serialVersionUID = 1L ;

            @Override
            public Component getListCellRendererComponent( final JList list,
                                      final Object val,  final int index,
                                      final boolean sel, final boolean focus ) {

                final JLabel label = ( JLabel )super.getListCellRendererComponent(
                                                list, val, index, sel, focus ) ;
                // Create an thin border for the rendered label - to save
                // on real estate. Remember, we are very stingy for real estate.
                label.setBorder( BorderFactory.createEmptyBorder(1,0,0,0) ) ;
                label.setBackground( Color.DARK_GRAY ) ;
                label.setForeground( Color.LIGHT_GRAY ) ;

                final String entityName = ( String )val ;
                // When we delete the last entity in the combo, Swing calls on
                // us to render a cell with a null value.
                if( entityName != null ) {
                    final ChartEntity entity= ChartControlPanel.this.entityMap.get( entityName ) ;
                    label.setIcon( new ColorIcon( entity.getColor() ) ) ;
                }
                return label ;
            }
        } ) ;
        combo.setPreferredSize( new Dimension( 100, 15 ) ) ;
        return combo ;
    }

    /**
     * This method is invoked when the underlying chart model has changed. The
     * nature of the change can be ascertained by evaluating the type of the
     * event being generated.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void modelChanged( final ModelEvent event ) {

        switch( event.getType() ) {
            case PRIMARY_SERIES_ADDED:
                final List<ChartEntity> entities = ( List<ChartEntity> )event.getValue() ;
                for( final ChartEntity entity : entities ) {
                    this.entityMap.put( entity.getName(), entity ) ;
                    this.primaryEntityCombo.addItem( entity.getName() ) ;
                    this.primaryEntityCombo.setSelectedItem( entity.getName() ) ;
                }
                if( this.model.getNumPrimaryEntities() > 0 ) {
                    setRangeButtonsEnabled( true ) ;
                }

                break ;

            case PRIMARY_SERIES_REMOVED:
                final String series = ( String )event.getValue() ;
                this.primaryEntityCombo.removeItem( series ) ;
                this.entityMap.remove( series ) ;
                if( this.model.getNumPrimaryEntities() == 0 ) {
                    setRangeButtonsEnabled( false ) ;
                }

                break ;
        }

        final boolean enabled = this.model.isDataCondusiveForVolumeGraph() ;
        this.showVolChartBtn.setEnabled( enabled ) ;
        this.showVolChartBtn.setSelected( enabled ) ;
    }

    /**
     * This method is called when any of actions associated with this panel
     * is invoked by the user. Typically this method categorizes the actions
     * and delegates the processing to the underlying model, which can possibly
     * call back modelChanged if the action resulted in a model change.
     */
    @Override
    public void actionPerformed( final ActionEvent e ) {

        final String actCmd = e.getActionCommand() ;
        if( actCmd.equals( AC_DELETE ) ) {
            final String selItem = ( String )this.primaryEntityCombo.getSelectedItem() ;
            if( StringUtil.isNotEmptyOrNull( selItem ) ) {
                this.model.deletePrimaryEntity( selItem ) ;
            }
        }
        else if( actCmd.startsWith( AC_TIME_RANGE ) ) {

            // Deduce the time range from the button's action command.
            String range = actCmd.substring( AC_TIME_RANGE.length() ) ;

            // Get a reference to the button which generated the event.
            final JToggleButton btn = this.toggleBtnMap.get( range ) ;

            if( btn.isSelected() ) {
                // If the button has been selected, we need to unselect all the
                // other buttons in the range. Remember, we are dealing with
                // isolated togglebuttons which don't belong to a group. Hence
                // Swing can't help us get the button group functionality. We
                // will have to devise it ourselves
                for( final JToggleButton tBtn : this.toggleBtnMap.values() ) {
                    if( tBtn != btn ) {
                        tBtn.setSelected( false ) ;
                    }
                }
            }
            else {
                // If we have unselected the button, switch the default range to
                // 1D.
                range = RANGE_1D ;
                this.btn1D.setSelected( true ) ;
            }

            this.model.changeTimeRange( range ) ;
        }
        else if( actCmd.equals( AC_SHOW_HIDE_VOL_CHART ) ) {
            this.canvas.setShowVolGraph( this.showVolChartBtn.isSelected() ) ;
        }
    }
}
