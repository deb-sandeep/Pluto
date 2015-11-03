/**
 * 
 * 
 * 
 *
 * Creation Date: Oct 9, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.STException ;

/**
 * An abstract implementation of {@link IPlutoFramePanel}, which provides no operation
 * implementations of most of the methods. This class can be implemented by
 * those panels which do not need data entry capabilities.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public abstract class AbstractPlutoFramePanel extends JPanel implements
        IPlutoFramePanel {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L ;

    /** A reference to the dialog manager to which this panel belongs. */
    private PlutoInternalFrame dlgMgr = null ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( AbstractPlutoFramePanel.class ) ;

    /** Public constructor which takes the name of this panel. */
    public AbstractPlutoFramePanel( final String name ) {
        super() ;
        super.setName( name ) ;
    }

    /** No operation method. */
    @Override
    public void destroy() {
    }

    /** No operation method. */
    @Override
    public Object[][] getPanelIcons() {
        return null ;
    }

    /** No operation method. */
    @Override
    public void initializeData() throws STException {
    }

    /** No operation method. */
    @Override
    public boolean isDirty() {
        return false ;
    }

    /** No operation method. */
    @Override
    public void save() throws STException {
    }

    /** No operation method. */
    @Override
    public void setDialogManager( final PlutoInternalFrame dlgMgr ) {
        this.dlgMgr = dlgMgr ;
    }

    /** Returns the dialog manager to which this panel belongs. */
    public PlutoInternalFrame getDialogManager() {
        return this.dlgMgr ;
    }

    /** No operation method. */
    @Override
    public List<String> validateUserInput() throws STException {
        return null ;
    }

    /** No operation method. */
    @Override
    public void actionPerformed( final ActionEvent e ) {
    }

    /** No operation method. */
    public String getTitle() {
        return null ;
    }
}
