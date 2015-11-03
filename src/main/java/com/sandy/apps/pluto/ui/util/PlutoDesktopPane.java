/**
 * 
 * 
 * 
 *
 * Creation Date: Oct 15, 2008
 */

package com.sandy.apps.pluto.ui.util;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.StockTracker ;
import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoFrameType ;
import com.sandy.apps.pluto.ui.dialogmgr.PlutoInternalFrame ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.ChartingPanel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartEntityConfig ;

/**
 * This class extends the Swing provided JDesktopPane and adds on the functionality
 * of painting a background image. Also note that the desktop supports drop
 * operations. The user can drag scrip names from other windows and drop them
 * on the desktop.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class PlutoDesktopPane extends JDesktopPane
    implements UIConstant {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( PlutoDesktopPane.class ) ;

    /** The background image to paint. */
    private final Image bgImage ;

    /** The listener which will listen to drops on this class. */
    private final DropTargetListener dropTgtListener = new DropTargetAdapter() {

        /**
         * This method is invoked by Swing when the user drop some dragable
         * item on the component for which this listener is engaged to
         * listen to drops.
         */
        @Override
        public void drop( final DropTargetDropEvent dtde ) {

            ChartEntityConfig entityConfig  = null ;
            List<ChartEntityConfig> cfgList = null ;

            final Point     location = dtde.getLocation() ;
            final Component dropComp = PlutoDesktopPane.this.getComponentAt( location ) ;

            if( dropComp == PlutoDesktopPane.this ) {
                try {
                    final Transferable tfr     = dtde.getTransferable() ;
                    final DataFlavor[] flavors = tfr.getTransferDataFlavors() ;

                    if( flavors[0] == CHART_ENTITY_DATA_FLAVOR ) {

                        entityConfig = ( ChartEntityConfig )tfr.getTransferData( CHART_ENTITY_DATA_FLAVOR ) ;
                        cfgList = new ArrayList<ChartEntityConfig>() ;
                        cfgList.add( entityConfig ) ;

                        final ChartingPanel panel  = new ChartingPanel() ;
                        final PlutoInternalFrame dialog = new PlutoInternalFrame(
                                            "Charting", JFrame.DISPOSE_ON_CLOSE,
                                            PlutoFrameType.CHART_FRAME, panel ) ;

                        panel.addChartEntities( cfgList ) ;

                        dialog.setSize( ChartingPanel.DEFAULT_WIDTH, ChartingPanel.DEFAULT_HEIGHT ) ;
                        dialog.setResizable( true ) ;

                        StockTracker.MAIN_FRAME.addInternalFrame( dialog, true ) ;
                    }
                }
                catch ( final Exception e ) {
                    // Ignore the drop operation if it is not supported.
                }
            }
        }
    } ;

    /**
     * Public constructor which takes in the image it has to paint as the
     * desktop background.
     */
    public PlutoDesktopPane( final Image bgImage ) {
        super() ;
        this.bgImage = bgImage ;
        new DropTarget( this, this.dropTgtListener ) ;
        super.setDragMode( JDesktopPane.OUTLINE_DRAG_MODE ) ;
    }

    /**
     * This method is called by the Swing framework when it wants to paint a
     * component. In this case the desktop. This method does the normal processing
     * and also helps paint the PLUTO logo in the background of the component.
     */
    protected void paintComponent( final Graphics g ) {
        super.paintComponent( g ) ;

        final Dimension size = super.getSize() ;
        final int       imgW = this.bgImage.getWidth( this ) ;
        final int       imgH = this.bgImage.getHeight( this ) ;

        final int imgX = size.width/2 - imgW/2 ;
        final int imgY = size.height/2 - imgH/2 ;

        g.drawImage( this.bgImage, imgX, imgY, this ) ;
    }
}
