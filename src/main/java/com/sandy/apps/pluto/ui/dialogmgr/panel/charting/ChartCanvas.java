/**
 * 
 * 
 * 
 *
 * Creation Date: Oct 17, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.charting;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.STConstant ;
import com.sandy.apps.pluto.ui.GraphicsContextStack ;
import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.UIHelper ;
import com.sandy.apps.pluto.ui.dialogmgr.AbstractPlutoFramePanel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartEntity ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartModel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.EODValue ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ITDValue ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartModel.ModelEvent ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartModel.ModelListener ;

/**
 * This panel renders the chart in the charting panel. The charting panel
 * consists of three major parts, the chart controls on the top, the
 * participant editor on the right and the canvas, occupying the center stage.
 * This class has been refactored from the {@link ChartingPanel} to clarify
 * and partition logic more effectively.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ChartCanvas extends JPanel
    implements UIConstant, ModelListener, MouseMotionListener, MouseListener {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 561684807893431129L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ChartCanvas.class ) ;

    /** The decimal format used to render price values. */
    private static final DecimalFormat PRICE_DF = new DecimalFormat( "##0.00" ) ;

    /** Some constants dictating the UI layout. */
    public static final int MIN_TICK_LEN = 4 ;
    public static final int MAX_TICK_LEN = 7 ;

    /** The offset from any corner that the cross hair value will be printed. */
    private static final int CROSS_HAIR_OFFSET = 4 ;

    /** Some predefined colors */
    private static final Color AXIS_COLOR = Color.DARK_GRAY ;

    /** The major grid color. */
    private static final Color MAJOR_GRID_COLOR = new Color( 25, 25, 25 ) ;

    /** The major time grid color. Sligntly brighter than Y */
    private static final Color MAJOR_TIME_GRID_COLOR = new Color( 35, 35, 35 ) ;

    /** The minor grid color. */
    private static final Color MINOR_GRID_COLOR = new Color( 10, 10, 10 ) ;

    /** The color of the cross hair. */
    private static final Color CROSS_HAIR_COLOR = new Color( 0, 89, 22 ) ;

    /** The color in which cross hair value is printed. */
    private static final Color CROSS_HAIR_VALUE_COLOR = new Color( 0, 206, 115 ) ;

    /** The minor grid color for the time axis - slightly brighter than Y. */
    private static final Color MINOR_TIME_GRID_COLOR = new Color( 15, 15, 15 ) ;

    /** The day starts at 9:55 AM - This constant is number of milliseconds for 9:55 hrs. */
    public static final long DAY_START_MILLIS = (9*60+55)*60*1000 ;

    /** The day ends at 15:30 PM - This constant is number of milliseconds for 15:30 hrs. */
    public static final long DAY_END_MILLIS = (15*60+30)*60*1000 ;

    /** The number of business milliseconds in a normal day. */
    public static final long DAY_NUM_MILLIS = DAY_END_MILLIS - DAY_START_MILLIS ;

    /** The date format for day markers on the X axis. */
    private static final DateFormat X_AXIS_DATE_FMT_DAY = new SimpleDateFormat( "dd/MM" ) ;

    /** The date format for year markers on the X axis. */
    private static final DateFormat X_AXIS_DATE_FMT_YEAR = new SimpleDateFormat( "yyyy" ) ;

    /** The time format for day markers on the X axis. */
    private static final DateFormat X_AXIS_TIME_FORMAT = new SimpleDateFormat( "HH:mm" ) ;

    /** The stroke used to draw the previous day closing marker line. */
    private static final BasicStroke PREV_CLOSE_STROKE = new BasicStroke(
                               0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                               0, new float[]{1,4,1,4}, 0);

    /**
     * The X, Y co-ordinates relative to the graph rectangle where the mouse had
     * moved last. This is used for tracking the last coordinates of the mouse
     * and drawing the hairline.
     */
    private int lastMouseX = -1 ;
    private int lastMouseY = -1 ;

    /** The model which contains the data to be rendered. */
    private final ChartModel model ;

    /** The panel which owns this canvas and/or its ancestors. */
    protected final AbstractPlutoFramePanel owner ;

    /** The meta data helper who will compute the render meta data for each paint. */
    private final ChartMetaDataHelper metaDataHelper ;

    /** An instance of the chart meta data which will be refreshed by the helper. */
    private ChartMetaData meta = null ;

    /**
     * Public constructor which accepts a chart model whose data is being
     * rendered by this canvas.
     */
    public ChartCanvas( final ChartModel model, final AbstractPlutoFramePanel owner ) {
        super() ;
        this.model = model ;
        this.owner = owner ;
        this.metaDataHelper = new ChartMetaDataHelper( this, this.model ) ;
        setUpUI() ;
    }

    /**
     * Sets up the fundamental UI characteristics of this panel.
     */
    private void setUpUI() {
        setBackground( Color.BLACK ) ;
        addMouseListener( this ) ;
        addMouseMotionListener( this ) ;
        setCursor( UIHelper.CHART_CURSOR ) ;
        // Set the panel to double buffered. This will result in a more smoother
        // visual experience since we do a lot of custom painting and hence
        // want the painting to happen offline.
        super.setDoubleBuffered( true ) ;
    }

    /**
     * This function is called when the user selects to show or hide the
     * volume graph.
     */
    public void setShowVolGraph( final boolean selected ) {
        this.metaDataHelper.setUserPreferredShowVolGraph( selected ) ;
        repaint() ;
    }

    /**
     * This method is invoked when the underlying chart model has changed. The
     * nature of the change can be ascertained by evaluating the type of the
     * event being generated.
     */
    @Override
    public void modelChanged( final ModelEvent event ) {
        // Remember we can paint on the whole canvas, there is a one pixel on
        // the left and bottom edges which is painted by the parent panel.
        repaint( 1, 0, getSize().width-1, getSize().height-1 ) ;
    }

    /**
     * Paints the chart. This method calls upon the chart model for the data
     * that needs rendering. This method is called upon in different scenarios.
     * For example, if the window is being moved the swing framework calls on
     * paint to render itself, if the model changes substantially, the
     * modelChanged method is invoked, which in turn invokes a repaint.
     */
    @Override
    public void paint( final Graphics g ) {

        try {
            // Let the super class do the default paining, post which we will paint.
            // The default paint will clear the entire canvas to a black background.
            super.paint( g ) ;

            // Clean the cross hair if any
            cleanCrossHair() ;

            final Graphics2D g2d = ( Graphics2D )g ;
            final Map<Object, Object> hints = new HashMap<Object, Object>() ;
            hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON ) ;
            g2d.setRenderingHints( hints );

            // Comptue the charting meta data.
            this.meta = this.metaDataHelper.computeMetaData( g2d, this.meta ) ;

            drawYAxisTicks( g2d ) ;
            drawXAxisTicks( g2d ) ;

            // Determine if we are operating in a intra day display time range or
            // and end of day display time range.
            final boolean itdRange = this.model.isIntradayRange() ;

            // Get the number of days we are dealing with.
            final Map<ChartEntity, List<EODValue>> renderData = this.model.getRenderData() ;
            for( final ChartEntity entity : renderData.keySet() ) {
                drawPriceCurve( g2d, entity, renderData.get( entity ), itdRange ) ;
                if( this.meta.showVolChart ) {
                    drawVolCurve( g2d, entity, renderData.get( entity ), itdRange ) ;
                }
            }

            // Just repaint the axis lines for a cleaner finish.
            drawAxisLines( g2d ) ;
        }
        catch ( final Throwable e ) {
            e.printStackTrace();
        }
    }

    /** Debug function to highlight the coordinate space of a graph component. */
    protected void drawRectangle( final Graphics2D g, final Rectangle r,
                                  final Color color ) {

        GraphicsContextStack.push( g, r ) ;
        g.setColor( color ) ;
        g.drawRect( 0, 0, r.width, r.height ) ;
        GraphicsContextStack.pop( g ) ;
    }

    /**
     * Draws the axis lines without the tick marks. These lines have no
     * scale and are just visual markers. The Axis scales would be computed
     * in the draw X Axis Ticks and draw Y Axis Ticks methods.
     *
     * @param g The graphics context for this canvas.
     */
    private void drawAxisLines( final Graphics2D g ) {

        Rectangle r = this.meta.graphRect ;

        GraphicsContextStack.push( g, r ) ;
        g.setColor( AXIS_COLOR ) ;
        g.drawLine( 0, 0, 0, r.height ) ;
        g.drawLine( 0, r.height, r.width, r.height ) ;
        GraphicsContextStack.pop( g ) ;

        r = this.meta.volumeRect ;
        if( !r.isEmpty() ) {
            GraphicsContextStack.push( g, r ) ;
            g.setColor( AXIS_COLOR ) ;
            g.drawLine( 0, 0, 0, r.height ) ;
            g.drawLine( 0, r.height, r.width, r.height ) ;
            GraphicsContextStack.pop( g ) ;
        }
    }

    /**
     * Renders the ticks on the Y axis based on the Y axis value range. This
     * method also saves some important variables like min Y value, value per
     * Y pixel and optimum Y tick markers for used by later rendering logic.
     *
     * @param g The graphics context for this canvas.
     */
    private void drawYAxisTicks( final Graphics2D g ) {

        // The co-ordinate system for the Y axis
        final Rectangle r             = this.meta.yAxisRect ;
        final Float[]   optTickValues = this.meta.yAxisOptTickValues ;
        final double    min           = this.meta.minYValue ;
        final double    max           = this.meta.maxYValue ;
        final double    valPerPixel   = this.meta.valPerYPixel ;

        final FontMetrics fm = g.getFontMetrics( UIConstant.CHART_AXIS_FONT ) ;

        GraphicsContextStack.push( g, r ) ;

        g.setFont( CHART_AXIS_FONT ) ;
        g.setColor( Color.gray ) ;

        // Print the minimum value on the axis
        final String minYStr = " " + PRICE_DF.format( this.meta.minYValue ) ;
        g.drawLine( r.width - MAX_TICK_LEN, r.height, r.width, r.height ) ;
        g.drawString( minYStr, 0, r.height + fm.getAscent()/2 ) ;

        // Now loop though all the optimum values and print the tick markers.
        for( int i=0; i<optTickValues.length; i++ ) {

            final double value = ( optTickValues[i] < min ) ? min : optTickValues[i] ;
            final int    y     = (int)(r.height - ( value - min )/valPerPixel ) ;

            if( value != max ) {
                // A major tick marker is one which is printed very pre defined tick interval.
                if( (i+1) % TICK_PRINT_INTERVAL == 0 && ( i != 0 ) ) {
                    g.setColor( Color.gray ) ;
                    g.drawString( " " + PRICE_DF.format( value ), 0, y+fm.getAscent()/2 ) ;
                    g.drawLine( r.width-MAX_TICK_LEN, y, r.width , y ) ;
                    g.setColor( MAJOR_GRID_COLOR ) ;

                    // Don't erase the prime axis line
                    if( y != r.height ) {
                        g.drawLine( r.width + 1 , y, r.width + this.meta.canvasRect.width, y ) ;
                    }
                }
                else {
                    // If it is not a major tick marker, we print the minor
                    // tick markers only if they don't clutter the axis. We
                    // draw the grid line anyways.
                    g.setColor( Color.gray ) ;
                    g.drawLine( r.width-MIN_TICK_LEN, y, r.width, y ) ;
                    g.setColor( MINOR_GRID_COLOR ) ;

                    // Don't erase the prime axis line
                    if( y != r.height ) {
                        g.drawLine( r.width + 1 , y, r.width + this.meta.canvasRect.width, y ) ;
                    }

                    if( this.meta.showYMinTickValues ) {
                        g.drawString( " " + PRICE_DF.format( value ), 0, y+fm.getAscent()/2 ) ;
                    }
                }
            }
        }

        GraphicsContextStack.pop( g ) ;
    }

    /**
     * Draws the X (time) axis ticks based on the time range and the width
     * of the canvas.
     *
     * @param g The graphics context.
     */
    private void drawXAxisTicks( final Graphics2D g ) {

        // Get the number of days we are dealing with.
        final Map<ChartEntity, List<EODValue>> renderData = this.model.getRenderData() ;

        // If there is no EOD data - highly unlikely, there is nothing to do.
        if( renderData.isEmpty() ) {
            return ;
        }

        GraphicsContextStack.push( g, this.meta.xAxisRect ) ;
        g.setColor( Color.white ) ;
        g.setFont( CHART_AXIS_FONT ) ;

        final FontMetrics fm = g.getFontMetrics( UIConstant.CHART_AXIS_FONT ) ;
        final String  fixedRange = this.model.getFixedTimeRange() ;
        final Calendar tempCal   = Calendar.getInstance() ;

        int     lastDayOfWeek       = -1 ;
        int     lastDateOfMonth     = -1 ;
        int     lastDayOfYear       = -1 ;
        boolean weekStart           = false ;
        boolean monthStart          = false ;
        boolean yearStart           = false ;

        // Determine if we are operating in a intra day display time range or
        // and end of day display time range.
        boolean itdRange = false ;
        if( fixedRange.equals( RANGE_1D ) || fixedRange.equals( RANGE_2D ) ||
            fixedRange.equals( RANGE_3D ) || fixedRange.equals( RANGE_4D ) ||
            fixedRange.equals( RANGE_5D ) || fixedRange.equals( RANGE_2W ) ) {
            itdRange = true ;
        }

        // All the chart entities will have the data for the same number of
        // days. Pick up the first value and get the size of the EODValue list
        final List<EODValue> eodValueList = renderData.values().iterator().next() ;

        // Iterate through the EOD value list and draw the curve.
        for( int i=0; i<eodValueList.size(); i++ ) {

            final EODValue eodVal = eodValueList.get( i ) ;

            // -----------------------------------------------------------------
            // Determine if this EOD value is for the start of a week.
            tempCal.setTime( eodVal.getDate() ) ;
            final int dayOfWeek = tempCal.get( Calendar.DAY_OF_WEEK ) ;
            if( lastDayOfWeek != -1 ) {
                weekStart = ( dayOfWeek < lastDayOfWeek ) ;
            }

            // Determine if this EOD value is for the start of a month.
            final int dateOfMonth = tempCal.get( Calendar.DATE ) ;
            if( lastDateOfMonth != -1 ) {
                monthStart = ( dateOfMonth < lastDateOfMonth ) ;
            }

            // Determine if this EOD value is for the start of a month.
            final int dayOfYear = tempCal.get( Calendar.DAY_OF_YEAR ) ;
            if( lastDayOfYear != -1 ) {
                yearStart = ( dayOfYear < lastDayOfYear ) ;
            }
            // -----------------------------------------------------------------

            final int xAxisPixel = this.meta.xAxisDayOffset.get( eodVal.getDate() ) ;
            final String dateStr = ( yearStart ) ?
                             X_AXIS_DATE_FMT_YEAR.format( eodVal.getDate() ) :
                             X_AXIS_DATE_FMT_DAY.format( eodVal.getDate() ) ;

            // Draw the major minor grid lines for day, week and month boundaries.
            drawXAxisGridLines( g, weekStart, monthStart, yearStart, itdRange,
                               xAxisPixel, dateStr ) ;

            // Now as per the intra day tick separation, draw the ticks only if
            // we have a valid intra day tick separation interval.
            if( this.meta.itdXAxisTickSepMins > 0 ) {

                final Date day            = eodVal.getDate() ;
                final long dayStartMillis = day.getTime() ;
                final long dayEndMillis   = day.getTime() + DAY_END_MILLIS ;

                // Tick baseline is 10:00 AM
                long tickMillis = dayStartMillis + 10*60*60*1000 ;

                // First tick mark is at 10:00 AM + intra day sep
                tickMillis +=  this.meta.itdXAxisTickSepMins * 60 * 1000 ;

                // Go on printing the tick marks till we exceed the businss end time.
                while( tickMillis < dayEndMillis ) {

                    final int xPixel = xAxisPixel + getGraphXPixel( tickMillis, dayStartMillis ) ;

                    final Date   date     = new Date( tickMillis ) ;
                    final String dateFmtd = X_AXIS_TIME_FORMAT.format( date ) ;
                    final int    strWidth = (int) fm.getStringBounds( dateFmtd, g ).getWidth() ;

                    g.drawString( dateFmtd, xPixel - strWidth/2, this.meta.xAxisRect.height-1 ) ;

                    // The intra day grid lines are always minor grid lines.
                    drawXGridLine( g, xPixel, false ) ;

                    tickMillis += this.meta.itdXAxisTickSepMins * 60 * 1000 ;
                }
            }

            // Save a the current values for use in the next iteration.
            lastDayOfWeek = dayOfWeek ;
            lastDateOfMonth = dateOfMonth ;
            lastDayOfYear = dayOfYear ;
        }

        GraphicsContextStack.pop( g ) ;
    }

    /**
     * Draws the X axis grid lines. It is assumed that the graphics context
     * has already been translated to the canvas rectangle.
     *
     * @param g The graphics context
     * @param weekStart A boolean flag indicating if this line is for a week start
     * @param monthStart A boolean flag indicating if this line is for a month start
     * @param yearStart A boolean flag indicating if this line is for a year start
     * @param itdRange Whether we are drawing line for a itd range time interval
     * @param xAxisPixel The x axis pixel at which to draw the line
     * @param dateStr The date string to print.
     */
    private void drawXAxisGridLines( final Graphics2D g,
                                     final boolean weekStart,
                                     final boolean monthStart,
                                     final boolean yearStart,
                                     final boolean itdRange,
                                     final int xAxisPixel,
                                     final String dateStr ) {

        final String  fixedRange = this.model.getFixedTimeRange() ;
        final FontMetrics fm = g.getFontMetrics( UIConstant.CHART_AXIS_FONT ) ;
        final int dateStrWidth = (int)fm.getStringBounds( dateStr, g ).getWidth() ;

        if( itdRange ) {
            // If we are in an ITD range, i.e. 1D, 5D or 2W - print every
            // day as a major tick.
            g.setColor( Color.gray ) ;
            g.drawString( dateStr, xAxisPixel-dateStrWidth/2, this.meta.xAxisRect.height-1 ) ;

            // Draw the grid line - in case of ITD range, each day is a major
            // grid line while the day time is a minor grid line.
            drawXGridLine( g, xAxisPixel, true ) ;
        }
        else {
            if( fixedRange.equals( RANGE_1M ) || fixedRange.equals( RANGE_3M ) ) {
                // If we are in the 1M, 3M range - print it by the week. Days
                // are minor markers
                if( weekStart ) {
                    g.setColor( Color.gray ) ;
                    g.drawString( dateStr, xAxisPixel-dateStrWidth/2, this.meta.xAxisRect.height-1 ) ;

                    // Draw the grid line - in case of 1M and 3M range, each
                    // week is a major grid line while the day is a minor grid line.
                    drawXGridLine( g, xAxisPixel, true ) ;
                }
                else {
                    // Draw the grid line - in case of 1M and 3M range, each
                    // week is a major grid line while the day is a minor grid line.
                    drawXGridLine( g, xAxisPixel, false ) ;
                }
            }
            else if( fixedRange.equals( RANGE_6M ) || fixedRange.equals( RANGE_1Y ) ){
                // If we are in the 6M, 1Y range - print the months as the
                // major ticks and the weeks as the minor ticks.
                if( monthStart ) {
                    g.setColor( Color.gray ) ;
                    g.drawString( dateStr, xAxisPixel-dateStrWidth/2, this.meta.xAxisRect.height-1 ) ;

                    // Draw the grid line - in case of 6M and 1Y range, each
                    // month is a major grid line while the week is a minor grid line.
                    drawXGridLine( g, xAxisPixel, true ) ;
                }
                else if( weekStart ){
                    // Draw the grid line - in case of 6M and 1Y range, each
                    // month is a major grid line while the week is a minor grid line.
                    drawXGridLine( g, xAxisPixel, false ) ;
                }
            }
            else {
                // If we are above a year's range, i.e for 2Y, 5Y, 10Y we print the
                // year as the major grid and months as the minor grid
                if( yearStart ) {
                    g.setColor( Color.gray ) ;
                    g.drawString( dateStr, xAxisPixel-dateStrWidth/2, this.meta.xAxisRect.height-1 ) ;

                    // Draw the grid line - in case of 6M and 1Y range, each
                    // month is a major grid line while the week is a minor grid line.
                    drawXGridLine( g, xAxisPixel, true ) ;
                }
                else if( monthStart ){
                    // Draw the grid line - in case of 6M and 1Y range, each
                    // month is a major grid line while the week is a minor grid line.
                    drawXGridLine( g, xAxisPixel, false ) ;
                }
            }
        }
    }

    /**
     * Draws a grid line for the time (X) axis. This function also differentiates
     * between major and minor grid lines based on the boolean parameter passed.
     * NOTE: This function assumes that the graphics context has been already
     * translated to the graph rectangle.
     *
     * @param g The graphics context.
     *
     * @param xPixel The x pixel at which the grid line is to be drawn on the
     *        chart rectangle.
     *
     * @param major A boolean parameter indicating whether a major or a minor
     *        grid line has to be drawn.
     */
    private void drawXGridLine( final Graphics2D g, final int xPixel,
                                final boolean major ) {
        final Color oldColor = g.getColor() ;

        // If the xPixel is zero - we don't draw anything. Drawing in this
        // condition will overwrite the axix
        if( xPixel == 0 ) {
            return ;
        }

        Color gridColor = null ;
        Color tickColor = null ;
        int   tickLen   = 0 ;

        if( major ) {
            gridColor = MAJOR_TIME_GRID_COLOR ;
            tickColor = Color.GRAY ;
            tickLen   = MAX_TICK_LEN ;
        }
        else {
            gridColor = MINOR_TIME_GRID_COLOR ;
            tickColor = Color.GRAY ;
            tickLen   = MIN_TICK_LEN ;
        }

        g.setColor( gridColor ) ;
        g.drawLine( xPixel, 0, xPixel, -this.meta.graphRect.height-this.meta.volumeRect.height ) ;

        g.setColor( tickColor ) ;
        g.drawLine( xPixel, 0, xPixel, tickLen ) ;

        g.setColor( oldColor ) ;
    }

    /**
     * Draws the curves for the given chart entity and the list of end of day
     * values. A chart can be drawn in either absolute or comparision mode -
     * however this function is agnostic of the mode and relies solely on the
     * values contained in the EOD values and the chart charateristics deduced
     * by analyzing data from the model - for example, the value of each pixel
     * on the axis, number of days etc.
     * <p>
     * This function renders the following as a part of rendering the entity
     * data points.
     * <p>
     * <b>Draw the previous closing line :</b><br/>
     * a) The previous closing line is drawn only in case of absolute rendering.
     *    Absolute rendering can be deduced by the presence of only on entity
     *    in the entity map.
     *
     * b) If we are operating under a 1D or 5D time range - the previous closing
     *    line is drawn for the previous day.
     *
     * c) If we are operating under 2W or 1M - the previous closing is drawn at
     *    week boundaries.
     *
     * c) If we are operating under 3M, 6M or 1Y - the previous closing is
     *    drawn at month boundaries.
     *
     * <b>Draw the intra day values :</b><br/>
     * The intra day values are drawn only in case of 1D, 5D or 2W
     *
     * @param g The graphics context.
     */
    private void drawPriceCurve( final Graphics2D g,
                             final ChartEntity entity,
                             final List<EODValue> eodValues,
                             final boolean itdRange ) {

        GraphicsContextStack.push( g, this.meta.graphRect ) ;

        final String  fixedRange = this.model.getFixedTimeRange() ;
        final boolean isAbsolute = ( this.model.getRenderData().size() == 1 ) ;
        final Calendar tempCal   = Calendar.getInstance() ;

        int     lastDayOfWeek       = -1 ;
        int     lastDateOfMonth     = -1 ;
        int     lastDayOfYear       = -1 ;
        boolean weekStart           = true ;
        boolean monthStart          = true ;
        boolean yearStart           = true ;
        float   lastWeekClosing     = 0.0f ;
        float   lastMonthClosing    = 0.0f ;
        float   lastYearClosing     = 0.0f ;
        boolean firstEODValue       = true ;

        int lastEODYPix = 0 ;

        for( final EODValue eodVal : eodValues ) {

            // -----------------------------------------------------------------
            // Determine if this EOD value is for the start of a week.
            tempCal.setTime( eodVal.getDate() ) ;
            final int day = tempCal.get( Calendar.DAY_OF_WEEK ) ;
            if( lastDayOfWeek != -1 ) {
                weekStart = ( day < lastDayOfWeek ) ;
            }

            // Determine if this EOD value is for the start of a month.
            final int date = tempCal.get( Calendar.DATE ) ;
            if( lastDateOfMonth != -1 ) {
                monthStart = ( date < lastDateOfMonth ) ;
            }

            // Determine if this EOD value is for the start of a year.
            final int dayOfYear = tempCal.get( Calendar.DAY_OF_YEAR ) ;
            if( lastDayOfYear != -1 ) {
                yearStart = ( dayOfYear < lastDayOfYear ) ;
            }
            // -----------------------------------------------------------------

            // It might happen that some dates might not have a x axix pixel
            // marker for multi curve charts - data anomaly etc. In such a case
            // quitely ignore this EOD value and print a debug message.
            final Integer dayStartX = this.meta.xAxisDayOffset.get( eodVal.getDate() ) ;
            if( dayStartX == null ) {
                if( logger.isDebugEnabled() ) {
                    logger.debug( "X axix pixel marker not found for " +
                                  entity.getName() + " and date = " +
                                  STConstant.DATE_FMT.format( eodVal.getDate() ) ) ;
                }
                continue ;
            }

            final long dayTime     = eodVal.getDate().getTime() ;
            final int  dayStartXPx = this.meta.xAxisDayOffset.get( eodVal.getDate() ) ;
            final int  dayEndXPx   = ( int )( Math.ceil( dayStartXPx + this.meta.xPixelsPerDay ) ) ;

            int lastITDXPix = dayStartXPx ;
            int lastITDYPix = 0 ;
            lastITDYPix = getPriceGraphYPixel( eodVal.getOpen() ) ;

            // ============ Previous close marker rendering ====================
            // Set the graphics properties for printing the previous close marker.
            g.setColor( Color.pink ) ;
            g.setStroke( PREV_CLOSE_STROKE ) ;

            // Paint the previous close marker. In case of comparision charts,
            // there is no previous close markers. Instead the line at y=0 is
            // highlighted since all values are normalized relative to 0.
            if( !isAbsolute ) {
                final int zeroY = getPriceGraphYPixel( 0 ) ;
                g.drawLine( dayStartXPx, zeroY, dayEndXPx, zeroY ) ;
            }
            else {
                // If we are dealing with absolute curves. The rendering of
                // previous close markers takes on different meanings based on
                // the time frame we are dealing with. For example:
                if( fixedRange.equals( RANGE_1D ) || fixedRange.equals( RANGE_2D ) ||
                    fixedRange.equals( RANGE_3D ) || fixedRange.equals( RANGE_4D ) ||
                    fixedRange.equals( RANGE_5D ) || fixedRange.equals( RANGE_2W ) ) {
                    // If we are dealing with 1D or 5D, the previous close for every
                    // day is marked.
                    final int prevCloseY = getPriceGraphYPixel( eodVal.getPrevClose() ) ;
                    g.drawLine( dayStartXPx, prevCloseY, dayEndXPx, prevCloseY ) ;
                }
                else if( fixedRange.equals( RANGE_1M ) || fixedRange.equals( RANGE_3M ) ) {
                    // If we are dealing with 2W or 1M range, we draw the previous
                    // close markers at week intervals.
                    if( weekStart ) {
                        final int prevCloseY = getPriceGraphYPixel( eodVal.getPrevClose() ) ;
                        g.drawLine( dayStartXPx, prevCloseY, dayEndXPx, prevCloseY ) ;
                        lastWeekClosing = eodVal.getPrevClose() ;
                    }
                    else {
                        final int prevCloseY = getPriceGraphYPixel( lastWeekClosing ) ;
                        g.drawLine( dayStartXPx, prevCloseY, dayEndXPx, prevCloseY ) ;
                    }
                }
                else if( fixedRange.equals( RANGE_6M ) || fixedRange.equals( RANGE_1Y ) ){
                    // If we are dealing with 6M or 1Y range, we draw the previous
                    // close markers at month intervals.
                    if( monthStart ) {
                        final int prevCloseY = getPriceGraphYPixel( eodVal.getPrevClose() ) ;
                        g.drawLine( dayStartXPx, prevCloseY, dayEndXPx, prevCloseY ) ;
                        lastMonthClosing = eodVal.getPrevClose() ;
                    }
                    else {
                        final int prevCloseY = getPriceGraphYPixel( lastMonthClosing ) ;
                        g.drawLine( dayStartXPx, prevCloseY, dayEndXPx, prevCloseY ) ;
                    }
                }
                else {
                    // If we are dealing with 2Y, 5Y or 1oY range, we draw the previous
                    // close markers at year intervals.
                    if( yearStart ) {
                        final int prevCloseY = getPriceGraphYPixel( eodVal.getPrevClose() ) ;
                        g.drawLine( dayStartXPx, prevCloseY, dayEndXPx, prevCloseY ) ;
                        lastYearClosing = eodVal.getPrevClose() ;
                    }
                    else {
                        final int prevCloseY = getPriceGraphYPixel( lastYearClosing ) ;
                        g.drawLine( dayStartXPx, prevCloseY, dayEndXPx, prevCloseY ) ;
                    }
                }
            }
            // ============ Previous close marker rendering ends ===============

            // ############ Curve rendering ####################################
            // For this day, draw the curve for the intra day points. We do this
            // if and only if we are operating under 1D, 5D or 2W range. Note:
            // Even if we are attempting to draw the intraday curves, we might
            // not have any data for these days - special consideration required.
            if( itdRange ) {

                g.setColor( entity.getColor() ) ;
                g.setStroke( entity.getStroke() ) ;
                final Collection<ITDValue> itdValues = eodVal.getITDValues() ;

                if( itdValues.isEmpty() ) {
                    // If we do not have ITD values for this date, draw a line
                    // between the opening and closing values, approximating
                    // a linear extrapolation.
                    final int openingY = getPriceGraphYPixel( eodVal.getOpen() ) ;
                    final int closingY = getPriceGraphYPixel( eodVal.getClose() ) ;
                    g.setColor( entity.getColor().brighter() ) ;
                    g.drawLine( dayStartXPx, openingY, dayEndXPx, closingY ) ;
                }
                else {
                    for( final ITDValue itdVal : itdValues ) {
                        final long deltaMillis = itdVal.getTime() - dayTime - DAY_START_MILLIS ;
                        final int xPixels = (int)( deltaMillis / this.meta.valPerXPixel ) ;

                        final int xPix = dayStartXPx + xPixels ;
                        final int yPix = getPriceGraphYPixel( itdVal.getValue() ) ;

                        g.drawLine( lastITDXPix, lastITDYPix, xPix, yPix ) ;
                        lastITDXPix = xPix ;
                        lastITDYPix = yPix ;
                    }
                }
            }
            else {
                g.setColor( entity.getColor() ) ;
                g.setStroke( entity.getStroke() ) ;
                // If we are dealing with 1M, 3M, 6M or 1Y ranges, we revert to
                // the standard way of rendering, joining all the closing values.

                final float eodClose = eodVal.getClose() ;
                if( !firstEODValue ) {
                    final int closingY = getPriceGraphYPixel( eodClose ) ;
                    g.drawLine( (int)( dayStartXPx - this.meta.xPixelsPerDay ),
                                lastEODYPix, dayStartXPx, closingY ) ;
                    lastEODYPix = closingY ;
                }
                else {
                    lastEODYPix = getPriceGraphYPixel( eodClose ) ;
                    firstEODValue = false ;
                }
            }
            // ############ Curve rendering ends ###############################

            // Save a the current values for use in the next iteration.
            lastDayOfWeek = day ;
            lastDateOfMonth = date ;
            lastDayOfYear = dayOfYear ;
        }

        GraphicsContextStack.pop( g ) ;
    }

    /**
     * This function is called if and only if the volume drawing condition is
     * satisfied. This function extracts the volume information from the EOD
     * list and plots them as a bar chart in the volume graph co-ordinate system.
     */
    private void drawVolCurve( final Graphics2D g, final ChartEntity entity,
                               final List<EODValue> eodValues, final boolean itdRange ) {

        GraphicsContextStack.push( g, this.meta.volumeRect ) ;

        for( final EODValue eodVal : eodValues ) {

            // It might happen that some dates might not have a x axix pixel
            // marker for multi curve charts - data anomaly etc. In such a case
            // quitely ignore this EOD value and print a debug message.
            final Integer dayStartX = this.meta.xAxisDayOffset.get( eodVal.getDate() ) ;
            if( dayStartX == null ) {
                if( logger.isDebugEnabled() ) {
                    logger.debug( "X axix pixel marker not found for " +
                                  entity.getName() + " and date = " +
                                  STConstant.DATE_FMT.format( eodVal.getDate() ) ) ;
                }
                continue ;
            }

            final long dayTime     = eodVal.getDate().getTime() ;
            final int  dayStartXPx = this.meta.xAxisDayOffset.get( eodVal.getDate() ) ;

            // ############ Curve rendering ####################################
            // If it is ITD range, we do not chart values for EOD volumes.
            g.setColor( entity.getColor() ) ;
            g.setStroke( entity.getStroke() ) ;
            if( itdRange ) {

                // Note that the ITD values would have been filtered based on
                // resolution. The data store will contain a lot more data than
                // what we are seeing. Also note that filtering only removes
                // interpolated data points. Filtering does not remove non
                // interpolated data points. It is the non interpolated data
                // points that contain the volume information.
                final Collection<ITDValue> itdValues = eodVal.getITDValues() ;

                long lastVol = 0, vol = 0 ;
                long interpolationCount = 0 ;
                boolean skipFirstVol = true ;

                // Synchronize on the collection of ITD values before
                // iterating, since the itd values can be added to during the
                // time the volue is being painted. Similarly the itd array
                // would be synchronized inside the EODVal instance.
                synchronized( itdValues ) {
                    for( final ITDValue itdVal : itdValues ) {

                        // Logic behind the interpolation count - Take for example a
                        // case where we have received ITD data for a period of
                        // 11:00 - 11:30. Now say, we loose volume continuity for
                        // 30 minutes. When we receive the volume data at 12:00 PM,
                        // the difference since the last volume data will be huge,
                        // overshadowing the graph. Hence we have this logic where
                        // if we have received more than 15 interpolated data points
                        // we treat it as a fresh start. Hence we zero out the last
                        // volume.
                        if( interpolationCount > 15 ) {
                            lastVol = 0 ;
                            skipFirstVol = true ;
                        }

                        // Ignore the interpolated values - interpolated ITD values
                        // do not have volume information.
                        if( itdVal.isInterpolated() ) {
                            interpolationCount++ ;
                            continue ;
                        }
                        else {
                            interpolationCount = 0 ;

                            final long deltaMillis = itdVal.getTime() - dayTime - DAY_START_MILLIS ;
                            final int xPixels = (int)( deltaMillis / this.meta.valPerXPixel ) ;

                            final int xPix = dayStartXPx + xPixels ;
                            // Skip the first value - this is because we are ignoring
                            // interpolated values and hence if data is being collected
                            // late, we might end up having a huge spike since ITD
                            // data is shown as differential
                            if( skipFirstVol ) {
                                skipFirstVol = false ;
                            }
                            else {
                                if( lastVol != 0 ) {
                                    vol = itdVal.getVolume() - lastVol ;
                                    // I have been seeing some stray negative strokes. It is
                                    // not supposed to happen, but then data erronity can't
                                    // be ruled out. Just putting in a check to ignore such
                                    // spikes.
                                    if( vol >= 0 ) {
                                        final int yPix = (int)
                                                  (this.meta.volumeRect.height -
                                                   vol/this.meta.volValPerYPixel) ;
                                        g.drawLine( xPix, this.meta.volumeRect.height-1,
                                                    xPix, yPix-1 ) ;
                                    }
                                }
                                lastVol = itdVal.getVolume() ;
                            }
                        }
                    }
                }
            }
            else {
                // If we are dealing with 1M, 3M, 6M or 1Y ranges, we revert to
                // the standard way of rendering, joining all the closing values.
                final int yPix = (int)(this.meta.volumeRect.height - eodVal.getVolume()/this.meta.volValPerYPixel) ;
                g.drawLine( dayStartXPx, this.meta.volumeRect.height-1, dayStartXPx, yPix-1 ) ;
            }
        }

        GraphicsContextStack.pop( g ) ;
    }

    /** Converts the given value into a Y pixel in the chart rectangle. */
    private int getPriceGraphYPixel( final float value ) {

        int retVal = -1 ;

        final double rangeRelValue = value - this.meta.minYValue ;
        retVal = ( int )(rangeRelValue/this.meta.valPerYPixel ) ;
        retVal = this.meta.graphRect.height - retVal ;

        return retVal ;
    }

    /**
     * Given a y pixel value relative to the graph rectangle co-ordinate system,
     * return the absolute float value that this Y pixel represents. The value
     * returned by this function will be used to paint the cross hair Y value
     * on the axis.
     *
     * @param y The Y pixel relative to the co-ordinate space of the graph rectangle
     *
     * @return The absolute Y value based on the min and max Y values that the
     *         Y axis represents.
     */
    private float getGraphYValue( final int y ) {

        final float realY = this.meta.graphRect.height - y ;
        final float rangeRelValue = (float)(realY * this.meta.valPerYPixel) ;
        final float value = (float)(rangeRelValue + this.meta.minYValue) ;

        return value ;
    }

    /**
     * Given a x pixel value returns the date that the pixel represents on the
     * X axis.
     *
     * @param x The X pixel relative to the co-ordinate space of the graph rectangle
     *
     * @return The absolute Date as represented by the X pixel.
     */
    private Date getGraphXValue( final int x ) {
        Date date = null ;
        if( !this.meta.xAxisDayOffset.isEmpty() ) {
            final int    dayCnt  = (int)(x/this.meta.xPixelsPerDay) ;
            final Date[] dateArr = this.meta.xAxisDayOffset.keySet().toArray( new Date[0] ) ;

            for( int i=0; i<=dayCnt; i++ ) {
                date = dateArr[i] ;
            }

            if( date != null ) {
                final int dateXPixel = this.meta.xAxisDayOffset.get( date ) ;
                final int diffPixel  = x - dateXPixel ;
                final long numMillis = (long)(diffPixel*this.meta.valPerXPixel + DAY_START_MILLIS) ;

                final Calendar cal = Calendar.getInstance() ;
                cal.setTime( date ) ;
                cal.add( Calendar.MILLISECOND, (int)numMillis ) ;
                date = cal.getTime() ;
            }
        }
        return date ;
    }

    /** Returns the value at the cross hair as a time value string. */
    private String getCrossHairValue( final int x, final int y ) {
        final Date date = getGraphXValue( x ) ;
        String retVal   = "" ;
        if( date != null ) {
            retVal = STConstant.DATE_TIME_FMT.format( getGraphXValue( x ) ) +
                     " [" + PRICE_DF.format( getGraphYValue( y ) ) + "]" ;
        }
        return retVal ;
    }

    /**
     * Converts the given time in milliseconds into a X pixel value on the
     * current chart based on UI characteristics of the chart panel. This
     * x pixel value is relative to the day marker, i.e. the value returned
     * by this function has to be added to the day baseline pixel to get the
     * absolute pixel value on the X axis.
     *
     * @param millis The time which needs to be transformed into pixel value
     *
     * @param dayStartMillis The number of milliseconds till 12:00:00 of the
     *        day we are considering.
     */
    private int getGraphXPixel( final long millis, final long dayStartMillis ) {

        final long deltaMillis = millis - dayStartMillis - DAY_START_MILLIS ;
        final int xPixels = (int)( deltaMillis / this.meta.valPerXPixel ) ;
        return xPixels ;
    }

    // ================== CROSS HAIR DRAW ROUTINES =============================
    public void mousePressed( final MouseEvent e ) { /*NOP*/ }

    public void mouseReleased( final MouseEvent e ) { /*NOP*/ }

    public void mouseClicked( final MouseEvent e ) { /*NOP*/ }

    /**
     * Mouse drag will have the same effect as mouse move under these circumstances.
     * This method delegates the processing to the mouse move logic with the
     * same parameter set.
     */
    public void mouseDragged( final MouseEvent e ) {
        mouseMoved( e ) ;
    }

    /**
     * Whenever the mouse moves, we redraw the cross hair at the new mouse
     * location. In case we were inside the graph rectangle during the last
     * movement - we erase the old cross hair.
     */
    @Override
    public void mouseMoved( final MouseEvent e ) {

        // If this dialog is not selected (on the top), we don't draw the cross
        // hair. If not done so, it has hairy effect of drawing the corss hair
        // on anything that lies on top.. how disgusting.
        if( !this.owner.getDialogManager().isSelected() ) {
            return ;
        }

        final Point point = e.getPoint() ;
        final Graphics2D g = ( Graphics2D )super.getGraphics() ;

        // Although I can't explain it - but once control came here before
        // paint was called and resulted in a null pointer since meta was null.
        // Till the time I explain, we just handle the scenario of a null meta
        if( this.meta == null ) {
            return ;
        }

        GraphicsContextStack.push( g, this.meta.graphRect ) ;
        g.setFont( CHART_AXIS_FONT ) ;

        if( this.meta.graphRect != null && this.meta.graphRect.contains( point ) ) {

            final int x = point.x - this.meta.graphRect.x ;
            final int y = point.y - this.meta.graphRect.y ;

            // If we are not painting the cross hair for the first time, then
            // erase the last cross hair.
            if( !(this.lastMouseX == -1 && this.lastMouseY == -1) ) {
                g.setXORMode( CROSS_HAIR_COLOR ) ;
                g.drawLine( this.lastMouseX, 0, this.lastMouseX, this.meta.graphRect.height ) ;
                g.drawLine( 0, this.lastMouseY, this.meta.graphRect.width, this.lastMouseY ) ;

                g.setXORMode( CROSS_HAIR_VALUE_COLOR ) ;
                g.drawString( getCrossHairValue( this.lastMouseX, this.lastMouseY ),
                              CROSS_HAIR_OFFSET, this.meta.graphRect.height-CROSS_HAIR_OFFSET ) ;
            }

            g.setXORMode( CROSS_HAIR_COLOR ) ;
            g.drawLine( x, 0, x, this.meta.graphRect.height ) ;
            g.drawLine( 0, y, this.meta.graphRect.width, y ) ;

            g.setXORMode( CROSS_HAIR_VALUE_COLOR ) ;
            g.drawString( getCrossHairValue( x, y ), CROSS_HAIR_OFFSET,
                          this.meta.graphRect.height-CROSS_HAIR_OFFSET ) ;

            this.lastMouseX = x ;
            this.lastMouseY = y ;
        }
        else {
            cleanCrossHair() ;
        }
        g.setPaintMode() ;
        GraphicsContextStack.pop( g ) ;
    }

    /** Clean the cross hair if we have left a dirty trace behind. */
    @Override
    public void mouseEntered( final MouseEvent e ) {
        cleanCrossHair() ;
    }

    /** Clean the cross hair if we have left a dirty trace behind. */
    @Override
    public void mouseExited( final MouseEvent e ) {
        cleanCrossHair() ;
    }

    /** Clean the old cross hair if and only if one exists. */
    private void cleanCrossHair() {
        final Graphics2D g = ( Graphics2D )super.getGraphics() ;

        if( !(this.lastMouseX == -1 && this.lastMouseY == -1) ) {
            GraphicsContextStack.push( g, this.meta.graphRect ) ;
            g.setFont( CHART_AXIS_FONT ) ;
            g.setXORMode( CROSS_HAIR_COLOR ) ;

            g.drawLine( this.lastMouseX, 0, this.lastMouseX, this.meta.graphRect.height ) ;
            g.drawLine( 0, this.lastMouseY, this.meta.graphRect.width, this.lastMouseY ) ;

            g.setXORMode( CROSS_HAIR_VALUE_COLOR ) ;
            g.drawString( getCrossHairValue( this.lastMouseX, this.lastMouseY ),
                          CROSS_HAIR_OFFSET, this.meta.graphRect.height-CROSS_HAIR_OFFSET ) ;

            this.lastMouseX = this.lastMouseY = -1 ;
            g.setPaintMode() ;
            GraphicsContextStack.pop( g ) ;
        }
    }
    // ================== CROSS HAIR DRAW ROUTINES END =========================
}
