package com.example.trafficlightsim.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

/** Warna, font, dan helper gaya yang dipakai semua tampilan. */
public final class Theme {
    private Theme() { }

    // --- Palet referensi (marmer merah-emas) ---
    public static final Color MAROON_DEEP = new Color(0x350200);
    public static final Color MAROON = new Color(0x6B0200);
    public static final Color RED = new Color(0x900500);
    public static final Color GOLD = new Color(0xBD953E);
    public static final Color GOLD_LIGHT = new Color(0xD4B356);
    public static final Color GOLD_SOFT = new Color(0xF5EBD0); // emas sangat muda, untuk latar lembut dan ujung gradasi

    // Nama konstanta lama dipertahankan (NAVY, TEAL, ...) agar file lain tidak perlu diubah; hanya nilainya yang berganti.
    public static final Color NAVY = MAROON_DEEP;              // header, teks utama, tombol garis
    public static final Color BACKGROUND = new Color(0xF8F1E1);
    public static final Color CARD = new Color(0xFFFCF6);
    public static final Color BORDER = new Color(0xE3D5B5);
    public static final Color INPUT_BORDER = new Color(0xCDB98A);
    public static final Color TEXT = NAVY;
    public static final Color TEXT_MUTED = new Color(0x7A5A52);
    public static final Color TEAL = RED;                      // warna aksi utama (Kirim, Cari, Download, ...)
    public static final Color SUCCESS = new Color(0x15803D);
    public static final Color DANGER = new Color(0xC0392B);
    public static final Color WARNING = GOLD;
    public static final Color ALERT_TEXT = new Color(0xA11B10);
    public static final Color TABLE_HEADER = new Color(0xEFE3C4);
    public static final Color TABLE_STRIPE = new Color(0xFCF8EE);
    public static final Color TABLE_LINE = new Color(0xEADFC6);
    public static final Color TABLE_SELECTION = new Color(0xF3E2B3);

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

    /** Warna campuran: amount 0 = a, 1 = b. */
    public static Color mix(Color a, Color b, float amount) {
        float t = Math.max(0f, Math.min(1f, amount));
        return new Color(Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                         Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                         Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    /** Gradasi atas-bawah setinggi h piksel. */
    public static GradientPaint vertical(int h, Color top, Color bottom) {
        return new GradientPaint(0, 0, top, 0, Math.max(1, h), bottom);
    }

    /** Warna teks dan latar lencana untuk status log pelanggaran. */
    public static Color statusForeground(com.example.trafficlightsim.model.StatusPelanggaran s) {
        switch (s) {
            case TERKIRIM: return new Color(0x8A5A00);
            case DITERIMA: return new Color(0x1F5F8B);
            case DISANGGAH: return new Color(0xA11B10);
            case SELESAI: return new Color(0x146C34);
            default: return new Color(0x6B5B57);
        }
    }

    public static Color statusBackground(com.example.trafficlightsim.model.StatusPelanggaran s) {
        switch (s) {
            case TERKIRIM: return new Color(0xF8E7B5);
            case DITERIMA: return new Color(0xD9EAF6);
            case DISANGGAH: return new Color(0xF9D9D4);
            case SELESAI: return new Color(0xD9F0E0);
            default: return new Color(0xEDE4D0);
        }
    }
}