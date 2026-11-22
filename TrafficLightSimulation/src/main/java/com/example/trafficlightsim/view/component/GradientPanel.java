package com.example.trafficlightsim.view.component;

import java.awt.*;
import javax.swing.JPanel;

/** Panel dengan latar gradasi (kiri-kanan atau atas-bawah) dan garis aksen opsional di tepi bawah. */
public class GradientPanel extends JPanel {
    private final Color from;
    private final Color to;
    private final boolean horizontal;
    private Color accent; // garis 3 px di tepi bawah; null = tanpa garis

    public GradientPanel(Color from, Color to, boolean horizontal) {
        super(new BorderLayout());
        this.from = from;
        this.to = to;
        this.horizontal = horizontal;
        setBackground(from);
        setOpaque(true);
    }

    public GradientPanel withAccent(Color accent) { this.accent = accent; return this; }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setPaint(horizontal ? new GradientPaint(0, 0, from, getWidth(), 0, to)
                              : new GradientPaint(0, 0, from, 0, getHeight(), to));
        g.fillRect(0, 0, getWidth(), getHeight());
        if (accent != null) {
            g.setColor(accent);
            g.fillRect(0, getHeight() - 3, getWidth(), 3);
        }
        g.dispose();
    }
}