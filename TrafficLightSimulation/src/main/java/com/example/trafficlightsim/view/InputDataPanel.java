package com.example.trafficlightsim.view;

import com.example.trafficlightsim.model.Jalur;
import com.example.trafficlightsim.model.JenisKendaraan;
import com.example.trafficlightsim.model.ViolationType;
import com.example.trafficlightsim.model.WarnaKendaraan;
import com.example.trafficlightsim.view.component.RoundedButton;
import com.example.trafficlightsim.view.component.RoundedPanel;
import java.awt.*;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

/**
 * Form "Input Data Pelanggaran" versi ringkas dua kolom. Dipasang di panel kanan halaman simulasi
 * (area di bawah Total Pelanggaran), bukan lagi di jendela terpisah.
 */
public class InputDataPanel extends JPanel implements Scrollable {
    private static final int FIELD_HEIGHT = 36;
    private static final int FIELD_WIDTH = 170; // lebar dasar kolom kiri; melebar mengikuti panel
    private static final Color LOCKED_BG = new Color(33, 37, 41); // latar plat saat terkunci (dihitamkan)

    private final JTextField plateField = new JTextField();
    private final Map<ViolationType, JCheckBox> violationBoxes = new EnumMap<>(ViolationType.class);
    private final JComboBox<Jalur> jalurBox = new JComboBox<>(Jalur.values());
    private final JTextField totalFineField = new JTextField("Rp0");
    private final JComboBox<JenisKendaraan> kindBox = new JComboBox<>(JenisKendaraan.values());
    private final JComboBox<String> brandBox = new JComboBox<>();
    private final JComboBox<WarnaKendaraan> colorBox = new JComboBox<>(WarnaKendaraan.values());
    private final JTextField ownerField = new JTextField("-");
    private final RoundedButton submitButton = RoundedButton.filled("Kirim", Theme.TEAL);
    private final RoundedButton cancelButton = RoundedButton.outline("Batal");

