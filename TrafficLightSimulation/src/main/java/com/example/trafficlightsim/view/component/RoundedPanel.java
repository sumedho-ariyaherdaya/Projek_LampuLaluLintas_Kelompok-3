package com.example.trafficlightsim.view.component;

import java.awt.*;
import javax.swing.JPanel;

/** Panel kartu dengan sudut membulat dan garis tepi tipis. */
public class RoundedPanel extends JPanel {
    private final int arc;
    private final Color borderColor;

    public RoundedPanel(int arc, Color background, Color borderColor) {
        this.arc = arc;
        this.borderColor = borderColor;
        setBackground(background);
        setOpaque(false);
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(getBackground());
        g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        if (borderColor != null) {
            g.setColor(borderColor);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        }
        g.dispose();
        super.paintComponent(g0);
    }
}
