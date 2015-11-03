/**
 * Creation Date: Aug 2, 2008
 */

package com.sandy.apps.pluto.shared.event;
import java.util.concurrent.LinkedBlockingQueue ;

import org.apache.log4j.Logger ;

/**
 * An implementation of {@link IEventSubscriber}, which wraps around concrete
 * implementations of subscribers and dispatches events to them in an
 * asynchronous fashion.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
class AsyncEventDispatchProxy implements IEventSubscriber, Runnable {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( AsyncEventDispatchProxy.class ) ;

    /**
     * The concrete implementation of {@link IEventSubscriber} which this
     * class is a proxy for.
     */
    private final IEventSubscriber subscriber ;

    /**
     * The dispatch thread. This is created if and only if the dispatch has
     * to be done asynchronously.
     */
    private Thread dispatchThread = null ;

    /**
     * A boolean flag which advises this dispatch proxy to stop dispatching
     * events and purge the remaining events from the event queue.
     */
    private boolean stop = false ;

    /** The unbounded queue in which events are stored before dispatching. */
    private final LinkedBlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>() ;

    /**
     * Constructor, which accepts the concrete implementation of the subscriber
     * to proxy.
     *
     * @param subscriber A concrete implementation of IEventSubscriber
     *
     * @param async A boolean flag which indicates if the event dispatch
     *        has to be done asynchronously.
     */
    public AsyncEventDispatchProxy( final IEventSubscriber subscriber ) {
        this.subscriber = subscriber ;
        this.dispatchThread = new Thread( this ) ;
        this.dispatchThread.setDaemon( true ) ;
        this.dispatchThread.start() ;
    }

    /**
     * OVERRIDDEN METHOD: Dispatches the events in the queue to the subscriber
     * asynchronously and blocks when the dispatch queue is empty.
     */
    public void run() {
        while( !this.stop ) {
            Event evt = null ;
            try {
                evt = this.eventQueue.take() ;
                this.subscriber.handleEvent( evt ) ;
            }
            catch ( final Throwable e ) {
                logger.error( "Dispatch failed for event " + evt, e ) ;
            }
        }
        this.eventQueue.clear() ;
    }

    /**
     * OVERRIDDEN METHOD: Adds the event to the queue for dispatching asynchronously
     * by the dispatch thread.
     */
    public void handleEvent( final Event event ) {
        this.eventQueue.add( event ) ;
    }

    /**
     * @return the subscriber
     */
    public IEventSubscriber getSubscriber() {
        return this.subscriber ;
    }

    /**
     * Advises the dispatch proxy to stop dispatching and purge all the remaining
     * events in the queue
     */
    public void stop() {
        this.stop = true ;
    }

    /**
     * OVERRIDDEN METHOD: Delegates the equating to the underlying subscriber.
     */
    public boolean equals( final Object obj ) {
        return this.subscriber.equals( obj ) ;
    }

    /**
     * OVERRIDDEN METHOD: Returns the hash code of the underlying subscriber
     */
    public int hashCode() {
        return this.subscriber.hashCode() ;
    }
}
