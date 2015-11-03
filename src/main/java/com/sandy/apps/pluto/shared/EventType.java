/**
 * Creation Date: Aug 1, 2008
 */

package com.sandy.apps.pluto.shared;

import com.sandy.apps.pluto.shared.dto.JobConfig ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.dto.Trade ;
import com.sandy.apps.pluto.shared.event.EventBus ;

/**
 * This interface defines static constants for event types by which the event
 * subscribers can register onto the {@link EventBus} for receiving notifications.
 * Event types provided in this interface can be added together by applying
 * the or operator with each other or simply by adding them.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public enum EventType {

    /** The event type for ITD insertion events. */
    EVT_SCRIP_ITD_INSERT,

    /** High resolution ITD value for the scrip was downloaded. */
    EVT_HI_RES_SCRIP_ITD_INSERT,

    /** The event type for NSE index ITD import events. */
    EVT_NSE_INDEX_ITD_INSERT,

    /** High resolution ITD value for the index was downloaded. */
    EVT_HI_RES_NSE_INDEX_ITD_INSERT,

    /** The event type for EOD batch upload events. */
    EVT_BHAVCOPY_IMPORT_SUCCESS,

    /**
     * Message event type - an event type used to convey messages. The value
     * of this event type is an instance of {@link LogMsg} containing the
     * details of the log message.
     */
    MSG,

    /** Published when an user preference changes. */
    USER_PREF_CHANGED,

    /** ============= NETWORK RELATED EVENTS ================================ */
    /**
     * Published when the connectivity status of the network changes. The
     * value of this event is a Boolean value indicating the state of the
     * network. Boolean.TRUE implies online, Boolean.FALSE indicates offline.
     */
    NETWORK_STATUS_CHANGE,

    /**
     * Event indicating that information has been downloaded from the network.
     * This event is accompanied by a value (Long) indicating the number of
     * bytes that have been downloaded. This event is fired on all successful
     * network communications.
     */
    NETWORK_DATA_DOWNLOADED,

    /**
     * Event indicating the last network communication status. This is published
     * after every network communication and communicates the outcome of the
     * communication. If the communication was successful the value of this
     * event holds a Boolean.TRUE, else FALSE. This event is fired on all
     * successful communications.
     */
    NETWORK_COMM_STATUS,

    /** ============= JOB EXECUTION RELATED EVENTS=========================== */
    /**
     * This event is published when a particular job is being started by the
     * Job service. The receipt of this event does not imply that the job has
     * successfully started, it simply implies that an attempt is being made
     * to start the job. The value of this event is the {@link JobConfig} instance
     * which is being scheduled.
     */
    JOB_STARTING,

    /**
     * This event is published when a particular job has been successfully
     * scheduled for execution. The value of this event is the {@link JobConfig}
     * instance which has been scheduled.
     */
    JOB_STARTED,

    /**
     * This event is published when a particular job is being stopped by the
     * Job service. The receipt of this event does not imply that the job has
     * successfully stopped, it simply implies that an attempt is being made
     * to stop the job. The value of this event is the {@link JobConfig} instance
     * which is being stopped.
     */
    JOB_STOPPING,

    /**
     * This event is published when a particular job has been successfully
     * stopped for execution. The value of this event is the {@link JobConfig}
     * instance which has been stopped.
     */
    JOB_STOPPED,

    /**
     * This event is published when a particular job is being executed. The value
     * of this event is a {@link JobConfig} instance.
     */
    JOB_EXECUTING,

    /** ============= INTERNAL FRAME RELATED EVENTS ========================= */
    /**
     * This event is published when any of the internal frames is added to the
     * desktop pane. The value of this event is a reference to the internal frame
     * being minimized.
     */
    INTERNAL_FRAME_ADDED,

    /**
     * This event is published when any of the internal frames is closed by the
     * user. The value of this event is a reference to the internal frame
     * being closed.
     */
    INTERNAL_FRAME_CLOSED,

    /** ============= RSS IMPORT RELATED EVENTS ============================= */
    /**
     * This event is published when one or more news RSS news items are inserted
     * into the database. The value of this event is a list of RSSNewsItem
     * instances. Please note that since the news items are fresh from the oven,
     * the description is populated however the receiver should not make this
     * assumption and fetch individual details as and when required.
     */
    RSS_NEWS_IMPORTED,

    /** ============= TRADE RELATED EVENTS ================================== */
    /**
     * This event is published when a new trade is entered into the system. The
     * value of the event is a {@link Trade} instance with a valid trade id.
     */
    TRADE_ADDED,

    /**
     * This event is published when a trade is deleted the system. The
     * value of the event is a {@link Trade} instance that was deleted.
     */
    TRADE_DELETED,

    /**
     * This event is published when a trade is updated in the system. The
     * value of the event is a {@link Trade} instance that was updated. The
     * value contains the updated trade values.
     */
    TRADE_UPDATED,
}
