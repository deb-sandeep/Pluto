/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 11, 2008
 */

package com.sandy.stocktracker.test.poc;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * This class tests a proof of concept on the JDK 1.6 task bar icon and associated
 * actions.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class TaskBarIconTest {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( TaskBarIconTest.class ) ;

    /** Public constructor. */
    public TaskBarIconTest() {
        super() ;
    }

    public void begin() throws Exception {

        TrayIcon trayIcon = null;
        if (SystemTray.isSupported()) {

            // get the SystemTrayWrapper instance
            final SystemTray tray = SystemTray.getSystemTray();
            // load an image
            final URL imgURL = TaskBarIconTest.class.getResource( "/com/sandy/stocktracker/images/pluto.png" ) ;
            final Image image = Toolkit.getDefaultToolkit().getImage( imgURL );

            // create a action listener to listen for default action executed on the tray icon
            final ActionListener listener = new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    logger.debug( "Tray icon clicked" ) ;
                }
            };

            // create a popup menu
            final PopupMenu popup = new PopupMenu();
            // create menu item for the default action
            final MenuItem defaultItem = new MenuItem( "Default Item" );
            defaultItem.addActionListener(listener);
            popup.add( defaultItem ) ;

            /// ... add other items
            // construct a TrayIcon
            trayIcon = new TrayIcon( image, "Tray Demo", popup ) ;
            // set the TrayIcon properties
            trayIcon.addActionListener( listener );
            // add the tray image
            try {
                tray.add(trayIcon);
            }
            catch ( final AWTException e ) {
                logger.error( "Could not add the tray icon", e ) ;
            }
        }
        else {
            logger.error( "Tray icon is not supported" ) ;

        }

        Thread.sleep( 50000 ) ;
    }

    public static void main( final String[] args ) throws Exception {
        final TaskBarIconTest test = new TaskBarIconTest() ;
        test.begin() ;
    }
}
