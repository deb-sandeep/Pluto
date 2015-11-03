/**
 * Creation Date: Aug 7, 2008
 */

package com.sandy.apps.pluto.biz.svc.job;
import org.apache.log4j.Logger ;
import org.quartz.JobExecutionException ;

import com.sandy.apps.pluto.biz.svc.INetworkSvc ;
import com.sandy.apps.pluto.shared.dto.JobConfig ;
import com.sandy.apps.pluto.shared.util.util.BizObjectFactory ;

/**
 * This Job should be scheduled at regular intervals to check for network
 * connectivity status. In case the network connection heuristics have been
 * polluted beyond acceptable limits, the network would be transitioned to
 * an offline mode till the time a force network check is performed. This
 * job helps to perform a forceful network check to recover from auto offline
 * scenarios.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class NetworkCheckJob extends AbstractBaseJob {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( NetworkCheckJob.class ) ;

    /** Public constructor. */
    public NetworkCheckJob() {
        super() ;
    }

    /**
     * Force checks the availability of the network.
     */
    @Override
    public void executeJob( final JobConfig config )
            throws JobExecutionException {

        final BizObjectFactory of = ( BizObjectFactory )BizObjectFactory.getInstance() ;
        final INetworkSvc netSvc = ( INetworkSvc )of.getBean( "NetworkSvc" ) ;

        netSvc.checkNetworkStatus( true ) ;
    }
}
