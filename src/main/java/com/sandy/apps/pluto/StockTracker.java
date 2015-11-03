/**
 * Creation Date: Aug 3, 2008
 */

package com.sandy.apps.pluto;
import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.shared.Initializable ;
import com.sandy.apps.pluto.shared.STConstant ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.util.bootstrap.Bootstrap ;
import com.sandy.apps.pluto.ui.MainFrame ;
import com.sandy.apps.pluto.ui.SystemTrayWrapper ;

/**
 * The entry point for the StockTracker application. This application should
 * be installed as a windows service and the conduit to the user interface
 * console will be only through the system tray icon.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class StockTracker implements STConstant, Initializable {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( StockTracker.class ) ;

    /** The location of the bootstrap configuration file. */
    private static final String BOOTSTRAP_CFG_PATH = BASE_RES_PATH + "bootstrap-config.xml" ;

    /** The system tray icon wrapper. */
    public static final SystemTrayWrapper SYS_TRAY = new SystemTrayWrapper() ;

    /** A reference to the main console window. */
    public static final MainFrame MAIN_FRAME = new MainFrame() ;

    /** Public no argument constructor. */
    public StockTracker() {
        super() ;
    }

    /** Returns the main console window for this application. */
    public static MainFrame getMainFrame() {
        return MAIN_FRAME ;
    }

    /**
     * Handles the initialization logic of the application, like setting up the
     * task bar icon, initializing the console etc.
     */
    @Override
    public void initialize() throws STException {
        logger.info( "Initializing Pluto" ) ;

        logger.info( "Setting up system tray icon" ) ;
        SYS_TRAY.initialize() ;

        logger.info( "Setting up the main console" ) ;
        MAIN_FRAME.initialize() ;
    }

    /**
     * Starts the StockTracker application.
     */
    public static void main( final String[] args )
        throws Exception {

        // Bootstrap the Pluto application
        logger.info( "Bootstrapping Pluto. This will initialize basic " +
        		     "services like Network and Job scheduler." ) ;
        final Bootstrap bootstrap = new Bootstrap() ;
        bootstrap.initialize( StockTracker.class.getResource( BOOTSTRAP_CFG_PATH ) ) ;

        // Initialize the Pluto application
        final StockTracker pluto = new StockTracker() ;
        pluto.initialize() ;
    }
}
