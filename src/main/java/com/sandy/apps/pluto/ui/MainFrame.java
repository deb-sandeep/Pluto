/**
 * Creation Date: Aug 12, 2008
 */

package com.sandy.apps.pluto.ui;
import java.awt.BorderLayout ;
import java.awt.Color ;
import java.awt.Container ;
import java.awt.Dimension ;
import java.awt.Toolkit ;

import javax.swing.BorderFactory ;
import javax.swing.JFrame ;
import javax.swing.JMenu ;
import javax.swing.JMenuBar ;
import javax.swing.JMenuItem ;
import javax.swing.JToolBar ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.Initializable ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.STUtils ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.util.util.BizObjectFactory ;
import com.sandy.apps.pluto.shared.util.util.SpringObjectFactory ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoInternalFrame ;
import com.sandy.apps.pluto.ui.statusbar.StatusBar ;
import com.sandy.apps.pluto.ui.svc.STViewService ;
import com.sandy.apps.pluto.ui.util.PlutoDesktopPane ;

/**
 * This is the main window of the Pluto application. Since the application runs
 * as a service, the visibility of this window will be managed by user's action
 * on the system tray icon.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class MainFrame extends JFrame implements Initializable {

    /** Generated serial version UID. */
    private static final long serialVersionUID = -4642449569928125446L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( MainFrame.class ) ;

    /** The X delta offset to use for adding new internal frames. */
    private static final int X_DELTA_OFFSET = 40 ;

    /** The Y delta offset to use for adding new internal frames. */
    private static final int Y_DELTA_OFFSET = 50 ;

    /** The menu bar for the console. */
    private JMenuBar menuBar = null ;

    /** The toolbar for the console. */
    private JToolBar toolBar = null ;

    /** The status bar for the console. */
    private final StatusBar statusBar = new StatusBar() ;

    /** The desktop pane for the main console where child windows will reside. */
    private final PlutoDesktopPane desktop = new PlutoDesktopPane( UIHelper.getImage( "desktop_logo.png" ) ) ;

    /** The last X, Y location of the internal frame added. */
    private int lastX, lastY = 0 ;

    /** Public constructor. */
    public MainFrame() {
        super() ;
    }

    /**
     * This class needs to be externally initialized post construction and
     * prior to usage. This method sets up the frame by setting it's user
     * interface and aggregating the contents of this frame.
     */
    public void initialize() throws STException {

        // Set frame characteristics.
        setTitle( I18N.APP_TITLE ) ;
        setSize( Toolkit.getDefaultToolkit().getScreenSize() ) ;
        setDefaultCloseOperation( HIDE_ON_CLOSE ) ;
        setExtendedState( JFrame.MAXIMIZED_BOTH ) ;
        setVisible( false ) ;
        setIconImage( UIHelper.IMG_APP ) ;

        // Initialize the menu service and the event menu enabler
        ServiceMgr.getMenuManager().initialize() ;
        ServiceMgr.getMenuEnabler().initialize() ;

        // Initialize the UI components
        initializeMenuBar() ;
        initializeToolBar() ;
        initializeStatusBar() ;
        initializeDesktop() ;

        // Set up the logging window so that it starts capturing all the log
        // messages.
        final SpringObjectFactory bizOF = BizObjectFactory.getInstance() ;
        final STViewService viewSvc = ( STViewService )bizOF.getBean( "STViewService" ) ;
        viewSvc.showLogWindow() ;
        viewSvc.hideLogWindow() ;

        // Aggregate the components into the desired console layout.
        layoutComponents() ;

        // Make things simple for development mode. In case of development mode
        // don't hide the window to start off with.
        if( STUtils.isDevMode() ) {
            logger.info( "Starting Pluto in DEV mode" ) ;
            setVisible( true ) ;
            setDefaultCloseOperation( EXIT_ON_CLOSE ) ;
        }
        else {
            logger.info( "Starting Pluto in PROD mode" ) ;
        }
    }

    /** Lays out the components for the main console window. */
    private void layoutComponents() {

        final Container contentPane = super.getContentPane() ;
        contentPane.setLayout( new BorderLayout() ) ;

        setJMenuBar( this.menuBar ) ;
        contentPane.add( this.toolBar,   BorderLayout.EAST ) ;
        contentPane.add( this.desktop,   BorderLayout.CENTER ) ;
        contentPane.add( this.statusBar, BorderLayout.SOUTH ) ;
    }

    /** Private helper method to initialize the console Desktop */
    private void initializeDesktop() {
        this.desktop.setBackground( Color.BLACK ) ;
    }

    /** Private helper method to initialize the console Status Bar */
    private void initializeStatusBar() throws STException {
        // Initialize the status bar
        this.statusBar.initialize() ;
    }

    /** Private helper method to initialize the console Tool Bar */
    private void initializeToolBar() {
        this.toolBar = ServiceMgr.getMenuManager().getToolBar() ;
        this.toolBar.setName( I18N.LBL_TOOLBAR_NAME ) ;
        this.toolBar.setOrientation( JToolBar.VERTICAL ) ;
        this.toolBar.setFloatable( false ) ;
        this.toolBar.setBackground( Color.BLACK ) ;
    }

    /** Private helper method to initialize the console Menu Bar */
    private void initializeMenuBar() {
        this.menuBar = ServiceMgr.getMenuManager().getMenuBar() ;
        this.menuBar.setBackground( Color.black ) ;
        this.menuBar.setBorder( BorderFactory.createEmptyBorder() ) ;
        this.menuBar.setFont( UIConstant.LOG_FONT_BOLD ) ;
        this.menuBar.setForeground( Color.LIGHT_GRAY ) ;
        this.menuBar.setOpaque( true ) ;

        final int menuCount = this.menuBar.getMenuCount() ;
        for( int i=0; i<menuCount; i++ ) {

            final JMenu menu = this.menuBar.getMenu( i ) ;
            menu.setBackground( Color.black ) ;
            menu.setBorder( BorderFactory.createEmptyBorder() ) ;
            menu.setBorderPainted( false ) ;
            menu.setFont( UIConstant.LOG_FONT_BOLD ) ;
            menu.setForeground( Color.LIGHT_GRAY ) ;
            menu.setOpaque( true ) ;

            final int numMI = menu.getMenuComponentCount() ;
            for( int j=0; j<numMI; j++ ) {
                final JMenuItem mi = ( JMenuItem )menu.getMenuComponent( j ) ;
                mi.setBackground( Color.black ) ;
                mi.setBorder( BorderFactory.createEmptyBorder() ) ;
                mi.setFont( UIConstant.LOG_FONT_BOLD ) ;
                mi.setForeground( Color.LIGHT_GRAY ) ;
                mi.setOpaque( true ) ;
            }
        }
    }

    /**
     * Adds an internal frame to the desktop.
     * @param frame The frame to add to the desktop.
     * @param position A boolean value indicating if the main frame should
     *        position the new internal frame being added.
     */
    public void addInternalFrame( final PlutoInternalFrame frame ,
                                  final boolean position ) {

        // We decide the location of the new internal frame only if it
        // has not been explicitly set by the caller.
        if( position ) {
            final Dimension desktopSize = this.desktop.getSize() ;
            int x = this.lastX + X_DELTA_OFFSET ;
            if( x + frame.getWidth() > desktopSize.width ) {
                x = X_DELTA_OFFSET ;
            }

            int y = this.lastY + Y_DELTA_OFFSET ;
            if( y + frame.getHeight() > desktopSize.height ) {
                y = Y_DELTA_OFFSET ;
            }
            this.lastX = x ;
            this.lastY = y ;
            frame.setLocation( x, y ) ;
        }

        this.desktop.add( frame ) ;
        EventBus.publish( EventType.INTERNAL_FRAME_ADDED, frame ) ;
        frame.setVisible( true ) ;
    }

    /**
     * Returns the size of the desktop
     */
    public Dimension getDesktopSize() {
        return this.desktop.getSize() ;
    }
}
