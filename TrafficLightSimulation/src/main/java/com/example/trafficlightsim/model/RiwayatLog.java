package com.example.trafficlightsim.model;

import java.time.LocalDateTime;

/** Satu baris riwayat sebuah log pelanggaran (pengiriman email, tanggapan penerima, peninjauan operator). */
public final class RiwayatLog {

    public enum Aksi {
        DICATAT("Pelanggaran dicatat"),
        DIKIRIM("Email dikirim"),
        DIKIRIM_ULANG("Email dikirim ulang"),
        DIBUKA("Email diterima penerima"),
        DIKONFIRMASI("Dikonfirmasi penerima"),
        DISANGGAH("Disanggah penerima"),
        SANGGAHAN_DITERIMA("Sanggahan diterima operator"),
        SANGGAHAN_DITOLAK("Sanggahan ditolak operator");

        private final String label;
        Aksi(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    private final long id;
    private final LocalDateTime time;
    private final Aksi aksi;
    private final String aktor;
    private final String emailTujuan;
    private final StatusPelanggaran statusBaru;
    private final String catatan;

    public RiwayatLog(long id, LocalDateTime time, Aksi aksi, String aktor, String emailTujuan,
                      StatusPelanggaran statusBaru, String catatan) {
        this.id = id;
        this.time = time;
        this.aksi = aksi;
        this.aktor = aktor;
        this.emailTujuan = emailTujuan == null ? "" : emailTujuan;
        this.statusBaru = statusBaru;
        this.catatan = catatan == null ? "" : catatan;
    }

    public long getId() { return id; }
    public LocalDateTime getTime() { return time; }
    public Aksi getAksi() { return aksi; }
    public String getAktor() { return aktor; }
    public String getEmailTujuan() { return emailTujuan; }
    public StatusPelanggaran getStatusBaru() { return statusBaru; }
    public String getCatatan() { return catatan; }
}