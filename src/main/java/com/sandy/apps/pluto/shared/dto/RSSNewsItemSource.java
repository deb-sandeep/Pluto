/**
 * Creation Date: Nov 27, 2008
 */

package com.sandy.apps.pluto.shared.dto;
import org.apache.log4j.Logger ;

/**
 * This DTO class encapsulates information about one RSS feed site. A feed
 * site is qualified by a site name and a URL. The site name is a Pluto
 * defined identifier for a RSS feed site. For example the RSS feed from
 * NDTV.com could have the site as NDTV. The site helps relate news items
 * to their points of origin.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class RSSNewsItemSource {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( RSSNewsItemSource.class ) ;

    /** The URL of the feed. */
    private String url = null ;

    /** The site of the feed. */
    private String site = null ;

    /** The category of the news item, Biz, Finance etc. */
    private String category = null ;

    /** A boolean flag to indicate if this feed site is active. */
    private boolean active = false ;

    /** Public no argument constructor. */
    public RSSNewsItemSource() {
        super() ;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return this.url ;
    }

    /**
     * @param url the url to set
     */
    public void setUrl( final String url ) {
        this.url = url ;
    }

    /**
     * @return the site
     */
    public String getSite() {
        return this.site ;
    }

    /**
     * @param site the site to set
     */
    public void setSite( final String site ) {
        this.site = site ;
    }

    /**
     * @return the category
     */
    public String getCategory() {
        return this.category ;
    }

    /**
     * @param category the category to set
     */
    public void setCategory( final String category ) {
        this.category = category ;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return this.active ;
    }

    /**
     * @param active the active to set
     */
    public void setActive( final boolean active ) {
        this.active = active ;
    }

    /** A string representation of this instance's state. */
    public String toString() {
        final StringBuffer buffer = new StringBuffer() ;
        buffer.append( "RSSNewsItemSource[" ) ;
        buffer.append( " active = " ).append( this.active ) ;
        buffer.append( " site = " ).append( this.site ) ;
        buffer.append( " category = " ).append( this.category ) ;
        buffer.append( " url = " ).append( this.url ) ;
        buffer.append( "]" ) ;
        return buffer.toString() ;
    }
}
