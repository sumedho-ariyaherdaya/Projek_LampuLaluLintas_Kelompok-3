package com.example.trafficlightsim.view;

import com.example.trafficlightsim.model.Jalur;
import com.example.trafficlightsim.model.SignalState;
import com.example.trafficlightsim.view.component.RoundedPanel;
import com.example.trafficlightsim.view.component.StatusLamp;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.*;

/** Kartu "Live Metrics" (lampu empat jalur) dan kartu hitung mundur "Berikutnya". */
public class MetricsPanel extends JPanel {
    private final Map<Jalur, StatusLamp> lamps = new EnumMap<>(Jalur.class);
    private final JLabel countdown = new JLabel("12", SwingConstants.CENTER);

    public MetricsPanel() {
        super(new BorderLayout(16, 0));
        setOpaque(false);
        add(buildLampCard(), BorderLayout.CENTER);
        add(buildCountdownCard(), BorderLayout.EAST);
        // Nilai awal contoh dari mockup; akan ditimpa SimulationController.
        for (Jalur j : Jalur.values()) setSignal(j, j == Jalur.UTARA ? SignalState.GREEN : SignalState.RED);
    }

    private JComponent buildLampCard() {
        RoundedPanel card = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(Theme.padding(16));
        JLabel title = new JLabel("Live Metrics");
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(10));
        for (Jalur j : Jalur.values()) {
            StatusLamp lamp = new StatusLamp();
            lamps.put(j, lamp);
            JLabel name = new JLabel(j.getLabel());
            name.setFont(Theme.BODY);
            name.setForeground(Theme.TEXT);
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
            row.add(lamp, BorderLayout.WEST);
            row.add(name, BorderLayout.CENTER);
            card.add(row);
            card.add(Box.createVerticalStrut(6));
        }
        return card;
    }

    private JComponent buildCountdownCard() {
        RoundedPanel card = new RoundedPanel(16, Theme.TEAL, null);
        card.setLayout(new BorderLayout());
        card.setBorder(Theme.padding(16, 8, 16, 8));
        card.setPreferredSize(new Dimension(112, 10));
        JLabel title = new JLabel("Berikutnya", SwingConstants.CENTER);
        title.setFont(Theme.LABEL);
        title.setForeground(Color.WHITE);
        countdown.setFont(Theme.BIG_NUMBER);
        countdown.setForeground(Color.WHITE);
        card.add(title, BorderLayout.NORTH);
        card.add(countdown, BorderLayout.CENTER);
        return card;
    }

    public void setSignal(Jalur jalur, SignalState state) { lamps.get(jalur).setState(state); }
    public void setCountdown(int seconds) { countdown.setText(String.valueOf(seconds)); }
}
