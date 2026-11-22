package com.example.trafficlightsim.view;

import com.example.trafficlightsim.model.Jalur;
import com.example.trafficlightsim.model.ViolationRecord;
import com.example.trafficlightsim.view.component.HeaderBar;
import com.example.trafficlightsim.view.component.RoundedButton;
import com.example.trafficlightsim.view.component.RoundedPanel;
import com.example.trafficlightsim.view.component.RowActionCell;
import com.example.trafficlightsim.view.component.TabStyle;
import com.example.trafficlightsim.view.component.TableStyler;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Layar "Daftar Pelanggaran". Terdiri dari tiga tab: Daftar Pelanggaran (pencarian, filter, tabel dengan tombol
 * Cetak Log dan Hapus di setiap baris), Kotak Masuk (simulasi penerima email), dan Tinjau Sanggahan (operator).
 */
public class ViolationListPanel extends JPanel {
    private final ViolationTableModel tableModel = new ViolationTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField();
    private final JComboBox<Object> filterBox = new JComboBox<>();
    private final RoundedButton searchButton = RoundedButton.filled("Cari", Theme.TEAL);
    private final RoundedButton backButton = RoundedButton.onDark("Kembali ke simulasi");
    private Consumer<ViolationRecord> onPrintRow;
    private Consumer<ViolationRecord> onDeleteRow;
    private final RowActionCell actionCell = new RowActionCell("Cetak Log", 100);
    private final InboxPanel inboxPanel = new InboxPanel();
    private final DisputeReviewPanel reviewPanel = new DisputeReviewPanel();
    private final JTabbedPane tabs = new JTabbedPane();
    private boolean resettingFilterBox; // mencegah pengembalian kotak FILTER ke "FILTER" menghapus filter yang baru dipasang

    private TableRowSorter<ViolationTableModel> rowSorter;
    private final java.util.List<TableColumn> daftarSemuaKolom = new java.util.ArrayList<>();
    public ViolationListPanel() {
        super(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        HeaderBar header = new HeaderBar("Daftar Pelanggaran");
        header.setTrailing(backButton);
        add(header, BorderLayout.NORTH);

        JPanel listTab = new JPanel(new BorderLayout(0, 16));
        listTab.setOpaque(false);
        listTab.add(buildToolbar(), BorderLayout.NORTH);
        listTab.add(buildTableCard(), BorderLayout.CENTER);

        TabStyle.apply(tabs);
        tabs.addTab("Daftar Pelanggaran", listTab);
        tabs.addTab("Kotak Masuk", inboxPanel);
        tabs.addTab("Tinjau Sanggahan", reviewPanel);
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(Theme.padding(20, 32, 24, 32));
        body.add(tabs, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        // --- AKTIFKAN ROW SORTER UNTUK PENCARIAN & SORTING ---
        rowSorter = new TableRowSorter<>(tableModel);
        // Kolom denda berisi teks "Rp1.000.000"; urutkan berdasarkan angkanya.
        rowSorter.setComparator(ViolationTableModel.COL_FINE, (a, b) -> Long.compare(rupiahToLong(a), rupiahToLong(b)));
        table.setRowSorter(rowSorter);

        // Simpan referensi kolom asli
        daftarSemuaKolom.clear();
        TableColumnModel colModel = table.getColumnModel();
        for (int i = 0; i < colModel.getColumnCount(); i++) {
            daftarSemuaKolom.add(colModel.getColumn(i));
        }

        // Listener Tombol Cari
        searchButton.addActionListener(e -> eksekusiPencarian());
        
        // Listener Kotak Filter Kolom
        filterBox.addActionListener(e -> { if (!resettingFilterBox) eksekusiFilterKolom(); });
    }

    // Method Logika Pencarian
    private void eksekusiPencarian() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            // Mencari di semua kolom yang tampil; jalur (tidak lagi punya kolom) tetap ikut dicari.
            final String needle = keyword.toLowerCase();
            rowSorter.setRowFilter(new RowFilter<ViolationTableModel, Integer>() {
                @Override public boolean include(Entry<? extends ViolationTableModel, ? extends Integer> e) {
                    for (int i = 0; i < ViolationTableModel.ACTION_COLUMN; i++) {
                        if (e.getStringValue(i).toLowerCase().contains(needle)) return true;
                    }
                    return e.getModel().getRecord(e.getIdentifier()).getJalur().getLabel().toLowerCase().contains(needle);
                }
            });
        }
    }

