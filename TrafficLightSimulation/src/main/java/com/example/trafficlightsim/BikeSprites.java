package com.example.trafficlightsim;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.example.trafficlightsim.model.WarnaKendaraan;

/**
 * Gambar sepeda motor per (varian, warna). Aset PNG hanya punya satu warna badan per varian
 * (merah, biru, hitam), jadi badan motor (tangki dan kedua spakbor) diwarnai ulang ke warna kendaraan.
 * Pengendara, helm, ban, dan lampu tidak diubah. Hasil disimpan agar tiap kombinasi dibuat sekali.
 */
final class BikeSprites {
    private static final String PATH = "/com/example/trafficlightsim/assets/";
    private static final Map<String, Image> CACHE = new HashMap<>();
    // Titik di badan motor (tangki, spakbor depan, spakbor belakang) untuk memulai flood fill.
    private static final int[][] SEEDS = { {128, 130}, {128, 30}, {128, 275} };

    private BikeSprites() { }

    private static final class Spec {
        final String file; final int base; final int shade; final int tolerance;
        Spec(String file, int base, int shade, int tolerance) {
            this.file = file; this.base = base; this.shade = shade; this.tolerance = tolerance;
        }
    }

    private static Spec spec(Vehicle.MotorcycleVariant v) {
        switch (v) {
            case NO_HELMET: return new Spec("motorcycle-no-helmet.png", 0x2980B9, 0x2673A5, 24);
            case NOISY_EXHAUST: return new Spec("motorcycle-noisy-exhaust.png", 0x2D3436, 0x2A3032, 8);
            default: return new Spec("motorcycle-helmet.png", 0xC0392B, 0xAB3428, 24);
        }
    }

    /** Gambar motor berwarna; null jika file aset tidak ada (SimulationPanel memakai gambar Java2D cadangan). */
    static synchronized Image get(Vehicle.MotorcycleVariant variant, WarnaKendaraan color) {
        String key = variant + "/" + color;
        if (CACHE.containsKey(key)) return CACHE.get(key);
        Image img = null;
        try {
            Spec sp = spec(variant);
            java.net.URL url = BikeSprites.class.getResource(PATH + sp.file);
            if (url != null) img = recolor(ImageIO.read(url), sp, color.getColor());
        } catch (IOException | RuntimeException ex) {
            img = null;
        }
        CACHE.put(key, img);
        return img;
    }

    private static BufferedImage recolor(BufferedImage src, Spec sp, Color target) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        out.getGraphics().drawImage(src, 0, 0, null);
        boolean[] seen = new boolean[w * h];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        double baseLum = lum(sp.base);
        for (int[] s : SEEDS) {
            int sx = s[0] * w / 256, sy = s[1] * h / 384;
            if (isBody(out.getRGB(sx, sy), sp)) { queue.add(new int[] {sx, sy}); seen[sy * w + sx] = true; }
        }
        int[][] dirs = { {1, 0}, {-1, 0}, {0, 1}, {0, -1} };
        while (!queue.isEmpty()) {
            int[] p = queue.poll();
            int argb = out.getRGB(p[0], p[1]);
            double ratio = lum(argb) / baseLum;
            int r = clamp(target.getRed() * ratio), g = clamp(target.getGreen() * ratio), b = clamp(target.getBlue() * ratio);
            out.setRGB(p[0], p[1], (argb & 0xFF000000) | (r << 16) | (g << 8) | b);
            for (int[] d : dirs) {
                int nx = p[0] + d[0], ny = p[1] + d[1];
                if (nx < 0 || ny < 0 || nx >= w || ny >= h || seen[ny * w + nx]) continue;
                // Piksel yang sudah diwarnai ulang tidak dibaca lagi karena ditandai seen lebih dulu.
                if (isBody(src.getRGB(nx, ny), sp)) { seen[ny * w + nx] = true; queue.add(new int[] {nx, ny}); }
            }
        }
        return out;
    }

    private static boolean isBody(int argb, Spec sp) {
        if (((argb >>> 24) & 0xFF) < 40) return false; // transparan
        return dist(argb, sp.base) <= sp.tolerance || dist(argb, sp.shade) <= sp.tolerance;
    }

    private static double dist(int a, int b) {
        int dr = ((a >> 16) & 0xFF) - ((b >> 16) & 0xFF);
        int dg = ((a >> 8) & 0xFF) - ((b >> 8) & 0xFF);
        int db = (a & 0xFF) - (b & 0xFF);
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    private static double lum(int rgb) {
        return 0.299 * ((rgb >> 16) & 0xFF) + 0.587 * ((rgb >> 8) & 0xFF) + 0.114 * (rgb & 0xFF);
    }

    private static int clamp(double v) { return (int) Math.max(0, Math.min(255, Math.round(v))); }
}
