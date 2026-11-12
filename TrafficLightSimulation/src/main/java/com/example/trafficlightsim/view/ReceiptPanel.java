package com.example.trafficlightsim.view;

import com.example.trafficlightsim.model.ViolationRecord;
import com.example.trafficlightsim.model.ViolationType;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.MatteBorder;

/** Lembar "Bukti Pelanggaran Lampu Lalu Lintas". Dibuat sebagai panel agar nanti bisa dicetak apa adanya. */
public class ReceiptPanel extends JPanel {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final JLabel plate = valueLabel();
    private final JTextArea violations = new JTextArea(4, 30);
    private final JLabel time = valueLabel();
    private final JLabel jalur = valueLabel();
    private final JLabel kind = valueLabel();

    public ReceiptPanel() {
        super(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        JLabel title = new JLabel("BUKTI PELANGGARAN LAMPU LALU LINTAS", SwingConstants.CENTER);
        title.setFont(Theme.font(Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(Theme.NAVY);
        titleBar.setBorder(Theme.padding(18, 28, 18, 28));
        titleBar.add(title, BorderLayout.CENTER);
        add(titleBar, BorderLayout.NORTH);

        violations.setEditable(false);
        violations.setFocusable(false); // supaya kursor teks tidak ikut tampil di lembar / file PNG
        violations.setOpaque(false);
        violations.setLineWrap(true);
        violations.setWrapStyleWord(true);
        violations.setFont(Theme.font(Font.PLAIN, 15));
        violations.setForeground(Theme.TEXT);

        JLabel name = valueLabel(); // kosong: diisi tulis tangan
        name.setBorder(new MatteBorder(0, 0, 1, 0, Theme.TEXT_MUTED));
        name.setText(" ");

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);
        rows.setBorder(Theme.padding(4, 28, 8, 28));
        rows.add(row("Nama", name, false));
        rows.add(row("Plat Kendaraan", plate, false));
        rows.add(row("Pelanggaran", violations, false));
        rows.add(row("Waktu", time, false));
        rows.add(row("Jalur", jalur, false));
        rows.add(row("Jenis Kendaraan", kind, true));
        add(rows, BorderLayout.CENTER);
        setPreferredSize(new Dimension(600, 440));
    }

    private static JLabel valueLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(Theme.font(Font.PLAIN, 15));
        l.setForeground(Theme.TEXT);
        return l;
    }

    private JComponent row(String label, JComponent value, boolean last) {
        JLabel l = new JLabel(label);
        l.setFont(Theme.font(Font.BOLD, 15));
        l.setForeground(Theme.TEXT_MUTED);
        l.setPreferredSize(new Dimension(150, 22));
        l.setVerticalAlignment(SwingConstants.TOP);
        JLabel colon = new JLabel(":");
        colon.setFont(Theme.font(Font.PLAIN, 15));
        colon.setForeground(Theme.TEXT_MUTED);
        colon.setVerticalAlignment(SwingConstants.TOP);
        JPanel left = new JPanel(new BorderLayout(8, 0));
        left.setOpaque(false);
        left.add(l, BorderLayout.CENTER);
        left.add(colon, BorderLayout.EAST);
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                last ? BorderFactory.createEmptyBorder() : new MatteBorder(0, 0, 1, 0, Theme.TABLE_LINE),
                Theme.padding(12, 0, 12, 0)));
        p.add(left, BorderLayout.WEST);
        p.add(value, BorderLayout.CENTER);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    /** Mengisi lembar dari satu catatan pelanggaran. */
    public void setRecord(ViolationRecord r) {
        plate.setText(r.getPlate());
        violations.setText(r.getViolations().stream()
                .map(ViolationType::getLabel).map(s -> "• " + s).collect(Collectors.joining("\n")));
        time.setText(TIME.format(r.getTime()));
        jalur.setText(r.getJalur().getLabel());
        kind.setText(r.getVehicleKind().getLabel());
    }

    public void clear() {
        plate.setText(" "); violations.setText(""); time.setText(" "); jalur.setText(" "); kind.setText(" ");
    }
}
