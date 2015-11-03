package com.sandy.stocktracker.test.poc ;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class Scrap {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( Scrap.class ) ;

    /**
     * @param args
     */
    public static void main( final String[] args ) {

        //logger.debug( floor( 4.5, 0.2 ) ) ;
        //logger.debug( ceil( 4.6, 0.25 ) ) ;
        breakup( 23.5, 27.5, 345 ) ;
        //logger.debug( Math.nextUp( 4.6499999999999995 )) ;
    }

    private static Float[] breakup( final double min, final double max, final int numPixels ) {
        final ArrayList<Float> breakup = new ArrayList<Float>() ;
        final double[][] arr = {
                { 0.05, 0.0, 0.0, 0.0, Double.MAX_VALUE },
                { 0.10, 0.0, 0.0, 0.0, Double.MAX_VALUE },
                { 0.20, 0.0, 0.0, 0.0, Double.MAX_VALUE },
                { 0.25, 0.0, 0.0, 0.0, Double.MAX_VALUE },
                { 0.50, 0.0, 0.0, 0.0, Double.MAX_VALUE },
                { 1.0,  0.0, 0.0, 0.0, Double.MAX_VALUE },
                { 10.0, 0.0, 0.0, 0.0, Double.MAX_VALUE },
        } ;

        int optIndex = 0 ;
        double minVal   = Integer.MAX_VALUE ;

        for( int i=0; i<arr.length; i++ ) {
            arr[i][1] = ceil( min, arr[i][0] ) ;
            arr[i][2] = floor( max, arr[i][0] ) ;

            if( arr[i][2] > arr[i][1] ) {
                final int numPoints = (int)(( arr[i][2] - arr[i][1] )/arr[i][0]) + 1 ;
                arr[i][3] = numPixels/numPoints ;
                arr[i][4] = Math.abs( 10-arr[i][3] ) ;
                if( arr[i][4] < minVal ) {
                    optIndex = i ;
                    minVal = arr[i][4] ;
                }
            }
        }

        for( int i=0; i<arr.length; i++ ) {
            for( int j=0; j<5; j++ ) {
                System.out.print( arr[i][j] ) ;
                System.out.print( "  " ) ;
            }
            System.out.println() ;
        }

        logger.debug( "Optimum breakup = " + arr[optIndex][0] ) ;

        final double optDistance = arr[optIndex][0] ;
        double val = ceil( min, optDistance ) ;
        if( val == min ) {
            val += optDistance ;
        }
        while( val < max ) {
            breakup.add( new Float( (float)val ) ) ;
            val += optDistance ;
        }

        logger.debug( "Breakup = " + breakup ) ;

        return breakup.toArray( new Float[0] ) ;
    }

    private static double ceil( final double base, final double minGap ) {
        double result = base ;
        while( true ) {

            if( result % minGap < 0.009 ) {
                break ;
            }
            else {
                result = Math.nextUp( result + 0.001 ) ;
            }
        }
        return ((float)((int)(result*100)))/100 ;
    }

    private static double floor( final double base, final double minGap ) {
        double result = base ;
        while( true ) {

            if( result % minGap < 0.009 ) {
                break ;
            }
            else {
                result = Math.nextAfter( ( result - 0.001 ), Double.NEGATIVE_INFINITY ) ;
            }
        }
        return ((float)((int)(result*100)))/100 ;
    }
}
