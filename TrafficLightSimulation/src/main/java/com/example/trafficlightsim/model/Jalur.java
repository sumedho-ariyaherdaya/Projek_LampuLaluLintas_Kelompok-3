package com.example.trafficlightsim.model;

/** Empat jalur persimpangan. Urutannya sama dengan Vehicle.Direction (NORTH, EAST, SOUTH, WEST). */
public enum Jalur {
    UTARA("Utara"), TIMUR("Timur"), SELATAN("Selatan"), BARAT("Barat");

    private final String label;
    Jalur(String label) { this.label = label; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
}
