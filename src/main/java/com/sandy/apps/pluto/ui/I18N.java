/**
 * Creation Date: Aug 11, 2008
 */

package com.sandy.apps.pluto.ui;
import java.io.IOException;
import java.io.InputStream;
import java.util.PropertyResourceBundle;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.STConstant ;

/**
 * This singleton class helps with the internationalization of the messages to
 * be displayed to the user.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class I18N {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( I18N.class ) ;

    /** The property resource bundle for this application. */
    private static PropertyResourceBundle resourceBundle = null ;
    static {
        final InputStream is = I18N.class.getResourceAsStream(
                            STConstant.BASE_RES_PATH + "messages.properties" ) ;
        try {
            logger.debug( "Loading internationalized messages" ) ;
            resourceBundle = new PropertyResourceBundle( is ) ;
        }
        catch ( final IOException e ) {
            logger.error( "Could not load i18n messages", e ) ;
            throw new IllegalStateException( e ) ;
        }
    }

    /** Private constructor to enforce the singleton pattern. */
    private I18N() {
        super() ;
    }

    /** A static method to lookup a resource from the bundle. */
    private static String getMessage( final String key ) {
        return resourceBundle.getString( key ) ;
    }

    /**************************************************************************/
    /* List of constants representing messages in the resource bundle.        */
    /**************************************************************************/
    public static final String APP_NAME                         = getMessage( "APP_NAME" ) ;
    public static final String APP_TITLE                        = getMessage( "APP_TITLE" ) ;
    public static final String LBL_MI_WORK_OFFLINE              = getMessage( "LBL_MI_WORK_OFFLINE" ) ;
    public static final String LBL_MI_WORK_ONLINE               = getMessage( "LBL_MI_WORK_ONLINE" ) ;
    public static final String LBL_TOOLBAR_NAME                 = getMessage( "LBL_TOOLBAR_NAME" ) ;
    public static final String LBL_TASK_CONFIG_DLG_NAME         = getMessage( "LBL_TASK_CONFIG_DLG_NAME" ) ;
    public static final String MSG_USER_ACTION_FAILURE          = getMessage( "MSG_USER_ACTION_FAILURE" ) ;
    public static final String MSG_INVALID_INTEGER              = getMessage( "MSG_INVALID_INTEGER" ) ;
    public static final String MSG_INVALID_STRING               = getMessage( "MSG_INVALID_STRING" ) ;
    public static final String MSG_PROXY_HOST_INVALID           = getMessage( "MSG_PROXY_HOST_INVALID" ) ;
    public static final String MSG_PROXY_PORT_INVALID           = getMessage( "MSG_PROXY_PORT_INVALID" ) ;
    public static final String MSG_SAVE_FAILURE                 = getMessage( "MSG_SAVE_FAILURE" ) ;
    public static final String MSG_PROXY_USER_INVALID           = getMessage( "MSG_PROXY_USER_INVALID" ) ;
    public static final String MSG_PROXY_PWD_INVALID            = getMessage( "MSG_PROXY_PWD_INVALID" ) ;
    public static final String MSG_PROXY_DISABLED               = getMessage( "MSG_PROXY_DISABLED" ) ;
}
