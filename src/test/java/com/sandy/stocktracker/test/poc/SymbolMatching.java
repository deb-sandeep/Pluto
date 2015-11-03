/**
 * 
 * 
 * 
 *
 * Creation Date: Dec 13, 2008
 */

package com.sandy.stocktracker.test.poc;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.util.util.StringUtil ;

/**
 * This class tries to fuzzy match the NSE and ICICI Direct symbols by using
 * Levenstein distance between their descriptions.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class SymbolMatching {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( SymbolMatching.class ) ;

    /** Map to store NSE symbol-name information. */
    private static final Map<String, String> nseSymbolMap = new HashMap<String, String>() ;

    /** Map to store ICICI Direct symbol-name information. */
    private static final Map<String, String> iciciSymbolMap = new HashMap<String, String>() ;

    /** Loads data from the specified file into the given map. */
    public void loadDataIntoMap( final File file, final Map<String, String> map )
        throws Exception {

        final BufferedReader reader = new BufferedReader( new FileReader( file ) ) ;
        String line = null ;
        while( ( line = reader.readLine() ) != null ) {

            final int index = line.indexOf( ' ' ) ;
            final String symbol = line.substring( 0, index ) ;
            final String name   = line.substring( index+1 ).trim() ;

            final String oldEntry = map.put( name, symbol ) ;
            if( oldEntry != null ) {
                logger.debug( "Ignoring duplicate name = " + name ) ;
                map.remove( name ) ;
            }
        }
        reader.close() ;
    }

    /** Starts matching the NSE symbols with equivalent ICICI direct names. */
    public void startMatch() throws Exception {

        final FileWriter writer = new FileWriter( "match.csv" ) ;
        for( final String name : nseSymbolMap.keySet() ) {
            final String symbol = nseSymbolMap.get( name ) ;

            final String[] iciciInfo = findClosestICICISymbol( name ) ;
            writer.write( symbol + "," + name + "," + iciciInfo[0] + "," + iciciInfo[1] + "\n" ) ;
        }

        writer.flush() ;
        writer.close() ;
    }

    /** Creates updates for the symbol table for the NSE-ICICI mapping */
    public void createUpdateStatements() throws Exception {

        final BufferedReader reader = new BufferedReader( new FileReader( new File( "docs/nse-icici-mapping.txt") ) ) ;
        String line = null ;
        while( ( line = reader.readLine() ) != null ) {

            int index = line.indexOf( ' ' ) ;
            final String nseSymbol = line.substring( 0, index ) ;
            final String temp      = line.substring( index+1 ).trim() ;

            index = temp.indexOf( ' ' ) ;
            final String iciciSymbol = temp.substring( 0, index ) ;
            String name = temp.substring( index ).trim() ;

            if( StringUtil.isNotEmptyOrNull( name ) ) {
                name = name.replace( "'", "''" ) ;

                logger.debug( "update \"SYMBOL\" set \"ICICI_CODE\"='" + iciciSymbol +
                              "', \"DESCR\"='" + name + "' where \"SYMBOL\"='" + nseSymbol + "' ;" ) ;
            }
        }
        reader.close() ;
    }

    public String[] findClosestICICISymbol( final String nseName ) {
        final String[] info = new String[2] ;
        int distance = Integer.MAX_VALUE ;
        for( final String iciciName : iciciSymbolMap.keySet() ) {
            final int d = StringUtils.getLevenshteinDistance( nseName, iciciName ) ;
            if( d < distance ) {
                distance = d ;
                info[0] = iciciSymbolMap.get( iciciName ) ;
                info[1] = iciciName ;
            }
        }
        return info ;
    }

    /** The main driver. */
    public static void main( final String[] args ) throws Exception {

        final SymbolMatching driver = new SymbolMatching() ;
        //driver.loadDataIntoMap( new File( "docs/nse-symbol-name.txt"), nseSymbolMap ) ;
        //driver.loadDataIntoMap( new File( "docs/ICICIDirect-symbol-name.txt"), iciciSymbolMap ) ;
        //driver.startMatch() ;
        driver.createUpdateStatements() ;
    }
}
