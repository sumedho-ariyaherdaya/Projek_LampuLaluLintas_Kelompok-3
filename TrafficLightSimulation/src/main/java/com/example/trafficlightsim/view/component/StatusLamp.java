package com.example.trafficlightsim.view.component;

import com.example.trafficlightsim.model.SignalState;
import com.example.trafficlightsim.view.Theme;
import java.awt.*;
import javax.swing.JComponent;

/** Lingkaran kecil berwarna merah / kuning / hijau untuk Live Metrics. */
public class StatusLamp extends JComponent {
    private SignalState state = SignalState.RED;

    public StatusLamp() { setPreferredSize(new Dimension(16, 16)); }

    public void setState(SignalState state) { this.state = state; repaint(); }
    public SignalState getState() { return state; }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color c = state == SignalState.GREEN ? Theme.SIGNAL_GREEN
                : state == SignalState.YELLOW ? Theme.SIGNAL_YELLOW : Theme.SIGNAL_RED;
        int d = Math.min(getWidth(), getHeight()) - 2;
        int x = (getWidth() - d) / 2, y = (getHeight() - d) / 2;
        g.setColor(c);
        g.fillOval(x, y, d, d);
        g.setColor(c.darker());
        g.drawOval(x, y, d, d);
        g.dispose();
    }
}
