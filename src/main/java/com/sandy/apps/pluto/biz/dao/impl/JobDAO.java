/**
 * Creation Date: Aug 19, 2008
 */

package com.sandy.apps.pluto.biz.dao.impl;
import java.util.ArrayList ;
import java.util.Collections ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.dao.IJobDAO ;
import com.sandy.apps.pluto.shared.dto.JobAttribute ;
import com.sandy.apps.pluto.shared.dto.JobConfig ;
import com.sandy.apps.pluto.shared.dto.JobDef ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * Implementation of {@link IJobDAO} interface.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class JobDAO extends AbstractBaseDAO implements IJobDAO {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( JobDAO.class ) ;

    /** Public constructor. */
    public JobDAO() {
        super() ;
    }

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
    public JobDef getJobDefinition( final String type )
        throws DataAccessException {

        final String QUERY_ID = "Job.getJobDef" ;
        return ( JobDef )super.daMgr.retrieveRecord( QUERY_ID, type ) ;
    }

    /**
     * Retrieves a list of job definitions registered in the system.
     *
     * @return A list of {@link JobDef} instance registered in the system.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         database operation.
     */
    @SuppressWarnings("unchecked")
    public List<JobDef> getJobDefinitions() throws DataAccessException {

        final String QUERY_ID = "Job.getAllJobDef" ;

        List<JobDef> jobDefs = null ;
        jobDefs = super.daMgr.searchRecords( QUERY_ID, null ) ;
        if( jobDefs == null ) {
            jobDefs = Collections.emptyList() ;
        }
        return jobDefs ;
    }

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
    public JobConfig getJobConfig( final Integer jobId )
        throws DataAccessException {
        final String QUERY_ID = "Job.getJobConfig" ;
        JobConfig jobConfig = null ;

        jobConfig = ( JobConfig )super.daMgr.retrieveRecord( QUERY_ID, jobId ) ;
        if( jobConfig != null ) {
            jobConfig.setAttribute( getJobAttributes( jobId ) ) ;
        }

        return jobConfig ;
    }

    /**
     * Retrieves a list of all {@link JobConfig} registered in the system.
     *
     * @return A list of {@link JobConfig} instances registered in the system.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         database operation.
     */
    @SuppressWarnings("unchecked")
    public List<JobConfig> getJobConfigs()
        throws DataAccessException {

        final String QUERY_ID = "Job.getAllJobConfig" ;

        List<JobConfig> jobCfgs = null ;
        jobCfgs = super.daMgr.searchRecords( QUERY_ID, null ) ;
        if( jobCfgs == null ) {
            jobCfgs = Collections.emptyList() ;
        }
        else {
            for( final JobConfig cfg : jobCfgs ) {
                cfg.setAttribute( getJobAttributes( cfg.getJobId() ) ) ;
            }
        }
        return jobCfgs ;
    }

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
    public JobConfig addJobConfig( final JobConfig jobConfig )
        throws DataAccessException {

        final String INS_QUERY_ID        = "Job.addJobConfig" ;
        final String GET_ID_QUERY_ID     = "Job.getNextJobID" ;

        // Now get the identifier for the newly inserted task
        final Integer id = ( Integer )super.daMgr.retrieveRecord( GET_ID_QUERY_ID, null ) ;

        // Insert the task into the database
        jobConfig.setJobId( id ) ;
        super.daMgr.createRecord( INS_QUERY_ID, jobConfig ) ;

        // Now save the job attributes
        saveJobAttributes( jobConfig.getAttributes() ) ;

        return jobConfig ;
    }

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
    public void updateJobConfig( final JobConfig jobConfig )
        throws DataAccessException {

        final String UPD_QUERY_ID  = "Job.addJobConfig" ;
        super.daMgr.updateRecord( UPD_QUERY_ID, jobConfig ) ;
        saveJobAttributes( jobConfig.getAttributes() ) ;
    }

    /**
     * Deletes the given job configuration from the persistent storage.
     *
     * @param jobId The job identifier to delete.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         database operation.
     */
    public void deleteJobConfig( final Integer jobId )
        throws DataAccessException {

        final String DEL_QUERY_ID  = "Job.deleteJobConfig" ;
        super.daMgr.deleteRecord( DEL_QUERY_ID, jobId ) ;
    }

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
    @SuppressWarnings("unchecked")
    public Map<String, List<JobAttribute>> getJobAttributes( final Integer jobId )
        throws DataAccessException {

        final String SEL_ATTR_ID = "Job.getAttributes" ;
        final Map<String, List<JobAttribute>> retVal = new HashMap<String,  List<JobAttribute>>() ;
        List<JobAttribute> attrList = null ;

        attrList = super.daMgr.searchRecords( SEL_ATTR_ID, jobId ) ;
        for( final JobAttribute attr : attrList ) {

            final String       name = attr.getName() ;
            List<JobAttribute> val  = retVal.get( name ) ;
            if( val == null ) {
                val = new ArrayList<JobAttribute>() ;
                retVal.put( name, val ) ;
            }
            val.add( attr ) ;
        }
        return retVal ;
    }

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
    public void saveJobAttributes( final Map<String, List<JobAttribute>> attribs )
        throws DataAccessException {

        final String INS_ATTR_ID = "Job.insertAttribute" ;
        final String DEL_ATTR_ID = "Job.deleteAttribute" ;

        final Map<String, Object> params = new HashMap<String, Object>() ;
        for( final String key : attribs.keySet() ) {

            final List<JobAttribute> attributes = attribs.get( key ) ;
            if( attributes != null && !attributes.isEmpty() ) {

                final Integer jobId = attributes.get( 0 ).getJobId() ;

                // Delete the existing values from the database
                params.clear() ;
                params.put( "jobId", jobId ) ;
                params.put( "attrName", key ) ;
                super.daMgr.deleteRecord( DEL_ATTR_ID, params ) ;

                // Insert each of the attributes
                for( final JobAttribute attr : attributes ) {
                    super.daMgr.createRecord( INS_ATTR_ID, attr ) ;
                }
            }
        }
    }

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
    public void updateJobAttribute( final JobAttribute attrib )
        throws DataAccessException {

        final String UPD_ATTR_ID = "Job.updateAttribute" ;
        super.daMgr.updateRecord( UPD_ATTR_ID, attrib ) ;
    }
}
