package com.example.trafficlightsim.view;

import com.example.trafficlightsim.model.ViolationRecord;
import java.awt.Frame;
import javax.swing.JDialog;

/** Jendela pratinjau Bukti Pelanggaran. */
public class ReceiptDialog extends JDialog {
    private final ReceiptPanel panel = new ReceiptPanel();

    public ReceiptDialog(Frame owner) {
        super(owner, "Bukti Pelanggaran", true);
        setContentPane(panel);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public void setRecord(ViolationRecord record) { panel.setRecord(record); }
    public ReceiptPanel getPanel() { return panel; }
}
