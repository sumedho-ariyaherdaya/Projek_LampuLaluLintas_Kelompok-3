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

    public ViolationRecord(String plate, List<ViolationType> violations, LocalDateTime time,
                           Jalur jalur, JenisKendaraan vehicleKind) {
        this(plate, violations, time, jalur, vehicleKind, null);
    }

    public ViolationRecord(String plate, List<ViolationType> violations, LocalDateTime time,
                           Jalur jalur, JenisKendaraan vehicleKind, VehicleIdentity vehicle) {
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

    /** Total denda = jumlah denda semua pelanggaran pada catatan ini. */
    public int getTotalFine() { return violations.stream().mapToInt(ViolationType::getFine).sum(); }

    public String getViolationSummary() {
        return violations.stream().map(ViolationType::getLabel).collect(Collectors.joining(", "));
    }
}
