package com.example.trafficlightsim.view;

import com.example.trafficlightsim.model.ViolationRecord;
import com.example.trafficlightsim.util.Rupiah;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Model tabel Daftar Pelanggaran. Kolom Jalur tidak ditampilkan lagi (informasi jalur tetap ada di
 * detail/cetak log dan masih bisa dipakai sebagai filter lewat getRecord().getJalur()).
 */
public class ViolationTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"ID LOG", "PLAT", "PEMILIK", "PELANGGARAN", "WAKTU", "DENDA (Rp)", "JENIS", "STATUS", "AKSI"};
    public static final int COL_ID = 0;
    public static final int COL_PLATE = 1;
    public static final int COL_OWNER = 2;
    public static final int COL_VIOLATION = 3;
    public static final int COL_TIME = 4;
    public static final int COL_FINE = 5;
    public static final int COL_KIND = 6;
    public static final int COL_STATUS = 7;
    public static final int ACTION_COLUMN = 8; // kolom tombol "Cetak Log" dan "Hapus" di tiap baris
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private List<ViolationRecord> records = new ArrayList<>();

    public void setRecords(List<ViolationRecord> records) {
        this.records = new ArrayList<>(records);
        fireTableDataChanged();
    }

    public ViolationRecord getRecord(int row) { return records.get(row); }

    @Override public int getRowCount() { return records.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int c) { return COLUMNS[c]; }

    @Override public Object getValueAt(int row, int col) {
        ViolationRecord r = records.get(row);
        switch (col) {
            case COL_ID: return r.getKodeLog().isEmpty() ? "-" : r.getKodeLog();
            case COL_PLATE: return r.getPlate();
            case COL_OWNER: return r.getOwnerName();
            case COL_VIOLATION: return r.getViolationSummary();
            case COL_TIME: return TIME.format(r.getTime());
            case COL_FINE: return Rupiah.format(r.getTotalFine());
            case COL_KIND: return r.getVehicleKind().getLabel();
            case COL_STATUS: return r.getStatus();
            default: return "Cetak Log";
        }
    }
}