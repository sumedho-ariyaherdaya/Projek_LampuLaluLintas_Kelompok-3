package com.example.trafficlightsim.model;

/** Identitas pasti sebuah kendaraan: plat, pemilik, jenis, warna, dan merek (baris tabel kendaraan). */
public final class VehicleIdentity {
    private final String plate;
    private final String owner;
    private final JenisKendaraan kind;
    private final WarnaKendaraan color;
    private final String brand;

    public VehicleIdentity(String plate, String owner, JenisKendaraan kind, WarnaKendaraan color, String brand) {
        this.plate = plate;
        this.owner = owner;
        this.kind = kind;
        this.color = color;
        this.brand = brand;
    }

    public String getPlate() { return plate; }
    public String getOwner() { return owner; }
    public JenisKendaraan getKind() { return kind; }
    public WarnaKendaraan getColor() { return color; }
    public String getBrand() { return brand; }

    /** Kunci ciri (jenis + warna + merek). Dua kendaraan dengan kunci sama tampak identik di layar. */
    public String traitKey() { return kind + "|" + color + "|" + brand.toUpperCase(); }
}
