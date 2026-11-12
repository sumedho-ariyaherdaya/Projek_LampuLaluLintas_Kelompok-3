package com.example.trafficlightsim.view;

import com.example.trafficlightsim.model.ViolationRecord;
import com.example.trafficlightsim.util.Rupiah;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/** Model tabel Daftar Pelanggaran; kolom mengikuti mockup. */
public class ViolationTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"PLAT", "PELANGGARAN", "WAKTU", "JALUR", "DENDA (Rp)", "JENIS", "AKSI"};
    public static final int ACTION_COLUMN = 6; // kolom tombol "Cetak Log" di tiap baris
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
            case 0: return r.getPlate();
            case 1: return r.getViolationSummary();
            case 2: return TIME.format(r.getTime());
            case 3: return r.getJalur().getLabel();
            case 4: return Rupiah.format(r.getTotalFine());
            case 5: return r.getVehicleKind().getLabel();
            default: return "Cetak Log";
        }
    }
}
