/**
 * Creation Date: Aug 3, 2008
 */

package com.sandy.apps.pluto.shared;
import java.io.File ;
import java.text.SimpleDateFormat ;
import java.util.Calendar ;
import java.util.Date ;
import java.util.StringTokenizer ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.shared.dto.Trade ;
import com.sandy.apps.pluto.shared.util.util.ConfigManager ;
import com.sandy.apps.pluto.shared.util.util.StringUtil ;

/**
 * A public static utility class which has string utility functions not found
 * in the common libraries.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class STUtils {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( STUtils.class ) ;

    public static SimpleDateFormat DF_dd_MM_yyyy = new SimpleDateFormat( "dd-MMM-yyyy" ) ;

    private static boolean isBizTimingLoaded = false ;
    private static long    bizStartMillis    = 0 ;
    private static long    bizEndMillis      = 0 ;

    // The config directory
    private static File configDir = null ;

    /** Private constructor. */
    private STUtils() {
        super() ;
    }

    /**
     * Returns the config directory for this installed instance of the program
     */
    public static File getConfigDir() {
        if( configDir == null ) {
            final ConfigManager cfgMgr = ConfigManager.getInstance() ;
            final File installDir = new File( cfgMgr.getString( STConstant.CFG_KEY_INSTALL_DIR ) ) ;
            configDir  = new File( installDir, "config" ) ;
        }
        return configDir ;
    }

    /**
     * Given a date, returns the NSE start of business day time as per the
     * nse.business.start.time configuration. For example 21-Oct-2008 12:32:23
     * would return 21-Oct-2008 09:55:00 if 09:55 is configured in the
     * application properties
     */
    public static Date getBizStartTime( final Date input ) {

        loadBizStartEndMillis() ;
        final Date dayStart = getStartOfDay( input ) ;
        return new Date( dayStart.getTime() + bizStartMillis  ) ;
    }

    /**
     * Given a date, returns the NSE end of business day time as per the
     * nse.business.end.time configuration. For example 21-Oct-2008 12:32:23
     * would return 21-Oct-2008 15:30:00 if 09:55 is configured in the
     * application properties
     */
    public static Date getBizEndTime( final Date input ) {

        loadBizStartEndMillis() ;
        final Date dayStart = getStartOfDay( input ) ;
        return new Date( dayStart.getTime() + bizEndMillis  ) ;
    }

    /** Loads the biz start and end milliseconds from configuration. */
    private static void loadBizStartEndMillis() {

        if( !isBizTimingLoaded ) {

            final ConfigManager cfgMgr = ConfigManager.getInstance() ;
            final String startHr = cfgMgr.getString( STConstant.CFG_KEY_NSE_BIZ_START_HR, "09:55" ) ;
            bizStartMillis = getMillisForHHmm( startHr.trim() ) ;

            final String endHr = cfgMgr.getString( STConstant.CFG_KEY_NSE_BIZ_END_HR, "15:30" ) ;
            bizEndMillis = getMillisForHHmm( endHr.trim() ) ;

            isBizTimingLoaded = true ;
        }
    }

    /** Converts a HH:mm format string into equivalent milliseconds. */
    private static long getMillisForHHmm( final String HHmm ) {

        long millis = 0 ;
        final StringTokenizer tokenizer = new StringTokenizer( HHmm, ":" ) ;

        final String hrStr = tokenizer.nextToken() ;
        final int    hrInt = Integer.parseInt( hrStr ) ;
        millis += hrInt * 60 * 60 * 1000 ;

        final String minStr = tokenizer.nextToken() ;
        final int    minInt = Integer.parseInt( minStr ) ;
        millis += minInt * 60 * 1000 ;

        return millis ;
    }

    /**
     * Truncates the input date to 12:00:00 AM of the same day
     */
    public static Date getStartOfDay( final Date input ) {
        final Calendar cal = Calendar.getInstance() ;
        cal.setTime( input ) ;
        cal.set( Calendar.HOUR_OF_DAY, 0 ) ;
        cal.set( Calendar.MINUTE, 0 ) ;
        cal.set( Calendar.SECOND, 0 ) ;
        cal.set( Calendar.MILLISECOND, 0 ) ;
        return cal.getTime() ;
    }

    /**
     * Truncates the input date to 11:59:59:999 PM of the same day
     */
    public static Date getEndOfDay( final Date input ) {
        final Calendar cal = Calendar.getInstance() ;
        cal.setTime( input ) ;
        cal.set( Calendar.HOUR_OF_DAY, 23 ) ;
        cal.set( Calendar.MINUTE, 59 ) ;
        cal.set( Calendar.SECOND, 59 ) ;
        cal.set( Calendar.MILLISECOND, 999 ) ;
        return cal.getTime() ;
    }

    /** Gets a calendar representing today 12:00:00 AM*/
    public static Calendar getToday() {
        final Calendar today = Calendar.getInstance() ;
        today.set( Calendar.HOUR_OF_DAY, 0 ) ;
        today.set( Calendar.MINUTE, 0 ) ;
        today.set( Calendar.SECOND, 0 ) ;
        today.set( Calendar.MILLISECOND, 0 ) ;
        return today ;
    }

    /**
     * Converts the given input string into an equivalent camel case
     * representation. For example an input string 'INDIAN RUPEES' will be
     * converted into 'Indian Rupees'.
     *
     * @param input The input string
     *
     * @return The camel case equivalent of the string.
     */
    public static String getCamelCase( final String input ) {
        final StringBuffer buffer = new StringBuffer() ;
        final StringTokenizer tok = new StringTokenizer( input, " " ) ;
        String nextToken = null ;

        while( tok.hasMoreTokens() ) {
            nextToken = tok.nextToken() ;
            if( nextToken.indexOf( '.' ) != -1 ) {
                buffer.append( nextToken ) ;
            }
            else {
                final String remaining = nextToken.substring( 1 ) ;
                if( !Character.isUpperCase( nextToken.charAt( 0 ) ) ) {
                    buffer.append( Character.toUpperCase( nextToken.charAt( 0 ) ) ) ;
                }
                else {
                    buffer.append( nextToken.charAt( 0 ) ) ;
                }
                buffer.append( remaining.toLowerCase() ) ;
            }
            buffer.append( ' ' ) ;
        }

        return buffer.toString().trim() ;
    }

    /**
     * Returns true if the application is operating in the development mode.
     * The application can do special handling of logic to facilitate development
     * process, for example not setting the main window to invisible during
     * boot-up etc. The development runtime is indicated by setting a system
     * property named "DEV_MODE" to true or TRUE.
     *
     * @return true if the application is operating in the dev mode, false
     *         otherwise.
     */
    public static boolean isDevMode() {

        boolean isDevMode = false ;
        final String devMode = System.getProperty( "DEV_MODE" ) ;
        if( StringUtil.isNotEmptyOrNull( devMode ) &&
            devMode.equalsIgnoreCase( "true" ) ) {
            isDevMode = true ;
        }
        return isDevMode ;
    }

    /**
     * This function computes the approximate brokerage charges based on the
     * parameters specified. Brokerage for ICICI Direct is computed based on
     * the following rules:
     * <ol>
     *   <li>Brokerage is calculated on a per share basis and is rounded off to
     *       the fourth decimal.</li>
     *   <ul>
     *     <li>For CASH transactions with less than 1000000 Rs, 0.75% of
     *         trade value is charged as brokerage for each leg.</li>
     *     <li>In case of CASH intra day square off, brokerage for second
     *         leg is waived off</li>
     *     <li>In case of MARGIN trading with less than 10 Crores, 0.05% of the
     *         trade value will be charged as brokerage in each leg.</li>
     *   </ul>
     *   <li>SERVICE TAX OF 10.30 % of BROKERAGE will be charged additional.
     *       It is calculated per share and rounded off to four  decimals.</li>
     *   <li>SECURITIES TRANSACTION TAX (STT) EQUITY - STT is calculated on the
     *       Weighted Average Price of the client for a particular day. This is
     *       also calculated on the value and rounded of the second decimal.</li>
     *   <li>STT at the rate of 0.125% of turnover will be charged in addition
     *       to the Brokerage on all delivery trades.</li>
     *   <li>STT at the rate of 0.025% of turnover will be charged in addition
     *       to the Brokerage on sell leg of all non-delivery trades.</li>
     *   <li>TRANSACTION CHARGES at the rate of 0.0035% of turnover in NSE and
     *       0.0034% of turnover in BSE will be charged additional. The
     *       transaction charge is calculated on per share basis and rounded off
     *       to four decimals.</li>
     *   <li>STAMP DUTY at the rate of 0.01% on delivery based turnover and
     *       0.002% on non-delivery based turnover will be charged in addition
     *       to the charges mentioned above. The Stamp Duty is calculated on
     *       per share basis and rounded off to four decimals</li>
     * </ol>
     *
     * Note that for CASH and BTST, ICICI Direct charges a minimum of 25 rupees
     * brokerage per trade or 2.5% of trade value which ever is lower. For
     * margin trades the minimum brokerage is 15 rupees per trade.
     *
     * All said and done this is this function gives an approximate value
     * of the brokerage. The user can always edit the brokerage value later
     * by editing the trade.
     *
     * @param numUnits The number of units traded
     * @param unitPrice The unit price of the stock traded
     * @param tradeType The trade type. Can be CASH, MARGIN or SPOT
     * @param isBuy A boolean value indicating if the brokerage is to be
     *        computed for a buy trade
     * @param buyTradeDate If this is a sell trade, (isBuy = false), this
     *        parameter indicates the date in which the corresponding buy
     *        trade was done. If this parameter is set to null, the logic
     *        would assume that the trade is not for intra day square off. In
     *        case of a buy trade, this parameter is the buy date.
     * @param sellTradeDate If the trade is a sell trade, this value is the
     *        date of the sell trade.
     *
     * @return The approximate brokerage charges for this trade.
     */
    public static double computeBrokerage( final int numUnits,
                                           final double unitPrice,
                                           final String tradeType,
                                           final boolean isBuy,
                                           final Date buyTradeDate,
                                           final Date sellTradeDate ) {

        final double tradeValue = numUnits * unitPrice ;
        double brokerage  = 0.0 ;
        double serviceTax = 0.0 ;
        double secTxnTax  = 0.0 ;
        double exTxnChgs  = 0.0 ;
        double stampDuty  = 0.0 ;

        // Calculate brokerage. Impose the minimum value. Note that for CASH and
        // BTST, ICICI Direct charges a minimum of 25 Rupees brokerage per trade
        // or 2.5% of trade value which ever is lower. For margin trades the
        // minimum brokerage is 15 rupees per trade.
        if( tradeType.equals( Trade.CASH ) ) {
            brokerage = (0.75/100) * tradeValue ;
            if( !isBuy ) {
                // If sell trade
                if( buyTradeDate != null && sellTradeDate != null ) {
                    // If we have an intra day square off, the brokerage is
                    // waived off
                    if( sellTradeDate.getTime() - buyTradeDate.getTime() <
                        ( 24*60*60*100 ) ) {
                        brokerage = 0.0 ;
                    }
                }
            }

            // Impose the minimum value
            if( brokerage != 0.0 ) {
                double temp = (2.5/100)*tradeValue ;
                temp = ( 25 < temp ) ? 25 : temp ;
                brokerage = ( brokerage < temp ) ? temp : brokerage ;
            }
        }
        else if( tradeType.equals( Trade.MARGIN ) ) {
            brokerage = (0.05/100) * tradeValue ;
            brokerage = ( brokerage < 15 ) ? 15 : brokerage ;
        }
        else {
            throw new UnsupportedOperationException( "SPOT trade not supported" ) ;
        }

        // Calculate service tax
        serviceTax = (10.3/100)*brokerage ;

        // Calculate STT
        if( tradeType.equals( Trade.CASH ) ) {
            secTxnTax = (0.125/100)*brokerage ;
        }
        else {
            secTxnTax = (0.025/100)*brokerage ;
        }

        // Calculate exchange transaction tax
        exTxnChgs = (0.0035/100)*tradeValue ;

        // Calculate stamp duty
        // STAMP DUTY at the rate of 0.01% on delivery based turnover and
        // 0.002% on non-delivery based turnover will be charged in addition
        // to the charges mentioned above. The Stamp Duty is calculated on
        // per share basis and rounded off to four decimals</li>
        if( tradeType.equals( Trade.CASH ) ) {
            stampDuty = (0.01/100)*tradeValue ;
        }
        else {
            stampDuty = (0.002/100)*tradeValue ;
        }

        final double total = brokerage + serviceTax + secTxnTax  +
                             exTxnChgs + stampDuty  ;
        return total ;
    }
}
