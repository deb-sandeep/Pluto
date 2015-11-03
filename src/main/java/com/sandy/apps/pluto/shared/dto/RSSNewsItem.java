/**
 * Creation Date: Nov 26, 2008
 */

package com.sandy.apps.pluto.shared.dto;

import java.io.Serializable ;
import java.util.Date ;

/**
 * This data transfer object encapsulates the information related to one
 * RSS news item.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class RSSNewsItem implements Serializable, Comparable<RSSNewsItem> {

    /** Generated serial version UID. */
    private static final long serialVersionUID = -430787609618543981L ;

    /** The date when the news was published by the host site. */
    private Date publishDate = null ;

    /** The title of the news item. */
    private String title = null ;

    /** The description of the news. */
    private String description = null ;

    /** The URL associated with the news item, which will provide more details. */
    private String url = null ;

    /** A boolean value indicating whether this news item is new or has been read. */
    private boolean newItem = true ;

    /** The category of the news item, Biz, Finance etc. */
    private String category = null ;

    /**
     * The site of this news item. The site is typically an identifier
     * which identifies from where this news item was obtained.
     */
    private String site = null ;

    /** Public no argument constructor. */
    public RSSNewsItem() {
        super() ;
    }

    /**
     * @return the publishDate
     */
    public Date getPublishDate() {
        return this.publishDate ;
    }

    /**
     * @param publishDate the publishDate to set
     */
    public void setPublishDate( final Date publishDate ) {
        this.publishDate = publishDate ;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return this.title ;
    }

    /**
     * @param title the title to set
     */
    public void setTitle( final String title ) {
        this.title = title ;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return this.description ;
    }

    /**
     * @param description the description to set
     */
    public void setDescription( final String description ) {
        this.description = description ;
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
     * @return the newItem
     */
    public boolean isNewItem() {
        return this.newItem ;
    }

    /**
     * @param newItem the newItem to set
     */
    public void setNewItem( final boolean newItem ) {
        this.newItem = newItem ;
    }

    /**
     * A string representation of this item.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer() ;
        buffer.append( "RSSNewsItem[" ) ;
        buffer.append( " site = " ).append( this.site ) ;
        buffer.append( " category = " ).append( this.category ) ;
        buffer.append( " title = " ).append( this.title ) ;
        buffer.append( " publishDate = " ).append( this.publishDate ) ;
        buffer.append( " url = " ).append( this.url ) ;
        buffer.append( " description = " ).append( this.description ) ;
        buffer.append( " newItem = " ).append( this.newItem ) ;
        buffer.append( "]" ) ;
        return buffer.toString() ;
    }

    /**
     * Compares itself with another news item. News items are always sorted
     * in descending order of their publish time. So, a news item with higher
     * value of publish time is greater.
     */
    @Override
    public int compareTo( final RSSNewsItem o ) {
        return o.getPublishDate().compareTo( this.publishDate ) ;
    }
}
