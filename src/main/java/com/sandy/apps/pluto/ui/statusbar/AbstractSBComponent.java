/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 13, 2008
 */

package com.sandy.apps.pluto.ui.statusbar;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.Initializable ;

/**
 * This class defines the base class of a typical status bar component. Concrete
 * status bar components should derive from this class.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public abstract class AbstractSBComponent extends JPanel
    implements Initializable {

    /** Serial version UID. */
    private static final long serialVersionUID = -7087385729013001309L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( AbstractSBComponent.class ) ;

    /** Public constructor. */
    public AbstractSBComponent() {
        super() ;
    }
}
