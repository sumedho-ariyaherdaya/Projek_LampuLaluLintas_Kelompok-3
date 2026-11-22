package com.example.trafficlightsim.view;

import com.example.trafficlightsim.model.RiwayatLog;
import com.example.trafficlightsim.model.StatusPelanggaran;
import com.example.trafficlightsim.model.ViolationRecord;
import com.example.trafficlightsim.model.ViolationType;
import com.example.trafficlightsim.util.Rupiah;
import com.example.trafficlightsim.view.component.RoundedButton;
import com.example.trafficlightsim.view.component.RoundedPanel;
import com.example.trafficlightsim.view.component.TableStyler;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;

/**
 * Tab "Kotak Masuk": simulasi kotak surat penerima. Penerima dipilih dari alamat email yang pernah
 * dikirimi log, membuka pesan (status menjadi Diterima), lalu mengonfirmasi atau mengajukan sanggahan.
 */
public class InboxPanel extends JPanel {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] COLUMNS = {"ID LOG", "PELANGGARAN", "WAKTU", "DENDA (Rp)", "STATUS"};

    private final JComboBox<String> recipientBox = new JComboBox<>();
    private final JLabel countLabel = new JLabel(" ");
    private final RecordListModel model = new RecordListModel(COLUMNS, (r, c) -> {
        switch (c) {
            case 0: return r.getKodeLog();
            case 1: return r.getViolationSummary();
            case 2: return TIME.format(r.getTime());
            case 3: return Rupiah.format(r.getTotalFine());
            default: return r.getStatus();
        }
    });
    private final JTable table = new JTable(model);

    private final JLabel subject = new JLabel("Pilih pesan di daftar sebelah kiri");
    private final JLabel header = new JLabel(" ");
    private final JLabel statusLabel = new JLabel(" ");
    private final JTextArea body = new JTextArea();
    private final JLabel result = new JLabel(" ");
    private final RoundedButton confirmButton = RoundedButton.filled("Konfirmasi Pelanggaran", Theme.SUCCESS);
    private final RoundedButton disputeButton = RoundedButton.outline("Ajukan Sanggahan");

    public InboxPanel() {
        super(new BorderLayout(0, 14));
        setOpaque(false);
        add(buildToolbar(), BorderLayout.NORTH);

        TableStyler.apply(table, 0);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] widths = {120, 220, 170, 110, 120};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JComponent tableCard = TableStyler.card(table);
        tableCard.setPreferredSize(new Dimension(620, 300));
        tableCard.setMinimumSize(new Dimension(420, 100));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableCard, buildDetailCard());
        split.setOpaque(false);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setResizeWeight(0.55);
        split.setDividerSize(14);
        split.setContinuousLayout(true);
        add(split, BorderLayout.CENTER);
        clearDetail();
    }

    private JComponent buildToolbar() {
        Theme.styleInput(recipientBox);
        recipientBox.setPreferredSize(new Dimension(340, 44));
        JLabel label = new JLabel("Masuk sebagai (email penerima):");
        label.setFont(Theme.BODY_BOLD);
        label.setForeground(Theme.TEXT);
        countLabel.setFont(Theme.BODY);
        countLabel.setForeground(Theme.TEXT_MUTED);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        left.add(label);
        left.add(recipientBox);
        left.add(countLabel);
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.add(left, BorderLayout.WEST);
        return bar;
    }

    private JComponent buildDetailCard() {
        RoundedPanel card = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(Theme.padding(18, 20, 18, 20));

        subject.setFont(Theme.HEADING);
        subject.setForeground(Theme.TEXT);
        header.setFont(Theme.font(Font.PLAIN, 12));
        header.setForeground(Theme.TEXT_MUTED);
        statusLabel.setFont(Theme.BODY_BOLD);
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        for (JComponent c : new JComponent[] {subject, header, statusLabel}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            top.add(c);
            top.add(Box.createVerticalStrut(4));
        }
        card.add(top, BorderLayout.NORTH);

        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13)); // rata kolom seperti email teks
        body.setForeground(Theme.TEXT);
        body.setBackground(Color.WHITE);
        body.setBorder(Theme.padding(10, 12, 10, 12));
        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        card.add(scroll, BorderLayout.CENTER);

        result.setFont(Theme.BODY);
        result.setForeground(Theme.TEXT_MUTED);
        JPanel buttons = new JPanel(new GridLayout(1, 2, 12, 0));
        buttons.setOpaque(false);
        buttons.add(disputeButton);
        buttons.add(confirmButton);
        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);
        bottom.add(result, BorderLayout.NORTH);
        bottom.add(buttons, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    // ------------------------------------------------------------------------------------------

    public void setRecipients(List<String> emails) {
        Object keep = recipientBox.getSelectedItem();
        recipientBox.setModel(new DefaultComboBoxModel<>(emails.toArray(new String[0])));
        if (keep != null && emails.contains(keep)) recipientBox.setSelectedItem(keep);
        else recipientBox.setSelectedIndex(emails.isEmpty() ? -1 : 0);
    }

    public String getSelectedRecipient() { return (String) recipientBox.getSelectedItem(); }

    public void setMessages(List<ViolationRecord> records) {
        model.setRecords(records);
        long baru = records.stream().filter(r -> r.getStatus() == StatusPelanggaran.TERKIRIM).count();
        countLabel.setText(records.size() + " pesan" + (baru > 0 ? ", " + baru + " belum dibuka" : ""));
    }

    /** Memilih baris pesan berdasarkan id database; true jika ditemukan. */
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
        subject.setText("Pilih pesan di daftar sebelah kiri");
        header.setText(" ");
        statusLabel.setText(" ");
        body.setText("");
        result.setText(" ");
        confirmButton.setEnabled(false);
        disputeButton.setEnabled(false);
    }

    /** Menampilkan satu email beserta tanggapan yang sudah ada (dari riwayat). */
    public void showMessage(ViolationRecord r, List<RiwayatLog> history) {
        subject.setText("[Pemberitahuan] Pelanggaran lalu lintas " + r.getKodeLog());
        header.setText("<html>Dari: Sistem Simulasi Lalu Lintas &lt;noreply@lalulintas.example&gt;<br>Kepada: "
                + escape(r.getEmailTujuan()) + "</html>");
        StatusPelanggaran s = r.getStatus();
        statusLabel.setText("Status: " + s.getLabel());
        statusLabel.setForeground(Theme.statusForeground(s));
        body.setText(emailBody(r));
        body.setCaretPosition(0);
        boolean canRespond = s == StatusPelanggaran.DITERIMA;
        confirmButton.setEnabled(canRespond);
        disputeButton.setEnabled(canRespond);
        result.setText("<html>" + escape(responseSummary(s, history)) + "</html>");
    }

    private static String responseSummary(StatusPelanggaran s, List<RiwayatLog> history) {
        RiwayatLog last = null;
        for (RiwayatLog h : history) {
            switch (h.getAksi()) {
                case DIKONFIRMASI: case DISANGGAH: case SANGGAHAN_DITERIMA: case SANGGAHAN_DITOLAK: last = h; break;
                default: break;
            }
        }
        if (s == StatusPelanggaran.DITERIMA) return "Silakan konfirmasi pelanggaran ini atau ajukan sanggahan.";
        if (s == StatusPelanggaran.TERKIRIM) return "Pesan belum dibuka.";
        if (last == null) return " ";
        switch (last.getAksi()) {
            case DIKONFIRMASI: return "Anda telah mengonfirmasi pelanggaran ini. Terima kasih.";
            case DISANGGAH: return "Sanggahan Anda sedang ditinjau operator. Alasan Anda: " + last.getCatatan();
            case SANGGAHAN_DITERIMA: return "Sanggahan Anda DITERIMA operator. Catatan: " + last.getCatatan();
            default: return "Sanggahan Anda DITOLAK operator. Alasan: " + last.getCatatan();
        }
    }

    /** Isi email pemberitahuan (hasil simulasi; tidak benar-benar dikirim lewat jaringan). */
    public static String emailBody(ViolationRecord r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Yth. ").append(r.getOwnerName()).append(",\n\n");
        sb.append("Kendaraan Anda tercatat melakukan pelanggaran lalu lintas dengan rincian berikut.\n\n");
        sb.append("ID Log       : ").append(r.getKodeLog()).append('\n');
        sb.append("Plat         : ").append(r.getDisplayPlate()).append('\n');
        if (r.getVehicle() != null) {
            sb.append("Kendaraan    : ").append(r.getVehicleKind().getLabel()).append(' ')
              .append(r.getVehicle().getBrand()).append(", ").append(r.getVehicle().getColor().getLabel()).append('\n');
        }
        sb.append("Waktu        : ").append(TIME.format(r.getTime())).append('\n');
        sb.append("Jalur        : ").append(r.getJalur().getLabel()).append('\n');
        sb.append("Pelanggaran  :\n");
        for (ViolationType t : r.getViolations())
            sb.append("  - ").append(r.describe(t)).append(" (").append(Rupiah.format(t.getFine())).append(")\n");
        sb.append("Total denda  : ").append(Rupiah.format(r.getTotalFine())).append("\n\n");
        sb.append("Mohon konfirmasi pelanggaran ini, atau ajukan sanggahan beserta alasannya, melalui kotak masuk ini.\n\n");
        sb.append("Hormat kami,\nSistem Simulasi Lalu Lintas");
        return sb.toString();
    }

    /** Dialog isian alasan sanggahan. Mengembalikan teks, atau null jika dibatalkan. */
    public String askDisputeReason(Component parent, String initial) {
        JTextArea area = new JTextArea(initial == null ? "" : initial, 6, 36);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(Theme.BODY);
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.add(new JLabel("Jelaskan alasan sanggahan Anda (minimal 10 karakter):"), BorderLayout.NORTH);
        p.add(new JScrollPane(area), BorderLayout.CENTER);
        int ok = JOptionPane.showConfirmDialog(parent, p, "Ajukan Sanggahan", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        return ok == JOptionPane.OK_OPTION ? area.getText() : null;
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }

    public JComboBox<String> getRecipientBox() { return recipientBox; }
    public JTable getTable() { return table; }
    public JButton getConfirmButton() { return confirmButton; }
    public JButton getDisputeButton() { return disputeButton; }
}