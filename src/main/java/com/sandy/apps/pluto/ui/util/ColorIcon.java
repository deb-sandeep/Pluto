/**
 * 
 * 
 * 
 *
 * Creation Date: Nov 21, 2008
 */

package com.sandy.apps.pluto.ui.util;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * This class represents a simple coloured icon. Given a size and color during
 * construction, this class will represent a Icon of the same nature.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ColorIcon implements Icon {

    /** The default width & height . */
    private static final int DEF_WIDTH = 10 ;
    private static final int DEF_HEIGHT= 10 ;

    /** The color of this icon. */
    private Color color = null ;

    /** The height and width of this icon. */
    private int width = DEF_WIDTH, height = DEF_HEIGHT ;

    /**
     * Public constructor which creates an icon of size default width and height
     * and of the specified color.
     *
     * @param color The color of the icon.
     */
    public ColorIcon( final Color color ) {
        this.color = color ;
    }

    /**
     * Public constructor which creates an icon of the specified width and height
     * and of the specified color.
     *
     * @param color The color of the icon.
     */
    public ColorIcon( final Color color, final int width, final int height ) {
        this.color = color ;
        this.width = width ;
        this.height = height ;
    }

    /** Returns the height of this icon. */
    public int getIconHeight() {
        return this.height ;
    }

    /** Returns the width of this icon. */
    public int getIconWidth() {
        return this.width ;
    }

    /** Paints the icon, essentially renders it with the specified color. */
    public void paintIcon( final Component c, final Graphics g, final int x, final int y ) {
        g.setColor( this.color ) ;
        g.fill3DRect( x, y, this.width, this.height, true ) ;
    }
} ;
