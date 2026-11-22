package com.example.trafficlightsim.service;

import com.example.trafficlightsim.database.KendaraanDao;
import com.example.trafficlightsim.model.VehicleIdentity;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Aturan bisnis untuk halaman Database kendaraan: validasi input, pencegahan plat ganda,
 * dan perlindungan kendaraan yang sudah punya riwayat pelanggaran.
 * Pesan kesalahan berupa teks siap tampil (bahasa Indonesia).
 */
public class KendaraanService {
    /** Pola plat Indonesia setelah dinormalisasi (tanpa spasi): 1-2 huruf, 1-4 angka, 0-3 huruf. Mis. B19N, BM1234AB. */
    private static final Pattern PLATE = Pattern.compile("^[A-Z]{1,2}[0-9]{1,4}[A-Z]{0,3}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$");
    private static final int MAX_TEXT = 60;

    private final KendaraanDao dao;

    public KendaraanService() { this(new KendaraanDao()); }
    public KendaraanService(KendaraanDao dao) { this.dao = dao; }

    public KendaraanDao getDao() { return dao; }
    public List<VehicleIdentity> findAll() { return dao.findAll(); }
    public static boolean isValidEmail(String email) { return email != null && EMAIL.matcher(email.trim()).matches(); }

    /**
     * Memeriksa isian kendaraan. v.getId() = 0 berarti kendaraan baru; selain itu kendaraan yang sedang diedit.
     * @return pesan kesalahan pertama yang ditemukan, atau null jika semua valid.
     */
    public String validate(VehicleIdentity v) {
        String owner = v.getOwner() == null ? "" : v.getOwner().trim();
        String brand = v.getBrand() == null ? "" : v.getBrand().trim();
        String email = v.getEmail() == null ? "" : v.getEmail().trim();
        String plate = KendaraanDao.normalizePlate(v.getPlate());

        if (owner.isEmpty()) return "Nama pemilik belum diisi.";
        if (owner.length() > MAX_TEXT) return "Nama pemilik terlalu panjang (maksimal " + MAX_TEXT + " karakter).";
        if (plate.isEmpty()) return "Plat nomor belum diisi.";
        if (!v.getPlate().matches("[A-Za-z0-9 \\-]+")) return "Plat hanya boleh berisi huruf, angka, spasi, dan tanda hubung.";
        if (!PLATE.matcher(plate).matches())
            return "Format plat tidak valid. Contoh yang benar: B 1234 XY, BM 19 N, atau B19N.";
        if (v.getKind() == null) return "Jenis kendaraan belum dipilih.";
        if (brand.isEmpty()) return "Merek belum diisi.";
        if (brand.length() > MAX_TEXT) return "Merek terlalu panjang (maksimal " + MAX_TEXT + " karakter).";
        if (v.getColor() == null) return "Warna belum dipilih.";
        if (email.isEmpty()) return "Email belum diisi.";
        if (!EMAIL.matcher(email).matches()) return "Format email tidak valid. Contoh: nama@contoh.com";

        VehicleIdentity samePlate = dao.findOtherByPlate(plate, v.getId());
        if (samePlate != null)
            return "Plat " + plate + " sudah terdaftar atas nama " + samePlate.getOwner() + ".";
        // Jenis + warna + merek harus unik: kendaraan tanpa plat dikenali dari kombinasi ini, dan tampilan simulasi bergantung padanya.
        VehicleIdentity sameTraits = dao.findOtherByTraits(v.getKind(), v.getColor(), brand, v.getId());
        if (sameTraits != null)
            return "Kombinasi " + v.getKind().getLabel() + " " + v.getColor().getLabel() + " merek " + brand
                    + " sudah dipakai kendaraan " + sameTraits.getPlate() + " (" + sameTraits.getOwner() + ").\n"
                    + "Ubah warna atau merek agar setiap kendaraan bisa dibedakan.";

        if (v.getId() > 0) {
            VehicleIdentity old = dao.findById(v.getId());
            if (old == null) return "Data kendaraan tidak ditemukan (mungkin sudah dihapus).";
            if (old.getKind() != v.getKind()) {
                int n = dao.countViolations(v.getId());
                if (n > 0) return "Jenis kendaraan tidak dapat diubah karena kendaraan ini memiliki " + n
                        + " riwayat pelanggaran yang bergantung pada jenisnya.";
            }
        }
        return null;
    }

    /** Menyimpan kendaraan baru setelah divalidasi ulang. Mengembalikan id baru. */
    public long add(VehicleIdentity v) {
        String error = validate(v);
        if (error != null) throw new IllegalArgumentException(error);
        return dao.insert(clean(v, 0));
    }

    /** Menyimpan perubahan setelah divalidasi ulang. */
    public void update(VehicleIdentity v) {
        if (v.getId() <= 0) throw new IllegalArgumentException("Kendaraan belum tersimpan.");
        String error = validate(v);
        if (error != null) throw new IllegalArgumentException(error);
        dao.update(clean(v, v.getId()));
    }

    /** Pesan alasan kendaraan tidak boleh dihapus, atau null jika boleh dihapus. */
    public String deleteBlockedReason(long id) {
        int n = dao.countViolations(id);
        return n == 0 ? null
                : "Kendaraan ini tidak dapat dihapus karena memiliki " + n + " riwayat pelanggaran.\n"
                + "Hapus dulu catatan pelanggarannya di Daftar Pelanggaran jika memang diperlukan.";
    }

    /** Menghapus kendaraan yang belum punya riwayat pelanggaran. */
    public void delete(long id) {
        String blocked = deleteBlockedReason(id);
        if (blocked != null) throw new IllegalArgumentException(blocked);
        dao.delete(id);
    }

    private static VehicleIdentity clean(VehicleIdentity v, long id) {
        return new VehicleIdentity(id, KendaraanDao.normalizePlate(v.getPlate()), v.getOwner().trim(),
                v.getKind(), v.getColor(), v.getBrand().trim(), v.getEmail().trim());
    }
}