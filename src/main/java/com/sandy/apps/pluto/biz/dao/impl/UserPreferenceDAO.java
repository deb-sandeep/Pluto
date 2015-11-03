/**
 * Creation Date: Aug 4, 2008
 */

package com.sandy.apps.pluto.biz.dao.impl;
import java.util.List ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.dao.IUserPreferenceDAO ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.dto.UserPreference ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * Implementation of the {@link IUserPreferenceDAO} interface.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class UserPreferenceDAO extends AbstractBaseDAO
       implements IUserPreferenceDAO {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( UserPreferenceDAO.class ) ;

    /** Public constructor. */
    public UserPreferenceDAO() {
        super() ;
    }

    /**
     * Returns a list of {@link UserPreference} instances as stored in the
     * application data store.
     *
     * @return A list of user preferences
     *
     * @throws DataAccessException In case an exception is encountered during
     *         the database operation.
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<UserPreference> getUserPreferences() throws DataAccessException {
        final String QUERY_ID = "UserPreference.getAll" ;
        return super.daMgr.searchRecords( QUERY_ID, null ) ;
    }

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
     * @event {@link EventType#USER_PREF_CHANGED} is published on successful
     *        update. The value of this event is null. Receivers are required
     *        to poll the user preference service for the properties they
     *        are interested in.
     */
    @Override
    public void updatePreferences( final List<UserPreference> preferences )
            throws DataAccessException {
        final String INSERT_QUERY_ID = "UserPreference.insert" ;
        final String DELETE_QUERY_ID = "UserPreference.delete" ;

        for( final UserPreference pref : preferences ) {
            super.daMgr.deleteRecord( DELETE_QUERY_ID, pref ) ;
            super.daMgr.createRecord( INSERT_QUERY_ID, pref ) ;
        }
    }
}
