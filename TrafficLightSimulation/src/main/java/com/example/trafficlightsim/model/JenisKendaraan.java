package com.example.trafficlightsim.model;

/** Jenis kendaraan. Sejajar dengan Vehicle.Type (CAR, MOTORCYCLE). */
public enum JenisKendaraan {
    MOBIL("Mobil"), SEPEDA_MOTOR("Sepeda Motor");

    private final String label;
    JenisKendaraan(String label) { this.label = label; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
}
