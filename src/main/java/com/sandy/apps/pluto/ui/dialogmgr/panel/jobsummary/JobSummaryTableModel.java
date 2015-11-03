/**
 * 
 * 
 * 
 *
 * Creation Date: Sep 8, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.jobsummary;
import java.util.List;

import javax.swing.Icon;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.biz.svc.IJobSvc ;
import com.sandy.apps.pluto.biz.svc.IJobSvc.JobState ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.JobConfig ;
import com.sandy.apps.pluto.shared.event.Event ;
import com.sandy.apps.pluto.shared.event.IEventSubscriber ;
import com.sandy.apps.pluto.ui.UIHelper ;

/**
 * This class provides the table model required for displaying the job
 * summary information.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class JobSummaryTableModel extends AbstractTableModel
    implements IEventSubscriber {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 561684807893431129L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( JobSummaryTableModel.class ) ;

    /** A string array containing the columns in the log table. */
    public static final String[] COL_NAMES = {
        "",         // Job Type
        "",         // Startup Type
        "",         // Current status (started/stopped)
        "Name"      // Name of the job
    } ;

    // The column indices as constants
    private static final int COL_JOB_TYPE       = 0 ;
    private static final int COL_STARTUP_TYPE   = 1 ;
    private static final int COL_CURR_STATE     = 2 ;
    private static final int COL_NAME           = 3 ;

    /**
     * A local cache of the job configurations. This cache will be refreshed
     * once this model receives notification of the change of state or attribute
     * of any job instance.
     */
    private List<JobConfig> jobConfigs = null ;

    /** A flag indicating that the local cache needs refreshing. */
    private boolean dirtyCache = true ;

    /** Public no argument constructor. */
    public JobSummaryTableModel() {
    }

    /**
     * A helper method to refresh the local list of job configurations based on
     * the dirty cache flag. The cache is considered dirty on startup and
     * on every job related event notification.
     */
    private List<JobConfig> getJobConfigs() {
        if( this.dirtyCache ) {
            final IJobSvc jobSvc = ServiceMgr.getJobSvc() ;
            this.jobConfigs = jobSvc.getJobConfigs() ;
            this.dirtyCache = false ;
        }
        return this.jobConfigs ;
    }

    /** Returns the number of columns supported by the task summary panel. */
    @Override
    public int getColumnCount() {
        return COL_NAMES.length ;
    }

    /**
     * Returns the number of rows for the task summary table. The number of
     * rows is equal to the number of Job instances registered with the
     * application.
     *
     * @return The number of job instances registered with the application.
     */
    @Override
    public int getRowCount() {
        return getJobConfigs().size() ;
    }

    /** Returns the name of the column at the specified column index. */
    @Override
    public String getColumnName( final int column ) {
        return COL_NAMES[column] ;
    }

    /** None of this table cells are editable. */
    @Override
    public boolean isCellEditable( final int rowIndex, final int columnIndex ) {
        return false ;
    }

    /**
     * Returns the value of the column at the specified row index.
     * <p>
     * The following object types are returned for values at different columns
     * <ul>
     *  <li>Column 0 - Icon</li>
     *  <li>Column 1 - Icon</li>
     *  <li>Column 2 - Icon</li>
     *  <li>Column 3 - String</li>
     * </ul>
     */
    @Override
    public Object getValueAt( final int rowIndex, final int columnIndex ) {

        Object cellValue = null ;
        final List<JobConfig> jobCfgs = getJobConfigs() ;
        final JobConfig       jobCfg  = jobCfgs.get( rowIndex ) ;

        if( columnIndex == COL_JOB_TYPE ) {
            final String iconName = jobCfg.getJobDef().getIcon() ;
            cellValue = UIHelper.getIcon( iconName ) ;
        }
        else if( columnIndex == COL_STARTUP_TYPE ) {
            final String startup = jobCfg.getStartupType().trim() ;
            if( startup.equals( "AUTO" ) ) {
                cellValue = UIHelper.getIcon( "auto.png" ) ;
            }
            else {
                cellValue = UIHelper.getIcon( "manual.png" ) ;
            }
        }
        else if( columnIndex == COL_CURR_STATE ) {
            final IJobSvc jobSvc = ServiceMgr.getJobSvc() ;
            final JobState state = jobSvc.getJobState( jobCfg.getJobId() ) ;

            // Need to change this to return state of a particular job
            if( state == JobState.STARTED ) {
                cellValue = UIHelper.getIcon( "running.png" ) ;
            }
            else if( state == JobState.STARTING ) {
                cellValue = UIHelper.getIcon( "starting.png" ) ;
            }
            else if( state == JobState.STOPPING ) {
                cellValue = UIHelper.getIcon( "stopping.png" ) ;
            }
            else if( state == JobState.STOPPED ) {
                cellValue = UIHelper.getIcon( "stopped.png" ) ;
            }
            else if( state == JobState.EXECUTING ) {
                cellValue = UIHelper.getIcon( "executing.png" ) ;
            }
            else {
                cellValue = UIHelper.getIcon( "undefined.png" ) ;
            }
        }
        else if( columnIndex == COL_NAME ) {
            cellValue = jobCfg.getName() ;
        }

        return cellValue ;
    }

    /** Returns the class of the column at the specified column index. */
    @Override
    public Class<?> getColumnClass( final int columnIndex ) {
        Class<?> cls = null ;
        switch( columnIndex ) {
            case COL_JOB_TYPE:
            case COL_STARTUP_TYPE:
            case COL_CURR_STATE:
                cls = Icon.class ;
                break ;
            case COL_NAME:
                cls = String.class ;
                break ;
        }
        return cls ;
    }

    /**
     * Returns the job configuration at the specified table row index. This
     * method can be used to fetch the job configuration and display attributes
     * which are not a part of the normal table columns.
     *
     * @param rowIndex The row index
     *
     * @return An instance of {@link JobConfig}, representing the data shown
     *         in the table at the specified row index.
     */
    public JobConfig getJobConfigAtIndex( final int rowIndex ) {
        final List<JobConfig> jobCfgs = getJobConfigs() ;
        return jobCfgs.get( rowIndex ) ;
    }

    /**
     * This method is invoked when any of the JOB_* events is published on the
     * event bus. Since this class mirrors the service status at runtime, this
     * method fires a table changed event for the corresponding job row.
     *
     * @param event The JOB_* event. The value of the event is a {@link JobConfig}
     *        instance.
     */
    @Override
    public void handleEvent( final Event event ) {
        final JobConfig config = ( JobConfig )event.getValue() ;

        // Establish which row in the table corresponds to the job config
        // for which this event was received.
        int rowIndex = 0 ;
        final List<JobConfig> cfgs = getJobConfigs() ;
        for( int rowId=0; rowId<cfgs.size(); rowId++ ) {
            if( cfgs.get( rowId ).getJobId().equals( config.getJobId() ) ) {
                rowIndex = rowId ;
                break ;
            }
        }

        int colIndex = 0 ;
        switch( event.getEventType() ) {
            case JOB_STARTING:
            case JOB_STARTED:
            case JOB_STOPPING:
            case JOB_STOPPED:
            case JOB_EXECUTING:
                colIndex = COL_CURR_STATE ;
                fireTableCellUpdated( rowIndex, colIndex ) ;
                break ;
        }
    }
}
