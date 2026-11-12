package com.example.trafficlightsim.view;

import com.example.trafficlightsim.view.component.RoundedPanel;
import java.awt.*;
import javax.swing.*;

/** Kartu "Status Kendaraan" (plat dan jenis kendaraan yang sedang dipilih di simulasi). */
public class VehicleStatusPanel extends RoundedPanel {
    private final JLabel plate = valueLabel();
    private final JLabel kind = valueLabel();

    public VehicleStatusPanel() {
        super(16, Theme.CARD, Theme.BORDER);
        setLayout(new GridBagLayout());
        setBorder(Theme.padding(16));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.gridwidth = 3; c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(0, 0, 10, 0);
        JLabel title = new JLabel("Status Kendaraan");
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.TEXT);
        add(title, c);
        addRow(1, "Plat", plate);
        addRow(2, "Jenis", kind);
        GridBagConstraints fill = new GridBagConstraints();
        fill.gridx = 0; fill.gridy = 3; fill.weighty = 1; // dorong isi ke atas
        add(Box.createGlue(), fill);
    }

    private void addRow(int row, String name, JLabel value) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(4, 0, 4, 8);
        JLabel label = new JLabel(name);
        label.setFont(Theme.BODY);
        label.setForeground(Theme.TEXT_MUTED);
        c.gridx = 0; add(label, c);
        JLabel colon = new JLabel(":");
        colon.setFont(Theme.BODY);
        colon.setForeground(Theme.TEXT_MUTED);
        c.gridx = 1; add(colon, c);
        c.gridx = 2; c.weightx = 1; add(value, c);
    }

    private static JLabel valueLabel() {
        JLabel l = new JLabel("-");
        l.setFont(Theme.BODY_BOLD);
        l.setForeground(Theme.TEXT);
        return l;
    }

    public void setVehicle(String plateText, String kindText) { plate.setText(plateText); kind.setText(kindText); }
    public void clear() { setVehicle("-", "-"); }
}
