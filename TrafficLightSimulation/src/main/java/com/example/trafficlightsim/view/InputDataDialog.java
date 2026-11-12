package com.example.trafficlightsim.view;

import java.awt.Frame;
import javax.swing.JDialog;

/** Jendela modal pembungkus InputDataPanel (dibuka dari tombol "Input data"). */
public class InputDataDialog extends JDialog {
    private final InputDataPanel panel = new InputDataPanel();

    public InputDataDialog(Frame owner) {
        super(owner, "Input Data Pelanggaran", true);
        setContentPane(panel);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public InputDataPanel getPanel() { return panel; }
}
