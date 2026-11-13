package com.example.trafficlightsim.controller;

import com.example.trafficlightsim.DashboardFrame;
import com.example.trafficlightsim.database.KendaraanDao;
import com.example.trafficlightsim.model.Jalur;
import com.example.trafficlightsim.model.JenisKendaraan;
import com.example.trafficlightsim.model.VehicleIdentity;
import com.example.trafficlightsim.model.ViolationRecord;
import com.example.trafficlightsim.model.ViolationType;
import com.example.trafficlightsim.model.WarnaKendaraan;
import com.example.trafficlightsim.service.ViolationLogService;
import com.example.trafficlightsim.util.Rupiah;
import com.example.trafficlightsim.view.InputDataPanel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Form Input Data di panel kanan halaman simulasi: membuka form, mengenali kendaraan dari plat
 * atau dari jenis + merek + warna (untuk kendaraan tanpa plat), tombol Kirim/Batal, dan memunculkan
 * data baru di Daftar Pelanggaran.
 */
public class ViolationController {
    private final DashboardFrame frame;
    private final ViolationLogService service;
    private final KendaraanDao kendaraanDao = new KendaraanDao();
    private final InputDataPanel form;
    private boolean updating;       // true saat isi form diubah lewat kode, agar listener tidak saling memicu
    private VehicleIdentity matched; // kendaraan yang saat ini dikenali form

    public ViolationController(DashboardFrame frame, ViolationLogService service) {
        this.frame = frame;
        this.service = service;
        this.form = frame.getDashboardPanel().getInputForm();
        wireForm();
        refreshViews();
    }

    /** Tombol "Input data": mengosongkan form lalu memunculkannya di panel kanan. */
    public void openInputDialog() {
        updating = true;
        form.reset();
        form.setBrands(kendaraanDao.brands(null));
        updating = false;
        matched = null;
        frame.getDashboardPanel().showInputForm();
        form.getPlateField().requestFocusInWindow();
    }

    private void closeForm() { frame.getDashboardPanel().hideInputForm(); }

