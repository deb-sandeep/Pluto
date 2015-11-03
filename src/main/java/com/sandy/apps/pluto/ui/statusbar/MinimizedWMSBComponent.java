/**
 * 
 * 
 * 
 *
 * Creation Date: Nov 8, 2008
 */

package com.sandy.apps.pluto.ui.statusbar;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.event.Event ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.event.IEventSubscriber ;
import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.UIHelper ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoFrameType ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoInternalFrame ;

/**
 * This class represents the minimized window manager. Whenever an internal
 * frame in the desktop pane is minimized, an event is generated which is
 * published on the event bus. This class subscribes to the event bus for
 * such events and on receipt of the event manages the minimized window by
 * setting it's visible status to false and generating a categorized popup
 * menu element, which the user can later choose to recurrect the minimized
 * window.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class MinimizedWMSBComponent extends AbstractSBComponent
    implements IEventSubscriber, ActionListener {

    /** Generated serial version UID. */
    private static final long serialVersionUID = -3218408918171054923L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( MinimizedWMSBComponent.class ) ;

    /** A map containing the dialog types versus the array of dialogs. */
    private final EnumMap<PlutoFrameType, List<PlutoInternalFrame>> dialogMap =
              new EnumMap<PlutoFrameType, List<PlutoInternalFrame>>( PlutoFrameType.class ) ;

    /** A map containing a mapping of the dialog types and the buttons associated with it. */
    private final EnumMap<PlutoFrameType, JButton> buttonMap =
              new EnumMap<PlutoFrameType, JButton>( PlutoFrameType.class ) ;

    /** A map to associate a dialog's hash code with it's instance. */
    private final Map<String, PlutoInternalFrame> dialogHashMap =
                                            new HashMap<String, PlutoInternalFrame>() ;

    /** The action command prefix for buttons. */
    private static final String BTN_PREFIX = "BTN-" ;

    /** The action command prefix for frame popup menu items. */
    private static final String FRM_PREFIX = "FRM-" ;

    /** Public constructor. */
    public MinimizedWMSBComponent() {
        super() ;
    }

    /**
     * This method will be called back on this instance during the creation of
     * the status bar and before this component is added as a part of the
     * status bar. We take this opportunity to set up this components in terms
     * of UI and event subscriptions.
     */
    @Override
    public void initialize() throws STException {
        super.setBackground( Color.BLACK ) ;
        super.setLayout( new FlowLayout( FlowLayout.LEFT ) ) ;
        super.setAlignmentY( BOTTOM_ALIGNMENT ) ;
        EventBus.instance().addSubscriberForEventTypes( this, EventType.INTERNAL_FRAME_ADDED ) ;
        EventBus.instance().addSubscriberForEventTypes( this, EventType.INTERNAL_FRAME_CLOSED ) ;
    }

    /**
     * This method is called whenever an internal frame is minimized in the
     * desktop pane. This method preserves the reference of the internal frame
     * for later resurrection.
     */
    @Override
    public void handleEvent( final Event event ) {

        final EventType evtType = event.getEventType() ;
        if( evtType == EventType.INTERNAL_FRAME_ADDED ) {

            final PlutoInternalFrame frame = ( PlutoInternalFrame )event.getValue() ;
            final PlutoFrameType    type  = frame.getDialogType() ;

            List<PlutoInternalFrame> dialogList = this.dialogMap.get( type ) ;
            if( dialogList == null ) {
                // Create a new list and add the frame to the list.
                dialogList = new ArrayList<PlutoInternalFrame>() ;
                this.dialogMap.put( type, dialogList ) ;

                // Create a new button and add it to the panel.
                final JButton button = createButton( type ) ;
                this.buttonMap.put( type, button ) ;
                super.add( button ) ;
                super.validate() ;
            }
            dialogList.add( frame ) ;
            this.dialogHashMap.put( Integer.toHexString( frame.hashCode() ), frame ) ;
        }
        else if( evtType == EventType.INTERNAL_FRAME_CLOSED ) {

            final PlutoInternalFrame dlgMgr = ( PlutoInternalFrame )event.getValue() ;
            final List<PlutoInternalFrame> dlgList = this.dialogMap.get( dlgMgr.getDialogType() ) ;

            // Remove the frame from the list of frames of the same time. This
            // will ensure that the next popup will not contain this frame.
            if( dlgList != null ) {
                dlgList.remove( dlgMgr ) ;

                // If no more such windows are left, we remove the button and
                // repaint this component.
                if( dlgList.isEmpty() ) {
                    this.dialogMap.remove( dlgMgr.getDialogType() ) ;
                    final JButton btn = this.buttonMap.get( dlgMgr.getDialogType() ) ;
                    super.remove( btn ) ;
                    super.validate() ;
                    super.repaint() ;
                }
            }
        }
    }

    /** Configures a button for use in this panel. A refactored method. */
    private JButton createButton( final PlutoFrameType dialogType ) {

        final JButton button = new JButton() ;
        final Icon    icon   = getFrameIcon( dialogType ) ;
        button.setBackground( Color.black ) ;
        button.addActionListener( this ) ;
        button.setContentAreaFilled( true ) ;
        button.setBorderPainted( false ) ;
        button.setFocusPainted( false ) ;
        button.setIconTextGap( 0 ) ;
        button.setMargin( new Insets(0,0,0,0) ) ;
        button.setBorder( BorderFactory.createEmptyBorder() ) ;
        button.setIcon( icon ) ;
        button.setMinimumSize( new Dimension( icon.getIconWidth(), icon.getIconHeight() ) ) ;
        button.setActionCommand( BTN_PREFIX + dialogType.toString() ) ;
        return button ;
    }

    private Icon getFrameIcon( final PlutoFrameType type ) {
        Image image = null ;
        switch( type ) {
            case CHART_FRAME:
                image = UIHelper.IMG_SHOW_CHART ;
                break ;
            case CONFIG_FRAME:
                image = UIHelper.IMG_CFG_WIZARD ;
                break ;
            case INDEX_ITD_FRAME:
                image = UIHelper.IMG_SHOW_INDEX_ITD_PANEL ;
                break ;
            case SCRIP_ITD_FRAME:
                image = UIHelper.IMG_SHOW_ITD_PANEL ;
                break ;
            case SCRIP_EOD_FRAME:
                image = UIHelper.IMG_SCRIP_EOD_TABLE ;
                break ;
            case LOG_FRAME:
                image = UIHelper.IMG_SHOW_LOG_DLG ;
                break ;
            case NEWS_FRAME:
                image = UIHelper.IMG_RSS ;
                break ;
            case PORTFOLIO_FRAME:
                image = UIHelper.IMG_PORTFOLIO ;
                break ;
            case TRADE_EDIT_FRAME:
                image = UIHelper.IMG_TRADE_EDIT ;
                break ;
        }
        return new ImageIcon( image ) ;
    }

    /**
     * This method is invoked when any of the dialog type buttons is pressed
     * by the user. If the user has pressed the button, it implies that there
     * are one of more windows of this type minimized by the user. We show
     * a popup menu containing the titles of the minimized windows of this
     * category and let the user select the window he wants resurrected.
     */
    @Override
    public void actionPerformed( final ActionEvent e ) {

        final String actCmd = e.getActionCommand() ;
        if( actCmd.startsWith( BTN_PREFIX ) ) {
            final String     dlgTypeStr = actCmd.substring( BTN_PREFIX.length() ) ;
            final PlutoFrameType dlgType    = PlutoFrameType.valueOf( dlgTypeStr ) ;
            final JButton    btn        = this.buttonMap.get( dlgType ) ;

            // If we have only one window which is a part of this minimize group
            // there is no point in further showing a popup with one menu item.
            // We just show the window.
            final List<PlutoInternalFrame> dialogList = this.dialogMap.get( dlgType ) ;
            if( dialogList.size() == 1 ) {
                displayDialog( dialogList.get( 0 ) ) ;
            }
            else {
                final JPopupMenu popup = getPopupMenu( dlgType ) ;
                popup.show( btn, 0, -popup.getComponentCount()*15 ) ;
            }
        }
        else if( actCmd.startsWith( FRM_PREFIX ) ) {

            // Get the hash of the frame the user intends to unhide. The hash
            // is the suffix of the action command. From the hash deduce a
            // reference to the frame and the list of frames associated with
            // the type of frame.
            final String        hash    = actCmd.substring( FRM_PREFIX.length() ) ;
            final PlutoInternalFrame dlgMgr  = this.dialogHashMap.get( hash ) ;

            // Display the dialog.
            displayDialog( dlgMgr ) ;
        }
    }

    /**
     * Displays the given dialog. In case the dialog is hidden, it is set visible
     * and then brought forward and selected. Please note that the dialog is
     * managed by the minimize window manager till the time the window is closed.
     */
    private void displayDialog( final PlutoInternalFrame dlgMgr ) {

        // Now we try to unhide the frame, or if the window is already visible
        // try to bring it to the front and have it selected.
        try {
            if( !dlgMgr.isVisible() ) {
                dlgMgr.setVisible( true ) ;
            }
            dlgMgr.toFront() ;
            dlgMgr.setSelected( true ) ;
        }
        catch ( final PropertyVetoException e1 ) {
            logger.error( "Could not unhide a frame", e1 ) ;
        }
    }

    /**
     * Creates a JPopupMenu for all the windows of the given dialog type.
     *
     * @param dlgType The type of dialog. This will be used to associated with
     *        all the minimized windows of the specified type.
     *
     * @return A popup menu with JMenuItem provided for each of the minimized
     *         frame under the specified dialog type category.
     */
    private JPopupMenu getPopupMenu( final PlutoFrameType dlgType ) {
        final JPopupMenu popup = new JPopupMenu() ;
        popup.setBackground( Color.BLACK ) ;
        popup.setFont( UIConstant.LOG_FONT ) ;
        popup.setForeground( Color.LIGHT_GRAY ) ;

        final List<PlutoInternalFrame> dialogList = this.dialogMap.get( dlgType ) ;
        for( final PlutoInternalFrame dialog : dialogList ) {
            popup.add( getJMenuItem( dialog ) ) ;
        }

        return popup ;
    }

    /**
     * Creates a menu item for the specified dialog manager. The active title
     * of the dialog manager is used to create the menu item. This menu item
     * will be the part of a transitent popup menu and when the user clicks
     * the menu item, the window associated with the menu item will be made
     * visible.
     */
    private JMenuItem getJMenuItem( final PlutoInternalFrame dialog ) {

        final JMenuItem menuItem = new JMenuItem() ;
        menuItem.setBackground( Color.BLACK ) ;
        menuItem.setForeground( Color.WHITE ) ;
        menuItem.setFont( UIConstant.LOG_FONT ) ;
        menuItem.setText( dialog.getActiveTitle() ) ;
        menuItem.addActionListener( this ) ;
        menuItem.setActionCommand( FRM_PREFIX + Integer.toHexString( dialog.hashCode() ) ) ;
        return menuItem ;
    }
}
