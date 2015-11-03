/**
 * Creation Date: Nov 27, 2008
 */

package com.sandy.apps.pluto.biz.dao;

import java.util.Date ;
import java.util.List ;

import com.sandy.apps.pluto.shared.dto.RSSNewsItem ;
import com.sandy.apps.pluto.shared.dto.RSSNewsItemSource ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * This interface encapsulates operations related to the RSS news items and
 * related information in the persistent storage. Note that this interface
 * defines only the contract of the data access operations and not the behavioral
 * aspects of the RSS news items.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface IRSSDAO {

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
    RSSNewsItem insertNewsItem( final RSSNewsItem newsItem )
        throws DataAccessException ;

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
    void updateNewsItem( final RSSNewsItem newsItem )
        throws DataAccessException ;

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
    List<RSSNewsItem> getNewsItemOverviews( final Date startDate, final Date endDate,
                                            final boolean includeReadItems )
        throws DataAccessException ;

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
    RSSNewsItem getNewsItemDetail( final RSSNewsItem metaData )
        throws DataAccessException ;

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
    List<RSSNewsItemSource> getNewsSources( final boolean includeInactive )
        throws DataAccessException ;

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
    List<RSSNewsItemSource> getNewsSources( final String source,
                                            final boolean includeInactive )
        throws DataAccessException ;

}
