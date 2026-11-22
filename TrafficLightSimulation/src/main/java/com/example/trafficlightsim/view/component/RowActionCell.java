package com.example.trafficlightsim.view.component;

import com.example.trafficlightsim.view.Theme;
import java.awt.*;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class RowActionCell extends JComponent implements TableCellRenderer {
    private static final int HEIGHT = 30;
    private static final int GAP = 8;
    private static final int MARGIN = 14;
    private static final int DELETE_WIDTH = 38;

    private final String label;
    private final int primaryWidth;
    private final TrashIcon icon = new TrashIcon(18, Theme.DANGER);
    private Color rowBackground = Color.WHITE;

    public RowActionCell(String label, int primaryWidth) {
        this.label = label;
        this.primaryWidth = primaryWidth;
        setOpaque(false);
    }

    /** Lebar yang cukup untuk kedua tombol (dipakai untuk lebar kolom). */
    public int preferredColumnWidth() { return MARGIN + primaryWidth + GAP + DELETE_WIDTH + MARGIN; }

    public Rectangle deleteRect(Rectangle cell) {
        return new Rectangle(cell.x + cell.width - MARGIN - DELETE_WIDTH, cell.y + (cell.height - HEIGHT) / 2,
                DELETE_WIDTH, HEIGHT);
    }

    public Rectangle primaryRect(Rectangle cell) {
        Rectangle d = deleteRect(cell);
        return new Rectangle(d.x - GAP - primaryWidth, d.y, primaryWidth, HEIGHT);
    }

    /** 1 = tombol utama, 2 = tombol hapus, 0 = bukan tombol. */
    public int hit(JTable table, int row, int col, Point p) {
        if (row < 0 || col < 0) return 0;
        Rectangle cell = table.getCellRect(row, col, false);
        if (primaryRect(cell).contains(p)) return 1;
        if (deleteRect(cell).contains(p)) return 2;
        return 0;
    }

    @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus,
                                                             int row, int col) {
        rowBackground = sel ? Theme.TABLE_SELECTION : row % 2 == 0 ? Color.WHITE : Theme.TABLE_STRIPE;
        return this;
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(rowBackground);
        g.fillRect(0, 0, getWidth(), getHeight());
        Rectangle cell = new Rectangle(0, 0, getWidth(), getHeight());

        Rectangle p = primaryRect(cell);
        g.setPaint(new GradientPaint(0, p.y, Theme.RED, 0, p.y + p.height, Theme.MAROON));
        g.fillRoundRect(p.x, p.y, p.width, p.height, 10, 10);
        g.setFont(Theme.BODY_BOLD);
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, p.x + (p.width - fm.stringWidth(label)) / 2, p.y + (p.height + fm.getAscent() - fm.getDescent()) / 2);

        Rectangle d = deleteRect(cell);
        g.setColor(new Color(0xFCE9E6));
        g.fillRoundRect(d.x, d.y, d.width, d.height, 10, 10);
        g.setColor(new Color(0xE9B3AC));
        g.drawRoundRect(d.x, d.y, d.width - 1, d.height - 1, 10, 10);
        icon.paintIcon(this, g, d.x + (d.width - icon.getIconWidth()) / 2, d.y + (d.height - icon.getIconHeight()) / 2);
        g.dispose();
    }
}