package com.example.trafficlightsim.controller;

import javax.swing.Timer;

import com.example.trafficlightsim.SimulationPanel;
import com.example.trafficlightsim.model.Jalur;
import com.example.trafficlightsim.model.SignalState;
import com.example.trafficlightsim.view.ControlBar;
import com.example.trafficlightsim.view.MetricsPanel;

/**
 * Tombol Mulai / Berhenti di halaman simulasi.
 * (Pengisian MetricsPanel dan VehicleStatusPanel dari simulasi masih TODO.)
 */
public class SimulationController {
    private final SimulationPanel simulation;
    private final ControlBar controlBar;
    private final MetricsPanel metrics;
    private final Timer metricsTimer;

    public SimulationController(SimulationPanel simulation, ControlBar controlBar, MetricsPanel metrics) {
        this.simulation = simulation;
        this.controlBar = controlBar;
        this.metrics = metrics;
        controlBar.getStartButton().addActionListener(e -> { simulation.start(); refreshButtons(); refreshMetrics(); });
        controlBar.getStopButton().addActionListener(e -> { simulation.stop(); refreshButtons(); refreshMetrics(); });
        refreshButtons();

        metricsTimer = new Timer(100, e -> refreshMetrics());
        metricsTimer.start();
        refreshMetrics();
    }

    private void refreshMetrics() {
    for (Jalur j : Jalur.values()) {
        SignalState state = simulation.getSignalState(j);
        metrics.setSignal(j, state);
        metrics.setRedCountdown(j, state, simulation.getSecondsToGreen(j));
    }
    metrics.setCountdown(simulation.getSecondsToNextChange());
    }
    
    /** Hanya satu dari Mulai / Berhenti yang aktif sesuai keadaan simulasi. */
    private void refreshButtons() {
        boolean running = simulation.isRunning();
        controlBar.getStartButton().setEnabled(!running);
        controlBar.getStopButton().setEnabled(running);
    }
}