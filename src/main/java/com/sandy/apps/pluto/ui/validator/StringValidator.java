/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 15, 2008
 */

package com.sandy.apps.pluto.ui.validator;
import javax.swing.JComponent;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.ui.I18N ;

/**
 * Validates that the input entered by the user is a valid integer.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class StringValidator extends AbstractValidator {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( StringValidator.class ) ;

    /**
     * Public constructor. Null values entered by the user are assumed valid.
     *
     * @param c The UI component to which this validator is registered.
     */
    public StringValidator( final JComponent c ) {
        super( c, I18N.MSG_INVALID_STRING, true ) ;
    }

    /**
     * Public constructor.
     *
     * @param c The UI component to which this validator is registered.
     * @param allowNullValue If set to true, null values entered by the user
     *        will not be treated as error values.
     */
    public StringValidator( final JComponent c, final boolean allowNullValue ) {
        super( c, I18N.MSG_INVALID_STRING, allowNullValue ) ;
    }

    /**
     * Returns a true if the toString value of the object is a valid integer,
     * false otherwise.
     */
    @Override
    protected boolean validationCriteria( final Object value ) {
        boolean valid = false ;
        if( value != null ) {
            valid = true ;
        }
        return valid ;
    }
}
