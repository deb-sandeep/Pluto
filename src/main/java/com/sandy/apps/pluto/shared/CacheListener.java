/**
 * Creation Date: Nov 21, 2008
 */

package com.sandy.apps.pluto.shared;

/**
 * This interface defines the protocol of communication between any cache and
 * its listeners. This interface is implemented by the listeners interested
 * in listening to data changes in the cache. Once instances of the listeners
 * are registered with the cache, they will be called back upon the change
 * in cached data.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface CacheListener {

    /**
     * This method is called upon the cache listener when the underlying
     * cache value has changed. It is upto the listener implementation to
     * treat this as a trigger and communicate directly with the cache for
     * fetching further change details.
     */
    void cacheDataChanged() ;
}
