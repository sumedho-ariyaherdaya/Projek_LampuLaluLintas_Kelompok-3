package com.example.trafficlightsim.view;

import com.example.trafficlightsim.model.Jalur;
import com.example.trafficlightsim.model.ViolationRecord;
import com.example.trafficlightsim.view.component.HeaderBar;
import com.example.trafficlightsim.view.component.RoundedButton;
import com.example.trafficlightsim.view.component.RoundedPanel;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

/** Layar "Daftar Pelanggaran": pencarian, filter, tabel, dan tombol Cetak Log. */
public class ViolationListPanel extends JPanel {
    private final ViolationTableModel tableModel = new ViolationTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField();
    private final JComboBox<Object> filterBox = new JComboBox<>();
    private final RoundedButton searchButton = RoundedButton.filled("Cari", Theme.TEAL);
    private final RoundedButton printButton = RoundedButton.filled("Cetak Log", Theme.NAVY);
    private final RoundedButton backButton = RoundedButton.onDark("Kembali ke simulasi");

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
    }

    private JComponent buildToolbar() {
        Theme.styleInput(searchField);
        searchField.setPreferredSize(new Dimension(300, 44));
        searchField.setToolTipText("Cari plat atau pelanggaran");
        filterBox.addItem("Semua jalur");
        for (Jalur j : Jalur.values()) filterBox.addItem(j);
        Theme.styleInput(filterBox);
        filterBox.setPreferredSize(new Dimension(170, 44));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        left.add(searchField);
        left.add(searchButton);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(filterBox);
        right.add(printButton);

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
        table.setDefaultRenderer(Object.class, new CellRenderer());
        JTableHeader th = table.getTableHeader();
        th.setDefaultRenderer(new HeaderRenderer());
        th.setReorderingAllowed(false);
        th.setPreferredSize(new Dimension(10, 44));
        int[] widths = {160, 240, 190, 100, 130, 140};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

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

    public ViolationTableModel getTableModel() { return tableModel; }
    public JTable getTable() { return table; }
    public JTextField getSearchField() { return searchField; }
    public JComboBox<Object> getFilterBox() { return filterBox; }
    public JButton getSearchButton() { return searchButton; }
    public JButton getPrintButton() { return printButton; }
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

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                                 boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, false, false, row, col);
            setBorder(Theme.padding(0, 16, 0, 16));
            setFont(Theme.BODY_BOLD);
            setForeground(Theme.TEXT);
            setBackground(Theme.TABLE_HEADER);
            setOpaque(true);
            return this;
        }
    }
}
