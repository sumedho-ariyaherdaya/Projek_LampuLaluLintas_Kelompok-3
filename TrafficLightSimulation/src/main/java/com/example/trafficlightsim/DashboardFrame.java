package com.example.trafficlightsim;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.example.trafficlightsim.controller.NavigationController;
import com.example.trafficlightsim.controller.PrintController;
import com.example.trafficlightsim.controller.SimulationController;
import com.example.trafficlightsim.controller.ViolationController;
import com.example.trafficlightsim.database.KendaraanDao;
import com.example.trafficlightsim.service.LogPrinter;
import com.example.trafficlightsim.service.ViolationLogService;
import com.example.trafficlightsim.view.DashboardPanel;
import com.example.trafficlightsim.view.Theme;
import com.example.trafficlightsim.view.ViolationListPanel;

/**
 * Jendela utama. Memuat dua layar bergantian (CardLayout): Simulasi dan Daftar Pelanggaran.
 * Menggantikan versi hasil NetBeans GUI Builder (DashboardFrame.form dihapus) agar bisa diedit langsung.
 * Main.java tidak perlu diubah.
 */
public class DashboardFrame extends JFrame {
    public static final String SCREEN_DASHBOARD = "dashboard";
    public static final String SCREEN_LIST = "list";

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final SimulationPanel simulation = new SimulationPanel();
    private final DashboardPanel dashboardPanel;
    private final ViolationListPanel listPanel = new ViolationListPanel();

    public DashboardFrame() {
        super("Simulasi Lalu Lintas 4 Jalur");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Ukuran awal simulasi tetap seperti kode sebelumnya: 70% lebar dan 60% tinggi layar.
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        simulation.setPreferredSize(new Dimension((int) (screen.width * 0.55), (int) (screen.height * 0.60)));

        dashboardPanel = new DashboardPanel(simulation);

        // Kendaraan yang muncul di simulasi diambil dari tabel kendaraan di database.
        try {
            simulation.setVehiclePool(new KendaraanDao().findAll());
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database", JOptionPane.ERROR_MESSAGE);
        }

        root.setBackground(Theme.BACKGROUND);
        root.add(dashboardPanel, SCREEN_DASHBOARD);
        root.add(listPanel, SCREEN_LIST);
        setContentPane(root);
        setMinimumSize(new Dimension(1100, 700));
        pack();

        // Menyambungkan semua tombol (urutan ini penting: ViolationController dulu, dipakai NavigationController).
        ViolationController violationController = new ViolationController(this, new ViolationLogService());
        new SimulationController(simulation, dashboardPanel.getControlBar(), dashboardPanel.getMetrics());
        new NavigationController(this, violationController);
        new PrintController(this, new LogPrinter());
    }

    /** Pindah layar; dipanggil oleh NavigationController nanti. */
    public void showScreen(String name) { cards.show(root, name); }

    public SimulationPanel getSimulation() { return simulation; }
    public DashboardPanel getDashboardPanel() { return dashboardPanel; }
    public ViolationListPanel getListPanel() { return listPanel; }
}
