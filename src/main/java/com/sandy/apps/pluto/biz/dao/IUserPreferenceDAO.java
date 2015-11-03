/**
 * Creation Date: Aug 4, 2008
 */

package com.sandy.apps.pluto.biz.dao;

import java.util.List ;

import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.dto.UserPreference ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * Encapsulates methods to manage the user's preferences against the
 * persistent storage.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface IUserPreferenceDAO {

    /**
     * Updates the list of user preferences as specified in the parameter. The
     * parameters present in the specified list would be deleted and inserted
     * as a part of this operation.
     *
     * @param preferences A list of user preferences
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the database operation.
     *
     * @event {@link EventType#USER_PREF_CHANGED} is published on each
     *        successful update, the value of this event is the user preference
     *        instance that has been updated in the database.
     */
    void updatePreferences( final List<UserPreference> preferences )
        throws DataAccessException ;

    /**
     * Returns a list of {@link UserPreference} instances as stored in the
     * application data store.
     *
     * @return A list of user preferences
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the database operation.
     */
    List<UserPreference> getUserPreferences()
        throws DataAccessException ;
}
