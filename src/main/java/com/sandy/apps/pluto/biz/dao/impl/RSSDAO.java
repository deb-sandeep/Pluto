/**
 * Creation Date: Nov 27, 2008
 */

package com.sandy.apps.pluto.biz.dao.impl;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.dao.IRSSDAO ;
import com.sandy.apps.pluto.shared.dto.RSSNewsItem ;
import com.sandy.apps.pluto.shared.dto.RSSNewsItemSource ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * This class is a realization of the {@link IRSSDAO} interface which encapsulates
 * operations related to the RSS news items and related information in the persistent
 * storage. Note that this interface defines only the contract of the data access
 * operations and not the behavioral aspects of the RSS news items.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class RSSDAO extends AbstractBaseDAO implements IRSSDAO {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( RSSDAO.class ) ;

    /** Public no argument constructor. */
    public RSSDAO() {
        super() ;
    }

    // ------ RSS news item operations -----------------------------------------
    /**
     *  Insert a news item into the persistent storage. In case this insertion
     *  causes a primary key violation, the exception is silently consumed.
     *
     *  @param newsItem An instance of {@link RSSNewsItem}
     *
     *  @return The same news item in case the insert was successful or a null
     *          value if a duplicate was found and this item was not inserted.
     *
     *  @throws DataAccessException In case an unanticipated database exception
     *          was encountered. Please note that a data access exception will
     *          not be raised because of a primary key violation.
     */
    public RSSNewsItem insertNewsItem( final RSSNewsItem newsItem )
        throws DataAccessException {

        final String INSERT_QUERY_ID = "RSS.insertNewsItem" ;
        RSSNewsItem retVal = null ;

        try {
            if( logger.isDebugEnabled() ) {
                logger.debug( "Inserting news item " + newsItem ) ;
            }

            if( newsItem.getPublishDate().after( new Date() ) ) {
                logger.info( "Found a news item in the future, ignoring." ) ;
            }
            else {
                super.daMgr.createRecord( INSERT_QUERY_ID, newsItem ) ;
                retVal = newsItem ;
            }
        }
        catch( final DataAccessException dae ) {
            // We ignore primary key violations but let any other exception
            // pass through.
            if( PostGresUtil.isPKViolation( dae ) ) {
                if( logger.isDebugEnabled() ) {
                    logger.debug( "Found a duplicate for news item " + newsItem ) ;
                }
            }
            else {
                throw dae ;
            }
        }

        return retVal ;
    }

    /**
     * Update a news item. Typically a news item is updated for its read status.
     * Note that RSS news items are immutable except for their read status.
     *
     * @param newsItem An instance of {@link RSSNewsItem} with minimally the
     *        PK information and its read status set.
     *
     * @throws DataAccessException In case an unanticipated database exception
     *          was encountered.
     */
    public void updateNewsItem( final RSSNewsItem newsItem )
        throws DataAccessException {

        final String UPDATE_QUERY_ID = "RSS.updateNewsItem" ;

        if( logger.isDebugEnabled() ) {
            logger.debug( "Updating news item " + newsItem ) ;
        }
        super.daMgr.createRecord( UPDATE_QUERY_ID, newsItem ) ;
    }

    /**
     * Get all the news item overview for the time period. An overview is
     * an instance of RSS new item with the description not populated. A boolean
     * parameter specifies whether to include or exclude the already read
     * news items.
     *
     * @param startDate The start date of the time range
     * @param endDate The end date of the time range. If the end date is null,
     *        the current time is considered.
     * @param includeReadItems If true, the returned list will have items which
     *        are already read. If false, only the unread items will be returned.
     *
     * @return A list of {@link RSSNewsItem} instances. Please note that the
     *         returned instances do not have the description information
     *         populated. It is expected that news item details will be fetched
     *         on an individual basis when the user explicitly wants to read
     *         it.
     *
     * @throws DataAccessException In case an unanticipated database exception
     *          was encountered.
     */
    @SuppressWarnings("unchecked")
    public List<RSSNewsItem> getNewsItemOverviews( final Date startDate,
                                                   final Date endDate,
                                                   final boolean includeReadItems )
        throws DataAccessException {

        final String SEARCH_QUERY_ID = "RSS.getNewsItemOverview" ;

        if( logger.isDebugEnabled() ) {
            logger.debug( "Getting news item overviews for : " ) ;
            logger.debug( "\tStart time = " + startDate ) ;
            logger.debug( "\tEnd time   = " + endDate ) ;
            logger.debug( "\tInclude read=" + includeReadItems ) ;
        }

        final Map<String, Object> params = new HashMap<String, Object>() ;
        params.put( "startDate", startDate ) ;
        params.put( "endDate", endDate ) ;
        params.put( "includeReadItems", includeReadItems ) ;

        List<RSSNewsItem> list = super.daMgr.searchRecords( SEARCH_QUERY_ID, params ) ;
        if( list == null ) {
            list = new ArrayList<RSSNewsItem>() ;
        }
        return list ;
    }

    /**
     * Get the news item as specified by the item overview. Note that the
     * only difference between the input and output data items is that the
     * output data item has the description populated.
     *
     * @param metaData An instance of {@link RSSNewsItem} with minimally the
     *        PK information and its read status set.
     *
     * @return An instance of {@link RSSNewsItem} with the description information
     *         populated. In case the meta information could not be matched
     *         with an existing record, a null value will be returned.
     *
     * @throws DataAccessException In case an unanticipated database exception
     *          was encountered.
     */
    public RSSNewsItem getNewsItemDetail( final RSSNewsItem metaData )
        throws DataAccessException {

        final String SEARCH_QUERY_ID = "RSS.getNewsItemDetail" ;

        if( logger.isDebugEnabled() ) {
            logger.debug( "Getting news item details for " + metaData ) ;
        }
        return ( RSSNewsItem )super.daMgr.retrieveRecord( SEARCH_QUERY_ID, metaData ) ;
    }

    // ------ RSS news item source operations ----------------------------------
    /**
     * Get all the news item sources. A boolean parameter specifies whether to
     * include or exclude the inactive sources.
     *
     * @param includeInactive If set to true, even the inactive news sources
     *        will be returned.
     *
     * @return A list of {@link RSSNewsItemSource} instances. If no news sources
     *         are found matching the specified criteria, an empty list is
     *         returned, never null.
     */
    @SuppressWarnings("unchecked")
    public List<RSSNewsItemSource> getNewsSources( final boolean includeInactive )
        throws DataAccessException {

        final String QUERY_ID = "RSS.getNewsItemSources" ;

        if( logger.isDebugEnabled() ) {
            logger.debug( "Getting news item sources. " +
                          "Include inactive = " + includeInactive ) ;
        }
        List<RSSNewsItemSource> list = null ;
        final Map<String, Object> params = new HashMap<String, Object>() ;
        params.put( "includeInactive", includeInactive ) ;

        list = super.daMgr.searchRecords( QUERY_ID, params ) ;
        if( list == null ) {
            list = new ArrayList<RSSNewsItemSource>() ;
        }
        return list ;
    }

    /**
     * Get all the news item sources associated with the specified source name.
     * A boolean parameter specifies whether to include or exclude the inactive
     * sources.
     *
     * @param source The source identifier
     * @param includeInactive If set to true, even the inactive news sources
     *        will be returned.
     *
     * @return A list of {@link RSSNewsItemSource} instances. If no news sources
     *         are found matching the specified criteria, an empty list is
     *         returned, never null.
     */
    @SuppressWarnings("unchecked")
    public List<RSSNewsItemSource> getNewsSources( final String source,
                                                   final boolean includeInactive )
        throws DataAccessException {

        final String QUERY_ID = "RSS.getNewsItemSourcesForSource" ;

        if( logger.isDebugEnabled() ) {
            logger.debug( "Getting news item sources. " +
                          "Include inactive = " + includeInactive ) ;
        }
        List<RSSNewsItemSource> list = null ;
        final Map<String, Object> params = new HashMap<String, Object>() ;
        params.put( "site", source ) ;
        params.put( "includeInactive", includeInactive ) ;

        list = super.daMgr.searchRecords( QUERY_ID, params ) ;
        if( list == null ) {
            list = new ArrayList<RSSNewsItemSource>() ;
        }
        return list ;
    }
}
