package com.example.trafficlightsim.service;

import com.example.trafficlightsim.database.Database;
import com.example.trafficlightsim.model.Jalur;
import com.example.trafficlightsim.model.JenisKendaraan;
import com.example.trafficlightsim.model.RiwayatLog;
import com.example.trafficlightsim.model.StatusPelanggaran;
import com.example.trafficlightsim.model.VehicleIdentity;
import com.example.trafficlightsim.model.ViolationRecord;
import com.example.trafficlightsim.model.ViolationType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
                        "INSERT INTO detail_pelanggaran (pelanggaran_id, kode, denda, keterangan) VALUES (?, ?, ?, ?)")) {
                    for (ViolationType t : r.getViolations()) {
                        ps.setLong(1, id);
                        ps.setString(2, t.name());
                        ps.setInt(3, t.getFine());
                        ps.setString(4, t == ViolationType.OTHER ? r.getOtherNote() : "");
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                // ID unik permanen: diturunkan dari id AUTOINCREMENT (tidak pernah dipakai ulang), lalu dikunci trigger.
                try (PreparedStatement ps = c.prepareStatement("UPDATE pelanggaran SET kode_log = ? WHERE id = ?")) {
                    ps.setString(1, kodeLog(id));
                    ps.setLong(2, id);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO riwayat_pelanggaran (pelanggaran_id, waktu, aksi, aktor, status_baru, catatan) "
                        + "VALUES (?, ?, ?, ?, ?, ?)")) {
                    ps.setLong(1, id);
                    ps.setString(2, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString());
                    ps.setString(3, RiwayatLog.Aksi.DICATAT.name());
                    ps.setString(4, "Operator");
                    ps.setString(5, StatusPelanggaran.BELUM_DIKIRIM.name());
                    ps.setString(6, "");
                    ps.executeUpdate();
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

    /** Format ID log permanen, mis. PLG-00012 (sama dengan yang dipakai migrasi di Database). */
    public static String kodeLog(long id) { return String.format("PLG-%05d", id); }

    /** Semua catatan, yang terbaru di paling atas. */
    public List<ViolationRecord> getAll() { return query("", null); }

    /** Satu catatan berdasarkan id database, atau null jika tidak ada (mis. sudah dihapus). */
    public ViolationRecord findById(long id) {
        List<ViolationRecord> r = query("WHERE p.id = ?", id);
        return r.isEmpty() ? null : r.get(0);
    }

    /** Catatan yang pernah dikirim ke email tertentu (isi Kotak Masuk penerima), terbaru di atas. */
    public List<ViolationRecord> getInbox(String email) {
        return queryText("WHERE p.status <> 'BELUM_DIKIRIM' AND p.email_tujuan = ? COLLATE NOCASE", email);
    }

    /** Catatan dengan status tertentu, terbaru di atas. */
    public List<ViolationRecord> getByStatus(StatusPelanggaran status) {
        return queryText("WHERE p.status = ?", status.name());
    }

    /** Alamat email yang pernah menerima log (untuk pilihan Kotak Masuk), urut abjad. */
    public List<String> getRecipients() {
        List<String> out = new ArrayList<>();
        try (Connection c = Database.open(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT DISTINCT email_tujuan FROM pelanggaran "
                     + "WHERE status <> 'BELUM_DIKIRIM' AND email_tujuan <> '' ORDER BY email_tujuan COLLATE NOCASE")) {
            while (rs.next()) out.add(rs.getString(1));
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal membaca penerima: " + e.getMessage(), e);
        }
        return out;
    }

    /**
     * Menghapus satu catatan beserta rincian dan riwayatnya (ON DELETE CASCADE).
     * ID log yang sudah dihapus tidak akan dipakai ulang.
     * @return true jika ada baris yang terhapus.
     */
    public boolean delete(long id) {
        try (Connection c = Database.open(); PreparedStatement ps = c.prepareStatement("DELETE FROM pelanggaran WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal menghapus pelanggaran: " + e.getMessage(), e);
        }
    }

    private List<ViolationRecord> query(String where, Long arg) {
        return run(where, ps -> { if (arg != null) ps.setLong(1, arg); });
    }

    private List<ViolationRecord> queryText(String where, String arg) {
        return run(where, ps -> ps.setString(1, arg));
    }

    private interface Binder { void bind(PreparedStatement ps) throws SQLException; }

    private List<ViolationRecord> run(String where, Binder binder) {
        List<ViolationRecord> out = new ArrayList<>();
        try (Connection c = Database.open()) {
            Map<Long, List<ViolationType>> details = new HashMap<>();
            Map<Long, String> notes = new HashMap<>();
            try (Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery("SELECT pelanggaran_id, kode, keterangan FROM detail_pelanggaran ORDER BY rowid")) {
                while (rs.next()) {
                    details.computeIfAbsent(rs.getLong(1), k -> new ArrayList<>())
                           .add(ViolationType.valueOf(rs.getString(2)));
                    if (ViolationType.OTHER.name().equals(rs.getString(2))) notes.put(rs.getLong(1), rs.getString(3));
                }
            }
            String sql = "SELECT p.id, p.plat, p.waktu, p.jalur, p.jenis_kendaraan, "
                    + "k.id, k.plat, k.pemilik, k.jenis, k.warna, k.merek, k.email, "
                    + "p.kode_log, p.status, p.email_tujuan "
                    + "FROM pelanggaran p JOIN kendaraan k ON k.id = p.kendaraan_id " + where
                    + " ORDER BY p.waktu DESC, p.id DESC";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                binder.bind(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        VehicleIdentity v = new VehicleIdentity(rs.getLong(6), rs.getString(7), rs.getString(8),
                                JenisKendaraan.valueOf(rs.getString(9)),
                                com.example.trafficlightsim.model.WarnaKendaraan.valueOf(rs.getString(10)),
                                rs.getString(11), rs.getString(12));
                        long id = rs.getLong(1);
                        out.add(new ViolationRecord(rs.getString(2),
                                details.getOrDefault(id, List.of()),
                                LocalDateTime.parse(rs.getString(3)),
                                Jalur.valueOf(rs.getString(4)),
                                JenisKendaraan.valueOf(rs.getString(5)), v,
                                id, rs.getString(13), StatusPelanggaran.fromCode(rs.getString(14)), rs.getString(15),
                                notes.getOrDefault(id, "")));
                    }
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