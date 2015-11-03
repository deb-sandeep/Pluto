/**
 * Creation Date: Oct 10, 2008
 */

package com.sandy.apps.pluto.ui;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.Stack;

/**
 * This class provides a simple stack for saving and retrieving graphics
 * contexts from multiple call sequences. This class frees the application
 * code from the overhead of preserving graphics attributes and reloading
 * them frequently at the change of the graphics context.s
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class GraphicsContextStack {

    private final static Stack<GraphicsContext> stack = new Stack<GraphicsContext>() ;

    private static class GraphicsContext {

        public Color  color  = null ;
        public Font   font   = null ;
        public Stroke stroke = null ;
        public int    transX = 0 ;
        public int    transY = 0 ;

        public void save( final Graphics2D g, final Rectangle trans ) {
            this.color = g.getColor() ;
            this.font  = g.getFont() ;
            this.stroke= g.getStroke() ;

            if( trans != null ) {
                g.translate( trans.x, trans.y ) ;
                this.transX= trans.x ;
                this.transY= trans.y ;
            }
        }

        public void load( final Graphics2D g ) {
            if( this.color  != null ) { g.setColor( this.color ) ; }
            if( this.font   != null ) { g.setFont( this.font ) ; }
            if( this.stroke != null ) { g.setStroke( this.stroke ) ; }
            g.translate( -1*this.transX, -1*this.transY ) ;
        }
    }

    private GraphicsContextStack(){
    }

    public static void push( final Graphics2D g ) {
        final GraphicsContext ctx = new GraphicsContext() ;
        ctx.save( g, null ) ;
        stack.push( ctx ) ;
    }

    public static void push( final Graphics2D g, final Rectangle trans ) {
        final GraphicsContext ctx = new GraphicsContext() ;
        ctx.save( g, trans ) ;
        stack.push( ctx ) ;
    }

    public static void pop( final Graphics2D g ) {

        if( !stack.isEmpty() ) {
            final GraphicsContext ctx = stack.pop() ;
            ctx.load( g ) ;
        }
    }
}
