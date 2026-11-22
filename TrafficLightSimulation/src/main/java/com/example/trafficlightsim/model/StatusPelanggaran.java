package com.example.trafficlightsim.model;

/**
 * Status penanganan sebuah log pelanggaran.
 * Alur: BELUM_DIKIRIM -> TERKIRIM -> DITERIMA -> SELESAI (dikonfirmasi penerima)
 *                                              \-> DISANGGAH -> SELESAI (sanggahan ditinjau operator)
 */
public enum StatusPelanggaran {
    BELUM_DIKIRIM("Belum Dikirim"),
    TERKIRIM("Terkirim"),
    DITERIMA("Diterima"),
    DISANGGAH("Disanggah"),
    SELESAI("Selesai");

    private final String label;
    StatusPelanggaran(String label) { this.label = label; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }

    /** Membaca kode dari database; nilai kosong / tidak dikenal dianggap BELUM_DIKIRIM. */
    public static StatusPelanggaran fromCode(String code) {
        if (code == null) return BELUM_DIKIRIM;
        try { return valueOf(code); } catch (IllegalArgumentException e) { return BELUM_DIKIRIM; }
    }
}