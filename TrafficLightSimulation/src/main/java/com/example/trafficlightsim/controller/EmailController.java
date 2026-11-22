package com.example.trafficlightsim.controller;

import com.example.trafficlightsim.DashboardFrame;
import com.example.trafficlightsim.model.RiwayatLog;
import com.example.trafficlightsim.model.StatusPelanggaran;
import com.example.trafficlightsim.model.ViolationRecord;
import com.example.trafficlightsim.service.ViolationLogService;
import com.example.trafficlightsim.service.ViolationWorkflowService;
import com.example.trafficlightsim.view.DisputeReviewPanel;
import com.example.trafficlightsim.view.InboxPanel;
import com.example.trafficlightsim.view.ReceiptDialog;
import com.example.trafficlightsim.view.ViolationListPanel;
import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

/**
 * Penanganan log lewat email (simulasi): tombol Kirim Email di halaman Log, tab Kotak Masuk (penerima membuka,
 * mengonfirmasi, atau menyanggah), dan tab Tinjau Sanggahan (operator menerima atau menolak).
 * Semua perubahan status dan riwayat disimpan lewat ViolationWorkflowService (SQLite).
 */
public class EmailController {
    /** Lama animasi "mengirim" pada simulasi pengiriman email. */
    public static int sendDelayMillis = 1200;

    private final DashboardFrame frame;
    private final ViolationController violationController;
    private final PrintController printController;
    private final ViolationWorkflowService workflow;
    private final ViolationLogService logService = new ViolationLogService();
    private final ViolationListPanel listPanel;
    private final InboxPanel inbox;
    private final DisputeReviewPanel review;
    private final ReceiptDialog logDialog;
    private boolean adjusting; // true saat isi tabel/pilihan diubah lewat kode, agar listener tidak ikut bereaksi

    public EmailController(DashboardFrame frame, ViolationController violationController,
                           PrintController printController, ViolationWorkflowService workflow) {
        this.frame = frame;
        this.violationController = violationController;
        this.printController = printController;
        this.workflow = workflow;
        this.listPanel = frame.getListPanel();
        this.inbox = listPanel.getInboxPanel();
        this.review = listPanel.getReviewPanel();
        this.logDialog = printController.getDialog();
        wire();
        refreshAll();
    }

