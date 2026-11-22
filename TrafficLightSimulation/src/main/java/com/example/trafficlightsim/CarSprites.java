package com.example.trafficlightsim;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import com.example.trafficlightsim.model.VehicleIdentity;
import com.example.trafficlightsim.model.WarnaKendaraan;

/** Brand/color car sprites cropped from separate horizontal and vertical sheets. */
final class CarSprites {
    private static final String HORIZONTAL_RESOURCE =
            "/com/example/trafficlightsim/assets/car-sprites-horizontal.png";
    private static final String VERTICAL_RESOURCE =
            "/com/example/trafficlightsim/assets/car-sprites-vertical.png";
    private static final Map<String, SpritePair> SPRITES = new HashMap<>();

    private static final class SpritePair {
        final BufferedImage horizontal;
        final BufferedImage vertical;

        SpritePair(BufferedImage horizontal, BufferedImage vertical) {
            this.horizontal = horizontal;
            this.vertical = vertical;
        }
    }

    static {
        BufferedImage horizontal = load(HORIZONTAL_RESOURCE);
        BufferedImage vertical = load(VERTICAL_RESOURCE);
        if (horizontal != null && vertical != null) {
            add(horizontal, vertical, "AVNZ", WarnaKendaraan.MERAH,
                    new int[] {30, 95, 240, 140}, new int[] {115, 30, 150, 260});
            add(horizontal, vertical, "AVNZ", WarnaKendaraan.BIRU,
                    new int[] {30, 235, 240, 140}, new int[] {270, 30, 155, 260});
            add(horizontal, vertical, "AVNZ", WarnaKendaraan.HIJAU,
                    new int[] {30, 370, 240, 150}, new int[] {424, 30, 155, 260});

            add(horizontal, vertical, "TYT", WarnaKendaraan.KUNING,
                    new int[] {276, 34, 242, 137}, new int[] {38, 286, 155, 244});
            add(horizontal, vertical, "TYT", WarnaKendaraan.UNGU,
                    new int[] {276, 169, 242, 138}, new int[] {196, 286, 155, 244});
            add(horizontal, vertical, "TYT", WarnaKendaraan.MERAH,
                    new int[] {276, 306, 242, 132}, new int[] {346, 286, 150, 244});
            add(horizontal, vertical, "TYT", WarnaKendaraan.BIRU,
                    new int[] {276, 437, 242, 155}, new int[] {505, 286, 165, 244});

            add(horizontal, vertical, "INNOV", WarnaKendaraan.HIJAU,
                    new int[] {525, 96, 259, 143}, new int[] {108, 525, 150, 270});
            add(horizontal, vertical, "INNOV", WarnaKendaraan.OREN,
                    new int[] {525, 237, 259, 143}, new int[] {265, 525, 155, 270});
            add(horizontal, vertical, "INNOV", WarnaKendaraan.KUNING,
                    new int[] {525, 374, 259, 146}, new int[] {420, 525, 160, 270});
        }
    }

    private CarSprites() {}

    static BufferedImage get(VehicleIdentity identity, boolean horizontal) {
        if (identity == null || identity.getColor() == null || identity.getBrand() == null) return null;
        String brand = normalizeBrand(identity.getBrand());
        SpritePair pair = SPRITES.get(brand + "|" + identity.getColor().name());
        if (pair == null) return null;
        return horizontal ? pair.horizontal : pair.vertical;
    }

    private static BufferedImage load(String resource) {
        try (InputStream input = CarSprites.class.getResourceAsStream(resource)) {
            return input == null ? null : ImageIO.read(input);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static void add(BufferedImage horizontalSheet, BufferedImage verticalSheet,
                            String brand, WarnaKendaraan color,
                            int[] horizontalCrop, int[] verticalCrop) {
        BufferedImage horizontal = crop(horizontalSheet, horizontalCrop);
        BufferedImage vertical = crop(verticalSheet, verticalCrop);
        if (horizontal != null && vertical != null) {
            SPRITES.put(brand + "|" + color.name(), new SpritePair(horizontal, vertical));
        }
    }

    private static BufferedImage crop(BufferedImage sheet, int[] region) {
        int x = region[0], y = region[1], width = region[2], height = region[3];
        if (x < 0 || y < 0 || x + width > sheet.getWidth() || y + height > sheet.getHeight()) return null;
        return sheet.getSubimage(x, y, width, height);
    }

    private static String normalizeBrand(String value) {
        String brand = value.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        if (brand.equals("AVANZA") || brand.equals("AVNZ")) return "AVNZ";
        if (brand.equals("TOYOTA")) return "TYT";
        if (brand.equals("INNOVA")) return "INNOV";
        return brand;
    }
}
