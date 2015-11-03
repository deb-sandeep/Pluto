/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 15, 2008
 */

package com.sandy.apps.pluto.ui.svc;
import java.awt.Dimension;
import java.beans.PropertyVetoException;
import java.util.Date;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.StockTracker ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.dto.Trade ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoFrameType ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoInternalFrame ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.ChartingPanel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.indexitdsummary.IndexITDSummaryPanel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.jobsummary.JobSummaryPanel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.log.LogDisplayPanel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.news.NewsPanel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.portfolio.EquityBuySellDialog ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.portfolio.PortfolioSummaryPanel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.proxy.ProxyCfgPanel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.scripeodsummary.ScripEODSummaryPanel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.scripitdsummary.ScripITDSummaryPanel ;

/**
 * This class encapsulates all the view related services which needs to be looked
 * up via configuration. Typical examples are logic invocations from the
 * selection of system menu items.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class STViewService {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( STViewService.class ) ;

    /** A reference to the log window. */
    private PlutoInternalFrame logDialog = null ;

    /** A reference to the news window. */
    private PlutoInternalFrame newsWindow = null ;

    /** A reference to the equity buy/sell dialog. */
    private PlutoInternalFrame equityBSDialog = null ;

    // late initialization
    private EquityBuySellDialog bsPanel = null ;

    /** Public constructor for ease of creation by the DI engine. */
    public STViewService() {
        super() ;
    }

    /** Displays the configuration wizard. */
    public void showConfigWizard() throws STException {

        final ProxyCfgPanel proxyCfg  = new ProxyCfgPanel( "Proxy" ) ;
        final JobSummaryPanel taskCfg = new JobSummaryPanel() ;

        final PlutoInternalFrame wizard = new PlutoInternalFrame( "System preferences",
                                  JFrame.DISPOSE_ON_CLOSE,
                                  PlutoFrameType.CONFIG_FRAME, proxyCfg, taskCfg ) ;

        wizard.setSize( 330, 300 ) ;

        StockTracker.MAIN_FRAME.addInternalFrame( wizard, true ) ;
    }

    /** Displays the log window. */
    public void showLogWindow() {
        if( this.logDialog == null ) {
            try {
                final LogDisplayPanel logPanel = new LogDisplayPanel() ;
                this.logDialog = new PlutoInternalFrame( "Log window", JFrame.HIDE_ON_CLOSE,
                                                    PlutoFrameType.LOG_FRAME, logPanel ) ;
                this.logDialog.setSize( 350, 400 ) ;
                this.logDialog.setVisible( false ) ;
                this.logDialog.setResizable( true ) ;
                StockTracker.MAIN_FRAME.addInternalFrame( this.logDialog, true ) ;
            }
            catch ( final STException e ) {
                logger.error( "Could not create the log panel", e ) ;
            }
        }

        if( !this.logDialog.isVisible() ) {
            this.logDialog.setVisible( true ) ;
        }
    }

    /** Hides the log window via programatic action. */
    public void hideLogWindow() {
        if( this.logDialog != null ) {
            this.logDialog.setVisible( false ) ;
        }
    }

    /** Displays the visualization charting window. */
    public void showChart() throws STException {

        final ChartingPanel panel  = new ChartingPanel() ;
        final PlutoInternalFrame dialog = new PlutoInternalFrame( "Charting",
                                              JFrame.DISPOSE_ON_CLOSE,
                                              PlutoFrameType.CHART_FRAME, panel ) ;

        dialog.setSize( ChartingPanel.DEFAULT_WIDTH, ChartingPanel.DEFAULT_HEIGHT ) ;
        dialog.setResizable( true ) ;

        StockTracker.MAIN_FRAME.addInternalFrame( dialog, true ) ;
    }

    /**
     * This method is called when the user intends to see the ITD data for
     * scrips, for whom the system is fetching the ITD data asynchronously. This
     * includes the index based scrip ITD fetch and high resolution ITD data
     * fetch.
     * <p>
     * The user can open multiple ITD panels and choose to filter the scrips
     * in individual panels.
     *
     * @throws STException If an unanticipated exception was encountered while
     *         opening the ITD panel.
     */
    public void showITDPanel() throws STException {

        final ScripITDSummaryPanel   panel  = new ScripITDSummaryPanel( "ITD" ) ;
        final PlutoInternalFrame     dialog = new PlutoInternalFrame( "ITD",
                                              JFrame.DISPOSE_ON_CLOSE,
                                              PlutoFrameType.SCRIP_ITD_FRAME, panel ) ;

        dialog.setSize( panel.getPreferredWidth(), 450 ) ;
        dialog.setResizable( true ) ;

        StockTracker.MAIN_FRAME.addInternalFrame( dialog, true ) ;
    }

    /**
     * This method is called when the user intends to see the ITD data for
     * indices, for whom the system is fetching the ITD data asynchronously.
     * <p>
     *
     * @throws STException If an unanticipated exception was encountered while
     *         opening the ITD panel.
     */
    public void showIndexITDPanel() throws STException {

        final IndexITDSummaryPanel panel = new IndexITDSummaryPanel( "Index" ) ;
        final PlutoInternalFrame  dialog = new PlutoInternalFrame( "Index",
                                              JFrame.DISPOSE_ON_CLOSE,
                                              PlutoFrameType.INDEX_ITD_FRAME, panel ) ;

        dialog.setSize( 300, 180 ) ;
        dialog.setResizable( true ) ;

        StockTracker.MAIN_FRAME.addInternalFrame( dialog, true ) ;
    }

    /**
     * This method is called when the user chooses to see the RSS window.
     *
     * @throws STException If an unanticipated exception was encountered while
     *         opening the RSS reader frame.
     */
    public void showRSSFrame() throws STException {
        if( this.newsWindow == null ) {
            try {
                final NewsPanel newsPanel = new NewsPanel() ;
                this.newsWindow = new PlutoInternalFrame( "News", JFrame.HIDE_ON_CLOSE,
                                                     PlutoFrameType.NEWS_FRAME, newsPanel ) ;
                this.newsWindow.setSize( 650, 380 ) ;
                this.newsWindow.setVisible( false ) ;
                this.newsWindow.setResizable( true ) ;
                StockTracker.MAIN_FRAME.addInternalFrame( this.newsWindow, true ) ;
            }
            catch ( final STException e ) {
                logger.error( "Could not create the news panel", e ) ;
            }
        }

        if( !this.newsWindow.isVisible() ) {
            this.newsWindow.setVisible( true ) ;
        }
        else {
            this.newsWindow.toFront() ;
            try {
                this.newsWindow.setSelected( true ) ;
            }
            catch ( final PropertyVetoException e ) {
                logger.debug( "Error making the news window selected", e ) ;
            }
        }
    }

    /**
     * This method is called when the user chooses to see the EOD window for
     * the scrips tracked by Pluto. The scrip EOD table, shows only the EOD
     * values and can lead the user for a detailed view on the scrips.
     *
     * @throws STException If an unanticipated exception was encountered while
     *         opening the RSS reader frame.
     */
    public void showScripEODPanel() throws STException {

        final ScripEODSummaryPanel   panel  = new ScripEODSummaryPanel( "EOD" ) ;
        final PlutoInternalFrame dialog = new PlutoInternalFrame( "EOD",
                                              JFrame.DISPOSE_ON_CLOSE,
                                              PlutoFrameType.SCRIP_EOD_FRAME, panel ) ;

        dialog.setSize( panel.getPreferredWidth(), 450 ) ;
        dialog.setResizable( true ) ;

        StockTracker.MAIN_FRAME.addInternalFrame( dialog, true ) ;
    }

    /**
     * This method is called when the user chooses to enter his trade details
     * into Pluto. This method shows the equity buy sell dialog. There is only
     * one instance of the buy/sell dialog in the system. This method ensures
     * that the old dialog state is cleaned and new state loaded before display.
     *
     * @param stockCode The NSE stock code that the user wants to trade on
     * @param isBuy A boolean parameter indicating if the dialog is being
     *        opened to capture a buy trade.
     *
     * @throws STException If an unanticipated exception was encountered while
     *         opening the RSS reader frame.
     */
    public void showEquityBuySellDialog( final String stockCode,
                                         final boolean isBuy )
        throws STException {
        if( this.equityBSDialog == null ) {

            // late initialization
            this.bsPanel = new EquityBuySellDialog( "Equity Buy/Sell" ) ;

            this.equityBSDialog = new PlutoInternalFrame( "Equity Buy/Sell",
                                                     JFrame.HIDE_ON_CLOSE,
                                                     PlutoFrameType.TRADE_EDIT_FRAME,
                                                     this.bsPanel ) ;
            this.equityBSDialog.setSize( 275, 250 ) ;
            this.equityBSDialog.setResizable( false ) ;
            StockTracker.MAIN_FRAME.addInternalFrame( this.equityBSDialog, true ) ;
        }

        final Trade trade = new Trade() ;
        trade.setSymbol( stockCode ) ;
        trade.setBuy( isBuy ) ;
        trade.setDate( new Date() ) ;
        trade.setTradeId( -1 ) ;
        trade.setUnitPrice( 0.0 ) ;
        trade.setUnits( 0 ) ;

        this.bsPanel.reInitPanel( trade ) ;

        if( !this.equityBSDialog.isVisible() ) {
            this.equityBSDialog.setVisible( true ) ;
        }
    }

    /**
     * This method is called when the user chooses to see the portfolio of
     * stocks that he has invested in. Please note that there can be multiple
     * instances of portfolio panel opened by the user.
     *
     * @throws STException If an unanticipated exception was encountered while
     *         opening the portfolio panel.
     */
    public void showPortfolioPanel() throws STException {

        final PortfolioSummaryPanel panel  = new PortfolioSummaryPanel( "Portfolio" ) ;
        final PlutoInternalFrame dialog = new PlutoInternalFrame( "Portfolio",
                                              JFrame.DISPOSE_ON_CLOSE,
                                              PlutoFrameType.PORTFOLIO_FRAME, panel ) ;

        dialog.setSize( panel.getPreferredWidth(), 170 ) ;
        dialog.setResizable( true ) ;

        StockTracker.MAIN_FRAME.addInternalFrame( dialog, true ) ;
    }

    /**
     * This method is called when the user chooses to set up a fresh layout
     * of windows on the desktop. The layout most typically used comprises of
     * the following:
     *
     * a) The Scrip ITD panel attached to the east desktop border
     * b) The Index ITD panel attached to the east desktop border, below the Scrip ITD panel
     * c) The Scrip EOD panel attached to the east desktop border, below te Index ITD panel
     * d) Rest of the desktop arranged with 3x3 chart panels
     */
    public void layoutCleanWorkspace() throws STException {

        final Dimension desktopSz = StockTracker.MAIN_FRAME.getDesktopSize() ;
        final int desktopWidth = desktopSz.width ;
        final int desktopHeight= desktopSz.height ;

        final PortfolioSummaryPanel portPanel = new PortfolioSummaryPanel( "Portfolio" ) ;
        final ScripITDSummaryPanel  itdPanel  = new ScripITDSummaryPanel( "ITD" ) ;
        final IndexITDSummaryPanel  indPanel  = new IndexITDSummaryPanel( "Index" ) ;

        PlutoInternalFrame prtDlg = null ;
        PlutoInternalFrame itdDlg = null ;
        PlutoInternalFrame indDlg = null ;

        prtDlg = new PlutoInternalFrame( "Portfolio", JFrame.DISPOSE_ON_CLOSE,
                                    PlutoFrameType.SCRIP_EOD_FRAME, portPanel ) ;

        itdDlg = new PlutoInternalFrame( "ITD", JFrame.DISPOSE_ON_CLOSE,
                                    PlutoFrameType.SCRIP_ITD_FRAME, itdPanel ) ;

        indDlg = new PlutoInternalFrame( "ITD", JFrame.DISPOSE_ON_CLOSE,
                                    PlutoFrameType.SCRIP_ITD_FRAME, indPanel ) ;

        final int width   = itdPanel.getPreferredWidth() ;
        final int indDlgHeight = 180 ;
        final int itdDlgHeight = (( desktopHeight - indDlgHeight ) * 2)/3 ;
        final int prtDlgHeight = (( desktopHeight - indDlgHeight ) * 1)/3 ;
        final int indDlgY      = desktopHeight - 180 ;
        final int indDlgX      = desktopWidth  - width ;

        indDlg.setBounds( indDlgX, indDlgY, width, 180 ) ;
        itdDlg.setBounds( indDlgX, 0, width, itdDlgHeight ) ;
        prtDlg.setBounds( indDlgX, itdDlgHeight, width, prtDlgHeight ) ;

        StockTracker.MAIN_FRAME.addInternalFrame( indDlg, false ) ;
        StockTracker.MAIN_FRAME.addInternalFrame( prtDlg, false ) ;
        StockTracker.MAIN_FRAME.addInternalFrame( itdDlg, false ) ;

        final int chartHeight = desktopHeight/3 ;
        final int chartWidth  = ( desktopWidth - width ) / 3 ;

        for( int row=0; row<3; row++ ) {
            for( int col=0; col<3; col++ ) {
                final PlutoInternalFrame chart = new PlutoInternalFrame( "Charting",
                                                    JFrame.DISPOSE_ON_CLOSE,
                                                    PlutoFrameType.CHART_FRAME,
                                                    new ChartingPanel( true ) ) ;

                chart.setBounds( row*chartWidth, col*chartHeight,
                                 chartWidth, chartHeight ) ;

                StockTracker.MAIN_FRAME.addInternalFrame( chart, false ) ;
            }
        }
    }
}
