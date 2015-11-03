/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 13, 2008
 */

package com.sandy.apps.pluto.ui.statusbar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.Initializable ;
import com.sandy.apps.pluto.shared.STException ;

/**
 * This class represents the application console's status bar and is shown at
 * the south most component on the application console. The status bar houses
 * many components like the network status, price index values etc.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class StatusBar extends JPanel implements Initializable {

    /** The generated serial version UID. */
    private static final long serialVersionUID = 1257815562094770997L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( StatusBar.class ) ;

    /** The panel which will hold the components oriented towards the west.*/
    private final JPanel westPanel = new JPanel() ;

    /** The panel which will hold the components oriented towards the right. */
    private final JPanel eastPanel = new JPanel() ;

    /** The network status component. */
    private final AbstractSBComponent networkStats = new NetworkSBComponent() ;

    /** The minimized window manager component. */
    private final AbstractSBComponent minWM = new MinimizedWMSBComponent() ;

    /** The clock component. */
    private final ClockSBComponent clock = new ClockSBComponent() ;

    /** The index value display status bar component. */
    private final IndexValueSBComponent indexValComponent = new IndexValueSBComponent() ;

    /** Public constructor. */
    public StatusBar() {
        super() ;
    }

    /**
     * This method needs to be called externally before this class can be used.
     * This method holds the logic of initializing the status bar by setting
     * up its layout, UI characteristics and child components.
     */
    @Override
    public void initialize() throws STException {

        // Set the background color to Black
        setBackground( Color.black ) ;

        // Set up child components
        this.networkStats.initialize() ;
        this.minWM.initialize() ;
        this.clock.initialize() ;
        this.indexValComponent.initialize() ;

        // Set up the east panel.
        this.eastPanel.setLayout( new FlowLayout( FlowLayout.RIGHT ) ) ;
        this.eastPanel.add( this.indexValComponent ) ;
        this.eastPanel.add( getSeparator() ) ;
        this.eastPanel.add( this.networkStats ) ;
        this.eastPanel.add( getSeparator() ) ;
        this.eastPanel.add( this.clock ) ;
        this.eastPanel.setBackground( Color.BLACK ) ;

        // Set up the west panel
        this.westPanel.setLayout( new BorderLayout() ) ;
        this.westPanel.add( this.minWM, BorderLayout.CENTER ) ;
        this.westPanel.setBackground( Color.BLACK ) ;
        this.westPanel.setPreferredSize( new Dimension( 150, 10 ) ) ;

        // Set up UI characteristics.
        setLayout( new BorderLayout() ) ;
        add( this.eastPanel, BorderLayout.CENTER ) ;
        add( this.westPanel, BorderLayout.WEST ) ;
    }

    /**
     * Creates a separator component for separating different status bar items
     */
    private Component getSeparator() {
        final JPanel label = new JPanel() ;
        label.setBackground( Color.DARK_GRAY ) ;
        label.setPreferredSize( new Dimension( 2, 25 ) ) ;
        return label ;
    }
}
