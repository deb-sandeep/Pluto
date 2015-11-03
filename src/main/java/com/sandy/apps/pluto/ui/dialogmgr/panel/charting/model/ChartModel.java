/**
 * Creation Date: Oct 12, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model;
import java.util.ArrayList ;
import java.util.Calendar ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.Hashtable ;
import java.util.List ;
import java.util.Map ;

import org.apache.commons.lang.math.FloatRange ;
import org.apache.commons.lang.math.LongRange ;
import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.STUtils ;
import com.sandy.apps.pluto.shared.dto.AbstractITDValue ;
import com.sandy.apps.pluto.shared.dto.ExIndexITD ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.dto.ScripITD ;
import com.sandy.apps.pluto.shared.event.Event ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.event.IEventSubscriber ;
import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartEntity.EntityType ;

/**
 * The charting model contains the data organized in the format suitable for
 * display. It provides read only view of the data to the UI, which uses the
 * data for rendering.
 * <p>
 * The model is fed the data by the controller. The model never talks directly
 * to the controller. The model notifies the view that the underlying data
 * has changed and upon receiving the notification, the view can initiate
 * multiple read only operations to extract the data for rendering.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ChartModel implements IEventSubscriber, UIConstant {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ChartModel.class ) ;

    /**
     * An interface defines the contract for the chart model listeners.
     * There are going to be many entities interested in model changes. For
     * example, if a charting entity is being added to the model, the
     * participant panel needs to add a corresponding button, the control
     * panel needs to update it's drop down, the view needs to refresh itself
     * in speculation of any changed data etc. All the interested listeners
     * can register themselves with this model to get notified on interested
     * events.
     */
    public interface ModelListener {

        /**
         * Notify the listener that the model has changed. The model event
         * will contain specific information pertaining to the event type and
         * any value associated with the event.
         *
         * @param event The {@link ModelEvent} that is generated.
         */
        void modelChanged( final ModelEvent event ) ;
    }

    /** An enumeration of possible model event types. */
    public enum ModelEventType {
        PRIMARY_SERIES_ADDED,
        PRIMARY_SERIES_REMOVED,
        MODEL_DATA_CHANGED
    } ;

    /**
     * A generic event to transfer the information associated with a model
     * event.
     */
    public static class ModelEvent {

        private final ModelEventType type ;
        private final Object         value ;

        private ModelEvent( final ModelEventType type, final Object value ) {
            this.type = type ;
            this.value = value ;
        }

        public ModelEventType getType() { return this.type ; }

        public Object getValue() { return this.value ; }
    }

    /** The list of listeners interested in this model. */
    private final List<ModelListener> listeners = new ArrayList<ModelListener>() ;

    /** A map of primary entities currently being managed by this chart.*/
    private final Map<String, ChartEntity> entityMap = new HashMap<String, ChartEntity>() ;

    /** The start of the time range for which data is being currently rendered.*/
    private Date renderStartTime = null ;

    /** The end of the time range for which data is being rendered. */
    private Date renderEndTime = null ;

    /**
     * The base time against which the fixed range markers are calculated. This
     * time is set the first time any scrip, index or chartable entity is
     * added to the chart.
     */
    private Date baseChartDate = null ;

    /** The fixed time range that this model is operating against.*/
    private String fixedRangeId = UIConstant.RANGE_1D ;

    /**
     * This flag will be set to true every time the time interval changes. This
     * will give us a trigger to refresh the render value cache.
     */
    private boolean cacheRefreshRequired = false ;

    /**
     * A data structure holding the cache data to be given to the rendering
     * engine. This cache will be refreshed every time some input to the model
     * changes which require a re-computation of the cache, for example change
     * in the time range or addition of a new entity etc.
     */
    private final Map<ChartEntity, List<EODValue>> renderDataCache =
                                    new Hashtable<ChartEntity, List<EODValue>>() ;

    /**
     * A part of render cache, where we cache the Y axis range based on the
     * high low values of the chart entity data present in the render cache.
     * This value will be updated as a part of the cache refresh process.
     */
    private FloatRange yAxisRange = new FloatRange( 0, 1 ) ;

    /** The range for the volume chart. */
    private LongRange volAxisRange = null ;

    /** Constructor taking in the chart UI which needs notification of data change. */
    public ChartModel() {
        super() ;
        // Add the Chart model as a subscriber for Scrip ITD and Index ITD events.
        // NOTE: Remember to unsubscribe during destruction
        final EventBus bus = EventBus.instance() ;
        bus.addSubscriberForEventTypes( this, EventType.EVT_SCRIP_ITD_INSERT ) ;
        bus.addSubscriberForEventTypes( this, EventType.EVT_NSE_INDEX_ITD_INSERT ) ;
        bus.addSubscriberForEventTypes( this, EventType.EVT_HI_RES_SCRIP_ITD_INSERT ) ;
        bus.addSubscriberForEventTypes( this, EventType.EVT_HI_RES_NSE_INDEX_ITD_INSERT ) ;
    }

    /**
     * Destroys the model by unsubscribing itself from the event bus.
     */
    public void destroy() {
        // Unsubscribe the model from the event bus. The model would be listening
        // to ITD and EOD updates to keep the chart updated.
        EventBus.instance().removeSubscriber( this, EventType.EVT_SCRIP_ITD_INSERT ) ;
        EventBus.instance().removeSubscriber( this, EventType.EVT_NSE_INDEX_ITD_INSERT ) ;
        EventBus.instance().removeSubscriber( this, EventType.EVT_HI_RES_SCRIP_ITD_INSERT ) ;
        EventBus.instance().removeSubscriber( this, EventType.EVT_HI_RES_NSE_INDEX_ITD_INSERT ) ;
    }

    /** Adds a model listener. */
    public void addModelListener( final ModelListener listener ) {
        this.listeners.add( listener ) ;
    }

    /** Removes a model listener. */
    public void removeModelListener( final ModelListener listener ) {
        this.listeners.remove( listener ) ;
    }

    /** Notifies the listeners. */
    private void notifyListeners( final ModelEventType eventType,
                                  final Object value ) {

        final ModelEvent evt = new ModelEvent( eventType, value ) ;
        for( final ModelListener listener : this.listeners ) {
            try {
                listener.modelChanged( evt ) ;
            }
            catch ( final Exception e ) {
                logger.error( "Model listener " + listener.getClass() +
                              " generated an exception", e ) ;
            }
        }
    }

    /** Returns true if the given entity is being managed by this chart. */
    public boolean containsEntity( final String entityName ) {
        return this.entityMap.containsKey( entityName ) ;
    }

    /**
     * Adds a list of primary entities to this chart. An entity is added if
     * and only if it is not currently being managed by this chart, to prevent
     * duplication.
     * <p>
     * A notification is sent out to all the listeners after the model has
     * been primed with all the entity related data points.
     *
     * @param entity The primary charting entity to add to the chart.
     *
     */
    public synchronized void addPrimaryEntities( final List<ChartEntity> entities ) {

        // If we have an empty list, there is nothing to do - return
        if( entities == null || entities.isEmpty() ) {
            return ;
        }

        for( final ChartEntity entity : entities ) {

            // If the given entity is already being managed by this model, return.
            if( containsEntity( entity.getName() ) ) {
                return ;
            }

            // If this is the first entity being added to the chart, deduce the
            // time interval. For example, if a ScripITD is being added, the
            // time interval would be for the date of the ITD value. Or, if the
            // value is ScripEOD then the range would be the date of the EOD value
            // till today.
            if( this.entityMap.isEmpty() ) {
                deduceTimeInterval( entity ) ;
                // We don't need a cache refresh for this boundary cache as
                // we will update the cache for all the symbols being added
                // after the loop. This assignment seems redundant !!
                this.cacheRefreshRequired = false ;
            }

            // Ask the entity to load the data for the display time range if it
            // does not already contain the data. NOTE that the deduce time
            // interval also tries to refresh all the entities data, so one
            // might think that for the first entry being added, the data would
            // be loaded twice, one by deduce time interval and once again by
            // this load request - this is not correct. The deduce time interval
            // operation only loads data for all the existing entities and
            // if we are dealing with the first entity, it would not have been
            // added to the map and hence the deduce time interval method will
            // not be able to request the entity to load the data.
            try {
                entity.loadDataForInterval( this.renderStartTime,
                                       this.renderEndTime, isIntradayRange() ) ;
            }
            catch ( final STException e ) {
                LogMsg.error( "Could not load data for entity " + entity.getName() ) ;
                logger.error( "Unanticipated exception loading entity data", e ) ;
            }

            // Add the entity to the book keeping data structure.
            this.entityMap.put( entity.getName(), entity ) ;
        }

        // Refresh the render cache for all the entities in the chart. We
        // could have optimized this by loading the data for only the entry
        // being added but this would be too much of a trouble since we would
        // also have to track the old range etc. Decided to refresh the entire
        // model.
        refreshRenderCache() ;

        notifyListeners( ModelEventType.PRIMARY_SERIES_ADDED, entities ) ;
        notifyListeners( ModelEventType.MODEL_DATA_CHANGED, null ) ;
    }

    /**
     * Deduces the render time interval for the model based on the type of
     * chart entity being added to the chart. This method is typically called
     * for the first entry that is being added to the chart. See the
     * addPrimaryEntity for more details on when this method is invoked.
     *
     * @param entity The chart entity being added to the model.
     */
    private void deduceTimeInterval( final ChartEntity entity ) {

        final ChartEntityConfig config = entity.getConfig() ;

        final Date time = config.getTime() ;

        this.renderStartTime = STUtils.getStartOfDay( time ) ;
        this.renderEndTime   = STUtils.getEndOfDay( time ) ;
        this.baseChartDate   = this.renderStartTime ;

        // Now reset the render start and end based on the current time range.
        // You might ask - why is this required. Well, imagine the scenario
        // where the user has added an entity, played with it and set the
        // range to 6M and then removed it. As soon as the entity is removed
        // all the toggle buttons are disabled - but the 6M is still selected
        // Next time when the user adds an entity, the buttons would be
        // enabled and he would expect a 6M display.
        if( this.entityMap.isEmpty() ) {
            // If this is the first entry being added to the chart, we deduce
            // the intial time interval based on the type of entity we are
            // adding. If we have a time, for which hour is 00, it implies that
            // we are dealing with a EOD entity - treat the time interval as
            // 2 weeks
            final Calendar cal = Calendar.getInstance() ;
            cal.setTime( time ) ;
            if( cal.get( Calendar.HOUR_OF_DAY ) == 0 ) {
                this.fixedRangeId = UIConstant.RANGE_1M ;
            }
            else {
                this.fixedRangeId = UIConstant.RANGE_1D ;
            }
        }
        changeTimeRange( this.fixedRangeId ) ;

        this.cacheRefreshRequired = true ;
    }

    /**
     * Deletes an entity from the chart model. Please note that the entity
     * requested for removal can be either a primary entity or a derived
     * entity. A derived entity is identified by the existence of a '.' separator
     * in the entity name. If the entity to be removed is a primary entity,
     * we removed the entity and all the related derived entities from the model.
     * In case the entity is a derived entity, we identify the associated
     * primary entity and delegate the removal to the primary entity.
     *
     * @param entityName
     */
    public synchronized void deletePrimaryEntity( final String entityName ) {

        final int indexOfDot = entityName.indexOf( '.' ) ;

        ChartEntity entity = null ;
        if( indexOfDot != -1 ) {
            // This is a derived entity. Deduce the primary entity name
            final String primaryEntityName = entityName.substring( 0, indexOfDot ) ;
            final String derivedEntityName = entityName.substring( indexOfDot + 1 ) ;
            entity = this.entityMap.get( primaryEntityName ) ;

            if( entity != null ) {
                entity.removeDerivedEntity( derivedEntityName ) ;
            }
        }
        else {
            // The entity for removal is a primary entity. Remove it directly
            // from the entity cache.
            entity = this.entityMap.get( entityName ) ;
            if( entity != null ) {
                this.entityMap.remove( entityName ) ;
            }
        }

        if( entity != null ) {
            refreshRenderCache() ;
            notifyListeners( ModelEventType.PRIMARY_SERIES_REMOVED, entityName ) ;
        }
        else {
            logger.info( "Could not find entity " + entityName + " for removal" ) ;
        }
    }

    /** Returns the range of Y axis values based on the current render cache. */
    public FloatRange getYAxisRange() {
        // If the time range has changed since the last call to this method
        // recompute the entire render cache.
        if( this.cacheRefreshRequired ) {
            refreshRenderCache() ;
        }
        return this.yAxisRange ;
    }

    /** Returns the range of Y axis for volume chart. */
    public LongRange getVolAxisRange() {

        // If the time range has changed since the last call to this method
        // recompute the entire render cache.
        if( this.cacheRefreshRequired ) {
            refreshRenderCache() ;
        }
        return this.volAxisRange ;
    }

    /**
     * Refreshes the render cache for all the chart entities. Also computes
     * the Y Axis range from the cross section of chart entity render data.
     * This function refreshes the cache irrespective of the chacheRefreshRequired
     * boolean flag.
     */
    private void refreshRenderCache() {
        if( logger.isDebugEnabled() ) {
            logger.debug( "Refreshing render cach for time range - " ) ;
            logger.debug( "\tStart time = " + this.renderStartTime ) ;
            logger.debug( "\tEnd Time   = " + this.renderEndTime ) ;
        }

        this.renderDataCache.clear() ;
        Map<ChartEntity, List<EODValue>> cache = null ;
        final boolean absolute = this.entityMap.size() == 1 ;

        for( final ChartEntity entity : this.entityMap.values() ) {
            cache = entity.getValues( this.renderStartTime, this.renderEndTime, absolute ) ;
            this.renderDataCache.putAll( cache ) ;
        }

        // Now we calculate the Y axis range.
        calculateYAxisRange() ;

        this.cacheRefreshRequired = false ;
    }

    /**
     * Returns true if the time range is to be considered for rendering ITD
     * data. ITD data is rendered till we go above 2W range.
     *
     * @return true if the fixed range ID is equal to or less than 2W, false
     *         otherwise.
     */
    public boolean isIntradayRange() {
        boolean itdRange = false ;
        if( this.fixedRangeId.equals( RANGE_1D ) || this.fixedRangeId.equals( RANGE_2D ) ||
            this.fixedRangeId.equals( RANGE_3D ) || this.fixedRangeId.equals( RANGE_4D ) ||
            this.fixedRangeId.equals( RANGE_5D ) || this.fixedRangeId.equals( RANGE_2W ) ) {
            itdRange = true ;
        }
        return itdRange ;
    }

    /** Calculates the Y axis range based on the values in the render cache. */
    private synchronized void calculateYAxisRange() {

        float lower = Float.MAX_VALUE ;
        float upper = Float.MIN_VALUE ;

        long    maxVol= 0 ;
        boolean calcVolRange = false ;

        final boolean itdRange = isIntradayRange() ;
        final boolean absolute = this.entityMap.size() <= 1 ;

        if( this.renderDataCache.isEmpty() ) {
            lower = 0F ;
            upper = 1F ;
        }
        else {
            if( this.entityMap.size() == 1 &&
                this.entityMap.values().iterator().next().getEntityType() == EntityType.SCRIP ) {
                calcVolRange = true ;
            }

            for( final List<EODValue> eodList : this.renderDataCache.values() ) {
                for( final EODValue eod : eodList ) {

                    if( eod.getLow() < lower ) {
                        lower = eod.getLow() ;
                    }
                    if( eod.getHigh() > upper ) {
                        upper = eod.getHigh() ;
                    }

                    if( absolute ) {
                        if( eod.getPrevClose() < lower ) {
                            lower = eod.getPrevClose() ;
                        }
                        else if( eod.getPrevClose() > upper ) {
                            upper = eod.getPrevClose() ;
                        }
                    }

                    if( calcVolRange ) {
                        if( itdRange ) {
                            long vol, lastVol = 0 ;
                            long interpolationCount = 0 ;
                            boolean skipFirstVol = true ;

                            for( final ITDValue itdVal : eod.getITDValues() ) {
                                // Logic behind the interpolation count - Take for example a
                                // case where we have received ITD data for a period of
                                // 11:00 - 11:30. Now say, we loose volume continuity for
                                // 30 minutes. When we receive the volume data at 12:00 PM,
                                // the difference since the last volume data will be huge,
                                // overshadowing the graph. Hence we have this logic where
                                // if we have received more than 15 interpolated data points
                                // we treat it as a fresh start. Hence we zero out the last
                                // volume.
                                if( interpolationCount > 15 ) {
                                    lastVol = 0 ;
                                    skipFirstVol = true ;
                                }

                                if( itdVal.isInterpolated() ) {
                                    interpolationCount ++ ;
                                    continue ;
                                }
                                else {
                                    interpolationCount = 0 ;
                                    // Skip the first value - this is because we are ignoring
                                    // interpolated values and hence if data is being collected
                                    // late, we might end up having a huge spike since ITD
                                    // data is shown as differential
                                    if( skipFirstVol ) {
                                        skipFirstVol = false ;
                                    }
                                    else {
                                        if( lastVol != 0 ) {
                                            vol = (int)(itdVal.getVolume() - lastVol) ;
                                            maxVol = ( vol > maxVol ) ? vol : maxVol ;
                                        }
                                        lastVol = itdVal.getVolume() ;
                                    }
                                }
                            }
                        }
                        else {
                            if( eod.getVolume() > maxVol ) {
                                maxVol = eod.getVolume() ;
                            }
                        }
                    }
                }
            }

            // Funny situation handling - what if we have an entity in the model
            // but the model does not have any data? In this case we would give
            // an infinite range :). This situation can arise in strange scenarios
            // For example -
            // ITD panel shows data @ 16:00:00 and that is the only ITD value
            // Now the model would fetch ITD for that day and come up with zero
            // records within the time range. Assume EOD has not yet arrived.
            // In this case, we would not have a single EOD value for the entity.
            // Put a check for this case and revert the range to 0-1
            // NOTE: This is an extremely rare case and should not happen in
            //       practice.
            if( lower == Float.MAX_VALUE && upper == Float.MIN_VALUE ) {
                lower = 0F ;
                upper = 1F ;
            }
        }

        // If we are dealing with multiple entities, the data is always in
        // a comparative mode based on the zero Y marker. Hence if the lower
        // value is greater than zero, we need to set it to 0.
        if( (this.renderDataCache.size() > 1) && (lower > 0) ) {
            lower = 0 ;
        }

        this.yAxisRange = new FloatRange( lower, upper ) ;
        if( calcVolRange ) {
            this.volAxisRange = new LongRange( 0, maxVol ) ;
        }
    }

    public Map<ChartEntity, List<EODValue>> getRenderData() {
        // If the time range has changed since the last call to this method
        // recompute the entire render cache.
        if( this.cacheRefreshRequired ) {
            refreshRenderCache() ;
        }
        return this.renderDataCache ;
    }

    /**
     * This method is invoked when EVT_SCRIP_ITD_INSERT events are generated.
     * The value of the event is a list of {@link ScripITD} instances. This
     * method processes the event and updates the table model. The update to
     * the table model will trigger the table UI rendering via the Swing
     * event notification mechanism.
     *
     * @param event The EVT_SCRIP_ITD_INSERT event. The value of the event is
     *        a List of {@link ScripITD} instances.
     */
    @SuppressWarnings("unchecked")
    @Override
    public synchronized void handleEvent( final Event event ) {

        final EventType eventType = event.getEventType() ;

        if( eventType == EventType.EVT_HI_RES_SCRIP_ITD_INSERT ||
            eventType == EventType.EVT_HI_RES_NSE_INDEX_ITD_INSERT ) {

            final String symbol = ( String )event.getValue() ;
            final ChartEntity entity = this.entityMap.get( symbol ) ;
            if( entity != null ) {
                try {
                    entity.loadDataForInterval( this.renderStartTime, this.renderEndTime,
                                                isIntradayRange() ) ;
                    this.cacheRefreshRequired = true ;
                    notifyListeners( ModelEventType.MODEL_DATA_CHANGED, null ) ;
                }
                catch ( final STException e ) {
                    logger.error( "Could not load data for scrip " + symbol, e ) ;
                }
            }
        }
        if( eventType == EventType.EVT_SCRIP_ITD_INSERT ||
            eventType == EventType.EVT_NSE_INDEX_ITD_INSERT ) {

            final List<AbstractITDValue> itdValues = ( List<AbstractITDValue> )event.getValue() ;

            if( !itdValues.isEmpty() ) {
                Date    itdTime  = null ;
                String  name     = null ;
                boolean added    = false ;

                for( final AbstractITDValue itdVal : itdValues ) {

                    if( itdVal instanceof ScripITD ) {
                        final ScripITD sItd = ( ScripITD )itdVal ;
                        itdTime = sItd.getTime() ;
                        name    = sItd.getSymbolId() ;
                    }
                    else {
                        final ExIndexITD iItd = ( ExIndexITD )itdVal ;
                        itdTime = iItd.getDate() ;
                        name    = iItd.getIndex() ;
                    }

                    final ChartEntity entity = this.entityMap.get( name ) ;

                    if( entity != null ) {
                        // Add the itd value to the entity.
                        entity.addITDValue( itdVal ) ;

                        // Determine if this addition requires a render cache
                        // refresh. Just set the flag, it will be loaded during
                        // the next paint operation.
                        final Date time = itdTime ;
                        if( time.after( this.renderStartTime ) &&
                            time.before( this.renderEndTime ) ) {
                            this.cacheRefreshRequired = true ;
                            added = true ;
                        }
                    }
                }

                // Now its time to notify the model listeners that the model
                // has changed and they should refresh themselves. We notify
                // the listeners if and only if an addition has happened within
                // the render time range.
                if( added ) {
                    notifyListeners( ModelEventType.MODEL_DATA_CHANGED, null ) ;
                }
            }
        }
    }

    /** Returns a date which is num working days prior to the base date. */
    private Date getPastDate( final Date baseDate, final int numWrkDays ) {

        final Calendar cal = Calendar.getInstance() ;
        cal.setTime( baseDate ) ;
        int numDays = 0 ;
        while( numDays < numWrkDays ) {
            cal.add( Calendar.DATE, -1 ) ;
            if( cal.get( Calendar.DAY_OF_WEEK ) == Calendar.SATURDAY ||
                cal.get( Calendar.DAY_OF_WEEK ) == Calendar.SUNDAY ) {
                continue ;
            }
            numDays++ ;
        }
        return STUtils.getStartOfDay( cal.getTime() ) ;
    }

    /**
     * This method is called upon the model whenever the user changes the
     * time range using the fixed date range markers, for example if the user
     * is switching between 1 day, 5 days, 2 weeks, 1 month etc. This function
     * accepts a string which can be either 1D, 5D, 2W, 1M, 3M, 6M or 1Y and
     * sets the render time accordingly and enables the render cache to be
     * refreshed the next time the model is asked for the render data. It
     * also notifes the model observers that the model might have changed.
     *
     * @param range A string constant indicating the range marker. It can be
     *        one of the following values 1D, 5D, 2W, 1M, 3M, 6M or 1Y.
     */
    public synchronized void changeTimeRange( final String range ) {

        // If no entity has been added to the chart yet, just return.
        if( this.baseChartDate == null ) {
            return ;
        }

        this.fixedRangeId = range ;

        final Calendar cal = Calendar.getInstance() ;
        cal.setTime( this.baseChartDate ) ;

        if( range.equalsIgnoreCase( UIConstant.RANGE_1D ) ) {
            cal.setTime( this.baseChartDate ) ;
        }
        else if( range.equalsIgnoreCase( UIConstant.RANGE_2D ) ) {
            cal.setTime( getPastDate( this.baseChartDate, 1 ) ) ;
        }
        else if( range.equalsIgnoreCase( UIConstant.RANGE_3D ) ) {
            cal.setTime( getPastDate( this.baseChartDate, 2 ) ) ;
        }
        else if( range.equalsIgnoreCase( UIConstant.RANGE_4D ) ) {
            cal.setTime( getPastDate( this.baseChartDate, 3 ) ) ;
        }
        else if( range.equalsIgnoreCase( UIConstant.RANGE_5D ) ) {
            cal.setTime( getPastDate( this.baseChartDate, 4 ) ) ;
        }
        else if( range.equalsIgnoreCase( UIConstant.RANGE_2W ) ) {
            cal.add( Calendar.DATE, -15 ) ;
        }
        else if( range.equalsIgnoreCase( UIConstant.RANGE_1M ) ) {
            cal.add( Calendar.MONTH, -1 ) ;
        }
        else if( range.equalsIgnoreCase( UIConstant.RANGE_3M ) ) {
            cal.add( Calendar.MONTH, -3 ) ;
        }
        else if( range.equalsIgnoreCase( UIConstant.RANGE_6M ) ) {
            cal.add( Calendar.MONTH, -6 ) ;
        }
        else if( range.equalsIgnoreCase( UIConstant.RANGE_1Y ) ) {
            cal.add( Calendar.YEAR, -1 ) ;
        }
        else if( range.equalsIgnoreCase( UIConstant.RANGE_2Y ) ) {
            cal.add( Calendar.YEAR, -2 ) ;
        }
        else if( range.equalsIgnoreCase( UIConstant.RANGE_5Y ) ) {
            cal.add( Calendar.YEAR, -5 ) ;
        }
        else if( range.equalsIgnoreCase( UIConstant.RANGE_10Y ) ) {
            cal.add( Calendar.YEAR, -10 ) ;
        }
        else {
            throw new IllegalArgumentException( "Invalid range " + range +
              " specified. Possible values are 1D, 5D, 2W, 1M, 3M, 6M, 1Y, 2Y or 5Y." ) ;
        }

        this.renderStartTime = cal.getTime() ;
        this.cacheRefreshRequired = true ;

        // If we have changed the time range - it implies that the model needs
        // to be refreshed with the data for all the entities for the new
        // time range - Request each entity to load their data for the new
        // time interval.
        for( final ChartEntity entity : this.entityMap.values() ) {

            // Ask the entity to load the data for the display time range if it
            // does not already contain the data.
            try {
                entity.loadDataForInterval( this.renderStartTime, this.renderEndTime,
                                            isIntradayRange() ) ;
            }
            catch ( final STException e ) {
                LogMsg.error( "Could not load data for entity " + entity.getName() ) ;
                logger.error( "Unanticipated exception loading entity data", e ) ;
            }
        }

        notifyListeners( ModelEventType.MODEL_DATA_CHANGED, null ) ;
    }

    /** Returns the fixed time range we are operating against. */
    public String getFixedTimeRange() {
        return this.fixedRangeId ;
    }

    /** Returns the number of primary chart entities being managed by the model.*/
    public int getNumPrimaryEntities() {
        return this.entityMap.size() ;
    }

    /** Returns a collection of entity names that are being managed. */
    public String[] getPrimaryEntityNames() {
        return this.entityMap.keySet().toArray( new String[0] ) ;
    }

    /**
     * Returns true if the data is condusive for showing volume graph. Please
     * note that this function does not consider the user preference, but
     * returns a true if the model has only one entity and the entity is a
     * scrip.
     */
    public boolean isDataCondusiveForVolumeGraph() {
        boolean retVal = false ;
        if( this.entityMap.size() == 1 ) {
            retVal = ( this.entityMap.values().iterator().next().getEntityType() ==
                       EntityType.SCRIP ) ;
        }
        return retVal ;
    }
}
