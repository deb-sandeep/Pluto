/**
 * Creation Date: Aug 11, 2008
 */

package com.sandy.apps.pluto.shared;

import java.text.SimpleDateFormat ;

/**
 * This interface encapsulates the ST application constants which relate to
 * a wide variety of entities and no one in particular.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface STConstant {

    /** The base resource path for this application. */
    String BASE_RES_PATH = "/com/sandy/stocktracker/" ;

    /** The date format used for NSE EOD dates in the CSV files. */
    SimpleDateFormat DATE_FMT = new SimpleDateFormat( "dd-MMM-yyyy" ) ;

    /** The time format used for ITD title displays and general time displays. */
    SimpleDateFormat TIME_FMT = new SimpleDateFormat( "HH:mm:ss" ) ;

    /** The the expanded time format. */
    SimpleDateFormat DATE_TIME_FMT = new SimpleDateFormat( "dd-MMM-yyyy HH:mm:ss" ) ;

    /** The prefix for drop values indicating the drop value as scrip name. */
    String DROP_VAL_SCRIP = "SCRIP:" ;

    /** The application config key against which the install directory is specified. */
    String CFG_KEY_INSTALL_DIR = "pluto.install.dir" ;

    /** The application config key against which biz start hour is specified. */
    String CFG_KEY_NSE_BIZ_START_HR = "nse.business.start.time" ;

    /** The application config key against which biz end hour is specified. */
    String CFG_KEY_NSE_BIZ_END_HR = "nse.business.end.time" ;

    /** The number of days for which to show old news. */
    String CFG_KEY_NUM_OLD_DAYS_NEWS = "news.display.num.days" ;
}
