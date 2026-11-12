package com.example.trafficlightsim.view;

import com.example.trafficlightsim.SimulationPanel;
import com.example.trafficlightsim.view.component.HeaderBar;
import com.example.trafficlightsim.view.component.RoundedPanel;
import java.awt.*;
import javax.swing.*;

/** Layar utama: simulasi persimpangan di kiri, metrik dan status di kanan, tombol di bawah. */
public class DashboardPanel extends JPanel {
    private final MetricsPanel metrics = new MetricsPanel();
    private final TotalViolationCard totalCard = new TotalViolationCard();
    private final VehicleStatusPanel statusPanel = new VehicleStatusPanel();
    private final ControlBar controlBar = new ControlBar();

    /** @param simulation SimulationPanel yang sudah ada; tidak diubah, hanya ditempatkan di kartu. */
    public DashboardPanel(SimulationPanel simulation) {
        super(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        add(new HeaderBar("Simulasi Lalu Lintas 4 Jalur"), BorderLayout.NORTH);

        RoundedPanel simCard = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        simCard.setLayout(new BorderLayout());
        simCard.setBorder(Theme.padding(12));
        simulation.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        simCard.add(simulation, BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(24, 0));
        body.setOpaque(false);
        body.setBorder(Theme.padding(24, 32, 16, 32));
        body.add(simCard, BorderLayout.CENTER);
        body.add(buildSidePanel(), BorderLayout.EAST);

        add(body, BorderLayout.CENTER);
        add(controlBar, BorderLayout.SOUTH);
    }

    private JComponent buildSidePanel() {
        JPanel side = new JPanel(new GridBagLayout());
        side.setOpaque(false);
        side.setPreferredSize(new Dimension(340, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 16, 0);
        c.gridy = 0; side.add(metrics, c);
        c.gridy = 1; side.add(totalCard, c);
        c.gridy = 2; c.weighty = 1; c.fill = GridBagConstraints.BOTH; c.insets = new Insets(0, 0, 0, 0);
        side.add(statusPanel, c);
        return side;
    }

    public MetricsPanel getMetrics() { return metrics; }
    public TotalViolationCard getTotalCard() { return totalCard; }
    public VehicleStatusPanel getStatusPanel() { return statusPanel; }
    public ControlBar getControlBar() { return controlBar; }
}
