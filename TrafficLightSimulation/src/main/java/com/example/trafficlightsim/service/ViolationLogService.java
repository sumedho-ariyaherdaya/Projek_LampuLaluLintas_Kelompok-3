package com.example.trafficlightsim.service;

import com.example.trafficlightsim.model.Jalur;
import com.example.trafficlightsim.model.JenisKendaraan;
import com.example.trafficlightsim.model.ViolationRecord;
import com.example.trafficlightsim.model.ViolationType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Menyimpan ViolationRecord di memori (hilang saat aplikasi ditutup). */
public class ViolationLogService {
    private final List<ViolationRecord> records = new ArrayList<>();

    // Konstruktor untuk mengisi 7 data awal (dummy) secara otomatis saat aplikasi dibuka
    public ViolationLogService() {
        records.add(new ViolationRecord(
            "BM 1234 AB", 
            List.of(ViolationType.NO_HELMET, ViolationType.SPEEDING), 
            LocalDateTime.of(2026, 11, 12, 8, 30, 15), 
            Jalur.UTARA, 
            JenisKendaraan.SEPEDA_MOTOR
        ));

        records.add(new ViolationRecord(
            "BM 5678 CD", 
            List.of(ViolationType.NO_PLATE), 
            LocalDateTime.of(2026, 11, 12, 9, 15, 40), 
            Jalur.BARAT, 
            JenisKendaraan.MOBIL
        ));

        records.add(new ViolationRecord(
            "BM 9999 EF", 
            List.of(ViolationType.TRIPLE_RIDING, ViolationType.NOISY_EXHAUST), 
            LocalDateTime.of(2026, 11, 12, 10, 5, 22), 
            Jalur.SELATAN, 
            JenisKendaraan.SEPEDA_MOTOR
        ));

        records.add(new ViolationRecord(
            "BM 2468 GH", 
            List.of(ViolationType.NO_SEATBELT), 
            LocalDateTime.of(2026, 11, 12, 10, 45, 10), 
            Jalur.TIMUR, 
            JenisKendaraan.MOBIL
        ));

        records.add(new ViolationRecord(
            "BM 1357 JK", 
            List.of(ViolationType.RED_LIGHT, ViolationType.NO_HELMET), 
            LocalDateTime.of(2026, 11, 12, 11, 20, 5), 
            Jalur.UTARA, 
            JenisKendaraan.SEPEDA_MOTOR
        ));

        records.add(new ViolationRecord(
            "BM 4321 LM", 
            List.of(ViolationType.SPEEDING), 
            LocalDateTime.of(2026, 11, 12, 11, 55, 30), 
            Jalur.BARAT, 
            JenisKendaraan.MOBIL
        ));

        records.add(new ViolationRecord(
            "BM 8765 NP", 
            List.of(ViolationType.NOISY_EXHAUST, ViolationType.NO_PLATE), 
            LocalDateTime.of(2026, 11, 12, 12, 10, 50), 
            Jalur.SELATAN, 
            JenisKendaraan.SEPEDA_MOTOR
        ));
    }

    public void add(ViolationRecord record) {
        // Gunakan indeks 0 agar data terbaru selalu berada di paling atas
        records.add(0, record); 
    }

    public List<ViolationRecord> getAll() { 
        return Collections.unmodifiableList(records); 
    }

    public int size() { 
        return records.size(); 
    }
}