    // Method Logika Tampilan Kolom
    // Method Logika Filter Data Berdasarkan Kategori
    private void eksekusiFilterKolom() {
        String selected = (String) filterBox.getSelectedItem();
        if (selected == null || "FILTER".equals(selected)) {
            rowSorter.setRowFilter(null); // Reset filter
            return;
        }

        switch (selected) {
            case "PELANGGARAN": {
                // Munculkan pilihan jenis pelanggaran
                String[] options = {"Semua", "Tidak menggunakan helm", "Tidak ada plat nomor", 
                    "Knalpot brong", "Melebihi kecepatan", "Melanggar lalin", "Bonceng 3+", "Tanpa sabuk pengaman", "Pelanggaran lainnya"};
                String choice = (String) JOptionPane.showInputDialog(
                    this, "Pilih Pelanggaran:", "Filter Pelanggaran",
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]
                );
                if (choice != null) {
                    if ("Semua".equals(choice)) {
                        rowSorter.setRowFilter(null);
                    } else {
                        // Kolom Pelanggaran
                        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(choice), ViolationTableModel.COL_VIOLATION));
                    }
                }
                break;
            }
            case "WAKTU": {
                String[] options = {"Dari yang Terbaru", "Dari yang Terlama"};
                String choice = (String) JOptionPane.showInputDialog(
                    this, "Pilih Urutan Waktu:", "Filter Waktu",
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]
                );
                if (choice != null) {
                    java.util.List<RowSorter.SortKey> sortKeys = new java.util.ArrayList<>();
                    if ("Dari yang Terbaru".equals(choice)) {
                        // Kolom Waktu, urut turun (DESCENDING)
                        sortKeys.add(new RowSorter.SortKey(ViolationTableModel.COL_TIME, SortOrder.DESCENDING));
                    } else {
                        // Urut naik (ASCENDING)
                        sortKeys.add(new RowSorter.SortKey(ViolationTableModel.COL_TIME, SortOrder.ASCENDING));
                    }
                    rowSorter.setSortKeys(sortKeys);
                }
                break;
            }
            case "JALUR": {
                String[] options = {"Semua", "Utara", "Timur", "Barat", "Selatan"};
                String choice = (String) JOptionPane.showInputDialog(
                    this, "Pilih Jalur:", "Filter Jalur",
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]
                );
                if (choice != null) {
                    if ("Semua".equals(choice)) {
                        rowSorter.setRowFilter(null);
                    } else {
                        // Kolom Jalur sudah tidak ditampilkan; disaring langsung dari data catatan.
                        final String jalurDipilih = choice;
                        rowSorter.setRowFilter(new RowFilter<ViolationTableModel, Integer>() {
                            @Override public boolean include(Entry<? extends ViolationTableModel, ? extends Integer> e) {
                                return e.getModel().getRecord(e.getIdentifier()).getJalur().getLabel().equals(jalurDipilih);
                            }
                        });
                    }
                }
                break;
            }
            case "JENIS": {
                String[] options = {"Semua", "Mobil", "Sepeda Motor"};
                String choice = (String) JOptionPane.showInputDialog(
                    this, "Pilih Jenis Kendaraan:", "Filter Jenis",
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]
                );
                if (choice != null) {
                    if ("Semua".equals(choice)) {
                        rowSorter.setRowFilter(null);
                    } else {
                        // Kolom Jenis
                        rowSorter.setRowFilter(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(choice) + "$", ViolationTableModel.COL_KIND));
                    }
                }
                break;
            }
            case "STATUS": {
                String[] options = {"Semua", "Belum Dikirim", "Terkirim", "Diterima", "Disanggah", "Selesai"};
                String choice = (String) JOptionPane.showInputDialog(
                    this, "Pilih Status:", "Filter Status",
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]
                );
                if (choice != null) {
                    if ("Semua".equals(choice)) {
                        rowSorter.setRowFilter(null);
                    } else {
                        rowSorter.setRowFilter(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(choice) + "$", ViolationTableModel.COL_STATUS));
                    }
                }
                break;
            }
        }
        
        // Kembalikan pilihan ComboBox ke "FILTER" setelah dipilih agar bisa digunakan lagi
        resettingFilterBox = true;
        filterBox.setSelectedItem("FILTER");
        resettingFilterBox = false;
    }

    private static long rupiahToLong(Object text) {
        String digits = String.valueOf(text).replaceAll("\\D", "");
        return digits.isEmpty() ? 0 : Long.parseLong(digits);
    }
    

    private JComponent buildToolbar() {
        Theme.styleInput(searchField);
        searchField.setPreferredSize(new Dimension(300, 44));
        searchField.setToolTipText("Cari ID, plat, pemilik, pelanggaran, atau jalur");
        
        // Filter
        filterBox.addItem("FILTER");
        filterBox.addItem("PELANGGARAN");
        filterBox.addItem("WAKTU");
        filterBox.addItem("JALUR");
        filterBox.addItem("JENIS");
        filterBox.addItem("STATUS");
        
        // Filter Style
        Theme.styleInput(filterBox);
        filterBox.setPreferredSize(new Dimension(170, 44));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        left.add(searchField);
        left.add(searchButton);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(filterBox);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JComponent buildTableCard() {
        TableStyler.apply(table, ViolationTableModel.COL_ID);
        TableStyler.alignHeaderRight(table, ViolationTableModel.ACTION_COLUMN);
        // ID, Plat, Pemilik, Pelanggaran, Waktu, Denda, Jenis, Status, Aksi
        int[] widths = {100, 90, 170, 250, 160, 100, 135, 120, actionCell.preferredColumnWidth()};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        installRowButtons();
        return TableStyler.card(table);
    }

    public void setRecords(List<ViolationRecord> records) { tableModel.setRecords(records); }

    /** Aksi yang dijalankan saat tombol "Cetak Log" pada sebuah baris ditekan (diisi PrintController). */
    public void setOnPrintRow(Consumer<ViolationRecord> action) { this.onPrintRow = action; }

    /** Aksi yang dijalankan saat tombol hapus (tong sampah) pada sebuah baris ditekan (diisi ViolationController). */
    public void setOnDeleteRow(Consumer<ViolationRecord> action) { this.onDeleteRow = action; }

    /** Memasang tombol "Cetak Log" dan tong sampah di kolom terakhir; klik pada sel dikenali lewat RowActionCell. */
    private void installRowButtons() {
        table.getColumnModel().getColumn(ViolationTableModel.ACTION_COLUMN).setCellRenderer(actionCell);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0 || col != ViolationTableModel.ACTION_COLUMN) return;
                int hit = actionCell.hit(table, row, col, e.getPoint());
                ViolationRecord record = tableModel.getRecord(table.convertRowIndexToModel(row));
                if (hit == 1 && onPrintRow != null) onPrintRow.accept(record);
                else if (hit == 2 && onDeleteRow != null) onDeleteRow.accept(record);
            }
        });
        table.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                boolean overButton = col == ViolationTableModel.ACTION_COLUMN && actionCell.hit(table, row, col, e.getPoint()) != 0;
                table.setCursor(Cursor.getPredefinedCursor(overButton ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
    }

    public ViolationTableModel getTableModel() { return tableModel; }
    public JTable getTable() { return table; }
    public JTextField getSearchField() { return searchField; }
    public JComboBox<Object> getFilterBox() { return filterBox; }
    public JButton getSearchButton() { return searchButton; }
    public JButton getBackButton() { return backButton; }
    public JTabbedPane getTabs() { return tabs; }
    public InboxPanel getInboxPanel() { return inboxPanel; }
    public DisputeReviewPanel getReviewPanel() { return reviewPanel; }
}