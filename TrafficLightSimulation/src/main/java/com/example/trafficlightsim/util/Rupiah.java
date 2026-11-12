package com.example.trafficlightsim.util;

import java.util.Locale;

/** Format angka rupiah seperti di mockup: Rp300.000 */
public final class Rupiah {
    private Rupiah() { }

    public static String format(int amount) {
        return "Rp" + String.format(Locale.US, "%,d", amount).replace(',', '.');
    }
}
