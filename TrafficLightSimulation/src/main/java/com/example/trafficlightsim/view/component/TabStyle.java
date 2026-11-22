package com.example.trafficlightsim.view.component;

import com.example.trafficlightsim.view.Theme;
import java.awt.*;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/** Tab berbentuk pil: tab aktif bergradasi merah, tab lain krem bergaris emas. */
public final class TabStyle {
    private TabStyle() { }

    public static void apply(JTabbedPane tabs) {
        tabs.setOpaque(false);
        tabs.setFont(Theme.BODY_BOLD);
        tabs.setUI(new PillTabsUI());
        tabs.setFocusable(false);
    }

    private static class PillTabsUI extends BasicTabbedPaneUI {
        @Override protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(9, 22, 9, 22);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            tabAreaInsets = new Insets(0, 0, 12, 0);
            contentBorderInsets = new Insets(0, 0, 0, 0);
        }

        @Override protected void paintTabBackground(Graphics g0, int placement, int index, int x, int y, int w, int h,
                                                    boolean selected) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int px = x + 3, pw = w - 6, arc = h;
            if (selected) {
                g.setPaint(new GradientPaint(0, y, Theme.RED, 0, y + h, Theme.MAROON));
                g.fillRoundRect(px, y, pw, h, arc, arc);
            } else {
                g.setColor(Theme.CARD);
                g.fillRoundRect(px, y, pw, h, arc, arc);
                g.setColor(Theme.INPUT_BORDER);
                g.drawRoundRect(px, y, pw - 1, h - 1, arc, arc);
            }
            g.dispose();
        }

        @Override protected void paintText(Graphics g, int placement, Font font, FontMetrics metrics, int index,
                                           String title, Rectangle textRect, boolean selected) {
            g.setFont(font);
            g.setColor(selected ? Color.WHITE : Theme.TEXT);
            g.drawString(title, textRect.x, textRect.y + metrics.getAscent());
        }

        @Override protected void paintTabBorder(Graphics g, int placement, int index, int x, int y, int w, int h,
                                                boolean selected) { }
        @Override protected void paintContentBorder(Graphics g, int placement, int selectedIndex) { }
        @Override protected void paintFocusIndicator(Graphics g, int placement, Rectangle[] rects, int index,
                                                     Rectangle iconRect, Rectangle textRect, boolean selected) { }
    }
}