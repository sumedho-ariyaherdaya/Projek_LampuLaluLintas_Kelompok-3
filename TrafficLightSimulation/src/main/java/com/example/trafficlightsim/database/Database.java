package com.example.trafficlightsim.database;

import com.example.trafficlightsim.model.VehicleIdentity;
import com.example.trafficlightsim.model.ViolationType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Koneksi SQLite lewat JDBC. File database dibuat otomatis di folder kerja:
 * data/lalulintas.db (saat dijalankan dari NetBeans, folder kerja = folder TrafficLightSimulation).
 * Tabel dibuat dan diisi data awal pada pemakaian pertama.
 *
 * Database lama dimigrasikan otomatis (lihat {@link #migrate}): kolom/tabel baru ditambahkan tanpa
 * menghapus data, dan file lama dicadangkan lebih dulu ke data/lalulintas-sebelum-migrasi.db.
 */
public final class Database {
    private static final String DB_FILE = "data/lalulintas.db";
    private static final String BACKUP_FILE = "data/lalulintas-sebelum-migrasi.db";
    private static final String URL = "jdbc:sqlite:" + DB_FILE;
    /** Versi skema (PRAGMA user_version). 0 = skema lama; 1 = email, status, ID log, dan riwayat. */
    private static final int SCHEMA_VERSION = 1;
    private static boolean ready;

    private Database() { }

    /** Membuka koneksi baru; tutup dengan try-with-resources. */
    public static Connection open() throws SQLException {
        init();
        return connect();
    }

    /** Memastikan database, tabel, dan data awal ada. Aman dipanggil berkali-kali. */
    public static synchronized void init() {
        if (ready) return;
        try {
            Files.createDirectories(Path.of("data"));
            try (Connection c = connect()) {
                backupIfNeeded(c); // sebelum ada perubahan apa pun pada database lama
                c.setAutoCommit(false);
                try (Statement s = c.createStatement()) {
                    s.execute("CREATE TABLE IF NOT EXISTS kendaraan ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "plat TEXT NOT NULL UNIQUE,"
                        + "pemilik TEXT NOT NULL,"
                        + "jenis TEXT NOT NULL CHECK (jenis IN ('MOBIL','SEPEDA_MOTOR')),"
                        + "warna TEXT NOT NULL,"
                        + "merek TEXT NOT NULL,"
                        + "UNIQUE (jenis, warna, merek))");
                    s.execute("CREATE TABLE IF NOT EXISTS jenis_pelanggaran ("
                        + "kode TEXT PRIMARY KEY,"
                        + "label TEXT NOT NULL,"
                        + "denda INTEGER NOT NULL)");
                    s.execute("CREATE TABLE IF NOT EXISTS pelanggaran ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "kendaraan_id INTEGER NOT NULL REFERENCES kendaraan(id),"
                        + "plat TEXT NOT NULL,"
                        + "waktu TEXT NOT NULL,"
                        + "jalur TEXT NOT NULL,"
                        + "jenis_kendaraan TEXT NOT NULL)");
                    s.execute("CREATE TABLE IF NOT EXISTS detail_pelanggaran ("
                        + "pelanggaran_id INTEGER NOT NULL REFERENCES pelanggaran(id) ON DELETE CASCADE,"
                        + "kode TEXT NOT NULL REFERENCES jenis_pelanggaran(kode),"
                        + "denda INTEGER NOT NULL,"
                        + "PRIMARY KEY (pelanggaran_id, kode))");
                }
                migrate(c);
                syncViolationTypes(c);
                seedVehicles(c);
                backfillEmails(c);
                c.commit();
            }
            ready = true;
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Database gagal disiapkan: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------------------------------
    // Migrasi skema
    // ------------------------------------------------------------------------------------------

    private static int userVersion(Connection c) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("PRAGMA user_version")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static boolean hasTable(Connection c, String table) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?")) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    private static boolean hasColumn(Connection c, String table, String column) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) if (column.equalsIgnoreCase(rs.getString("name"))) return true;
        }
        return false;
    }

    /** Menyalin file database lama (skema belum bermigrasi) sebelum diubah. Hanya sekali; tidak menimpa cadangan lama. */
    private static void backupIfNeeded(Connection c) throws SQLException, IOException {
        if (userVersion(c) >= SCHEMA_VERSION || !hasTable(c, "kendaraan")) return; // baru / sudah dimigrasi
        Path backup = Path.of(BACKUP_FILE);
        if (!Files.exists(backup)) Files.copy(Path.of(DB_FILE), backup, StandardCopyOption.COPY_ATTRIBUTES);
    }

    /**
     * Menaikkan skema lama ke versi terbaru. Semua langkah idempoten (dicek dulu sebelum dijalankan),
     * dan berjalan dalam satu transaksi bersama init(): bila gagal, database tetap seperti semula.
     */
    private static void migrate(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            if (!hasColumn(c, "kendaraan", "email"))
                s.execute("ALTER TABLE kendaraan ADD COLUMN email TEXT NOT NULL DEFAULT ''");
            if (!hasColumn(c, "pelanggaran", "kode_log"))
                s.execute("ALTER TABLE pelanggaran ADD COLUMN kode_log TEXT");
            if (!hasColumn(c, "pelanggaran", "status"))
                s.execute("ALTER TABLE pelanggaran ADD COLUMN status TEXT NOT NULL DEFAULT 'BELUM_DIKIRIM'");
            if (!hasColumn(c, "pelanggaran", "email_tujuan"))
                s.execute("ALTER TABLE pelanggaran ADD COLUMN email_tujuan TEXT NOT NULL DEFAULT ''");

            if (!hasColumn(c, "detail_pelanggaran", "keterangan"))
                s.execute("ALTER TABLE detail_pelanggaran ADD COLUMN keterangan TEXT NOT NULL DEFAULT ''");

            s.execute("CREATE TABLE IF NOT EXISTS riwayat_pelanggaran ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "pelanggaran_id INTEGER NOT NULL REFERENCES pelanggaran(id) ON DELETE CASCADE,"
                + "waktu TEXT NOT NULL,"
                + "aksi TEXT NOT NULL,"
                + "aktor TEXT NOT NULL,"
                + "email_tujuan TEXT NOT NULL DEFAULT '',"
                + "status_baru TEXT NOT NULL,"
                + "catatan TEXT NOT NULL DEFAULT '')");
            s.execute("CREATE INDEX IF NOT EXISTS idx_riwayat_pelanggaran ON riwayat_pelanggaran(pelanggaran_id)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_pelanggaran_status ON pelanggaran(status, email_tujuan)");

            // ID log: baris lama diberi kode dari id-nya. Id memakai AUTOINCREMENT, jadi tidak pernah dipakai ulang
            // meskipun baris dihapus; trigger di bawah mencegah kode yang sudah terisi diubah.
            s.execute("UPDATE pelanggaran SET kode_log = 'PLG-' || printf('%05d', id) WHERE kode_log IS NULL");
            s.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_pelanggaran_kode_log ON pelanggaran(kode_log)");
            s.execute("CREATE TRIGGER IF NOT EXISTS trg_kode_log_permanen BEFORE UPDATE OF kode_log ON pelanggaran "
                + "WHEN OLD.kode_log IS NOT NULL AND NEW.kode_log IS NOT OLD.kode_log "
                + "BEGIN SELECT RAISE(ABORT, 'ID log bersifat permanen dan tidak dapat diubah'); END");

            // Baris lama belum punya riwayat: beri satu catatan awal "dicatat" pada waktu pelanggaran.
            s.execute("INSERT INTO riwayat_pelanggaran (pelanggaran_id, waktu, aksi, aktor, status_baru, catatan) "
                + "SELECT p.id, p.waktu, 'DICATAT', 'Operator', 'BELUM_DIKIRIM', 'Data pelanggaran lama dimigrasikan' "
                + "FROM pelanggaran p WHERE NOT EXISTS (SELECT 1 FROM riwayat_pelanggaran r WHERE r.pelanggaran_id = p.id)");
            s.execute("PRAGMA user_version = " + SCHEMA_VERSION);
        }
    }

    /** Kendaraan yang belum punya email diberi alamat contoh dari nama pemilik (bisa diubah di halaman Database). */
    private static void backfillEmails(Connection c) throws SQLException {
        try (Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT id, pemilik FROM kendaraan WHERE email = ''");
             PreparedStatement up = c.prepareStatement("UPDATE kendaraan SET email = ? WHERE id = ?")) {
            while (rs.next()) {
                String local = rs.getString(2).toLowerCase().replaceAll("[^a-z0-9]+", ".").replaceAll("^\\.|\\.$", "");
                if (local.isEmpty()) local = "pemilik" + rs.getLong(1);
                up.setString(1, local + "@example.com");
                up.setLong(2, rs.getLong(1));
                up.addBatch();
            }
            up.executeBatch();
        }
    }

    private static Connection connect() throws SQLException {
        Connection c = DriverManager.getConnection(URL);
        try (Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON"); // SQLite mematikannya secara bawaan
        }
        return c;
    }

    /** Menyamakan tabel jenis_pelanggaran dengan enum ViolationType (label dan denda). */
    private static void syncViolationTypes(Connection c) throws SQLException {
        String sql = "INSERT INTO jenis_pelanggaran (kode, label, denda) VALUES (?, ?, ?) "
                + "ON CONFLICT(kode) DO UPDATE SET label = excluded.label, denda = excluded.denda";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (ViolationType t : ViolationType.values()) {
                ps.setString(1, t.name());
                ps.setString(2, t.getLabel());
                ps.setInt(3, t.getFine());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Mengisi 20 kendaraan awal, hanya jika tabel kendaraan masih kosong. */
    private static void seedVehicles(Connection c) throws SQLException {
        try (Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM kendaraan")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }
        String sql = "INSERT INTO kendaraan (plat, pemilik, jenis, warna, merek) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (VehicleIdentity v : VehicleSeed.all()) {
                ps.setString(1, v.getPlate());
                ps.setString(2, v.getOwner());
                ps.setString(3, v.getKind().name());
                ps.setString(4, v.getColor().name());
                ps.setString(5, v.getBrand());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}