package com.example.trafficlightsim.database;

import com.example.trafficlightsim.model.VehicleIdentity;
import com.example.trafficlightsim.model.ViolationType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
 */
public final class Database {
    private static final String URL = "jdbc:sqlite:data/lalulintas.db";
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
                syncViolationTypes(c);
                seedVehicles(c);
                c.commit();
            }
            ready = true;
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Database gagal disiapkan: " + e.getMessage(), e);
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
