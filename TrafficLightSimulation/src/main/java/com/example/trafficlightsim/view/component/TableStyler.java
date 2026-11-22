package com.example.trafficlightsim.view.component;

import com.example.trafficlightsim.model.StatusPelanggaran;
import com.example.trafficlightsim.model.WarnaKendaraan;
import com.example.trafficlightsim.view.Theme;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Gaya tabel seragam (baris belang, header bergradasi emas, lencana status, titik warna kendaraan)
 * yang dipakai semua tabel di aplikasi.
 */
public final class TableStyler {
    private TableStyler() { }

    /** Memasang tampilan standar pada tabel; kolom boldColumn (mis. plat/ID) dicetak tebal, -1 = tidak ada. */
    public static void apply(JTable table, int boldColumn) {
        table.setRowHeight(44);
        table.setFont(Theme.BODY);
        table.setForeground(Theme.TEXT);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Theme.TABLE_LINE);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(Theme.TABLE_SELECTION);
        table.setSelectionForeground(Theme.TEXT);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setDefaultRenderer(Object.class, new CellRenderer(boldColumn));
        JTableHeader th = table.getTableHeader();
        th.setDefaultRenderer(new HeaderRenderer(-1));
        th.setReorderingAllowed(false);
        th.setPreferredSize(new Dimension(10, 44));
    }

    /** Header dengan kolom aksi rata kanan (dipanggil setelah apply). */
    public static void alignHeaderRight(JTable table, int actionColumn) {
        table.getTableHeader().setDefaultRenderer(new HeaderRenderer(actionColumn));
    }

    /** Membungkus tabel dalam kartu bulat dengan scroll. */
    public static JComponent card(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setColumnHeaderView(table.getTableHeader());
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        RoundedPanel card = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(Theme.padding(1));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    /** Baris belang; nilai StatusPelanggaran digambar sebagai lencana, WarnaKendaraan sebagai titik warna + nama. */
    private static class CellRenderer extends DefaultTableCellRenderer {
        private final int boldColumn;
        private StatusPelanggaran badge;

        CellRenderer(int boldColumn) { this.boldColumn = boldColumn; }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                                 boolean focus, int row, int col) {
            badge = v instanceof StatusPelanggaran ? (StatusPelanggaran) v : null;
            super.getTableCellRendererComponent(t, badge != null ? "" : v, sel, false, row, col);
            setIcon(v instanceof WarnaKendaraan ? dot(((WarnaKendaraan) v).getColor()) : null);
            if (v instanceof WarnaKendaraan) setText(((WarnaKendaraan) v).getLabel());
            setIconTextGap(8);
            setToolTipText(badge == null && v != null && !(v instanceof WarnaKendaraan) ? v.toString() : null); // teks panjang terlihat penuh saat disorot
            setBorder(Theme.padding(0, 16, 0, 16));
            setFont(col == boldColumn ? Theme.BODY_BOLD : Theme.BODY);
            setForeground(Theme.TEXT);
            setBackground(sel ? Theme.TABLE_SELECTION : row % 2 == 0 ? Color.WHITE : Theme.TABLE_STRIPE);
            return this;
        }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            if (badge == null) return;
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(Theme.font(Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            String text = badge.getLabel();
            int w = fm.stringWidth(text) + 22, h = 24, x = 16, y = (getHeight() - h) / 2;
            g.setColor(Theme.statusBackground(badge));
            g.fillRoundRect(x, y, w, h, h, h);
            g.setColor(Theme.statusForeground(badge));
            g.drawString(text, x + 11, y + (h + fm.getAscent() - fm.getDescent()) / 2);
            g.dispose();
        }

        private static Icon dot(Color color) {
            return new Icon() {
                @Override public void paintIcon(Component c, Graphics g0, int x, int y) {
                    Graphics2D g = (Graphics2D) g0.create();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(color);
                    g.fillOval(x, y, 14, 14);
                    g.setColor(Theme.mix(color, Color.BLACK, 0.35f));
                    g.drawOval(x, y, 13, 13);
                    g.dispose();
                }
                @Override public int getIconWidth() { return 14; }
                @Override public int getIconHeight() { return 14; }
            };
        }
    }

    /** Header bergradasi emas muda; kolom aksi (jika ada) rata kanan agar sejajar dengan tombol. */
    private static class HeaderRenderer implements TableCellRenderer {
        private final int rightColumn;
        private final JLabel label = new JLabel();
        private final GradientPanel panel = new GradientPanel(Theme.GOLD_SOFT, Theme.TABLE_HEADER, false);

        HeaderRenderer(int rightColumn) {
            this.rightColumn = rightColumn;
            panel.setLayout(new BorderLayout());
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.GOLD), Theme.padding(0, 16, 0, 16)));
            label.setFont(Theme.BODY_BOLD);
            label.setForeground(Theme.TEXT);
            panel.add(label, BorderLayout.CENTER);
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                                 boolean focus, int row, int col) {
            label.setText(v != null ? v.toString() : "");
            label.setHorizontalAlignment(col == rightColumn ? SwingConstants.RIGHT : SwingConstants.LEFT);
            return panel;
        }
    }
}