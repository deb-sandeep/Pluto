/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 13, 2008
 */

package com.sandy.apps.pluto.ui.menumgr;
import java.awt.Color;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.apache.log4j.Logger;

import com.sandy.apps.pluto.StockTracker ;
import com.sandy.apps.pluto.shared.ErrorCode ;
import com.sandy.apps.pluto.shared.Initializable ;
import com.sandy.apps.pluto.shared.STConstant ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.util.util.LoadErrorHandler ;
import com.sandy.apps.pluto.shared.util.util.StringUtil ;
import com.sandy.apps.pluto.ui.I18N ;
import com.sandy.apps.pluto.ui.UIHelper ;

/**
 * This singleton class manages the user interaction actions in the application.
 * This class is loaded via the view spring configuration and injected with the
 * resource path of the menu-config.xml file.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class MenuManager implements Initializable, ActionListener {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( MenuManager.class ) ;

    /** INJECTABLE: This variable should be injected prior to initialization. */
    private String menuConfigRes = null ;

    private static final String DTD_SYSTEM_STR = "-//Menu Config//DTD 1.0//EN" ;
    private static final String DTD_RES_PATH = STConstant.BASE_RES_PATH + "config/menu-config.dtd" ;
    private static final String DIG_RES_PATH = STConstant.BASE_RES_PATH + "config/menu-config-digester-rules.xml" ;

    private List<MenuCfg> menuList = null ;
    private Map<String, ActionCmdCfg> cmdMap = null ;
    private Map<String, List<Object>> abstractBtnMap = null ;

    private final JMenuBar menuBar = new JMenuBar() ;
    private final JToolBar toolBar = new JToolBar() ;
    private final Map<String, PopupMenu> popupMenuMap = new HashMap<String, PopupMenu>() ;

    /** Public constructor. */
    public MenuManager() {
        super() ;
        this.menuList       = new ArrayList<MenuCfg>() ;
        this.cmdMap         = new HashMap<String, ActionCmdCfg>() ;
        this.abstractBtnMap = new HashMap<String, List<Object>>() ;
    }

    /**
     * @return the menuConfigRes
     */
    public String getMenuConfigRes() {
        return this.menuConfigRes ;
    }

    /**
     * @param menuConfigRes the menuConfigRes to set
     */
    public void setMenuConfigRes( final String menuConfigRes ) {
        this.menuConfigRes = menuConfigRes ;
    }

    /**
     * DIGESTER CALLBACK: This method is invoked by the parser during the
     * act of parsing the menu configuration XML file.
     *
     * @param menuCfg The menu to add to this menu item.
     */
    public void addMenuCfg( final MenuCfg menuCfg ) {
        this.menuList.add( menuCfg ) ;
    }

    /**
     * DIGESTER CALLBACK: This method is invoked by the parser during the
     * act of parsing the menu configuration XML file.
     *
     * @param menuCfg The menu to add to this menu item.
     */
    public void addActionCmdCfg( final ActionCmdCfg actCfg ) {
        this.cmdMap.put( actCfg.getName(), actCfg ) ;
    }

    /**
     * This method should be called during the construction process after
     * injecting the menuConfigRes variable. This function implements the
     * initialization logic by loading the menu configuration and
     * preparing the internal data structures for later usage.
     */
    public void initialize() throws STException {

        logger.info( "Initializing the menu manager..." ) ;
        loadConfiguration( MenuManager.class.getResource( this.menuConfigRes ) ) ;

        // NOTE: We do not handle nested menus yet, although there is provision
        // for the logic to be added later.

        // Loop through each menu configuration and construct the application
        // menu.
        for( final MenuCfg menuCfg : this.menuList ) {

            boolean toolbarBtnAdded = false ;
            final JMenu menu = new JMenu( menuCfg.getName() ) ;
            final List<MenuItemCfg> menuItems = menuCfg.getMenuItemList() ;

            // For each menu item in the menu, construct a JMenuItem and attach
            // it to the current menu. Also, create a tool bar button if any
            // of the menu items are registered as tool bar participants.
            for( final MenuItemCfg menuItem : menuItems ) {

                // Construct the menu item.
                menu.add( createMenuItem( menuItem ) ) ;

                // If the menu item is a tool bar participant, create a toolbar button
                if( menuItem.isToolbarParticipant() ) {
                    toolbarBtnAdded = true ;
                    final JButton button = createToolbarBtn( menuItem ) ;
                    this.toolBar.add( button ) ;
                }

                // If the menu item belongs to a popup group, add it to the
                // popup corresponding to the popup group specified.
                final List<String> popupGrps = menuItem.getPopupGroupList() ;
                for( final String popupGrp : popupGrps ) {
                    addPopupMenuItem( popupGrp, menuItem ) ;
                }
            }

            this.menuBar.add( menu ) ;
            if( toolbarBtnAdded ) {
                this.toolBar.addSeparator() ;
            }
        }
    }

    /**
     * Creates a menu item and adds it to the popup menu of the popup group
     * specified.
     *
     * @param popupGrp The popup group to which this menu item belongs.
     * @param menuItem The menu item configuration.
     */
    private void addPopupMenuItem( final String popupGrp, final MenuItemCfg menuItem ) {

        PopupMenu popup = this.popupMenuMap.get( popupGrp ) ;
        if( popup == null ) {
            popup = new PopupMenu() ;
            this.popupMenuMap.put( popupGrp, popup ) ;
        }

        // Create the popup menu item.
        final String actCmd = menuItem.getActionCmdRef() ;
        final MenuItem item = new MenuItem( menuItem.getName() ) ;
        item.setActionCommand( actCmd ) ;
        item.addActionListener( this ) ;

        popup.add( item ) ;

        registerComponentWithCmd( actCmd, item ) ;
    }

    /**
     * Creates a JMenuItem from the given menu item configuration and adds it
     * to the cache of action command versus the list of abstract buttons.
     *
     * @param menuItem The menu item configuration
     *
     * @return An instance of JMenuItem.
     */
    private JMenuItem createMenuItem( final MenuItemCfg menuItem ) {

        final JMenuItem item = new JMenuItem( menuItem.getName() ) ;
        final String iconImg = menuItem.getIcon() ;
        final String actCmd  = menuItem.getActionCmdRef() ;

        if( StringUtil.isNotEmptyOrNull( iconImg ) ) {
            item.setIcon( UIHelper.getIcon( iconImg ) ) ;
        }
        item.setActionCommand( actCmd ) ;
        item.addActionListener( this ) ;

        registerComponentWithCmd( actCmd, item ) ;

        return item ;
    }

    /**
     * Creates a JButton from the given menu item configuration and adds it
     * to the cache of action command versus the list of abstract buttons.
     *
     * @param menuItem The menu item configuration
     *
     * @return An instance of JButton.
     */
    private JButton createToolbarBtn( final MenuItemCfg menuItem ) {

        final JButton toolbarBtn = new JButton() ;
        final String iconImg = menuItem.getIcon() ;
        final String actCmd  = menuItem.getActionCmdRef() ;

        if( StringUtil.isNotEmptyOrNull( iconImg ) ) {
            toolbarBtn.setIcon( UIHelper.getIcon( iconImg ) ) ;
        }
        else {
            toolbarBtn.setText( menuItem.getName() ) ;
        }
        toolbarBtn.setActionCommand( actCmd ) ;
        toolbarBtn.addActionListener( this ) ;
        toolbarBtn.setBorderPainted( false ) ;
        toolbarBtn.setIconTextGap( 0 ) ;
        toolbarBtn.setMargin( new Insets(0,0,0,0) ) ;
        toolbarBtn.setBackground( Color.BLACK ) ;

        registerComponentWithCmd( actCmd, toolbarBtn ) ;

        return toolbarBtn ;
    }

    /**
     * Builds a list of components registered against a given action command.
     * Note that since PopupMenu and AbstractButton don't have a common
     * hierarchy we store all of them in an array of objects. While retrieving
     * and operating on the list, we will have to explicitly check for
     * instances of objects before attempting any stunt on them. Since this
     * is an internal implementation - the risk is acceptable.
     *
     * @param actCmd The action command for which this component is to be registered
     *
     * @param component The UI component.
     */
    private void registerComponentWithCmd( final String actCmd, final Object component ) {
        List<Object> btnList = this.abstractBtnMap.get( actCmd ) ;
        if( btnList == null ) {
            btnList = new ArrayList<Object>() ;
            this.abstractBtnMap.put( actCmd, btnList ) ;
        }
        btnList.add( component ) ;
    }

    /**
     * Loads the menu configuration from the given URL. It is assumed that the
     * supplied URL refers to a XML resource conforming to the menu-config
     * DTD definition.
     *
     * @param cfgURL An URL pointing to a XML file.
     *
     * @throws STException If an exception condition is encountered during
     *         the act of loading the XML configuration file.
     */
    private void loadConfiguration( final URL cfgURL )
        throws STException {

        logger.debug( "Parsing the menu configuration..." ) ;
        InputStream iStream = null ;
        Digester digester = null ;

        logger.debug( "Loading menu DTD from path " + DTD_RES_PATH ) ;
        final URL dtdURL  = MenuManager.class.getResource( DTD_RES_PATH ) ;

        logger.debug( "Loading menu Digester Rules from path " + DIG_RES_PATH ) ;
        final URL ruleURL = MenuManager.class.getResource( DIG_RES_PATH ) ;

        if ( dtdURL == null ) {
            throw new STException( "DTD not found at " + ruleURL,
                                   ErrorCode.INIT_FAILURE ) ;
        }
        if ( ruleURL == null ) {
            throw new STException( "Digester rules not found at " + ruleURL,
                                   ErrorCode.INIT_FAILURE ) ;
        }

        try {
            iStream = cfgURL.openStream() ;
            digester = DigesterLoader.createDigester( ruleURL ) ;

            digester.register( DTD_SYSTEM_STR, dtdURL.toString() ) ;
            digester.setValidating( true ) ;
            digester.setErrorHandler( new LoadErrorHandler( cfgURL.toString() ) ) ;
            digester.push( this ) ;
            digester.parse( iStream ) ;
        }
        catch ( final Exception e ) {
            logger.error( "Error in initialization : " + e.getMessage(), e ) ;
            throw new STException( "Error loading menu config", e, ErrorCode.INIT_FAILURE ) ;
        }
        finally {
            if (iStream != null) {
                try {
                    iStream.close() ;
                }
                catch (final IOException e) {
                    logger.error( "Can't close input stream", e ) ;
                }
            }
        }
    }

    /**
     * Invokes the action command associated with this event and subsequently
     * enables or disables the action commands configured as dependencies
     * along with this action.
     */
    @Override
    public void actionPerformed( final ActionEvent e ) {
        logger.debug( "User action " + e.getActionCommand() + " invoked" ) ;
        final ActionCmdCfg actionCmd = this.cmdMap.get( e.getActionCommand() ) ;
        try {
            actionCmd.invoke() ;
            final List<String> enableDeps = actionCmd.getAcToEnableOnClick() ;
            for( final String cmd : enableDeps ) {
                enableActionCmd( cmd, true ) ;
            }
            final List<String> disableDeps = actionCmd.getAcToDisableOnClick() ;
            for( final String cmd : disableDeps ) {
                enableActionCmd( cmd, false ) ;
            }
        }
        catch ( final STException stEx ) {
            JOptionPane.showMessageDialog( StockTracker.MAIN_FRAME,
                            I18N.MSG_USER_ACTION_FAILURE + stEx.getMessage() ) ;
        }
    }

    /**
     * Enables or disables the abstract buttons registered with the action
     * command specified.
     *
     * @param cmd The action command whose registrations are to be enabled or
     *        disabled.
     *
     * @param b A boolean flag indicating whether to enable (true), or
     *        disable (false).
     */
    public void enableActionCmd( final String cmd, final boolean b ) {

        logger.debug( "Enabling action command " + cmd + " status=" + b ) ;
        final List<Object> assocBtnList = this.abstractBtnMap.get( cmd ) ;
        for( final Object btn : assocBtnList ) {
            if( btn instanceof AbstractButton ) {
                final AbstractButton button = ( AbstractButton )btn ;
                button.setEnabled( b ) ;
            }
            else if( btn instanceof MenuItem ) {
                final MenuItem item = ( MenuItem )btn ;
                item.setEnabled( b ) ;
            }
        }
    }

    /**
     * @return The application tool bar
     */
    public JToolBar getToolBar() {
        return this.toolBar ;
    }

    /**
     * @return The application menu bar
     */
    public JMenuBar getMenuBar() {
        return this.menuBar ;
    }

    /**
     * @return The popup menu for the specified group
     */
    public PopupMenu getPopup( final String popupGroup ) {
        return this.popupMenuMap.get( popupGroup ) ;
    }
}
