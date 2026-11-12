package com.example.trafficlightsim.controller;

import com.example.trafficlightsim.DashboardFrame;
import com.example.trafficlightsim.model.ViolationRecord;
import com.example.trafficlightsim.service.LogPrinter;
import com.example.trafficlightsim.view.ReceiptDialog;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Tombol "Cetak Log" di setiap baris tabel (membuka halaman Log Pelanggaran)
 * serta tombol Download dan Kembali pada halaman Log.
 */
public class PrintController {
    private final DashboardFrame frame;
    private final LogPrinter printer;
    private final ReceiptDialog dialog;
    private ViolationRecord current;

    public PrintController(DashboardFrame frame, LogPrinter printer) {
        this.frame = frame;
        this.printer = printer;
        this.dialog = new ReceiptDialog(frame);
        frame.getListPanel().setOnPrintRow(this::showLog);
        dialog.getBackButton().addActionListener(e -> dialog.setVisible(false)); // kembali ke Daftar Pelanggaran
        dialog.getDownloadButton().addActionListener(e -> download());
    }

    /** Halaman 2 -> Halaman 4 untuk satu catatan. */
    private void showLog(ViolationRecord record) {
        current = record;
        dialog.setRecord(record);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true); // modal: kode berhenti di sini sampai dialog ditutup
    }

    private void download() {
        if (current == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Download log pelanggaran");
        FileNameExtensionFilter png = new FileNameExtensionFilter("Gambar PNG (*.png)", "png");
        FileNameExtensionFilter txt = new FileNameExtensionFilter("Teks (*.txt)", "txt");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(png);
        chooser.addChoosableFileFilter(txt);
        chooser.setFileFilter(png);
        chooser.setSelectedFile(new File(printer.suggestedName(current)));
        if (chooser.showSaveDialog(dialog) != JFileChooser.APPROVE_OPTION) return;

        boolean asTxt = chooser.getFileFilter() == txt;
        String ext = asTxt ? ".txt" : ".png";
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(ext)) file = new File(file.getPath() + ext);
        if (file.exists() && JOptionPane.showConfirmDialog(dialog, file.getName() + " sudah ada. Timpa?",
                "Download", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            if (asTxt) printer.saveTxt(current, file);
            else printer.savePng(dialog.getPanel(), file);
            JOptionPane.showMessageDialog(dialog, "Log tersimpan di:\n" + file.getAbsolutePath(),
                    "Download", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(dialog, "Gagal menyimpan log: " + ex.getMessage(),
                    "Download", JOptionPane.ERROR_MESSAGE);
        }
    }
}
