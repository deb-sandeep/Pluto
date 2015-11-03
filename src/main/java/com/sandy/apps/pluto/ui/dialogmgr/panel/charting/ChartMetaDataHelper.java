/**
 * 
 * 
 * 
 *
 * Creation Date: Nov 15, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.charting;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.math.FloatRange;
import org.apache.commons.lang.math.LongRange;
import org.apache.log4j.Logger;

import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.UIHelper ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartEntity ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartModel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.EODValue ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartEntity.EntityType ;

/**
 * A data container to hold on the chart meta data information.
 */
class ChartMetaData {

    /**
     * A boolean flag which is set at the begining of every paint operation,
     * indicating whether this chart should show the volume chart. A volume
     * chart is shown if and only if we are dealing with only one entity and
     * the entity is of scrip type. Also, please note that the volume chart
     * can be hidden explicitly by the user. Hence whether the volume chart
     * is shown or not is an and operation with the user preference for
     * volume chart.
     */
    boolean showVolChart = false ;

    /* The coordinate space for rendering the different graph elements. This
     * rectangles are populated the first thing in the paint logic based on
     * the current size of the component. These rectangles will be used during
     * the paint operation to easy access to the co-ordinate system.
     */
    final Rectangle canvasRect = new Rectangle( 0, 0, 0, 0 );
    final Rectangle graphRect  = new Rectangle( 0, 0, 0, 0 );
    final Rectangle xAxisRect  = new Rectangle( 0, 0, 0, 0 );
    final Rectangle yAxisRect  = new Rectangle( 0, 0, 0, 0 );
    final Rectangle volumeRect = new Rectangle( 0, 0, 0, 0 );

    // ---------------- Y Axis Meta Data ---------------------------------------
    /** Value per pixel on the Y Axis. */
    double valPerYPixel = 0 ;

    /** The absolute minimum Y axis value. */
    double minYValue = 0 ;

    /** The maximum Y axis value. */
    double maxYValue = 0 ;

    /** The optimal tick values for the Y axis. */
    Float[] yAxisOptTickValues = { 0F, 0.5F, 1F } ;

    /** Whether to show Y axis min tick values. */
    boolean showYMinTickValues = false ;

    // ---------------- X Axis Meta Data ---------------------------------------
    /** The number of milliseconds per pixels in the X axis. */
    double valPerXPixel = 0 ;

    /** The number of pixels per day on the X axis. */
    double xPixelsPerDay = 0 ;

    /** The time interval between the X axis intra day tick markers in minutes. */
    int itdXAxisTickSepMins = 60 ;

    /** The map of Day versus X axis offset in pixels. */
    final Map<Date, Integer> xAxisDayOffset = new TreeMap<Date, Integer>() ;

    // ------------------- Volume graph meta data ------------------------------
    /** The value that each pixel on the volume Y graph represents. */
    double volValPerYPixel = 0 ;

    /** Reverts all values to their default state. */
    public void clean() {
        this.showVolChart = false ;

        this.canvasRect.x = this.canvasRect.y = this.canvasRect.width = this.canvasRect.height = 0 ;
        this.graphRect.x  = this.graphRect.y  = this.graphRect.width  = this.graphRect.height  = 0 ;
        this.xAxisRect.x  = this.xAxisRect.y  = this.xAxisRect.width  = this.xAxisRect.height  = 0 ;
        this.yAxisRect.x  = this.yAxisRect.y  = this.yAxisRect.width  = this.yAxisRect.height  = 0 ;
        this.volumeRect.x = this.volumeRect.y = this.volumeRect.width = this.volumeRect.height = 0 ;

        this.valPerXPixel  = 0 ;
        this.xPixelsPerDay = 0 ;
        this.xAxisDayOffset.clear() ;
        this.itdXAxisTickSepMins = 60 ;

        this.minYValue     = 0 ;
        this.maxYValue     = 0 ;
        this.valPerYPixel  = 0 ;
        this.yAxisOptTickValues = new Float[]{ 0F, 1F } ;
        this.showYMinTickValues = false ;
    }
}

