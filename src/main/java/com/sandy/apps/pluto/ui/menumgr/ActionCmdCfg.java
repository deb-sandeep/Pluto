/**
 * Creation Date: Aug 13, 2008
 */

package com.sandy.apps.pluto.ui.menumgr;
import java.lang.reflect.Method ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.svc.MethodExecutionAsyncTask ;
import com.sandy.apps.pluto.shared.ErrorCode ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.util.util.BizObjectFactory ;
import com.sandy.apps.pluto.shared.util.util.ReflectionUtil ;
import com.sandy.apps.pluto.shared.util.util.SpringObjectFactory ;

/**
 * This class encapsulates the information regarding an action command. One
 * action command can be associated with a abstract button representing a menu
 * item. An action command is linked to a target, which is essentially a spring
 * bean and method definition. The action command is also associated with a list
 * of disable and enable of click action commands.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ActionCmdCfg {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ActionCmdCfg.class ) ;

    // Configuration properties
    private String name        = null ;
    private String targetBean  = null ;
    private String targetOp    = null ;
    private boolean async      = false ;

    // Derived values
    private Object bean = null ;
    private Method method = null ;

    private final List<String>  acToEnableOnClick  = new ArrayList<String>() ;
    private final List<String>  acToDisableOnClick = new ArrayList<String>() ;

    /** Public constructor. */
    public ActionCmdCfg() {
        super() ;
    }

    /**
     * DIGESTER CALLBACK: This method is invoked by the parser during the
     * act of parsing the menu configuration XML file.
     *
     * @param actionCommand The action command to enable when this menu item
     *        is clicked.
     */
    public void addEnableOnClick( final String actionCommand ) {
        this.acToEnableOnClick.add( actionCommand ) ;
    }

    /**
     * DIGESTER CALLBACK: This method is invoked by the parser during the
     * act of parsing the menu configuration XML file.
     *
     * @param actionCommand The action command to disable when this menu item
     *        is clicked.
     */
    public void addDisableOnClick( final String actionCommand ) {
        this.acToDisableOnClick.add( actionCommand ) ;
    }

    /** Invokes the method on the target bean. */
    public void invoke() throws STException {
        try {
            if( isAsync() ) {
                MethodExecutionAsyncTask task = null ;
                task = new MethodExecutionAsyncTask( this.targetBean, this.targetOp ) ;
                ServiceMgr.getAsyncExecutorSvc().submit( task ) ;
            }
            else {
                this.method.invoke( this.bean, (Object[])null ) ;
            }
        }
        catch ( final Exception e ) {
            logger.error( "Could not invoke action " + this.name, e ) ;
            throw new STException( "Action command failure ", e, ErrorCode.UNKNOWN_EXCEPTION ) ;
        }
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
     * @return the targetBean
     */
    public String getTargetBean() {
        return this.targetBean ;
    }

    /**
     * @param targetBean the targetBean to set
     */
    public void setTargetBean( final String targetBean ) {
        final SpringObjectFactory of = BizObjectFactory.getInstance() ;
        this.bean = of.getBean( targetBean ) ;
        this.targetBean = targetBean ;
    }

    /**
     * @return the targetOp
     */
    public String getTargetOp() {
        return this.targetOp ;
    }

    /**
     * @param targetOp the targetOp to set
     */
    public void setTargetOp( final String targetOp ) {

        this.method = ReflectionUtil.getMethodIfAvailable( this.bean.getClass(),
                                                           targetOp, null ) ;
        if( this.method == null ) {
            throw new IllegalArgumentException( "No method by name " + targetOp +
                    " found in class " + this.bean.getClass().getName() +
                    " for bean name = " + this.targetBean ) ;
        }
        this.targetOp = targetOp ;
    }

    /**
     * @return the acToEnableOnClick
     */
    public List<String> getAcToEnableOnClick() {
        return this.acToEnableOnClick ;
    }

    /**
     * @return the acToDisableOnClick
     */
    public List<String> getAcToDisableOnClick() {
        return this.acToDisableOnClick ;
    }

    /**
     * @return the async
     */
    public boolean isAsync() {
        return this.async ;
    }

    /**
     * @param async the async to set
     */
    public void setAsync( final String async ) {
        this.async = Boolean.parseBoolean( async ) ;
    }
}
