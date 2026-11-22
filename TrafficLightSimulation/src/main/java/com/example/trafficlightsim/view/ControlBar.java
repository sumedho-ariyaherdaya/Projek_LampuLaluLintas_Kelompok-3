package com.example.trafficlightsim.view;

import com.example.trafficlightsim.view.component.RoundedButton;
import java.awt.*;
import javax.swing.*;

/** Baris tombol di bawah simulasi. Tombol dibuka lewat getter agar controller bisa memasang aksi. */
public class ControlBar extends JPanel {
    private final RoundedButton listButton = RoundedButton.outline("Lihat daftar pelanggaran");
    private final RoundedButton databaseButton = RoundedButton.outline("Database");
    private final RoundedButton inputButton = RoundedButton.gold("Input data");
    private final RoundedButton stopButton = RoundedButton.filled("Berhenti", Theme.DANGER);
    private final RoundedButton startButton = RoundedButton.filled("Mulai", Theme.SUCCESS);

    public ControlBar() {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(Theme.padding(0, 32, 28, 32));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(inputButton);
        right.add(stopButton);
        right.add(startButton);
        // Tombol "Database" tepat di kanan "Lihat daftar pelanggaran".
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        left.add(listButton);
        left.add(databaseButton);
        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);
    }

    public JButton getListButton() { return listButton; }
    public JButton getDatabaseButton() { return databaseButton; }
    public JButton getInputButton() { return inputButton; }
    public JButton getStopButton() { return stopButton; }
    public JButton getStartButton() { return startButton; }
}