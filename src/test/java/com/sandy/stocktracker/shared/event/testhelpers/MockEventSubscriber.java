/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 2, 2008
 */

package com.sandy.stocktracker.shared.event.testhelpers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.event.Event ;
import com.sandy.apps.pluto.shared.event.IEventSubscriber ;

/**
 * Implementation of a mock event subscriber used to test the event bus
 * functionality.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class MockEventSubscriber implements IEventSubscriber {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( MockEventSubscriber.class ) ;

    /** A map storing the received events. */
    private final Map<EventType, List<Event>> eventMap = new HashMap<EventType, List<Event>>() ;

    /**
     * OVERRIDDEN METHOD:
     */
    public void handleEvent( final Event event ) {
        List<Event> evtList = this.eventMap.get( event.getEventType() ) ;
        if( evtList == null ) {
            evtList = new ArrayList<Event>() ;
            this.eventMap.put( event.getEventType(), evtList ) ;
        }
        evtList.add( event ) ;
    }

    /**
     * Returns the list of events received for the given event type.
     *
     * @param type The type of the event
     * @return A list of events received for this event type
     */
    public List<Event> getEvents( final EventType type ) {
        List<Event> events = this.eventMap.get( type ) ;
        if( events == null ) {
            events = Collections.emptyList() ;
        }
        return events ;
    }

    /**
     * Clears all the events received.
     */
    public void clearEvents() {
        this.eventMap.clear() ;
    }
}
