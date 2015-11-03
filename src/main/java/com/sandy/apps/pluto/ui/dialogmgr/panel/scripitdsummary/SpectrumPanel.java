/**
 * 
 * 
 * 
 *
 * Creation Date: Nov 24, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.scripitdsummary;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Arrays;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.ui.UIHelper ;

/**
 * This panel is a part of Scrip ITD summary panel and shows the profile/loss
 * spectrum of the market, in shades of green to red.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class SpectrumPanel extends JPanel {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( SpectrumPanel.class ) ;

    /** A reference to the ITD value cache. */
    private final ScripITDValueCache itdCache = ScripITDValueCache.getInstance() ;

    /** Public no argument constructor. */
    public SpectrumPanel() {
        super() ;
        super.setDoubleBuffered( true ) ;
        super.setBackground( Color.RED ) ;
        super.setPreferredSize( new Dimension( 1, 7 ) ) ;
    }

    /** Renders the spectrum panel with the data spread of the intra day values. */
    public void paint( final Graphics g ) {

        final Dimension size = super.getSize() ;
        final int numRows    = this.itdCache.getRowCount() ;

        if( numRows > 0 ) {

            final double barWidth = ((double)size.width)/numRows ;
            final double[] pctVals = new double[numRows] ;

            for( int i=0; i<numRows; i++ ) {
                try {
                    pctVals[i] = this.itdCache.getScripITDForRow( i ).getPctChange() ;
                }
                catch ( final IndexOutOfBoundsException e ) {
                    // Catch and ignore any out of bound exceptions. This occurs
                    // in rare situations since the value in the ITD cache is
                    // populated by multiple threads, ScripITD, BulkScripImport
                    // etc and hence there might be situations where the
                    // values in the cache might be different from the rows in
                    // the table momentarily. This is a momentary condition
                    // during the day. In case of exception, set the percentage
                    // value for the current index to 0.
                    pctVals[i] = 0 ;
                }
            }

            Arrays.sort( pctVals ) ;

            final int closestMarketIndex = findZeroClosestIndex( numRows, pctVals ) ;

            for( int i=0; i<numRows; i++ ) {
                final int xPixel = (int)Math.floor( i*barWidth ) ;

                g.setColor( UIHelper.getProfitLossHighlight( pctVals[i] ) ) ;
                g.fillRect( xPixel, 0, (int)Math.ceil(barWidth), size.height ) ;
                if( i == closestMarketIndex ) {
                    g.setColor( Color.BLUE ) ;
                    g.drawLine( xPixel,   0, xPixel,   size.height ) ;
                    g.drawLine( xPixel+1, 0, xPixel+1, size.height ) ;
                }
            }

            // Paint the middle of the spectrum with the zero marker such that
            // the current zero position can be easily identified with relation
            // to the middle of the spectrum.
            g.setColor( Color.GRAY ) ;
            g.drawLine( size.width/2, 0, size.width/2, size.height ) ;
        }
    }

    /**
     * Returns the value of the index in the array, whose absolute value is
     * closest to zero.
     */
    private int findZeroClosestIndex( final int numRows, final double[] pctVals ) {

        int closestMarketIndex = 0 ;
        double lastClosestDist = -1 ;
        double distance        = -1 ;

        for( int i=0; i<numRows; i++ ) {
            // Find the index whose value is the closest to zero. We plan
            // to mark it in the spectrum.
            if( lastClosestDist == -1 ) {
                lastClosestDist = Math.abs( pctVals[i] ) ;
            }
            else {
                distance = Math.abs( pctVals[i] ) ;
                if( distance < lastClosestDist ) {
                    closestMarketIndex = i ;
                    lastClosestDist = distance ;
                }
            }
        }
        return closestMarketIndex ;
    }
}
