package com.sandy.stocktracker.test.poc ;
/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 5, 2008
 */

import java.util.Date;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SysoutJob implements Job {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( SysoutJob.class ) ;

    public SysoutJob() {
    }

    @Override
    public void execute( final JobExecutionContext context )
            throws JobExecutionException {

        logger.error( "Firing job " + context.getMergedJobDataMap().get( "key" ) + " " + new Date() ) ;
    }
}
