/**
 * 
 * 
 * 
 *
 * Creation Date: Oct 16, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.indexitdsummary;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.CacheListener ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.STUtils ;
import com.sandy.apps.pluto.shared.dto.ExIndexITD ;
import com.sandy.apps.pluto.shared.event.Event ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.event.IEventSubscriber ;

/**
 * This is a singleton class (private constructor), which helps cache and
 * pre-load ITD scrip values. The existence of this class is justified because
 * of the following reasons:
 * <p>
 * The user can open multiple ITD summary tables for projecting different
 * views of the ITD scrips. All models of all the tables subscribe themselves
 * to the ITD insert event. The filtering, sorting is handled purely by
 * Swing APIs. In this respect, all the models have the same data structure.
 * This can be remedied by keeping the model data in one place.
 * <p>
 * Each table needs a dedicated model and hence we have to keep a singleton
 * cache to which the models can pull the data from.
 * <p>
 * This cache will also have the additional logic of persisting the last
 * refreshed data, such that when the user open the ITD summary panel, he
 * does not have to wait till the next refresh cycle for his view to be populated.
 * This is especially useful in cases when we are doing offline analysis.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class IndexITDValueCache implements IEventSubscriber {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( IndexITDValueCache.class ) ;

    /** The singleton instance of this class. */
    private static IndexITDValueCache instance = null ;

    /** A string array containing the columns in the log table. */
    public static final String[] COL_NAMES = {
        "Index",
        "% E",
        "% O",
        "Value",
        "Chg"
    } ;

    // The column indices as constants
    public static final int COL_INDEX      = 0 ;
    public static final int COL_PCT_E      = 1 ;
    public static final int COL_PCT_O      = 2 ;
    public static final int COL_VALUE      = 3 ;
    public static final int COL_CHANGE     = 4 ;

    /**
     * A list of {@link CacheListener} instances which are interested
     * in monitoring the changes to the cached data. A set prevents duplicate
     * registrations.
     */
    private final Set<CacheListener> listeners = new HashSet<CacheListener>() ;

    /** The last time the table model had changed. */
    private Date lastRefreshTime = null ;

    /** The sorted map which holds on to the latest {@link ExIndexITD} instances. */
    private final TreeMap<String, ExIndexITD> itdMap = new TreeMap<String, ExIndexITD>() ;

    /** The linear collection of ITD values for model operations. */
    private final List<ExIndexITD> itdList = new ArrayList<ExIndexITD>() ;

    /** The location of the cache file. */
    private File cacheFile = null ;

    /** Private constructor to enforce singleton pattern. */
    private IndexITDValueCache() {
        super() ;
    }

    /** The singleton accessor. */
    public static IndexITDValueCache getInstance() {
        if( instance == null ) {
            instance = new IndexITDValueCache() ;
            instance.initialize() ;
        }
        return instance ;
    }

    /**
     * Returns the ITD cache file to use for storing and loading ITD data. Note
     * that we have to differentiate the file for the production and development
     * instances, else the production cache would be overwritten with mock
     * development data :)
     */
    private File getITDCacheFile() {
        if( this.cacheFile == null ) {
            if( STUtils.isDevMode() ) {
                this.cacheFile = new File( STUtils.getConfigDir(),
                                           "dev.pluto_index_itd_cache" ) ;
            }
            else {
                this.cacheFile = new File( STUtils.getConfigDir(),
                                           "prod.pluto_index_itd_cache" ) ;
            }
        }
        return this.cacheFile ;
    }

    /**
     * Initializes the ITD value cache by registering itself with the Event
     * Bus for listening to ITD insert events. This method also loads the
     * last refreshed data.
     */
    @SuppressWarnings("unchecked")
    private void initialize() {
        // Add the table model as a subscriber interested in listening to JOB_*
        // events.
        EventBus.instance().addSubscriberForEventTypes(
                                        this, EventType.EVT_NSE_INDEX_ITD_INSERT ) ;

        // We take this opportunity to load the latest ITD values from a secret
        // file in the user's home directory. We will write this file
        // every time this cache is updated with fresh data points.
        final File secretFile = getITDCacheFile() ;
        ObjectInputStream in = null ;

        try {
            if( secretFile.exists() ) {
                in = new ObjectInputStream( new FileInputStream( secretFile ) ) ;
                this.lastRefreshTime = ( Date )in.readObject() ;

                final List<ExIndexITD> cachedITDList = ( List<ExIndexITD> )in.readObject() ;
                addExIndexITDValues( cachedITDList ) ;
                logger.info( "Reading Index ITD cache from " + secretFile.getAbsolutePath() ) ;
            }
        }
        catch ( final Exception e ) {
            // This is a non critical process. Just dump the exception in the
            // logs so that it can be picked up and fixed during regular
            // Maintenance work.
            logger.error( "Could not load Index ITD cache data", e ) ;
        }
        finally {
            if( in != null ) {
                try {
                    in.close() ;
                }
                catch ( final Exception e2 ) {
                    // Ignore the exception. Nothing we can do
                    logger.error( "Could not close the Index ITD cache file", e2 ) ;
                }
            }
        }
    }

    /** Notifies all the listeners that the ITD data in the cache has changed. */
    private void notifyChange() {

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

        // Dump the current cache values in a serialized file.
        writeITDCache() ;
    }

    /**
     * Write the ITD list and the last refresh time into a serialized file. This
     * will be read back on system startup such that the ITD summary is filled
     * with the last cached entries at startup.
     */
    private void writeITDCache() {

        // After all the models have been notified, we have enough time till
        // the next batch of updates comes our way. We take this opportunity
        // to persist the latest ITD values into a secret file in the user's
        // home directory. We will read this file back during initialization
        // to have the initial data set to display.
        final File secretFile = getITDCacheFile() ;
        ObjectOutputStream out = null ;

        try {
            if( !secretFile.exists() ) {
                secretFile.createNewFile() ;
            }
            out = new ObjectOutputStream( new FileOutputStream( secretFile ) ) ;
            out.writeObject( this.lastRefreshTime ) ;
            out.writeObject( this.itdList ) ;
        }
        catch ( final Throwable e ) {
            // This is a non critical process. Just dump the exception in the
            // logs so that it can be picked up and fixed during regular
            // Maintenance work.
            logger.error( "Could not save Index ITD cache data", e ) ;
        }
        finally {
            if( out != null ) {
                try {
                    out.flush() ;
                    out.close() ;
                }
                catch ( final Throwable e2 ) {
                    // Ignore the exception. Nothing we can do
                    logger.error( "Could not close the Index ITD cache file", e2 ) ;
                }
            }
        }
    }

    /** Registers a cache listener. */
    public void addITDValueCacheListener( final CacheListener model ) {
        this.listeners.add( model ) ;
    }

    /** Deregisters a cache listener. */
    public void removeITDValueCacheListener( final CacheListener model ) {
        this.listeners.remove( model ) ;
    }

    /**
     * This method is invoked when EVT_NSE_INDEX_ITD_INSERT events are generated.
     * The value of the event is a list of {@link ExIndexITD} instances. This
     * method processes the event and updates the table model. The update to
     * the table model will trigger the table UI rendering via the Swing
     * event notification mechanism.
     *
     * @param event The EVT_NSE_INDEX_ITD_INSERT event. The value of the event is
     *        a List of {@link ExIndexITD} instances.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void handleEvent( final Event event ) {

        final List<ExIndexITD> itdValues = ( List<ExIndexITD> )event.getValue() ;

        // It can happen that all the values of this event might have got
        // filtered. In such a case, we have nothing to do - just return.
        if( itdValues == null || itdValues.isEmpty() ) {
            return ;
        }

        addExIndexITDValues( itdValues ) ;

        // Set the time the model was last refreshed. Note that this method
        // can be called because of ITD updates from multiple sources, which
        // may in themselves be time lagged. Hence we take the latest time
        // and use it as the last refresh time.
        final Date lastRefTime = itdValues.get( 0 ).getDate() ;
        if( this.lastRefreshTime != null ) {
            if( lastRefTime.after( this.lastRefreshTime ) ) {
                this.lastRefreshTime = lastRefTime ;
            }
        }
        else {
            this.lastRefreshTime = lastRefTime ;
        }

        notifyChange() ;
    }

    /**
     * Adds the list of {@link ExIndexITD} instances to this cache, refreshing
     * the old values if they exist for the same scrip.
     */
    private void addExIndexITDValues( final List<ExIndexITD> itdValues ) {

        String     indexName   = null ;
        ExIndexITD existingITD = null ;

        for( final ExIndexITD itd : itdValues ) {
            indexName = itd.getIndex() ;
            existingITD = this.itdMap.get( indexName ) ;
            if( existingITD != null ) {
                this.itdMap.remove( indexName ) ;
                this.itdList.remove( existingITD ) ;
            }

            this.itdMap.put( indexName, itd ) ;
            this.itdList.add( itd ) ;
        }

        Collections.sort( this.itdList ) ;
    }

    /** Returns the number of columns supported by the ITD summary panel. */
    public int getColumnCount() {
        return COL_NAMES.length ;
    }

    /** Returns the number of rows for the table. */
    public int getRowCount() {
        return this.itdList.size() ;
    }

    /** Returns the name of the column at the specified column index. */
    public String getColumnName( final int column ) {
        return COL_NAMES[column] ;
    }

    /**
     * Returns the value of the column at the specified row index.
     */
    public Object getValueAt( final int rowIndex, final int columnIndex ) {

        final ExIndexITD itd = this.itdList.get( rowIndex ) ;

        Object retVal = null ;
        switch( columnIndex ) {

            case COL_INDEX :
                retVal = " " + itd.getIndex() ;
                break ;

            case COL_VALUE :
                retVal = itd.getCurrentVal() ;
                break ;

            case COL_CHANGE :
                retVal = itd.getCurrentVal() - itd.getPrevClose() ;
                break ;

            case COL_PCT_E :
                retVal = ((itd.getCurrentVal() - itd.getPrevClose())/itd.getPrevClose())*100 ;
                break ;

            case COL_PCT_O :
                retVal = ((itd.getCurrentVal() - itd.getOpen())/itd.getOpen())*100 ;
                break ;
        }

        return retVal ;
    }

    /** Returns the class of the column at the specified column index. */
    public Class<?> getColumnClass( final int columnIndex ) {
        Class<?> cls = null ;
        switch( columnIndex ) {
            case COL_INDEX :
                cls = String.class ;
                break ;

            case COL_VALUE :
                cls = Integer.class ;

            case COL_CHANGE :
            case COL_PCT_E :
            case COL_PCT_O :
                cls = Double.class ;
                break ;
        }
        return cls ;
    }

    /**
     * Returns the last time the data in the model was refreshed.
     */
    public Date getLastRefreshTime() {
        return this.lastRefreshTime ;
    }

    /** Returns the ExIndexITD instance for the specified row. */
    public ExIndexITD getExIndexITDForRow( final int row ) {
        return this.itdList.get( row ) ;
    }

    /** Returns the ExIndexITD instance for the given index name. */
    public ExIndexITD getExIndexITDForIndex( final String indexName ) {
        return this.itdMap.get( indexName ) ;
    }

    /** Returns a list of index names that are being tracked by the cache. */
    public Collection<String> getIndexNames() {
        return this.itdMap.keySet() ;
    }
}
