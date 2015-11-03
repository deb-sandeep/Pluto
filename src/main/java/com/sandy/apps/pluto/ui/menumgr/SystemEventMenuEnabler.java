/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 14, 2008
 */

package com.sandy.apps.pluto.ui.menumgr;
import org.apache.log4j.Logger;

import com.sandy.apps.pluto.biz.svc.INetworkSvc ;
import com.sandy.apps.pluto.shared.Initializable ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.event.Event ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.event.IEventSubscriber ;
import com.sandy.apps.pluto.ui.UIConstant ;

/**
 * This singleton class is loaded by the dependency injection engine during
 * startup and after loading the menu manager. This class subscribes the system
 * events and maps those events into apporpriate actions related to enabling
 * and disabling the UI menu controls.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class SystemEventMenuEnabler implements Initializable, IEventSubscriber {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( SystemEventMenuEnabler.class ) ;

    /** Public constructor. */
    public SystemEventMenuEnabler() {
        super() ;
    }

    /** Registers itself with the event bus for all kinds of events. */
    @Override
    public void initialize() throws STException {
        logger.debug( "Initializing system event menu enabler" ) ;
        // Subscribe for all events.
        EventBus.instance().addSubscriberForEventTypes( this ) ;

        // Do some bookkeeping of events that have happended (possibly) before
        // this class was initialized. For example, check for network status etc.
        final INetworkSvc netSvc = ServiceMgr.getNetworkSvc() ;
        final MenuManager menuMgr = ServiceMgr.getMenuManager() ;
        menuMgr.enableActionCmd( UIConstant.AC_WORK_OFFLINE, netSvc.isOnline() ) ;
        menuMgr.enableActionCmd( UIConstant.AC_WORK_ONLINE, !netSvc.isOnline() ) ;
    }

    /** Depending upon the event, enables or disables certain menu items. */
    @Override
    public void handleEvent( final Event event ) {

        final MenuManager menuMgr = ServiceMgr.getMenuManager() ;
        switch ( event.getEventType() ) {
            case NETWORK_STATUS_CHANGE:
                final boolean status = ( Boolean )event.getValue() ;
                menuMgr.enableActionCmd( UIConstant.AC_WORK_OFFLINE, status ) ;
                menuMgr.enableActionCmd( UIConstant.AC_WORK_ONLINE, !status ) ;
                break ;

            default:
                break ;
        }
    }
}
