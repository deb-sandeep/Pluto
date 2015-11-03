/**
 * 
 * 
 * 
 *
 * Creation Date: Apr 12, 2011
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.jobsummary;
import java.awt.event.ActionEvent ;
import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.ui.dialogmgr.IPlutoFramePanel ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoInternalFrame ;

/**
 * This class represents the concrete class for encapsulating the logic of the
 * common attribute editor for the job.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class JobCommonAttrEditorPanel extends JobCommonAttrEditorPanelUI
        implements IPlutoFramePanel {

    /** Default serial version UID */
    private static final long serialVersionUID = 1L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger
            .getLogger( JobCommonAttrEditorPanel.class ) ;

    /**
     * OVERRIDDEN METHOD:
     */
    @Override
    public void actionPerformed( final ActionEvent arg0 ) {
    }

    /**
     * OVERRIDDEN METHOD:
     */
    @Override
    public Object[][] getPanelIcons() {
        return null ;
    }

    /**
     * OVERRIDDEN METHOD:
     */
    @Override
    public void initializeData() throws STException {
    }

    /**
     * OVERRIDDEN METHOD:
     */
    @Override
    public void destroy() {
    }

    /**
     * OVERRIDDEN METHOD:
     */
    @Override
    public boolean isDirty() {
        return false ;
    }

    /**
     * OVERRIDDEN METHOD:
     */
    @Override
    public List<String> validateUserInput() throws STException {
        return null ;
    }

    /**
     * OVERRIDDEN METHOD:
     */
    @Override
    public void save() throws STException {
    }

    /**
     * OVERRIDDEN METHOD:
     */
    @Override
    public void setDialogManager( final PlutoInternalFrame dlgMgr ) {
    }

    /**
     * OVERRIDDEN METHOD:
     */
    @Override
    public String getTitle() {
        return null ;
    }
}
