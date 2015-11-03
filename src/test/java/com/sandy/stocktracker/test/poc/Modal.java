
package com.sandy.stocktracker.test.poc;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.MouseInputAdapter;

public class Modal  {

    static class ModalAdapter
        extends InternalFrameAdapter {
      Component glass;

      public ModalAdapter(final Component glass) {
        this.glass = glass;

        // Associate dummy mouse listeners
        // Otherwise mouse events pass through
        final MouseInputAdapter adapter =
          new MouseInputAdapter(){};
        glass.addMouseListener(adapter);
        glass.addMouseMotionListener(adapter);
      }

      public void internalFrameClosed(
          final InternalFrameEvent e) {
        this.glass.setVisible(false);
      }
    }

    public static void main(final String args[]) {
      final JFrame frame = new JFrame(
        "Modal Internal Frame");
      frame.setDefaultCloseOperation(
        JFrame.EXIT_ON_CLOSE);

      final JDesktopPane desktop = new JDesktopPane();

      final ActionListener showModal =
          new ActionListener() {
        public void actionPerformed(final ActionEvent e) {

          // Manually construct a message frame popup
          final JOptionPane optionPane = new JOptionPane();
          optionPane.setMessage("Hello, World");
          optionPane.setMessageType(
            JOptionPane.INFORMATION_MESSAGE);
          final JInternalFrame modal = optionPane.
            createInternalFrame(desktop, "Modal");

          // create opaque glass pane
          final JPanel glass = new JPanel();
          glass.setOpaque(false);

          // Attach modal behavior to frame
          modal.addInternalFrameListener(
            new ModalAdapter(glass));

          // Add modal internal frame to glass pane
          glass.add(modal);

          // Change glass pane to our panel
          frame.setGlassPane(glass);

          // Show glass pane, then modal dialog
          glass.setVisible(true);
          modal.setVisible(true);

          System.out.println("Returns immediately");
        }
      };

      final JInternalFrame internal =
        new JInternalFrame("Opener");
      desktop.add(internal);

      final JButton button = new JButton("Open");
      button.addActionListener(showModal);

      final Container iContent = internal.getContentPane();
      iContent.add(button, BorderLayout.CENTER);
      internal.setBounds(25, 25, 200, 100);
      internal.setVisible(true);

      final Container content = frame.getContentPane();
      content.add(desktop, BorderLayout.CENTER);
      frame.setSize(500, 300);
      frame.setVisible(true);
    }
  }