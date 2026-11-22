package com.example.trafficlightsim.view.component;

import com.example.trafficlightsim.view.Theme;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/** Jam dan tanggal realtime dari waktu sistem komputer (diperbarui tiap detik). Timer hanya berjalan saat tampil. */
public class ClockLabel extends JLabel {
    private static final Locale ID = Locale.forLanguageTag("id-ID");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss", ID);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", ID);
    private final Timer timer = new Timer(1000, e -> refresh());

    public ClockLabel() {
        super(" ", SwingConstants.RIGHT);
        setFont(Theme.font(Font.PLAIN, 12));
        setForeground(Theme.GOLD_LIGHT);
        refresh();
    }

    private void refresh() {
        LocalDateTime now = LocalDateTime.now();
        setText("<html><div style='text-align:right'><span style='font-size:18px;color:white'><b>" + TIME.format(now)
                + "</b></span><br>" + DATE.format(now) + "</div></html>");
    }

    @Override public void addNotify() { super.addNotify(); refresh(); timer.start(); }
    @Override public void removeNotify() { timer.stop(); super.removeNotify(); }
}