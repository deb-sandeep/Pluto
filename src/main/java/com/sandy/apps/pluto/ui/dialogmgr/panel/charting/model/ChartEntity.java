/**
 * Creation Date: Oct 18, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model;
import java.awt.BasicStroke ;
import java.awt.Color ;
import java.awt.Stroke ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;
import java.util.TreeMap ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.svc.IExIndexSvc ;
import com.sandy.apps.pluto.biz.svc.IScripSvc ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.STUtils ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.AbstractITDValue ;
import com.sandy.apps.pluto.shared.dto.ExIndexEOD ;
import com.sandy.apps.pluto.shared.dto.ExIndexITD ;
import com.sandy.apps.pluto.shared.dto.ScripEOD ;
import com.sandy.apps.pluto.shared.dto.ScripITD ;

/**
 * This class represents an entity that needs to be plotted on the chart.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ChartEntity {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ChartEntity.class ) ;

    /** The minimum resolution in milliseconds between two ITD values. */
    private static final int MIN_ITD_RESOLUTION = 30000 ;

    /** The pre defined set of entity types supported by the charting window. */
    public enum EntityType {
        SCRIP,
        INDEX
    } ;

    /** The color that this entity will be rendered in. */
    private Color color = Color.cyan ;

    /** The stroke that this entity will be rendered in. */
    private Stroke stroke = new BasicStroke() ;

    /** The display name of the entity. */
    private final String name ;

    /**
     * The entity type that this charting entity was created with. This is
     * a more symbolic name given to the type of the seed that this entity
     * is created with.
     */
    private final EntityType entityType ;

    /**
     * A mapping of date versus the EOD value instance. A ChartEntity contains
     * multiple EODValue instances in an ordered fashion. Where each EODValue
     * value can contain an aggregation of ITDValue instances.
     */
    private final Map<Date, EODValue> eodValueMap = new TreeMap<Date, EODValue>() ;

    /** The configuration with which this chart entity was constructued. */
    private final ChartEntityConfig config ;

    /** Constructor. */
    public ChartEntity( final ChartEntityConfig config ) {

        super() ;
        this.config     = config ;
        this.name       = config.getName() ;
        this.entityType = config.getType() ;
    }

    // ========== Bean property getters and setters. ===========================
    public Color getColor() { return this.color ; }
    public void setColor( final Color color ) { this.color = color ; }

    public Stroke getStroke() { return this.stroke ; }
    public void setStroke( final Stroke stroke ) { this.stroke = stroke ; }

    public String getName() { return this.name ; }

    public ChartEntityConfig getConfig() { return this.config ; }

    public EntityType getEntityType() { return this.entityType ; }
    // ========== Bean property getters and setters. ===========================

    /**
     * This method is called upon this entity by the chart model whenever the
     * render time interval of the chart changes. In this method, the
     * chart entity ensures that it has loaded the the data from the database
     * for the specified time interval.
     *
     * @param startTime The start time for the time range.
     * @param endTime The end time for the time range.
     * @param itdRange A boolean flag indicating if we have to load ITD data
     *
     * @throws STException In case an unanticipated exception scenario was
     *         encountered during the process of loading data for the
     *         specified interval.
     */
    public void loadDataForInterval( final Date startTime, final Date endTime,
                                     final boolean itdRange )
        throws STException {

        // First - clear off the existing data and load the data afresh. This
        // might be optimized later to load only the missing part.
        this.eodValueMap.clear() ;

        // Determine whether we are dealing with a scrip or index. This would
        // decide on which methods we want to invoke on the service tier.
        if( this.entityType == EntityType.SCRIP ) {
            loadScripDataForInterval( startTime, endTime, itdRange ) ;
        }
        else {
            loadIndexDataForInterval( startTime, endTime, itdRange ) ;
        }
    }

    /**
     * Loads the data for this chart entity, assuming that the entity represents
     * a Scrip. A chart entity can represent either a scrip or an index entity,
     * however the logic to populate the data for either scrip or index are
     * different.
     *
     * @param startTime The start of the time range.
     * @param endTime The end of the time range.
     * @param itdRange A boolean flag indicating if we have to load ITD data
     *
     * @throws STException If an unanticipated exception is encountered.
     */
    private void loadScripDataForInterval( final Date startTime, final Date endTime,
                                           final boolean itdRange )
        throws STException {

        Date eodDate = null ;
        EODValue eodValue = null ;

        // Try getting the EOD data for the specified range.
        final IScripSvc scripSvc = ServiceMgr.getScripSvc() ;

        // Note - we might not always get the EOD data for the specified range
        // For example, if we are dealing with intra day intervals, the
        // EOD is yet to be loaded and out database does not have a record.
        final List<ScripEOD> eodData = scripSvc.getEODData( getName(), startTime, endTime ) ;
        if( eodData != null ) {
            for( final ScripEOD scripEOD : eodData ) {
                eodDate  = scripEOD.getDate() ;
                eodValue = new EODValue( eodDate ) ;
                populateEODValue( eodValue, scripEOD ) ;
                this.eodValueMap.put( eodDate, eodValue ) ;
            }
        }

        // Now we load the intra day values for the range and try associating
        // them with the EOD values that we have loaded. In case we do not
        // find an EOD value to associate with, we create a new one.
        if( itdRange ) {
            filterAndAddITDValues( scripSvc.getITDData( getName(), startTime, endTime ) ) ;
        }
    }

    /**
     * Loads the data for this chart entity, assuming that the entity represents
     * an Index. A chart entity can represent either a scrip or an index entity,
     * however the logic to populate the data for either scrip or index are
     * different.
     *
     * @param startTime The start of the time range.
     * @param endTime The end of the time range.
     * @param itdRange A boolean flag indicating if we have to load ITD data
     *
     * @throws STException If an unanticipated exception is encountered.
     */
    private void loadIndexDataForInterval( final Date startTime, final Date endTime,
                                           final boolean itdRange )
        throws STException {

        Date eodDate = null ;
        EODValue eodValue = null ;

        // Try getting the EOD data for the specified range.
        final IExIndexSvc indexSvc = ServiceMgr.getExIndexSvc() ;

        // Note - we might not always get the EOD data for the specified range
        // For example, if we are dealing with intra day intervals, the
        // EOD is yet to be loaded and out database does not have a record.
        final List<ExIndexEOD> eodData = indexSvc.getExIndexEODList( this.name, startTime, endTime ) ;
        if( eodData != null ) {
            for( final ExIndexEOD scripEOD : eodData ) {
                eodDate  = scripEOD.getDate() ;
                eodValue = new EODValue( eodDate ) ;
                populateEODValue( eodValue, scripEOD ) ;
                this.eodValueMap.put( eodDate, eodValue ) ;
            }
        }

        // Now we load the intra day values for the range and try associating
        // them with the EOD values that we have loaded. In case we do not
        // find an EOD value to associate with, we create a new one.
        if( itdRange ) {
            filterAndAddITDValues( indexSvc.getExIndexITDList( this.name, startTime, endTime ) ) ;
        }
    }

    /** Filters the itd values and adds them to the model. */
    private void filterAndAddITDValues( List<? extends AbstractITDValue> itdData ) {

        itdData = filterITDValuesForResolution( itdData ) ;
        if( itdData != null ) {
            for( final AbstractITDValue scripITD : itdData ) {
                addITDValue( scripITD ) ;
            }
        }
    }

    /**
     * Filters the given input list of ITD values (both scrip and index) by weeding
     * out entries which fall below the desired the resolution. For example,
     * if the desired resolution is 30 seconds, this method will guarantee that
     * the output list will contain ITD values which can not be closer than 30
     * seconds from each other. It is assumed that the input list contains the
     * ITD values in ascending order of their time markers.
     * <p>
     * NOTE: For Scrip ITD values, we only filter non interpolated values.
     *       Interpolated values contains rich information which we don't want
     *       to loose for example volume information.
     *
     * @param inputList A list of ITD values in ascending order of their time
     *        markers.
     *
     * @return A list containing filtered ITD values based on the desired resolution.
     */
    private List<AbstractITDValue> filterITDValuesForResolution(
                                      final List<? extends AbstractITDValue> inputList ) {
        final List<AbstractITDValue> list = new ArrayList<AbstractITDValue>() ;

        long    lastTime   = -1 ;
        long    time       = -1 ;
        boolean skipFilter = false ;

        for( final AbstractITDValue itd : inputList ) {
            skipFilter = false ;
            if( itd instanceof ScripITD ) {
                final ScripITD scripItd = ( ScripITD )itd ;
                time = scripItd.getTime().getTime() ;
                if( lastTime == -1 ) {
                    lastTime = time ;
                }
                // Don't filter if we are dealing with a non interpolated value.
                if( !scripItd.isInterpolated() ) {
                    skipFilter = true ;
                }
            }
            else {
                final ExIndexITD indexItd = ( ExIndexITD )itd ;
                time = indexItd.getDate().getTime() ;
                if( lastTime == -1 ) {
                    lastTime = time ;
                }
            }

            if( (time - lastTime) < MIN_ITD_RESOLUTION && !skipFilter ) {
                continue ;
            }
            else {
                lastTime = time ;
                list.add( itd ) ;
            }
        }

        return list ;
    }

    /**
     * Adds the specified ITD value to the list of ITD values of this entity.
     * This method will ensure that the date of the ITD value is respected and
     * it is assigned to the appropriate EOD value.
     *
     * @param itd The ITD value to add.
     */
    public void addITDValue( final AbstractITDValue itd ) {

        Date eodDate = null ;
        EODValue eodValue = null ;
        ITDValue itdValue = null ;

        Date    time         = null ;
        boolean interpolated = false ;
        float   high         = 0.0F ;
        float   low          = 0.0F ;
        float   prevClose    = 0.0F ;

        if( itd instanceof ScripITD ) {
            final ScripITD sItd = ( ScripITD )itd ;
            interpolated = sItd.isInterpolated() ;
            time         = sItd.getTime() ;
            high         = (float)sItd.getHigh() ;
            low          = (float)sItd.getLow() ;
            prevClose    = (float)sItd.getPrevClose() ;
        }
        else {
            final ExIndexITD iItd = ( ExIndexITD )itd ;
            interpolated = false ;
            time         = iItd.getDate() ;
            prevClose    = (float)iItd.getPrevClose() ;
            high         = Float.MIN_VALUE ;
            low          = Float.MAX_VALUE ;
        }

        // Ignore interpolated data - this makes the graph too complicated.
        //if( interpolated ) { return ; }

        // Ignore any ScripITD value which is outside a business day timing.
        final Date bizStartTime = STUtils.getBizStartTime( time ) ;
        final Date bizEndTime   = STUtils.getBizEndTime( time ) ;

        // If we are outside the business window - return.
        if( time.before( bizStartTime ) || time.after( bizEndTime ) ) {
            return ;
        }

        eodDate  = STUtils.getStartOfDay( time ) ;
        eodValue = this.eodValueMap.get( eodDate ) ;
        if( eodValue == null ) {
            eodValue = new EODValue( eodDate, true ) ;
            if( !interpolated ) {
                eodValue.setHigh( high ) ;
                eodValue.setLow( low ) ;
            }
            eodValue.setPrevClose( prevClose ) ;
            this.eodValueMap.put( eodDate, eodValue ) ;
        }

        itdValue = new ITDValue() ;
        populateITDValue( itdValue, itd ) ;
        eodValue.addITDValue( itdValue ) ;
    }

    /** Populates the given EODValue instance with values from the ScripEOD */
    private void populateEODValue( final EODValue eodValue, final ScripEOD scripEOD ) {
        eodValue.setOpen(  (float)scripEOD.getOpeningPrice() ) ;
        eodValue.setClose( (float)scripEOD.getClosingPrice() ) ;
        eodValue.setHigh(  (float)scripEOD.getHighestPrice() ) ;
        eodValue.setLow(   (float)scripEOD.getLowestPrice()  ) ;
        eodValue.setPrevClose( (float)scripEOD.getPrevClosePrice() ) ;
        eodValue.setVolume( scripEOD.getTotalTradeQty() ) ;
    }

    /** Populates the given EODValue instance with values from the ScripEOD */
    private void populateEODValue( final EODValue eodValue, final ExIndexEOD indexEOD ) {
        eodValue.setOpen(  (float)indexEOD.getOpen() ) ;
        eodValue.setClose( (float)indexEOD.getClose() ) ;
        eodValue.setHigh(  (float)indexEOD.getHigh() ) ;
        eodValue.setLow(   (float)indexEOD.getLow()  ) ;
        eodValue.setPrevClose( (float)indexEOD.getPrevClose() ) ;
    }

    /** Populates the given ITDValue instance with values from the ScripITD */
    private void populateITDValue( final ITDValue itdValue, final AbstractITDValue itd ) {

        if( itd instanceof ScripITD ) {
            final ScripITD sItd = ( ScripITD )itd ;
            itdValue.setTime( sItd.getTime().getTime() ) ;
            itdValue.setValue( (float)sItd.getPrice() ) ;
            itdValue.setInterpolated( sItd.isInterpolated() ) ;
            itdValue.setVolume( sItd.getTotalTradeQty() ) ;
        }
        else if( itd instanceof ExIndexITD ) {
            final ExIndexITD iItd = ( ExIndexITD )itd ;
            itdValue.setTime( iItd.getDate().getTime() ) ;
            itdValue.setValue( (float)iItd.getCurrentVal() ) ;
            itdValue.setInterpolated( false ) ;
        }
    }

    /** Chart entities are equal if they have the same name. */
    @Override
    public boolean equals( final Object obj ) {
        return getName().equals( ((ChartEntity)obj ).getName() ) ;
    }

    /** Hash code of a chart entity is the hash code of the name. */
    @Override
    public int hashCode() {
        return getName().hashCode() ;
    }

    /**
     * Returns the values of this chart entity and all the derived entities
     * for the time range specified.
     *
     * @param absolute If true, the values would be returned as absolute values,
     *        else a percentage value as compared to the previous closing value
     *        will be returned. Typically when we are comparing multiple entities,
     *        we normalize them on a comparative percentage change scale.
     */
    public Map<ChartEntity, List<EODValue>> getValues( final Date renderStartTime,
                                                       final Date renderEndTime,
                                                       final boolean absolute ) {

        final Map<ChartEntity, List<EODValue>> retVal =
                                    new HashMap<ChartEntity, List<EODValue>>() ;

        final Date eodDate = STUtils.getStartOfDay( renderStartTime ) ;
        final List<EODValue> eodList = new ArrayList<EODValue>() ;

        float baseValue = 0.0F ;
        boolean firstMatch = true ;

        for( final EODValue eodVal : this.eodValueMap.values() ) {
            final Date date = eodVal.getDate() ;
            if( date.equals( eodDate ) ||
                ( date.after( eodDate ) && date.before( renderEndTime ) ||
                date.equals( renderEndTime ) ) ) {

                if( absolute ) {
                    eodList.add( eodVal ) ;
                }
                else {
                    if( firstMatch ) {
                        firstMatch = false ;
                        baseValue = eodVal.getPrevClose() ;
                    }
                    eodList.add( eodVal.getPctChangeValue( baseValue ) ) ;
                }
            }
        }

        retVal.put( this, eodList ) ;

        // TODO: Now get the cache data recursively from all the derived entities

        return retVal ;
    }

    /**
     * Removes the specified derived entity from this chart entity.
     *
     * @param derivedEntityName The name of the derived entity.
     */
    public void removeDerivedEntity( final String derivedEntityName ) {
        logger.error( "TODO: removal of derived entity yet to be implemented" ) ;
    }
}
