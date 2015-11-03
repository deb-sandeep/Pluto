/**
 * Creation Date: Jul 31, 2008
 */

package com.sandy.apps.pluto.shared.event;
import java.util.ArrayList ;
import java.util.Collection ;
import java.util.EnumMap ;
import java.util.List ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.shared.ConfigKey ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.util.util.ConfigManager ;
import com.sandy.apps.pluto.shared.util.util.StringUtil ;

/**
 * A singleton class which acts as an event bus within this application. This
 * class can register multiple subscribers and helps in dispatching events to
 * the registered subscribers in a synchronous or asynchronous fashion based
 * on the configuration 'event.bus.event.dispatch.async'.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class EventBus {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( EventBus.class ) ;

    /** The singleton instance. */
    private static EventBus BUS = null ;

    /**
     * The map holding and categorizing the registered subscribers of the
     * event bus.
     */
    private final EnumMap<EventType, List<IEventSubscriber>> subscriberMap =
                   new EnumMap<EventType, List<IEventSubscriber>>( EventType.class ) ;

    /** A boolean flag indicating if this event bus operates asynchronously. */
    private boolean asyncOperation = false ;

    /** Private constructor to prevent instantiation. */
    private EventBus() {
        super() ;
    }

    /** Singleton accessor method. */
    public static EventBus instance() {
        if( BUS == null ) {
            BUS = new EventBus() ;
            BUS.initialize() ;
        }
        return BUS ;
    }

    /**
     * Initializes the event bus by preparing the internal data structure and
     * setting up the logic for synchronous or asynchronous dispatching.
     */
    public void initialize() {
        final ConfigManager cfgMgr = ConfigManager.getInstance() ;
        this.asyncOperation = cfgMgr.getBoolean( ConfigKey.ASYNC_EVENT_DISPATCH, false ) ;
    }

    /**
     * Register a subscriber with a variable number of interested event types.
     * The added subscriber will be notified if an event is generated for
     * any of the interested event types.
     *
     * @param subscriber The subscriber instance to register.
     *
     * @param eventTypes The interested event types for which this subscriber
     *        will be notified by the bus. If the event types is null,
     *        this subscriber will be notified on all the events.
     */
    public void addSubscriberForEventTypes( final IEventSubscriber subscriber,
                                            final EventType... eventTypes ) {

        if( eventTypes == null || eventTypes.length == 0 ) {
            // If no specific event types are specified, it implies that this
            // subscriber is to be registered to all the event types.
            for( final EventType type : EventType.values() ) {
                addSubscriberForEventTypes( subscriber, type ) ;
            }
        }
        else {
            for( final EventType type : eventTypes ) {
                synchronized (this.subscriberMap ) {
                    List<IEventSubscriber> sbsList = this.subscriberMap.get( type ) ;
                    if( sbsList == null ) {
                        sbsList = new ArrayList<IEventSubscriber>() ;
                        this.subscriberMap.put( type, sbsList ) ;
                    }

                    // Adds the subscriber to the list of subscribers registered
                    // for this event type. If the subscriber is already present
                    // in the list, do not add another instance.
                    if( !sbsList.contains( subscriber ) ) {
                        // If we are dealing with an asynchronous event bus, wrap
                        // the subscriber in an async proxy and add it to the list
                        if( this.asyncOperation ) {
                            sbsList.add( new AsyncEventDispatchProxy( subscriber ) ) ;
                        }
                        else {
                            sbsList.add( subscriber ) ;
                        }
                    }
                }
            }
        }
    }

    /**
     * Register a subscriber against the event types whose name match the
     * specified patterns.
     *
     * @param subscriber The subscriber instance to register.
     *
     * @param eventTypePatterns A variable number of event type name patterns.
     *        Each string is matched against the event types in the system and
     *        the subscriber is registered to any event type for whom a
     *        match is found. If no patterns are specified, the subscriber is
     *        registered to receive all the events.
     */
    public void addSubscriberForEventPatterns( final IEventSubscriber subscriber,
                                               final String... eventTypesPatterns ) {

        if( eventTypesPatterns == null || eventTypesPatterns.length == 0 ) {
            // If no specific event types are specified, it implies that this
            // subscriber is to be registered to all the event types.
            for( final EventType type : EventType.values() ) {
                addSubscriberForEventTypes( subscriber, type ) ;
            }
        }
        else {
            for( String regexp : eventTypesPatterns ) {
                if( !StringUtil.isEmptyOrNull( regexp ) ) {
                    if( regexp.startsWith( "*" ) ) {
                        regexp = "." + regexp ;
                    }

                    final Pattern pattern = Pattern.compile( regexp ) ;
                    for( int i=0; i<EventType.values().length; i++ ) {

                        final EventType eventType = EventType.values()[i] ;
                        final Matcher   matcher   = pattern.matcher( eventType.name() ) ;

                        if( matcher.matches() ) {
                            addSubscriberForEventTypes( subscriber, eventType ) ;
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes the specified subscriber from the provided event types. Once this
     * method is called, notifications to the subscriber will not be sent for
     * the event types for which the subscriber is being removed.
     *
     * @param subscriber The subscriber instance to de-register.
     *
     * @param eventTypes The interested event types for which this subscriber
     *        will be notified by the bus. If the event types is null,
     *        this subscriber will be notified on all the events.
     */
    public void removeSubscriber( final IEventSubscriber subscriber,
                                  EventType... eventTypes ) {

        if( eventTypes == null || eventTypes.length == 0 ) {
            // If no specific event types are specified, it implies that this
            // subscriber is to be deregistered from all the event types.
            eventTypes = EventType.values() ;
        }

        AsyncEventDispatchProxy asyncProxy ;
        for( final EventType type : eventTypes ) {
            synchronized (this.subscriberMap ) {
                final List<IEventSubscriber> sbsList = this.subscriberMap.get( type ) ;
                if( sbsList != null && sbsList.contains( subscriber ) ) {

                    final int index = sbsList.indexOf( subscriber ) ;
                    final IEventSubscriber proxy = sbsList.get( index ) ;
                    if( proxy instanceof AsyncEventDispatchProxy ) {
                        asyncProxy = ( AsyncEventDispatchProxy )proxy ;
                        asyncProxy.stop() ;
                    }
                    sbsList.remove( index ) ;
                }
            }
        }
    }

    /**
     * Removes all the subscribers from this event bus.
     */
    public void removeAllSubscribers() {

        AsyncEventDispatchProxy asyncProxy = null ;
        synchronized ( this.subscriberMap ) {

            Collection<List<IEventSubscriber>> subscriberListCol = null ;
            subscriberListCol = this.subscriberMap.values() ;

            for( final List<IEventSubscriber> subsList : subscriberListCol ) {
                while( subsList.size() > 0 ) {
                    final IEventSubscriber proxy = subsList.remove( 0 ) ;
                    if( proxy instanceof AsyncEventDispatchProxy ) {
                        asyncProxy = ( AsyncEventDispatchProxy )proxy ;
                        asyncProxy.stop() ;
                    }
                }
            }

            this.subscriberMap.clear() ;
        }
    }

    /**
     * Publishes an event. All the subscribers registered to the given event
     * type are notified of the event. The notification happens either
     * synchronously or asynchronously depending upon the configuration
     * parameter 'event.bus.event.dispatch.async'.
     *
     * @param eventType The type of event being publishes.
     *
     * @param value The value associated with this event.
     */
    public static void publish( final EventType eventType, final Object value ) {
        BUS.publishEvent( eventType, value ) ;
    }

    /**
     * Publishes an event. All the subscribers registered to the given event
     * type are notified of the event. The notification happens either
     * synchronously or asynchronously depending upon the configuration
     * parameter 'event.bus.event.dispatch.async'.
     *
     * @param eventType The type of event being publishes.
     *
     * @param value The value associated with this event.
     */
    public void publishEvent( final EventType eventType, final Object value ) {

        synchronized ( this.subscriberMap ) {
            final Event event = new Event( eventType, value ) ;
            final List<IEventSubscriber> sbsList = this.subscriberMap.get( eventType ) ;
            if( sbsList != null && !sbsList.isEmpty() ) {
                for( final IEventSubscriber subscriber : sbsList ) {
                    try {
                        subscriber.handleEvent( event ) ;
                    }
                    catch ( final Throwable e ) {
                        // Harden the event propagation. A subscriber error
                        // should not be able to bring down the event bus
                        logger.error( "Exception while dispatching event", e ) ;
                    }
                }
            }
        }
    }
}
