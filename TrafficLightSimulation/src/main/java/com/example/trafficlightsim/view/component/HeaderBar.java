package com.example.trafficlightsim.view.component;

import com.example.trafficlightsim.view.Theme;
import java.awt.*;
import javax.swing.*;

/** Bilah judul bergradasi marun-merah dengan garis emas di tepi bawah, di bagian atas setiap layar. */
public class HeaderBar extends GradientPanel {
    public HeaderBar(String title) {
        super(Theme.MAROON_DEEP, Theme.RED, true);
        withAccent(Theme.GOLD);
        setBorder(Theme.padding(0, 32, 0, 32));
        setPreferredSize(new Dimension(10, 72));
        JLabel label = new JLabel(title);
        label.setFont(Theme.TITLE);
        label.setForeground(Color.WHITE);
        add(label, BorderLayout.CENTER);
    }

    /** Menaruh komponen (mis. tombol kembali) di sisi kanan. */
    public void setTrailing(JComponent c) {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        wrap.add(c);
        add(wrap, BorderLayout.EAST);
    }
}