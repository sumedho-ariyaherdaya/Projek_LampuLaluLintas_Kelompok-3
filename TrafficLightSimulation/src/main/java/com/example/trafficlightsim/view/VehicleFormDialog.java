package com.example.trafficlightsim.view;

import com.example.trafficlightsim.model.JenisKendaraan;
import com.example.trafficlightsim.model.VehicleIdentity;
import com.example.trafficlightsim.model.WarnaKendaraan;
import com.example.trafficlightsim.view.component.GradientPanel;
import com.example.trafficlightsim.view.component.RoundedButton;
import java.awt.*;
import java.util.function.Function;
import javax.swing.*;

/**
 * Form tambah / ubah kendaraan (jendela modal). Tombol Simpan memanggil saveHandler; bila handler
 * mengembalikan pesan kesalahan, pesan itu ditampilkan di form dan jendela tetap terbuka.
 */
public class VehicleFormDialog extends JDialog {
    private final JTextField ownerField = new JTextField();
    private final JTextField plateField = new JTextField();
    private final JComboBox<JenisKendaraan> kindBox = new JComboBox<>(JenisKendaraan.values());
    private final JTextField brandField = new JTextField();
    private final JComboBox<WarnaKendaraan> colorBox = new JComboBox<>(WarnaKendaraan.values());
    private final JTextField emailField = new JTextField();
    private final JLabel errorLabel = new JLabel(" ");
    private final RoundedButton saveButton = RoundedButton.filled("Simpan", Theme.TEAL);
    private final RoundedButton cancelButton = RoundedButton.outline("Batal");
    private long editingId; // 0 = kendaraan baru
    private Function<VehicleIdentity, String> saveHandler = v -> null;

    public VehicleFormDialog(Frame owner) {
        super(owner, "Kendaraan", true);
        for (JComponent c : new JComponent[] {ownerField, plateField, kindBox, brandField, colorBox, emailField}) {
            Theme.styleInput(c);
            c.setPreferredSize(new Dimension(300, 40));
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        }
        ownerField.setToolTipText("Nama lengkap pemilik");
        plateField.setToolTipText("Contoh: B 1234 XY (spasi boleh dikosongkan)");
        emailField.setToolTipText("Alamat tujuan pengiriman log pelanggaran");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BACKGROUND);
        form.setBorder(Theme.padding(18, 24, 8, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 10, 0);
        c.gridx = 0;
        JComponent[][] rows = {
            {label("Pemilik"), ownerField}, {label("Plat nomor"), plateField}, {label("Jenis kendaraan"), kindBox},
            {label("Merek"), brandField}, {label("Warna"), colorBox}, {label("Email"), emailField}
        };
        int y = 0;
        for (JComponent[] r : rows) {
            c.gridy = y++;
            JPanel g = new JPanel();
            g.setLayout(new BoxLayout(g, BoxLayout.Y_AXIS));
            g.setOpaque(false);
            r[0].setAlignmentX(Component.LEFT_ALIGNMENT);
            r[1].setAlignmentX(Component.LEFT_ALIGNMENT);
            g.add(r[0]);
            g.add(Box.createVerticalStrut(4));
            g.add(r[1]);
            form.add(g, c);
        }
        errorLabel.setFont(Theme.BODY_BOLD);
        errorLabel.setForeground(Theme.ALERT_TEXT);
        c.gridy = y;
        c.insets = new Insets(0, 0, 4, 0);
        form.add(errorLabel, c);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 12, 0));
        buttons.setOpaque(false);
        buttons.add(cancelButton);
        buttons.add(saveButton);
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(Theme.BACKGROUND);
        south.setBorder(Theme.padding(4, 24, 20, 24));
        south.add(buttons, BorderLayout.CENTER);

        JLabel title = new JLabel("Data Kendaraan");
        title.setFont(Theme.HEADING);
        title.setForeground(Color.WHITE);
        JPanel titleBar = new GradientPanel(Theme.MAROON_DEEP, Theme.RED, true).withAccent(Theme.GOLD);
        titleBar.setBorder(Theme.padding(14, 24, 14, 24));
        titleBar.add(title, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout());
        content.add(titleBar, BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);
        content.add(south, BorderLayout.SOUTH);
        setContentPane(content);
        setResizable(false);

        cancelButton.addActionListener(e -> dispose());
        saveButton.addActionListener(e -> save());
        getRootPane().setDefaultButton(saveButton);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.LABEL);
        l.setForeground(Theme.TEXT_MUTED);
        return l;
    }

    /** Menyimpan lewat saveHandler; menutup jendela jika berhasil (handler mengembalikan null). */
    private void save() {
        VehicleIdentity v = new VehicleIdentity(editingId, plateField.getText().trim(), ownerField.getText().trim(),
                (JenisKendaraan) kindBox.getSelectedItem(), (WarnaKendaraan) colorBox.getSelectedItem(),
                brandField.getText().trim(), emailField.getText().trim());
        String error = saveHandler.apply(v);
        if (error == null) dispose();
        else errorLabel.setText("<html>" + error.replace("&", "&amp;").replace("<", "&lt;").replace("\n", "<br>") + "</html>");
    }

    /** Membuka form kosong untuk kendaraan baru. */
    public void openForNew(Component relativeTo, Function<VehicleIdentity, String> handler) {
        open(relativeTo, "Tambah Kendaraan", null, handler);
    }

    /** Membuka form berisi data kendaraan yang diubah. */
    public void openForEdit(Component relativeTo, VehicleIdentity v, Function<VehicleIdentity, String> handler) {
        open(relativeTo, "Ubah Kendaraan", v, handler);
    }

    private void open(Component relativeTo, String title, VehicleIdentity v, Function<VehicleIdentity, String> handler) {
        setTitle(title);
        saveHandler = handler;
        editingId = v == null ? 0 : v.getId();
        ownerField.setText(v == null ? "" : v.getOwner());
        plateField.setText(v == null ? "" : v.getPlate());
        kindBox.setSelectedItem(v == null ? JenisKendaraan.MOBIL : v.getKind());
        brandField.setText(v == null ? "" : v.getBrand());
        colorBox.setSelectedItem(v == null ? WarnaKendaraan.MERAH : v.getColor());
        emailField.setText(v == null ? "" : v.getEmail());
        errorLabel.setText(" ");
        pack();
        setSize(Math.max(getWidth(), 380), getHeight() + 40); // ruang untuk pesan kesalahan dua baris
        setLocationRelativeTo(relativeTo);
        ownerField.requestFocusInWindow();
        setVisible(true); // modal: kembali setelah form ditutup
    }

    // untuk pengujian
    public JTextField getOwnerField() { return ownerField; }
    public JTextField getPlateField() { return plateField; }
    public JComboBox<JenisKendaraan> getKindBox() { return kindBox; }
    public JTextField getBrandField() { return brandField; }
    public JComboBox<WarnaKendaraan> getColorBox() { return colorBox; }
    public JTextField getEmailField() { return emailField; }
    public JButton getSaveButton() { return saveButton; }
    public JButton getCancelButton() { return cancelButton; }
}