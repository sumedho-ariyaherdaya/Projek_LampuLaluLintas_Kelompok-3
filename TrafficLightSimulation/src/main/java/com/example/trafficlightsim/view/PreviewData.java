package com.example.trafficlightsim.view;

import com.example.trafficlightsim.model.*;
import com.example.trafficlightsim.model.ViolationType;
import java.time.LocalDateTime;
import java.util.List;

/** Data contoh dari mockup, hanya untuk melihat tampilan. TODO: hapus setelah ViolationLogService jadi. */
public final class PreviewData {
    private PreviewData() { }

    public static List<ViolationRecord> records() {
        LocalDateTime t = LocalDateTime.of(2026, 11, 5, 9, 10, 25);
        return List.of(
            new ViolationRecord("PQ 5794 RIZZ", List.of(ViolationType.NO_HELMET), t, Jalur.BARAT, JenisKendaraan.SEPEDA_MOTOR),
            new ViolationRecord("SP 0078 GB", List.of(ViolationType.SPEEDING), t, Jalur.BARAT, JenisKendaraan.MOBIL),
            new ViolationRecord("KEL 3 PBO", List.of(ViolationType.RED_LIGHT), t, Jalur.TIMUR, JenisKendaraan.MOBIL),
            new ViolationRecord("COOL 2025 TIA", List.of(ViolationType.NO_PLATE), t, Jalur.UTARA, JenisKendaraan.MOBIL),
            new ViolationRecord("BM 2507 SA", List.of(ViolationType.TRIPLE_RIDING), t, Jalur.SELATAN, JenisKendaraan.SEPEDA_MOTOR),
            new ViolationRecord("B 5126 AA", List.of(ViolationType.NO_SEATBELT), t, Jalur.UTARA, JenisKendaraan.MOBIL));
    }
}
