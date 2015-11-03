/**
 * Creation Date: Apr 11, 2011
 */

package com.sandy.apps.pluto.biz.svc.impl.scraper;
import java.io.ByteArrayOutputStream ;
import java.io.StringReader ;
import java.util.List ;

import org.apache.log4j.Logger ;
import org.jaxen.dom.DOMXPath ;
import org.w3c.dom.Document ;
import org.w3c.dom.Node ;
import org.w3c.tidy.Tidy ;

/**
 * This class contains method to help scrape data out of an HTML string
 * via XPath queries.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class HTMLScraper {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( HTMLScraper.class ) ;

    // The DOM model of the HTML content.
    private final Document doc ;

    private final Tidy tidy = new Tidy() ;

    /** Constructor which takes in the HTML string to scrape. */
    public HTMLScraper( final String html ) {

        this.tidy.setQuiet( true ) ;
        this.tidy.setShowWarnings( false ) ;
        this.tidy.setXHTML( true ) ;
        this.tidy.setIndentContent( true ) ;
        this.doc = this.tidy.parseDOM( new StringReader( html ), null ) ;
    }

    /** Returns the pretty formatted document. */
    public String toString() {

        final ByteArrayOutputStream bOs = new ByteArrayOutputStream() ;
        this.tidy.pprint( this.doc, bOs ) ;
        return bOs.toString() ;
    }

    /**
     * Select all nodes that are selected by this XPath expression. If multiple
     * nodes match, multiple nodes will be returned. Nodes will be returned in
     * document-order, as defined by the XPath specification. If the expression
     * selects a non-node-set (i.e. a number, boolean, or string) then a List
     * containing just that one object is returned.
     *
     * Note that the xPath is evaluated against the root document.
     *
     * @param xPath The xPath expression to be evaluated to select the nodes.
     */
    @SuppressWarnings("unchecked")
    public List<Node> selectNodes( final String xPath ) throws Exception {

        final DOMXPath domXPath = new DOMXPath( xPath ) ;
        return domXPath.selectNodes( this.doc ) ;
    }

    /**
     * Select all nodes that are selected by this XPath expression. If multiple
     * nodes match, multiple nodes will be returned. Nodes will be returned in
     * document-order, as defined by the XPath specification. If the expression
     * selects a non-node-set (i.e. a number, boolean, or string) then a List
     * containing just that one object is returned.
     *
     * @param xPath The xPath expression to be evaluated to select the nodes.
     *
     * @param node The Node against which the xPath will be evaluated.
     */
    @SuppressWarnings("unchecked")
    public List<Node> selectNodes( final Object node, final String xPath )
        throws Exception {

        final DOMXPath domXPath = new DOMXPath( xPath ) ;
        return domXPath.selectNodes( node ) ;
    }

    /**
     * Select only the first node selected by this XPath expression. If multiple
     * nodes match, only one node will be returned. The selected node will be
     * the first selected node in document-order, as defined by the XPath
     * specification.
     *
     * Note that the xPath is evaluated against the root document.
     *
     * @param xPath The xPath expression to be evaluated to select the nodes.
     */
    public Node selectSingleNode( final String xPath ) throws Exception {

        final DOMXPath domXPath = new DOMXPath( xPath ) ;
        return ( Node )domXPath.selectSingleNode( this.doc ) ;
    }

    /**
     * Select only the first node selected by this XPath expression. If multiple
     * nodes match, only one node will be returned. The selected node will be
     * the first selected node in document-order, as defined by the XPath
     * specification.
     *
     * @param xPath The xPath expression to be evaluated to select the nodes.
     * @param node The Node against which the xPath will be evaluated.
     */
    public Node selectSingleNode( final Object node, final String xPath )
        throws Exception {

        final DOMXPath domXPath = new DOMXPath( xPath ) ;
        return ( Node )domXPath.selectSingleNode( node ) ;
    }

    /**
     * Retrieves the string-value of the result of evaluating this XPath
     * expression when evaluated against the document.
     *
     * The string-value of the expression is determined per the string(..) core
     * function defined in the XPath specification. This means that an
     * expression that selects zero nodes will return the empty string, while
     * an expression that selects one-or-more nodes will return the string-value
     * of the first node.
     *
     * @param xPath The xPath whose value need to be extracted
     */
    public String stringValueOf( final String xPath ) throws Exception {

        final DOMXPath domXPath = new DOMXPath( xPath ) ;
        return domXPath.stringValueOf( this.doc ) ;
    }

    /**
     * Retrieves the string-value of the result of evaluating this XPath
     * expression when evaluated against the document.
     *
     * The string-value of the expression is determined per the string(..) core
     * function defined in the XPath specification. This means that an
     * expression that selects zero nodes will return the empty string, while
     * an expression that selects one-or-more nodes will return the string-value
     * of the first node.
     *
     * @param xPath The xPath whose value need to be extracted
     * @param node The Node against which the xPath will be evaluated.
     */
    public String stringValueOf( final Object node, final String xPath )
        throws Exception {

        final DOMXPath domXPath = new DOMXPath( xPath ) ;
        return domXPath.stringValueOf( node ) ;
    }
}
