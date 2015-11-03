/**
 * Creation Date: Aug 12, 2008
 */

package com.sandy.apps.pluto.ui;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import com.sandy.apps.pluto.shared.STConstant ;

/**
 * This interface extends the system wide {@link STConstant} interface to
 * provide constants specifically oriented towards the UI functionality of the
 * system.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface UIConstant extends STConstant {

    /** The base resource path for all images in the system. */
    String IMG_RES_PATH = BASE_RES_PATH + "images/" ;

    /** =================== The fixed range time intervals supported =========*/
    String RANGE_1D = "1D" ;
    String RANGE_2D = "2D" ;
    String RANGE_3D = "3D" ;
    String RANGE_4D = "4D" ;
    String RANGE_5D = "5D" ;
    String RANGE_2W = "2W" ;
    String RANGE_1M = "1M" ;
    String RANGE_3M = "3M" ;
    String RANGE_6M = "6M" ;
    String RANGE_1Y = "1Y" ;
    String RANGE_2Y = "2Y" ;
    String RANGE_5Y = "5Y" ;
    String RANGE_10Y= "10Y" ;

    /** The optimal number of pixels between two chart ticks. */
    int TICK_SEPARATION_PIXELS = 5 ;
    int TICK_PRINT_INTERVAL    = 5 ;

    /** =================== Fonts used by the application   ==================*/
    Font DLG_FONT                = new Font( "Tahoma", Font.PLAIN, 11 ) ;
    Font DLG_FONT_BOLD           = new Font( "Tahoma", Font.BOLD, 11 ) ;
    Font READ_TXT_AREA_FONT      = new Font( "Courier New", Font.PLAIN, 10 ) ;
    Font LOG_FONT                = new Font( "Tahoma", Font.PLAIN, 9 ) ;
    Font NEWS_DETAIL_FONT        = new Font( "Calibri", Font.PLAIN, 12 ) ;
    Font NEWS_SUMMARY_FONT       = new Font( "Tahoma", Font.PLAIN, 10 ) ;
    Font NEWS_SUMMARY_FONT_BOLD  = new Font( "Tahoma", Font.BOLD, 11 ) ;
    Font NEWS_TITLE_FONT         = new Font( "Tahoma", Font.PLAIN, 11 ) ;
    Font LOG_FONT_BOLD           = new Font( "Tahoma", Font.BOLD, 9 ) ;
    Font CHART_AXIS_FONT         = new Font( "Tahoma", Font.PLAIN, 9 ) ;
    Font CHART_TITLE_FONT        = new Font( "Tahoma", Font.BOLD, 9 ) ;
    Font CHART_PANEL_HDR_FONT    = new Font( "Tahoma", Font.BOLD, 10 ) ;

    /** =================== Images used by the application UI ================*/
    Image IMG_APP                   = UIHelper.getImage( "pluto.png" ) ;
    Image IMG_APP_CONNECTED         = UIHelper.getImage( "pluto_connected.png" ) ;
    Image IMG_APP_DISCONNECTED      = UIHelper.getImage( "pluto_disconnected.png" ) ;
    Image IMG_ONLINE                = UIHelper.getImage( "online.png" ) ;
    Image IMG_OFFLINE               = UIHelper.getImage( "offline.png" ) ;
    Image IMG_FLAG_RED              = UIHelper.getImage( "flag_red.png" ) ;
    Image IMG_ACCEPT                = UIHelper.getImage( "accept.png" ) ;
    Image IMG_CANCEL                = UIHelper.getImage( "cancel.png" ) ;
    Image IMG_ACCEPT_PRESSED        = UIHelper.getImage( "accept_pressed.png" ) ;
    Image IMG_CANCEL_PRESSED        = UIHelper.getImage( "cancel_pressed.png" ) ;
    Image IMG_CFG_WIZARD            = UIHelper.getImage( "config_wizard.png" ) ;
    Image IMG_START                 = UIHelper.getImage( "start.png" ) ;
    Image IMG_START_PRESSED         = UIHelper.getImage( "start_pressed.png" ) ;
    Image IMG_STOP                  = UIHelper.getImage( "stop.png" ) ;
    Image IMG_STOP_PRESSED          = UIHelper.getImage( "stop_pressed.png" ) ;
    Image IMG_EDIT                  = UIHelper.getImage( "edit.png" ) ;
    Image IMG_EDIT_PRESSED          = UIHelper.getImage( "edit_pressed.png" ) ;
    Image IMG_RESTART               = UIHelper.getImage( "restart.png" ) ;
    Image IMG_RESTART_PRESSED       = UIHelper.getImage( "restart_pressed.png" ) ;
    Image IMG_DELETE                = UIHelper.getImage( "delete.png" ) ;
    Image IMG_DELETE_PRESSED        = UIHelper.getImage( "delete_pressed.png" ) ;
    Image IMG_EXEC_NOW              = UIHelper.getImage( "exec_now.png" ) ;
    Image IMG_EXEC_NOW_PRESSED      = UIHelper.getImage( "exec_now_pressed.png" ) ;
    Image IMG_LOG_INFO              = UIHelper.getImage( "log_info.png" ) ;
    Image IMG_LOG_WARN              = UIHelper.getImage( "log_warn.png" ) ;
    Image IMG_LOG_ERROR             = UIHelper.getImage( "log_error.png" ) ;
    Image IMG_LOG_DELETE            = UIHelper.getImage( "log_delete.png" ) ;
    Image IMG_LOG_DELETE_PRESSED    = UIHelper.getImage( "log_delete_pressed.png" ) ;
    Image IMG_CHART_EDIT            = UIHelper.getImage( "chart_edit.png" ) ;
    Image IMG_CHART_EDIT_PRESSED    = UIHelper.getImage( "chart_edit_pressed.png" ) ;
    Image IMG_ITD_PANEL_EDIT        = UIHelper.getImage( "itd_panel_edit.png" ) ;
    Image IMG_ITD_PANEL_EDIT_PRESSED= UIHelper.getImage( "itd_panel_edit_pressed.png" ) ;
    Image IMG_SHOW_ITD_PANEL        = UIHelper.getImage( "itd_panel.png" ) ;
    Image IMG_SHOW_ITD_PANEL_PRESSED= UIHelper.getImage( "itd_panel_pressed.png" ) ;
    Image IMG_SHOW_VALUES           = UIHelper.getImage( "show_values.png" ) ;
    Image IMG_SHOW_VALUES_PRESSED   = UIHelper.getImage( "show_values_pressed.png" ) ;
    Image IMG_SHOW_CHART            = UIHelper.getImage( "charting.png" ) ;
    Image IMG_SHOW_CHART_PRESSED    = UIHelper.getImage( "charting_pressed.png" ) ;
    Image IMG_SHOW_CONTROL          = UIHelper.getImage( "chart_ctrl_panel.png" ) ;
    Image IMG_SHOW_CONTROL_PRESSED  = UIHelper.getImage( "chart_ctrl_panel_pressed.png" ) ;
    Image IMG_REFRESH               = UIHelper.getImage( "refresh.png" ) ;
    Image IMG_REFRESH_PRESSED       = UIHelper.getImage( "refresh_pressed.png" ) ;
    Image IMG_FETCH                 = UIHelper.getImage( "fetch.png" ) ;
    Image IMG_FETCH_PRESSED         = UIHelper.getImage( "fetch_pressed.png" ) ;
    Image IMG_MINIMIZE              = UIHelper.getImage( "minimize.png" ) ;
    Image IMG_MINIMIZE_PRESSED      = UIHelper.getImage( "minimize_pressed.png" ) ;
    Image IMG_SHOW_LOG_DLG          = UIHelper.getImage( "show_log_dialog.png" ) ;
    Image IMG_SHOW_INDEX_ITD_PANEL  = UIHelper.getImage( "index_itd_panel.png" ) ;
    Image IMG_CHART_CURSOR          = UIHelper.getImage( "chart_cursor.png" ) ;
    Image IMG_SHOW_VOL_CHART        = UIHelper.getImage( "volume_show.png" ) ;
    Image IMG_HIDE_VOL_CHART        = UIHelper.getImage( "volume_hide.png" ) ;
    Image IMG_RSS                   = UIHelper.getImage( "rss.png" ) ;
    Image IMG_RSS_BIG               = UIHelper.getImage( "rss_big.png" ) ;
    Image IMG_RSS_PRESSED           = UIHelper.getImage( "rss_pressed.png" ) ;
    Image IMG_BROWSER               = UIHelper.getImage( "world.png" ) ;
    Image IMG_BROWSER_PRESSED       = UIHelper.getImage( "world_pressed.png" ) ;
    Image IMG_FEED_ACTIVE           = UIHelper.getImage( "feed_active.png" ) ;
    Image IMG_FEED_INACTIVE         = UIHelper.getImage( "feed_inactive.png" ) ;
    Image IMG_SCRIP_EOD_TABLE       = UIHelper.getImage( "scrip_eod_table.png" ) ;
    Image IMG_SCRIP_EOD_TABLE_PRESSED=UIHelper.getImage( "scrip_eod_table_pressed.png" ) ;
    Image IMG_INDEX_LINKED          = UIHelper.getImage( "feed_active.png" ) ;
    Image IMG_WINDOW_LAYOUT         = UIHelper.getImage( "wnd_layout.png" ) ;
    Image IMG_PORTFOLIO             = UIHelper.getImage( "portfolio.png" ) ;
    Image IMG_PORTFOLIO_PRESSED     = UIHelper.getImage( "portfolio_pressed.png" ) ;
    Image IMG_PORTFOLIO_CURRENT     = UIHelper.getImage( "portfolio_current.png" ) ;
    Image IMG_PORTFOLIO_ALL         = UIHelper.getImage( "portfolio_all.png" ) ;
    Image IMG_TRADE_ADD             = UIHelper.getImage( "trade_add.png" ) ;
    Image IMG_TRADE_ADD_PRESSED     = UIHelper.getImage( "trade_add_pressed.png" ) ;
    Image IMG_TRADE_EDIT            = UIHelper.getImage( "trade_edit.png" ) ;
    Image IMG_TRADE_EDIT_PRESSED    = UIHelper.getImage( "trade_edit_pressed.png" ) ;

    /** =================== UI ACTION COMMANDS ===============================*/
    String AC_TRAY_ICON_DBLCLICK = "tray.icon.doubleclicked" ;
    String AC_WORK_OFFLINE       = "AC_WORK_OFFLINE" ;
    String AC_WORK_ONLINE        = "AC_WORK_ONLINE" ;
    String AC_CFG_WIZ_OK         = "AC_CFG_WIZ_OK" ;
    String AC_CFG_WIZ_CANCEL     = "AC_CFG_WIZ_CANCEL" ;
    String AC_SHOW_CFG_WIZ       = "AC_SHOW_CFG_WIZ" ;
    String AC_START              = "AC_START" ;
    String AC_STOP               = "AC_STOP" ;
    String AC_RESTART            = "AC_RESTART" ;
    String AC_DELETE             = "AC_DELETE" ;
    String AC_EXEC_NOW           = "AC_EXEC_NOW" ;
    String AC_EDIT               = "AC_EDIT" ;
    String AC_LOG_DELETE         = "AC_LOG_DELETE" ;
    String AC_SHOW_LOG_DIALOG    = "AC_SHOW_LOG_DIALOG" ;
    String AC_CHART_EDIT         = "AC_CHART_EDIT" ;
    String AC_ITD_PANEL_EDIT     = "AC_ITD_PANEL_EDIT" ;
    String AC_ITD_SEARCH_ENTRY   = "AC_ITD_SEARCH_ENTRY" ;
    String AC_SHOW_ITD_PANEL     = "AC_SHOW_ITD_PANEL" ;
    String AC_SHOW_VALUES        = "AC_SHOW_VALUES" ;
    String AC_SHOW_CHART         = "AC_SHOW_CHART" ;
    String AC_SHOW_CONTROL       = "AC_SHOW_CONTROL" ;
    String AC_TIME_RANGE         = "AC_TIME_RANGE_" ;
    String AC_TIME_RANGE_1D      = AC_TIME_RANGE + RANGE_1D ;
    String AC_TIME_RANGE_2D      = AC_TIME_RANGE + RANGE_2D ;
    String AC_TIME_RANGE_3D      = AC_TIME_RANGE + RANGE_3D ;
    String AC_TIME_RANGE_4D      = AC_TIME_RANGE + RANGE_4D ;
    String AC_TIME_RANGE_5D      = AC_TIME_RANGE + RANGE_5D ;
    String AC_TIME_RANGE_2W      = AC_TIME_RANGE + RANGE_2W ;
    String AC_TIME_RANGE_1M      = AC_TIME_RANGE + RANGE_1M ;
    String AC_TIME_RANGE_3M      = AC_TIME_RANGE + RANGE_3M ;
    String AC_TIME_RANGE_6M      = AC_TIME_RANGE + RANGE_6M ;
    String AC_TIME_RANGE_1Y      = AC_TIME_RANGE + RANGE_1Y ;
    String AC_TIME_RANGE_2Y      = AC_TIME_RANGE + RANGE_2Y ;
    String AC_TIME_RANGE_5Y      = AC_TIME_RANGE + RANGE_5Y ;
    String AC_TIME_RANGE_10Y     = AC_TIME_RANGE + RANGE_10Y ;
    String AC_REFRESH            = "AC_REFRESH" ;
    String AC_FETCH              = "AC_FETCH" ;
    String AC_MINIMIZE           = "AC_MINIMIZE" ;
    String AC_SHOW_HIDE_VOL_CHART= "AC_SHOW_HIDE_VOL_CHART" ;
    String AC_SHOW_INDEX_SEL     = "AC_SHOW_INDEX_SEL" ;
    String AC_SHOW_RSS_FRAME     = "AC_SHOW_RSS_FRAME" ;
    String AC_SHOW_BROWSER       = "AC_SHOW_BROWSER" ;
    String AC_NEWS_SEARCH        = "AC_NEWS_SEARCH" ;
    String AC_SCRIP_EOD_TABLE    = "AC_SCRIP_EOD_TABLE" ;
    String AC_WND_LAYOUT         = "AC_WND_LAYOUT" ;
    String AC_SHOW_PORTFOLIO     = "AC_SHOW_PORTFOLIO" ;
    String AC_TOGGLE_PORTFOLIO   = "AC_TOGGLE_PORTFOLIO" ;
    String AC_TRADE_ADD          = "AC_TRADE_ADD" ;
    String AC_TRADE_EDIT         = "AC_TRADE_EDIT" ;

    /** =================== POPUP MENU GROUPS ================================*/
    String TRAY_ICON_POPUP = "TRAY_ICON_POPUP" ;

    /** =================== UI RELATED CONSTANTS =============================*/
    Border EMPTY_BORDER = BorderFactory.createEmptyBorder( 0, 0, 0, 0 ) ;
    Insets EMPTY_INSETS = new Insets( 0, 0, 0, 0 ) ;

    /** =================== DATA FLAVORS SUPPORTED BY PLUTO FOR DnD ==========*/
    DataFlavor CHART_ENTITY_DATA_FLAVOR = new DataFlavor( DataFlavor.javaJVMLocalObjectMimeType, "ChartEntity" ) ;

    /** The cursor to use on charting panels. */
    Cursor CHART_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor( IMG_CHART_CURSOR, new Point(1,1), "CHART_CURSOR" ) ;
}
