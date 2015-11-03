/**
 * Creation Date: Aug 3, 2008
 */

package com.sandy.apps.pluto.shared.dto;
import java.util.HashMap ;
import java.util.Map ;

/**
 * This class encapsulates the details of a task.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class TaskDetail {

    private Integer id      = null ;
    private String  name    = null ;
    private String  type    = null ;
    private String  cron    = null ;
    private String  startup = null ;
    private Map<String, String> attributes = new HashMap<String, String>() ;

    /** Constructor. */
    public TaskDetail() {
        super() ;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return this.id ;
    }

    /**
     * @param id the id to set
     */
    public void setId( final Integer id ) {
        this.id = id ;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name ;
    }

    /**
     * @param name the name to set
     */
    public void setName( final String name ) {
        this.name = name ;
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
     * @return the cron
     */
    public String getCron() {
        return this.cron ;
    }

    /**
     * @param cron the cron to set
     */
    public void setCron( final String cron ) {
        this.cron = cron ;
    }

    /**
     * @return the startup
     */
    public String getStartup() {
        return this.startup ;
    }

    /**
     * @param startup the startupType to set
     */
    public void setStartup( final String startup ) {
        this.startup = startup ;
    }

    /**
     * @return the attributes
     */
    public Map<String, String> getAttributes() {
        return this.attributes ;
    }

    /**
     * @param attributes the attributes to set
     */
    public void setAttributes( final Map<String, String> attributes ) {
        this.attributes = attributes ;
    }
}
