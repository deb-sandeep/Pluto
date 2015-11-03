/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 15, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.proxy;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.biz.svc.IUserPreferenceSvc ;
import com.sandy.apps.pluto.shared.ConfigKey ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.util.util.StringUtil ;
import com.sandy.apps.pluto.ui.I18N ;
import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.UIHelper ;
import com.sandy.apps.pluto.ui.dialogmgr.IPlutoFramePanel ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoInternalFrame ;
import com.sandy.apps.pluto.ui.validator.IntegerValidator ;
import com.sandy.apps.pluto.ui.validator.StringValidator ;

/**
 * This class is the concrete implementation of the proxy configuration panel.
 * It implements {@link IPlutoFramePanel} interface and hence becomes eligble for
 * management by the Config Wizard.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ProxyCfgPanel extends ProxyCfgPanelUI
    implements IPlutoFramePanel, UIConstant, ActionListener {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 561684807893431129L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ProxyCfgPanel.class ) ;

    private boolean useProxy  = false ;
    private String  proxyHost = null ;
    private int     proxyPort = 80 ;
    private boolean useAuth   = false ;
    private String  userName  = "" ;
    private String  password  = "" ;

    /** Public constructor. */
    public ProxyCfgPanel( final String name ) {
        super() ;
        super.setName( name ) ;
    }

    /**
     * This method is called by the dialog manager on the instance of the
     * dialog panel to register itself with the panel. The reference passed
     * as parameter can be saved by the dialog panel and used to invoke
     * operations provided by the dialog manager, for example requests for
     * enabling or disabling a particular button based on its action command.
     */
    public void setDialogManager( final PlutoInternalFrame dlgMgr ) {
        // We don't need to preserve the dialog manager's reference for this panel.
    }

    /**
     * Returns the icons that need to be displayed in the wizard toolbar when
     * this panel is selected.
     * <ul>
     *  <li>Element 0 [String][M] - Action command for the button</li>
     *  <li>Element 1 [Image] [M] - Button image</li>
     *  <li>Element 2 [Image] [O] - Pressed image for button</li>
     *  <li>Element 3 [Image] [O] - Roll over image for button[</li>
     * </ul>
     * <p>
     *
     * @return A two dimensional array of Objects, with each row having two
     *         elements.
     */
    public Object[][] getPanelIcons() {
        return new Object[][] {
            { AC_CFG_WIZ_OK, IMG_ACCEPT, IMG_ACCEPT_PRESSED, "Save" },
        } ;
    }

    /**
     * This method fetches the data that is to be used for pre filling the
     * form elements. This method also has the logic of enriching the UI
     * elements by attaching input validators to the UI elements.
     */
    @Override
    public void initializeData() throws STException {

        // Load the current values and populate the UI controls with the values.
        final IUserPreferenceSvc userPrefSvc = ServiceMgr.getUserPrefSvc() ;

        this.useProxy  = userPrefSvc.getBoolean( ConfigKey.USE_PROXY, false ) ;
        this.proxyHost = userPrefSvc.getUserPref( ConfigKey.PROXY_HOST, null ) ;
        this.proxyPort = userPrefSvc.getInt( ConfigKey.PROXY_PORT, 80 ) ;
        this.useAuth   = userPrefSvc.getBoolean( ConfigKey.USE_AUTH, false ) ;
        this.userName  = userPrefSvc.getUserPref( ConfigKey.PROXY_USER, "" ) ;
        this.password  = userPrefSvc.getUserPref( ConfigKey.PROXY_PWD, "" ) ;

        super.useProxyCB.setSelected( this.useProxy ) ;
        super.proxyHostTF.setText( this.proxyHost ) ;
        super.proxyPortTF.setText( String.valueOf( this.proxyPort ) ) ;
        super.useProxyAuthCB.setSelected( this.useAuth ) ;
        super.userIdTF.setText( this.userName ) ;
        super.passwordTF.setText( this.password ) ;

        // Now attach input verifiers with the UI elements.
        super.proxyHostTF.setInputVerifier( new StringValidator ( super.proxyHostTF ) ) ;
        super.proxyPortTF.setInputVerifier( new IntegerValidator( super.proxyPortTF ) ) ;
        super.userIdTF.setInputVerifier( new StringValidator ( super.userIdTF ) ) ;
        super.passwordTF.setInputVerifier( new StringValidator ( super.passwordTF ) ) ;
    }

    /** Returns a true if the user has changed the values since the panel was displayed. */
    @Override
    public boolean isDirty() {

        boolean dirty = false ;
        String tmp = null ;

        if( this.useProxy != super.useProxyCB.isSelected() ) {
            dirty = true ;
        }
        if( !dirty ) {
            tmp = super.proxyHostTF.getText() ;
            dirty = !UIHelper.isEqual( this.proxyHost, tmp ) ;
        }
        if( !dirty ) {
            tmp = super.proxyPortTF.getText() ;
            dirty = !UIHelper.isEqual( String.valueOf( this.proxyPort ), tmp ) ;
        }
        if( !dirty ) {
            if( this.useAuth != super.useProxyAuthCB.isSelected() ) {
                dirty = true ;
            }
        }
        if( !dirty ) {
            tmp = super.userIdTF.getText() ;
            dirty = !UIHelper.isEqual( this.userName, tmp ) ;
        }
        if( !dirty ) {
            tmp = new String( super.passwordTF.getPassword() ) ;
            dirty = !UIHelper.isEqual( String.valueOf( this.password ), tmp ) ;
        }

        return dirty ;
    }

    /**
     * Saves the values populated by the user. Saving the user preferences will
     * result in the publishing of the event {@link EventType#USER_PREF_CHANGED}
     * on the event bus.
     */
    @Override
    public void save() throws STException {
        // Load the current values and populate the UI controls with the values.
        final IUserPreferenceSvc userPrefSvc = ServiceMgr.getUserPrefSvc() ;

        final Map<String, String> userPrefMap = new HashMap<String, String>() ;
        userPrefMap.put( ConfigKey.USE_PROXY,  String.valueOf( super.useProxyCB.isSelected() ) ) ;
        userPrefMap.put( ConfigKey.PROXY_HOST, String.valueOf( super.proxyHostTF.getText() ) ) ;
        userPrefMap.put( ConfigKey.PROXY_PORT, String.valueOf( super.proxyPortTF.getText() ) ) ;
        userPrefMap.put( ConfigKey.USE_AUTH,   String.valueOf( super.useProxyAuthCB.isSelected() ) ) ;
        userPrefMap.put( ConfigKey.PROXY_USER, String.valueOf( super.userIdTF.getText() ) ) ;
        userPrefMap.put( ConfigKey.PROXY_PWD,  String.valueOf( super.passwordTF.getPassword() ) ) ;

        userPrefSvc.saveUserPreferences( userPrefMap ) ;

        // Reload the proxy preferences so that we can compute dirtiness
        this.useProxy  = userPrefSvc.getBoolean( ConfigKey.USE_PROXY, false ) ;
        this.proxyHost = userPrefSvc.getUserPref( ConfigKey.PROXY_HOST, null ) ;
        this.proxyPort = userPrefSvc.getInt( ConfigKey.PROXY_PORT, 80 ) ;
        this.useAuth   = userPrefSvc.getBoolean( ConfigKey.USE_AUTH, false ) ;
        this.userName  = userPrefSvc.getUserPref( ConfigKey.PROXY_USER, "" ) ;
        this.password  = userPrefSvc.getUserPref( ConfigKey.PROXY_PWD, "" ) ;
    }

    /**
     * Validates if the inputs used by the user are valid from a holistic
     * perspective. Individual field validations are handled at the field level.
     */
    @Override
    public List<String> validateUserInput() throws STException {
        final List<String> msgs = new ArrayList<String>() ;

        String tmp = null ;
        if( super.useProxyCB.isSelected() ) {

            tmp = super.proxyHostTF.getText() ;
            if( StringUtil.isEmptyOrNull( tmp ) ) {
                msgs.add( I18N.MSG_PROXY_HOST_INVALID ) ;
            }

            tmp = super.proxyPortTF.getText() ;
            if( StringUtil.isEmptyOrNull( tmp ) ) {
                msgs.add( I18N.MSG_PROXY_PORT_INVALID ) ;
            }
        }

        if( super.useProxyAuthCB.isSelected() ) {

            if( !super.useProxyCB.isSelected() ) {
                msgs.add(  I18N.MSG_PROXY_DISABLED ) ;
            }

            tmp = super.userIdTF.getText() ;
            if( StringUtil.isEmptyOrNull( tmp ) ) {
                msgs.add( I18N.MSG_PROXY_USER_INVALID ) ;
            }

            tmp = new String( super.passwordTF.getPassword() ) ;
            if( StringUtil.isEmptyOrNull( tmp ) ) {
                msgs.add( I18N.MSG_PROXY_PWD_INVALID ) ;
            }
        }

        return msgs ;
    }

    /**
     * This method is invoked when the user selects any of the panel specific
     * buttons. Since this panel does not have any panel specific buttons,
     * this method does nothing.
     */
    @Override
    public void actionPerformed( final ActionEvent e ) {
        // NO OPERATION
    }

    /**
     * This method is invoked on the child panels when the dialog is being
     * disposed as a result of a cancel operation chosen by the user. Subclasses
     * can implement this function to perform cleanup logic like de-registering
     * from the event bus etc.
     */
    public void destroy() {
        // NO OPERATION
    }

    /** No operation method. */
    public String getTitle() {
        return null ;
    }
}
