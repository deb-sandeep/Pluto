/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 19, 2008
 */

package com.sandy.stocktracker.test.poc;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.biz.dao.IJobDAO ;
import com.sandy.apps.pluto.biz.svc.ITaskSvc ;
import com.sandy.apps.pluto.shared.dto.JobAttribute ;
import com.sandy.apps.pluto.shared.dto.JobConfig ;
import com.sandy.apps.pluto.shared.dto.JobDef ;
import com.sandy.apps.pluto.shared.util.bootstrap.Bootstrap ;
import com.sandy.apps.pluto.shared.util.util.BizObjectFactory ;

/**
 * This class tests the operations of TaskDAO during development.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class JobDAOTest {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( JobDAOTest.class ) ;

    /** A reference to the job DAO instance. */
    private static IJobDAO jobDAO = null ;

    public static void main( final String[] args ) throws Exception {

        final String cfgPath = "/com/sandy/stocktracker/bootstrap-config.xml" ;
        new Bootstrap().initialize( JobDAOTest.class.getResource( cfgPath ) ) ;
        final BizObjectFactory of = ( BizObjectFactory )BizObjectFactory.getInstance() ;
        jobDAO = ( IJobDAO )of.getBean( "JobDAO" ) ;

        try {
            final JobDAOTest test = new JobDAOTest() ;
            test.testGetAllJobDefs() ;
            test.testGetJobDef() ;
            test.testGetJobConfig() ;
            test.testGetAllJobConfig() ;
            test.testAddJobConfig() ;
            test.testGetJobAttributes() ;
            test.testDeleteJobConfig() ;
        }
        finally {
            final ITaskSvc svc = ( ITaskSvc )of.getBean( "TaskSvc" ) ;
            svc.shutdownScheduler() ;
        }
    }

    private void testDeleteJobConfig() {
        jobDAO.deleteJobConfig( new Integer( 2 ) ) ;
    }

    private void testGetJobAttributes() {
        final Map<String, List<JobAttribute>> attrs = jobDAO.getJobAttributes( new Integer( 2 ) ) ;
        logger.debug( "Printing attributes" ) ;
        logger.debug( attrs ) ;
    }

    private void testAddJobConfig() throws Exception {
        JobConfig job = new JobConfig() ;
        job.setName( "Test Job" ) ;
        job.setDescription( "This is a test job" ) ;
        job.setJobDef( jobDAO.getJobDefinition( "ITDImport" ) ) ;
        job.setCron( "0/2 * * ? * 3-6" ) ;
        job.setStartupType( "AUTO" ) ;
        job.setUpperTimeBand( "07:00:00" ) ;
        job.setLowerTimeBand( "00:00:00" ) ;

        job = jobDAO.addJobConfig( job ) ;
        logger.debug( "New job added " + job.getJobId() ) ;
    }

    private void testGetAllJobDefs() throws Exception {
        final List<JobDef> defs = jobDAO.getJobDefinitions() ;
        logger.debug( "Get all Job definitions" ) ;
        for( final JobDef def : defs ) {
            logger.debug( "\t" + def ) ;
        }
    }

    private void testGetJobDef() throws Exception {
        final JobDef def = jobDAO.getJobDefinition( "ITDImport" ) ;
        logger.debug( "Get Job definition" ) ;
        logger.debug( "\t" + def ) ;
    }

    private void testGetJobConfig() throws Exception {
        final JobConfig cfg = jobDAO.getJobConfig( new Integer( 0 ) ) ;
        logger.debug( "Get Job configuration" ) ;
        logger.debug( "\t" + cfg ) ;
    }

    private void testGetAllJobConfig() throws Exception {
        final List<JobConfig> cfgs = jobDAO.getJobConfigs() ;
        logger.debug( "Get All Job configuration" ) ;
        for( final JobConfig cfg : cfgs ) {
            logger.debug( "\t" + cfg ) ;
        }
    }
}
