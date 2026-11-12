package com.example.trafficlightsim;

import javax.swing.JFrame;
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
