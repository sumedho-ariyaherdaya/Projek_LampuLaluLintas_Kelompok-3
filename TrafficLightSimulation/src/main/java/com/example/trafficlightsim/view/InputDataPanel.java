package com.example.trafficlightsim.view;

import com.example.trafficlightsim.model.JenisKendaraan;
import com.example.trafficlightsim.model.Jalur;
import com.example.trafficlightsim.model.ViolationType;
import com.example.trafficlightsim.view.component.HeaderBar;
import com.example.trafficlightsim.view.component.RoundedButton;
import com.example.trafficlightsim.view.component.RoundedPanel;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.*;

/** Isi form "Input Data Pelanggaran" (dipakai oleh InputDataDialog). */
public class InputDataPanel extends JPanel {
    private final JTextField plateField = new JTextField();
    private final Map<ViolationType, JCheckBox> violationBoxes = new EnumMap<>(ViolationType.class);
    private final JComboBox<Jalur> jalurBox = new JComboBox<>(Jalur.values());
    private final JTextField totalFineField = new JTextField("Rp0");
    private final JComboBox<JenisKendaraan> kindBox = new JComboBox<>(JenisKendaraan.values());
    private final RoundedButton submitButton = RoundedButton.filled("Kirim", Theme.TEAL);

    public InputDataPanel() {
        super(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        add(new HeaderBar("Input Data Pelanggaran"), BorderLayout.NORTH);

        Theme.styleInput(plateField);
        Theme.styleInput(jalurBox);
        Theme.styleInput(totalFineField);
        totalFineField.setEditable(false);
        Theme.styleInput(kindBox);
        jalurBox.setSelectedIndex(-1);
        kindBox.setSelectedIndex(-1);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(Theme.padding(24, 28, 24, 28));
        form.add(group("Plat nomor", plateField));
        form.add(Box.createVerticalStrut(16));
        form.add(buildViolationCard());
        form.add(Box.createVerticalStrut(16));
        form.add(group("Jalur", jalurBox));
        form.add(Box.createVerticalStrut(16));
        form.add(group("Total denda", totalFineField));
        form.add(Box.createVerticalStrut(16));
        form.add(group("Jenis kendaraan", kindBox));
        form.add(Box.createVerticalStrut(20));
        submitButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitButton.setPreferredSize(new Dimension(10, 48));
        submitButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        form.add(submitButton);
        add(form, BorderLayout.CENTER);
        setPreferredSize(new Dimension(520, 780));
    }

    private JComponent group(String label, JComponent field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(label(label));
        p.add(Box.createVerticalStrut(6));
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
        card.setBorder(Theme.padding(12, 16, 12, 16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(label("Pelanggaran"));
        card.add(Box.createVerticalStrut(8));
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
            card.add(Box.createVerticalStrut(4));
        }
        return card;
    }

    public JTextField getPlateField() { return plateField; }
    public Map<ViolationType, JCheckBox> getViolationBoxes() { return violationBoxes; }
    public JComboBox<Jalur> getJalurBox() { return jalurBox; }
    public JTextField getTotalFineField() { return totalFineField; }
    public JComboBox<JenisKendaraan> getKindBox() { return kindBox; }
    public JButton getSubmitButton() { return submitButton; }
}
