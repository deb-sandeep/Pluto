/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 15, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr;
import java.awt.BorderLayout ;
import java.awt.Color ;
import java.awt.Component ;
import java.awt.Container ;
import java.awt.Dimension ;
import java.awt.FlowLayout ;
import java.awt.Image ;
import java.awt.event.ActionEvent ;
import java.awt.event.ActionListener ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import javax.swing.AbstractButton ;
import javax.swing.BorderFactory ;
import javax.swing.Icon ;
import javax.swing.ImageIcon ;
import javax.swing.JButton ;
import javax.swing.JFrame ;
import javax.swing.JInternalFrame ;
import javax.swing.JLabel ;
import javax.swing.JOptionPane ;
import javax.swing.JPanel ;
import javax.swing.JTabbedPane ;
import javax.swing.JToggleButton ;
import javax.swing.event.ChangeEvent ;
import javax.swing.event.ChangeListener ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.ui.I18N ;
import com.sandy.apps.pluto.ui.UIConstant ;

/**
 * This internal frame provides a host for aggregating {@link IPlutoFramePanel}
 * instances.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class PlutoInternalFrame extends JInternalFrame
    implements ActionListener, ChangeListener, UIConstant {

    /** Serial Version UID for this class. */
    private static final long serialVersionUID = -6835715657639737793L ;

    /** Logger instance for this class. */
    private static final Logger logger = Logger.getLogger( PlutoInternalFrame.class ) ;

    /**
     * Static constant to qualify a toggle button. The concrete dialog
     * managers can use this to quality the action command, based on which
     * appropriate type of buttons would be displayed.
     */
    public static final String BTN_TYPE_TOGGLE = "T:" ;

    /** A reference to the tab pane that will host the config panels. */
    private JTabbedPane tabbedPane = null ;

    /**
     * A variable storing the currently active tab. This will help us roll
     * back to the old tab once the user switches dirty tabs and wants to come
     * back to save the dirty one.
     */
    private int lastSelTabIndex = -1 ;

    /** The config panels that are a part of this wizard. */
    private IPlutoFramePanel[] configPanels = null ;

    /** An array of JPanels corresponding to the wizard header for panels. */
    private JPanel[] panelHeaders = null ;

    /** The default close operation. */
    private int defaultCloseOp = JFrame.DISPOSE_ON_CLOSE ;

    /** The standard buttons that will be added to a panel header always. */
    final private static Object COMMON_BTN_ATTRS[][] = new Object[][] {
        { AC_MINIMIZE,       IMG_MINIMIZE, IMG_MINIMIZE_PRESSED, "Minimize" },
        { AC_CFG_WIZ_CANCEL, IMG_CANCEL,   IMG_CANCEL_PRESSED,   "Close" }
    } ;

    /**
     * This data structure book keeps the panel button information and is
     * looked up to zero in into a particular button, when enabling or
     * disabling requests are processed by this class.
     */
    private final Map<String, Map<String, AbstractButton>> panelBtnMap =
                             new HashMap<String, Map<String,AbstractButton>>() ;

    /**
     * The data structure for book keeping the panel names versus their title
     * labels. This will be used to change the title of the panel during
     * runtime. Please note that the name of the panel should not change. The
     * panel should explicitly call on the dialog manager to change the title
     * if required.
     */
    private final Map<String, JLabel> panelTitleMap = new HashMap<String, JLabel>() ;

    /** The type of the dialog. */
    private final PlutoFrameType dialogType ;

    /**
     * Public constructor.
     *
     * @param title The title of this configuration wizard.
     * @param defaultCloseOp The action to invoke when the user chooses cancel.
     *        The value of this is the same as that of JFrame.defaultCloseOperation
     * @param dialogType The type of the dialog.
     * @param panels A variable number of dialog panels which will be appended
     *        to the dialog.
     */
    public PlutoInternalFrame( final String title, final int defaultCloseOp,
                          final PlutoFrameType dialogType, final IPlutoFramePanel... panels )
        throws STException {

        super( title ) ;
        this.dialogType = dialogType ;

        if( panels == null || panels.length==0 ) {
            throw new IllegalArgumentException( "No config panels specified." ) ;
        }

        // Create a cross relationship between the panels and the dialog manager.
        for( final IPlutoFramePanel panel : panels ) {
            panel.setDialogManager( this ) ;
        }

        createPanelHeaders( panels ) ;
        this.configPanels = panels ;
        this.defaultCloseOp = defaultCloseOp ;
        setUpUI() ;
    }

    /**
     * Creates panel headers with the panel name and the preferred list of
     * icons for each panel.
     */
    private void createPanelHeaders( final IPlutoFramePanel[] panels ) {

        this.panelHeaders = new JPanel[ panels.length ] ;
        for( int panelIndex=0; panelIndex < panels.length; panelIndex++ ) {

            final IPlutoFramePanel panel = panels[ panelIndex ] ;
            final JPanel hdrPanel = new JPanel() ;
            hdrPanel.setLayout( new BorderLayout() ) ;

            // Create the label
            final JLabel titleLabel = new JLabel() ;
            titleLabel.setFont(new java.awt.Font("Lucida Handwriting", 1, 12));
            titleLabel.setForeground( Color.BLUE );
            titleLabel.setDoubleBuffered( true ) ;
            titleLabel.setOpaque( true ) ;
            titleLabel.setText( " " + panel.getName() );

            // Add the title to the internal data structure, so that we can
            // update the title at runtime.
            this.panelTitleMap.put( panel.getName(), titleLabel ) ;

            // add the label to the panel with left alignment
            hdrPanel.add( titleLabel, BorderLayout.WEST ) ;

            // Create the buttons and add them progressively to the right
            final JPanel btnPanel = new JPanel() ;
            btnPanel.setLayout( new FlowLayout( FlowLayout.RIGHT, 1, 0 ) ) ;

            final Map<String, AbstractButton> btnMap = new HashMap<String, AbstractButton>() ;
            final Object[][] btnAttrs = panel.getPanelIcons() ;
            // If a panel does not need any panel specific buttons, it will
            // return a null value - honor it.
            if( btnAttrs != null ) {
                AbstractButton btn = null ;
                for( int i=0; i<btnAttrs.length; i++ ) {
                    btn = addHeaderBtn( btnAttrs[i], btnPanel, panel ) ;
                    btnMap.put( btn.getActionCommand(), btn ) ;
                }
            }
            this.panelBtnMap.put( panel.getName(), btnMap ) ;

            // Add the common buttons to all the panels. The action listener
            // of these common buttons are the wizard and not the individual
            // panels.
            for( int i=0; i<COMMON_BTN_ATTRS.length; i++ ) {
                addHeaderBtn( COMMON_BTN_ATTRS[i], btnPanel, this ) ;
            }

            hdrPanel.add( btnPanel, BorderLayout.EAST ) ;
            this.panelHeaders[panelIndex] = hdrPanel ;
        }
    }

    /**
     * Adds a button with the given attributes to the header and associated
     * the listener with the button
     *
     * @param btnAttrs The attributes of the button to be added.
     * @param btnPanel The panel into which the buttons are to be added.
     *
     * @return Returns the button that was added based on the configuration
     */
    private AbstractButton addHeaderBtn( final Object[] btnAttrs,
                                         final JPanel btnPanel,
                                         final ActionListener actListener ) {

        AbstractButton btn = null ;
        String actCmd = ( String )btnAttrs[0] ;
        final Icon btnIcon = new ImageIcon( ( Image )btnAttrs[1] ) ;
        final Icon selIcon = new ImageIcon( ( Image )btnAttrs[2] ) ;

        if( actCmd.startsWith( BTN_TYPE_TOGGLE ) ) {
            actCmd = actCmd.substring( BTN_TYPE_TOGGLE.length() ) ;
            btn = new JToggleButton( btnIcon ) ;
            btn.setSelectedIcon( selIcon ) ;
        }
        else {
            btn = new JButton( btnIcon ) ;
            btn.setPressedIcon( selIcon ) ;
        }

        btn.setActionCommand( actCmd ) ;
        btn.setPreferredSize( new Dimension( 18, 18 ) ) ;
        btn.setContentAreaFilled( false ) ;
        btn.setBorderPainted( false ) ;
        btn.setFocusPainted( false ) ;

        if( btn.getActionCommand().equals( AC_CFG_WIZ_OK ) ) {
            btn.addActionListener( this ) ;
        }
        else {
            btn.addActionListener( actListener ) ;
        }

        if( btnAttrs.length > 3 ) {
            btn.setToolTipText( (String)btnAttrs[3] ) ;
        }
        btnPanel.add( btn, btnPanel.getComponentCount() ) ;

        return btn ;
    }

    /** Sets the background of all the panel headers to the specified color. */
    public void setHeaderBackground( final Color hdrBg ) {
        for( final JPanel panel : this.panelHeaders ) {
            panel.setBackground( hdrBg ) ;
            final Component[] children = panel.getComponents() ;
            for( final Component child : children ) {
                child.setBackground( hdrBg ) ;
            }
        }
    }

    /**
     * A helper method to set up the UI. If there are multiple panels, a tabbed
     * pane will be used to house those components else if there is only one
     * config panel, it will occupy the center portion of this dialog.
     */
    private void setUpUI() throws STException {

        final Container contentPane = super.getContentPane() ;
        setLayout( new BorderLayout() ) ;
        setBorder( BorderFactory.createLineBorder( Color.lightGray, 1 ) ) ;
        setMaximizable( true ) ;
        setIconifiable( true ) ;
        setResizable  ( true ) ;
        setClosable   ( false ) ;
        setDefaultCloseOperation( JInternalFrame.DISPOSE_ON_CLOSE ) ;
        putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);

        // Now initialize the config panels
        for( final IPlutoFramePanel panel : this.configPanels ) {
            panel.initializeData() ;
        }

        if( this.configPanels.length > 1 ) {
            this.tabbedPane = new JTabbedPane() ;
            this.tabbedPane.setFont( UIConstant.DLG_FONT_BOLD ) ;
            this.tabbedPane.addChangeListener( this ) ;

            for( final IPlutoFramePanel panel : this.configPanels ) {
                this.tabbedPane.add( panel.getName(), (JPanel)panel ) ;
            }
            this.tabbedPane.setSelectedIndex( 0 ) ;
            this.lastSelTabIndex = 0 ;

            contentPane.add( this.tabbedPane, BorderLayout.CENTER ) ;
        }
        else {
            contentPane.add( (JPanel)this.configPanels[0], BorderLayout.CENTER ) ;
            contentPane.add( this.panelHeaders[0], BorderLayout.NORTH ) ;
        }
    }

    /**
     * This method is invoked when the user selects either the OK or the
     * Cancel button on the wizard. Please note that the OK and Cancel actions
     * are always applicable on the currently active tab.
     */
    @Override
    public void actionPerformed( final ActionEvent e ) {

        final String actCmd = e.getActionCommand() ;
        if( actCmd.equals( UIConstant.AC_CFG_WIZ_CANCEL ) ) {
            if( this.defaultCloseOp != JFrame.DO_NOTHING_ON_CLOSE ) {
                if( this.defaultCloseOp == JFrame.DISPOSE_ON_CLOSE ) {
                    for( final IPlutoFramePanel panel : this.configPanels ) {
                        panel.destroy() ;
                    }
                    setVisible( false ) ;
                    EventBus.publish( EventType.INTERNAL_FRAME_CLOSED, this ) ;
                    dispose() ;
                }
                else if( this.defaultCloseOp == JFrame.HIDE_ON_CLOSE ) {
                    setVisible( false ) ;
                }
            }
            return ;
        }
        else if( actCmd.equals( UIConstant.AC_MINIMIZE ) ) {
            setVisible( false ) ;
        }

        IPlutoFramePanel cfgPanel = null ;
        if( this.configPanels.length > 1 ) {
            // If we have more than one configuration panels, we are dealing
            // with a tab pane. Get the panel which is currently active.
            final Component activeComp = this.tabbedPane.getSelectedComponent() ;
            cfgPanel = ( IPlutoFramePanel )activeComp ;
        }
        else {
            cfgPanel = this.configPanels[0] ;
        }

        if( cfgPanel.isDirty() ) {
            try {
                final List<String> msgs = cfgPanel.validateUserInput() ;
                if( msgs != null && !msgs.isEmpty() ) {
                    final StringBuffer buffer = new StringBuffer() ;
                    buffer.append( "Invalid input\n" ) ;
                    for( final String msg : msgs ) {
                        buffer.append( "=> " + msg + "\n" ) ;
                    }
                    JOptionPane.showInternalMessageDialog( this, buffer,
                            "Invalid input", JOptionPane.ERROR_MESSAGE ) ;
                }
                else {
                    cfgPanel.save() ;
                    if( this.tabbedPane != null ) {
                        this.tabbedPane.setTitleAt( this.lastSelTabIndex,
                                                    cfgPanel.getName() ) ;
                    }
                }
            }
            catch ( final STException saveEx ) {
                logger.error( "Save failure", saveEx ) ;
                JOptionPane.showInternalMessageDialog( this,
                        I18N.MSG_SAVE_FAILURE + ". Msg = " + saveEx.getMessage(),
                        "Save Failure", JOptionPane.ERROR_MESSAGE ) ;
            }
        }
    }

    /**
     * This method is invoked when the user changes the tab selection. We
     * first check if the currently active tab is dirty and if so, ask the user
     * for saving it first before proceeding to the next tab.
     */
    @Override
    public void stateChanged( final ChangeEvent e ) {

        // Get a reference to the last tab that was selected, and ascertain
        // if the tab is dirty - if so, we add a '*' to the tab name to
        // indicate its dirty status.
        if( this.lastSelTabIndex != -1 ) {
            IPlutoFramePanel panel = null ;
            panel = ( IPlutoFramePanel )this.tabbedPane.getComponentAt( this.lastSelTabIndex ) ;
            if( panel.isDirty() ) {
                this.tabbedPane.setTitleAt( this.lastSelTabIndex, panel.getName() + " *" ) ;
            }
            else {
                this.tabbedPane.setTitleAt( this.lastSelTabIndex, panel.getName() ) ;
            }
        }

        this.lastSelTabIndex = this.tabbedPane.getSelectedIndex() ;
        final Container contentPane = super.getContentPane() ;
        final Component[] components = contentPane.getComponents() ;
        for( final Component comp : components ) {
            if( comp instanceof JPanel ) {
                contentPane.remove( comp ) ;
            }
        }
        contentPane.add( this.panelHeaders[ this.lastSelTabIndex ], BorderLayout.NORTH ) ;
        contentPane.validate() ;
        contentPane.repaint() ;
    }

    /**
     * Enables or disables the panel button with the specified action command,
     * based on the value of the enable flag.
     *
     * @param panel The panel for whose buttons need to be enabled or disabled
     * @param actionCmd The action command identifying the button
     * @param enable If true, the button is enabled, else disabled.
     */
    public void enablePanelBtn( final IPlutoFramePanel panel,
                                final String actionCmd, final boolean enable ) {

        final Map<String, AbstractButton> btnMap =
                                       this.panelBtnMap.get( panel.getName() ) ;
        final AbstractButton button = btnMap.get( actionCmd ) ;
        button.setEnabled( enable ) ;
    }

    /**
     * Returns the current selection state of the specified button in the given
     * panel.
     *
     * @param panel The panel in which the button is contained.
     * @param actionCmd The action command identifying the button
     *
     * @return true if the button is selected/enabled, false otherwise.
     */
    public boolean isBtnSelected( final IPlutoFramePanel panel,
                                  final String actionCmd ) {
        final Map<String, AbstractButton> btnMap =
                                       this.panelBtnMap.get( panel.getName() ) ;
        final AbstractButton button = btnMap.get( actionCmd ) ;
        boolean state = false ;
        if( button != null ) {
            if( button.isEnabled() ) {
                state = button.isSelected() ;
            }
        }
        return state ;
    }

    /**
     * Changes the title of the panel at runtime to the specified value.
     *
     * @param panel The panel whose title has to be changed.
     * @param title The new title that needs to be put into place.
     */
    public void setPanelTitle( final IPlutoFramePanel panel, final String title ) {

        final JLabel titleLabel = this.panelTitleMap.get( panel.getName() ) ;
        titleLabel.setText( title ) ;
        titleLabel.repaint() ;
    }

    /** Returns the current title for the given panel. */
    public String getPanelTitle( final IPlutoFramePanel panel ) {

        final JLabel titleLabel = this.panelTitleMap.get( panel.getName() ) ;
        if( titleLabel != null ) {
            return titleLabel.getText() ;
        }
        return null ;
    }

    /**
     * Returns the header label associated with the panel. The returned header
     * label can be used for customizing the fonts and color by the individual
     * participant panels.
     */
    public JLabel getHeaderPanel( final IPlutoFramePanel panel ) {
        return this.panelTitleMap.get( panel.getName() ) ;
    }

    /** Returns the type of the dialog. */
    public PlutoFrameType getDialogType() {
        return this.dialogType ;
    }

    /**
     * Returns the active title of the dialog panel. Note that a dialog manager
     * can have multiple panels in a tabbed pane, with each panel having it's
     * own title. At any point in time, the title of the dialog manager is the
     * title of the active panel. Note that some panels have dynamic title strings,
     * which change over time.
     */
    public String getActiveTitle() {

        String titleName   = null ;
        JLabel panelHeader = null ;
        IPlutoFramePanel activePanel = null ;

        if( this.configPanels.length > 1 ) {
            // If we have more than one panel, the active title is the title of
            // the active panel.
            final int selIndex = this.tabbedPane.getSelectedIndex() ;
            activePanel  = ( IPlutoFramePanel )this.tabbedPane.getComponentAt( selIndex ) ;
        }
        else {
            // If we are dealing with only one panel - the title of the panel
            // is the active title.
            activePanel = this.configPanels[0] ;
        }

        // Give a chance to the panel to return the title. This is important in
        // cases where different instances of the panel have the same title
        // but have different qualifiers for identification. For example, in case
        // of scrip ITD panels, the title might be the query string used to
        // filter ITD data.
        titleName = activePanel.getTitle() ;
        if( titleName == null ) {
            panelHeader = this.panelTitleMap.get( activePanel.getName() ) ;
            titleName = panelHeader.getText() ;
        }

        return titleName ;
    }
}
