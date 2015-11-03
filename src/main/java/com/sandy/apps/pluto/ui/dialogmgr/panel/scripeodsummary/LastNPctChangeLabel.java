/**
 * 
 * 
 * 
 *
 * Creation Date: Dec 22, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.scripeodsummary;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;

import com.sandy.apps.pluto.ui.UIHelper ;

/** A specialized label to render the last N percentage change data. */
public class LastNPctChangeLabel extends JLabel {

    /** Default serial version UID. This class will never be serialized. */
    private static final long serialVersionUID = 1L ;

    /** The grid color for separating the past EOD pct change values. */
    private static final Color EOD_PCT_CHG_GRID_COLOR = new Color( 235, 235, 235 ) ;

    private final Double[] changeData ;

    public LastNPctChangeLabel( final Double[] data ) {
        this.changeData = data ;
    }

    /**
     * Renders the last N percent changes in a visual format on the given
     * label. The number of days is defined in a constant in the
     * {@link PortfolioSummaryPanel} class.
     *
     * @param g The graphics context for this label
     * @param value The double array containing data for the last N days.
     */
    public void paint( final Graphics g ) {

        final int numDays   = ScripEODSummaryPanel.LAST_N_PCT_CHANGE_DAYS ;
        final int height    = getSize().height ;
        final int width     = getSize().width ;
        final int pixPerDay = Math.round( (float)width/numDays ) ;

        final Graphics2D g2d = ( Graphics2D )g ;
        g2d.setColor( EOD_PCT_CHG_GRID_COLOR ) ;

        // Draw the grid which separates the days.
        for( int i=0; i<numDays; i++ ) {
            final int x = pixPerDay*(i) ;
            g2d.drawLine( x, 0, x, height ) ;
        }

        // Note this interesting rendering algorithm - remember that the
        // list of percentage change values contain the values in descending
        // order of their dates - hence the first value will be the right
        // most cell rendered for the label.
        if( this.changeData != null && this.changeData.length > 0 ) {
            for( int i=0; i<this.changeData.length; i++ ) {
                final Double pctChange = this.changeData[i] ;
                final Color  color     =  UIHelper.getProfitLossHighlight( pctChange ) ;
                final int    x = pixPerDay*( numDays-i-1 )+1 ;

                g2d.setColor( color ) ;
                g.fillRect( x, 0, pixPerDay-1, height ) ;
            }
        }
    }
}

