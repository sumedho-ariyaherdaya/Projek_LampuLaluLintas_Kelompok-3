package com.example.trafficlightsim.view;

import com.example.trafficlightsim.model.JenisKendaraan;
import com.example.trafficlightsim.model.VehicleIdentity;
import com.example.trafficlightsim.model.WarnaKendaraan;
import com.example.trafficlightsim.view.component.HeaderBar;
import com.example.trafficlightsim.view.component.RoundedButton;
import com.example.trafficlightsim.view.component.RowActionCell;
import com.example.trafficlightsim.view.component.TableStyler;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.table.TableRowSorter;

/**
 * Layar "Database Kendaraan": tabel seluruh kendaraan dengan pencarian, filter jenis dan warna,
 * serta tombol Tambah, Edit, dan Hapus. Logika penyimpanan ada di DatabaseController.
 */
public class DatabasePanel extends JPanel {
    private static final String ALL = "Semua";

    private final VehicleTableModel tableModel = new VehicleTableModel();
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<VehicleTableModel> sorter = new TableRowSorter<>(tableModel);
    private final JTextField searchField = new JTextField();
    private final JComboBox<Object> kindFilter = new JComboBox<>();
    private final JComboBox<Object> colorFilter = new JComboBox<>();
    private final RoundedButton searchButton = RoundedButton.filled("Cari", Theme.TEAL);
    private final RoundedButton addButton = RoundedButton.gold("+ Tambah Kendaraan");
    private final RoundedButton backButton = RoundedButton.onDark("Kembali ke simulasi");
    private final JLabel countLabel = new JLabel(" ");
    private final RowActionCell actionCell = new RowActionCell("Edit", 70);
    private Consumer<VehicleIdentity> onEdit;
    private Consumer<VehicleIdentity> onDelete;

    public DatabasePanel() {
        super(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        HeaderBar header = new HeaderBar("Database Kendaraan");
        header.setTrailing(backButton);
        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.setBorder(Theme.padding(24, 32, 24, 32));
        body.add(buildToolbar(), BorderLayout.NORTH);
        body.add(buildTableCard(), BorderLayout.CENTER);
        countLabel.setFont(Theme.BODY);
        countLabel.setForeground(Theme.TEXT_MUTED);
        body.add(countLabel, BorderLayout.SOUTH);
        add(body, BorderLayout.CENTER);

        searchButton.addActionListener(e -> applyFilters());
        searchField.addActionListener(e -> applyFilters());     // tombol Enter
        kindFilter.addActionListener(e -> applyFilters());
        colorFilter.addActionListener(e -> applyFilters());
    }

    private JComponent buildToolbar() {
        Theme.styleInput(searchField);
        searchField.setPreferredSize(new Dimension(300, 44));
        searchField.setToolTipText("Cari pemilik, plat, merek, atau email");

        kindFilter.addItem(ALL);
        for (JenisKendaraan k : JenisKendaraan.values()) kindFilter.addItem(k);
        colorFilter.addItem(ALL);
        for (WarnaKendaraan w : WarnaKendaraan.values()) colorFilter.addItem(w);
        for (JComboBox<Object> box : java.util.List.of(kindFilter, colorFilter)) {
            Theme.styleInput(box);
            box.setPreferredSize(new Dimension(150, 44));
        }

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        left.add(searchField);
        left.add(searchButton);
        left.add(label("Jenis"));
        left.add(kindFilter);
        left.add(label("Warna"));
        left.add(colorFilter);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(addButton);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.BODY_BOLD);
        l.setForeground(Theme.TEXT);
        return l;
    }