    private void wireForm() {
        // Total denda ikut berubah saat pelanggaran dicentang / dilepas.
        for (JCheckBox box : form.getViolationBoxes().values()) {
            box.addActionListener(e -> form.getTotalFineField().setText(Rupiah.format(selectedFine())));
        }
        form.getPlateField().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { identify(); }
            @Override public void removeUpdate(DocumentEvent e) { identify(); }
            @Override public void changedUpdate(DocumentEvent e) { identify(); }
        });
        form.getKindBox().addActionListener(e -> {
            if (updating) return;
            JenisKendaraan kind = (JenisKendaraan) form.getKindBox().getSelectedItem();
            updating = true;
            form.setBrands(kendaraanDao.brands(kind));
            updating = false;
            identify();
        });
        form.getBrandBox().addActionListener(e -> { if (!updating) identify(); });
        form.getColorBox().addActionListener(e -> { if (!updating) identify(); });
        form.getSubmitButton().addActionListener(e -> { if (submit()) closeForm(); });
        form.getCancelButton().addActionListener(e -> closeForm());
    }

    /**
     * Mengenali kendaraan. Plat terisi: cari berdasarkan plat dan isi jenis/merek/warna otomatis.
     * Plat kosong: cari berdasarkan jenis + merek + warna yang dipilih operator.
     */
    private void identify() {
        String plate = KendaraanDao.normalizePlate(form.getPlateField().getText());
        VehicleIdentity found;
        String status;
        if (!plate.isEmpty()) {
            found = kendaraanDao.findByPlate(plate);
            status = found != null ? found.getOwner() : "Plat tidak terdaftar";
            if (found != null) fillTraits(found);
            form.setTraitsEditable(found == null);
        } else {
            form.setTraitsEditable(true);
            JenisKendaraan kind = (JenisKendaraan) form.getKindBox().getSelectedItem();
            String brand = (String) form.getBrandBox().getSelectedItem();
            WarnaKendaraan color = (WarnaKendaraan) form.getColorBox().getSelectedItem();
            if (kind != null && brand != null && color != null) {
                found = kendaraanDao.findByTraits(kind, color, brand);
                status = found != null ? found.getOwner() : "Tidak ada kendaraan dengan ciri ini";
            } else {
                found = null;
                status = "-";
            }
        }
        matched = found;
        form.getOwnerField().setText(status);
    }

    private void fillTraits(VehicleIdentity v) {
        updating = true;
        form.getKindBox().setSelectedItem(v.getKind());
        form.setBrands(kendaraanDao.brands(v.getKind()));
        form.getBrandBox().setSelectedItem(v.getBrand());
        form.getColorBox().setSelectedItem(v.getColor());
        updating = false;
    }

    private int selectedFine() {
        int total = 0;
        for (Map.Entry<ViolationType, JCheckBox> en : form.getViolationBoxes().entrySet()) {
            if (en.getValue().isSelected()) total += en.getKey().getFine();
        }
        return total;
    }

    /** Tombol Kirim. Mengembalikan true jika data valid dan sudah disimpan. */
    private boolean submit() {
        identify(); // pastikan kendaraan yang dikenali sesuai isi form terakhir
        List<ViolationType> chosen = new ArrayList<>();
        for (Map.Entry<ViolationType, JCheckBox> en : form.getViolationBoxes().entrySet()) {
            if (en.getValue().isSelected()) chosen.add(en.getKey());
        }
        String plateText = form.getPlateField().getText().trim();
        Jalur jalur = (Jalur) form.getJalurBox().getSelectedItem();
        VehicleIdentity vehicle = matched;
        boolean noPlate = KendaraanDao.normalizePlate(plateText).isEmpty();

        String error = null;
        if (noPlate && !chosen.contains(ViolationType.NO_PLATE)) error = "Plat nomor belum diisi.";
        else if (!noPlate && chosen.contains(ViolationType.NO_PLATE)) error = "Plat nomor diisi, tetapi pelanggaran \"Tidak ada plat nomor\" dipilih.";
        else if (chosen.isEmpty()) error = "Pilih minimal satu pelanggaran.";
        else if (vehicle == null && noPlate) error = "Kendaraan tanpa plat belum teridentifikasi. Pilih jenis, merek, dan warna yang sesuai.";
        else if (vehicle == null) error = "Plat " + plateText.toUpperCase() + " tidak terdaftar di data kendaraan.";
        else if (jalur == null) error = "Jalur belum dipilih.";

        // Validasi kesesuaian jenis kendaraan dan pelanggaran
        else if (vehicle.getKind() == JenisKendaraan.MOBIL && chosen.contains(ViolationType.NO_HELMET)) {
            error = "Validasi Gagal: Kendaraan roda empat (Mobil) tidak diwajibkan menggunakan helm!";
        } else if (vehicle.getKind() == JenisKendaraan.MOBIL && chosen.contains(ViolationType.TRIPLE_RIDING)) {
            error = "Validasi Gagal: \"Bonceng 3+\" hanya berlaku untuk sepeda motor!";
        } else if (vehicle.getKind() == JenisKendaraan.SEPEDA_MOTOR && chosen.contains(ViolationType.NO_SEATBELT)) {
            error = "Validasi Gagal: Sepeda motor tidak memiliki sabuk pengaman!";
        }

        if (error != null) {
            JOptionPane.showMessageDialog(frame, error, "Peringatan Pelanggaran", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String plate = noPlate ? "-" : vehicle.getPlate(); // kendaraan memang tanpa plat

        try {
            service.add(new ViolationRecord(plate, chosen, LocalDateTime.now(), jalur, vehicle.getKind(), vehicle));
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Database", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        refreshViews();
        return true;
    }

    /** Menyegarkan Daftar Pelanggaran (halaman 2) dan kartu Total Pelanggaran (halaman 1). */
    private void refreshViews() {
        frame.getListPanel().setRecords(service.getAll());
        frame.getDashboardPanel().getTotalCard().setTotal(service.size());
    }
}
