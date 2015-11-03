/**
 * 
 * 
 * 
 *
 * Creation Date: Nov 25, 2008
 */

package com.sandy.stocktracker.test.poc;
import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;

/**
 * This class is a simple POC for parsing RSS/Atom feeds using the ROME framework.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class RSSParser {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( RSSParser.class ) ;

    @SuppressWarnings("unchecked")
    public void test() throws Exception {
        final SyndFeedInput input = new SyndFeedInput( true );
        final SyndFeed feed = input.build( new File( "c:\\temp\\google.xml" ) ) ;

        logger.debug( "Successfully parsed the RSS feed" ) ;
        logger.debug( "Author      = " + feed.getAuthors() ) ;
        logger.debug( "Categories  = " + feed.getCategories() ) ;
        final List<SyndEntry> entries = feed.getEntries() ;
        for( final SyndEntry entry : entries ) {
            logger.debug( "Title = " + StringEscapeUtils.unescapeHtml( entry.getTitle() ) ) ;
            logger.debug( "Description = " + StringEscapeUtils.unescapeHtml( entry.getDescription().getValue() ) ) ;
            logger.debug( entry.getUri() ) ;
            logger.debug( "Updated date = " + entry.getUpdatedDate() ) ;
            logger.debug( "Published date = " + entry.getPublishedDate() ) ;
            logger.debug( "====================================================" ) ;
        }
    }

    public static void main( final String[] args ) throws Exception {
        new RSSParser().test() ;
    }
}