    private JComponent buildTableCard() {
        TableStyler.apply(table, VehicleTableModel.COL_ID);
        TableStyler.alignHeaderRight(table, VehicleTableModel.ACTION_COLUMN);
        table.setRowSorter(sorter);
        table.getColumnModel().getColumn(VehicleTableModel.ACTION_COLUMN).setCellRenderer(actionCell);
        sorter.setSortable(VehicleTableModel.ACTION_COLUMN, false);
        sorter.setComparator(VehicleTableModel.COL_ID, (a, b) -> Long.compare((Long) a, (Long) b)); // urut angka, bukan teks
        int[] widths = {50, 190, 100, 120, 100, 110, 260, actionCell.preferredColumnWidth()};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0 || col != VehicleTableModel.ACTION_COLUMN) return;
                int hit = actionCell.hit(table, row, col, e.getPoint());
                VehicleIdentity v = tableModel.getVehicle(table.convertRowIndexToModel(row));
                if (hit == 1 && onEdit != null) onEdit.accept(v);
                else if (hit == 2 && onDelete != null) onDelete.accept(v);
            }
        });
        table.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                boolean over = col == VehicleTableModel.ACTION_COLUMN && actionCell.hit(table, row, col, e.getPoint()) != 0;
                table.setCursor(Cursor.getPredefinedCursor(over ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
        return TableStyler.card(table);
    }

    /** Menerapkan kata kunci pencarian dan filter jenis/warna sekaligus. */
    private void applyFilters() {
        final String needle = searchField.getText().trim().toLowerCase();
        final Object kind = kindFilter.getSelectedItem();
        final Object color = colorFilter.getSelectedItem();
        sorter.setRowFilter(new RowFilter<VehicleTableModel, Integer>() {
            @Override public boolean include(Entry<? extends VehicleTableModel, ? extends Integer> e) {
                VehicleIdentity v = e.getModel().getVehicle(e.getIdentifier());
                if (kind instanceof JenisKendaraan && v.getKind() != kind) return false;
                if (color instanceof WarnaKendaraan && v.getColor() != color) return false;
                if (needle.isEmpty()) return true;
                return v.getOwner().toLowerCase().contains(needle) || v.getPlate().toLowerCase().contains(needle)
                        || v.getBrand().toLowerCase().contains(needle) || v.getEmail().toLowerCase().contains(needle)
                        || String.valueOf(v.getId()).equals(needle);
            }
        });
        updateCount();
    }

    private void updateCount() {
        countLabel.setText("Menampilkan " + table.getRowCount() + " dari " + tableModel.getRowCount() + " kendaraan");
    }

    /** Mengisi tabel; pencarian dan filter yang sedang aktif tetap berlaku. */
    public void setVehicles(List<VehicleIdentity> vehicles) {
        tableModel.setVehicles(vehicles);
        updateCount();
    }

    /** Menyorot baris kendaraan dengan id tertentu (mis. setelah tambah/ubah). */
    public void selectVehicle(long id) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getVehicle(i).getId() == id) {
                int view = table.convertRowIndexToView(i);
                if (view >= 0) {
                    table.getSelectionModel().setSelectionInterval(view, view);
                    table.scrollRectToVisible(table.getCellRect(view, 0, true));
                }
                return;
            }
        }
    }

    /** Mengosongkan pencarian dan filter. */
    public void resetFilters() {
        searchField.setText("");
        kindFilter.setSelectedIndex(0);
        colorFilter.setSelectedIndex(0);
        applyFilters();
    }

    public void setOnEdit(Consumer<VehicleIdentity> action) { this.onEdit = action; }
    public void setOnDelete(Consumer<VehicleIdentity> action) { this.onDelete = action; }

    public List<VehicleIdentity> getVisibleVehicles() {
        List<VehicleIdentity> out = new ArrayList<>();
        for (int i = 0; i < table.getRowCount(); i++) out.add(tableModel.getVehicle(table.convertRowIndexToModel(i)));
        return out;
    }

    public JTable getTable() { return table; }
    public JButton getAddButton() { return addButton; }
    public JButton getBackButton() { return backButton; }
    public JButton getSearchButton() { return searchButton; }
    public JTextField getSearchField() { return searchField; }
    public JComboBox<Object> getKindFilter() { return kindFilter; }
    public JComboBox<Object> getColorFilter() { return colorFilter; }
}