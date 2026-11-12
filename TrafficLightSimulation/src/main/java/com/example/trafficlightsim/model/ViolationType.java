package com.example.trafficlightsim.model;

/**
 * Daftar jenis pelanggaran beserta dendanya.
 * Tiga yang pertama persis sama dengan teks di Vehicle.violations; sisanya dari mockup.
 */
public enum ViolationType {
    NO_HELMET("Tidak menggunakan helm", 300_000),
    NO_PLATE("Tidak ada plat nomor", 150_000),
    NOISY_EXHAUST("Knalpot brong", 250_000), // TODO: nominal belum ada di mockup, sesuaikan
    SPEEDING("Melebihi kecepatan", 150_000),
    RED_LIGHT("Melanggar lalin", 500_000),
    TRIPLE_RIDING("Bonceng 3+", 200_000),
    NO_SEATBELT("Tanpa sabuk pengaman", 200_000);

    private final String label;
    private final int fine;

    ViolationType(String label, int fine) { this.label = label; this.fine = fine; }

    public String getLabel() { return label; }
    public int getFine() { return fine; }

    /** Mencari jenis dari teks yang dipakai Vehicle.violations; null jika tidak dikenal. */
    public static ViolationType fromLabel(String label) {
        for (ViolationType t : values()) if (t.label.equalsIgnoreCase(label)) return t;
        return null;
    }

    @Override public String toString() { return label; }
}
