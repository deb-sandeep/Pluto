/**
 * Creation Date: Aug 19, 2008
 */

package com.sandy.apps.pluto.biz.dao;

import java.util.List ;
import java.util.Map ;

import com.sandy.apps.pluto.shared.dto.JobAttribute ;
import com.sandy.apps.pluto.shared.dto.JobConfig ;
import com.sandy.apps.pluto.shared.dto.JobDef ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * This interfaces exposes data access operations related to Job definition and
 * instance configurations stored in the persistent storage.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface IJobDAO {

    /**
     * Retrieves the job definition for the given job type.
     *
     * @param type The job type for which the definition is required.
     * @return A {@link JobDef} instance or null if no job definition exists
     *         for the given type.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         database operation.
     */
    JobDef getJobDefinition( final String type )
        throws DataAccessException ;

    /**
     * Retrieves a list of job definitions registered in the system.
     *
     * @return A list of {@link JobDef} instance registered in the system.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         database operation.
     */
    List<JobDef> getJobDefinitions()
        throws DataAccessException ;

    /**
     * Retrieves the job configuration for the given job identifier.
     *
     * @param jobId The unique job identifier for the job.
     * @return A {@link JobConfig} instance or null if no job configuration
     *         exists for the given type.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         database operation.
     */
    JobConfig getJobConfig( final Integer jobId )
        throws DataAccessException ;

    /**
     * Retrieves a list of all {@link JobConfig} registered in the system.
     *
     * @return A list of {@link JobConfig} instances registered in the system.
     *         If no job configurations are registered, an empty list is
     *         returned, never null.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         database operation.
     */
    List<JobConfig> getJobConfigs()
        throws DataAccessException ;

    /**
     * Adds (inserts) a job configuration into the persistent storage.
     *
     * @param jobConfig The job configuration to add to the system. Only the
     *        type attribute is fetched from the associated job definition, rest
     *        all the fields of job definition will be ignored during insertion.
     *
     * @return The same instance of {@link JobConfig} enriched with the newly
     *         generated identifier for the job configuration.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         database operation.
     */
    JobConfig addJobConfig( final JobConfig jobConfig )
        throws DataAccessException ;

    /**
     * Updates the given job configuration into the persistent storage.
     *
     * @param jobConfig The job configuration to update in the system. The
     *        job definition for a job is a not updatable field and is set
     *        during creation.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         database operation.
     */
    void updateJobConfig( final JobConfig jobConfig )
        throws DataAccessException ;

    /**
     * Deletes the given job configuration from the persistent storage.
     *
     * @param jobId The job identifier to delete.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         database operation.
     */
    void deleteJobConfig( final Integer jobId )
        throws DataAccessException ;

    /**
     * Retrieves the attributes for the given job identifier. Job attributes
     * are returned as a map of String key versus the value of the key.
     *
     * @param jobId The job identifier to delete.
     *
     * @return The attributes of the given job identifier as a map. If no
     *         identifiers exists an empty map will be returned, never null.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         database operation.
     */
    Map<String, List<JobAttribute>> getJobAttributes( final Integer jobId )
        throws DataAccessException ;

    /**
     * Saves the attributes of a job as provided in the input map. Please note
     * that all the properties as specified in the map would be deleted and
     * reinserted again, including indexed properties. The properties which are
     * not specified in the input map will be left untouched.
     *
     * @param attribs The attributes of the given job.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         database operation.
     */
    void saveJobAttributes( final Map<String, List<JobAttribute>> attribs )
        throws DataAccessException ;

    /**
     * Updates the specified job attribute. Note that it is assumed that such an
     * attribute already exists, if not this operation will have no action.
     * Also, only the value, extraData1 and extraData2 attributes are updated.
     * The name, sequence and JobID of the attribute are not updated.
     *
     * @param attrib The job attribute to update.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         database operation.
     */
    void updateJobAttribute( JobAttribute attrib )
        throws DataAccessException ;
}
