/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 13, 2008
 */

package com.sandy.apps.pluto.ui.menumgr;

import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates the Menu Item related information from the XML file
 * conforming to the "menu-config.dtd". This class will be used solely by the
 * MenuManager and should not be relied upon by others.
 * <p>
 * This class will be instantiated and aggregated using the commons digester
 * framework.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class MenuItemCfg {

    private String  name               = null ;
    private String  icon               = null ;
    private String  actionCmdRef       = null ;
    private boolean toolbarParticipant = false ;

    private final List<String>  popupGroupList = new ArrayList<String>() ;
    private final List<MenuCfg> menuList       = new ArrayList<MenuCfg>() ;

    /** Public constructor. */
    public MenuItemCfg() {
        super() ;
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
     * @param popupGroup The popup group to which this menu item belongs
     */
    public void addPopupGroup( final String popupGroup ) {
        this.popupGroupList.add( popupGroup ) ;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name ;
    }

    /**
     * @param name the name to set
     */
    public void setName( final String name ) {
        this.name = name ;
    }

    /**
     * @return the icon
     */
    public String getIcon() {
        return this.icon ;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon( final String icon ) {
        this.icon = icon ;
    }

    /**
     * @return the toolbarParticipant
     */
    public boolean isToolbarParticipant() {
        return this.toolbarParticipant ;
    }

    /**
     * @param toolbarParticipant the toolbarParticipant to set
     */
    public void setToolbarParticipant( final boolean toolbarParticipant ) {
        this.toolbarParticipant = toolbarParticipant ;
    }

    /**
     * @return the popupGroupList
     */
    public List<String> getPopupGroupList() {
        return this.popupGroupList ;
    }

    /**
     * @return the actionCmdRef
     */
    public String getActionCmdRef() {
        return this.actionCmdRef ;
    }

    /**
     * @param actionCmdRef the actionCmdRef to set
     */
    public void setActionCmdRef( final String actionCmdRef ) {
        this.actionCmdRef = actionCmdRef ;
    }
}
