package com.example.trafficlightsim.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

/** Warna, font, dan helper gaya yang dipakai semua tampilan. */
public final class Theme {
    private Theme() { }

    public static final Color NAVY = new Color(0x14243D);
    public static final Color BACKGROUND = new Color(0xF2F5F9);
    public static final Color CARD = Color.WHITE;
    public static final Color BORDER = new Color(0xDDE3EC);
    public static final Color INPUT_BORDER = new Color(0xC5CEDB);
    public static final Color TEXT = NAVY;
    public static final Color TEXT_MUTED = new Color(0x4A5A72);
    public static final Color TEAL = new Color(0x0E7C86);
    public static final Color SUCCESS = new Color(0x15803D);
    public static final Color DANGER = new Color(0xC0392B);
    public static final Color WARNING = new Color(0xB45309);
    public static final Color ALERT_TEXT = new Color(0xB42318);
    public static final Color TABLE_HEADER = new Color(0xE8EEF5);
    public static final Color TABLE_STRIPE = new Color(0xF8FAFC);
    public static final Color TABLE_LINE = new Color(0xE6EBF2);
    public static final Color TABLE_SELECTION = new Color(0xD8EEF0);

    /** Warna lampu: sama dengan panah sinyal yang digambar SimulationPanel.drawSignal. */
    public static final Color SIGNAL_GREEN = new Color(58, 230, 115);
    public static final Color SIGNAL_YELLOW = new Color(255, 202, 54);
    public static final Color SIGNAL_RED = new Color(244, 72, 72);

    public static final String FAMILY = "Segoe UI"; // font bawaan Windows; Java otomatis cadangan jika tidak ada

    public static Font font(int style, int size) { return new Font(FAMILY, style, size); }

    public static final Font TITLE = font(Font.BOLD, 24);
    public static final Font HEADING = font(Font.BOLD, 16);
    public static final Font BODY = font(Font.PLAIN, 14);
    public static final Font BODY_BOLD = font(Font.BOLD, 14);
    public static final Font LABEL = font(Font.BOLD, 13);
    public static final Font BIG_NUMBER = font(Font.BOLD, 52);

    public static Border padding(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    public static Border padding(int all) { return padding(all, all, all, all); }

    /** Gaya kolom isian / dropdown: putih, garis tipis, tinggi 44. */
    public static void styleInput(JComponent c) {
        c.setFont(BODY);
        c.setForeground(TEXT);
        c.setBackground(Color.WHITE);
        Border line = BorderFactory.createLineBorder(INPUT_BORDER);
        // Dropdown sudah punya jarak dalam sendiri; cukup garis tepi agar tidak tampak dobel.
        c.setBorder(c instanceof JComboBox ? line
                : BorderFactory.createCompoundBorder(line, padding(0, 12, 0, 12)));
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, 44));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    /** Ikon checkbox 20px: kotak putih, atau kotak teal dengan centang. */
    public static Icon checkIcon(boolean selected) {
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g0, int x, int y) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(selected ? TEAL : Color.WHITE);
                g.fillRoundRect(x, y, 19, 19, 6, 6);
                g.setColor(selected ? TEAL : INPUT_BORDER);
                g.drawRoundRect(x, y, 19, 19, 6, 6);
                if (selected) {
                    g.setColor(Color.WHITE);
                    g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.drawPolyline(new int[]{x + 5, x + 8, x + 14}, new int[]{y + 10, y + 13, y + 6}, 3);
                }
                g.dispose();
            }
            @Override public int getIconWidth() { return 20; }
            @Override public int getIconHeight() { return 20; }
        };
    }
}
