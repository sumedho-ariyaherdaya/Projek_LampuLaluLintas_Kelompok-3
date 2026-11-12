package com.example.trafficlightsim.controller;

import com.example.trafficlightsim.DashboardFrame;
import com.example.trafficlightsim.model.Jalur;
import com.example.trafficlightsim.model.JenisKendaraan;
import com.example.trafficlightsim.model.ViolationRecord;
import com.example.trafficlightsim.model.ViolationType;
import com.example.trafficlightsim.service.ViolationLogService;
import com.example.trafficlightsim.util.Rupiah;
import com.example.trafficlightsim.view.InputDataDialog;
import com.example.trafficlightsim.view.InputDataPanel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

/**
 * Halaman Input Data: membuka form, tombol Kirim, dan memunculkan data baru di Daftar Pelanggaran.
 * (Cari dan Filter pada Daftar Pelanggaran masih TODO.)
 */
public class ViolationController {
    private final DashboardFrame frame;
    private final ViolationLogService service;

    public ViolationController(DashboardFrame frame, ViolationLogService service) {
        this.frame = frame;
        this.service = service;
        refreshViews();
    }

    /** Halaman 1 -> Halaman 3. Form baru (kosong) dibuat setiap kali dibuka. */
    public void openInputDialog() {
        InputDataDialog dialog = new InputDataDialog(frame);
        InputDataPanel form = dialog.getPanel();
        // Total denda ikut berubah saat pelanggaran dicentang / dilepas.
        for (JCheckBox box : form.getViolationBoxes().values()) {
            box.addActionListener(e -> form.getTotalFineField().setText(Rupiah.format(selectedFine(form))));
        }
        form.getSubmitButton().addActionListener(e -> {
            if (submit(form, dialog)) dialog.dispose();
        });
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private int selectedFine(InputDataPanel form) {
        int total = 0;
        for (Map.Entry<ViolationType, JCheckBox> en : form.getViolationBoxes().entrySet()) {
            if (en.getValue().isSelected()) total += en.getKey().getFine();
        }
        return total;
    }

    /** Tombol Kirim. Mengembalikan true jika data valid dan sudah disimpan. */
    private boolean submit(InputDataPanel form, InputDataDialog dialog) {
        List<ViolationType> chosen = new ArrayList<>();
        for (Map.Entry<ViolationType, JCheckBox> en : form.getViolationBoxes().entrySet()) {
            if (en.getValue().isSelected()) chosen.add(en.getKey());
        }
        String plate = form.getPlateField().getText().trim().toUpperCase();
        Jalur jalur = (Jalur) form.getJalurBox().getSelectedItem();
        JenisKendaraan kind = (JenisKendaraan) form.getKindBox().getSelectedItem();

        String error = null;
        if (plate.isEmpty() && !chosen.contains(ViolationType.NO_PLATE)) error = "Plat nomor belum diisi.";
        else if (chosen.isEmpty()) error = "Pilih minimal satu pelanggaran.";
        else if (jalur == null) error = "Jalur belum dipilih.";
        else if (kind == null) error = "Jenis kendaraan belum dipilih.";
        if (error != null) {
            JOptionPane.showMessageDialog(dialog, error, "Data belum lengkap", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (plate.isEmpty()) plate = "-"; // kendaraan memang tanpa plat (pelanggaran "Tidak ada plat nomor")

        service.add(new ViolationRecord(plate, chosen, LocalDateTime.now(), jalur, kind));
        refreshViews();
        return true;
    }

    /** Menyegarkan Daftar Pelanggaran (halaman 2) dan kartu Total Pelanggaran (halaman 1). */
    private void refreshViews() {
        frame.getListPanel().setRecords(service.getAll());
        frame.getDashboardPanel().getTotalCard().setTotal(service.size());
    }
}
