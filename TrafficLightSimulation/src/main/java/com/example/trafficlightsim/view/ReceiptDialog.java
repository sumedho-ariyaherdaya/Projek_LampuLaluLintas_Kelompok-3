package com.example.trafficlightsim.view;

import com.example.trafficlightsim.model.ViolationRecord;
import com.example.trafficlightsim.view.component.RoundedButton;
import java.awt.*;
import javax.swing.*;

/** Halaman Log Pelanggaran: lembar Bukti Pelanggaran dengan tombol Kembali dan Download. */
public class ReceiptDialog extends JDialog {
    private final ReceiptPanel panel = new ReceiptPanel();
    private final RoundedButton backButton = RoundedButton.outline("Kembali");
    private final RoundedButton downloadButton = RoundedButton.filled("Download", Theme.TEAL);

    public ReceiptDialog(Frame owner) {
        super(owner, "Bukti Pelanggaran", true);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setBackground(Theme.BACKGROUND);
        buttons.setBorder(Theme.padding(14, 20, 14, 20));
        buttons.add(backButton);
        buttons.add(downloadButton);

        JPanel content = new JPanel(new BorderLayout());
        content.add(panel, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        setContentPane(content);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public void setRecord(ViolationRecord record) { panel.setRecord(record); }
    public ReceiptPanel getPanel() { return panel; }
    public JButton getBackButton() { return backButton; }
    public JButton getDownloadButton() { return downloadButton; }
}
