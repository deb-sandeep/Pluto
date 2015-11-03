/**
 * Creation Date: Mar 6, 2009
 */

package com.sandy.apps.pluto.shared.dto;
import java.io.Serializable ;
import java.util.Date ;

/**
 * This DTO class encapsulates the information about the value of a world maket
 * index. Data for the world markets is scraped from the bloomber site.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class WorldIndex implements Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1045294985031496585L ;

    private String  country      = null ;
    private String  indexName    = null ;
    private float   value        = 0.0F ;
    private float   change       = 0.0F ;
    private float   pctChange    = 0.0F ;
    private Date    time         = null ;
    private boolean itd          = false ;

    /** Public no argument constructor. */
    public WorldIndex() {
        super() ;
    }

    public String getCountry() {
        return this.country ;
    }

    public void setCountry( final String country ) {
        this.country = country ;
    }

    public String getIndexName() {
        return this.indexName ;
    }

    public void setIndexName( final String indexName ) {
        this.indexName = indexName ;
    }

    public float getValue() {
        return this.value ;
    }

    public void setValue( final float value ) {
        this.value = value ;
    }

    public float getChange() {
        return this.change ;
    }

    public void setChange( final float change ) {
        this.change = change ;
    }

    public float getPctChange() {
        return this.pctChange ;
    }

    public void setPctChange( final float pctChange ) {
        this.pctChange = pctChange ;
    }

    public Date getTime() {
        return this.time ;
    }

    public void setTime( final Date time ) {
        this.time = time ;
    }

    public boolean isItd() {
        return this.itd ;
    }

    public void setItd( final boolean itd ) {
        this.itd = itd ;
    }

    /**
     * Returns a string form of this instance.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer() ;
        buffer.append( "WorldIndex[" ) ;
        buffer.append( "change = " ).append( this.change ) ;
        buffer.append( ", country = " ).append( this.country ) ;
        buffer.append( ", indexName = " ).append( this.indexName ) ;
        buffer.append( ", pctChange = " ).append( this.pctChange ) ;
        buffer.append( ", value = " ).append( this.value ) ;
        buffer.append( ", time = " ).append( this.time ) ;
        buffer.append( ", itd = " ).append( this.itd ) ;
        buffer.append( "]" ) ;
        return buffer.toString() ;
    }
}
