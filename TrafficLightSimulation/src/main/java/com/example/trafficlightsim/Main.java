package com.example.trafficlightsim;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Starts the traffic simulation window. */
public class Main {
    public static void main(String[] args) {
       SwingUtilities.invokeLater(() -> {
    DashboardFrame frame = new DashboardFrame();
    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
});
    }
}
