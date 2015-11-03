/**
 * Creation Date: Aug 20, 2008
 */

package com.sandy.apps.pluto.biz.svc.impl;
import java.util.ArrayList ;
import java.util.List ;
import java.util.concurrent.Callable ;
import java.util.concurrent.Executors ;
import java.util.concurrent.ThreadPoolExecutor ;
import java.util.concurrent.TimeUnit ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.svc.IAsyncExecutorSvc ;
import com.sandy.apps.pluto.shared.Initializable ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;

/**
 * Implementation of {@link IAsyncExecutorSvc} interface. Note that this
 * implementation is initializable and hence it is imperative that the creation
 * mechanism invokes the initialize method before the instance of this class
 * is ready for usage.
 * <p>
 * This class is design to be used as a singleton.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class AsyncExecutorSvc implements IAsyncExecutorSvc, Initializable {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( AsyncExecutorSvc.class ) ;

    /**
     * A hardned worker which wraps around the given runnable job and ensures
     * that the external runnable doesn't cause the executor to stall.
     *
     * @author Sandeep Deb [deb.sandeep@gmail.com]
     */
    private class HardnedWorker implements Callable<Object> {

        private final AsyncTask job ;

        public HardnedWorker( final AsyncTask job ) {
            this.job = job ;
        }

        @Override
        public Object call() throws Exception {
            try {
                logger.debug( "Executing async task " + this.job.getName() ) ;
                this.job.run() ;
            }
            catch ( final Throwable e ) {
                logger.error( "Async task resulted in an exception. Msg=" +
                              e.getMessage(), e ) ;
                LogMsg.error( "Error executing async task " + this.job.getName() +
                              "Msg = " + e.getMessage() ) ;
            }
            return null ;
        }
    }

    /** An instance of thread pool executor to which the jobs are submitted. */
    private ThreadPoolExecutor executor = null ;

    /**
     * INJECTABLE: This variable should be injected with the core pool size of
     * the executor. A default value of 10 is used in case this parameter is
     * not injected.
     */
    private int corePoolSize = 10 ;

    /** Public constructor. */
    public AsyncExecutorSvc() {
        super() ;
    }

    /**
     * @return the corePoolSize
     */
    public int getCorePoolSize() {
        return this.corePoolSize ;
    }

    /**
     * @param corePoolSize the corePoolSize to set
     */
    public void setCorePoolSize( final int corePoolSize ) {
        if( corePoolSize <= 0 ) {
            throw new IllegalArgumentException( "Core thread pool size can " +
                                                "not be a negative integer" ) ;
        }
        this.corePoolSize = corePoolSize ;
    }

    /**
     * This method should be invoked on this instance before this instance is
     * used for public usage. This method sets up the internal executor and
     * prepares the instance to receive asynchronous jobs.
     *
     * @throws STException In case the executor could not be initialized with
     *         the specified parameters.
     */
    @Override
    public void initialize() throws STException {

        this.executor = ( ThreadPoolExecutor )Executors.newFixedThreadPool( this.corePoolSize ) ;
        this.executor.setMaximumPoolSize( (int)(this.corePoolSize * 1.5) ) ;
        this.executor.setKeepAliveTime( 60, TimeUnit.SECONDS ) ;
    }

    /**
     * Submits the given job for asynchronous execution and returns immediately.
     */
    @Override
    public void submit( final AsyncTask job ) {
        this.executor.submit( new HardnedWorker( job ) ) ;
    }

    /**
     * Submits all the asynchronous tasks for execution and waits for all of
     * them to complete before returning from this method.
     */
    @Override
    public void submitAndWait( final List<? extends AsyncTask> jobs ) {

        final List<HardnedWorker> workers = new ArrayList<HardnedWorker>() ;
        for( final AsyncTask job: jobs ) {
            workers.add( new HardnedWorker( job ) ) ;
        }
        try {
            this.executor.invokeAll( workers ) ;
        }
        catch ( final InterruptedException e ) {
            logger.error( "Interrupted exception waiting for tasks to finish", e ) ;
        }
    }
}
