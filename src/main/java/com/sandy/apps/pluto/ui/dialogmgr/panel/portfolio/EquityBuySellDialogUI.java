/**
 * 
 * 
 * 
 *
 * Creation Date: Mar 27, 2009
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.portfolio;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.STUtils ;
import com.sandy.apps.pluto.shared.dto.LogMsg ;
import com.sandy.apps.pluto.shared.dto.ScripEOD ;
import com.sandy.apps.pluto.shared.dto.ScripITD ;
import com.sandy.apps.pluto.shared.dto.Symbol ;
import com.sandy.apps.pluto.shared.dto.Trade ;
import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.scripeodsummary.ScripEODValueCache ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.scripitdsummary.ScripITDValueCache ;


/**
 * This panel is used to capture trade related information from the user.
 * With this panel the user can enter his trade details which are captured
 * in the portfolio.
 *
 * This panel provides the following functionality.
 * a) Trade enter - User can enter a new trade
 * b) Trade edit  - User can edit an existing trade
 * c) Validation of fields provided by the user
 *    c.1) Price should be greater than 0
 *    c.2) Number of units should be an integer greater than 0
 *    c.3) Date format should be dd-MMM-yy HH:mm:ss
 * d) Auto computation of brokerage charges
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class EquityBuySellDialogUI extends JPanel implements ItemListener {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L ;

    /** Log4J logger for this class. */
    public static final Logger logger =
                               Logger.getLogger( EquityBuySellDialogUI.class ) ;

    /** The button group which creates a mutual exclusion for buy/sell */
    final ButtonGroup buySellGroup = new ButtonGroup() ;

    /**
     * A reference to the EOD value cache, which is used to populate the
     * equity codes.
     */
    final ScripEODValueCache eodCache = ScripEODValueCache.getInstance() ;

    /** The time format for rendering the time in the title. */
    final static SimpleDateFormat TRADE_TIME_FMT =
                                  new SimpleDateFormat( "dd-MMM-yy HH:mm:ss" ) ;

    /** The decimal format for displaying price. */
    final static DecimalFormat DECIMAL_FMT = new DecimalFormat( "###.##" ) ;

    /**
     * A reference to the ITD value cache, which is used to deduce the last
     * traded price etc for reinitializing the components before display.
     */
    final ScripITDValueCache itdCache = ScripITDValueCache.getInstance() ;

    /** The parent component for this panel. */
    final EquityBuySellDialog parent ;

    /** The current trade being edited. */
    private Trade currTrade = null ;

    /** Creates new form EquityBuySellPanel */
    public EquityBuySellDialogUI( final EquityBuySellDialog parent ) {
        this.parent = parent ;
        initComponents();
        postInitComponents() ;
        initListeners() ;
    }

    /**
     * This method is called upon this class to reinitialize the controls. This
     * is done before this panel is being set up for display.     *
     */
    public void reInitComponents( final Trade trade ) {

        this.currTrade = trade ;
        if( this.currTrade != null ) {
            this.buyRB.setSelected( this.currTrade.isBuy() ) ;
            this.numUnitsTF.setText( "" + this.currTrade.getUnits() ) ;
            this.brokerageTF.setText( "" + this.currTrade.getBrokerage() ) ;

            final String nseCode = trade.getSymbol() ;
            setNseCode( nseCode ) ;
            double ltp = 0.0 ;
            final ScripITD itd = this.itdCache.getScripITDForSymbol( nseCode ) ;
            if( itd == null ) {
                final ScripEOD eod = this.eodCache.getScripEOD( nseCode ) ;
                if( eod != null ) {
                    ltp = eod.getClosingPrice() ;
                }
            }
            else {
                ltp = itd.getPrice() ;
            }
            this.unitPriceTF.setText( DECIMAL_FMT.format( ltp ) ) ;
            this.tradeTimeTF.setText( TRADE_TIME_FMT.format( trade.getDate() ) ) ;
        }
        else {
            this.buyRB.setSelected( true ) ;
            this.numUnitsTF.setText( "0" ) ;
            this.unitPriceTF.setText( "0.00" ) ;
            this.brokerageTF.setText( "0.00" ) ;
            this.tradeTimeTF.setText( TRADE_TIME_FMT.format( new Date() ) ) ;
        }
    }

    /** Sets the ICICI direct code value. */
    public void setIciciCode( final String value ) {
        this.iciciDirectCodeLabel.setText( value ) ;
    }

    /** Sets the nse code value in the combo. */
    public void setNseCode( final String nseCode ) {
        this.nseSymbolCombo.setSelectedItem( nseCode ) ;
    }

    /**
     * Post initialize the UI. This is done to ensure that we do not touch
     * the initComponents method which is generated automatically using
     * NetBeans.
     */
    public void postInitComponents() {
        this.nseSymbolLabel.setFont( UIConstant.DLG_FONT ) ;
        this.iciciDirectLabel.setFont( UIConstant.DLG_FONT ) ;
        this.buySellLabel.setFont( UIConstant.DLG_FONT ) ;
        this.unitPriceLabel.setFont( UIConstant.DLG_FONT ) ;
        this.numUnitsLabel.setFont( UIConstant.DLG_FONT ) ;
        this.brokerageLabel.setFont( UIConstant.DLG_FONT ) ;
        this.brokerageTF.setFont( UIConstant.DLG_FONT ) ;
        this.buyRB.setFont( UIConstant.DLG_FONT ) ;
        this.sellRB.setFont( UIConstant.DLG_FONT ) ;
        this.tradeTypeLabel.setFont( UIConstant.DLG_FONT ) ;
        this.tradeTimeLabel.setFont( UIConstant.DLG_FONT ) ;

        this.buySellGroup.add( this.buyRB ) ;
        this.buySellGroup.add( this.sellRB ) ;

        for( final ScripEOD eod : this.eodCache.getScripEODList() ) {
            this.nseSymbolCombo.addItem( eod.getSymbolId() ) ;
        }

        this.tradeTypeCombo.addItem( Trade.CASH ) ;
        this.tradeTypeCombo.addItem( Trade.MARGIN ) ;
        this.tradeTypeCombo.addItem( Trade.SPOT ) ;

        this.numUnitsTF.setText( "0" ) ;
        this.unitPriceTF.setText( "0.00" ) ;
        this.brokerageTF.setText( "0.00" ) ;
    }

    /**
     * Initializes the listeners for the UI components in this panel. Since
     * this panel is primarily UI, all the listeners are delegated to the
     * dialog which houses this panel.
     */
    public void initListeners() {
        this.nseSymbolCombo.addItemListener( this ) ;
        this.unitPriceTF.getDocument().addDocumentListener( new DocumentListener(){
            public void changedUpdate( final DocumentEvent e ){ unitPriceChanged( e ) ; }
            public void insertUpdate( final DocumentEvent e ) { unitPriceChanged( e ) ; }
            public void removeUpdate( final DocumentEvent e ) { unitPriceChanged( e ) ; }
        }) ;

        this.numUnitsTF.getDocument().addDocumentListener( new DocumentListener(){
            public void changedUpdate( final DocumentEvent e ){ numUnitsChanged( e ) ; }
            public void insertUpdate( final DocumentEvent e ) { numUnitsChanged( e ) ; }
            public void removeUpdate( final DocumentEvent e ) { numUnitsChanged( e ) ; }
        }) ;

        this.brokerageTF.getDocument().addDocumentListener( new DocumentListener(){
            public void changedUpdate( final DocumentEvent e ){ brokerageChanged( e ) ; }
            public void insertUpdate( final DocumentEvent e ) { brokerageChanged( e ) ; }
            public void removeUpdate( final DocumentEvent e ) { brokerageChanged( e ) ; }
        }) ;

        this.tradeTimeTF.getDocument().addDocumentListener( new DocumentListener(){
            public void changedUpdate( final DocumentEvent e ){ tradeTimeChanged( e ) ; }
            public void insertUpdate( final DocumentEvent e ) { tradeTimeChanged( e ) ; }
            public void removeUpdate( final DocumentEvent e ) { tradeTimeChanged( e ) ; }
        }) ;

        this.tradeTypeCombo.addItemListener( new ItemListener() {
            public void itemStateChanged( final ItemEvent e ) {
                try {
                    final int numUnits = Integer.parseInt( EquityBuySellDialogUI.this.numUnitsTF.getText() ) ;
                    final double price = Double.parseDouble( EquityBuySellDialogUI.this.unitPriceTF.getText() ) ;
                    computeBrokerage( numUnits, price ) ;
                }
                catch ( final NumberFormatException e1 ) {
                    final String msg = "Invalid number in either num unites or price" ;
                    logger.error( msg ) ;
                    LogMsg.error( msg ) ;
                }
            }
        } ) ;
    }

    /**
     * This method is invoked on this class when the stock code is selected in
     * the NSE stock code combo box in the UI panel. This method updates the
     * ICICI direct stock code in the panel.
     */
    public void itemStateChanged( final ItemEvent e ) {
        final JComboBox nseCodeCombo = ( JComboBox )e.getSource() ;
        final String nseCode = ( String )nseCodeCombo.getSelectedItem() ;
        final ScripEOD eod = this.eodCache.getScripEOD( nseCode ) ;

        if( eod != null ) {
            final Symbol sbl = eod.getSymbol() ;
            setIciciCode( sbl.getIciciCode() ) ;
        }
    }

    /**
     * This method is called upon when the text in the brokerage text field
     * is changed by the user. We take this opportunity to validate the
     * brokerage charges entered.
     *
     * @param e The document event signifying the change in the unit price.
     */
    private void tradeTimeChanged( final DocumentEvent e ) {
        final Document doc = e.getDocument() ;
        try {
            final String text = doc.getText( 0, doc.getLength() ) ;
            TRADE_TIME_FMT.parse( text ) ;
            this.tradeTimeTF.setBackground( Color.WHITE ) ;
        }
        catch ( final BadLocationException e1 ) {
            logger.error( "Bad location exception", e1 ) ;
        }
        catch ( final ParseException dfe ) {
            this.tradeTimeTF.setBackground( Color.PINK ) ;
        }
    }

    /**
     * This method is called upon when the text in the brokerage text field
     * is changed by the user. We take this opportunity to validate the
     * brokerage charges entered.
     *
     * @param e The document event signifying the change in the unit price.
     */
    private void brokerageChanged( final DocumentEvent e ) {
        final Document doc = e.getDocument() ;
        try {
            final String text = doc.getText( 0, doc.getLength() ) ;
            Double.parseDouble( text ) ;

            this.brokerageTF.setBackground( Color.WHITE ) ;
        }
        catch ( final BadLocationException e1 ) {
            logger.error( "Bad location exception", e1 ) ;
        }
        catch ( final NumberFormatException nfe ) {
            this.brokerageTF.setBackground( Color.PINK ) ;
        }
    }

    /**
     * This method is called upon when the text in the unit price text field
     * is changed by the user. We take this opportunity to auto compute the
     * brokerage charges.
     *
     * @param e The document event signifying the change in the unit price.
     */
    private void unitPriceChanged( final DocumentEvent e ) {
        final Document doc = e.getDocument() ;
        try {
            final String text = doc.getText( 0, doc.getLength() ) ;
            final double val  = Double.parseDouble( text ) ;
            final int    units= Integer.parseInt( this.numUnitsTF.getText() ) ;

            computeBrokerage( units, val ) ;

            this.unitPriceTF.setBackground( Color.WHITE ) ;
        }
        catch ( final BadLocationException e1 ) {
            logger.error( "Bad location exception", e1 ) ;
        }
        catch ( final NumberFormatException nfe ) {
            this.unitPriceTF.setBackground( Color.PINK ) ;
        }
    }

    /**
     * This method is called upon when the text in the number of units text field
     * is changed by the user. We take this opportunity to auto compute the
     * brokerage charges.
     *
     * @param e The document event signifying the change in the unit price.
     */
    private void numUnitsChanged( final DocumentEvent e ) {
        final Document doc = e.getDocument() ;
        try {
            final String text = doc.getText( 0, doc.getLength() ) ;
            final int    val       = Integer.parseInt( text ) ;
            final double unitPrice = Double.parseDouble( this.unitPriceTF.getText() ) ;

            computeBrokerage( val, unitPrice ) ;

            this.numUnitsTF.setBackground( Color.WHITE ) ;
        }
        catch ( final BadLocationException e1 ) {
            logger.error( "Bad location exception", e1 ) ;
        }
        catch ( final NumberFormatException nfe ) {
            this.numUnitsTF.setBackground( Color.PINK ) ;
        }
    }

    /**
     * Computes the brokerage charges based on the number of units and unit
     * price selected.
     *
     * @param numUnits The number of units traded
     * @param unitPrice The unit price of the stock
     */
    private void computeBrokerage( final int numUnits, final double unitPrice ) {
        final String tradeType = ( String )this.tradeTypeCombo.getSelectedItem() ;
        final double brokerage = STUtils.computeBrokerage( numUnits,
                                 unitPrice, tradeType, this.buyRB.isSelected(),
                                 null, null ) ;

        this.brokerageTF.setText( DECIMAL_FMT.format( brokerage ) ) ;
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

        final List<String> msgList = new ArrayList<String>() ;
        try {
            final int numUnits = Integer.parseInt( this.numUnitsTF.getText() ) ;
            if( numUnits < 1 ) {
                msgList.add( "Num units should be greater than 1" ) ;
            }
        }
        catch ( final NumberFormatException nfe ) {
            msgList.add( "Invalid number of units" ) ;
        }

        try {
            final double price = Double.parseDouble( this.unitPriceTF.getText() ) ;
            if( price <= 0 ) {
                msgList.add( "Unit price should be greater than 0" ) ;
            }
        }
        catch ( final NumberFormatException nfe ) {
            msgList.add( "Invalid unit price" ) ;
        }

        try {
            Double.parseDouble( this.brokerageTF.getText() ) ;
        }
        catch ( final NumberFormatException nfe ) {
            msgList.add( "Invalid brokerage" ) ;
        }

        try {
            TRADE_TIME_FMT.parse( this.tradeTimeTF.getText() ) ;
        }
        catch ( final ParseException nfe ) {
            msgList.add( "Invalid trade time" ) ;
        }

        return msgList ;
    }

    /**
     * This function is called upon the encapsulate the data entered by the
     * user into a Trade instance.
     *
     * @return The trade instance encapsulating the data entered by the user.
     */
    public Trade getTrade() {
        if( this.currTrade == null ) {
            this.currTrade = new Trade() ;
        }

        try {
            this.currTrade.setSymbol( ( String )this.nseSymbolCombo.getSelectedItem() ) ;
            this.currTrade.setBrokerage( Double.parseDouble( this.brokerageTF.getText() ) ) ;
            this.currTrade.setUnitPrice( Double.parseDouble( this.unitPriceTF.getText() ) ) ;
            this.currTrade.setUnits( Integer.parseInt( this.numUnitsTF.getText() ) ) ;
            this.currTrade.setTradeType( ( String )this.tradeTypeCombo.getSelectedItem() ) ;
            this.currTrade.setBuy( this.buyRB.isSelected() ) ;
            this.currTrade.setDate( TRADE_TIME_FMT.parse( this.tradeTimeTF.getText() ) ) ;
        }
        catch ( final Exception e ) {
            this.currTrade = null ;
        }

        Trade retVal = null ;
        try {
            retVal = ( Trade )this.currTrade.clone() ;
        }
        catch( final CloneNotSupportedException e ) {
            // Ignore - this will never happen
        }
        return retVal ;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">
    private void initComponents() {
        this.nseSymbolLabel = new JLabel();
        this.nseSymbolCombo = new JComboBox();
        this.iciciDirectLabel = new JLabel();
        this.iciciDirectCodeLabel = new JLabel();
        this.buySellLabel = new JLabel();
        this.unitPriceLabel = new JLabel();
        this.unitPriceTF = new JTextField();
        this.numUnitsLabel = new JLabel();
        this.numUnitsTF = new JTextField();
        this.brokerageLabel = new JLabel();
        this.brokerageTF = new JTextField();
        this.buyRB = new JRadioButton();
        this.sellRB = new JRadioButton();
        this.tradeTypeLabel = new JLabel();
        this.tradeTypeCombo = new JComboBox();
        this.tradeTimeLabel = new JLabel();
        this.tradeTimeTF = new JTextField();

        this.nseSymbolLabel.setText("NSE Symbol");

        this.nseSymbolCombo.setFont( UIConstant.DLG_FONT );

        this.iciciDirectLabel.setText("ICICIDirect Code");

        this.iciciDirectCodeLabel.setFont( UIConstant.DLG_FONT_BOLD );
        this.iciciDirectCodeLabel.setForeground(new Color(153, 153, 153));
        this.iciciDirectCodeLabel.setText("ICIBAN");

        this.buySellLabel.setText("Buy/Sell");

        this.unitPriceLabel.setText("Unit Price");

        this.unitPriceTF.setFont( UIConstant.DLG_FONT );

        this.numUnitsLabel.setText("Number of Units");

        this.numUnitsTF.setFont( UIConstant.DLG_FONT );

        this.brokerageLabel.setText("Brokerage");

        this.brokerageTF.setFont( UIConstant.DLG_FONT );

        this.buyRB.setText("Buy");
        this.buyRB.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        this.buyRB.setMargin(new Insets(0, 0, 0, 0));

        this.sellRB.setText("Sell");
        this.sellRB.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        this.sellRB.setMargin(new Insets(0, 0, 0, 0));

        this.tradeTypeLabel.setText("Trade Type");

        this.tradeTypeCombo.setFont( UIConstant.DLG_FONT );

        this.tradeTimeLabel.setText("Trade Time");

        this.tradeTimeTF.setFont( UIConstant.DLG_FONT );

        final GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(this.nseSymbolLabel)
                            .addComponent(this.iciciDirectLabel)
                            .addComponent(this.buySellLabel)
                            .addComponent(this.unitPriceLabel)
                            .addComponent(this.numUnitsLabel)
                            .addComponent(this.brokerageLabel)
                            .addComponent(this.tradeTypeLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(this.unitPriceTF, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addComponent(this.nseSymbolCombo, 0, 144, Short.MAX_VALUE)
                                .addComponent(this.iciciDirectCodeLabel))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(this.buyRB)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(this.sellRB))
                            .addComponent(this.tradeTypeCombo, 0, 144, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(this.brokerageTF, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                                    .addComponent(this.numUnitsTF, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                                    .addComponent(this.tradeTimeTF, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)))))
                    .addComponent(this.tradeTimeLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(this.nseSymbolLabel)
                    .addComponent(this.nseSymbolCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(this.iciciDirectLabel)
                    .addComponent(this.iciciDirectCodeLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(this.buySellLabel)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.buyRB)
                        .addComponent(this.sellRB)))
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(this.tradeTypeLabel)
                    .addComponent(this.tradeTypeCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(this.unitPriceLabel)
                    .addComponent(this.unitPriceTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(this.numUnitsLabel)
                    .addComponent(this.numUnitsTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(this.brokerageLabel)
                    .addComponent(this.brokerageTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(this.tradeTimeLabel)
                    .addComponent(this.tradeTimeTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>


    // Variables declaration - do not modify
    private JLabel brokerageLabel;
    private JTextField brokerageTF;
    private JRadioButton buyRB;
    private JLabel buySellLabel;
    private JLabel iciciDirectCodeLabel;
    private JLabel iciciDirectLabel;
    private JComboBox nseSymbolCombo;
    private JLabel nseSymbolLabel;
    private JLabel numUnitsLabel;
    private JTextField numUnitsTF;
    private JRadioButton sellRB;
    private JLabel tradeTimeLabel;
    private JTextField tradeTimeTF;
    private JComboBox tradeTypeCombo;
    private JLabel tradeTypeLabel;
    private JLabel unitPriceLabel;
    private JTextField unitPriceTF;
    // End of variables declaration
}
