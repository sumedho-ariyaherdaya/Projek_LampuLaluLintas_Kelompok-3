package com.example.trafficlightsim.model;

import java.awt.Color;

/**
 * Warna kendaraan yang tersedia di simulasi. Nilai Color sama dengan palet lama di Vehicle,
 * dipakai untuk badan mobil dan untuk mewarnai badan sepeda motor (lihat BikeSprites).
 */
public enum WarnaKendaraan {
    MERAH("Merah", new Color(225, 82, 70)),
    BIRU("Biru", new Color(50, 133, 204)),
    KUNING("Kuning", new Color(244, 181, 55)),
    HIJAU("Hijau", new Color(94, 171, 112)),
    UNGU("Ungu", new Color(139, 104, 190)),
    OREN("Oren", new Color(239, 125, 64));

    private final String label;
    private final Color color;

    WarnaKendaraan(String label, Color color) { this.label = label; this.color = color; }

    public String getLabel() { return label; }
    public Color getColor() { return color; }
    @Override public String toString() { return label; }
}
