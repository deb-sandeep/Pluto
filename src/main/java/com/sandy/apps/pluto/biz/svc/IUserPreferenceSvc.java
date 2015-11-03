/**
 * Creation Date: Aug 4, 2008
 */

package com.sandy.apps.pluto.biz.svc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.sandy.apps.pluto.shared.EventType ;

/**
 * This service encapsulates operations related to the management of uimport java.text.DateFormat ;
import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.Map ;

import com.sandy.apps.pluto.shared.EventType ;
nd not typically supposed to be changed by the user, however preferences
 * are for user to change during runtime - for example proxy settings.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface IUserPreferenceSvc {

    /** The date format used to store user preference dates. */
    DateFormat DF = new SimpleDateFormat( "dd-MM-yyyy" ) ;

    /**
     * Gets the user preference set against the specified configuration key as
     * a String value. If no such configuration key is found, the default
     * value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default configuration value for the key.
     *
     * @return The configuration value.
     */
    String getUserPref( final String key, final String defaultValue ) ;

    /**
     * Gets the user preference set against the specified configuration key as
     * an integer value. If no such configuration key is found, the default
     * value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default configuration value for the key.
     *
     * @return The configuration value.
     */
    int getInt( final String key, final int defaultValue ) ;

    /**
     * Gets the user preference set against the specified configuration key as
     * a boolean value. If no such configuration key is found, the default
     * value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default configuration value for the key.
     *
     * @return The configuration value.
     */
    boolean getBoolean( final String key, final boolean defaultValue ) ;

    /**
     * Gets the user preference set against the specified configuration key as
     * a Date value as per the DF date format. If no such configuration key is
     * found, the default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default configuration value for the key.
     *
     * @return The configuration value.
     */
    Date getDate( final String key, final Date defaultValue ) ;

    /**
     * Saves the user preferences specified as parameters.
     *
     * @param preferences A map of name value pairs which need to be saved or
     *        updated as user preferences.
     *
     * @event {@link EventType#USER_PREF_CHANGED} is published on
     *        successful update, the value of this event is the map of preferences
     *        that have been updated.
     */
    void saveUserPreferences( final Map<String, String> preferences ) ;

    /**
     * Loads the user preferences from the database and returns the preferences
     * as a map.
     *
     * @return A map of user preferences.
     */
    Map<String, String> loadUserPreferences() ;
}
