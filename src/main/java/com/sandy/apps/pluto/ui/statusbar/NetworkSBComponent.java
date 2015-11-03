/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 13, 2008
 */

package com.sandy.apps.pluto.ui.statusbar;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.event.Event ;
import com.sandy.apps.pluto.shared.event.EventBus ;
import com.sandy.apps.pluto.shared.event.IEventSubscriber ;
import com.sandy.apps.pluto.ui.UIHelper ;

/**
 * This status bar component shows the real time network statistics. The
 * statistics displayed by this component are namely:
 * <ul>
 *  <li>The network connection heuristic pattern</li>
 *  <li>The network availability status</li>
 *  <li>The number of KB downloaded</li>
 * </ul>
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class NetworkSBComponent extends AbstractSBComponent
    implements IEventSubscriber {

    /** Serial version UID. */
    private static final long serialVersionUID = -7087385729013001309L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( NetworkSBComponent.class ) ;

    /** The format in which the show the number. */
    private static final DecimalFormat NUM_FMT = new DecimalFormat( "#.##" ) ;

    /** An internal array storing the number of bytes downloaded. */
    protected final List<Long> networkStatusList = new ArrayList<Long>() ;

    /** The number of network status to store before recycling. */
    private final int NUM_STATUS = 30 ;

    /** The number of bytes downloaded till now. */
    private long numBytesDownloaded = 0 ;

    // Variables declaration - do not modify
    private JLabel connectivityStatusImg = null ;
    private JLabel downloadSizeLabel     = null ;
    private JLabel heuristicDisplayLabel = null ;
    private JLabel separator         = null ;
    // End of variables declaration

    /**
     * A panel derivative which paints the heuristics on screen.
     *
     * @author Sandeep Deb [deb.sandeep@gmail.com]
     */
    private class HeuristicPanel extends JLabel {

        /** Serial version UID. */
        private static final long serialVersionUID = -7087385729013001309L ;

        /** Constructor. */
        public HeuristicPanel() {
            super() ;
            setBackground( Color.black ) ;
        }

        /**
         * Renders the heuristics by rendering the network status as a bar
         * chart, with the X axis as the index and the Y axis as the normalized
         * download size
         */
        public synchronized void paint( final Graphics g ) {

            final List<Long> dataPoints = NetworkSBComponent.this.networkStatusList ;
            final Color oldColor  = g.getColor() ;
            final int maxHeight   = super.getHeight();
            final int maxWidth    = super.getWidth();
            final float cubeWidth = (float)maxWidth/NetworkSBComponent.this.NUM_STATUS ;

            long maxVal = 0 ;
            // Do not use an iterator since we might get a concurrent
            // modification exception. This is because the network download
            // data points is updated by a separate thread of execution.
            for( int i=0; i<dataPoints.size(); i++ ) {
                final Long element = dataPoints.get( i ) ;
                if( maxVal < element ) {
                    maxVal = element ;
                }
            }

            g.setColor( Color.BLACK ) ;
            g.fillRect( 0, 0, maxWidth, maxHeight ) ;

            g.setColor( Color.GRAY ) ;
            g.drawRect( 0, 0, maxWidth-1, maxHeight-1 ) ;

            for( int i=0; i<dataPoints.size(); i++ ) {
                final long val = dataPoints.get( i ).longValue() ;
                int height = (int)(( ((float)val) / maxVal ) * (maxHeight-5) ) ;
                if( val == 0 ) {
                    continue ;
                }
                else if( val < 0 ) {
                    height = maxHeight-5 ;
                    g.setColor( Color.PINK ) ;
                }
                else {
                    g.setColor( Color.GREEN ) ;
                }

                g.fillRect( (int)(i*cubeWidth)+1, maxHeight-height-2, (int)cubeWidth, height ) ;
            }

            g.setColor( oldColor ) ;
        }
    }

    /** Public constructor. */
    public NetworkSBComponent() {
        super() ;
    }

    /** Initializes this component by setting up the UI, event subscription etc.*/
    @Override
    public void initialize() throws STException {
        // Set up the user interface. Note that UI has been created in NetBeans
        setUpUI() ;

        // Set the initial state of the components.
        this.downloadSizeLabel.setText( "0.0 KB" ) ;
        setConnectivityStatus( ServiceMgr.getNetworkSvc().isOnline() ) ;

        // Register with the event bus for receiving network related events
        EventBus.instance().addSubscriberForEventPatterns( this, "NETWORK.*" ) ;
    }

    /**
     * A private helper method to set up the UI. Note that the code for this
     * method has been generated in NetBeans and should not be changed.
     */
    private void setUpUI() {

        this.heuristicDisplayLabel = new HeuristicPanel() ;
        this.connectivityStatusImg = new JLabel();
        this.downloadSizeLabel     = new JLabel();
        this.separator             = new JLabel();

        super.setBackground( Color.black ) ;
        this.heuristicDisplayLabel.setBackground( Color.black ) ;
        this.connectivityStatusImg.setBackground( Color.black ) ;
        this.downloadSizeLabel.setBackground( Color.black ) ;
        this.separator.setBackground( Color.black ) ;

        this.downloadSizeLabel.setOpaque( true ) ;
        this.connectivityStatusImg.setOpaque( true ) ;
        this.separator.setOpaque( true ) ;

        setMaximumSize(new Dimension(32767, 20));
        setMinimumSize(new Dimension(0, 20));

        this.heuristicDisplayLabel.setToolTipText("Network heuristics");
        this.heuristicDisplayLabel.setMaximumSize(new Dimension(30, 14));
        this.heuristicDisplayLabel.setMinimumSize(new Dimension(30, 14));

        this.connectivityStatusImg.setIcon( new ImageIcon( UIHelper.IMG_OFFLINE ));
        this.connectivityStatusImg.setToolTipText("Connectivity Status");

        this.downloadSizeLabel.setFont(new Font("Tahoma", 0, 10));
        this.downloadSizeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        this.downloadSizeLabel.setText("0.0 B");
        this.downloadSizeLabel.setToolTipText("KB downloaded");
        this.downloadSizeLabel.setForeground( Color.LIGHT_GRAY ) ;

        //this.separator.setOrientation(SwingConstants.VERTICAL);

        final GroupLayout layout = new GroupLayout( this ) ;
        setLayout( layout ) ;

        layout.setHorizontalGroup( layout.createParallelGroup(
                GroupLayout.Alignment.LEADING ).addGroup(
                GroupLayout.Alignment.TRAILING,
                layout.createSequentialGroup().addContainerGap().addComponent(
                        this.separator, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
                        .addPreferredGap(
                                LayoutStyle.ComponentPlacement.RELATED )
                        .addComponent( this.downloadSizeLabel,
                                GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE )
                        .addPreferredGap(
                                LayoutStyle.ComponentPlacement.RELATED )
                        .addComponent( this.connectivityStatusImg )
                        .addPreferredGap(
                                LayoutStyle.ComponentPlacement.RELATED )
                        .addComponent( this.heuristicDisplayLabel,
                                GroupLayout.PREFERRED_SIZE, 83,
                                GroupLayout.PREFERRED_SIZE ) ) ) ;

        layout.setVerticalGroup( layout.createParallelGroup(
                GroupLayout.Alignment.LEADING ).addComponent(
                this.heuristicDisplayLabel, GroupLayout.DEFAULT_SIZE, 23,
                Short.MAX_VALUE ).addComponent( this.connectivityStatusImg,
                GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE ).addComponent(
                this.separator, GroupLayout.Alignment.TRAILING,
                GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE ).addComponent(
                this.downloadSizeLabel, GroupLayout.DEFAULT_SIZE, 23,
                Short.MAX_VALUE ) ) ;
    }


    /**
     * Changes the connectivity light to green or red depending upon the online
     * status.
     *
     * @param online true if the system is online, false otherwise.
     */
    private void setConnectivityStatus( final boolean online ) {
        final Image newImg = ( online ) ? UIHelper.IMG_ONLINE : UIHelper.IMG_OFFLINE ;
        this.connectivityStatusImg.setIcon( new ImageIcon( newImg ) ) ;
    }

    /**
     * Receives network related events and changes the component display
     * accordingly.
     */
    @Override
    public void handleEvent( final Event event ) {

        switch( event.getEventType() ) {
            case NETWORK_STATUS_CHANGE:
                setConnectivityStatus( ((Boolean)event.getValue()).booleanValue() ) ;
                break ;

            case NETWORK_COMM_STATUS:
                final Boolean status = ( Boolean )event.getValue() ;
                if( status.booleanValue() == false ) {
                    updateHeuristics( new Long( -1 ) ) ;
                }
                break ;

            case NETWORK_DATA_DOWNLOADED:
                final Long numBytes = ( Long )event.getValue() ;
                this.numBytesDownloaded += numBytes ;

                final float numKB = (float)this.numBytesDownloaded/1024 ;
                final float numMB = numKB / 1024 ;

                String text = null ;
                if( numMB > 1 ) {
                    text = NUM_FMT.format( numMB ) + " MB" ;
                }
                else if( numKB > 1 ) {
                    text = NUM_FMT.format( numKB ) + " KB" ;
                }
                else {
                    text = this.numBytesDownloaded + " B" ;
                }

                this.downloadSizeLabel.setText( text ) ;
                updateHeuristics( numBytes ) ;
                break ;
        }

    }

    /**
     * Updates the network heuristics and paints the component as soon as possible.
     */
    private void updateHeuristics( final Long numBytes ) {

        // Synchronize the network status list since it can be updated and
        // painted at the same time.
        synchronized ( this.networkStatusList ) {
            this.networkStatusList.add( numBytes ) ;
            // This will ensure that we always store no more than NUM_STATUS elements
            while( this.networkStatusList.size() > this.NUM_STATUS ) {
                this.networkStatusList.remove( 0 ) ;
            }
        }
        this.heuristicDisplayLabel.repaint() ;
    }
}
