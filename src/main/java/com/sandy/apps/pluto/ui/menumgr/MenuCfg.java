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
 * This class encapsulates the Menu related information from the XML file
 * conforming to the "menu-config.dtd". This class will be used solely by the
 * MenuManager and should not be relied upon by others.
 * <p>
 * This class will be instantiated and aggregated using the commons digester
 * framework.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class MenuCfg {

    private String name = null ;
    private final List<MenuItemCfg> menuItemList = new ArrayList<MenuItemCfg>() ;

    /** Public constructor. */
    public MenuCfg() {
        super() ;
    }

    /**
     * DIGESTER CALLBACK: This method is invoked by the parser during the
     * act of parsing the menu configuration XML file.
     *
     * @param menuItemCfg The menu item to add to this menu.
     */
    public void addMenuItemCfg( final MenuItemCfg menuItemCfg ) {
        this.menuItemList.add( menuItemCfg ) ;
    }

    /**
     * @return the menuItemList
     */
    public List<MenuItemCfg> getMenuItemList() {
        return this.menuItemList ;
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
}
