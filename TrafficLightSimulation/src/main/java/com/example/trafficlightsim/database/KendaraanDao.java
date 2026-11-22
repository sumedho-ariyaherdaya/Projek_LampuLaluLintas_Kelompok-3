package com.example.trafficlightsim.database;

import com.example.trafficlightsim.model.JenisKendaraan;
import com.example.trafficlightsim.model.VehicleIdentity;
import com.example.trafficlightsim.model.WarnaKendaraan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Membaca dan mengelola (CRUD) tabel kendaraan: identitas kendaraan, pemilik, dan email. */
public class KendaraanDao {
    private static final String COLS = "id, plat, pemilik, jenis, warna, merek, email";

    /** "b 19-n" -> "B19N": plat dibandingkan tanpa spasi/tanda baca dan tanpa beda huruf besar. */
    public static String normalizePlate(String plate) {
        return plate == null ? "" : plate.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    public List<VehicleIdentity> findAll() {
        return query("SELECT " + COLS + " FROM kendaraan ORDER BY id");
    }

    /** Kendaraan dengan plat tersebut, atau null jika tidak terdaftar. */
    public VehicleIdentity findByPlate(String plate) {
        List<VehicleIdentity> r = query("SELECT " + COLS + " FROM kendaraan WHERE plat = ?",
                normalizePlate(plate));
        return r.isEmpty() ? null : r.get(0);
    }

    /** Kendaraan dengan jenis + warna + merek tersebut (unik), atau null. Dipakai untuk kendaraan tanpa plat. */
    public VehicleIdentity findByTraits(JenisKendaraan kind, WarnaKendaraan color, String brand) {
        List<VehicleIdentity> r = query("SELECT " + COLS + " FROM kendaraan "
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

    /** Kendaraan dengan id tersebut, atau null. */
    public VehicleIdentity findById(long id) {
        List<VehicleIdentity> r = query("SELECT " + COLS + " FROM kendaraan WHERE id = ?", String.valueOf(id));
        return r.isEmpty() ? null : r.get(0);
    }

    /** Kendaraan lain (bukan excludeId) dengan plat yang sama (setelah dinormalisasi), atau null. */
    public VehicleIdentity findOtherByPlate(String plate, long excludeId) {
        List<VehicleIdentity> r = query("SELECT " + COLS + " FROM kendaraan WHERE plat = ? AND id <> ?",
                normalizePlate(plate), String.valueOf(excludeId));
        return r.isEmpty() ? null : r.get(0);
    }

    /** Kendaraan lain (bukan excludeId) dengan jenis + warna + merek yang sama (merek tak peduli huruf besar/kecil), atau null. */
    public VehicleIdentity findOtherByTraits(JenisKendaraan kind, WarnaKendaraan color, String brand, long excludeId) {
        List<VehicleIdentity> r = query("SELECT " + COLS + " FROM kendaraan "
                + "WHERE jenis = ? AND warna = ? AND merek = ? COLLATE NOCASE AND id <> ?",
                kind.name(), color.name(), brand, String.valueOf(excludeId));
        return r.isEmpty() ? null : r.get(0);
    }

    /** Jumlah catatan pelanggaran yang terhubung ke kendaraan ini. */
    public int countViolations(long vehicleId) {
        try (Connection c = Database.open();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM pelanggaran WHERE kendaraan_id = ?")) {
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal membaca riwayat pelanggaran: " + e.getMessage(), e);
        }
    }

    /** Menambah kendaraan; mengembalikan id baru. Plat disimpan dalam bentuk normal (tanpa spasi, huruf besar). */
    public long insert(VehicleIdentity v) {
        String sql = "INSERT INTO kendaraan (plat, pemilik, jenis, warna, merek, email) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = Database.open(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, v);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menambah kendaraan: " + e.getMessage(), e);
        }
    }

    /** Mengubah semua isian kendaraan dengan id v.getId(). */
    public void update(VehicleIdentity v) {
        String sql = "UPDATE kendaraan SET plat = ?, pemilik = ?, jenis = ?, warna = ?, merek = ?, email = ? WHERE id = ?";
        try (Connection c = Database.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, v);
            ps.setLong(7, v.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal mengubah kendaraan: " + e.getMessage(), e);
        }
    }

    /** Menghapus kendaraan. Pemanggil wajib memastikan tidak ada riwayat pelanggaran (database juga menolaknya). */
    public void delete(long id) {
        try (Connection c = Database.open(); PreparedStatement ps = c.prepareStatement("DELETE FROM kendaraan WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menghapus kendaraan: " + e.getMessage(), e);
        }
    }

    private static void bind(PreparedStatement ps, VehicleIdentity v) throws SQLException {
        ps.setString(1, normalizePlate(v.getPlate()));
        ps.setString(2, v.getOwner());
        ps.setString(3, v.getKind().name());
        ps.setString(4, v.getColor().name());
        ps.setString(5, v.getBrand());
        ps.setString(6, v.getEmail());
    }

    static VehicleIdentity read(ResultSet rs, int offset) throws SQLException {
        return new VehicleIdentity(rs.getLong(offset), rs.getString(offset + 1), rs.getString(offset + 2),
                JenisKendaraan.valueOf(rs.getString(offset + 3)),
                WarnaKendaraan.valueOf(rs.getString(offset + 4)), rs.getString(offset + 5), rs.getString(offset + 6));
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