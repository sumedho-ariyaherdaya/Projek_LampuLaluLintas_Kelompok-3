package com.example.trafficlightsim.controller;

import com.example.trafficlightsim.DashboardFrame;
import com.example.trafficlightsim.model.VehicleIdentity;
import com.example.trafficlightsim.service.KendaraanService;
import com.example.trafficlightsim.view.DatabasePanel;
import com.example.trafficlightsim.view.VehicleFormDialog;
import javax.swing.JOptionPane;

/**
 * Halaman Database kendaraan: memuat data saat halaman dibuka, lalu tambah, ubah, dan hapus kendaraan.
 * Setelah data berubah, daftar kendaraan di simulasi ikut diperbarui.
 */
public class DatabaseController {
    private final DashboardFrame frame;
    private final KendaraanService service;
    private final DatabasePanel panel;
    private final VehicleFormDialog form;

    public DatabaseController(DashboardFrame frame, KendaraanService service) {
        this.frame = frame;
        this.service = service;
        this.panel = frame.getDatabasePanel();
        this.form = new VehicleFormDialog(frame);

        // Data dimuat ulang setiap kali tombol "Database" ditekan (perpindahan layar oleh NavigationController).
        frame.getDashboardPanel().getControlBar().getDatabaseButton().addActionListener(e -> {
            panel.resetFilters();
            reload();
        });
        panel.getAddButton().addActionListener(e -> add());
        panel.setOnEdit(this::edit);
        panel.setOnDelete(this::delete);
        reload();
    }

    private void reload() {
        try {
            panel.setVehicles(service.findAll());
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void add() {
        final long[] newId = {0};
        form.openForNew(frame, v -> {
            try {
                String error = service.validate(v);
                if (error != null) return error;
                newId[0] = service.add(v);
                return null;
            } catch (RuntimeException ex) {
                return ex.getMessage();
            }
        });
        if (newId[0] > 0) afterChange(newId[0]);
    }

    private void edit(VehicleIdentity v) {
        final boolean[] saved = {false};
        form.openForEdit(frame, v, edited -> {
            try {
                String error = service.validate(edited);
                if (error != null) return error;
                service.update(edited);
                saved[0] = true;
                return null;
            } catch (RuntimeException ex) {
                return ex.getMessage();
            }
        });
        if (saved[0]) afterChange(v.getId());
    }

    private void delete(VehicleIdentity v) {
        try {
            String blocked = service.deleteBlockedReason(v.getId());
            if (blocked != null) {
                JOptionPane.showMessageDialog(frame, blocked, "Kendaraan Dilindungi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int ok = JOptionPane.showConfirmDialog(frame,
                    "Hapus kendaraan " + v.getPlate() + " (" + v.getOwner() + ")?\n\nTindakan ini tidak dapat dibatalkan.",
                    "Hapus Kendaraan", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok != JOptionPane.YES_OPTION) return;
            service.delete(v.getId());
            afterChange(0);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Database", JOptionPane.ERROR_MESSAGE);
            reload();
        }
    }

    /** Memuat ulang tabel dan memperbarui kendaraan yang bisa muncul di simulasi. */
    private void afterChange(long selectId) {
        reload();
        if (selectId > 0) panel.selectVehicle(selectId);
        try {
            frame.getSimulation().setVehiclePool(service.findAll());
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Database", JOptionPane.ERROR_MESSAGE);
        }
    }
}