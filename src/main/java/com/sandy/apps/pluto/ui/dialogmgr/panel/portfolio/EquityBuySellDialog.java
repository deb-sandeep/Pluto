/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 15, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.portfolio ;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.Trade ;
import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.dialogmgr.AbstractPlutoFramePanel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.scripeodsummary.ScripEODValueCache ;

/**
 * This dialog hosts the {@link EquityBuySellDialogUI} which enables the user to
 * capture the trade details.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class EquityBuySellDialog extends AbstractPlutoFramePanel
    implements UIConstant, ActionListener {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 0xCAFECAFE ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( EquityBuySellDialog.class ) ;

    /** The buy/sell entry panel */
    private final EquityBuySellDialogUI buySellPanel ;

    /**
     * A reference to the EOD value cache, which is used to populate the
     * equity codes.
     */
    final ScripEODValueCache eodCache = ScripEODValueCache.getInstance() ;

    /** Public constructor. */
    public EquityBuySellDialog( final String name ) {
        super( name ) ;
        this.buySellPanel = new EquityBuySellDialogUI( this ) ;
    }

    /**
     * Returns the icons that need to be displayed in the wizard toolbar when
     * this panel is selected.
     * <ul>
     *  <li>Element 0 [String][M] - Action command for the button</li>
     *  <li>Element 1 [Image] [M] - Button image</li>
     *  <li>Element 2 [Image] [O] - Pressed image for button</li>
     *  <li>Element 3 [Image] [O] - Roll over description for button[</li>
     * </ul>
     * <p>
     *
     * @return A two dimensional array of Objects, with each row having two
     *         elements.
     */
    public Object[][] getPanelIcons() {
        return new Object[][] {
            { AC_CFG_WIZ_OK, IMG_ACCEPT, IMG_ACCEPT_PRESSED, "Enter trade" },
        } ;
    }

    /**
     * This method is invoked when the user selects any of the panel specific
     * buttons. Depending upon the action initiated by the user, this method
     * delegates the processing to the appropriate method.
     */
    @Override
    public void actionPerformed( final ActionEvent e ) {
        // This dialog does not have any dialog specific actions and hence
        // this method is void.
    }

    /**
     * Initializes the nature of this panel. This method is called upon this
     * class during the setting up of the UI and gives a chance to initialize
     * or tweak the UI of this panel prior to display.
     */
    @Override
    public void initializeData() throws STException {
        setLayout( new BorderLayout() ) ;
        add( this.buySellPanel, BorderLayout.CENTER ) ;
    }

    /**
     * This function is called upon this class to validate the user input. If
     * the input is not proper, this function highlights the UI field with the
     * invalid input.
     *
     * @return A null value in case the input is valid, a not null string
     *         indicating the validation error if the input is not found
     *         proper.
     */
    public List<String> validateUserInput() {
        return this.buySellPanel.validateUserInput() ;
    }

    /**
     * This method is invoked on the child panels when the dialog is being
     * disposed as a result of a cancel operation chosen by the user. Subclasses
     * can implement this function to perform cleanup logic like de-registering
     * from the event bus etc.
     */
    public void destroy() {
        // We reuse this dialog by reinitializing it every time a trade has
        // to be edited or entered and hence the lifetime of this dialog is
        // equal to the lifetime of the application.
    }

    /**
     * This method is called by the base framework when the user clicks the
     * OK button and if the user input is valid. We use this opportunity to
     * save the data entered by the user.
     */
    public void save() throws STException {
        final Trade trade = this.buySellPanel.getTrade() ;
        logger.debug( "Saving trade " + trade ) ;
        if( trade != null ) {
            if( trade.getTradeId() == -1 ) {
                ServiceMgr.getTradeDAO().add( trade ) ;
            }
            else {
                ServiceMgr.getTradeDAO().update( trade ) ;
            }
        }
        getDialogManager().setVisible( false ) ;
        this.buySellPanel.reInitComponents( null ) ;
    }

    /** Returns true if the input is dirty - we return true always. */
    public boolean isDirty() {
        return true ;
    }

    /**
     * Reinitializes the UI based on the provided values.
     */
    public void reInitPanel( final Trade trade ) {
        this.buySellPanel.reInitComponents( trade ) ;
    }
}
