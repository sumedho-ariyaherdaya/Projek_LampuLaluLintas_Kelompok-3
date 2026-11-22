package com.example.trafficlightsim.view.component;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;

/** Ikon tong sampah yang digambar dengan Java2D (tidak butuh file gambar). Dipakai tombol Hapus. */
public class TrashIcon implements Icon {
    private final int size;
    private final Color color;

    public TrashIcon(int size, Color color) { this.size = size; this.color = color; }

    @Override public int getIconWidth() { return size; }
    @Override public int getIconHeight() { return size; }

    @Override public void paintIcon(Component c, Graphics g0, int x, int y) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.translate(x, y);
        double k = size / 18.0; // semua koordinat di bawah dirancang untuk kotak 18 x 18
        g.scale(k, k);
        g.setColor(color);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(6, 1, 6, 3, 1, 1);                             // gagang tutup
        g.fill(new RoundRectangle2D.Double(2, 4, 14, 2.2, 1.5, 1.5));  // tutup
        Path2D body = new Path2D.Double();                              // badan
        body.moveTo(3.6, 7.2); body.lineTo(14.4, 7.2); body.lineTo(13.4, 16.2);
        body.quadTo(13.3, 17, 12.5, 17); body.lineTo(5.5, 17); body.quadTo(4.7, 17, 4.6, 16.2); body.closePath();
        g.fill(body);
        g.setColor(new Color(255, 255, 255, 235));                      // celah vertikal
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(7, 9, 7, 14);
        g.drawLine(9, 9, 9, 14);
        g.drawLine(11, 9, 11, 14);
        g.dispose();
    }
}