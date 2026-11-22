package com.example.trafficlightsim.view;

import com.example.trafficlightsim.model.VehicleIdentity;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/** Model tabel halaman Database kendaraan. */
public class VehicleTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"ID", "PEMILIK", "PLAT", "JENIS", "MEREK", "WARNA", "EMAIL", "AKSI"};
    public static final int COL_ID = 0;
    public static final int COL_OWNER = 1;
    public static final int COL_PLATE = 2;
    public static final int COL_KIND = 3;
    public static final int COL_BRAND = 4;
    public static final int COL_COLOR = 5;
    public static final int COL_EMAIL = 6;
    public static final int ACTION_COLUMN = 7; // tombol Edit dan Hapus

    private List<VehicleIdentity> vehicles = new ArrayList<>();

    public void setVehicles(List<VehicleIdentity> vehicles) {
        this.vehicles = new ArrayList<>(vehicles);
        fireTableDataChanged();
    }

    public VehicleIdentity getVehicle(int row) { return vehicles.get(row); }

    @Override public int getRowCount() { return vehicles.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int c) { return COLUMNS[c]; }

    @Override public Object getValueAt(int row, int col) {
        VehicleIdentity v = vehicles.get(row);
        switch (col) {
            case COL_ID: return v.getId();
            case COL_OWNER: return v.getOwner();
            case COL_PLATE: return v.getPlate();
            case COL_KIND: return v.getKind().getLabel();
            case COL_BRAND: return v.getBrand();
            case COL_COLOR: return v.getColor(); // digambar sebagai titik warna + nama oleh TableStyler
            case COL_EMAIL: return v.getEmail();
            default: return "Edit";
        }
    }
}