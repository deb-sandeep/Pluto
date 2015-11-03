/**
 * 
 * 
 * 
 *
 * Creation Date: Nov 28, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.news;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.shared.dto.RSSNewsItem ;
import com.sandy.apps.pluto.shared.dto.RSSNewsItemSource ;
import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.UIHelper ;

/**
 * This class renders the tree nodes for the news category tree.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class NewsTreeCellRenderer extends DefaultTreeCellRenderer {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( NewsTreeCellRenderer.class ) ;

    /**
     * A reference to the news category map for easy data access. Please note
     * that this class performs read only operations on the map and does not
     * manipulate the contents of the map in any way.
     *
     * A map which stores the news items categorized on the basis of their site
     * and category. Any new news items added in transit will be moved down
     * to their proper root node and stored. If the value of a key is a map,
     * the key represents a non terminal node, else if the value is a list the
     * key is a terminal node.
     */
    private final Map<String, Object> newsMap ;


    /** Public constructor which takes a reference of the news map. */
    public NewsTreeCellRenderer( final Map<String, Object> newsMap ) {
        this.newsMap = newsMap ;
    }

    /** Renders the tree node. */
    public Component getTreeCellRendererComponent(
                                    final JTree tree,   final Object value,
                                    final boolean sel,  final boolean expanded,
                                    final boolean leaf, final int row,
                                    final boolean isFocussed ) {

        // Collect the user object from the value we are going to render. Note
        // that we have made the tree nodes as DefaultMutableTreeNode and the
        // user user resides inside that. Also note that our tree is always 3
        // levels deep, with the root being a String, level 1 being the site
        // name with the user object as an instance of String and the 2nd level
        // node is an instance of RSSNewsItemSource class.
        final Object userObject = (( DefaultMutableTreeNode )value).getUserObject() ;

        // Delegate the rendering of the label to the super class, which will
        // take care of the default rendering in terms of focus, selection etc.
        // We will decorate the label after that.
        final JLabel label = ( JLabel )super.getTreeCellRendererComponent(
                            tree, value, sel, expanded, leaf, row, isFocussed ) ;

        // Set the font
        label.setFont( UIConstant.NEWS_SUMMARY_FONT ) ;

        // Extrat the path to the node. The path will give us the depth of the
        // node, helping us to make the rendering decisions.
        final TreePath path = tree.getPathForRow( row ) ;
        if( path != null ) {
            final Object[] pathComponents = path.getPath() ;
            setIcon( pathComponents, label, userObject ) ;
            setText( pathComponents, label, userObject ) ;
        }

        return label ;
    }

    /**
     * A helper method which sets the text of the tree node. The text of the
     * tree node consists of how many unread items are present in the node.
     *
     * @param pathComps The path components which point to the node we are
     *        dealing with.
     * @param label The label on which we are to set the text.
     * @param value The user object associated with the node. The type of the
     *        user object varies with the tree depth as documented below:
     *        <ul>
     *          <li>Depth 1 [Root node], java.lang.String</li>
     *          <li>Depth 2 [Site node], java.lang.String</li>
     *          <li>Depth 3 [Category node], {@link RSSNewsItemSource}</li>
     *        </ul>
     */
    @SuppressWarnings("unchecked")
    private void setText( final Object[] pathComps, final JLabel label,
                          final Object userObject ) {

        int unreadCount = 0 ;
        Map<String, List<RSSNewsItem>> siteMap = null ;
        String labelText = null ;

        if( pathComps.length == NewsCategoryTree.ROOT_NODE_DEPTH ) {
            // Implies the root node has been selected. Get the unread count of
            // the entire tree
            for( final String site : this.newsMap.keySet() ) {
                siteMap = ( Map<String, List<RSSNewsItem>> ) this.newsMap.get( site ) ;
                if( siteMap != null ) {
                    for( final String cat : siteMap.keySet() ) {
                        unreadCount += getUnreadCountForCategory( site, cat ) ;
                    }
                }
            }

            labelText = userObject.toString() ;
        }
        else if( pathComps.length == NewsCategoryTree.SITE_NODE_DEPTH ) {
            // Implies a site has been selected, get the unread count of all
            // the categories in the site. Note that the user object for a level
            // 2 node is a string, the value of which is the site identifier.
            final String site = ( String )userObject ;

            siteMap = ( Map<String, List<RSSNewsItem>> ) this.newsMap.get( site ) ;
            if( siteMap != null ) {
                for( final String cat : siteMap.keySet() ) {
                    unreadCount += getUnreadCountForCategory( site, cat ) ;
                }
            }

            labelText = userObject.toString() ;
        }
        else if( pathComps.length == NewsCategoryTree.CAT_NODE_DEPTH ) {
            // Implies a category has been selected, get the unread count of
            // the selected category. Note that the user object associated with a
            // level 2 node is a String (site identifier), while the user object
            // associated with a level 3 node is an instance of RSSNewsItemSource
            final DefaultMutableTreeNode siteNode = ( DefaultMutableTreeNode )pathComps[1] ;
            final RSSNewsItemSource      catNode  = ( RSSNewsItemSource )userObject ;

            unreadCount = getUnreadCountForCategory( siteNode.getUserObject().toString(),
                                                     catNode.getCategory() ) ;

            labelText = catNode.getCategory() ;
        }
        else {
            throw new IllegalStateException( "Unanticipated tree depth encountered." ) ;
        }


        if( unreadCount > 0 ) {
            label.setFont( label.getFont().deriveFont( Font.BOLD ) ) ;
            label.setText( labelText + " (" + unreadCount + ")" ) ;
        }
        else {
            label.setFont( label.getFont().deriveFont( Font.PLAIN ) ) ;
            label.setText( labelText ) ;
        }
    }

    /** Returns the unread count for the site and category specified. */
    @SuppressWarnings("unchecked")
    private int getUnreadCountForCategory( final String site, final String cat ) {

        int count = 0 ;
        final Map<String, List<RSSNewsItem>> siteMap =
                   ( Map<String, List<RSSNewsItem>> )this.newsMap.get( site ) ;

        // Note - site map can be null since we have the tree and news map
        // created from the news sources and list of past news items respectively.
        if( siteMap != null ) {
            final List<RSSNewsItem> newsItemList = siteMap.get( cat ) ;
            if( newsItemList != null ) {
                for( final RSSNewsItem item : newsItemList ) {
                    if( item.isNewItem() ) count++ ;
                }
            }
        }

        return count ;
    }

    /**
     * A helper method to set the icon for a tree node. Depending upon the
     * depth of the node, an appropriate icon is set.
     *
     * @param pathComps The path components which point to the node we are
     *        dealing with.
     * @param label The label on which we are to set the text.
     * @param value The user object associated with the node.
     *
     * @param label The label on which the icon needs to be set.
     */
    @SuppressWarnings("unchecked")
    private void setIcon( final Object[] pathComps, final JLabel label,
                          final Object value ) {

        final int depth = pathComps.length ;
        Image iconImage = null ;

        if( depth == NewsCategoryTree.ROOT_NODE_DEPTH ) {
            // The root node is always the RSS icon. No further iconic decoration
            // is warranted
            iconImage = UIHelper.IMG_RSS_BIG ;
        }
        else if( depth == NewsCategoryTree.SITE_NODE_DEPTH ) {
            // A site node is marked with an inactive icon if all the child
            // category icons are inactive, else it's marked as active
            final DefaultMutableTreeNode node = ( DefaultMutableTreeNode )pathComps[1] ;
            boolean active = true ;

            for( final Enumeration<DefaultMutableTreeNode> children = node.children();
                 children.hasMoreElements(); ) {

                RSSNewsItemSource newsSource = null ;
                newsSource = ( RSSNewsItemSource )children.nextElement().getUserObject() ;
                active &= newsSource.isActive() ;
            }

            iconImage = ( active ) ? UIHelper.IMG_FEED_ACTIVE : UIHelper.IMG_FEED_INACTIVE ;
        }
        else if( depth == NewsCategoryTree.CAT_NODE_DEPTH ){
            // A category node is marked as active if the associated source is active
            final RSSNewsItemSource source = ( RSSNewsItemSource )value ;
            iconImage = ( source.isActive() ) ? UIHelper.IMG_FEED_ACTIVE :
                                                UIHelper.IMG_FEED_INACTIVE ;
        }
        else {
            throw new IllegalStateException( "Unanticiated depth encountered" ) ;
        }

        label.setIcon( new ImageIcon( iconImage ) ) ;
    }
}
