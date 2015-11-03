/**
 * Creation Date: Aug 20, 2008
 */

package com.sandy.apps.pluto.biz.svc;

import java.util.List;

/**
 * This interface exposes operations for an asynchronous executor, which can
 * take instances of Runnable and schedule them import java.util.List ;
on.
 * The scheduling is hardened to ensure that worker malfunctions can not
 * cause the asynchronous executor to choke up.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface IAsyncExecutorSvc {

    /**
     * This interface should be implemented by classes whose instances can be
     * submitted for async execution.
     */
    public interface AsyncTask extends Runnable {

        /** Returns the name of this asynchronous task. */
        public String getName() ;
    } ;

    /**
     * Submits a runnable job for execution and returns immediately. The
     * submitted job is executed asynchronously.
     *
     * @param job The job to be executed in an asynchronous fashion.
     */
    void submit( final AsyncTask job ) ;

    /**
     * Submits all the asynchronous tasks for execution and waits for all of
     * them to complete before returning from this method.
     */
    void submitAndWait( final List<? extends AsyncTask> jobs ) ;
}
