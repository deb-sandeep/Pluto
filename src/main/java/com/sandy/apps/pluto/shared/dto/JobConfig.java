/**
 * Creation Date: Aug 19, 2008
 */

package com.sandy.apps.pluto.shared.dto;

import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

/**
 * This DTO class encapsulates information regarding a job instance configuration.
 * There can be many job instances for a given job type. This class has a
 * direct mapping to the JOB_CONFIG database table.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class JobConfig {

    /** The job definition for which this is an instance. */
    private JobDef jobDef = null ;

    private Integer jobId         = null ;
    private String  name          = null ;
    private String  description   = null ;
    private String  cron          = null ;
    private String  upperTimeBand = null ;
    private String  lowerTimeBand = null ;
    private String  startupType   = null ;

    private Map<String, List<JobAttribute>> attributes = new HashMap<String, List<JobAttribute>>() ;

    /** Public constructor. */
    public JobConfig() {
        super() ;
    }

    /**
     * @return the jobDef
     */
    public JobDef getJobDef() {
        return this.jobDef ;
    }

    /**
     * @param jobDef the jobDef to set
     */
    public void setJobDef( final JobDef jobDef ) {
        this.jobDef = jobDef ;
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
     * @return the description
     */
    public String getDescription() {
        return this.description ;
    }

    /**
     * @param description the description to set
     */
    public void setDescription( final String description ) {
        this.description = description ;
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
     * @return the upperTimeBand
     */
    public String getUpperTimeBand() {
        return this.upperTimeBand ;
    }

    /**
     * @param upperTimeBand the upperTimeBand to set
     */
    public void setUpperTimeBand( final String upperTimeBand ) {
        this.upperTimeBand = upperTimeBand ;
    }

    /**
     * @return the lowerTimeBand
     */
    public String getLowerTimeBand() {
        return this.lowerTimeBand ;
    }

    /**
     * @param lowerTimeBand the lowerTimeBand to set
     */
    public void setLowerTimeBand( final String lowerTimeBand ) {
        this.lowerTimeBand = lowerTimeBand ;
    }

    /**
     * @return the startupType
     */
    public String getStartupType() {
        return this.startupType ;
    }

    /**
     * @param startupType the startupType to set
     */
    public void setStartupType( final String startupType ) {
        this.startupType = startupType ;
    }

    /**
     * @return The attributes of this job configuration
     */
    public Map<String, List<JobAttribute>> getAttributes() {
        return this.attributes ;
    }

    /** Set the specified attributes as the attributes of this job config. */
    public void setAttribute( final Map<String, List<JobAttribute>> attribs ) {
        this.attributes = attribs ;
    }

    /**
     * Gets the attribute value for the specified attribute key. If no values
     * are found, this method returns a null. This method always returns the
     * attribute values as a collection of Strings. If the attribute represents
     * a single value attribute, the list will contain only one value.
     */
    public List<JobAttribute> getAttributeValues( final String key ) {
        List<JobAttribute> retVal = null ;
        retVal = this.attributes.get( key ) ;
        return retVal ;
    }

    /**
     * Gets the value of the specified attribute or null if the attribute
     * does not exist. If the attribute is an indexed attribute, the first
     * index value will be returned.
     */
    public JobAttribute getAttributeValue( final String key ) {
        JobAttribute retVal = null ;
        final List<JobAttribute> attrValues = getAttributeValues( key ) ;
        if( attrValues != null && !attrValues.isEmpty() ) {
            retVal = attrValues.get( 0 ) ;
        }
        return retVal ;
    }

    /** Removes all the values associated with the specified attribute key. */
    public void removeAttribute( final String key ) {
        this.attributes.remove( key ) ;
    }

    /** Removes all the attributes associated with this job instance. */
    public void removeAllAttributes() {
        this.attributes.clear() ;
    }

    /** Returns a string representation of this instance. */
    public String toString() {
        final StringBuffer buffer = new StringBuffer() ;
        buffer.append( "JobConfig[" ) ;
        buffer.append( "jobId = " ).append( this.jobId ) ;
        buffer.append( ", name = " ).append( this.name ) ;
        buffer.append( ", startupType = " ).append( this.startupType ) ;
        buffer.append( ", cron = " ).append( this.cron ) ;
        buffer.append( ", upperTimeBand = " ).append( this.upperTimeBand ) ;
        buffer.append( ", lowerTimeBand = " ).append( this.lowerTimeBand ) ;
        buffer.append( ", jobDef = " ).append( this.jobDef ) ;
        buffer.append( ", description = " ).append( this.description ) ;
        buffer.append( "]" ) ;
        return buffer.toString() ;
    }
}
