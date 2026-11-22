package com.example.trafficlightsim.view;

import com.example.trafficlightsim.model.ViolationRecord;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/** Model tabel sederhana untuk daftar ViolationRecord dengan kolom yang ditentukan pemakainya (Kotak Masuk, Tinjau Sanggahan). */
public class RecordListModel extends AbstractTableModel {
    /** Mengambil isi sel dari satu catatan. */
    public interface CellValue { Object get(ViolationRecord record, int column); }

    private final String[] columns;
    private final CellValue cellValue;
    private List<ViolationRecord> records = new ArrayList<>();

    public RecordListModel(String[] columns, CellValue cellValue) {
        this.columns = columns;
        this.cellValue = cellValue;
    }

    public void setRecords(List<ViolationRecord> records) {
        this.records = new ArrayList<>(records);
        fireTableDataChanged();
    }

    public ViolationRecord getRecord(int row) { return records.get(row); }

    /** Baris model untuk id database tertentu, atau -1. */
    public int indexOfId(long id) {
        for (int i = 0; i < records.size(); i++) if (records.get(i).getId() == id) return i;
        return -1;
    }

    @Override public int getRowCount() { return records.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int c) { return columns[c]; }
    @Override public Object getValueAt(int row, int col) { return cellValue.get(records.get(row), col); }
}