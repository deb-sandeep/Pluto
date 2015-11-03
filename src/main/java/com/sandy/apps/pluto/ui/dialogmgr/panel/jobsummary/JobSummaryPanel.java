/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 17, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.jobsummary;
import java.awt.BorderLayout ;
import java.awt.Color ;
import java.awt.event.ActionEvent ;
import java.awt.event.ActionListener ;
import java.util.List ;
import java.util.Map ;

import javax.swing.Icon ;
import javax.swing.JFrame ;
import javax.swing.JPanel ;
import javax.swing.JScrollPane ;
import javax.swing.JSplitPane ;
import javax.swing.JTable ;
import javax.swing.JTextArea ;
import javax.swing.ListSelectionModel ;
import javax.swing.event.ListSelectionEvent ;
import javax.swing.event.ListSelectionListener ;
import javax.swing.table.TableColumn ;
import javax.swing.table.TableColumnModel ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.StockTracker ;
import com.sandy.apps.pluto.biz.svc.IJobSvc ;
import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.JobAttribute ;
import com.sandy.apps.pluto.shared.dto.JobConfig ;
import com.sandy.apps.pluto.shared.event.Event ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.event.IEventSubscriber ;
import com.sandy.apps.pluto.shared.util.util.StringUtil ;
import com.sandy.apps.pluto.ui.I18N ;
import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.dialogmgr.IPlutoFramePanel ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoFrameType ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoInternalFrame ;

