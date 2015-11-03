/**
 * 
 * 
 * 
 *
 * Creation Date: Oct 16, 2008
 */

package com.sandy.stocktracker.test.poc;
import org.apache.log4j.Logger;

import com.sandy.apps.pluto.ui.dialogmgr.panel.scripitdsummary.ScripITDFilterQueryParser ;

/**
 * This class is used to POC the ITD filter query parser.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ITDFilterQueryTest {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ITDFilterQueryTest.class ) ;

    public void test() throws Exception {

        final String queryStr = "symbol = IFCI, NOIDATOLL and price > 100 or qty > 10000000 and %change > 2" ;
        logger.debug( "Parsing " + queryStr ) ;
        final ScripITDFilterQueryParser parser = new ScripITDFilterQueryParser( queryStr ) ;
        parser.parse() ;
    }

    public static void main( final String[] args ) throws Exception {
        new ITDFilterQueryTest().test() ;
    }
}
