package com.example.trafficlightsim.service;

import com.example.trafficlightsim.database.Database;
import com.example.trafficlightsim.model.RiwayatLog;
import com.example.trafficlightsim.model.RiwayatLog.Aksi;
import com.example.trafficlightsim.model.StatusPelanggaran;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Alur penanganan log pelanggaran: kirim email (simulasi), tanggapan penerima (diterima, konfirmasi,
 * sanggahan), dan peninjauan sanggahan oleh operator. Setiap perubahan status dan riwayatnya disimpan
 * dalam satu transaksi; perpindahan status yang tidak sah ditolak dengan pesan yang jelas.
 */
public class ViolationWorkflowService {
    public static final String OPERATOR = "Operator";
    public static final int MIN_ALASAN = 10; // panjang minimal alasan sanggahan

    public ViolationWorkflowService() { Database.init(); }

    /**
     * Mencatat pengiriman log ke email pemilik kendaraan. Boleh dikirim ulang selama belum ada tanggapan
     * (status Belum Dikirim atau Terkirim). Email diambil dari data kendaraan saat ini lalu disimpan pada log.
     * @return alamat email tujuan.
     */
    public String kirimEmail(long id) {
        String email;
        try (Connection c = Database.open()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT k.email FROM pelanggaran p JOIN kendaraan k ON k.id = p.kendaraan_id WHERE p.id = ?")) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw hilang();
                    email = rs.getString(1).trim();
                }
            }
        } catch (SQLException e) {
            throw dbError("membaca email kendaraan", e);
        }
        if (!KendaraanService.isValidEmail(email))
            throw new IllegalArgumentException("Email kendaraan belum diisi atau tidak valid.\n"
                    + "Lengkapi dulu di halaman Database, lalu kirim ulang.");
        StatusPelanggaran now = statusOf(id);
        Aksi aksi = now == StatusPelanggaran.TERKIRIM ? Aksi.DIKIRIM_ULANG : Aksi.DIKIRIM;
        transition(id, EnumSet.of(StatusPelanggaran.BELUM_DIKIRIM, StatusPelanggaran.TERKIRIM),
                StatusPelanggaran.TERKIRIM, aksi, OPERATOR, email, "Dikirim ke " + email, email);
        return email;
    }

    /** Penerima membuka email di Kotak Masuk: Terkirim -> Diterima. Tidak berbuat apa-apa jika sudah lewat tahap itu. */
    public void tandaiDiterima(long id) {
        if (statusOf(id) != StatusPelanggaran.TERKIRIM) return;
        String email = emailTujuan(id);
        transition(id, EnumSet.of(StatusPelanggaran.TERKIRIM), StatusPelanggaran.DITERIMA,
                Aksi.DIBUKA, email, email, "Email dibuka oleh penerima", null);
    }

    /** Penerima membenarkan pelanggaran: Diterima -> Selesai. */
    public void konfirmasi(long id) {
        String email = emailTujuan(id);
        transition(id, EnumSet.of(StatusPelanggaran.DITERIMA), StatusPelanggaran.SELESAI,
                Aksi.DIKONFIRMASI, email, email, "Penerima mengonfirmasi pelanggaran", null);
    }

    /** Penerima menyanggah pelanggaran dengan alasan: Diterima -> Disanggah. */
    public void sanggah(long id, String alasan) {
        String text = alasan == null ? "" : alasan.trim();
        if (text.length() < MIN_ALASAN)
            throw new IllegalArgumentException("Alasan sanggahan minimal " + MIN_ALASAN + " karakter.");
        String email = emailTujuan(id);
        transition(id, EnumSet.of(StatusPelanggaran.DITERIMA), StatusPelanggaran.DISANGGAH,
                Aksi.DISANGGAH, email, email, text, null);
    }

    /** Operator meninjau sanggahan: Disanggah -> Selesai, dengan keputusan diterima atau ditolak. */
    public void tinjau(long id, boolean terima, String catatan) {
        String text = catatan == null ? "" : catatan.trim();
        if (!terima && text.isEmpty())
            throw new IllegalArgumentException("Alasan penolakan wajib diisi agar penerima tahu sebabnya.");
        transition(id, EnumSet.of(StatusPelanggaran.DISANGGAH), StatusPelanggaran.SELESAI,
                terima ? Aksi.SANGGAHAN_DITERIMA : Aksi.SANGGAHAN_DITOLAK, OPERATOR, emailTujuan(id),
                text.isEmpty() ? "-" : text, null);
    }

    /** Riwayat satu log, dari yang paling awal. */
    public List<RiwayatLog> riwayat(long id) {
        List<RiwayatLog> out = new ArrayList<>();
        try (Connection c = Database.open();
             PreparedStatement ps = c.prepareStatement("SELECT id, waktu, aksi, aktor, email_tujuan, status_baru, catatan "
                     + "FROM riwayat_pelanggaran WHERE pelanggaran_id = ? ORDER BY id")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new RiwayatLog(rs.getLong(1), LocalDateTime.parse(rs.getString(2)),
                            Aksi.valueOf(rs.getString(3)), rs.getString(4), rs.getString(5),
                            StatusPelanggaran.fromCode(rs.getString(6)), rs.getString(7)));
                }
            }
        } catch (SQLException e) {
            throw dbError("membaca riwayat", e);
        }
        return out;
    }

    // ------------------------------------------------------------------------------------------

    private StatusPelanggaran statusOf(long id) {
        try (Connection c = Database.open();
             PreparedStatement ps = c.prepareStatement("SELECT status FROM pelanggaran WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw hilang();
                return StatusPelanggaran.fromCode(rs.getString(1));
            }
        } catch (SQLException e) {
            throw dbError("membaca status", e);
        }
    }

    private String emailTujuan(long id) {
        try (Connection c = Database.open();
             PreparedStatement ps = c.prepareStatement("SELECT email_tujuan FROM pelanggaran WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw hilang();
                return rs.getString(1);
            }
        } catch (SQLException e) {
            throw dbError("membaca email tujuan", e);
        }
    }

    /**
     * Mengubah status dan menambah baris riwayat dalam satu transaksi. Perubahan hanya terjadi jika status
     * saat ini termasuk dalam 'from' (UPDATE bersyarat), sehingga dua aksi yang bersamaan tidak saling menimpa.
     * @param newEmail jika tidak null, disimpan sebagai email_tujuan log.
     */
    private void transition(long id, Set<StatusPelanggaran> from, StatusPelanggaran to, Aksi aksi,
                            String aktor, String riwayatEmail, String catatan, String newEmail) {
        try (Connection c = Database.open()) {
            c.setAutoCommit(false);
            try {
                StatusPelanggaran current;
                try (PreparedStatement ps = c.prepareStatement("SELECT status FROM pelanggaran WHERE id = ?")) {
                    ps.setLong(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw hilang();
                        current = StatusPelanggaran.fromCode(rs.getString(1));
                    }
                }
                if (!from.contains(current))
                    throw new IllegalArgumentException("Aksi \"" + aksi.getLabel() + "\" tidak dapat dilakukan: status log saat ini "
                            + current.getLabel() + ".");
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE pelanggaran SET status = ?, email_tujuan = COALESCE(?, email_tujuan) WHERE id = ? AND status = ?")) {
                    ps.setString(1, to.name());
                    ps.setString(2, newEmail);
                    ps.setLong(3, id);
                    ps.setString(4, current.name());
                    if (ps.executeUpdate() != 1)
                        throw new IllegalArgumentException("Status log baru saja berubah. Muat ulang lalu coba lagi.");
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO riwayat_pelanggaran (pelanggaran_id, waktu, aksi, aktor, email_tujuan, status_baru, catatan) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                    ps.setLong(1, id);
                    ps.setString(2, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString());
                    ps.setString(3, aksi.name());
                    ps.setString(4, aktor);
                    ps.setString(5, riwayatEmail == null ? "" : riwayatEmail);
                    ps.setString(6, to.name());
                    ps.setString(7, catatan == null ? "" : catatan);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw dbError("menyimpan perubahan status", e);
        }
    }

    private static IllegalArgumentException hilang() {
        return new IllegalArgumentException("Log pelanggaran tidak ditemukan (mungkin sudah dihapus).");
    }

    private static IllegalStateException dbError(String what, SQLException e) {
        return new IllegalStateException("Gagal " + what + ": " + e.getMessage(), e);
    }
}