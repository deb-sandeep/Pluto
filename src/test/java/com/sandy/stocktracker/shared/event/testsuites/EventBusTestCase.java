/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 2, 2008
 */

package com.sandy.stocktracker.shared.event.testsuites;

import java.net.URL;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.ConfigKey ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.util.util.ConfigManager ;
import com.sandy.stocktracker.shared.event.testhelpers.MockEventSubscriber;

/**
 * This test case contains test for the {@link EventBus} class.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class EventBusTestCase extends TestCase {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( EventBusTestCase.class ) ;

    /** The configuration URL for this class. */
    static final URL CFG_FILE_URL = EventBusTestCase.class.getResource(
            "/com/sandy/stocktracker/shared/event/testresources/config.properties" ) ;

    private static EventBus BUS = EventBus.instance() ;

    private static final ConfigManager cfgManager = ConfigManager.getInstance() ;

    public void setUp() throws Exception {
        cfgManager.clear() ;
        cfgManager.initialize( CFG_FILE_URL ) ;
        BUS.removeAllSubscribers() ;
    }

    /**
     * FEATURE: Register a simple subscribe and publish an event to the bus
     */
    public void testRegisterAndPublishSync() {
        final MockEventSubscriber subs = new MockEventSubscriber() ;

        cfgManager.setProperty( ConfigKey.ASYNC_EVENT_DISPATCH, Boolean.FALSE ) ;
        BUS.initialize() ;
        BUS.addSubscriberForEventTypes( subs, EventType.EVT_BHAVCOPY_IMPORT_SUCCESS ) ;

        EventBus.publish( EventType.EVT_BHAVCOPY_IMPORT_SUCCESS, "Test" ) ;
        assertEquals( 1, subs.getEvents( EventType.EVT_BHAVCOPY_IMPORT_SUCCESS ).size() ) ;
    }

    /**
     * FEATURE: Register a simple subscribe and publish an event to the bus,
     *          the event should be received. De-register the subscriber
     *          and publish the event again, the event should not be received
     */
    public void testDeRegister() {
        final MockEventSubscriber subs = new MockEventSubscriber() ;
        BUS.addSubscriberForEventTypes( subs, EventType.EVT_BHAVCOPY_IMPORT_SUCCESS ) ;

        EventBus.publish( EventType.EVT_BHAVCOPY_IMPORT_SUCCESS, "Test" ) ;
        assertEquals( 1, subs.getEvents( EventType.EVT_BHAVCOPY_IMPORT_SUCCESS ).size() ) ;

        subs.clearEvents() ;
        BUS.removeSubscriber( subs, EventType.EVT_BHAVCOPY_IMPORT_SUCCESS ) ;
        EventBus.publish( EventType.EVT_BHAVCOPY_IMPORT_SUCCESS, "Test" ) ;
        assertEquals( 0, subs.getEvents( EventType.EVT_BHAVCOPY_IMPORT_SUCCESS ).size() ) ;
    }

    /**
     * FEATURE: Register a simple subscribe and publish an event asynchronously
     */
    public void testRegisterAndPublishASync()
        throws Exception {

        final MockEventSubscriber subs = new MockEventSubscriber() ;

        cfgManager.setProperty( ConfigKey.ASYNC_EVENT_DISPATCH, Boolean.TRUE ) ;
        BUS.initialize() ;
        BUS.addSubscriberForEventTypes( subs, EventType.EVT_BHAVCOPY_IMPORT_SUCCESS ) ;

        EventBus.publish( EventType.EVT_BHAVCOPY_IMPORT_SUCCESS, "Test" ) ;
        Thread.sleep( 100 ) ;
        assertEquals( 1, subs.getEvents( EventType.EVT_BHAVCOPY_IMPORT_SUCCESS ).size() ) ;
    }
}
