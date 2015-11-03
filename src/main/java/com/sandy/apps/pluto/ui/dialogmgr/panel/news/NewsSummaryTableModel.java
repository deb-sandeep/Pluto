/**
 * 
 * 
 * 
 *
 * Creation Date: Sep 8, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.news;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.List ;

import javax.swing.table.AbstractTableModel ;

import org.apache.log4j.Logger ;

import com.sandy.apps.pluto.shared.dto.RSSNewsItem ;

/**
 * This class provides the table model required for displaying the job
 * summary information.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class NewsSummaryTableModel extends AbstractTableModel {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 9850964192L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( NewsSummaryTableModel.class ) ;

    // The colums supported by the news overview table
    /** A string array containing the columns in the log table. */
    public static final String[] COL_NAMES = {
        "",
        "Time",
        "Title"
    } ;

    /** A class array containing the object types for individual columns. */
    public static final Class<?>[] COL_CLASS_ARR = {
       Boolean.class,
       Date.class,
       String.class
    } ;

    // The column indices as constants
    public static final int COL_READ   = 0 ;
    public static final int COL_TIME   = 1 ;
    public static final int COL_TITLE  = 2 ;

    /** The collection of news items being managed by this news window. */
    private final List<RSSNewsItem> newsItems = new ArrayList<RSSNewsItem>() ;

    /** Public no argument constructor. */
    public NewsSummaryTableModel() {
    }

    /** Returns the number of columns supported by the ITD summary panel. */
    @Override
    public int getColumnCount() {
        return COL_NAMES.length ;
    }

    /** Returns the number of rows for the table. */
    @Override
    public int getRowCount() {
        return this.newsItems.size() ;
    }

    /** Returns the name of the column at the specified column index. */
    @Override
    public String getColumnName( final int column ) {
        return COL_NAMES[column] ;
    }

    /** None of this table cells are editable. */
    @Override
    public boolean isCellEditable( final int rowIndex, final int columnIndex ) {
        return false ;
    }

    /**
     * Returns the value of the column at the specified row index.
     */
    @Override
    public Object getValueAt( final int rowIndex, final int columnIndex ) {

        Object retVal = null ;

        final RSSNewsItem item = this.newsItems.get( rowIndex ) ;
        switch( columnIndex ) {
            case COL_READ:
                retVal = item.isNewItem() ;
                break ;

            case COL_TIME:
                retVal = item.getPublishDate() ;
                break ;

            case COL_TITLE:
                retVal = item.getTitle() ;
                break ;
        }

        return retVal ;
    }

    /**
     * Returns the instance of the news item at the specified index.
     */
    public RSSNewsItem getNewsItemAtRow( final int rowIndex ) {
        return this.newsItems.get( rowIndex ) ;
    }

    /** Returns the class of the column at the specified column index. */
    @Override
    public Class<?> getColumnClass( final int columnIndex ) {
        return COL_CLASS_ARR[columnIndex] ;
    }

    /** Sets the specified news items as the contents of this table. */
    public void setNewsItems( final List<RSSNewsItem> newsItems ) {
        this.newsItems.clear() ;
        if( newsItems != null ) {
            this.newsItems.addAll( newsItems ) ;
        }
        fireTableDataChanged() ;
    }
}
