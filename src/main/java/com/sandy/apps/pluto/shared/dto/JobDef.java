/**
 * Creation Date: Aug 19, 2008
 */

package com.sandy.apps.pluto.shared.dto;

/**
 * This DTO class encapsulates information relating to a Job definition. This
 * class has a direct mapping to the JOB_DEF table.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class JobDef {

    private String  type        = null ;
    private String  className   = null ;
    private boolean networkReq  = false ;
    private String  icon        = null ;

    private Class<?> classType = null ;

    /** Public constructor. */
    public JobDef() {
        super() ;
    }

    /**
     * @return the type
     */
    public String getType() {
        return this.type ;
    }

    /**
     * @param type the type to set
     */
    public void setType( final String type ) {
        this.type = type ;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return this.className ;
    }

    /**
     * @param className the className to set
     */
    public void setClassName( final String className ) {
        this.className = className ;
        try {
            this.classType = Class.forName( className ) ;
        }
        catch ( final Exception e ) {
            throw new IllegalArgumentException( "A job class of name " + className +
                    " could not be located in the classpath", e ) ;
        }
    }

    /**
     * @return The type of the job class
     */
    public Class<?> getClassType() {
        return this.classType ;
    }

    /**
     * @return the networkReq
     */
    public boolean isNetworkReq() {
        return this.networkReq ;
    }

    /**
     * @param networkReq the networkReq to set
     */
    public void setNetworkReq( final boolean networkReq ) {
        this.networkReq = networkReq ;
    }

    /**
     * @return the icon
     */
    public String getIcon() {
        return this.icon ;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon( final String icon ) {
        this.icon = icon ;
    }

    /** String representation of this instance. */
    public String toString() {
        final StringBuffer buffer = new StringBuffer() ;
        buffer.append( "JobDef[" ) ;
        buffer.append( "className = " ).append( this.className ) ;
        buffer.append( ", icon = " ).append( this.icon ) ;
        buffer.append( ", networkReq = " ).append( this.networkReq ) ;
        buffer.append( ", type = " ).append( this.type ) ;
        buffer.append( "]" ) ;
        return buffer.toString() ;
    }
}
