package com.example.trafficlightsim.view;

import com.example.trafficlightsim.model.ViolationRecord;
import com.example.trafficlightsim.util.Rupiah;
import com.example.trafficlightsim.view.component.RoundedButton;
import com.example.trafficlightsim.view.component.RoundedPanel;
import com.example.trafficlightsim.view.component.TableStyler;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;

/** Tab "Tinjau Sanggahan": operator membaca sanggahan penerima lalu menerima atau menolaknya. */
public class DisputeReviewPanel extends JPanel {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] COLUMNS = {"ID LOG", "PEMILIK", "PLAT", "PELANGGARAN", "DENDA (Rp)", "WAKTU"};

    private final RecordListModel model = new RecordListModel(COLUMNS, (r, c) -> {
        switch (c) {
            case 0: return r.getKodeLog();
            case 1: return r.getOwnerName();
            case 2: return r.getDisplayPlate();
            case 3: return r.getViolationSummary();
            case 4: return Rupiah.format(r.getTotalFine());
            default: return TIME.format(r.getTime());
        }
    });
    private final JTable table = new JTable(model);
    private final JLabel title = new JLabel("Pilih sanggahan di daftar sebelah kiri");
    private final JTextArea info = new JTextArea(3, 20); // teks biasa (bukan HTML) agar tinggi selalu pas
    private final JTextArea reason = new JTextArea();
    private final JLabel countLabel = new JLabel(" ");
    private final RoundedButton acceptButton = RoundedButton.filled("Terima Sanggahan", Theme.SUCCESS);
    private final RoundedButton rejectButton = RoundedButton.filled("Tolak Sanggahan", Theme.DANGER);

    public DisputeReviewPanel() {
        super(new BorderLayout(0, 14));
        setOpaque(false);

        countLabel.setFont(Theme.BODY_BOLD);
        countLabel.setForeground(Theme.TEXT_MUTED);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        top.setBorder(Theme.padding(12, 4, 0, 0));
        top.add(countLabel);
        add(top, BorderLayout.NORTH);

        TableStyler.apply(table, 0);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] widths = {120, 150, 90, 190, 110, 160};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JComponent tableCard = TableStyler.card(table);
        tableCard.setPreferredSize(new Dimension(700, 300));
        tableCard.setMinimumSize(new Dimension(420, 100));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableCard, buildDetailCard());
        split.setOpaque(false);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setResizeWeight(0.58);
        split.setDividerSize(14);
        split.setContinuousLayout(true);
        add(split, BorderLayout.CENTER);
        clearDetail();
    }

    private JComponent buildDetailCard() {
        RoundedPanel card = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(Theme.padding(18, 20, 18, 20));

        title.setFont(Theme.HEADING);
        title.setForeground(Theme.TEXT);
        info.setEditable(false);
        info.setOpaque(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setBorder(null);
        info.setFont(Theme.BODY);
        info.setForeground(Theme.TEXT_MUTED);
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(title);
        top.add(Box.createVerticalStrut(6));
        top.add(info);
        top.add(Box.createVerticalStrut(8));
        JLabel caption = new JLabel("Alasan sanggahan penerima:");
        caption.setFont(Theme.LABEL);
        caption.setForeground(Theme.TEXT_MUTED);
        caption.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(caption);
        card.add(top, BorderLayout.NORTH);

        reason.setEditable(false);
        reason.setLineWrap(true);
        reason.setWrapStyleWord(true);
        reason.setFont(Theme.font(Font.PLAIN, 14));
        reason.setForeground(Theme.TEXT);
        reason.setBackground(Color.WHITE);
        reason.setBorder(Theme.padding(10, 12, 10, 12));
        JScrollPane scroll = new JScrollPane(reason);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        card.add(scroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 12, 0));
        buttons.setOpaque(false);
        buttons.add(rejectButton);
        buttons.add(acceptButton);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    public void setRecords(List<ViolationRecord> records) {
        model.setRecords(records);
        countLabel.setText(records.isEmpty() ? "Tidak ada sanggahan yang menunggu peninjauan."
                : records.size() + " sanggahan menunggu peninjauan");
    }

    public boolean selectById(long id) {
        int row = model.indexOfId(id);
        if (row < 0) return false;
        int view = table.convertRowIndexToView(row);
        table.getSelectionModel().setSelectionInterval(view, view);
        return true;
    }

    public ViolationRecord getSelectedRecord() {
        int row = table.getSelectedRow();
        return row < 0 ? null : model.getRecord(table.convertRowIndexToModel(row));
    }

    public void clearDetail() {
        title.setText("Pilih sanggahan di daftar sebelah kiri");
        info.setText("");
        reason.setText("");
        acceptButton.setEnabled(false);
        rejectButton.setEnabled(false);
    }

    /** Menampilkan satu sanggahan beserta alasannya. */
    public void showDispute(ViolationRecord r, String alasan) {
        title.setText(r.getKodeLog() + " \u2022 " + r.getDisplayPlate());
        info.setText("Pemilik: " + r.getOwnerName() + "\nEmail: " + r.getEmailTujuan()
                + "\nPelanggaran: " + r.getViolationSummary() + " (" + Rupiah.format(r.getTotalFine()) + ")");
        reason.setText(alasan == null || alasan.isEmpty() ? "(alasan tidak ditemukan)" : alasan);
        reason.setCaretPosition(0);
        acceptButton.setEnabled(true);
        rejectButton.setEnabled(true);
    }

    /** Dialog catatan keputusan. Mengembalikan teks (boleh kosong bila tidak wajib), atau null jika dibatalkan. */
    public String askNote(Component parent, String dialogTitle, String prompt) {
        JTextArea area = new JTextArea(5, 36);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(Theme.BODY);
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.add(new JLabel(prompt), BorderLayout.NORTH);
        p.add(new JScrollPane(area), BorderLayout.CENTER);
        int ok = JOptionPane.showConfirmDialog(parent, p, dialogTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return ok == JOptionPane.OK_OPTION ? area.getText() : null;
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public JTable getTable() { return table; }
    public JButton getAcceptButton() { return acceptButton; }
    public JButton getRejectButton() { return rejectButton; }
}