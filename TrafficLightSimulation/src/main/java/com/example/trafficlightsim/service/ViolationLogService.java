package com.example.trafficlightsim.service;

import com.example.trafficlightsim.database.Database;
import com.example.trafficlightsim.model.Jalur;
import com.example.trafficlightsim.model.JenisKendaraan;
import com.example.trafficlightsim.model.VehicleIdentity;
import com.example.trafficlightsim.model.ViolationRecord;
import com.example.trafficlightsim.model.ViolationType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Menyimpan ViolationRecord di database SQLite (tabel pelanggaran dan detail_pelanggaran).
 * API publik sama seperti versi memori: add, getAll, size. Data bertahan setelah aplikasi ditutup.
 */
public class ViolationLogService {

    public ViolationLogService() { Database.init(); }

    /** Menyimpan satu catatan. record.getVehicle() wajib terisi (kendaraan terdaftar). */
    public void add(ViolationRecord r) {
        if (r.getVehicle() == null) throw new IllegalArgumentException("Pelanggaran harus terhubung ke kendaraan terdaftar.");
        try (Connection c = Database.open()) {
            c.setAutoCommit(false);
            try {
                long id;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO pelanggaran (kendaraan_id, plat, waktu, jalur, jenis_kendaraan) "
                        + "VALUES ((SELECT id FROM kendaraan WHERE plat = ?), ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, r.getVehicle().getPlate());
                    ps.setString(2, r.getPlate());
                    ps.setString(3, r.getTime().toString());
                    ps.setString(4, r.getJalur().name());
                    ps.setString(5, r.getVehicleKind().name());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        id = keys.getLong(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO detail_pelanggaran (pelanggaran_id, kode, denda) VALUES (?, ?, ?)")) {
                    for (ViolationType t : r.getViolations()) {
                        ps.setLong(1, id);
                        ps.setString(2, t.name());
                        ps.setInt(3, t.getFine());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menyimpan pelanggaran: " + e.getMessage(), e);
        }
    }

    /** Semua catatan, yang terbaru di paling atas. */
    public List<ViolationRecord> getAll() {
        List<ViolationRecord> out = new ArrayList<>();
        try (Connection c = Database.open()) {
            Map<Long, List<ViolationType>> details = new HashMap<>();
            try (Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery("SELECT pelanggaran_id, kode FROM detail_pelanggaran ORDER BY rowid")) {
                while (rs.next()) {
                    details.computeIfAbsent(rs.getLong(1), k -> new ArrayList<>())
                           .add(ViolationType.valueOf(rs.getString(2)));
                }
            }
            String sql = "SELECT p.id, p.plat, p.waktu, p.jalur, p.jenis_kendaraan, "
                    + "k.plat, k.pemilik, k.jenis, k.warna, k.merek "
                    + "FROM pelanggaran p JOIN kendaraan k ON k.id = p.kendaraan_id "
                    + "ORDER BY p.waktu DESC, p.id DESC";
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
                while (rs.next()) {
                    VehicleIdentity v = new VehicleIdentity(rs.getString(6), rs.getString(7),
                            JenisKendaraan.valueOf(rs.getString(8)),
                            com.example.trafficlightsim.model.WarnaKendaraan.valueOf(rs.getString(9)),
                            rs.getString(10));
                    out.add(new ViolationRecord(rs.getString(2),
                            details.getOrDefault(rs.getLong(1), List.of()),
                            LocalDateTime.parse(rs.getString(3)),
                            Jalur.valueOf(rs.getString(4)),
                            JenisKendaraan.valueOf(rs.getString(5)), v));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal membaca pelanggaran: " + e.getMessage(), e);
        }
        return out;
    }

    public int size() {
        try (Connection c = Database.open();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM pelanggaran")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menghitung pelanggaran: " + e.getMessage(), e);
        }
    }
}
