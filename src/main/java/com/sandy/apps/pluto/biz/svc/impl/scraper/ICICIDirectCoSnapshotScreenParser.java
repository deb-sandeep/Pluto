/**
 * Creation Date: Dec 30, 2008
 */

package com.sandy.apps.pluto.biz.svc.impl.scraper;
import java.util.StringTokenizer ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.biz.svc.INetworkSvc ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.dto.Symbol ;
import com.sandy.apps.pluto.shared.util.util.StringUtil ;

/**
 * This class represents the screen parser for parsing the screen contents
 * scrapped from the ICICI Direct company snapshot screen.
 * <p>
 * http://content.icicidirect.com/research/snapshot.asp?icicicode=[code]
 * </p>
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ICICIDirectCoSnapshotScreenParser {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ICICIDirectCoSnapshotScreenParser.class ) ;

    /** The NSE URL for low resolution ITD index data. */
    private static final String CO_SNAPSHOT_URL = "http://content.icicidirect.com/research/snapshot.asp?icicicode=" ;

    private static final String TAG_IND_NAME = "<td class=\"content\"><b>Industry Name</b></td>" ;
    private static final String TAG_WEBSITE  = "<td class=\"content\"><b>Website</b></td>" ;
    private static final String TAG_TD       = "<td class=\"content\">" ;
    private static final String TAG_TD_END   = "</td>" ;

    /** The network sevice reference. */
    private final INetworkSvc networkSvc ;

    /** Public constructor which accepts a reference to the network service.*/
    public ICICIDirectCoSnapshotScreenParser( final INetworkSvc netSvc ) {
        super() ;
        this.networkSvc = netSvc ;
    }

    /**
     * Fetches the contents of the company snapshot for the specified ICICI
     * direct symbol code and returns a symbol instance with the segment,
     * segment category, icici direct code and the website information filled.
     * Please note that the symbol instance will not have any other data members
     * populated except for the ones mentioned above. Care should be taken to
     * transfer the data to a proper symbol instance before attempting any
     * insert or update information.
     *
     * @param iciciCode The ICICI Direct code for the scrip
     *
     * @return An instance of {@link Symbol}, populated with the following
     *         information. In case a particular value is not present in the
     *         company snapshot, the value will be set to null.
     *         <ul>
     *          <li>The ICICI direct code</li>
     *          <li>The web site information</li>
     *          <li>The scrip segment</li>
     *          <li>The scrip segment category</li>
     *         </ul>
     *
     * @throws STException In case problems are encountered while downloading
     *         the data.
     */
    public Symbol parse( final String iciciCode ) throws STException {

        final String         url      = CO_SNAPSHOT_URL + iciciCode ;
        final String         contents = this.networkSvc.getGETResult( url ) ;
        final Symbol         symbol   = new Symbol() ;
        final ParsingContext ctx      = new ParsingContext( contents ) ;

        parseWebsite( symbol, ctx ) ;
        parseSegment( symbol, ctx ) ;

        symbol.setIciciCode( iciciCode ) ;

        return symbol ;
    }

    /** Parses the web site information and populates it into the symbol instance. */
    private void parseWebsite( final Symbol symbol, final ParsingContext ctx ) {

        int index = ctx.contents.indexOf( TAG_WEBSITE, ctx.parsePos ) ;
        if( index != -1 ) {

            index += TAG_WEBSITE.length() ;
            index  = ctx.contents.indexOf( TAG_TD, index ) ;

            if( index != -1 ) {
                index += TAG_TD.length() ;
                final int endIndex = ctx.contents.indexOf( TAG_TD_END, index ) ;

                if( endIndex != -1 ) {
                    final String site = ctx.contents.substring( index, endIndex ) ;
                    if( StringUtil.isNotEmptyOrNull( site ) ) {
                        symbol.setWebsite( site.trim() ) ;
                        ctx.parsePos = endIndex + TAG_TD_END.length() ;
                    }
                }
            }
        }
    }

    /** Parses the segment information and inserts it into the symbol instance. */
    private void parseSegment( final Symbol symbol, final ParsingContext ctx ) {

        int index = ctx.contents.indexOf( TAG_IND_NAME, ctx.parsePos ) ;
        if( index != -1 ) {

            index += TAG_IND_NAME.length() ;
            index  = ctx.contents.indexOf( TAG_TD, index ) ;

            if( index != -1 ) {
                index += TAG_TD.length() ;
                final int endIndex = ctx.contents.indexOf( TAG_TD_END, index ) ;

                if( endIndex != -1 ) {
                    final String industry = ctx.contents.substring( index, endIndex ) ;
                    if( StringUtil.isNotEmptyOrNull( industry ) ) {

                        final StringTokenizer tokenizer = new StringTokenizer( industry, "-" ) ;
                        for( int i=0; tokenizer.hasMoreTokens(); i++ ) {
                            final String token = tokenizer.nextToken() ;
                            if( i==0 ) {
                                symbol.setSegment( token.trim() ) ;
                            }
                            else if( i==1 ) {
                                symbol.setSegmentCat( token.trim() ) ;
                            }
                        }

                        ctx.parsePos = endIndex + TAG_TD_END.length() ;
                    }
                }
            }
        }
    }
}
