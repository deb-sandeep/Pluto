/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 12, 2008
 */

package com.sandy.apps.pluto.ui;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * This static utility class encapsulates user interface related helper methods.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class UIHelper implements UIConstant {

    //private static final Logger logger = Logger.getLogger( UIHelper.class ) ;

    /** Grades of color for varying levels of profit or loss. */
    private static final Color LOSS_GRADE_0 = new Color( 255, 240, 251 ) ;
    private static final Color LOSS_GRADE_1 = new Color( 255, 210, 242 ) ;
    private static final Color LOSS_GRADE_2 = new Color( 255, 179, 232 ) ;
    private static final Color LOSS_GRADE_3 = new Color( 254, 139, 220 ) ;
    private static final Color LOSS_GRADE_4 = new Color( 254, 84,  203 ) ;
    private static final Color LOSS_GRADE_5 = new Color( 248, 1,   173 ) ;

    private static final Color PROFIT_GRADE_0 = new Color( 236, 255, 239 ) ;
    private static final Color PROFIT_GRADE_1 = new Color( 210, 255, 217 ) ;
    private static final Color PROFIT_GRADE_2 = new Color( 170, 255, 183 ) ;
    private static final Color PROFIT_GRADE_3 = new Color( 119, 255, 139 ) ;
    private static final Color PROFIT_GRADE_4 = new Color( 60,  255, 89  ) ;
    private static final Color PROFIT_GRADE_5 = new Color( 0,   253, 38  ) ;

    /** Background color for the even rows. */
    public static final Color EVEN_ROW_COLOR = new Color( 240, 240, 240 ) ;

    /** The grid color. */
    public static final Color GRID_COLOR = new Color( 243, 243, 243 ) ;

    /** Background color for the odd rows. */
    public static final Color ODD_ROW_COLOR = Color.white ;

    /** Private constructor to enforce static utility class pattern. */
    private UIHelper() {
        super() ;
    }

    /**
     * Returns an Image instance for the given image name. It is assumed that
     * the image is in the class path under the IMG_RES_PATH resource path.
     *
     * @param imageName The name of the image
     *
     * @return An image instance created from the image file.
     */
    public static Image getImage( final String imageName ) {
        final URL imgURL  = UIHelper.class.getResource( IMG_RES_PATH + imageName ) ;
        final Image image = Toolkit.getDefaultToolkit().getImage( imgURL );
        return image ;
    }

    /**
     * Returns an Icon instance for the given image name. It is assumed that
     * the image is in the class path under the IMG_RES_PATH resource path.
     *
     * @param imageName The name of the image
     *
     * @return An image instance created from the image file.
     */
    public static Icon getIcon( final String imageName ) {
        return new ImageIcon( getImage( imageName ) ) ;
    }

    /**
     * Returns true if both the string values are equal. This function takes
     * care of the null values of the strings.
     */
    public static boolean isEqual( final String str1, final String str2 ) {

        boolean valid = false ;
        if( str1 == null ) {
            if( str2 == null ) {
                valid = true ;
            }
        }
        else {
            if( str2 != null ) {
                valid = str1.equals( str2 ) ;
            }
        }
        return valid ;
    }

    /**
     * Breaks up the specified range optimally considering the number of pixels
     * and the optimum number of pixels for inter tick separation. The breakup
     * is in steps of 0.05, 0.1, 0.2, 0.25, 0.5, 1.0 and 10.0. This function
     * returns an array of Float values, each signifying the optimum marker.
     *
     * @param min The minimum value of the range.
     * @param max The max value of the range
     * @param numPixels The number of pixels over which the range has to be spread
     *
     * @return An array of Float instances representing the optimum markers.
     */
    public static Float[] breakup( final double min, final double max, final int numPixels ) {

        final ArrayList<Float> breakup = new ArrayList<Float>() ;
        // Index 0 - The step value (constant)
        // Index 1 - The ceil of the minumum value as per the step value
        // Index 2 - The floor of the max value as per the step value
        // Index 3 - The number of points as per this step value
        // Index 4 - The absolute of OPTIMUM_TICK_SEPARATION minus the number of ticks
        final double[][] arr = {
                { 0.05, 0.0, 0.0, 0.0, Double.MAX_VALUE },
                { 0.10, 0.0, 0.0, 0.0, Double.MAX_VALUE },
                { 0.20, 0.0, 0.0, 0.0, Double.MAX_VALUE },
                { 0.25, 0.0, 0.0, 0.0, Double.MAX_VALUE },
                { 0.50, 0.0, 0.0, 0.0, Double.MAX_VALUE },
                { 1.0,  0.0, 0.0, 0.0, Double.MAX_VALUE },
                { 2.0,  0.0, 0.0, 0.0, Double.MAX_VALUE },
                { 5.0,  0.0, 0.0, 0.0, Double.MAX_VALUE },
                { 10.0, 0.0, 0.0, 0.0, Double.MAX_VALUE },
                { 20.0, 0.0, 0.0, 0.0, Double.MAX_VALUE },
                { 50.0, 0.0, 0.0, 0.0, Double.MAX_VALUE },
                {100.0, 0.0, 0.0, 0.0, Double.MAX_VALUE },
                {150.0, 0.0, 0.0, 0.0, Double.MAX_VALUE },
                {200.0, 0.0, 0.0, 0.0, Double.MAX_VALUE },
                {250.0, 0.0, 0.0, 0.0, Double.MAX_VALUE },
                {500.0, 0.0, 0.0, 0.0, Double.MAX_VALUE },
               {1000.0, 0.0, 0.0, 0.0, Double.MAX_VALUE },
        } ;

        double minVal   = Integer.MAX_VALUE ;
        double optDist  = 0 ;

        for( int i=0; i<arr.length; i++ ) {
            arr[i][1] = ceil( min, arr[i][0] ) ;
            arr[i][2] = floor( max, arr[i][0] ) ;

            if( arr[i][2] > arr[i][1] ) {
                final int numPoints = (int)(( arr[i][2] - arr[i][1] )/arr[i][0]) + 1 ;
                arr[i][3] = numPixels/numPoints ;
                arr[i][4] = Math.abs( UIConstant.TICK_SEPARATION_PIXELS-arr[i][3] ) ;
                if( arr[i][4] <= minVal ) {
                    optDist = arr[i][0] ;
                    minVal = arr[i][4] ;
                }
            }
        }

        double val = ceil( min, optDist ) ;
        if( val == min ) {
            val = ( val < 0 ) ? ( val - optDist ) : ( val + optDist ) ;
        }

        while( val < max ) {
            breakup.add( new Float( (float)val ) ) ;
            val += optDist ;
        }

        return breakup.toArray( new Float[0] ) ;
    }

    public static double ceil( final double base, final double minGap ) {
        double result = base ;
        while( true ) {
            if( Math.abs( result ) % minGap < 0.009 ) {
                break ;
            }
            else {
                result = Math.nextUp( result + 0.001 ) ;
            }
        }
        return ((float)((int)(result*100)))/100 ;
    }

    public static double floor( final double base, final double minGap ) {
        double result = base ;
        while( true ) {
            if( Math.abs( result ) % minGap < 0.009 ) {
                break ;
            }
            else {
                result = Math.nextAfter( ( result - 0.001 ), Double.NEGATIVE_INFINITY ) ;
            }
        }
        return ((float)((int)(result*100)))/100 ;
    }

    /** Returns a color gradation of the percentage change. */
    public static Color getProfitLossHighlight( final double pctChange ) {

        Color retVal = Color.white ;

        if( pctChange < 0 ) {

            if( pctChange < -10 ) {
                retVal = LOSS_GRADE_5 ;
            }
            else if( pctChange < -8 ) {
                retVal = LOSS_GRADE_4 ;
            }
            else if( pctChange < -6 ) {
                retVal = LOSS_GRADE_3 ;
            }
            else if( pctChange < -4 ) {
                retVal = LOSS_GRADE_2 ;
            }
            else if( pctChange < -2 ) {
                retVal = LOSS_GRADE_1 ;
            }
            else {
                retVal = LOSS_GRADE_0 ;
            }
        }
        else if( pctChange > 0 ) {

            if( pctChange > 10 ) {
                retVal = PROFIT_GRADE_5 ;
            }
            else if( pctChange > 8 ) {
                retVal = PROFIT_GRADE_4 ;
            }
            else if( pctChange > 6 ) {
                retVal = PROFIT_GRADE_3 ;
            }
            else if( pctChange > 4 ) {
                retVal = PROFIT_GRADE_2 ;
            }
            else if( pctChange > 1 ) {
                retVal = PROFIT_GRADE_1 ;
            }
            else {
                retVal = PROFIT_GRADE_0 ;
            }
        }

        return retVal ;
    }
}
