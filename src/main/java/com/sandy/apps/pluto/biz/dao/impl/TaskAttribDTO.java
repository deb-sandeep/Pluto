/**
 * Creation Date: Aug 4, 2008
 */

package com.sandy.apps.pluto.biz.dao.impl;

/**
 * This DTO class encapsulates the details regarding a particular task attribute.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class TaskAttribDTO {

    private Integer taskId = null ;
    private String name = null ;
    private String value= null ;

    /** Public constructor. */
    public TaskAttribDTO() {
        super() ;
    }

    /**
     * @return the taskId
     */
    public Integer getTaskId() {
        return this.taskId ;
    }

    /**
     * @param taskId the taskId to set
     */
    public void setTaskId( final Integer taskId ) {
        this.taskId = taskId ;
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
     * @return the value
     */
    public String getValue() {
        return this.value ;
    }

    /**
     * @param value the value to set
     */
    public void setValue( final String value ) {
        this.value = value ;
    }
}
