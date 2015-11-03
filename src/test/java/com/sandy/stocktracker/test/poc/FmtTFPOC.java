/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 15, 2008
 */

package com.sandy.stocktracker.test.poc;
import java.awt.GridLayout;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

/**
 * Tests the behavior of formatted text fields.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class FmtTFPOC extends JPanel {

    private static final long serialVersionUID = 1L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( FmtTFPOC.class ) ;

    private JFormattedTextField field1;
    private JFormattedTextField field2;

    public FmtTFPOC() {
        super( new GridLayout( 2, 2 ) ) ;
        setUpField1() ;
        setUpField2() ;
        displayFields() ;
    }

    private void displayFields() {
        add( new JLabel( "field 1:" ) ) ;
        add( this.field1 ) ;
        add( new JLabel( "field 2:" ) ) ;
        add( this.field2 ) ;
    }

    private void setUpField1() {
        NumberFormat format1 ;
        format1 = NumberFormat.getInstance() ;
        format1.setMaximumFractionDigits( 2 ) ;
        this.field1 = new JFormattedTextField( format1 ) ;
    }

    private void setUpField2() {
        NumberFormat format2 ;
        format2 = NumberFormat.getInstance() ;
        format2.setMinimumFractionDigits( 1 ) ;
        format2.setMaximumFractionDigits( 4 ) ;
        this.field2 = new JFormattedTextField( format2 ) ;
    }

    public static void main( final String[] args ) {
        final JFrame frame = new JFrame( "Number Format Demo" ) ;
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE ) ;
        frame.setSize( 300, 200 ) ;
        frame.getContentPane().add( new FmtTFPOC() ) ;
        frame.setVisible( true ) ;
    }
}
