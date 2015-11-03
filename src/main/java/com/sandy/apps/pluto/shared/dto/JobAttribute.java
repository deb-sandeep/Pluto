/**
 * Creation Date: Aug 21, 2008
 */

package com.sandy.apps.pluto.shared.dto;

/**
 * This class represents an instance of job attribute. Please note that a job
 * attribute is lot more than just a value, a job attribute contains slots
 * for extra data where the job can store intermittent information.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class JobAttribute {

    /** The identifier of the job instance for which this is an attribute. */
    private Integer jobId = null ;

    /** The name of this attribute. */
    private String name = null ;

    /** The primary value of this attribute. */
    private String value = null ;

    /** Extra data 1 associated with this attribute. */
    private String extraData1 = null ;

    /** Extra data 2 associated with this attribute. */
    private String extraData2 = null ;

    /** The sequence number of this attribute in case this is an indexed attribute. */
    private int sequence = 0 ;

    /** Public constructor. */
    public JobAttribute() {
        super() ;
    }

    /**
     * @return the jobId
     */
    public Integer getJobId() {
        return this.jobId ;
    }

    /**
     * @param jobId the jobId to set
     */
    public void setJobId( final Integer jobId ) {
        this.jobId = jobId ;
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

    /**
     * @return the extraData1
     */
    public String getExtraData1() {
        return this.extraData1 ;
    }

    /**
     * @param extraData1 the extraData1 to set
     */
    public void setExtraData1( final String extraData1 ) {
        this.extraData1 = extraData1 ;
    }

    /**
     * @return the extraData2
     */
    public String getExtraData2() {
        return this.extraData2 ;
    }

    /**
     * @param extraData2 the extraData2 to set
     */
    public void setExtraData2( final String extraData2 ) {
        this.extraData2 = extraData2 ;
    }

    /**
     * @return the sequence
     */
    public int getSequence() {
        return this.sequence ;
    }

    /**
     * @param sequence the sequence to set
     */
    public void setSequence( final int sequence ) {
        this.sequence = sequence ;
    }
}
