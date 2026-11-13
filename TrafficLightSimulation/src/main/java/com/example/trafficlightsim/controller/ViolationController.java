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
    @Override public void insertUpdate(DocumentEvent e) { if (!updating) identify(); }
    @Override public void removeUpdate(DocumentEvent e) { if (!updating) identify(); }
    @Override public void changedUpdate(DocumentEvent e) { if (!updating) identify(); }
});
// Mode input berganti saat pelanggaran "Tidak ada plat nomor" dicentang / dilepas.
       form.getViolationBoxes().get(ViolationType.NO_PLATE).addActionListener(e -> applyPlateMode());
        
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
 * Mode normal: plat selesai diketik dan terdaftar -> jenis/merek/warna terisi otomatis (terkunci).
 * Mode tanpa plat: jenis + merek + warna terisi -> plat muncul otomatis (terkunci).
 */
    private void identify() {
        VehicleIdentity found;
        String status;
        if (isNoPlateSelected()) {
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
            setPlateText(found != null ? found.getPlate() : "");
        } else {
            String plate = KendaraanDao.normalizePlate(form.getPlateField().getText());
            if (plate.isEmpty()) {
                found = null;
                status = "-";
                clearTraits();
            } else {
                found = kendaraanDao.findByPlate(plate);
                if (found != null) {
                    fillTraits(found);
                    status = found.getOwner();
                } else {
                    clearTraits();
                    status = "Plat tidak terdaftar";
                }
            }
        }
        matched = found;
        form.getOwnerField().setText(status);
    }
    
    private boolean isNoPlateSelected() {
    return form.getViolationBoxes().get(ViolationType.NO_PLATE).isSelected();
}
    /** Berganti antara mode normal dan mode tanpa plat; isian identitas dikosongkan agar tidak tercampur. */
    private void applyPlateMode() {
        boolean noPlate = isNoPlateSelected();
        updating = true;
        form.getPlateField().setText("");
        form.getKindBox().setSelectedIndex(-1);
        form.getColorBox().setSelectedIndex(-1);
        form.setBrands(kendaraanDao.brands(null));
        form.getOwnerField().setText("-");
        form.setNoPlateMode(noPlate);
        updating = false;
        matched = null;
        if (noPlate) form.getKindBox().requestFocusInWindow();
        else form.getPlateField().requestFocusInWindow();
    }

    private void clearTraits() {
        if (form.getKindBox().getSelectedItem() == null && form.getBrandBox().getSelectedItem() == null
                && form.getColorBox().getSelectedItem() == null) return;
        boolean prev = updating;
        updating = true;
        form.getKindBox().setSelectedIndex(-1);
        form.setBrands(kendaraanDao.brands(null));
        form.getColorBox().setSelectedIndex(-1);
        updating = prev;
    }

    private void setPlateText(String text) {
        if (form.getPlateField().getText().equals(text)) return;
        boolean prev = updating;
        updating = true;
        form.getPlateField().setText(text);
        updating = prev;
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
        boolean noPlate = chosen.contains(ViolationType.NO_PLATE);

        String error = null;
        if (!noPlate && plateText.isEmpty()) error = "Plat nomor belum diisi.";
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
