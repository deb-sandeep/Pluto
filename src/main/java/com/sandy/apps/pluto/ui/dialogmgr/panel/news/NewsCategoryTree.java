/**
 * 
 * 
 * 
 *
 * Creation Date: Dec 4, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.news;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.biz.svc.IRSSSvc ;
import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.ServiceMgr ;
import com.sandy.apps.pluto.shared.dto.RSSNewsItem ;
import com.sandy.apps.pluto.shared.dto.RSSNewsItemSource ;
import com.sandy.apps.pluto.ui.UIConstant ;

/**
 * This class represents the category tree for the news display panel. This
 * class also encapsulates operations that can be performed by the user
 * on the tree, for example, refreshing a particular node, adding a category
 * to a site, adding a site etc.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class NewsCategoryTree extends JTree
    implements UIConstant, TreeSelectionListener {

    /** Default serial version UID. This tree will never be serialized. */
    private static final long serialVersionUID = 1L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( NewsCategoryTree.class ) ;

    /** Static constant for the depth of the site node. */
    public static final int ROOT_NODE_DEPTH = 1 ;
    public static final int SITE_NODE_DEPTH = 2 ;
    public static final int CAT_NODE_DEPTH  = 3 ;

    /** The root node of the tree. */
    private final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode( "Sites" ) ;

    /**
     * A reference to the news category map for easy data access. Please note
     * that this class performs read only operations on the map and does not
     * manipulate the contents of the map in any way.
     */
    private final Map<String, Object> newsMap ;

    /**
     * A reference to the news panel which holds this tree. This tree will use
     * this reference to call back methods on the panel to relay the user
     * initiated actions on the tree. The panel acts as a mediator which
     * translates calls inititated by this class to actions on other panel
     * participants.
     */
    private final NewsPanel newsPanel ;

    /**
     * Public constructor.
     *
     * @param newsMap The pre loaded news map which contains past news items.
     * @param newsPanel The parent panel of which this tree is a part.
     */
    public NewsCategoryTree( final Map<String, Object> newsMap,
                             final NewsPanel newsPanel ) {
        this.newsMap = newsMap ;
        this.newsPanel = newsPanel ;
    }

    /** A helper method to initialize the tree. */
    public void initialize() throws STException {

        (( DefaultTreeModel )getModel()).setRoot( this.rootNode ) ;

        setRowHeight( 15 ) ;
        setFont( LOG_FONT ) ;
        getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION ) ;
        setRootVisible( true ) ;
        setCellRenderer( new NewsTreeCellRenderer( this.newsMap ) ) ;
        setLargeModel( true ) ;
        addTreeSelectionListener( this ) ;
        setSelectionRow( 0 ) ;

        // Now create the tree nodes based on the news sources
        DefaultMutableTreeNode siteNode = null ;
        DefaultMutableTreeNode catNode  = null ;

        final IRSSSvc rssSvc = ServiceMgr.getRSSSvc() ;

        // A list of all the news item sources registered in the application
        final List<RSSNewsItemSource> sources = rssSvc.getNewsSources( true ) ;

        // A map which categorizes the sources, based on their site name.
        final Map<String, List<RSSNewsItemSource>> siteCatMap = new HashMap<String, List<RSSNewsItemSource>>() ;

        // Create a tree mapping of the site and category relationships, so
        // that we can build a tree easily.
        for( final RSSNewsItemSource source : sources ) {

            final String site = source.getSite() ;
            List<RSSNewsItemSource> catList = siteCatMap.get( site ) ;
            if( catList == null ) {
                catList = new ArrayList<RSSNewsItemSource>() ;
                siteCatMap.put( site, catList ) ;
            }

            catList.add( source ) ;
        }

        // Now build the node root structure.
        for( final String site : siteCatMap.keySet() ) {
            siteNode = new DefaultMutableTreeNode( site ) ;
            final List<RSSNewsItemSource> catList = siteCatMap.get( site ) ;
            if( catList != null ) {
                for( final RSSNewsItemSource cat : catList ) {
                    catNode = new DefaultMutableTreeNode( cat ) ;
                    siteNode.add( catNode ) ;
                }
            }
            this.rootNode.add( siteNode ) ;
        }

        // Now expand all the nodes of the tree. Strangely there is no other
        // way (convenience API) for expanding the entire tree.
        for( int i=0; i<getRowCount(); i++ ) {
            expandRow( i ) ;
        }
    }

    @Override
    public void valueChanged( final TreeSelectionEvent e ) {

        final TreePath selPath = e.getNewLeadSelectionPath() ;
        final List<RSSNewsItem> newsItems = getNewsItemsForPath( selPath.getPath() ) ;

        this.newsPanel.setNewsItemsInTable( newsItems ) ;
        this.newsPanel.setDescription( "" ) ;
        this.newsPanel.setNewsTitle( "" ) ;
    }

    /**
     * A helper method which gets an aggregate of news items for the
     * path specified.
     *
     * @param pathComps The path components which point to the node we are
     *        dealing with.
     */
    @SuppressWarnings("unchecked")
    private List<RSSNewsItem> getNewsItemsForPath( final Object[] pathComps ) {

        final List<RSSNewsItem> newsItems = new ArrayList<RSSNewsItem>() ;
        Map<String, List<RSSNewsItem>> siteMap = null ;

        if( pathComps.length == ROOT_NODE_DEPTH ) {
            // Implies the root node has been selected. Get the unread count of
            // the entire tree
            for( final String site : this.newsMap.keySet() ) {
                siteMap = ( Map<String, List<RSSNewsItem>> ) this.newsMap.get( site ) ;
                for( final String cat : siteMap.keySet() ) {
                    newsItems.addAll( this.newsPanel.getNewsItems( site, cat ) ) ;
                }
            }
        }
        else if( pathComps.length == SITE_NODE_DEPTH ) {
            // Implies a site has been selected, get the unread count of all
            // the categories in the site
            final DefaultMutableTreeNode node = ( DefaultMutableTreeNode )pathComps[1] ;
            final String site = node.getUserObject().toString() ;

            siteMap = ( Map<String, List<RSSNewsItem>> ) this.newsMap.get( site ) ;
            if( siteMap != null ) {
                for( final String cat : siteMap.keySet() ) {
                    newsItems.addAll( this.newsPanel.getNewsItems( site, cat ) ) ;
                }
            }
        }
        else if( pathComps.length == CAT_NODE_DEPTH ) {
            // Implies a category has been selected, get the unread count of
            // the selected category
            final DefaultMutableTreeNode siteNode = ( DefaultMutableTreeNode )pathComps[1] ;
            final DefaultMutableTreeNode catNode  = ( DefaultMutableTreeNode )pathComps[2] ;
            final RSSNewsItemSource      source   = ( RSSNewsItemSource )catNode.getUserObject() ;

            newsItems.addAll( this.newsPanel.getNewsItems( siteNode.getUserObject().toString(),
                                                           source.getCategory() ) ) ;
        }

        Collections.sort( newsItems ) ;
        return newsItems ;
    }
}
