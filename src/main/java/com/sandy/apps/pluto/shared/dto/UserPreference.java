/**
 * Creation Date: Aug 4, 2008
 */

package com.sandy.apps.pluto.shared.dto;

/**
 * A simple class which encapsulates a particular user preference as a name
 * value pair.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class UserPreference {

    private String key = null ;
    private String value = null ;

    /** Public constructor. */
    public UserPreference() {
        super() ;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return this.key ;
    }

    /**
     * @param key the key to set
     */
    public void setKey( final String key ) {
        this.key = key ;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return this.value ;
    }

    /**
     * @param value the value to set
     */
    public void setValue( final String value ) {
        this.value = value ;
    }

    /**
     * A toString implementation of this instance.
     * @return String implementation
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer() ;
        buffer.append( "UserPreference[" ) ;
        buffer.append( "key=" + this.key ) ;
        buffer.append( ", value=" + this.value ) ;
        buffer.append( "]" ) ;
        return buffer.toString() ;
    }
}
