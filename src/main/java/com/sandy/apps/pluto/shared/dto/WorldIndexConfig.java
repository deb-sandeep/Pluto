/**
 * Creation Date: Mar 6, 2009
 */

package com.sandy.apps.pluto.shared.dto;
import java.io.Serializable ;

/**
 * This DTO class encapsulates the information about the system configuration of
 * a world index. System configuration provides information related to the
 * region and index name along with attributes like whether the index is filtered
 * on view, URLs for fetching more information about the index etc.
 *
 * A combination of country and index name uniquely identifies an index.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class WorldIndexConfig implements Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 9850964192L ;

    private String  country      = null ;
    private String  indexName    = null ;
    private boolean viewFiltered = false ;

    /** Public no argument constructor. */
    public WorldIndexConfig() {
        super() ;
    }

    /** Public constructor. */
    public WorldIndexConfig( final String country, final String indexName ) {
        super() ;
        this.country = country ;
        this.indexName = indexName ;
        this.viewFiltered = false ;
    }

    /** Public constructor. */
    public WorldIndexConfig( final String country, final String indexName,
                             final boolean viewFiltered ) {
        this( country, indexName ) ;
        this.viewFiltered = viewFiltered ;
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

    public boolean isViewFiltered() {
        return this.viewFiltered ;
    }

    public void setViewFiltered( final boolean flag ) {
        this.viewFiltered = flag ;
    }

    /** Returns a string representation of this instance. */
    public String toString() {
        final StringBuffer buffer = new StringBuffer() ;
        buffer.append( "WorldIndexConfig[" ) ;
        buffer.append( "country = " ).append( this.country ) ;
        buffer.append( ", indexName = " ).append( this.indexName ) ;
        buffer.append( ", viewFiltered = " ).append( this.viewFiltered ) ;
        buffer.append( "]" ) ;
        return buffer.toString() ;
    }
}
