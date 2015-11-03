/**
 * Creation Date: Aug 20, 2008
 */

package com.sandy.apps.pluto.biz.svc;
import java.lang.reflect.Method ;

import org.apache.log4j.Logger ;
import org.springframework.beans.BeansException ;

import com.sandy.apps.pluto.biz.svc.IAsyncExecutorSvc.AsyncTask ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.util.util.BizObjectFactory ;
import com.sandy.apps.pluto.shared.util.util.ReflectionUtil ;
import com.sandy.apps.pluto.shared.util.util.SpringObjectFactory ;

/**
 * This class creates instances of async tasks which can invoke operations on
 * services exposed via Spring configuration in an asynchronous fashion.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class MethodExecutionAsyncTask implements AsyncTask {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( MethodExecutionAsyncTask.class ) ;

    private String beanName ;
    private String opName ;

    private final Object bean ;
    private final Method operation ;

    private Object[] params = null ;

    /**
     * Public constructor which takes identifiers for method with parameters.
     *
     * @param bean The instance on which the method needs to be invoked asynchronously
     * @param method The method which needs to be invoked
     * @param params The parameters required for the method invocation
     *
     * @throws IllegalArgumentException In case the bean or the operation on the
     *         bean can not be resolved.
     */
    public MethodExecutionAsyncTask( final Object bean, final Method method,
                                     final Object[] params )
        throws IllegalArgumentException {
        super() ;
        this.bean = bean ;
        this.operation = method ;
        this.params = params ;
    }

    /**
     * Public constructor which takes identifiers for method with no parameters
     *
     * @param beanName The name of the bean on which the operation is to be invoked
     * @param opName The name of the operation to invoke.
     *
     * @throws IllegalArgumentException In case the bean or the operation on the
     *         bean can not be resolved.
     */
    public MethodExecutionAsyncTask( final String beanName, final String opName )
        throws IllegalArgumentException {
        this.beanName = beanName ;
        this.opName = opName ;

        try {
            final SpringObjectFactory of = BizObjectFactory.getInstance() ;
            this.bean = of.getBean( this.beanName ) ;
            this.operation = ReflectionUtil.getMethodIfAvailable( this.bean.getClass(),
                                                                  this.opName, null ) ;
        }
        catch ( final BeansException e ) {
            throw new IllegalArgumentException( "The specified bean and method," +
                    this.beanName + "::" + this.opName + " could not be resolved", e ) ;
        }
    }

    /** Returns an identifying name for this async task. */
    @Override
    public String getName() {
        return "MethodAsyncTask[" + this.operation.getName() + "]" ;
    }

    /**
     * Executes the specified operation on the instance of the specified bean
     * via reflection mechanisms.
     */
    public void run() {
        try {
            logger.debug( "Invoking " + getName() ) ;
            this.operation.invoke( this.bean, this.params ) ;
        }
        catch ( final Throwable e ) {
            // Harden the run method so that the thread is not fucked.
            logger.error( getName() + " execution failure", e ) ;
            LogMsg.error( getName() + " execution failure. Msg = " + e.getMessage() ) ;
        }
    }
}
