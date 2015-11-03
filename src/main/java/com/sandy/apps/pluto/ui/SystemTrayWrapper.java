/**
 * Creation Date: Aug 12, 2008
 */

package com.sandy.apps.pluto.ui;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.StockTracker ;
import com.sandy.apps.pluto.shared.ErrorCode ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.Initializable ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.event.Event ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.event.IEventSubscriber ;

/**
 * This class encapsulates the application system tray icon and provides
 * functionality to change the state of the system tray and display system
 * messages based on the events generated by the application.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class SystemTrayWrapper
    implements IEventSubscriber, ActionListener, Initializable, UIConstant {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( SystemTrayWrapper.class ) ;

    /** The system tray icon which is managed by this class. */
    private TrayIcon trayIcon = null;

    /** The menu items associated with the tray icon popup. */
    final MenuItem workOfflineMI = new MenuItem( I18N.LBL_MI_WORK_OFFLINE ) ;
    final MenuItem workOnlineMI  = new MenuItem( I18N.LBL_MI_WORK_ONLINE ) ;

    /** Public constructor. */
    public SystemTrayWrapper() {
        super() ;
    }

    /**
     * Initializes this class by subscribing to the event bus and setting up
     * the internal data structures. This method also sets up the system
     * tray icon.
     */
    public void initialize() throws STException {

        EventBus.instance().addSubscriberForEventTypes( this,
                                             EventType.NETWORK_STATUS_CHANGE ) ;

        final SystemTray tray    = SystemTray.getSystemTray();
        final PopupMenu  popup   = getTrayPopupMenu() ;
        final String     tooltip = I18N.APP_NAME ;

        final boolean online = ServiceMgr.getNetworkSvc().isOnline() ;
        final Image appImg = ( online ) ? IMG_APP_CONNECTED : IMG_APP_DISCONNECTED ;

        this.trayIcon = new TrayIcon( appImg, tooltip, popup ) ;
        this.trayIcon.setActionCommand( AC_TRAY_ICON_DBLCLICK ) ;
        this.trayIcon.addActionListener( this );

        try {
            tray.add( this.trayIcon ) ;
        }
        catch ( final AWTException e ) {
            logger.error( "Could not install the tray icon", e ) ;
            throw new STException( e, ErrorCode.INIT_FAILURE ) ;
        }
    }

    /**
     * Sets up the popup menu associated with the tray icon. This popup will
     * be displayed when the user right clicks on the tray icon.
     */
    private PopupMenu getTrayPopupMenu() {

        final PopupMenu popupMenu = ServiceMgr.getMenuManager().getPopup(
                                                  UIConstant.TRAY_ICON_POPUP ) ;
        return popupMenu ;
    }


    /**
     * This handler is called by Swing when the user either double clicks on
     * the system tray icon or selects any of the menu items of the popup menu.
     */
    @Override
    public void actionPerformed( final ActionEvent e ) {

        final String actionCmd = e.getActionCommand() ;
        logger.debug( "System tray interaction detected. Cmd = " + actionCmd ) ;

        if( actionCmd.equals( AC_TRAY_ICON_DBLCLICK ) ) {
            final MainFrame mainFrame = StockTracker.getMainFrame() ;
            StockTracker.getMainFrame().setVisible( !mainFrame.isVisible() ) ;
        }
    }

    /**
     * Receives system events and changes the status of the tray icon.
     */
    @Override
    public void handleEvent( final Event event ) {

        switch( event.getEventType() ) {
            case NETWORK_STATUS_CHANGE :
                final Boolean status = ( Boolean )event.getValue() ;
                if( status.booleanValue() ) {
                    this.trayIcon.setImage( IMG_APP_CONNECTED ) ;
                    showMessage( "Network", "Network available", MessageType.INFO ) ;
                }
                else {
                    this.trayIcon.setImage( IMG_APP_DISCONNECTED ) ;
                    showMessage( "Network", "Network connection lost", MessageType.ERROR ) ;
                }
                break ;
        }
    }

    /** Displays a notification at the system tray icon. */
    public void showMessage( final String caption, final String text,
                             final MessageType msgType ) {
        this.trayIcon.displayMessage( caption, "Pluto - " + text, msgType ) ;
    }

    /** Sets a new tooltip to the task bar icon. */
    public void setTooltip( final String tooltip ) {
        this.trayIcon.setToolTip( tooltip ) ;
    }
}