    private void wire() {
        // Halaman Log: muat riwayat dan atur tombol kirim setiap log dibuka.
        printController.setOnLogOpened(this::loadLog);
        logDialog.getSendButton().addActionListener(e -> sendEmail());

        // Tab disegarkan saat layar Daftar Pelanggaran tampil dan saat berpindah tab.
        listPanel.addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) { refreshAll(); }
        });
        listPanel.getTabs().addChangeListener(e -> refreshAll());

        inbox.getRecipientBox().addActionListener(e -> { if (!adjusting) loadInbox(0); });
        inbox.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !adjusting) onInboxSelected();
        });
        inbox.getConfirmButton().addActionListener(e -> confirm());
        inbox.getDisputeButton().addActionListener(e -> dispute());

        review.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !adjusting) onReviewSelected();
        });
        review.getAcceptButton().addActionListener(e -> decide(true));
        review.getRejectButton().addActionListener(e -> decide(false));
    }

    private void refreshAll() {
        try {
            adjusting = true;
            inbox.setRecipients(logService.getRecipients());
            adjusting = false;
            loadInbox(0);
            loadReview(0);
        } catch (RuntimeException ex) {
            adjusting = false;
            showError(ex);
        }
    }

    // ------------------------------------------------------------------------------------------
    // Halaman Log: Kirim Email
    // ------------------------------------------------------------------------------------------

    private void loadLog(ViolationRecord r) {
        try {
            logDialog.setHistory(workflow.riwayat(r.getId()));
        } catch (RuntimeException ex) {
            showError(ex);
        }
        updateSendButton(r);
    }

    private void updateSendButton(ViolationRecord r) {
        JButton b = logDialog.getSendButton();
        StatusPelanggaran s = r.getStatus();
        boolean can = s == StatusPelanggaran.BELUM_DIKIRIM || s == StatusPelanggaran.TERKIRIM;
        b.setText(s == StatusPelanggaran.TERKIRIM ? "Kirim Ulang" : "Kirim Email");
        b.setEnabled(can);
        b.setToolTipText(can ? null : "Email sudah ditanggapi; status log: " + s.getLabel());
    }

    /** Simulasi pengiriman: dialog "Mengirim..." sebentar, lalu status dan riwayat disimpan ke database. */
    private void sendEmail() {
        ViolationRecord r = printController.getCurrentRecord();
        if (r == null) return;

        JDialog progress = new JDialog(logDialog, "Mengirim Email", true);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        JLabel text = new JLabel("Mengirim log " + r.getKodeLog() + " ke email pemilik (simulasi)...");
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        p.add(text, BorderLayout.NORTH);
        p.add(bar, BorderLayout.CENTER);
        progress.setContentPane(p);
        progress.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        progress.pack();
        progress.setLocationRelativeTo(logDialog);

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                Thread.sleep(sendDelayMillis); // pura-pura menghubungi server surat
                return workflow.kirimEmail(r.getId());
            }
            @Override protected void done() { progress.dispose(); }
        };
        SwingUtilities.invokeLater(worker::execute); // dijalankan setelah dialog modal tampil
        progress.setVisible(true);

        try {
            String email = worker.get();
            JOptionPane.showMessageDialog(logDialog, "Email berhasil dikirim ke " + email + "\n(simulasi: tidak ada email sungguhan yang dikirim).",
                    "Kirim Email", JOptionPane.INFORMATION_MESSAGE);
        } catch (ExecutionException ex) {
            Throwable c = ex.getCause() == null ? ex : ex.getCause();
            JOptionPane.showMessageDialog(logDialog, c.getMessage(), "Kirim Email", JOptionPane.WARNING_MESSAGE);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        reloadCurrentLog(r.getId());
    }

    /** Menyegarkan halaman Log yang terbuka, tabel daftar, dan tab lain setelah status berubah. */
    private void reloadCurrentLog(long id) {
        ViolationRecord fresh = logService.findById(id);
        if (fresh != null) {
            printController.reloadCurrent(fresh);
            loadLog(fresh);
        }
        violationController.refreshViews();
        refreshAll();
    }

    // ------------------------------------------------------------------------------------------
    // Kotak Masuk (penerima)
    // ------------------------------------------------------------------------------------------

    /** Memuat pesan penerima terpilih; keepId > 0 mempertahankan pilihan baris. */
    private void loadInbox(long keepId) {
        String email = inbox.getSelectedRecipient();
        List<ViolationRecord> messages = email == null ? List.of() : logService.getInbox(email);
        adjusting = true;
        try {
            inbox.setMessages(messages);
            if (keepId > 0) inbox.selectById(keepId);
        } finally {
            adjusting = false;
        }
        ViolationRecord sel = inbox.getSelectedRecord();
        if (sel == null) inbox.clearDetail();
        else inbox.showMessage(sel, workflow.riwayat(sel.getId()));
    }

    private void onInboxSelected() {
        ViolationRecord r = inbox.getSelectedRecord();
        if (r == null) { inbox.clearDetail(); return; }
        try {
            // Membuka pesan = status Terkirim menjadi Diterima (tercatat di riwayat).
            if (r.getStatus() == StatusPelanggaran.TERKIRIM) {
                workflow.tandaiDiterima(r.getId());
                violationController.refreshViews();
            }
            loadInbox(r.getId());
        } catch (RuntimeException ex) {
            showError(ex);
            loadInbox(0);
        }
    }

    private void confirm() {
        ViolationRecord r = inbox.getSelectedRecord();
        if (r == null) return;
        int ok = JOptionPane.showConfirmDialog(frame,
                "Konfirmasi bahwa pelanggaran " + r.getKodeLog() + " benar terjadi?\nStatus log akan menjadi Selesai.",
                "Konfirmasi Pelanggaran", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            workflow.konfirmasi(r.getId());
        } catch (RuntimeException ex) {
            showError(ex);
        }
        afterResponse(r.getId());
    }

    private void dispute() {
        ViolationRecord r = inbox.getSelectedRecord();
        if (r == null) return;
        String reason = null;
        while (true) {
            reason = inbox.askDisputeReason(frame, reason);
            if (reason == null) return; // dibatalkan
            try {
                workflow.sanggah(r.getId(), reason);
                break;
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Sanggahan", JOptionPane.WARNING_MESSAGE);
                if (ex.getMessage() != null && ex.getMessage().contains("tidak ditemukan")) break;
                if (ex.getMessage() != null && ex.getMessage().contains("tidak dapat dilakukan")) break;
            } catch (RuntimeException ex) {
                showError(ex);
                break;
            }
        }
        afterResponse(r.getId());
    }

    private void afterResponse(long id) {
        violationController.refreshViews();
        loadInbox(id);
        loadReview(0);
    }

    // ------------------------------------------------------------------------------------------
    // Tinjau Sanggahan (operator)
    // ------------------------------------------------------------------------------------------

    private void loadReview(long keepId) {
        List<ViolationRecord> disputes = logService.getByStatus(StatusPelanggaran.DISANGGAH);
        adjusting = true;
        try {
            review.setRecords(disputes);
            if (keepId > 0) review.selectById(keepId);
        } finally {
            adjusting = false;
        }
        ViolationRecord sel = review.getSelectedRecord();
        if (sel == null) review.clearDetail();
        else review.showDispute(sel, lastDisputeReason(sel.getId()));
    }

    private void onReviewSelected() {
        ViolationRecord r = review.getSelectedRecord();
        if (r == null) review.clearDetail();
        else review.showDispute(r, lastDisputeReason(r.getId()));
    }

    private String lastDisputeReason(long id) {
        String reason = "";
        for (RiwayatLog h : workflow.riwayat(id)) if (h.getAksi() == RiwayatLog.Aksi.DISANGGAH) reason = h.getCatatan();
        return reason;
    }

    private void decide(boolean accept) {
        ViolationRecord r = review.getSelectedRecord();
        if (r == null) return;
        String title = accept ? "Terima Sanggahan" : "Tolak Sanggahan";
        String prompt = accept ? "Catatan untuk penerima (opsional):" : "Alasan penolakan (wajib diisi):";
        String note = null;
        while (true) {
            note = review.askNote(frame, title + " " + r.getKodeLog(), prompt);
            if (note == null) return;
            try {
                workflow.tinjau(r.getId(), accept, note);
                break;
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), title, JOptionPane.WARNING_MESSAGE);
                if (ex.getMessage() == null || !ex.getMessage().contains("wajib")) break;
            } catch (RuntimeException ex) {
                showError(ex);
                break;
            }
        }
        violationController.refreshViews();
        loadReview(0);
        loadInbox(0);
        JOptionPane.showMessageDialog(frame, "Keputusan tersimpan. Status log " + r.getKodeLog() + " menjadi Selesai.",
                title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(RuntimeException ex) {
        JOptionPane.showMessageDialog(frame, ex.getMessage(), "Database", JOptionPane.ERROR_MESSAGE);
    }
}