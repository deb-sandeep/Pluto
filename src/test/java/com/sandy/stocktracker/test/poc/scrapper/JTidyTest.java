/**
 * 
 * 
 * 
 *
 * Creation Date: Apr 11, 2011
 */

package com.sandy.stocktracker.test.poc.scrapper;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.w3c.dom.Node ;

import com.sandy.apps.pluto.biz.svc.impl.scraper.HTMLScraper ;
import com.sandy.apps.pluto.shared.util.util.StringUtil ;
import com.sandy.common.util.IOUtils ;

/**
 * This class is used to try out the basics of the scrapper framework.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class JTidyTest {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( JTidyTest.class ) ;

    public void test() throws Exception {
        final String testHTML = getHTMLContent() ;
        if( StringUtil.isEmptyOrNull( testHTML ) ) {
            logger.error( "Could not read sample html file" ) ;
            return ;
        }

        final HTMLScraper scraper = new HTMLScraper( testHTML ) ;
        testScrapper( scraper ) ;
    }

    private void testScrapper( final HTMLScraper scraper ) throws Exception {

        logger.debug( "Evaluating XPath" ) ;

        logger.debug( "Scrape as on " + scraper.stringValueOf( "/html/body/table/tr/td/table/tr/td/table/tr[3]/td" ) ) ;

        final List<Node> nodeList = scraper.selectNodes( "/html/body/table/tr/td/table/tr/td/table/tr[4]/td/table//tr" ) ;

        logger.debug( "Found " + nodeList.size() + " matched nodes" ) ;
        for( int i=0; i<nodeList.size(); i++ ) {
            if( i == 0 ) continue ;
            final Node node = nodeList.get( i ) ;
            printRow( node, scraper ) ;
            System.out.println( "===============================" ) ;
        }
    }

    private void printRow( final Node node, final HTMLScraper scraper ) throws Exception {
        final String[] paths = {
           "td[1]/a" ,
           "td[3]" ,
           "td[4]" ,
           "td[5]" ,
           "td[6]" ,
           "td[7]" ,
           "td[8]"
        } ;

        for( final String path : paths ) {
            logger.debug( path + " = " + scraper.stringValueOf( node, path ) ) ;
        }
    }

    private String getHTMLContent() {
        String retVal = null ;
        final String resPath = "/com/sandy/stocktracker/test/poc/scrapper/sample.html" ;
        try {
            retVal = IOUtils.getPathContents( resPath ) ;
        }
        catch ( final Exception e ) {
            logger.error( "Could not read resource " + resPath ) ;
        }
        return retVal ;
    }

    public static void main( final String[] args ) throws Exception {
        final JTidyTest test = new JTidyTest() ;
        test.test() ;
    }
}