    public InputDataPanel() {
        super(new GridBagLayout());
        setBackground(Theme.BACKGROUND);
        setBorder(Theme.padding(4, 2, 4, 2));

        for (JComponent c : new JComponent[] {plateField, jalurBox, totalFineField, kindBox, brandBox, colorBox, ownerField}) {
            Theme.styleInput(c);
            c.setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
            c.setMinimumSize(new Dimension(100, FIELD_HEIGHT));
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT));
        }
        totalFineField.setEditable(false);
        ownerField.setEditable(false);
        jalurBox.setSelectedIndex(-1);
        kindBox.setSelectedIndex(-1);
        colorBox.setSelectedIndex(-1);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0.5;
        c.insets = new Insets(0, 6, 8, 6);

        JLabel title = new JLabel("Input Data Pelanggaran");
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.TEXT);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        add(title, c);
        c.gridwidth = 1;

        // Kolom kiri: identitas kendaraan.
        JComponent[] left = {
            group("Plat nomor", plateField), group("Jenis kendaraan", kindBox), group("Merek", brandBox),
            group("Warna", colorBox), group("Jalur", jalurBox)
        };
        for (int i = 0; i < left.length; i++) {
            c.gridx = 0; c.gridy = 1 + i;
            add(left[i], c);
        }

        // Kolom kanan: daftar pelanggaran, memanjang sebanyak lima baris kiri.
        c.gridx = 1; c.gridy = 1; c.gridheight = left.length; c.fill = GridBagConstraints.BOTH;
        add(buildViolationCard(), c);
        c.gridheight = 1; c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 1 + left.length;
        add(group("Pemilik (otomatis)", ownerField), c);
        c.gridx = 1;
        add(group("Total denda", totalFineField), c);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 12, 0));
        buttons.setOpaque(false);
        buttons.add(cancelButton);
        buttons.add(submitButton);
        c.gridx = 0; c.gridy = 2 + left.length; c.gridwidth = 2; c.insets = new Insets(4, 6, 4, 6);
        add(buttons, c);
    }

    private JComponent group(String label, JComponent field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.add(label(label));
        p.add(Box.createVerticalStrut(4));
        p.add(field);
        return p;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.LABEL);
        l.setForeground(Theme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JComponent buildViolationCard() {
        RoundedPanel card = new RoundedPanel(12, Theme.CARD, Theme.BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(Theme.padding(10, 14, 10, 14));
        card.add(label("Pelanggaran"));
        card.add(Box.createVerticalStrut(6));
        for (ViolationType t : ViolationType.values()) {
            JCheckBox box = new JCheckBox(t.getLabel());
            box.setIcon(Theme.checkIcon(false));
            box.setSelectedIcon(Theme.checkIcon(true));
            box.setFont(Theme.BODY);
            box.setForeground(Theme.TEXT);
            box.setOpaque(false);
            box.setFocusPainted(false);
            box.setIconTextGap(10);
            box.setAlignmentX(Component.LEFT_ALIGNMENT);
            violationBoxes.put(t, box);
            card.add(box);
            card.add(Box.createVerticalStrut(2));
        }
        return card;
    }

    /** Mengganti pilihan merek (tanpa memilih satu pun). */
    public void setBrands(List<String> brands) {
        brandBox.setModel(new DefaultComboBoxModel<>(brands.toArray(new String[0])));
        brandBox.setSelectedIndex(-1);
    }

    /** Jenis, merek, dan warna dikunci saat kendaraan sudah dikenali dari plat. */
    public void setTraitsEditable(boolean editable) {
        kindBox.setEnabled(editable);
        brandBox.setEnabled(editable);
        colorBox.setEnabled(editable);
    }

    /**
    * Mode normal (noPlate = false): plat bisa diketik; jenis/merek/warna terkunci dan terisi otomatis dari plat.
    * Mode tanpa plat (noPlate = true): plat dihitamkan dan terkunci (terisi otomatis); jenis/merek/warna bisa diisi.
    */
    public void setNoPlateMode(boolean noPlate) {
       plateField.setEditable(!noPlate);
       plateField.setFocusable(!noPlate);
       plateField.setBackground(noPlate ? LOCKED_BG : Color.WHITE);
       plateField.setForeground(noPlate ? Color.WHITE : Theme.TEXT);
       plateField.setToolTipText(noPlate ? "Terisi otomatis setelah jenis, merek, dan warna dipilih" : null);
       setTraitsEditable(noPlate);
   } 

    /** Mengosongkan form (dipanggil setiap form dibuka). Pilihan merek diisi ulang oleh controller. */
    public void reset() {
        plateField.setText("");
        for (JCheckBox box : violationBoxes.values()) box.setSelected(false);
        jalurBox.setSelectedIndex(-1);
        kindBox.setSelectedIndex(-1);
        colorBox.setSelectedIndex(-1);
        brandBox.setSelectedIndex(-1);
        totalFineField.setText("Rp0");
        ownerField.setText("-");
        setNoPlateMode(false);   
    }
    

    public JTextField getPlateField() { return plateField; }
    public Map<ViolationType, JCheckBox> getViolationBoxes() { return violationBoxes; }
    public JComboBox<Jalur> getJalurBox() { return jalurBox; }
    public JTextField getTotalFineField() { return totalFineField; }
    public JComboBox<JenisKendaraan> getKindBox() { return kindBox; }
    public JComboBox<String> getBrandBox() { return brandBox; }
    public JComboBox<WarnaKendaraan> getColorBox() { return colorBox; }
    public JTextField getOwnerField() { return ownerField; }
    public JButton getSubmitButton() { return submitButton; }
    public JButton getCancelButton() { return cancelButton; }

    // --- Scrollable: lebar mengikuti area; muncul scrollbar vertikal jika layar pendek ---
    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 80; }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() {
        return getParent() instanceof JViewport && ((JViewport) getParent()).getHeight() > getPreferredSize().height;
    }
}