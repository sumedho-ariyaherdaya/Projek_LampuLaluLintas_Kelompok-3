package com.example.trafficlightsim.view;

import com.example.trafficlightsim.view.component.RoundedPanel;
import java.awt.*;
import javax.swing.*;

/** Kartu "Total Pelanggaran: N". */
public class TotalViolationCard extends RoundedPanel {
    private final JLabel value = new JLabel("6");

    public TotalViolationCard() {
        super(16, Theme.CARD, Theme.BORDER);
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        setBorder(Theme.padding(12, 8, 12, 8));
        JLabel label = new JLabel("Total Pelanggaran:");
        label.setFont(Theme.BODY_BOLD);
        label.setForeground(Theme.TEXT);
        value.setFont(Theme.font(Font.BOLD, 22));
        value.setForeground(Theme.ALERT_TEXT);
        add(label);
        add(value);
    }

    public void setTotal(int total) { value.setText(String.valueOf(total)); }
}
