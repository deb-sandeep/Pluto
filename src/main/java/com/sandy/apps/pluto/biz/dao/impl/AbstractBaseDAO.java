/**
 * Creation Date: Jul 27, 2008
 */

package com.sandy.apps.pluto.biz.dao.impl;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessManager ;

/**
 * The base class of all Data Access Objects in this application.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public abstract class AbstractBaseDAO {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( AbstractBaseDAO.class ) ;

    /**
     * INJECTABLE: This value should be injected with the instance of
     * {@link DataAccessManager} responsible for interacting with the
     * database.
     */
    protected DataAccessManager daMgr = null ;

    /** Public no argument constructor. */
    public AbstractBaseDAO() {
        super() ;
    }

    /**
     * @param daMgr the daMgr to set
     */
    public void setDataAccessManager( final DataAccessManager dataAccessManager ) {
        this.daMgr = dataAccessManager;
    }

    /**
     * @return the daMgr
     */
    public DataAccessManager getDataAccessManager() {
        return this.daMgr;
    }
}
