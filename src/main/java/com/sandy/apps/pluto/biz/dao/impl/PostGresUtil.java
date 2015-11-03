/**
 * Creation Date: Aug 3, 2008
 */

package com.sandy.apps.pluto.biz.dao.impl;
import org.apache.log4j.Logger ;
import org.postgresql.util.PSQLException ;

import com.ibatis.common.jdbc.exception.NestedSQLException ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * An utility class which provides Postgres database specific support.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class PostGresUtil {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( PostGresUtil.class ) ;

    /** Private constructor to enforce static utility class behavior. */
    private PostGresUtil() {
        super() ;
    }

    /**
     * Returns true if the instance of {@link DataAccessException} has resulted
     * due to a primary key violation.
     *
     * @param dae An instance of {@link DataAccessException}
     *
     * @return true if this exception has resulted because of a PK violation,
     *         false otherwise.
     */
    public static boolean isPKViolation( final DataAccessException dae ) {
        boolean retVal = false ;
        Throwable cause = dae.getCause() ;
        if( cause instanceof NestedSQLException ) {
            cause = (( NestedSQLException )cause).getCause() ;
            if( cause instanceof PSQLException ) {
                final PSQLException psqlE = ( PSQLException )cause ;
                if( psqlE.getMessage().contains( "unique constraint" ) ) {
                    retVal = true ;
                }
            }
        }
        return retVal ;
    }

    /**
     * Returns true if the instance of {@link DataAccessException} has resulted
     * due to a foreign key violation.
     *
     * @param dae An instance of {@link DataAccessException}
     *
     * @return true if this exception has resulted because of a FK violation,
     *         false otherwise.
     */
    public static boolean isFKViolation( final DataAccessException dae ) {
        boolean retVal = false ;
        Throwable cause = dae.getCause() ;
        if( cause instanceof NestedSQLException ) {
            cause = (( NestedSQLException )cause).getCause() ;
            if( cause instanceof PSQLException ) {
                final PSQLException psqlE = ( PSQLException )cause ;
                if( psqlE.getMessage().contains( "foreign key constraint" ) ) {
                    retVal = true ;
                }
            }
        }
        return retVal ;
    }
}
