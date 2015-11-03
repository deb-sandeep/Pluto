/**
 * 
 * 
 * 
 *
 * Creation Date: Oct 16, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.scripeodsummary.filter;
import java.text.ParseException ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;
import java.util.StringTokenizer ;

import javax.swing.RowFilter ;
import javax.swing.RowFilter.ComparisonType ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.shared.util.util.StringUtil ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.scripeodsummary.ScripEODSummaryTableModel ;

/**
 * This class is used to parse the query string provided for the ITD filter
 * into an appropriate instance of {@link RowFilter} to be used on the table
 * for filtering the ITD data set.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ScripEODFilterQueryParser {

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ScripEODFilterQueryParser.class ) ;

    /** The operators supported for the input string. */
    private enum OP { AND, OR, GT, LT, NE, EQ } ;

    // The tokens of the operators supported by this parser.
    private final static String OPS_TOKEN_AND = " AND " ;
    private final static String OPS_TOKEN_OR  = " OR " ;
    private final static String OPS_TOKEN_GT  = ">" ;
    private final static String OPS_TOKEN_LT  = "<" ;
    private final static String OPS_TOKEN_EQ  = "=" ;
    private final static String OPS_TOKEN_NE  = "<>" ;

    // An aggregation of the all the supported operators.
    private final String[] OPS_TOKENS = {
            OPS_TOKEN_AND, OPS_TOKEN_OR, OPS_TOKEN_GT,
            OPS_TOKEN_LT,  OPS_TOKEN_EQ, OPS_TOKEN_NE
    } ;

    // A map to store a easy lookup for the token enumeration value.
    private static final Map<String, OP> TOKEN_OP_MAP = new HashMap<String, OP>() ;
    static {
        TOKEN_OP_MAP.put( OPS_TOKEN_AND, OP.AND ) ;
        TOKEN_OP_MAP.put( OPS_TOKEN_OR,  OP.OR ) ;
        TOKEN_OP_MAP.put( OPS_TOKEN_GT,  OP.GT ) ;
        TOKEN_OP_MAP.put( OPS_TOKEN_LT,  OP.LT ) ;
        TOKEN_OP_MAP.put( OPS_TOKEN_EQ,  OP.EQ ) ;
        TOKEN_OP_MAP.put( OPS_TOKEN_NE,  OP.NE ) ;
    }

    // A map storing the possible combination of column names to their
    // indices. This map is created to maintain multiple mappings of column
    // names to the indexes. This will facilitate ease of query writing by
    // not restricting the user to fixed column names.
    private static final Map<String, Integer> COL_INDEX_MAP = new HashMap<String, Integer>() ;
    static {
        COL_INDEX_MAP.put( "SCRIP",  new Integer( ScripEODSummaryTableModel.COL_SYMBOL ) ) ;
        COL_INDEX_MAP.put( "SYMBOL", new Integer( ScripEODSummaryTableModel.COL_SYMBOL ) ) ;
        COL_INDEX_MAP.put( "NAME",   new Integer( ScripEODSummaryTableModel.COL_SYMBOL ) ) ;

        COL_INDEX_MAP.put( "PRICE", new Integer( ScripEODSummaryTableModel.COL_PRICE ) ) ;
        COL_INDEX_MAP.put( "LTP",   new Integer( ScripEODSummaryTableModel.COL_PRICE ) ) ;
        COL_INDEX_MAP.put( "COST",  new Integer( ScripEODSummaryTableModel.COL_PRICE ) ) ;
        COL_INDEX_MAP.put( "PRC",   new Integer( ScripEODSummaryTableModel.COL_PRICE ) ) ;
        COL_INDEX_MAP.put( "VALUE", new Integer( ScripEODSummaryTableModel.COL_PRICE ) ) ;

        COL_INDEX_MAP.put( "PCTCHANGE", new Integer( ScripEODSummaryTableModel.COL_PCT_E ) ) ;
        COL_INDEX_MAP.put( "%CHANGE",   new Integer( ScripEODSummaryTableModel.COL_PCT_E ) ) ;
        COL_INDEX_MAP.put( "%CHG",      new Integer( ScripEODSummaryTableModel.COL_PCT_E ) ) ;
        COL_INDEX_MAP.put( "%DIFF",     new Integer( ScripEODSummaryTableModel.COL_PCT_E ) ) ;
        COL_INDEX_MAP.put( "PCT",       new Integer( ScripEODSummaryTableModel.COL_PCT_E ) ) ;

        COL_INDEX_MAP.put( "PCTOCHANGE", new Integer( ScripEODSummaryTableModel.COL_PCT_O ) ) ;
        COL_INDEX_MAP.put( "%OCHANGE",   new Integer( ScripEODSummaryTableModel.COL_PCT_O ) ) ;
        COL_INDEX_MAP.put( "%OCHG",      new Integer( ScripEODSummaryTableModel.COL_PCT_O ) ) ;
        COL_INDEX_MAP.put( "%ODIFF",     new Integer( ScripEODSummaryTableModel.COL_PCT_O ) ) ;
        COL_INDEX_MAP.put( "OPCT",       new Integer( ScripEODSummaryTableModel.COL_PCT_O ) ) ;

        COL_INDEX_MAP.put( "QTY",             new Integer( ScripEODSummaryTableModel.COL_QTY ) ) ;
        COL_INDEX_MAP.put( "VOL",             new Integer( ScripEODSummaryTableModel.COL_QTY ) ) ;
        COL_INDEX_MAP.put( "VOLUME",          new Integer( ScripEODSummaryTableModel.COL_QTY ) ) ;
        COL_INDEX_MAP.put( "TRADEQTY",        new Integer( ScripEODSummaryTableModel.COL_QTY ) ) ;
        COL_INDEX_MAP.put( "TOTAL TRADE QTY", new Integer( ScripEODSummaryTableModel.COL_QTY ) ) ;
    }

    /**
     * This class represents a recursive, two children node which will keep the
     * parsed expression tree of the ITD filter search query. A sub query node
     * can contain either a left and node subtree (if the node is not a
     * terminal node) or an empty left, right sub tree (in case of terminal
     * nodes) and a not null value of column and value.
     * <p>
     * Each sub query node is capable of formulating a row filter based on
     * the data it contains.
     *
     * @author Sandeep Deb [deb.sandeep@gmail.com]
     */
    private class SubQuery {

        private SubQuery leftQuery  = null ;
        private SubQuery rightQuery = null ;
        private OP       operator   = null ;
        private Integer  colIndex   = null ;
        private String   value      = null ;
        private RowFilter<Object, Object> rowFilter = null ;

        private final boolean isRoot ;

        public SubQuery( final boolean root ) {
            super() ;
            this.isRoot = root ;
        }

        /**
         * This method recursively parses the input string and creates a
         * parse tree out of the same.
         *
         * @param input The input to parse
         *
         * @throws ParseException In case the input is not a valid input,
         *         this method will generate a parse exception with the
         *         appropriate error message.
         */
        public void parse( final String input ) throws ParseException {

            int     index = -1 ;
            String  token = null ;
            String  lhsVal= null ;
            String  rhsVal= null ;

            for( int i=0; i<ScripEODFilterQueryParser.this.OPS_TOKENS.length; i++ ) {
                token = ScripEODFilterQueryParser.this.OPS_TOKENS[i] ;
                index = input.indexOf( token ) ;

                if( index > 0 ) {

                    lhsVal = input.substring( 0, index ).trim() ;
                    rhsVal = input.substring( index + token.length() ).trim() ;

                    if( StringUtil.isEmptyOrNull( lhsVal ) ) {
                        throw new ParseException( "Left hand expression for " +
                                "token " + token + " is not specified.", 0 ) ;
                    }

                    if( StringUtil.isEmptyOrNull( rhsVal ) ) {
                        throw new ParseException( "Right hand expression for " +
                                "token " + token + " is not specified.", 0 ) ;
                    }

                    if( token.equals( OPS_TOKEN_AND ) || token.equals( OPS_TOKEN_OR ) ) {
                        this.leftQuery = new SubQuery( false ) ;
                        this.leftQuery.parse( lhsVal ) ;
                        this.rightQuery = new SubQuery( false ) ;
                        this.rightQuery.parse( rhsVal ) ;
                    }
                    else {
                        this.colIndex = COL_INDEX_MAP.get( lhsVal ) ;
                        this.value  = rhsVal ;

                        if( this.colIndex == null ) {
                            throw new ParseException( "Invalid column : " + lhsVal +
                                                 " specified in the query", 0 ) ;
                        }

                        if( this.colIndex != ScripEODSummaryTableModel.COL_SYMBOL ) {
                            try {
                                Double.parseDouble( this.value ) ;
                            }
                            catch ( final Exception e ) {
                                if( this.colIndex == null ) {
                                    throw new ParseException(
                                            "Value " + this.value +
                                            " should be a decimal", 0 ) ;
                                }
                            }
                        }
                    }

                    this.operator = TOKEN_OP_MAP.get( token ) ;
                    return ;
                }
            }

            // If the control reaches here, it implies that the query string
            // does not contain any tokens - oops wrong input. Unless this
            // is the root node. Note that the user might just enter the
            // Scrip name in the search box without the name = scrip format
            // We need to respect that too. If this is not a root node,
            // we generate a parse exception
            if( !this.isRoot ) {
                throw new ParseException( "No tokens found in the input string", 0 ) ;
            }
            else {
                this.colIndex = COL_INDEX_MAP.get( ScripEODSummaryTableModel.COL_SYMBOL ) ;
                this.value = input ;

                final List<RowFilter<Object, Object>> filters =
                                     new ArrayList<RowFilter<Object,Object>>() ;
                final StringTokenizer tokenizer = new StringTokenizer( this.value, "," ) ;

                while( tokenizer.hasMoreTokens() ) {
                    final String regex = tokenizer.nextToken().trim() ;
                    filters.add( RowFilter.regexFilter( regex, ScripEODSummaryTableModel.COL_SYMBOL ) ) ;
                    filters.add( RowFilter.regexFilter( regex, ScripEODSummaryTableModel.COL_ICICI ) ) ;
                    filters.add( RowFilter.regexFilter( regex, ScripEODSummaryTableModel.COL_NAME ) ) ;
                }
                this.rowFilter = RowFilter.orFilter( filters ) ;
            }
        }

        private boolean isLeafNode() {
            return this.leftQuery == null ;
        }

        /**
         * Returns a row filter for the current subtree. If this node represents
         * the root of the query parse tree, it returns the row filter
         * for the complete query string.
         *
         * @return A row filter instance corresponding to the query string
         *         parsed.
         */
        public RowFilter<Object, Object> getRowFilter() {

            if( this.rowFilter == null ) {

                final List<RowFilter<Object, Object>> filters =
                                      new ArrayList<RowFilter<Object,Object>>() ;

                if( !isLeafNode() ) {

                    filters.add( this.leftQuery.getRowFilter() ) ;
                    filters.add( this.rightQuery.getRowFilter() ) ;

                    switch( this.operator ) {
                        case AND:
                            this.rowFilter = RowFilter.andFilter( filters ) ;
                            break ;
                        case OR:
                            this.rowFilter = RowFilter.orFilter( filters ) ;
                            break ;
                    }
                }
                else {
                    if( this.colIndex == ScripEODSummaryTableModel.COL_SYMBOL ) {

                        final StringTokenizer tokenizer = new StringTokenizer( this.value, "," ) ;
                        while( tokenizer.hasMoreTokens() ) {
                            final String regex = tokenizer.nextToken().trim() ;
                            filters.add( RowFilter.regexFilter( regex, ScripEODSummaryTableModel.COL_SYMBOL ) ) ;
                        }
                        this.rowFilter = RowFilter.orFilter( filters ) ;
                    }
                    else {
                        final Number number = Double.parseDouble( this.value ) ;
                        ComparisonType compType = null ;
                        switch( this.operator ) {
                            case EQ:
                                compType = ComparisonType.EQUAL ;
                                break ;
                            case NE:
                                compType = ComparisonType.NOT_EQUAL ;
                                break ;
                            case LT:
                                compType = ComparisonType.BEFORE ;
                                break ;
                            case GT:
                                compType = ComparisonType.AFTER ;
                                break ;
                        }
                        this.rowFilter = RowFilter.numberFilter( compType, number, this.colIndex ) ;
                    }
                }
            }

            return this.rowFilter ;
        }
    }

    // The query string to be parsed.
    private final String inputQueryStr ;

    // The root node of the query.
    private SubQuery queryRoot = null ;

    /**
     * Constructor, which takes in the input string to be parsed. The user
     * can call on the parse method on this instance to convert the input
     * string into an instance of {@link RowFilter}.
     *
     * @param input The filter query string.
     */
    public ScripEODFilterQueryParser( final String inputQuery ) {
        this.inputQueryStr = inputQuery ;
    }

    public RowFilter<Object, Object> parse() throws ParseException {
        if( this.queryRoot == null ) {
            this.queryRoot = new SubQuery( true ) ;
            this.queryRoot.parse( this.inputQueryStr.trim().toUpperCase() ) ;
        }

        return this.queryRoot.getRowFilter() ;
    }
}