/**
 * This class is responsible for encapsulating the charting meta data for the
 * chart that this meta data is associated with. Every chart panel contains an
 * instance of this class and requests this instance to refresh the rendering
 * meta data at the start of the paint operation.
 *
 * The logic for this class has been refactored from the canvas since the meta
 * data computation logic is significant and needs special attention,
 * encapsulation and maintainence.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
class ChartMetaDataHelper {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ChartMetaDataHelper.class ) ;

    /**
     * A lookup array for computing the optimal time interval for tick marks on
     * the X axis. The 0th index is the number of ticks and the 1st index is
     * the number of minutes of separation.
     */
    private static final int[][] X_AXIS_TICK_ARRAY = {
        { 2,  180 },
        { 3,  120 },
        { 6,   60 },
        { 11,  30 },
        { 17,  15 },
        { 33,  10 },
        { 66,   5 }
    } ;

    /** The height for the volume chart. */
    private static final int VOL_CHART_HEIGHT = 50 ;

    /** The height of the plottable area of the volume chart. */
    private static final int VOL_CHART_PLOTTABLE_HEIGHT = 45 ;

    /** A reference to the chart model .*/
    private final ChartModel model ;

    /** A reference to the chart canvas. */
    private final ChartCanvas canvas ;

    /** The rendering data. This will be refreshed at the start of compute meta data funciton. */
    private Map<ChartEntity, List<EODValue>> renderData = null ;

    /** User's preference on whether the vol graph is to be shown. */
    private boolean userPrefShowVolGraph = true ;

    /**
     * Package public constructor which takes it a reference t
     *
     * @param canvas The charting canvas
     * @param model The chart model
     */
    ChartMetaDataHelper( final ChartCanvas canvas, final ChartModel model ) {
        super() ;
        this.model = model ;
        this.canvas = canvas ;
    }

    /**
     * This function will be called upon by the owning chart canvas at the
     * begining of each paint operation. This function computes the charting
     * meta data based on the current state of the charting panel/canvas and
     * model and fills the information in the supplied meta data container.
     * If the parameter is null, a new meta data instance is created and
     * returned.
     *
     * @param metaData The meta data container to use instead of creating a
     *        new instance.
     *
     * @return A chart meta data instance containing the meta data information.
     */
    public ChartMetaData computeMetaData( final Graphics2D g,
                                          final ChartMetaData metaData ) {

        final ChartMetaData retVal = ( metaData == null ) ? new ChartMetaData() : metaData ;
        retVal.clean() ;

        this.renderData = this.model.getRenderData() ;

        computeShowVolChart( retVal ) ;
        computeComponentRectangles( g, retVal ) ;
        computeYAxisMetaData( g, retVal ) ;
        computeXAxisMetaData( g, retVal ) ;

        return retVal ;
    }

    /**
     * Computes whether the chart should show volume data. Volume data is
     * rendered if the following conditions are met:
     * a) User wants to see the volume data
     * b) Chart has only one entity
     * c) The chart entity is of Scrip type.
     *
     * @param metaData The meta data into which the value will be populated.
     */
    private void computeShowVolChart( final ChartMetaData metaData ) {

        // Check of if we need to show the volume graph. A volume graph is
        // shown if and only if we are dealing with one entity and the entity is
        // a scrip. Else we hide the volume chart.
        metaData.showVolChart = false ;
        if( this.renderData.size() == 1 && this.userPrefShowVolGraph ) {
            final ChartEntity entity = this.renderData.keySet().iterator().next() ;
            if( entity.getEntityType() == EntityType.SCRIP ) {
                metaData.showVolChart = true ;

                final LongRange volRange = this.model.getVolAxisRange() ;
                metaData.volValPerYPixel = volRange.getMaximumLong() / VOL_CHART_PLOTTABLE_HEIGHT ;
            }
        }
    }

    /**
     * Computes the rectangles in the overall coordinate space for individual
     * chart components, like the x axis, y axis, graph canvas, pointer value
     * etc. The component rectangles are computed before the custom painting
     * can happen and helps translate the painting coordinate system during
     * the paint operation.
     */
    private void computeComponentRectangles( final Graphics2D g,
                                             final ChartMetaData metaData ) {

        // Temporary variables to store intermediate results.
        int x, y, w, h = 0 ;
        final FontMetrics fm      = g.getFontMetrics( UIConstant.CHART_AXIS_FONT ) ;
        final Rectangle2D fWidth  = fm.getStringBounds( "8888.88", g ) ;
        final Dimension   dim     = this.canvas.getSize() ;

        final int xAxisHeight = ( int )( fWidth.getHeight() + ChartCanvas.MAX_TICK_LEN ) + 6 ;
        final int yAxisWidth  = ( int )( fWidth.getWidth()  + ChartCanvas.MAX_TICK_LEN ) + 5 ;

        // Compute the entire canvas rectangle.
        metaData.canvasRect.setBounds( 1, 1, dim.width-2, dim.height-2 ) ;

        // If we have to display the volume markers - compute the volume
        // rectangle. Else the dimensions will be 0
        if( metaData.showVolChart ) {
            x = metaData.canvasRect.x + yAxisWidth + 1 ;
            y = metaData.canvasRect.height - xAxisHeight - VOL_CHART_HEIGHT ;
            w = metaData.canvasRect.width  - yAxisWidth  - 1 ;
            h = VOL_CHART_HEIGHT ;
            metaData.volumeRect.setBounds( x, y, w, h ) ;
        }

        // Compute the Y axis space. This include the X tick, the width of the
        // tick label to be painted.
        x = metaData.canvasRect.x ;
        y = metaData.canvasRect.y ;
        w = yAxisWidth ;
        h = metaData.canvasRect.height - xAxisHeight - metaData.volumeRect.height ;
        metaData.yAxisRect.setBounds( x, y, w, h ) ;

        // Compute the X axis space. This include the Y tick, the height of the
        // tick label to be painted.
        x = metaData.canvasRect.x     + metaData.yAxisRect.width  + 1 ;
        y = metaData.canvasRect.y     + metaData.yAxisRect.height + + metaData.volumeRect.height + 1 ;
        w = metaData.canvasRect.width - metaData.yAxisRect.width  - 1 ;
        h = xAxisHeight-2 ;
        metaData.xAxisRect.setBounds( x, y, w, h ) ;

        // Compute the graph rectangle.
        x = metaData.canvasRect.x + metaData.yAxisRect.width + 1 ;
        y = metaData.canvasRect.y ;
        h = metaData.yAxisRect.height ;
        w = metaData.xAxisRect.width ;
        metaData.graphRect.setBounds( x, y, w, h ) ;
    }

    /**
     * Computes the meta data for the Y axis. This function should be called
     * after computing the chart rectangle spaces since it depends upon the
     * dimentions of the rectangles to compute the Y axis meta data.
     */
    private void computeYAxisMetaData( final Graphics2D g, final ChartMetaData metaData ) {

        FloatRange range = this.model.getYAxisRange() ;
        if( range == null ) {
            range = new FloatRange( 0, 1 ) ;
        }

        final FontMetrics fm          = g.getFontMetrics( UIConstant.CHART_AXIS_FONT ) ;
        final float       min         = ( float )UIHelper.floor( range.getMinimumFloat(), 0.5 ) ;
        final float       max         = ( float )UIHelper.ceil(  range.getMaximumFloat(), 0.5 ) ;
        final int         numPixels   = metaData.graphRect.height ;
        final float       valPerPixel = ( max-min )/numPixels ;

        // So we have to fit the number of price ticks within numPixels.
        metaData.yAxisOptTickValues = UIHelper.breakup( min, max, numPixels ) ;
        metaData.minYValue     = min ;
        metaData.maxYValue     = max ;
        metaData.valPerYPixel  = valPerPixel ;

        int tickSepPixel = 0 ;
        if( metaData.yAxisOptTickValues.length > 1 ) {
            tickSepPixel = (int)(( metaData.yAxisOptTickValues[1] - metaData.yAxisOptTickValues[0] )/valPerPixel ) ;
            if( tickSepPixel > fm.getHeight()*1.5 ) {
                metaData.showYMinTickValues = true ;
            }
        }
    }

    /**
     * Computes the meta data for the X axis. This function should be called
     * after computing the chart rectangle spaces since it depends upon the
     * dimentions of the rectangles to compute the X axis meta data.
     *
     * Please note that X axis meta data is not computed if there is no render
     * data. The existence of render data should be ascertained by the chart
     * canvas before using the X axis meta data.
     */
    private void computeXAxisMetaData( final Graphics2D g, final ChartMetaData metaData ) {

        if( this.renderData.isEmpty() ) {
            return ;
        }

        // All the chart entities will have the data for the same number of
        // days. Pick up the first value and get the size of the EODValue list
        final List<EODValue> eodValueList = this.renderData.values().iterator().next() ;
        final int numDays = eodValueList.size() ;

        // The X axis has to accommodate numDays worth of days. Implying that
        // each day would get x axis size/numDays pixels. Store the X axis
        // offsets in a map for easy lookup when we render the ITD values.
        metaData.xPixelsPerDay = ( (float)metaData.xAxisRect.width / numDays ) ;
        metaData.valPerXPixel  = ( ChartCanvas.DAY_NUM_MILLIS) / metaData.xPixelsPerDay ;

        // Iterate through the EOD value list and compute the X axis offsets
        // for days in the EOD value list.
        for( int i=0; i<eodValueList.size(); i++ ) {

            final EODValue eodVal = eodValueList.get( i ) ;
            final int xAxisPixel  = (int)( Math.ceil( i*metaData.xPixelsPerDay ) ) ;
            metaData.xAxisDayOffset.put( eodVal.getDate(), xAxisPixel ) ;
        }

        // Compute the optimal intra day time separation
        computeOptimalXAxisTickSeparationForDay( g, metaData ) ;
    }

    /**
     * Computes the optimal X axis tick separation in minutes for a day based
     * on the current graphics context and chart axis font. This is used for
     * graphs which render intra day values.
     *
     * @param g The graphics context
     *
     * @return An integer value representing time in minutes.
     */
    private void computeOptimalXAxisTickSeparationForDay(
                            final Graphics2D g, final ChartMetaData metaData ) {

        final FontMetrics fm = g.getFontMetrics( UIConstant.CHART_AXIS_FONT ) ;
        final int tickWidth = (int)(fm.getStringBounds( "888888", g ).getWidth()*1.5) ;
        int optInterval = -1 ;

        int optIndex = X_AXIS_TICK_ARRAY.length-1 ;
        for( int i=0; i<X_AXIS_TICK_ARRAY .length; i++ ) {
            if( metaData.xPixelsPerDay - X_AXIS_TICK_ARRAY[i][0]*tickWidth <= 0 ) {
                optIndex = i-1 ;
                break ;
            }
        }

        if( optIndex < 0 ) {
            optInterval = -1 ;
        }
        else {
            optInterval = X_AXIS_TICK_ARRAY[optIndex][1] ;
        }

        metaData.itdXAxisTickSepMins = optInterval ;
    }

    /** Sets the user preference on whether he wants to see the vol graph. */
    public void setUserPreferredShowVolGraph( final boolean selected ) {
        this.userPrefShowVolGraph = selected ;
    }
}
