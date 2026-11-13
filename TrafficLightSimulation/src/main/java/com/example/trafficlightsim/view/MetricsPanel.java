package com.example.trafficlightsim.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.example.trafficlightsim.model.Jalur;
import com.example.trafficlightsim.model.SignalState;
import com.example.trafficlightsim.view.component.RoundedPanel;
import com.example.trafficlightsim.view.component.StatusLamp;

/** Kartu "Live Metrics" (lampu empat jalur) dan kartu hitung mundur "Berikutnya". */
public class MetricsPanel extends JPanel {
    private static final Jalur[] URUTAN_LAMPU = Jalur.values(); // urutan siklus lampu: Utara, Timur, Selatan, Barat
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
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(Theme.padding(12, 12, 12, 12));
        JLabel title = new JLabel("Live Metrics", SwingConstants.CENTER);
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.TEXT);
        card.add(title, BorderLayout.NORTH);

        // Lampu berjajar ke samping, nama jalur di bawah tiap lampu. Ubah URUTAN_LAMPU untuk menukar posisi.
        JPanel row = new JPanel(new GridLayout(1, 4, 6, 0));
        row.setOpaque(false);
        for (Jalur j : URUTAN_LAMPU) {
            StatusLamp lamp = new StatusLamp();
            lamp.setPreferredSize(new Dimension(30, 30));
            lamps.put(j, lamp);
            JPanel lampWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            lampWrap.setOpaque(false);
            lampWrap.add(lamp);
            JLabel name = new JLabel(j.getLabel(), SwingConstants.CENTER);
            name.setFont(Theme.BODY);
            name.setForeground(Theme.TEXT);
            JPanel cell = new JPanel(new BorderLayout(0, 4));
            cell.setOpaque(false);
            cell.add(lampWrap, BorderLayout.NORTH);
            cell.add(name, BorderLayout.CENTER);
            row.add(cell);
        }
        card.add(row, BorderLayout.CENTER);
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
