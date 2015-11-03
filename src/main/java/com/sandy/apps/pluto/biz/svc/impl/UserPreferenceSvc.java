/**
 * Creation Date: Aug 4, 2008
 */

package com.sandy.apps.pluto.biz.svc.impl;
import java.text.ParseException ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.dao.IUserPreferenceDAO ;
import com.sandy.apps.pluto.biz.svc.IUserPreferenceSvc ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.dto.UserPreference ;
import com.sandy.apps.pluto.shared.event.EventBus ;

/**
 * Implementation of the {@link IUserPreferenceSvc} implementation.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class UserPreferenceSvc implements IUserPreferenceSvc {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( UserPreferenceSvc.class ) ;

    private IUserPreferenceDAO userPrefDAO = null ;

    /** A map to store the user's preferences in memory. */
    private Map<String, String> userPrefs = null ;

    /** Public constructor. */
    public UserPreferenceSvc() {
        super() ;
    }

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
    @Override
    public String getUserPref( final String key, final String defaultValue ) {
        if( this.userPrefs == null ) {
            this.userPrefs = loadUserPreferences() ;
        }
        String retVal = this.userPrefs.get( key ) ;
        if( retVal == null ) {
            retVal = defaultValue ;
        }
        return retVal ;
    }

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
    @Override
    public boolean getBoolean( final String key, final boolean defaultValue ) {
        if( this.userPrefs == null ) {
            this.userPrefs = loadUserPreferences() ;
        }
        boolean retVal = defaultValue ;
        final String temp = this.userPrefs.get( key ) ;
        if( temp != null ) {
            retVal = Boolean.parseBoolean( temp ) ;
        }
        return retVal ;
    }

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
    @Override
    public int getInt( final String key, final int defaultValue ) {
        if( this.userPrefs == null ) {
            this.userPrefs = loadUserPreferences() ;
        }
        int retVal = defaultValue ;
        final String temp = this.userPrefs.get( key ) ;
        if( temp != null ) {
            retVal = Integer.parseInt( temp ) ;
        }
        return retVal ;
    }

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
    public Date getDate( final String key, final Date defaultValue ) {

        if( this.userPrefs == null ) {
            this.userPrefs = loadUserPreferences() ;
        }
        Date retVal = defaultValue ;
        final String temp = this.userPrefs.get( key ) ;
        if( temp != null ) {
            try {
                retVal = DF.parse( temp.trim() ) ;
            }
            catch ( final ParseException e ) {
                logger.error( "Illegal date value specified against key " +
                              key + ", value = " + temp ) ;
            }
        }
        return retVal ;
    }

    /**
     * Loads the user preferences from the database and returns the preferences
     * as a map.
     *
     * @return A map of user preferences.
     */
    @Override
    public Map<String, String> loadUserPreferences() {

        if( this.userPrefs == null ) {
            final List<UserPreference> prefList = this.userPrefDAO.getUserPreferences() ;
            final Map<String, String> prefMap = new HashMap<String, String>() ;

            for( final UserPreference pref : prefList ) {
                prefMap.put( pref.getKey(), pref.getValue() ) ;
            }
            return prefMap ;
        }
        return this.userPrefs ;
    }

    /**
     * Saves the user preferences specified as parameters.
     *
     * @param preferences A map of name value pairs which need to be saved or
     *        updated as user preferences.
     *
     * @event {@link EventType#USER_PREF_CHANGED} is published on
     *        successful update, the value of this event is the map of
     *        preferences that have been updated using this save operation.
     */
    @Override
    public void saveUserPreferences( final Map<String, String> preferences ) {

        final List<UserPreference> prefList = new ArrayList<UserPreference>() ;
        this.userPrefs.putAll( preferences ) ;

        for( final String key : preferences.keySet() ) {
            final UserPreference pref = new UserPreference() ;
            pref.setKey( key ) ;
            pref.setValue( preferences.get( key ) ) ;
            prefList.add( pref ) ;
        }

        this.userPrefDAO.updatePreferences( prefList ) ;

        // Publish the event that the preference has changed.
        EventBus.publish( EventType.USER_PREF_CHANGED, preferences ) ;
        LogMsg.info( "User preferences changed. " + preferences ) ;
    }

    /**
     * @return the userPrefDAO
     */
    public IUserPreferenceDAO getUserPrefDAO() {
        return this.userPrefDAO ;
    }

    /**
     * @param userPrefDAO the userPrefDAO to set
     */
    public void setUserPrefDAO( final IUserPreferenceDAO userPrefDAO ) {
        this.userPrefDAO = userPrefDAO ;
    }
}
