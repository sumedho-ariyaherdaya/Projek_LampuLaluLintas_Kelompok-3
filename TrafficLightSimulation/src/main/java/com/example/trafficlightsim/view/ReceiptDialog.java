package com.example.trafficlightsim.view;

import com.example.trafficlightsim.model.RiwayatLog;
import com.example.trafficlightsim.model.ViolationRecord;
import com.example.trafficlightsim.view.component.RoundedButton;
import com.example.trafficlightsim.view.component.RoundedPanel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;

/**
 * Halaman Log Pelanggaran: lembar Bukti Pelanggaran (kiri), riwayat pengiriman dan tanggapan (kanan),
 * dengan tombol Kembali, Kirim Email, dan Download.
 */
public class ReceiptDialog extends JDialog {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ReceiptPanel panel = new ReceiptPanel();
    private final RoundedButton backButton = RoundedButton.outline("Kembali");
    private final RoundedButton sendButton = RoundedButton.gold("Kirim Email");
    private final RoundedButton downloadButton = RoundedButton.filled("Download", Theme.TEAL);
    private final JPanel historyList = new JPanel();

    public ReceiptDialog(Frame owner) {
        super(owner, "Bukti Pelanggaran", true);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setBackground(Theme.BACKGROUND);
        buttons.setBorder(Theme.padding(14, 20, 14, 20));
        buttons.add(backButton);
        buttons.add(sendButton);
        buttons.add(downloadButton);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Theme.BACKGROUND);
        content.add(panel, BorderLayout.WEST);
        content.add(buildHistoryCard(), BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        setContentPane(content);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private JComponent buildHistoryCard() {
        JLabel title = new JLabel("Riwayat Pengiriman & Tanggapan");
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.TEXT);
        historyList.setLayout(new BoxLayout(historyList, BoxLayout.Y_AXIS));
        historyList.setOpaque(false);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(historyList, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(wrap, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        RoundedPanel card = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(Theme.padding(16, 16, 16, 16));
        card.add(title, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setBorder(Theme.padding(12, 12, 0, 12));
        outer.setPreferredSize(new Dimension(360, 10));
        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    public void setRecord(ViolationRecord record) { panel.setRecord(record); }

    /** Mengisi daftar riwayat (urut dari yang paling awal). */
    public void setHistory(List<RiwayatLog> history) {
        historyList.removeAll();
        if (history.isEmpty()) {
            JLabel none = new JLabel("Belum ada riwayat.");
            none.setFont(Theme.BODY);
            none.setForeground(Theme.TEXT_MUTED);
            historyList.add(none);
        }
        for (RiwayatLog h : history) historyList.add(entry(h));
        historyList.revalidate();
        historyList.repaint();
    }

    private JComponent entry(RiwayatLog h) {
        StringBuilder html = new StringBuilder("<html><div style='width:250px'>");
        html.append("<b>").append(esc(h.getAksi().getLabel())).append("</b><br>");
        html.append("<span style='color:#7A5A52'>").append(TIME.format(h.getTime())).append(" \u2022 ")
            .append(esc(h.getAktor())).append("</span>");
        if (!h.getCatatan().isEmpty()) html.append("<br>").append(esc(h.getCatatan()));
        html.append("<br><span style='color:#7A5A52'>Status: ").append(esc(h.getStatusBaru().getLabel())).append("</span>");
        html.append("</div></html>");
        JLabel text = new JLabel(html.toString());
        text.setFont(Theme.font(Font.PLAIN, 13));
        text.setForeground(Theme.TEXT);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Theme.statusForeground(h.getStatusBaru()));
                g.fillOval(2, 6, 10, 10);
                g.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(16, 16));
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.TABLE_LINE), Theme.padding(8, 0, 8, 0)));
        JPanel dotWrap = new JPanel(new BorderLayout());
        dotWrap.setOpaque(false);
        dotWrap.add(dot, BorderLayout.NORTH);
        row.add(dotWrap, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }

    public ReceiptPanel getPanel() { return panel; }
    public JButton getBackButton() { return backButton; }
    public JButton getSendButton() { return sendButton; }
    public JButton getDownloadButton() { return downloadButton; }
}