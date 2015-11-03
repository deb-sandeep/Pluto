/**
 * Creation Date: Dec 30, 2008
 */

package com.sandy.apps.pluto.biz.svc.impl.scraper;

/**
 * A small data holder class to encapsulate the current parsing position
 * while parsing the scraped HTML contents.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ParsingContext {

    public final String contents ;
    public int    parsePos = 0 ;

    /** Public constructor which takes a string value as its argument. */
    public ParsingContext( final String value ) {
        super() ;
        this.contents = value ;
    }
}
