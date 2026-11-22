package com.example.trafficlightsim.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** Satu baris catatan pelanggaran (baris di tabel Daftar Pelanggaran dan isi Bukti Pelanggaran). */
public final class ViolationRecord {
    private final String plate;
    private final List<ViolationType> violations;
    private final LocalDateTime time;
    private final Jalur jalur;
    private final JenisKendaraan vehicleKind;
    private final VehicleIdentity vehicle; // kendaraan terdaftar (pemilik, merek, warna); null untuk data lama
    private final long id;                 // id baris di database; 0 jika belum tersimpan
    private final String kodeLog;          // ID unik permanen, mis. PLG-00012; "" jika belum tersimpan
    private final StatusPelanggaran status;
    private final String emailTujuan;      // email tujuan saat log dikirim; "" jika belum pernah dikirim
    private final String otherNote;        // keterangan "Pelanggaran lainnya"; "" jika tidak dipilih

    public ViolationRecord(String plate, List<ViolationType> violations, LocalDateTime time,
                           Jalur jalur, JenisKendaraan vehicleKind) {
        this(plate, violations, time, jalur, vehicleKind, null);
    }

    public ViolationRecord(String plate, List<ViolationType> violations, LocalDateTime time,
                           Jalur jalur, JenisKendaraan vehicleKind, VehicleIdentity vehicle) {
        this(plate, violations, time, jalur, vehicleKind, vehicle, 0, "", StatusPelanggaran.BELUM_DIKIRIM, "");
    }

    /** Konstruktor lengkap, dipakai saat membaca dari database. */
    public ViolationRecord(String plate, List<ViolationType> violations, LocalDateTime time,
                           Jalur jalur, JenisKendaraan vehicleKind, VehicleIdentity vehicle,
                           long id, String kodeLog, StatusPelanggaran status, String emailTujuan) {
        this(plate, violations, time, jalur, vehicleKind, vehicle, id, kodeLog, status, emailTujuan, "");
    }

    /** Konstruktor lengkap dengan keterangan "Pelanggaran lainnya". */
    public ViolationRecord(String plate, List<ViolationType> violations, LocalDateTime time,
                           Jalur jalur, JenisKendaraan vehicleKind, VehicleIdentity vehicle,
                           long id, String kodeLog, StatusPelanggaran status, String emailTujuan, String otherNote) {
        this.otherNote = otherNote == null ? "" : otherNote.trim();
        this.id = id;
        this.kodeLog = kodeLog == null ? "" : kodeLog;
        this.status = status == null ? StatusPelanggaran.BELUM_DIKIRIM : status;
        this.emailTujuan = emailTujuan == null ? "" : emailTujuan;
        this.vehicle = vehicle;
        this.plate = plate;
        this.violations = List.copyOf(violations);
        this.time = time;
        this.jalur = jalur;
        this.vehicleKind = vehicleKind;
    }

    public String getPlate() { return plate; }
    public List<ViolationType> getViolations() { return violations; }
    public LocalDateTime getTime() { return time; }
    public Jalur getJalur() { return jalur; }
    public JenisKendaraan getVehicleKind() { return vehicleKind; }
    public VehicleIdentity getVehicle() { return vehicle; }
    public String getOwnerName() { return vehicle == null ? "" : vehicle.getOwner(); }
    public long getId() { return id; }
    public String getKodeLog() { return kodeLog; }
    public StatusPelanggaran getStatus() { return status; }
    public String getEmailTujuan() { return emailTujuan; }
    /** Email pemilik kendaraan saat ini ("" jika belum ada). */
    public String getVehicleEmail() { return vehicle == null ? "" : vehicle.getEmail(); }

    /** Total denda = jumlah denda semua pelanggaran pada catatan ini. */
    public int getTotalFine() { return violations.stream().mapToInt(ViolationType::getFine).sum(); }

    public String getOtherNote() { return otherNote; }

    /** Plat untuk ditampilkan di log/cetak: plat kendaraan terdaftar, juga bila saat kejadian tanpa plat (tersimpan "-"). */
    public String getDisplayPlate() {
        boolean none = plate == null || plate.isBlank() || "-".equals(plate.trim());
        return none && vehicle != null ? vehicle.getPlate() : plate;
    }

    /** Label pelanggaran; untuk "Pelanggaran lainnya" ditambah keterangannya. */
    public String describe(ViolationType t) {
        return t == ViolationType.OTHER && !otherNote.isEmpty() ? t.getLabel() + ": " + otherNote : t.getLabel();
    }

    public String getViolationSummary() {
        return violations.stream().map(this::describe).collect(Collectors.joining(", "));
    }
}