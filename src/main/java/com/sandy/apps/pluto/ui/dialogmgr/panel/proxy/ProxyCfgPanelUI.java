/**
 * 
 * 
 * 
 *
 * Creation Date: Aug 15, 2008
 */

package com.sandy.apps.pluto.ui.dialogmgr.panel.proxy ;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.border.BevelBorder;

import org.apache.log4j.Logger;

import com.sandy.apps.pluto.ui.UIConstant ;

/**
 * A panel which lets the user enter network configurations like proxy and
 * authentication settings.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public abstract class ProxyCfgPanelUI extends JPanel {

    /** Generated serialVersionUID. */
    private static final long serialVersionUID = -5950735891940228583L ;

    /** Log4J logger for this class. */
    public static final Logger logger = Logger.getLogger( ProxyCfgPanelUI.class ) ;

    /** Creates new form ProxyCfgPanelUI */
    public ProxyCfgPanelUI() {
        initComponents() ;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {
        this.useProxyCB     = new JCheckBox() ;
        this.proxyHostLabel = new JLabel() ;
        this.proxyPortLabel = new JLabel() ;
        this.proxyHostTF    = new JTextField() ;
        this.proxyPortTF    = new JTextField() ;
        this.useProxyAuthCB = new JCheckBox() ;
        this.userIDLabel    = new JLabel() ;
        this.userIdTF       = new JTextField() ;
        this.passwordLabel  = new JLabel() ;
        this.passwordTF     = new JPasswordField() ;

        this.proxyHostTF.setFont( UIConstant.DLG_FONT ) ;
        this.proxyPortTF.setFont( UIConstant.DLG_FONT ) ;
        this.userIdTF.setFont( UIConstant.DLG_FONT ) ;
        this.passwordTF.setFont( UIConstant.DLG_FONT ) ;

        setBorder( BorderFactory.createBevelBorder( BevelBorder.RAISED ) ) ;
        this.useProxyCB.setText( "Use proxy" ) ;
        this.useProxyCB.setFont( UIConstant.DLG_FONT ) ;
        this.useProxyCB.setBorder( UIConstant.EMPTY_BORDER ) ;
        this.useProxyCB.setMargin( UIConstant.EMPTY_INSETS ) ;

        this.proxyHostLabel.setText( "Proxy Host" ) ;
        this.proxyHostLabel.setFont( UIConstant.DLG_FONT ) ;
        this.proxyPortLabel.setText( "Proxy Port" ) ;
        this.proxyPortLabel.setFont( UIConstant.DLG_FONT ) ;

        this.useProxyAuthCB.setText( "Use proxy authentication" ) ;
        this.useProxyAuthCB.setFont( UIConstant.DLG_FONT ) ;
        this.useProxyAuthCB.setBorder( UIConstant.EMPTY_BORDER ) ;
        this.useProxyAuthCB.setMargin( UIConstant.EMPTY_INSETS ) ;

        this.userIDLabel.setText( "User ID" ) ;
        this.userIDLabel.setFont( UIConstant.DLG_FONT ) ;
        this.passwordLabel.setText( "Password" ) ;
        this.passwordLabel.setFont( UIConstant.DLG_FONT ) ;

        final GroupLayout layout = new GroupLayout( this ) ;
        setLayout( layout ) ;
        layout.setHorizontalGroup( layout
                        .createParallelGroup( GroupLayout.Alignment.LEADING )
                        .addGroup( layout.createSequentialGroup().addContainerGap().addGroup(
                                     layout.createParallelGroup( GroupLayout.Alignment.LEADING )
                                           .addComponent( this.useProxyCB )
                       .addGroup( layout.createSequentialGroup()
                                      .addGroup(
                                                layout.createParallelGroup( GroupLayout.Alignment.TRAILING, false )
                                                      .addComponent(
                                                                this.proxyHostLabel,
                                                                GroupLayout.Alignment.LEADING,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                Short.MAX_VALUE )
                                                      .addComponent(
                                                                this.proxyPortLabel,
                                                                GroupLayout.Alignment.LEADING,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                69,
                                                                Short.MAX_VALUE ) )
                                        .addPreferredGap( LayoutStyle.ComponentPlacement.RELATED )
                                        .addGroup(
                                                layout.createParallelGroup(
                                                                GroupLayout.Alignment.LEADING,
                                                                false )
                                                      .addGroup( layout.createSequentialGroup()
                                                                      .addGap( 8, 8, 8 )
                                                                      .addComponent(
                                                                                this.proxyHostTF,
                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                226,
                                                                                Short.MAX_VALUE ) )
                                                        .addGroup( layout.createSequentialGroup()
                                                                      .addGap( 8, 8, 8 )
                                                                      .addGroup(
                                                                                layout.createParallelGroup( GroupLayout.Alignment.LEADING )
                                                                                      .addComponent(
                                                                                                this.userIdTF,
                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                226,
                                                                                                Short.MAX_VALUE )
                                                                                      .addComponent( this.proxyPortTF )
                                                                                      .addComponent(
                                                                                                this.passwordTF,
                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                226,
                                                                                                Short.MAX_VALUE ) ) ) ) )
                        .addComponent( this.useProxyAuthCB )
                        .addComponent(
                                this.userIDLabel,
                                GroupLayout.PREFERRED_SIZE,
                                63,
                                GroupLayout.PREFERRED_SIZE )
                        .addComponent(
                                this.passwordLabel ) )
                         .addContainerGap(
                                GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE ) ) ) ;

        layout.setVerticalGroup( layout.createParallelGroup(
                GroupLayout.Alignment.LEADING ).addGroup(
                layout.createSequentialGroup().addContainerGap().addComponent(
                        this.useProxyCB ).addGap( 14, 14, 14 ).addGroup(
                        layout.createParallelGroup(
                                GroupLayout.Alignment.BASELINE ).addComponent(
                                this.proxyHostLabel ).addComponent(
                                this.proxyHostTF, GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE ) ).addPreferredGap(
                        LayoutStyle.ComponentPlacement.RELATED ).addGroup(
                        layout.createParallelGroup(
                                GroupLayout.Alignment.BASELINE ).addComponent(
                                this.proxyPortLabel ).addComponent(
                                this.proxyPortTF, GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE ) ).addGap( 19, 19,
                        19 ).addComponent( this.useProxyAuthCB ).addGap( 14,
                        14, 14 ).addGroup(
                        layout.createParallelGroup(
                                GroupLayout.Alignment.BASELINE ).addComponent(
                                this.userIDLabel ).addComponent( this.userIdTF,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE ) ).addPreferredGap(
                        LayoutStyle.ComponentPlacement.RELATED ).addGroup(
                        layout.createParallelGroup(
                                GroupLayout.Alignment.BASELINE ).addComponent(
                                this.passwordLabel ).addComponent(
                                this.passwordTF, GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE ) ).addContainerGap(
                        16, Short.MAX_VALUE ) ) ) ;
    }

    // Variables declaration - do not modify
    private JLabel passwordLabel ;
    private JLabel proxyHostLabel ;
    private JLabel proxyPortLabel ;
    private JLabel userIDLabel ;

    protected JPasswordField passwordTF ;
    protected JTextField proxyHostTF ;
    protected JTextField proxyPortTF ;
    protected JCheckBox useProxyAuthCB ;
    protected JCheckBox useProxyCB ;
    protected JTextField userIdTF ;
    // End of variables declaration
}
