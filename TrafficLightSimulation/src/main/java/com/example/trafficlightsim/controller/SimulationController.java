package com.example.trafficlightsim.controller;

import com.example.trafficlightsim.SimulationPanel;
import com.example.trafficlightsim.view.ControlBar;

/**
 * Tombol Mulai / Berhenti di halaman simulasi.
 * (Pengisian MetricsPanel dan VehicleStatusPanel dari simulasi masih TODO.)
 */
public class SimulationController {
    private final SimulationPanel simulation;
    private final ControlBar controlBar;

    public SimulationController(SimulationPanel simulation, ControlBar controlBar) {
        this.simulation = simulation;
        this.controlBar = controlBar;
        controlBar.getStartButton().addActionListener(e -> { simulation.start(); refreshButtons(); });
        controlBar.getStopButton().addActionListener(e -> { simulation.stop(); refreshButtons(); });
        refreshButtons();
    }

    /** Hanya satu dari Mulai / Berhenti yang aktif sesuai keadaan simulasi. */
    private void refreshButtons() {
        boolean running = simulation.isRunning();
        controlBar.getStartButton().setEnabled(!running);
        controlBar.getStopButton().setEnabled(running);
    }
}
