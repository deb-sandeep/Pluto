/**
 * Creation Date: Dec 16, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model;
import java.util.Date ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.shared.STConstant ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartEntity.EntityType ;

/**
 * This class encapsulates the configuration for a chart entity. It encapsulates
 * information like the symbol, whether the symbol is a scrip or index,
 * the time marker, whether we have a ITD or EOD entity etc.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ChartEntityConfig {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ChartEntityConfig.class ) ;

    private final String name ;
    private final EntityType type ;
    private final Date time ;

    /**
     * Public constructor.
     *
     * @param name The name of the entity. In case of scrip, this is the
     *        symbol and in case of index it is the registered index name.
     * @param type The type of the entity. This is one of the {@link EntityType}
     *        enumerated values.
     * @param time The reference time of the entity.
     */
    public ChartEntityConfig( final String name, final EntityType type,
                              final Date time ) {
        this.name = name ;
        this.type = type ;
        this.time = time ;
    }

    public String getName() { return this.name ; }
    public EntityType getType() { return this.type ; }
    public Date getTime() { return this.time ; }

    /** Returns a string representation of this instance. */
    public String toString() {
        final StringBuffer buffer = new StringBuffer() ;
        buffer.append( "ChartEntity [" ) ;
        buffer.append( this.name ).append( ", " ) ;
        buffer.append( this.type ).append( ", " ) ;
        buffer.append( STConstant.DATE_FMT.format( this.time ) ) ;
        buffer.append( "]" ) ;
        return buffer.toString() ;
    }
}
