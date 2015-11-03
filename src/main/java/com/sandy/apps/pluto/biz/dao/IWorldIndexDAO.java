/**
 * Creation Date: Aug 9, 2008
 */

package com.sandy.apps.pluto.biz.dao;

import java.util.List ;

import com.sandy.apps.pluto.shared.dto.WorldIndexConfig ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * This DAO specifies the persistent operations related to world indexes.
 * World Index persistent data is maintained as configuration (filtered
 * indexes) and EOD data. This interface exposes operations to retrieve and
 * manipulate such information.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface IWorldIndexDAO {

    /**
     * Saves an index configuration in the persistent storage. If such an
     * index configuration is not present in the database, the configuration
     * is created. If such an index configuration is already present, the
     * value is updated.
     *
     * @param cfg An instance of the world index configuration.
     *
     * @throws DataAccessException In case of unanticipated database exceptions
     */
    void saveIndexConfig( final WorldIndexConfig cfg )
        throws DataAccessException ;

    /**
     * Returns a list of all index configurations stored in the database.
     *
     * @return A list of {@link WorldIndexConfig} instances. If no index
     *         configurations are present, an empty list is returned. Never
     *         null.
     *
     * @throws DataAccessException In case of unanticipated database exceptions
     */
    List<WorldIndexConfig> getAllIndexConfig()
        throws DataAccessException ;
}
