/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 18, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.log;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.dto.LogMsg.Sev ;
import com.sandy.apps.pluto.shared.event.Event ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.event.IEventSubscriber ;
import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.dialogmgr.IPlutoFramePanel ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoInternalFrame ;

/**
 * This panel is used to show the log messages in the system and supports
 * operations like filtering, sorting and cleaning of log messages.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class LogDisplayPanel extends JPanel
    implements IPlutoFramePanel, ActionListener, UIConstant, IEventSubscriber {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 561684807893431129L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( LogDisplayPanel.class ) ;

    /** The list of log messages. */
    private final List<LogMsg> logMsgList = new ArrayList<LogMsg>() ;

    /** The table model that the log table operates upon. */
    private final LogTableModel tableModel = new LogTableModel( this.logMsgList ) ;

    /** The JTable which will display the log messages. */
    private final JTable logTable = new JTable( this.tableModel ) ;

    /** The number of log messages to store in the memory buffer before rolling.*/
    private final int bufferSize = 500 ;

    /** A string array containing the columns in the log table. */
    public static final String[] COL_NAMES = { "", "Time", "Message" } ;

    /** An enumeration of columns. */
    public static enum ColType { SEV, TIME, MSG } ;

    /** A reference to the dialog manager to whom this panel belongs. */
    @SuppressWarnings("unused")
    private PlutoInternalFrame dlgMgr = null ;

    /** Public constructor. */
    public LogDisplayPanel() {
        super() ;
        setName( "Log window" ) ;
    }

    /** NO OPERATION. */
    public boolean isDirty() { return false ; }

    /** NO OPERATION. */
    public List<String> validateUserInput() throws STException { return null ; }

    /** NO OPERATION. */
    public void destroy() {}

    /** NO OPERATION. */
    public void save() throws STException {}

    /** Returns the attributes of the actions specific to this panel. */
    @Override
    public Object[][] getPanelIcons() {
        return new Object[][] {
            { AC_LOG_DELETE, IMG_LOG_DELETE, IMG_LOG_DELETE_PRESSED, "Clean logs" }
        } ;
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
     * Initializes the log display panel by registering itself with the
     * event bus for MSG type events and sets up the user interface to display
     * the messages.
     */
    @Override
    public void initializeData() throws STException {
        // Set up the table
        this.logTable.setDefaultRenderer( Sev.class,    new LogTableCellRenderer() ) ;
        this.logTable.setDefaultRenderer( Date.class,   new LogTableCellRenderer() ) ;
        this.logTable.setDefaultRenderer( String.class, new LogTableCellRenderer() ) ;
        this.logTable.setAutoCreateRowSorter( true ) ;
        this.logTable.setGridColor( new Color( 243, 243, 243 ) ) ;
        this.logTable.setFont( LOG_FONT ) ;
        this.logTable.getTableHeader().setFont( LOG_FONT_BOLD ) ;

        final TableColumnModel colModel = this.logTable.getColumnModel() ;
        TableColumn col = null ;
        // Set the column attributes for the severity column
        col = colModel.getColumn( 0 ) ;
        col.setMaxWidth( 10 ) ;
        col.setResizable( false ) ;
        // Set the column attributes for the time column
        col = colModel.getColumn( 1 ) ;
        col.setMaxWidth( 50 ) ;
        col.setResizable( false ) ;

        final JScrollPane scrollPane = new JScrollPane( this.logTable ) ;
        scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER ) ;
        scrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ) ;

        // Now attach the table to the panel
        setLayout( new BorderLayout() ) ;
        add( scrollPane, BorderLayout.CENTER ) ;

        // Add this panel as an interested subscriber for log messages.
        EventBus.instance().addSubscriberForEventTypes( this, EventType.MSG ) ;
    }

    /**
     * This method is invoked when any of the buttons specific to this panel
     * are invoked, for example clean logs, export logs etc.
     */
    @Override
    public void actionPerformed( final ActionEvent e ) {
        final String actionCmd = e.getActionCommand() ;
        if( actionCmd.equals( AC_LOG_DELETE ) ) {
            this.logMsgList.clear() ;
            this.tableModel.tableDataChanged() ;
        }
    }

    /**
     * This method is called by the event bus whenever a log event is generated.
     * This operation, updates the log display with the log message.
     */
    @Override
    public void handleEvent( final Event event ) {
        if( event.getEventType() == EventType.MSG ) {
            logger.debug( "Message received" ) ;
            final LogMsg msg = ( LogMsg )event.getValue() ;
            this.logMsgList.add( 0, msg ) ;
            while( this.logMsgList.size() > this.bufferSize ) {
                this.logMsgList.remove( this.logMsgList.size()-1 ) ;
            }
            this.tableModel.tableDataChanged() ;
        }
    }

    /** No operation method. */
    public String getTitle() {
        return null ;
    }
}
