package com.example.trafficlightsim.controller;

import com.example.trafficlightsim.DashboardFrame;

/**
 * Perpindahan halaman:
 * Simulasi -> Daftar Pelanggaran, Daftar Pelanggaran -> Simulasi, Simulasi -> Input Data,
 * serta Simulasi <-> Database kendaraan.
 * (Halaman Log dibuka oleh PrintController, halaman Input Data dibuat oleh ViolationController.)
 */
public class NavigationController {
    public NavigationController(DashboardFrame frame, ViolationController violationController) {
        // Halaman 1 -> Halaman 2
        frame.getDashboardPanel().getControlBar().getListButton()
                .addActionListener(e -> frame.showScreen(DashboardFrame.SCREEN_LIST));
        // Halaman 1 -> Halaman 3
        frame.getDashboardPanel().getControlBar().getInputButton()
                .addActionListener(e -> violationController.openInputDialog());
        // Simulasi -> Database kendaraan (data dimuat ulang oleh DatabaseController)
        frame.getDashboardPanel().getControlBar().getDatabaseButton()
                .addActionListener(e -> frame.showScreen(DashboardFrame.SCREEN_DATABASE));
        // Database kendaraan -> Simulasi
        frame.getDatabasePanel().getBackButton()
                .addActionListener(e -> frame.showScreen(DashboardFrame.SCREEN_DASHBOARD));
        // Halaman 2 -> Halaman 1
        frame.getListPanel().getBackButton()
                .addActionListener(e -> frame.showScreen(DashboardFrame.SCREEN_DASHBOARD));
    }
}