package com.example.trafficlightsim.service;

import com.example.trafficlightsim.model.ViolationRecord;
import com.example.trafficlightsim.model.ViolationType;
import com.example.trafficlightsim.util.Rupiah;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;

/** Menyimpan log pelanggaran ke file: gambar PNG (tampilan lembar apa adanya) atau teks TXT. */
public class LogPrinter {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Nama file yang disarankan, mis. "log-pelanggaran-B-5126-AA". */
    public String suggestedName(ViolationRecord r) {
        String plate = r.getPlate().replaceAll("[^A-Za-z0-9]+", "-").replaceAll("^-|-$", "");
        return "log-pelanggaran-" + (plate.isEmpty() ? "tanpa-plat" : plate);
    }

    /** Menyimpan gambar komponen (ReceiptPanel) sebagai PNG. */
    public void savePng(Component sheet, File file) throws IOException {
        BufferedImage img = new BufferedImage(sheet.getWidth(), sheet.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        sheet.paint(g);
        g.dispose();
        if (!ImageIO.write(img, "png", file)) throw new IOException("Format PNG tidak didukung");
    }

    /** Menyimpan isi log sebagai teks biasa. */
    public void saveTxt(ViolationRecord r, File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("BUKTI PELANGGARAN LAMPU LALU LINTAS\n");
        sb.append("===================================\n");
        sb.append("Nama            : \n");
        sb.append("Plat Kendaraan  : ").append(r.getPlate()).append('\n');
        sb.append("Pelanggaran     :\n");
        for (ViolationType t : r.getViolations()) {
            sb.append("  - ").append(t.getLabel()).append(" (").append(Rupiah.format(t.getFine())).append(")\n");
        }
        sb.append("Total denda     : ").append(Rupiah.format(r.getTotalFine())).append('\n');
        sb.append("Waktu           : ").append(TIME.format(r.getTime())).append('\n');
        sb.append("Jalur           : ").append(r.getJalur().getLabel()).append('\n');
        sb.append("Jenis Kendaraan : ").append(r.getVehicleKind().getLabel()).append('\n');
        Files.write(file.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}
