/**
 * 
 * 
 * 
 *
 * Creation Date: Dec 22, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.scripeodsummary;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.biz.svc.IScripSvc ;
import com.sandy.apps.pluto.shared.CacheListener ;
import com.sandy.apps.pluto.shared.ErrorCode ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.ScripEOD ;
import com.sandy.apps.pluto.shared.dto.Symbol ;
import com.sandy.apps.pluto.shared.dto.SymbolPctChange ;
import com.sandy.apps.pluto.shared.event.Event ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.event.IEventSubscriber ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * This class caches the previous 'N' EOD values for the registered scrips.
 * This is a singleton class and is initialized during system startup.
 * During startup, this class loads the latest EOD values of all the symbols,
 * also this class refreshes itself upon the receipt of notification that EOD
 * bhavcopy has been downloaded.
 * <p>
 *
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ScripEODValueCache implements IEventSubscriber {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ScripEODValueCache.class ) ;

    /** The singleton instance. */
    private static ScripEODValueCache INSTANCE = null ;

    /** The data for which this model encapsulates the EOD data. */
    private Date date = null ;

    /**
     * A list of {@link CacheListener} instances which are interested
     * in monitoring the changes to the cached data. A set prevents duplicate
     * registrations.
     */
    private final Set<CacheListener> listeners = new HashSet<CacheListener>() ;

    /** A list storing the {@link ScripEOD} instances which this model encapsulates. */
    private final List<ScripEOD> eodList = new ArrayList<ScripEOD>() ;

    /** A map to store the symbol versus its EOD value. */
    private final Map<String, ScripEOD> eodValMap = new HashMap<String, ScripEOD>() ;

    /** A map which stores the symbol name versus the symbol details. */
    private final Map<String, Symbol> symbolMap = new HashMap<String, Symbol>() ;

    /** A map to store the percentage changes of symbols for the last n days. */
    private final Map<String, List<SymbolPctChange>> lastNPctChangeMap =
                                  new HashMap<String, List<SymbolPctChange>>() ;

    /** A lock to synchronize read write access to the map. */
    private static final Object LOCK = new Object() ;

    /** Private constructor, since this is a singleton class. */
    private ScripEODValueCache() {
        super() ;
    }

    /** Registers a cache listener. */
    public void addEODValueCacheListener( final CacheListener model ) {
        this.listeners.add( model ) ;
    }

    /** Deregisters a cache listener. */
    public void removeEODValueCacheListener( final CacheListener model ) {
        this.listeners.remove( model ) ;
    }

    /**
     * The singleton accessor method.
     *
     * @return The singleton instance of this class.
     */
    public static ScripEODValueCache getInstance() {

        if( INSTANCE == null ) {
            final IScripSvc scripSvc = ServiceMgr.getScripSvc() ;
            final Date latestBhavcopyDate = scripSvc.getLastScripEODDate() ;

            INSTANCE = new ScripEODValueCache() ;
            try {
                INSTANCE.loadEODData( latestBhavcopyDate ) ;
                EventBus.instance().addSubscriberForEventTypes( INSTANCE,
                                       EventType.EVT_BHAVCOPY_IMPORT_SUCCESS ) ;
            }
            catch ( final STException e ) {
                logger.error( "Scrip EOD data could not be loaded", e ) ;
            }
        }
        return INSTANCE ;
    }

    /**
     * Loads the EOD data for the specified date. All the previous data if
     * any is cleared before adding the EOD data for the specified date. After
     * the data has been successfully loaded, all the registered listeners
     * are notified and it is expected that the listeners will take the
     * responsibility of refreshing any associated user interfaces.
     *
     * @param eodDate The date for which the EOD data has to be loaded
     *
     * @throws STException If an exception condition is generated during the
     *         process.
     */
    public void loadEODData( final Date eodDate ) throws STException {

        logger.info( "Initializing EOD value cache for " + eodDate ) ;

        final IScripSvc scripSvc = ServiceMgr.getScripSvc() ;
        List<SymbolPctChange> pctChgList = null ;

        // Load the last 10 percentage changes for all the symbols from the
        // reference date given.
        try {
            pctChgList = scripSvc.getLastNPctEODChange( eodDate,
                                 ScripEODSummaryPanel.LAST_N_PCT_CHANGE_DAYS+1 ) ;
        }
        catch ( final DataAccessException e ) {
            throw new STException( "Could not load bhavcopy for date " + eodDate,
                                   e, ErrorCode.INIT_FAILURE ) ;
        }

        synchronized ( LOCK ) {

            this.eodList.clear() ;
            this.eodValMap.clear() ;
            this.eodList.addAll( scripSvc.getScripEOD( eodDate, true ) ) ;

            this.symbolMap.clear() ;
            for( final ScripEOD eod : this.eodList ) {
                this.symbolMap.put( eod.getSymbolId(), eod.getSymbol() ) ;
                this.eodValMap.put( eod.getSymbolId(), eod ) ;
            }

            this.date = eodDate ;
            this.lastNPctChangeMap.clear() ;
            for( final SymbolPctChange chg : pctChgList ) {
                final String symbol = chg.getSymbol() ;
                List<SymbolPctChange> list = this.lastNPctChangeMap.get( symbol ) ;
                if( list == null ) {
                    list = new ArrayList<SymbolPctChange>() ;
                    this.lastNPctChangeMap.put( symbol, list ) ;
                }
                list.add( chg ) ;
            }
        }
    }

    /**
     * This method is invoked when EVT_BHAVCOPY_IMPORT_SUCCESS events are generated.
     * The value of the event is a Date instance. This method processes the event
     * and updates the table model. The update to the table model will trigger the
     * table UI rendering via the Swing event notification mechanism.
     *
     * @param event The EVT_BHAVCOPY_IMPORT_SUCCESS event. The value of the event
     *        is a Date instance for which the bhavcopy import was successful.
     */
    @Override
    public void handleEvent( final Event event ) {

        final Date importDate = ( Date )event.getValue() ;

        try {
            logger.debug( "Refreshing EOD cache for date " + importDate ) ;
            // Load the data for the latest bhavcopy imported
            loadEODData( importDate ) ;

            // Now advise all the listeners that the underlying cache has changed
            // and they should advice their UI's accordingly.
            for( final CacheListener model : this.listeners ) {
                try {
                    model.cacheDataChanged() ;
                }
                catch ( final Throwable e ) {
                    // Harden the dispatch loop such that problems with one model
                    // should not bring down the dispatch thread.
                    logger.info( "Exception while notifying model", e ) ;
                }
            }
        }
        catch ( final STException e ) {
            logger.error( "Bhavcopy load failed for date " + importDate, e ) ;
        }
    }

    /** Returns the date for which this class caches the last 'N' EOD values. */
    public Date getDate() {
        return this.date ;
    }

    /**
     * Returns a list of {@link SymbolPctChange} instances for the given
     * symbol is descending order of their date.
     *
     * @param symbol The symbol for which the last N pct change values are
     *        required.
     *
     * @return A list of percentage change values in descending order of date
     *         or null if such a symbol is not loaded.
     */
    public List<SymbolPctChange> getSymbolPctChangeList( final String symbol ) {

        List<SymbolPctChange> pctChangeList = null ;
        synchronized ( LOCK ) {
            pctChangeList = this.lastNPctChangeMap.get( symbol ) ;
        }
        return pctChangeList ;
    }

    /**
     * Returns the list of {@link ScripEOD} instances being cached.
     */
    public List<ScripEOD> getScripEODList() {
        return this.eodList ;
    }

    /**
     * Returns the Symbol associated with the given NSE symbol id or null if
     * such a symbol is not registered in the cache.
     */
    public Symbol getSymbol( final String id ) {
        return this.symbolMap.get( id ) ;
    }

    /**
     * Returns the {@link ScripEOD} value for the given symbol or null if
     * no such symbol is registered.
     */
    public ScripEOD getScripEOD( final String id ) {
        return this.eodValMap.get( id ) ;
    }
}
