package com.example.trafficlightsim.view.component;

import com.example.trafficlightsim.view.Theme;
import java.awt.*;
import javax.swing.JButton;

/** Tombol sudut membulat dengan efek hover dan tekan. */
public class RoundedButton extends JButton {
    private final Color base;
    private final Color outline;

    public RoundedButton(String text, Color background, Color foreground, Color outline) {
        super(text);
        this.base = background;
        this.outline = outline;
        setForeground(foreground);
        setFont(Theme.BODY_BOLD);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(Theme.padding(0, 20, 0, 20));
    }

    /** Tombol berisi warna penuh dengan teks putih (Mulai, Kirim, dst). */
    public static RoundedButton filled(String text, Color background) {
        return new RoundedButton(text, background, Color.WHITE, null);
    }

    /** Tombol putih bergaris navy (aksi sekunder). */
    public static RoundedButton outline(String text) {
        return new RoundedButton(text, Color.WHITE, Theme.NAVY, Theme.NAVY);
    }

    /** Tombol navy bergaris putih, untuk di atas header gelap. */
    public static RoundedButton onDark(String text) {
        return new RoundedButton(text, Theme.NAVY, Color.WHITE, Color.WHITE);
    }

    @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(44, d.height));
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill = base;
        if (getModel().isPressed()) fill = fill.darker().darker();
        else if (getModel().isRollover()) fill = fill.darker();
        g.setColor(fill);
        g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        if (outline != null) {
            g.setColor(outline);
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
        }
        g.dispose();
        super.paintComponent(g0); // menggambar teks
    }
}
