/**
 * 
 * 
 * 
 *
 * Creation Date: Oct 17, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.charting;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.ui.UIConstant ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartEntity ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartModel ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.EODValue ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ITDValue ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartModel.ModelEvent ;
import com.sandy.apps.pluto.ui.dialogmgr.panel.charting.model.ChartModel.ModelListener ;

/**
 * This panel shows the primary charting entities and their moving values on
 * the right hand side of the chart panel. This panel will be notified when
 * a primary entity is being added or removed from the chart. This panel
 * also supports operations to change the price value of a particular primary
 * entity.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ChartEntityEditPanel extends JPanel
    implements UIConstant, ModelListener {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 561684807893431129L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ChartEntityEditPanel.class ) ;

    /**
     * A private class to encapsulate all the visual information regarding
     * a primary entity.
     *
     * @author Sandeep Deb [deb.sandeep@gmail.com]
     */
    private class EntityPanel extends JPanel {

        /** Generated serial version UID. */
        private static final long serialVersionUID = 561684807893431129L ;

        /** The decimal format used to render the value of the entity. */
        private final DecimalFormat VAL_FMT = new DecimalFormat( "###.00" ) ;

        /** The dimension of this panel. */
        private final Dimension DIM = new Dimension( 100, 15 ) ;

        /** The name of the entity that this panel is representing. */
        private final ChartEntity entity ;

        /**
         * The button on which the entities name will be displayed. This button
         * will also serve as a trigger to open the entity editing dialog.
         */
        private final JButton entityBtn ;

        /** The label on which the entities value will be displayed. */
        private final JLabel valueLabel ;

        public EntityPanel( final ChartEntity entity ) {
            this.entity = entity ;
            this.entityBtn = getParticipantButton() ;
            this.valueLabel = getParticipantValueLabel() ;

            setUpUI() ;
        }

        /** Sets up the user interface of the entity panel. */
        private void setUpUI() {
            setBackground( Color.black ) ;
            setLayout( new BorderLayout() ) ;
            add( this.entityBtn, BorderLayout.CENTER ) ;
            add( this.valueLabel, BorderLayout.EAST ) ;
            setPreferredSize( this.DIM ) ;
            setMinimumSize( this.DIM ) ;
            setMaximumSize( this.DIM ) ;
            setBorder( BorderFactory.createEmptyBorder( 0, 0, 5, 0 ) ) ;
        }

        private JLabel getParticipantValueLabel() {
            final JLabel label = new JLabel() ;
            label.setBorder( BorderFactory.createEmptyBorder( 0, 4, 0, 0 ) ) ;
            label.setOpaque( true ) ;
            label.setFont( LOG_FONT ) ;
            label.setBackground( Color.black ) ;
            label.setForeground( Color.LIGHT_GRAY ) ;
            label.setHorizontalAlignment( JLabel.RIGHT ) ;
            label.setText( "000.00" ) ;
            return label ;
        }

        private JButton getParticipantButton() {
            final JButton button = new JButton() ;
            button.setText( this.entity.getName() ) ;
            button.setFont( LOG_FONT ) ;
            button.setForeground( this.entity.getColor() ) ;
            button.setBackground( Color.black ) ;
            button.setBorderPainted( true ) ;
            button.setContentAreaFilled( true ) ;
            button.setFocusPainted( false ) ;
            button.setBorder( EMPTY_BORDER ) ;
            button.setHorizontalAlignment( JButton.LEFT ) ;
            return button ;
        }

        /** Sets the value of the entity to the specified value. */
        public void setValue( final double newValue ) {
            final String fmtVal = this.VAL_FMT.format( newValue ) ;
            this.valueLabel.setText( fmtVal ) ;
        }
    }

    /** A map of the primary entity versus their entity panel. */
    private final Map<String, EntityPanel> entityPanelMap = new HashMap<String, EntityPanel>() ;

    /** A reference to the model of this chart. */
    private final ChartModel model ;

    /** Constructor. */
    public ChartEntityEditPanel( final ChartModel model ) {
        super() ;
        setBackground( Color.black ) ;
        setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 3 ) ) ;
        setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) ) ;
        this.model = model ;
    }

    /**
     * This method is invoked when the underlying chart model has changed. The
     * nature of the change can be ascertained by evaluating the type of the
     * event being generated.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void modelChanged( final ModelEvent event ) {

        EntityPanel panel  = null ;
        String      entityName = null ;

        switch( event.getType() ) {
            case PRIMARY_SERIES_ADDED:

                final List<ChartEntity> entities = ( List<ChartEntity> )event.getValue() ;
                for( final ChartEntity entity : entities ) {
                    panel      = new EntityPanel( entity ) ;
                    entityName = entity.getName() ;

                    if( !this.entityPanelMap.containsKey( entityName ) ) {
                        this.entityPanelMap.put( entity.getName(), panel ) ;
                        add( panel ) ;
                    }
                }
                updateLatestEntityValues() ;
                break ;

            case PRIMARY_SERIES_REMOVED:

                entityName = ( String )event.getValue() ;
                panel      = this.entityPanelMap.get( entityName ) ;
                if( panel != null ) {
                    this.entityPanelMap.remove( entityName ) ;
                    remove( panel ) ;
                    repaint( 0, 0, super.getWidth()-1, super.getHeight()-1 ) ;
                }
                break ;

            case MODEL_DATA_CHANGED:
                // Update the latest entity values.
                updateLatestEntityValues() ;
                break ;
        }
    }

    /** Updates the latest values of all the entities being supported by this chart. */
    private void updateLatestEntityValues() {

        final Map<ChartEntity, List<EODValue>> renderData = this.model.getRenderData() ;

        for( final ChartEntity entity : renderData.keySet() ) {

            final String entityName = entity.getName() ;
            final EntityPanel panel = this.entityPanelMap.get( entityName ) ;

            if( panel != null ) {
                final List<EODValue> eodValues = renderData.get( entity ) ;
                if( !eodValues.isEmpty() ) {
                    final EODValue eodVal = eodValues.get( eodValues.size()-1 ) ;
                    final SortedSet<ITDValue> itdValues = eodVal.getITDValues() ;

                    double value = 0.0 ;
                    if( itdValues.isEmpty() ) {
                        value = eodVal.getClose() ;
                    }
                    else {
                        value = itdValues.last().getValue() ;
                    }
                    panel.setValue( value ) ;
                }
            }
        }
    }
}