/**
 * This panel shows the status of the currently executing tasks in the system
 * and gives an opportunity to perform certain operations on the tasks, like.
 * <ul>
 *  <li>Open up a task detail for editing</li>
 *  <li>Stop a task</li>
 *  <li>Start a task</li>
 *  <li>Restart a task</li>
 *  <li>Delete a task</li>
 *  <li>Create a new task</li>
 * </ul>
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class JobSummaryPanel extends JPanel
    implements IPlutoFramePanel, ActionListener, UIConstant, ListSelectionListener,
               IEventSubscriber {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( JobSummaryPanel.class ) ;

    /** The table which will display the task summary. */
    private final JTable table = new JTable() ;

    /** The model used for the job summary table. */
    private final JobSummaryTableModel tableModel = new JobSummaryTableModel() ;

    /** The text area which will show the description of the currently selected task. */
    private final JTextArea textArea = new JTextArea( 6, 20 ) ;

    /** A reference to the dialog manager to whom this panel belongs. */
    private PlutoInternalFrame dlgMgr = null ;

    /** Public constructor. */
    public JobSummaryPanel() {
        super() ;
        setName( I18N.LBL_TASK_CONFIG_DLG_NAME ) ;
    }

    /** This panel does not supply an edit option and hence this is a no op. */
    @Override
    public boolean isDirty() {
        // NO OPERATION
        return false ;
    }

    /** This panel does not supply an edit option and hence this is a no op. */
    @Override
    public void save() throws STException {
        // NO OPERATION
    }

    /** This panel does not supply an edit option and hence this is a no op. */
    @Override
    public List<String> validateUserInput() throws STException {
        // NO OPERATION
        return null ;
    }

    /** There is nothing to initialize in this panel. Data is fetched at runtime. */
    @Override
    public void initializeData() throws STException {
        setLayout( new BorderLayout() ) ;

        final JSplitPane splitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT ) ;
        splitPane.setDividerSize( 3 ) ;
        splitPane.setDividerLocation( 160 ) ;
        splitPane.setOneTouchExpandable( true ) ;

        // Set up the text area
        this.textArea.setWrapStyleWord( true ) ;
        this.textArea.setEditable( false ) ;
        this.textArea.setFont( READ_TXT_AREA_FONT ) ;
        this.textArea.setLineWrap( true ) ;

        final JScrollPane textSP = new JScrollPane( this.textArea ) ;
        textSP.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER ) ;

        // Set up the table.
        final JScrollPane tableSP = new JScrollPane( this.table ) ;
        tableSP.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER ) ;

        splitPane.add( tableSP ) ;
        splitPane.add( textSP ) ;
        add( splitPane, BorderLayout.CENTER ) ;

        // Add the table model, table column model etc for the table.
        this.table.setModel( this.tableModel ) ;
        this.table.getSelectionModel().addListSelectionListener( this ) ;
        this.table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION ) ;
        this.table.setDefaultRenderer( Icon.class,   new JobSummaryTableCellRenderer() ) ;
        this.table.setDefaultRenderer( String.class, new JobSummaryTableCellRenderer() ) ;
        this.table.setAutoCreateRowSorter( true ) ;
        this.table.setGridColor( new Color( 243, 243, 243 ) ) ;
        this.table.setFont( LOG_FONT ) ;
        this.table.getTableHeader().setFont( LOG_FONT_BOLD ) ;
        this.table.setRowHeight( 18 ) ;

        final TableColumnModel colModel = this.table.getColumnModel() ;
        TableColumn col = null ;
        // Set the column attributes for the Job Type column
        col = colModel.getColumn( 0 ) ;
        col.setMaxWidth( 18 ) ;
        col.setResizable( false ) ;
        // Set the column attributes for the Startup Type column
        col = colModel.getColumn( 1 ) ;
        col.setMaxWidth( 18 ) ;
        col.setResizable( false ) ;
        // Set the column attributes for the Current Status column
        col = colModel.getColumn( 2 ) ;
        col.setMaxWidth( 18 ) ;
        col.setResizable( false ) ;

        // Add the table model as a subscriber interested in listening to JOB_*
        // events.
        EventBus.instance().addSubscriberForEventPatterns( this.tableModel, "JOB_.*" ) ;

        // Add this panel as a subscriber for job start and stop events so
        // that we can manage the state of the start, stop and restart buttons
        EventBus.instance().addSubscriberForEventTypes( this,
                                EventType.JOB_STARTED, EventType.JOB_STOPPED ) ;
    }

    /**
     * This method is called by the dialog manager on the instance of the
     * dialog panel to register itself with the panel. The reference passed
     * as parameter can be saved by the dialog panel and used to invoke
     * operations provided by the dialog manager, for example requests for
     * enabling or disabling a particular button based on its action command.
     */
    public void setDialogManager( final PlutoInternalFrame dlgMgr ) {
        this.dlgMgr = dlgMgr ;
    }

    /**
     * This method is invoked on the child panels when the dialog is being
     * disposed as a result of a cancel operation chosen by the user. Subclasses
     * can implement this function to perform cleanup logic like de-registering
     * from the event bus etc.
     */
    public void destroy() {
        // Remove the model as an event bus listener
        EventBus.instance().removeSubscriber( this.tableModel, (EventType[])null ) ;

        // Remove this panel as an event bus listener
        EventBus.instance().removeSubscriber( this, (EventType[])null ) ;
    }

    /**
     * Returns the icons that need to be displayed in the wizard toolbar when
     * this panel is selected.
     * <ul>
     *  <li>Element 0 [String][M] - Action command for the button</li>
     *  <li>Element 1 [Image] [M] - Button image</li>
     *  <li>Element 2 [Image] [O] - Pressed image for button</li>
     *  <li>Element 3 [Image] [O] - Roll over image for button[</li>
     * </ul>
     *
     * @return A two dimensional array of Objects, with each row having two
     *         elements.
     */
    @Override
    public Object[][] getPanelIcons() {
        return new Object[][] {
            { AC_DELETE,   IMG_DELETE,   IMG_DELETE_PRESSED,    "Delete Task" },
            { AC_EXEC_NOW, IMG_EXEC_NOW, IMG_EXEC_NOW_PRESSED,  "Execute Now" },
            { AC_EDIT,     IMG_EDIT,     IMG_EDIT_PRESSED,      "Edit Task" },
            { AC_RESTART,  IMG_RESTART,  IMG_RESTART_PRESSED,   "Restart Task" },
            { AC_STOP,     IMG_STOP,     IMG_STOP_PRESSED,      "Stop Task" },
            { AC_START,    IMG_START,    IMG_START_PRESSED,     "Start Task" },
        } ;
    }

    /**
     * This operation is called on this panel when any of the panel specific
     * buttons is clicked by the user. In the case of this panel, the panel
     * specific buttons are delete, edit, restart, stop and start - all of which
     * act on the currently selected row. If a row is not selected, no
     * action is performed.
     */
    @Override
    public void actionPerformed( final ActionEvent e ) {

        JobConfig             config= null ;
        Integer               jobId = null ;
        JobSummaryTableModel model = null ;

        final String actionCmd = e.getActionCommand() ;
        final IJobSvc jobSvc   = ServiceMgr.getAsyncJobSvc() ;

        // First get the select job configuration. If no row job configuration
        // is selected, we need to ignore this invocation
        final int selIndex = this.table.getSelectedRow() ;
        if( selIndex != -1 ) {
            model  = ( JobSummaryTableModel )this.table.getModel() ;
            config = model.getJobConfigAtIndex( selIndex ) ;
            jobId  = config.getJobId() ;

            if( actionCmd.equals( AC_START ) ) {
                jobSvc.startJob( jobId ) ;
            }
            else if( actionCmd.equals( AC_STOP ) ) {
                jobSvc.stopJob( jobId ) ;
            }
            else if( actionCmd.equals( AC_RESTART ) ) {
                jobSvc.restartJob( jobId ) ;
            }
            else if( actionCmd.equals( AC_DELETE ) ) {

            }
            else if( actionCmd.equals( AC_EDIT ) ) {
                // If the user wants to edit the currently selected job, we
                // show the job edit dialog to the user.
                showJobEditor( config ) ;
            }
            else if( actionCmd.equals( AC_EXEC_NOW ) ) {
                jobSvc.executeNow( jobId ) ;
            }
        }
    }

    /** Displays the job configuration editor. */
    private void showJobEditor( final JobConfig jobCfg ) {

        try {
            final JobCommonAttrEditorPanel panel = new JobCommonAttrEditorPanel() ;
            final PlutoInternalFrame dialog = new PlutoInternalFrame( "Job Editor",
                                                JFrame.DISPOSE_ON_CLOSE,
                                                PlutoFrameType.CHART_FRAME,
                                                panel ) ;

            dialog.setSize( 400, 475 ) ;
            dialog.setResizable( false ) ;
            StockTracker.MAIN_FRAME.addInternalFrame( dialog, true ) ;
        }
        catch ( final STException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * This method is invoked by Swing framework when the table row selection
     * changes. This is a trigger for us to change the long description of the
     * selected job in the description text box.
     */
    @Override
    public void valueChanged( final ListSelectionEvent evt ) {

        final int newIndex = this.table.getSelectedRow() ;
        final JobSummaryTableModel model = ( JobSummaryTableModel )this.table.getModel() ;
        final JobConfig config = model.getJobConfigAtIndex( newIndex ) ;

        // Set the new detailed description for the selected task
        this.textArea.setText( getJobDescription( config ) ) ;

        // Based on the state of the task, enable or disable buttons
        final IJobSvc jobSvc = ServiceMgr.getJobSvc() ;
        final boolean running = jobSvc.isRunning( config.getJobId() ) ;
        enableStartStopRestartButtons( running ) ;
    }

    /**
     * Enables or disables the start, stop and restart buttons depending upon
     * the state of the job.
     */
    private void enableStartStopRestartButtons( final boolean jobStarted ) {
        this.dlgMgr.enablePanelBtn( this, AC_STOP,    jobStarted ) ;
        this.dlgMgr.enablePanelBtn( this, AC_RESTART, jobStarted ) ;
        this.dlgMgr.enablePanelBtn( this, AC_START,  !jobStarted ) ;
    }

    /**
     * Returns the long description of the given job configuration. The long
     * description comprises of the job instance description, the cron entry,
     * the upper and lower time bands and the map of attributes.
     */
    private String getJobDescription( final JobConfig config ) {

        String descr = config.getDescription() ;
        descr += "\n\n" ;
        descr += "Cron = " + config.getCron() + "\n" ;
        descr += "LTB  = " + config.getLowerTimeBand() + "\n" ;
        descr += "UTB  = " + config.getUpperTimeBand() + "\n" ;
        descr += "\n" ;

        final Map<String, List<JobAttribute>> attribs = config.getAttributes() ;
        for( final String key : attribs.keySet() ) {
            final List<JobAttribute> attrList = attribs.get( key ) ;
            descr += StringUtil.rightPad( key, 25 ) + ":" ;
            for( int i=0; i<attrList.size(); i++ ) {
                descr += attrList.get( i ).getValue() ;
                if( i < attrList.size() - 1 ) {
                    descr += ", " ;
                }
            }
            descr += "\n" ;
        }
        return descr ;
    }

    /**
     * This method is invoked when JOB_STARTED and JOB_STOPPED events are
     * published on the event bus. This method turns on and off the corresponding
     * buttons for the panel based on whether the job is started or stopped.
     *
     * @param event The JOB_STARTED or JOB_STOPPED event. The value of the event
     *        is a {@link JobConfig} instance.
     */
    @Override
    public void handleEvent( final Event event ) {

        boolean jobStarted = false ;
        switch( event.getEventType() ) {
            case JOB_STARTED:
                jobStarted = true ;
                break ;
            case JOB_STOPPED:
                jobStarted = false ;
                break ;
        }
        enableStartStopRestartButtons( jobStarted ) ;
    }

    /** No operation method. */
    public String getTitle() {
        return null ;
    }
}
