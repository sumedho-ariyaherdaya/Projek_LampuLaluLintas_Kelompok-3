package com.example.trafficlightsim.view;

import com.example.trafficlightsim.SimulationPanel;
import com.example.trafficlightsim.view.component.HeaderBar;
import com.example.trafficlightsim.view.component.RoundedPanel;
import java.awt.*;
import javax.swing.*;

/**
 * Layar utama: simulasi di kiri; di kanan Live Metrics + hitung mundur, Total Pelanggaran,
 * dan area form Input Data; tombol di bawah.
 */
public class DashboardPanel extends JPanel {
    private static final String AREA_IDLE = "idle";
    private static final String AREA_FORM = "form";

    private final MetricsPanel metrics = new MetricsPanel();
    private final TotalViolationCard totalCard = new TotalViolationCard();
    private final VehicleStatusPanel statusPanel = new VehicleStatusPanel(); // belum ditampilkan di tata letak baru
    private final ControlBar controlBar = new ControlBar();
    private final InputDataPanel inputForm = new InputDataPanel();
    private final CardLayout inputCards = new CardLayout();
    private final JPanel inputArea = new JPanel(inputCards);

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
        // Lebar panel kanan = 40% lebar jendela (minimal 440 px).
        RoundedPanel side = new RoundedPanel(16, Theme.CARD, Theme.BORDER) {
            @Override public Dimension getPreferredSize() {
                Container p = getParent();
                int w = p == null ? 480 : (int) (p.getWidth() * 0.40);
                return new Dimension(Math.max(440, w), super.getPreferredSize().height);
            }
        };
        side.setLayout(new GridBagLayout());
        side.setBorder(Theme.padding(16));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 10, 0);
        c.gridy = 0; side.add(metrics, c);
        c.gridy = 1; side.add(totalCard, c);
        c.gridy = 2; c.weighty = 1; c.fill = GridBagConstraints.BOTH; c.insets = new Insets(0, 0, 0, 0);
        side.add(buildInputArea(), c);
        return side;
    }

    /** Area form: berisi petunjuk saat kosong, dan InputDataPanel saat tombol "Input data" ditekan. */
    private JComponent buildInputArea() {
        inputArea.setOpaque(false);

        RoundedPanel idle = new RoundedPanel(16, Theme.BACKGROUND, Theme.BORDER);
        idle.setLayout(new GridBagLayout());
        JLabel hint = new JLabel("Klik \"Input data\" untuk mencatat pelanggaran");
        hint.setFont(Theme.BODY);
        hint.setForeground(Theme.TEXT_MUTED);
        idle.add(hint);

        JScrollPane scroll = new JScrollPane(inputForm, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        RoundedPanel formCard = new RoundedPanel(16, Theme.BACKGROUND, Theme.BORDER);
        formCard.setLayout(new BorderLayout());
        formCard.setBorder(Theme.padding(10, 8, 10, 8));
        formCard.add(scroll, BorderLayout.CENTER);

        inputArea.add(idle, AREA_IDLE);
        inputArea.add(formCard, AREA_FORM);
        inputCards.show(inputArea, AREA_IDLE);
        return inputArea;
    }

    /** Memunculkan form Input Data di area kanan. */
    public void showInputForm() { inputCards.show(inputArea, AREA_FORM); }

    /** Menyembunyikan form dan kembali ke petunjuk. */
    public void hideInputForm() { inputCards.show(inputArea, AREA_IDLE); }

    public MetricsPanel getMetrics() { return metrics; }
    public TotalViolationCard getTotalCard() { return totalCard; }
    public VehicleStatusPanel getStatusPanel() { return statusPanel; }
    public ControlBar getControlBar() { return controlBar; }
    public InputDataPanel getInputForm() { return inputForm; }
}
