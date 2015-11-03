/**
 * Creation Date: Jul 27, 2008
 */

package com.sandy.apps.pluto.shared;
import org.apache.log4j.Logger ;

/**
 * This class represents application exceptions thrown from the Stock Tracker
 * application.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class STException extends Exception {

    private static final long serialVersionUID = -8133946300069151534L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( STException.class ) ;

    private ErrorCode errorCode = null ;

    public STException( final String message, final ErrorCode errorCode ) {
        super( message ) ;
        this.errorCode = errorCode ;
    }

    public STException( final Throwable cause, final ErrorCode errorCode  ) {
        super( cause ) ;
        this.errorCode = errorCode ;
    }

    public STException( final String message, final Throwable cause, final ErrorCode errorCode ) {
        super( message, cause ) ;
        this.errorCode = errorCode ;
    }

    public ErrorCode getErrorCode() {
        return this.errorCode ;
    }

    public static void wrapAndThrow( final Throwable t )
        throws STException {
        if( t instanceof STException ) {
            throw ( STException )t ;
        }
        else {
            throw new STException( "Wrapped Exception", t, ErrorCode.UNKNOWN_EXCEPTION ) ;
        }
    }
}
