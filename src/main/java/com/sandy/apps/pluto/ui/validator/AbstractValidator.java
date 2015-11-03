/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 15, 2008
 */

package com.sandy.apps.pluto.ui.validator ;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.util.util.StringUtil ;
import com.sandy.apps.pluto.ui.UIConstant ;

/**
 * This abstract base class forms the root of all validators supported by this
 * application. This class handles most of the details of validating a
 * component, including all display elements such as popup help boxes and color
 * changes.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public abstract class AbstractValidator extends InputVerifier implements
        KeyListener {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( AbstractValidator.class ) ;

    private JDialog     popup        = null ;
    private JLabel      messageLabel = null ;
    private JLabel      image        = null ;
    private Point       point        = null ;
    private Dimension   cDim         = null ;
    private Color       color        = null ;
    private JComponent  component    = null ;

    private boolean allowNullValue = true ;

    /**
     * Public constructor.
     *
     * @param c The UI component to which this validator is registered.
     * @param message The error message to display when the validation fails.
     * @param allowNullValue If set to true, null values entered by the user
     *        will not be treated as error values.
     */
    protected AbstractValidator( final JComponent c, final String message,
                                 final boolean allowNullValue ) {

        this.color = new Color( 243, 255, 159 ) ;
        this.allowNullValue = allowNullValue ;
        this.messageLabel = new JLabel( message + " " ) ;
        this.image = new JLabel( new ImageIcon( UIConstant.IMG_FLAG_RED ) ) ;
        this.popup = new JDialog() ;
        this.component = c ;
        this.component.addKeyListener( this ) ;

        initComponents() ;
    }

    /**
     * Implement the actual validation logic in this method. The method should
     * return false if data is invalid and true if it is valid. It is also
     * possible to set the popup message text with setMessage() before
     * returning, and thus customize the message text for different types of
     * validation problems.
     *
     * @param value The value entered by the user, as extracted from the UI.
     *
     * @return false if data is invalid. true if it is valid.
     */
    protected abstract boolean validationCriteria( final Object value ) ;

    /**
     * This method is called by Java when a component needs to be validated. It
     * should not be called directly. Do not override this method unless you
     * really want to change validation behavior. Implement validationCriteria()
     * instead.
     */
    public final boolean verify( final JComponent c ) {

        boolean valid = false ;
        final Object value = getComonentValue() ;

        if( this.allowNullValue && value == null ) {
            valid = true ;
        }
        else if( !this.allowNullValue && value == null ) {
            valid = false ;
        }
        else {
            valid = validationCriteria( value ) ;
        }

        if ( !valid ) {
            c.setBackground( Color.PINK ) ;
            this.popup.setSize( 0, 0 ) ;
            this.popup.setLocationRelativeTo( c ) ;
            this.point = this.popup.getLocation() ;
            this.cDim = c.getSize() ;
            this.popup.setLocation( this.point.x - ( int ) this.cDim.getWidth() / 2,
                                    this.point.y + ( int ) this.cDim.getHeight()/ 2 ) ;
            this.popup.pack() ;
            this.popup.setVisible( true ) ;
        }
        else {
            c.setBackground( Color.WHITE ) ;
        }

        return valid ;
    }

    /**
     * Changes the message that appears in the popup help tip when a component's
     * data is invalid. Subclasses can use this to provide context sensitive
     * help depending on what the user did wrong.
     *
     * @param message
     */
    protected void setMessage( final String message ) {
        this.messageLabel.setText( message ) ;
    }

    /**
     * Once the enter starts pressing keys on this user input field, hide the
     * popup if it is visible.
     */
    public void keyPressed( final KeyEvent e ) {
        // NO OP
    }

    /**
     * Once the enter starts pressing keys on this user input field, hide the
     * popup if it is visible.
     */
    public void keyTyped( final KeyEvent e ) {
        this.popup.setVisible( false ) ;
    }

    /**
     * Once the enter starts pressing keys on this user input field, hide the
     * popup if it is visible.
     */
    public void keyReleased( final KeyEvent e ) {
        // NO OP
    }

    /** Initializes the popup by setting up its layout and messages etc. */
    private void initComponents() {
        this.popup.getContentPane().setLayout( new FlowLayout() ) ;
        this.popup.setUndecorated( true ) ;
        this.popup.getContentPane().setBackground( this.color ) ;
        this.popup.getContentPane().add( this.image ) ;
        this.popup.getContentPane().add( this.messageLabel ) ;
        this.popup.setFocusableWindowState( false ) ;
    }

    /**
     * @return The value of the component.
     */
    private Object getComonentValue() {
        String value = null ;

        if( this.component instanceof JTextComponent ) {
            final JTextComponent textComp = ( JTextComponent )this.component ;
            value = textComp.getText() ;
            if( StringUtil.isEmptyOrNull( value ) ) {
                value = null ;
            }
        }

        return value ;
    }
}
