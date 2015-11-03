/**
 * 
 * 
 * 
 *
 * Creation Date: Oct 15, 2008
 */

package com.sandy.stocktracker.test.poc;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.EventType ;
import com.sandy.apps.pluto.shared.dto.ScripITD ;
import com.sandy.apps.pluto.shared.event.EventBus ;

/**
 * A simple bean which when initialized and if running in a test environment
 * will read a dummy ITD file and emit ITD values into the event bus. This
 * class can be used for testing intrday related charting or summary
 * functionality offline.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class MockITDEmitter extends Thread {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( MockITDEmitter.class ) ;

    private int sleepTime = 10 * 1000 ;
    private String resourceFilePath = "/dummy_itd.csv" ;
    private int batchSize = 100 ;

    private static final DateFormat DF = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ) ;

    public MockITDEmitter() {
    }

    public void run() {

        try {
            Thread.sleep( 5000 ) ;

            final InputStream is = MockITDEmitter.class.getResourceAsStream( getResourceFilePath() ) ;
            final BufferedReader reader = new BufferedReader( new InputStreamReader( is ) ) ;
            String line = null ;

            boolean done = false ;
            while( !done ) {
                final List<ScripITD> itdList = new ArrayList<ScripITD>() ;
                for( int i=0; i<this.batchSize; i++ ) {
                    line = reader.readLine() ;
                    if( line == null ) {
                        done = true ;
                        break ;
                    }
                    else {
                        final ScripITD itd = createScripITD( line ) ;
                        itdList.add( itd ) ;
                        logger.debug( "Emitting " + itd ) ;
                    }
                }

                if( !itdList.isEmpty() ) {
                    EventBus.publish( EventType.EVT_SCRIP_ITD_INSERT, itdList ) ;
                    Thread.sleep( this.sleepTime ) ;
                }
            }
        }
        catch ( final Exception e ) {
            logger.error( "Exception encountered :", e ) ;
        }
    }

    private ScripITD createScripITD( final String line ) throws Exception {
        final ScripITD itd = new ScripITD() ;

        final StringTokenizer tokenizer = new StringTokenizer( line, "," ) ;

        itd.setSymbolId( tokenizer.nextToken().trim() ) ;
        itd.setPrice( Double.parseDouble( tokenizer.nextToken() ) ) ;
        itd.setTime( DF.parse( tokenizer.nextToken() ) ) ;
        itd.setTotalTradeQty( Long.parseLong( tokenizer.nextToken() ) ) ;
        itd.setOpeningPrice( Double.parseDouble( tokenizer.nextToken() ) ) ;
        itd.setHigh( Double.parseDouble( tokenizer.nextToken() ) ) ;
        itd.setLow( Double.parseDouble( tokenizer.nextToken() ) ) ;
        itd.setPrevClose( Double.parseDouble( tokenizer.nextToken() ) ) ;
        itd.setPctChange( Double.parseDouble( tokenizer.nextToken() ) ) ;
        itd.setChange( itd.getPrice() - itd.getPrevClose() ) ;

        return itd ;
    }

    public int getSleepTime() {
        return this.sleepTime ;
    }

    public void setSleepTime( final int sleepTime ) {
        this.sleepTime = sleepTime ;
    }

    public String getResourceFilePath() {
        return this.resourceFilePath ;
    }

    public void setResourceFilePath( final String resourceFilePath ) {
        this.resourceFilePath = resourceFilePath ;
    }

    public int getBatchSize() {
        return this.batchSize ;
    }

    public void setBatchSize( final int batchSize ) {
        this.batchSize = batchSize ;
    }

    public static void main( final String[] args ) {
        EventBus.instance() ;
        final MockITDEmitter emitter = new MockITDEmitter() ;
        emitter.setResourceFilePath( "/dummy_itd.csv" ) ;
        emitter.start() ;
    }
}
