package com.example.trafficlightsim.database;

import com.example.trafficlightsim.model.JenisKendaraan;
import com.example.trafficlightsim.model.VehicleIdentity;
import com.example.trafficlightsim.model.WarnaKendaraan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Membaca tabel kendaraan (data identitas kendaraan dan pemiliknya). */
public class KendaraanDao {

    /** "b 19-n" -> "B19N": plat dibandingkan tanpa spasi/tanda baca dan tanpa beda huruf besar. */
    public static String normalizePlate(String plate) {
        return plate == null ? "" : plate.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    public List<VehicleIdentity> findAll() {
        return query("SELECT plat, pemilik, jenis, warna, merek FROM kendaraan ORDER BY id");
    }

    /** Kendaraan dengan plat tersebut, atau null jika tidak terdaftar. */
    public VehicleIdentity findByPlate(String plate) {
        List<VehicleIdentity> r = query("SELECT plat, pemilik, jenis, warna, merek FROM kendaraan WHERE plat = ?",
                normalizePlate(plate));
        return r.isEmpty() ? null : r.get(0);
    }

    /** Kendaraan dengan jenis + warna + merek tersebut (unik), atau null. Dipakai untuk kendaraan tanpa plat. */
    public VehicleIdentity findByTraits(JenisKendaraan kind, WarnaKendaraan color, String brand) {
        List<VehicleIdentity> r = query("SELECT plat, pemilik, jenis, warna, merek FROM kendaraan "
                + "WHERE jenis = ? AND warna = ? AND merek = ? COLLATE NOCASE",
                kind.name(), color.name(), brand);
        return r.isEmpty() ? null : r.get(0);
    }

    /** Daftar merek untuk pilihan form; kind == null berarti semua merek. */
    public List<String> brands(JenisKendaraan kind) {
        List<String> out = new ArrayList<>();
        String sql = "SELECT DISTINCT merek FROM kendaraan" + (kind == null ? "" : " WHERE jenis = ?") + " ORDER BY merek COLLATE NOCASE";
        try (Connection c = Database.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (kind != null) ps.setString(1, kind.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal membaca merek: " + e.getMessage(), e);
        }
        return out;
    }

    static VehicleIdentity read(ResultSet rs, int offset) throws SQLException {
        return new VehicleIdentity(rs.getString(offset), rs.getString(offset + 1),
                JenisKendaraan.valueOf(rs.getString(offset + 2)),
                WarnaKendaraan.valueOf(rs.getString(offset + 3)), rs.getString(offset + 4));
    }

    private List<VehicleIdentity> query(String sql, String... args) {
        List<VehicleIdentity> out = new ArrayList<>();
        try (Connection c = Database.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) ps.setString(i + 1, args[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(read(rs, 1));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal membaca data kendaraan: " + e.getMessage(), e);
        }
        return out;
    }
}
