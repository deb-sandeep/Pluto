/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 13, 2008
 */

package com.sandy.apps.pluto.ui.statusbar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.STException ;

/**
 * This status bar component shows a digital clock on the status bar, which
 * shows time at second interval. Internally, this component spawns a thread
 * which sleeps for 1000 milliseconds and changes the text of the status
 * bar accordingly.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ClockSBComponent extends AbstractSBComponent {

    /** Serial version UID. */
    private static final long serialVersionUID = -7087385729013001309L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ClockSBComponent.class ) ;

    /** The the expanded time format. */
    private static final SimpleDateFormat DATE_TIME_FMT = new SimpleDateFormat( "dd-MMM HH:mm:ss" ) ;

    // Variables declaration - do not modify
    private JLabel timeLabel = null ;
    // End of variables declaration

    /** Public constructor. */
    public ClockSBComponent() {
        super() ;
    }

    /** Initializes this component by setting up the UI, event subscription etc.*/
    @Override
    public void initialize() throws STException {
        // Set up the user interface. Note that UI has been created in NetBeans
        setUpUI() ;

        // Now set up the thread which will update the time string at a
        // regular interval.
        final Thread thread = new Thread() {
            public void run() {
                // Loop for ever
                while( true ) {
                    try {
                        Thread.sleep( 1000 ) ;
                        ClockSBComponent.this.timeLabel.setText( DATE_TIME_FMT.format( new Date() ) ) ;
                    }
                    catch ( final Throwable e ) {
                        // Harden the logic, so that this thread does not fail
                        logger.debug( "Time update failed", e ) ;
                    }
                }
            }
        } ;
        thread.start() ;
    }

    /**
     * A private helper method to set up the UI. Note that the code for this
     * method has been generated in NetBeans and should not be changed.
     */
    private void setUpUI() {

        super.setBackground( Color.black ) ;
        super.setLayout( new BorderLayout() ) ;

        this.timeLabel = new JLabel( DATE_TIME_FMT.format( new Date() ) );
        this.timeLabel.setBackground( Color.BLACK ) ;
        this.timeLabel.setForeground( Color.GREEN ) ;
        this.timeLabel.setFont( new Font( "Tahoma", Font.PLAIN, 12 ) ) ;
        this.timeLabel.setDoubleBuffered( true ) ;

        super.add( this.timeLabel, BorderLayout.CENTER ) ;
    }
}
