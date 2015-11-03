/**
 * Creation Date: Aug 2, 2008
 */

package com.sandy.apps.pluto.shared.event;
import com.sandy.apps.pluto.shared.EventType ;

/**
 * A class encapsulating the event information.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class Event {

    /** Log4J loggerimport org.apache.log4j.Logger ;

import com.sandy.apps.pluto.shared.EventType ;
t.class ) ;

    /** The type of event that this class represents. */
    private final EventType eventType ;

    /** The value of this event. */
    private final Object value ;

    /** The time this event was generated. */
    private final long eventTime ;

    /**
     * Constructor.
     * @param eventType The type of event
     */
    public Event( final EventType eventType, final Object value ) {
        this.eventType = eventType ;
        this.value = value ;
        this.eventTime = System.currentTimeMillis() ;
    }

    /**
     * @return the eventType
     */
    public EventType getEventType() {
        return this.eventType ;
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return this.value ;
    }

    /**
     * @return the eventTime
     */
    public long getEventTime() {
        return this.eventTime ;
    }
}
