/**
 * Creation Date: Nov 27, 2008
 */

package com.sandy.apps.pluto.biz.svc.impl;
import java.io.StringReader ;
import java.net.URL ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.List ;

import org.apache.commons.lang.StringEscapeUtils ;
import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.dao.IRSSDAO ;
import com.sandy.apps.pluto.biz.svc.INetworkSvc ;
import com.sandy.apps.pluto.biz.svc.IRSSSvc ;
import com.sandy.apps.pluto.shared.ErrorCode ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.dto.RSSNewsItem ;
import com.sandy.apps.pluto.shared.dto.RSSNewsItemSource ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;
import com.sun.syndication.feed.synd.SyndEntry ;
import com.sun.syndication.feed.synd.SyndFeed ;
import com.sun.syndication.io.SyndFeedInput ;

/**
 * This class realizes the operations set forth by the {@link IRSSSvc}
 * interface.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class RSSSvc implements IRSSSvc {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( RSSSvc.class ) ;

    /**
     * INJECTABLE: This variable should be injected with a reference to the
     * implementation of IRSSDAO interface.
     */
    private IRSSDAO rssDAO = null ;

    /**
     * INJECTABLE: This variable should be injected with a reference to the
     * implementation of INetworkSvc interface.
     */
    private INetworkSvc networkSvc = null ;

    /** Public no argument constructor. */
    public RSSSvc() {
        super() ;
    }

    /**
     * @return the networkSvc
     */
    public INetworkSvc getNetworkSvc() {
        return this.networkSvc ;
    }

    /**
     * @param networkSvc the networkSvc to set
     */
    public void setNetworkSvc( final INetworkSvc networkSvc ) {
        this.networkSvc = networkSvc ;
    }

    /**
     * @return the rssDAO
     */
    public IRSSDAO getRssDAO() {
        return this.rssDAO ;
    }

    /**
     * @param rssDAO the rssDAO to set
     */
    public void setRssDAO( final IRSSDAO rssDAO ) {
        this.rssDAO = rssDAO ;
    }

    /**
     * Parse RSS contents from a String. This function accepts a String which
     * is assumed to be a valid RSS content and parses the string to produce
     * a list of RSSNewsItem instances.
     *
     * @param site The source of the RSS content
     * @param category The category of the RSS content
     * @param rssContent A string representing a valid RSS feed content
     *
     * @return A list of RSSNewsItem instances extracted from the string. If
     *         there are no news items in the supplied content, an empty list
     *         is returned - never null.
     *
     * @throws An instance of STException with an error code of
     *        {@link ErrorCode#RSS_PARSE_FAILURE} and an appropriate error
     *        message providing details of the exception
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<RSSNewsItem> parseRSSContent( final String site,
                                              final String category,
                                              final String rssContent )
            throws STException {

        final SyndFeedInput input = new SyndFeedInput( true ) ;
        final List<RSSNewsItem> newsItems = new ArrayList<RSSNewsItem>() ;

        try {
            final StringReader reader = new StringReader( rssContent ) ;
            final SyndFeed feed = input.build( reader ) ;
            final List<SyndEntry> entries = feed.getEntries() ;

            RSSNewsItem newsItem = null ;
            for( final SyndEntry entry : entries ) {

                newsItem = new RSSNewsItem() ;
                newsItem.setSite( site ) ;
                newsItem.setCategory( category ) ;
                newsItem.setPublishDate( entry.getPublishedDate() ) ;
                newsItem.setNewItem( true ) ;

                // Strip off the title from any HTML tags
                String tempHTMLStr = entry.getTitle() ;
                tempHTMLStr = tempHTMLStr.replaceAll( "\\<.*?>", "" ) ;
                newsItem.setTitle( StringEscapeUtils.unescapeHtml( tempHTMLStr ) ) ;

                // Strip off the description from any HTML tags
                tempHTMLStr = entry.getDescription().getValue() ;
                tempHTMLStr = tempHTMLStr.replaceAll( "\\<.*?>", "" ) ;
                newsItem.setDescription( StringEscapeUtils.unescapeHtml( tempHTMLStr ) ) ;

                if( newsItem.getPublishDate() == null ) {
                    newsItem.setPublishDate( new Date() ) ;
                }

                // Set the URL. Set the URL if and only if it is a valid URL
                try {
                    final URL url = new URL( entry.getUri() ) ;
                    newsItem.setUrl( url.toString() ) ;
                }
                catch ( final Exception e ) {
                    logger.debug( "An invalid URL found in a news item " + newsItem ) ;
                }

                newsItems.add( newsItem ) ;
            }
        }
        catch ( final Exception e ) {
            final String msg = "Error parsing RSS content. Msg=" + e.getMessage() ;

            // Log only a debug message since we are rethrowing the exception.
            // The caller will handle proper logging.
            logger.debug( msg, e ) ;
            throw new STException( msg, e, ErrorCode.RSS_PARSE_FAILURE ) ;
        }
        return newsItems ;
    }

    /**
     * Parse RSS contents of all the URLs associated with a specified source.
     * This operations matches the source with the equivalent URLs, retrieves
     * the content of the each URL and creates a list of RSSNewsItem instances.
     * This method returns an assimilated list of all the news items.
     * <p>
     * In case processing any of the URLs causes an unanticipated exception,
     * the operation is aborted by raising the appropriate error code.
     *
     * @param site The source of the RSS content. This is the symbolic name
     *        with which the source is registered in the system.
     * @param category The category of the RSS content
     *
     * @return A list of RSSNewsItem instances extracted from the string
     *
     * @throws An instance of STException with the following error codes.
     *        <ul>
     *          <li>{@link ErrorCode#RSS_PARSE_FAILURE}</li>
     *          <li>{@link ErrorCode#DOWNLOAD_FAILURE}</li>
     *        </ul>
     *        The exception instance would also have an appropriate error
     *        message providing details of the exception
     */
    public List<RSSNewsItem> parseRSSContent( final String site, final String category )
        throws STException {

        final List<RSSNewsItemSource> sources = this.rssDAO.getNewsSources( site, false ) ;
        final List<RSSNewsItem> newsItems = new ArrayList<RSSNewsItem>() ;

        LogMsg.info( "Importing RSS news from " + site ) ;
        for( final RSSNewsItemSource src : sources ) {

            try {
                final String rssContent = new String( this.networkSvc.getRawGETResult( src.getUrl() ) ) ;
                newsItems.addAll( this.parseRSSContent( site, category, rssContent ) ) ;
            }
            catch ( final Exception e ) {
                STException.wrapAndThrow( e ) ;
            }
        }

        return newsItems ;
    }

    /**
     * This operation imports the latest news from the supplied news source.
     * The import operation consists of converting the source to it's host URL,
     * parsing the contents of the URL and inserting the non duplicate news items.
     * If this operation was successful in inserting one or more non duplicate
     * news items, a list of the newly added news items are published on the
     * event bus for the listening consumers. Please note that his operation
     * parses all the URLs associated with the source.
     *
     * @param site The source of the RSS content. This is the symbolic name
     *        with which the source is registered in the system.
     * @param category The category of the RSS content
     *
     * @return The number of news items imported from this site and category
     *
     * @event {@link EventType#RSS_NEWS_IMPORTED}. This event is published on
     *        the bus when one or more news items are imported into the database.
     *        The value of this event is a list of {@link RSSNewsItem} instances
     *        that were inserted as a part of this operation.
     *
     * @throws An instance of STException with the following error codes.
     *        <ul>
     *          <li>{@link ErrorCode#RSS_PARSE_FAILURE}</li>
     *          <li>{@link ErrorCode#DOWNLOAD_FAILURE}</li>
     *        </ul>
     *        The exception instance would also have an appropriate error
     *        message providing details of the exception
     */
    public int importRSS( final String site, final String category )
        throws STException {

        int numItems = 0 ;
        final List<RSSNewsItem> newsItems = parseRSSContent( site, category ) ;
        final List<RSSNewsItem> insItems  = new ArrayList<RSSNewsItem>() ;

        for( final RSSNewsItem item : newsItems ) {
            final RSSNewsItem ins = this.rssDAO.insertNewsItem( item ) ;
            if( ins != null ) {
                item.setDescription( null ) ;
                insItems.add( item ) ;
            }
        }

        if( !insItems.isEmpty() ) {
            EventBus.publish( EventType.RSS_NEWS_IMPORTED, insItems ) ;
            numItems = insItems.size() ;
        }

        return numItems ;
    }

    /**
     * Imports news from all the active news sites registered in the system
     * and returns a count of all the news items imported. Any exception
     * resulting from the import is logged, but not raised from this method.
     *
     * @return The number of new news items imported by this method.
     */
    public int importActiveNews() {

        final List<RSSNewsItemSource> newsSources = this.rssDAO.getNewsSources( false ) ;
        int numItems = 0 ;
        for( final RSSNewsItemSource src : newsSources ) {
            final String site = src.getSite() ;
            final String cat  = src.getCategory() ;
            try {
                LogMsg.info( "Importing news from " + site + " and category " + cat ) ;
                numItems += ServiceMgr.getRSSSvc().importRSS( site, cat ) ;
            }
            catch ( final Exception e ) {
                final String errMsg = "Error importing news from site " + site +
                                      " and category = " + cat ;
                logger.debug(  errMsg, e ) ;
                LogMsg.error( errMsg ) ;
            }
        }

        return numItems ;
    }

    /**
     * Gets all the news items overviews for the specified period. Whether to
     * include or exclude the already read news items is specified by the boolean
     * parameter.
     *
     * @param startDate The start of the time period
     * @param endDate The end of the time period
     * @param includeRead A boolean flag indicating whether the return list
     *        should contain items which have already been read.
     *
     * @return A list of {@link RSSNewsItem} instances or an empty list if no
     *         news items could be found for the specified period. Never null.
     *
     * @throws An instance of {@link STException} in case an unanticipated
     *         exception condition was encountered.
     */
    public List<RSSNewsItem> getNewsItems( final Date startDate, final Date endDate,
                                           final boolean includeRead )
        throws STException {
        return this.rssDAO.getNewsItemOverviews( startDate, endDate, includeRead ) ;
    }

    /**
     * Returns the details of the news item. If the news item is not yet read,
     * this service marks the news item as read.
     *
     * @param overview The news item overview with minimally the source, title
     *        and the publish date specified.
     *
     * @return An {@link RSSNewsItem} instance with all the details of the
     *         item.
     *
     * @throws An instance of {@link STException} in case an unanticipated
     *         exception condition was encountered.
     */
    public RSSNewsItem getNewsDetails( final RSSNewsItem overview )
        throws STException {

        final RSSNewsItem newsItem = this.rssDAO.getNewsItemDetail( overview ) ;
        if( newsItem.isNewItem() ) {
            overview.setNewItem( false ) ;
            this.rssDAO.updateNewsItem( overview ) ;
        }
        return newsItem ;
    }

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
    public List<RSSNewsItemSource> getNewsSources( final boolean includeInactive )
        throws DataAccessException {
        return this.rssDAO.getNewsSources( includeInactive ) ;
    }
}
