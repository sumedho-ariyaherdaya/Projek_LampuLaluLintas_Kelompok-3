package com.example.trafficlightsim.model;

/** Identitas pasti sebuah kendaraan: plat, pemilik, jenis, warna, merek, dan email (baris tabel kendaraan). */
public final class VehicleIdentity {
    private final long id;      // id baris di tabel kendaraan; 0 jika belum tersimpan / tidak diketahui
    private final String plate;
    private final String owner;
    private final JenisKendaraan kind;
    private final WarnaKendaraan color;
    private final String brand;
    private final String email; // email pemilik, tujuan pengiriman log pelanggaran; "" jika belum diisi

    /** Konstruktor lama (tanpa id dan email) tetap dipertahankan agar kode lama tidak berubah. */
    public VehicleIdentity(String plate, String owner, JenisKendaraan kind, WarnaKendaraan color, String brand) {
        this(0, plate, owner, kind, color, brand, "");
    }

    public VehicleIdentity(long id, String plate, String owner, JenisKendaraan kind, WarnaKendaraan color,
                           String brand, String email) {
        this.id = id;
        this.plate = plate;
        this.owner = owner;
        this.kind = kind;
        this.color = color;
        this.brand = brand;
        this.email = email == null ? "" : email;
    }

    public long getId() { return id; }
    public String getPlate() { return plate; }
    public String getOwner() { return owner; }
    public JenisKendaraan getKind() { return kind; }
    public WarnaKendaraan getColor() { return color; }
    public String getBrand() { return brand; }
    public String getEmail() { return email; }

    /** Kunci ciri (jenis + warna + merek). Dua kendaraan dengan kunci sama tampak identik di layar. */
    public String traitKey() { return kind + "|" + color + "|" + brand.toUpperCase(); }
}