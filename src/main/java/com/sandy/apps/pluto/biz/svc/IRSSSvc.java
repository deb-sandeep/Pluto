/**
 * Creation Date: Nov 27, 2008
 */

package com.sandy.apps.pluto.biz.svc;

import java.util.Date ;
import java.util.List ;

import com.sandy.apps.pluto.shared.ErrorCode ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.dto.RSSNewsItem ;
import com.sandy.apps.pluto.shared.dto.RSSNewsItemSource ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * This interface encapsulates operations related to RSS news items.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface IRSSSvc {

    /**
     * Parse RSS contents from a String. This function accepts a String which
     * is assumed to be a valid RSS content and parses the string to produce
     * a list of RSSNewsItem instances.
     *
     * @param site The source of the RSS content
     * @param category The category of the RSS content
     * @param rssContent A string representing a valid RSS feed content
     *
     * @return A list of RSSNewsItem instances extracted from the string
     *
     * @throws An instance of STException with an error code of
     *        {@link ErrorCode#RSS_PARSE_FAILURE} and an appropriate error
     *        message providing details of the exception
     */
    List<RSSNewsItem> parseRSSContent( final String site,
                                       final String category,
                                       final String rssContent )
        throws STException ;

    /**
     * Parse RSS contents from an specified source. This operations matches
     * the source with the equivalent URL, retrieves the content
     * of the specified URL and creates a list of RSSNewsItem instances.
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
    List<RSSNewsItem> parseRSSContent( final String site, final String category )
        throws STException ;

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
     * @event {@link EventType#RSS_NEWS_IMPORTED}. This event is published on
     *        the bus when one or more news items are imported into the database.
     *        The value of this event is a list of {@link RSSNewsItem} instances
     *        that were inserted as a part of this operation.
     *
     * @return The number of news items imported from this site and category
     *
     * @throws An instance of STException with the following error codes.
     *        <ul>
     *          <li>{@link ErrorCode#RSS_PARSE_FAILURE}</li>
     *          <li>{@link ErrorCode#DOWNLOAD_FAILURE}</li>
     *        </ul>
     *        The exception instance would also have an appropriate error
     *        message providing details of the exception
     */
    int importRSS( final String site, final String category )
        throws STException ;

    /**
     * Imports news from all the active news sites registered in the system
     * and returns a count of all the news items imported. Any exception
     * resulting from the import is logged, but not raised from this method.
     *
     * @return The number of new news items imported by this method.
     */
    int importActiveNews() ;

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
    List<RSSNewsItem> getNewsItems( final Date startDate, final Date endDate,
                                    final boolean includeRead )
        throws STException ;

    /**
     * Returns the details of the news item. If the news item is not yet read,
     * this service marks the news item as read.
     *
     * @param overview The news item overview with minimally the source and
     *        title specified.
     *
     * @return An {@link RSSNewsItem} instance with all the details of the
     *         item.
     *
     * @throws An instance of {@link STException} in case an unanticipated
     *         exception condition was encountered.
     */
    RSSNewsItem getNewsDetails( final RSSNewsItem overview )
        throws STException ;

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
    List<RSSNewsItemSource> getNewsSources( final boolean includeInactive )
        throws DataAccessException ;
}
