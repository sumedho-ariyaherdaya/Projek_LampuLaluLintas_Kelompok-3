package com.example.trafficlightsim.view;

import com.example.trafficlightsim.model.Jalur;
import com.example.trafficlightsim.model.ViolationRecord;
import com.example.trafficlightsim.view.component.HeaderBar;
import com.example.trafficlightsim.view.component.RoundedButton;
import com.example.trafficlightsim.view.component.RoundedPanel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.table.*;

/** Layar "Daftar Pelanggaran": pencarian, filter, dan tabel dengan tombol Cetak Log di setiap baris. */
public class ViolationListPanel extends JPanel {
    private final ViolationTableModel tableModel = new ViolationTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField();
    private final JComboBox<Object> filterBox = new JComboBox<>();
    private final RoundedButton searchButton = RoundedButton.filled("Cari", Theme.TEAL);
    private final RoundedButton backButton = RoundedButton.onDark("Kembali ke simulasi");
    private Consumer<ViolationRecord> onPrintRow;

    private TableRowSorter<ViolationTableModel> rowSorter;
    private final java.util.List<TableColumn> daftarSemuaKolom = new java.util.ArrayList<>();
    public ViolationListPanel() {
        super(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        HeaderBar header = new HeaderBar("Daftar Pelanggaran");
        header.setTrailing(backButton);
        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.setBorder(Theme.padding(24, 32, 24, 32));
        body.add(buildToolbar(), BorderLayout.NORTH);
        body.add(buildTableCard(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        // --- AKTIFKAN ROW SORTER UNTUK PENCARIAN & SORTING ---
        rowSorter = new TableRowSorter<>(tableModel);
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
        filterBox.addActionListener(e -> eksekusiFilterKolom());
    }

    // Method Logika Pencarian
    private void eksekusiPencarian() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
        }
    }

    // Method Logika Tampilan Kolom
    private void eksekusiFilterKolom() {
        String selected = (String) filterBox.getSelectedItem();
        TableColumnModel colModel = table.getColumnModel();

        if ("FILTER".equals(selected)) {
            while (colModel.getColumnCount() > 0) {
                colModel.removeColumn(colModel.getColumn(0));
            }
            for (TableColumn col : daftarSemuaKolom) {
                colModel.addColumn(col);
            }
        } else {
            int targetIndex = -1;
            switch (selected) {
                case "PLAT": targetIndex = 0; break;
                case "PELANGGARAN": targetIndex = 1; break;
                case "WAKTU": targetIndex = 2; break;
                case "JALUR": targetIndex = 3; break;
                case "DENDA (Rp)": targetIndex = 4; break;
                case "JENIS": targetIndex = 5; break;
            }

            if (targetIndex != -1) {
                while (colModel.getColumnCount() > 0) {
                    colModel.removeColumn(colModel.getColumn(0));
                }
                // Tampilkan kolom yang dipilih + kolom aksi (Cetak Log)
                colModel.addColumn(daftarSemuaKolom.get(targetIndex));
                colModel.addColumn(daftarSemuaKolom.get(ViolationTableModel.ACTION_COLUMN));
            }
        }
    }
    

    private JComponent buildToolbar() {
        Theme.styleInput(searchField);
        searchField.setPreferredSize(new Dimension(300, 44));
        searchField.setToolTipText("Cari plat atau pelanggaran");
        
        // Ganti isi ComboBox menjadi opsi kolom tabel
        filterBox.addItem("FILTER");
        filterBox.addItem("PLAT");
        filterBox.addItem("PELANGGARAN");
        filterBox.addItem("WAKTU");
        filterBox.addItem("JALUR");
        filterBox.addItem("DENDA (Rp)");
        filterBox.addItem("JENIS");
        
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
        table.setRowHeight(44);
        table.setFont(Theme.BODY);
        table.setForeground(Theme.TEXT);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Theme.TABLE_LINE);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(Theme.TABLE_SELECTION);
        table.setSelectionForeground(Theme.TEXT);
        table.setFillsViewportHeight(true);
        
        // ---> TARUH DI SINI <---
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        table.setDefaultRenderer(Object.class, new CellRenderer());
        JTableHeader th = table.getTableHeader();
        th.setDefaultRenderer(new HeaderRenderer());
        th.setReorderingAllowed(false);
        th.setPreferredSize(new Dimension(10, 44));
        int[] widths = {140, 320, 180, 90, 120, 120, 140};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        installRowPrintButton();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setColumnHeaderView(th);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        RoundedPanel card = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(Theme.padding(1));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    public void setRecords(List<ViolationRecord> records) { tableModel.setRecords(records); }

    /** Aksi yang dijalankan saat tombol "Cetak Log" pada sebuah baris ditekan (diisi PrintController). */
    public void setOnPrintRow(Consumer<ViolationRecord> action) { this.onPrintRow = action; }

    /** Memasang tombol "Cetak Log" di kolom terakhir; klik pada sel itu membuka log baris tersebut. */
    private void installRowPrintButton() {
        table.getColumnModel().getColumn(ViolationTableModel.ACTION_COLUMN).setCellRenderer(new PrintButtonRenderer());
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0 || col != ViolationTableModel.ACTION_COLUMN || onPrintRow == null) return;
                onPrintRow.accept(tableModel.getRecord(table.convertRowIndexToModel(row)));
            }
        });
        table.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                boolean overButton = table.rowAtPoint(e.getPoint()) >= 0
                        && table.columnAtPoint(e.getPoint()) == ViolationTableModel.ACTION_COLUMN;
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

    /** Baris belang dengan teks plat tebal. */
    private static class CellRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                                 boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, false, row, col);
            setBorder(Theme.padding(0, 16, 0, 16));
            setFont(col == 0 ? Theme.BODY_BOLD : Theme.BODY);
            setForeground(Theme.TEXT);
            setBackground(sel ? Theme.TABLE_SELECTION : row % 2 == 0 ? Color.WHITE : Theme.TABLE_STRIPE);
            return this;
        }
    }

    /** Menggambar tombol "Cetak Log" di sel; klik ditangani oleh mouse listener di installRowPrintButton(). */
    private static class PrintButtonRenderer implements TableCellRenderer {
        // Ubah GridBagLayout menjadi FlowLayout dengan alignment RIGHT dan padding kanan
        private final JPanel cell = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 6));
        private final RoundedButton button = new RoundedButton("Cetak Log", Theme.NAVY, Color.WHITE, null) {
            @Override public Dimension getPreferredSize() { return new Dimension(126, 32); }
            @Override public Dimension getMinimumSize() { return new Dimension(126, 32); }
        };

        PrintButtonRenderer() {
            cell.add(button);
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                                 boolean focus, int row, int col) {
            cell.setBackground(sel ? Theme.TABLE_SELECTION : row % 2 == 0 ? Color.WHITE : Theme.TABLE_STRIPE);
            return cell;
        }
    }

    /** Menggambar header tabel; untuk kolom aksi diset rata kanan agar sejajar dengan tombol. */
    private static class HeaderRenderer implements TableCellRenderer {
        private final JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        private final JLabel label = new JLabel();

        HeaderRenderer() {
            headerPanel.setOpaque(true);
            headerPanel.setBackground(Theme.TABLE_HEADER);
            headerPanel.setBorder(Theme.padding(0, 0, 0, 16));
            label.setFont(Theme.BODY_BOLD);
            label.setForeground(Theme.TEXT);
            headerPanel.add(label);
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                                 boolean focus, int row, int col) {
            label.setText(v != null ? v.toString() : "");
            
            // Jika ini kolom aksi, ubah tata letak panel menjadi rata kanan
            if (col == ViolationTableModel.ACTION_COLUMN) {
                ((FlowLayout) headerPanel.getLayout()).setAlignment(FlowLayout.RIGHT);
            } else {
                ((FlowLayout) headerPanel.getLayout()).setAlignment(FlowLayout.LEFT);
            }
            
            headerPanel.revalidate();
            return headerPanel;
        }
    }
}
