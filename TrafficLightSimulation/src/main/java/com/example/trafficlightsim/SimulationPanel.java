package com.example.trafficlightsim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.example.trafficlightsim.model.Jalur;
import com.example.trafficlightsim.model.SignalState;
import com.example.trafficlightsim.model.VehicleIdentity;

/** Java2D intersection, signals, and animated traffic. */
public class SimulationPanel extends JPanel {
    private static final Color ASPHALT = new Color(57, 65, 74);
    private static final Color ROAD_MARK = new Color(236, 232, 208);
    private final List<Vehicle> vehicles = new ArrayList<>();
    private List<VehicleIdentity> vehiclePool = new ArrayList<>(); // kendaraan yang boleh muncul (dari database)
    private final Random random = new Random();
    private final MusicPlayer musicPlayer = new MusicPlayer();
    private final Timer timer;
    private long pausedAt;
    private long phaseStarted = System.currentTimeMillis();
    private final double[] pedestrianCrossingProgress = new double[4];
    private final boolean[] pedestrianCrossed = new boolean[4];
    private long pedestrianLastUpdated = System.currentTimeMillis();
    private JDialog inspectDialog; // jendela pemeriksaan kendaraan yang sedang terbuka (non-modal)
    private int phase = 0; // for each approach: yellow-before-green, green, yellow-before-red

    /** Mengisi daftar kendaraan yang akan muncul di simulasi (dipanggil DashboardFrame dari database). */
    public void setVehiclePool(List<VehicleIdentity> pool) { this.vehiclePool = new ArrayList<>(pool); }

    /** Memilih kendaraan acak yang ciri dan platnya belum ada di layar; null jika semua sedang tampil. */
    private VehicleIdentity pickAvailableIdentity() {
        List<VehicleIdentity> free = new ArrayList<>();
        for (VehicleIdentity id : vehiclePool) {
            boolean onScreen = false;
            for (Vehicle v : vehicles) {
                if (v.identity.traitKey().equals(id.traitKey()) || v.identity.getPlate().equals(id.getPlate())) {
                    onScreen = true;
                    break;
                }
            }
            if (!onScreen) free.add(id);
        }
        return free.isEmpty() ? null : free.get(random.nextInt(free.size()));
    }

    public SimulationPanel() {
        setBackground(new Color(98, 150, 101));
        setToolTipText("Klik kendaraan untuk melihat hasil pemeriksaannya");
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showVehicleAt(e.getX(), e.getY()); }
        });
        musicPlayer.start();
        timer = new Timer(35, e -> {
            updateSimulation();
            repaint();
        });
        timer.start();
    }

    /** Melanjutkan simulasi dan musik (tombol Mulai). Waktu saat berhenti tidak dihitung ke fase lampu. */
    public void start() {
        if (timer.isRunning()) return;
        phaseStarted += System.currentTimeMillis() - pausedAt;
        timer.start();
        musicPlayer.start();
    }

    /** Menghentikan simulasi dan musik (tombol Berhenti). Tampilan tetap membeku di keadaan terakhir. */
    public void stop() {
        if (!timer.isRunning()) return;
        pausedAt = System.currentTimeMillis();
        timer.stop();
        musicPlayer.stop();
    }

    public boolean isRunning() { return timer.isRunning(); }

    public SignalState getSignalState(Jalur jalur) {
        Vehicle.Direction d = Vehicle.Direction.values()[jalur.ordinal()];
        return isGreen(d) ? SignalState.GREEN : isYellow(d) ? SignalState.YELLOW : SignalState.RED;
    }
    public int getSecondsToNextChange() {
        long reference = timer.isRunning() ? System.currentTimeMillis() : pausedAt;
        long remaining = Math.max(0, Math.min(phaseDuration(), phaseDuration() - (reference - phaseStarted)));
        return (int) Math.max(1, Math.ceil(remaining / 1000.0));
    }
    private long phaseDuration() { return phaseDuration(phase); }
    private static long phaseDuration(int p) { return (p % 3 == 1) ? 20_000 : 1_000; }

    /**
     * Sisa detik sampai lampu jalur ini mulai hijau (dibulatkan ke atas); 0 jika sedang hijau.
     * Dihitung dari sisa fase sekarang ditambah lama fase-fase berikutnya sampai fase hijau jalur tersebut.
     */
    public int getSecondsToGreen(Jalur jalur) {
        int target = jalur.ordinal() * 3 + 1; // fase hijau jalur ini
        if (phase == target) return 0;
        long reference = timer.isRunning() ? System.currentTimeMillis() : pausedAt;
        long ms = Math.max(0, phaseDuration() - (reference - phaseStarted));
        for (int p = (phase + 1) % 12; p != target; p = (p + 1) % 12) ms += phaseDuration(p);
        return (int) Math.ceil(ms / 1000.0);
    }
    private static String namaArah(Vehicle.Direction d) { return Jalur.values()[d.ordinal()].getLabel(); }
    
    private void updatePedestrianCrossings(long now) {
        double elapsed = Math.max(0, Math.min(0.2, (now - pedestrianLastUpdated) / 1000.0));
        pedestrianLastUpdated = now;
        for (Vehicle.Direction direction : Vehicle.Direction.values()) {
            int index = direction.ordinal();
            if (isRed(direction)) {
                if (!pedestrianCrossed[index]) {
                    pedestrianCrossingProgress[index] = Math.min(1.0,
                            pedestrianCrossingProgress[index] + elapsed / 8.0);
                    pedestrianCrossed[index] = pedestrianCrossingProgress[index] >= 1.0;
                }
            } else {
                pedestrianCrossingProgress[index] = 0.0;
                pedestrianCrossed[index] = false;
            }
        }
    }
    private void updateSimulation() {
        long now = System.currentTimeMillis();
        if (now - phaseStarted >= phaseDuration()) {
        phase = (phase + 1) % 12;     
        phaseStarted = now;
    }

        updatePedestrianCrossings(now);
        int w = getWidth(), h = getHeight();
        double scale = Math.min(w / 900.0, h / 540.0);
        double cx = w * 0.50, cy = h * 0.52;
        double road = Math.min(w * 0.25, h * 0.42);
        double half = road / 2;
        if (random.nextInt(16) == 0 && vehicles.size() < 24) {
            Vehicle.Direction d = Vehicle.Direction.values()[random.nextInt(4)];
            boolean laneOccupied = vehicles.stream().anyMatch(v -> v.approach == d);
            if (!laneOccupied) {
                int roll = random.nextInt(100);
                int groupSize = roll < 30 ? 1 : roll < 80 ? 2 : 3;
                double firstProgress = -Math.max(w, h) * 0.78;
                for (int i = 0; i < groupSize && vehicles.size() < 24; i++) {
                    VehicleIdentity identity = pickAvailableIdentity();
                    if (identity == null) break;
                    Vehicle vehicle = new Vehicle(d, identity, random);
                    double length = (vehicle.type == Vehicle.Type.CAR ? 48 : 38) * scale;
                    vehicle.laneIndex = i == 1 ? 1 : 0;
                    vehicle.progress = i == 2 ? firstProgress - length - 8 * scale : firstProgress;
                    vehicles.add(vehicle);
                }
            }
        }
        for (Vehicle v : vehicles) {
            double max = Math.max(w, h) * 0.65;
            double vehicleLength = (v.type == Vehicle.Type.CAR ? 48 : 38) * scale;
            double stopAt = -(half + 44 * scale) - vehicleLength;
            // Keep a safe following distance from the closest vehicle ahead in this lane.
            Vehicle leader = null;
            for (Vehicle other : vehicles) {
                if (other != v && other.approach == v.approach && other.laneIndex == v.laneIndex && !other.passed
                        && other.progress > v.progress
                        && (leader == null || other.progress < leader.progress)) {
                    leader = other;
                }
            }
            double maxProgress = leader == null ? Double.POSITIVE_INFINITY
                    : leader.progress - vehicleLength - 8 * scale;
            boolean alreadyCrossedStopLine = v.progress > stopAt;
            boolean mayEnter = isGreen(v.approach) || v.movement == Vehicle.Movement.LEFT
                    || alreadyCrossedStopLine;
            if (!mayEnter) maxProgress = Math.min(maxProgress, stopAt);
            double speed = (v.type == Vehicle.Type.CAR ? 2.0 : 2.6) * scale;
            v.progress = Math.max(v.progress, Math.min(v.progress + speed, maxProgress));
            if (!v.passed && v.progress > -road / 2 + 40 * scale) {
                v.passed = true;
                if (v.movement != Vehicle.Movement.STRAIGHT) {
                    v.direction = outboundTrack(v.approach, v.movement);
                    v.progress = 0;
                    v.turned = true;
                    v.laneIndex = 0;
                }
            }
            if (v.progress > max) v.progress = max;
        }
        vehicles.removeIf(v -> v.progress >= Math.max(w, h) * 0.62);
    }

    /** Converts the requested exit side into the track used to draw the outbound vehicle. */
    private Vehicle.Direction outboundTrack(Vehicle.Direction approach, Vehicle.Movement movement) {
        if (movement == Vehicle.Movement.RIGHT) {
            // Requested right-turn routes: N→W, E→N, S→E, W→S.
            switch (approach) {
                case NORTH: return Vehicle.Direction.EAST;  // travel west
                case EAST: return Vehicle.Direction.SOUTH;  // travel north
                case SOUTH: return Vehicle.Direction.WEST;  // travel east
                default: return Vehicle.Direction.NORTH;    // travel south
            }
        }
        // Left turns take the opposite exit side.
        switch (approach) {
            case NORTH: return Vehicle.Direction.WEST;      // travel east
            case EAST: return Vehicle.Direction.NORTH;      // travel south
            case SOUTH: return Vehicle.Direction.EAST;      // travel west
            default: return Vehicle.Direction.SOUTH;        // travel north
        }
    }

    private boolean isGreen(Vehicle.Direction d) { return phase % 3 == 1 && d.ordinal() == phase / 3; }
    private boolean isYellow(Vehicle.Direction d) { return (phase % 3 != 1) && d.ordinal() == phase / 3; }
    
    // ===== Siang / malam mengikuti jam sistem =====
private static final Color DAY_GRASS = new Color(76, 128, 79);
private static final Color NIGHT_GRASS = new Color(30, 56, 44);
private static final int JAM_MALAM_MULAI = 18; // mulai malam 18.00
private static final int JAM_MALAM_SELESAI = 6; // malam berakhir 06.00

/** Hanya untuk uji coba: isi 0-23 untuk memaksa jam tertentu, -1 = pakai jam sistem. */
static int jamUji = -1;

private static boolean isNight() {
    int hour = jamUji >= 0 ? jamUji : LocalTime.now().getHour();
    return hour >= JAM_MALAM_MULAI || hour < JAM_MALAM_SELESAI;
}

/** Matahari dengan cahaya dan sinar yang berputar pelan; ditempatkan di rumput antara jalan dan toko kanan atas. */
private void drawSun(Graphics2D g, int w, double s) {
    double x = w * 0.73, y = 62 * s, r = 20 * s;
    g.setPaint(new RadialGradientPaint((float) x, (float) y, (float) (64 * s), new float[]{0f, 1f},
            new Color[]{new Color(255, 236, 140, 190), new Color(255, 236, 140, 0)}));
    g.fill(new Ellipse2D.Double(x - 64 * s, y - 64 * s, 128 * s, 128 * s));
    g.setStroke(new BasicStroke(Math.max(1.5f, (float) (2.5 * s)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g.setColor(new Color(255, 205, 60));
    
    double spin = System.currentTimeMillis() / 4000.0;
    for (int i = 0; i < 12; i++) {
        double a = spin + i * Math.PI / 6;
        g.drawLine((int) (x + Math.cos(a) * (r + 5 * s)), (int) (y + Math.sin(a) * (r + 5 * s)),
                   (int) (x + Math.cos(a) * (r + 13 * s)), (int) (y + Math.sin(a) * (r + 13 * s)));
    }
    
    g.setColor(new Color(255, 214, 70));
    g.fill(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
    g.setColor(new Color(255, 170, 30));
    g.draw(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
}

/** Bulan sabit dengan cahaya lembut dan bintang berkelip, di posisi yang sama dengan matahari. */
private void drawMoon(Graphics2D g, int w, double s) {
    double x = w * 0.73, y = 62 * s, r = 20 * s;
    g.setPaint(new RadialGradientPaint((float) x, (float) y, (float) (64 * s), new float[]{0f, 1f},
            new Color[]{new Color(200, 215, 255, 130), new Color(200, 215, 255, 0)}));
    g.fill(new Ellipse2D.Double(x - 64 * s, y - 64 * s, 128 * s, 128 * s));
    Area crescent = new Area(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
    crescent.subtract(new Area(new Ellipse2D.Double(x - r + 10 * s, y - r - 5 * s, 2 * r * 0.9, 2 * r * 0.9)));
    g.setColor(new Color(246, 244, 218));
    g.fill(crescent);
    
    double[][] stars = {{-72, 8}, {-45, -20}, {48, -14}, {70, 20}, {30, 36}, {-20, 34}};
    long t = System.currentTimeMillis();
    for (int i = 0; i < stars.length; i++) {
        int alpha = (int) (150 + 100 * Math.sin(t / 400.0 + i * 1.7));
        g.setColor(new Color(255, 255, 235, Math.max(40, Math.min(255, alpha))));
        double sz = Math.max(2, 3 * s);
        g.fill(new Ellipse2D.Double(x + stars[i][0] * s - sz / 2, y + stars[i][1] * s - sz / 2, sz, sz));
    }
}

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        double scale = Math.min(w / 900.0, h / 540.0);
        double cx = w * 0.50, cy = h * 0.52;
        double road = Math.min(w * 0.25, h * 0.42);
        double half = road / 2;
        boolean night = isNight(); // malam: 18.00-05.59, siang: 06.00-17.59 (jam sistem, sama dengan ClockLabel)
        g.setColor(night ? NIGHT_GRASS : DAY_GRASS); g.fillRect(0, 0, w, h);
        g.setColor(new Color(255, 255, 255, 18));
        for (int x = 15; x < w; x += 46) for (int y = 15; y < h; y += 46) g.fillOval(x, y, 2, 2);
        drawNeighborhood(g, w, h, scale);
        g.setColor(ASPHALT); g.fillRect((int)(cx-half), 0, (int)road, h); g.fillRect(0, (int)(cy-half), w, (int)road);
        g.setColor(ROAD_MARK); g.setStroke(new BasicStroke(Math.max(1.5f, (float)(2*scale))));
        g.drawLine((int)cx, 0, (int)cx, (int)(cy-half-10*scale)); g.drawLine((int)cx, (int)(cy+half+10*scale), (int)cx, h);
        g.drawLine(0, (int)cy, (int)(cx-half-10*scale), (int)cy); g.drawLine((int)(cx+half+10*scale), (int)cy, w, (int)cy);
        drawCrosswalks(g, cx, cy, half, scale);
        drawMedians(g, w, h, cx, cy, half, scale);                 // pembatas jalan (baru)
        for (Vehicle v : vehicles) drawVehicle(g, v, cx, cy, scale);
        drawSignals(g, cx, cy, half, scale);                       // lampu + tiang di atas kendaraan
        drawCrossingPedestrians(g, cx, cy, half, scale);
        
        //Suasana Simulasi Malam dan Siang hari
        if (night) {
    g.setColor(new Color(8, 14, 48, 120)); g.fillRect(0, 0, w, h); // suasana malam
    drawSignals(g, cx, cy, half, scale);                           // lampu lalu lintas tetap terang
    drawMoon(g, w, scale);
} else {
    drawSun(g, w, scale);

}
        g.setColor(new Color(255,255,255,225)); g.setFont(new Font("SansSerif", Font.BOLD, Math.max(12,(int)(14*scale))));
        g.drawString("PERSIMPANGAN EMPAT ARAH", 18, 28);
        Vehicle.Direction active = Vehicle.Direction.values()[phase / 3];
        String stage = "ARAH " + namaArah(active).toUpperCase() + "  •  "
                 + (phase % 3 == 1 ? "HIJAU (durasi 20 dtk)" : "KUNING (durasi 1 dtk)");
        g.setFont(new Font("SansSerif", Font.PLAIN, Math.max(11,(int)(12*scale)))); g.drawString(stage, 18, 47);
        g.setColor(new Color(255,255,255,190)); g.drawString("Klik kendaraan untuk melihat hasil pemeriksaannya", 18, h-16);
        g.dispose();
    }

    private void drawCrossingPedestrians(Graphics2D g, double cx, double cy, double half, double s) {
        double span = 2 * half + 16 * s;
        long tick = System.currentTimeMillis();
        for (Vehicle.Direction direction : Vehicle.Direction.values()) {
            if (!isRed(direction) || pedestrianCrossed[direction.ordinal()]) continue;
            double progress = pedestrianCrossingProgress[direction.ordinal()];
            double gait = tick / 260.0 + direction.ordinal() * 1.4;
            switch (direction) {
                case NORTH:
                    drawPedestrian(g, cx - half - 8 * s + progress * span,
                            cy - half - 19 * s, s, new Color(43, 92, 159), gait);
                    break;
                case SOUTH:
                    drawPedestrian(g, cx + half + 8 * s - progress * span,
                            cy + half + 19 * s, s, new Color(164, 71, 88), gait);
                    break;
                case EAST:
                    drawPedestrian(g, cx - half - 19 * s,
                            cy + half + 8 * s - progress * span, s,
                            new Color(48, 128, 94), gait);
                    break;
                case WEST:
                    drawPedestrian(g, cx + half + 19 * s,
                            cy - half - 8 * s + progress * span, s,
                            new Color(119, 78, 151), gait);
                    break;
            }
        }
    }
    private void drawNeighborhood(Graphics2D g, int w, int h, double s) {
        drawShop(g, 16 * s, 20 * s, 132 * s, 86 * s, "MINI MALL",
                new Color(239, 218, 179), new Color(172, 75, 55), s);
        drawShop(g, w - 148 * s, 20 * s, 132 * s, 86 * s, "MARKET",
                new Color(230, 222, 192), new Color(56, 125, 91), s);
        drawShop(g, 16 * s, h - 106 * s, 132 * s, 86 * s, "CAFE",
                new Color(237, 211, 184), new Color(143, 87, 58), s);
        drawShop(g, w - 148 * s, h - 106 * s, 132 * s, 86 * s, "BAKERY",
                new Color(236, 225, 191), new Color(178, 118, 43), s);

        long tick = System.currentTimeMillis();
        drawPedestrian(g, 182 * s, 122 * s, s, new Color(51, 90, 157), tick / 260.0);
        drawPedestrian(g, w - 184 * s, 122 * s, s, new Color(174, 67, 91), tick / 260.0 + 1.7);
        drawPedestrian(g, 182 * s, h - 43 * s, s, new Color(46, 119, 91), tick / 260.0 + 3.1);
        drawPedestrian(g, w - 184 * s, h - 43 * s, s, new Color(116, 75, 151), tick / 260.0 + 4.4);
    }

    private void drawShop(Graphics2D g, double x, double y, double width, double height,
                          String label, Color wall, Color accent, double s) {
        int ix = (int) x, iy = (int) y, iw = (int) width, ih = (int) height;
        int radius = Math.max(5, (int) (8 * s));
        g.setColor(new Color(0, 0, 0, 42));
        g.fillRoundRect(ix + (int) (3 * s), iy + (int) (4 * s), iw, ih, radius, radius);
        g.setColor(wall);
        g.fillRoundRect(ix, iy, iw, ih, radius, radius);
        g.setColor(accent);
        g.fillRoundRect(ix, iy, iw, (int) (21 * s), radius, radius);
        g.fillRect(ix, iy + (int) (14 * s), iw, (int) (7 * s));

        int signFont = Math.max(7, (int) (10 * s));
        g.setFont(new Font("SansSerif", Font.BOLD, signFont));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, ix + (iw - fm.stringWidth(label)) / 2,
                iy + (int) (14 * s));

        int windowY = iy + (int) (31 * s);
        int windowW = (int) (29 * s), windowH = (int) (24 * s);
        g.setColor(new Color(128, 193, 203));
        g.fillRoundRect(ix + (int) (12 * s), windowY, windowW, windowH, radius / 2, radius / 2);
        g.fillRoundRect(ix + iw - (int) (12 * s) - windowW, windowY, windowW, windowH, radius / 2, radius / 2);
        g.setColor(new Color(255, 255, 255, 125));
        g.drawLine(ix + (int) (17 * s), windowY + (int) (4 * s),
                ix + (int) (17 * s), windowY + windowH - (int) (4 * s));
        g.drawLine(ix + iw - (int) (17 * s), windowY + (int) (4 * s),
                ix + iw - (int) (17 * s), windowY + windowH - (int) (4 * s));

        g.setColor(new Color(105, 76, 56));
        g.fillRoundRect(ix + iw / 2 - (int) (10 * s), iy + ih - (int) (31 * s),
                (int) (20 * s), (int) (31 * s), radius / 2, radius / 2);
        g.setColor(new Color(241, 201, 102));
        g.fillOval(ix + iw / 2 + (int) (4 * s), iy + ih - (int) (17 * s),
                Math.max(2, (int) (3 * s)), Math.max(2, (int) (3 * s)));
    }

    private void drawPedestrian(Graphics2D g, double x, double y, double s, Color shirt, double phase) {
        double stride = Math.sin(phase) * 2.2 * s;
        double head = 5.5 * s;
        double bodyW = 6.5 * s, bodyH = 9 * s;
        g.setColor(new Color(0, 0, 0, 40));
        g.fillOval((int) (x - 5 * s), (int) (y + 15 * s), (int) (10 * s), (int) (3 * s));
        g.setColor(new Color(227, 187, 145));
        g.fillOval((int) (x - head / 2), (int) y, (int) head, (int) head);
        g.setColor(shirt);
        g.fillRoundRect((int) (x - bodyW / 2), (int) (y + 5 * s),
                (int) bodyW, (int) bodyH, (int) (3 * s), (int) (3 * s));
        g.setStroke(new BasicStroke(Math.max(1f, (float) (1.5 * s)), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g.drawLine((int) x, (int) (y + 13 * s), (int) (x - 2 * s + stride),
                (int) (y + 18 * s));
        g.drawLine((int) x, (int) (y + 13 * s), (int) (x + 2 * s - stride),
                (int) (y + 18 * s));
        g.drawLine((int) (x - 1 * s), (int) (y + 14 * s), (int) (x - 2 * s - stride),
                (int) (y + 19 * s));
        g.drawLine((int) (x + 1 * s), (int) (y + 14 * s), (int) (x + 2 * s + stride),
                (int) (y + 19 * s));
    }
    private void drawCrosswalks(Graphics2D g, double cx, double cy, double half, double s) {
        g.setColor(new Color(244, 244, 232));
        int stripe = Math.max(2, (int) (4 * s));
        int step = Math.max(stripe + 2, (int) Math.round(6 * s));
        int roadWidth = Math.max(1, (int) (2 * half));
        int outer = (int) (half + 36 * s);
        int inner = (int) (half + 2 * s);
        int bandLength = outer - inner;
        int count = Math.max(1, (roadWidth - stripe) / step + 1);

        // North and south: vertical stripes across the vertical road.
        for (int i = 0; i < count; i++) {
            int x = (int) (cx - half) + i * step;
            g.fillRect(x, (int) (cy - outer), stripe, bandLength);
            g.fillRect(x, (int) (cy + inner), stripe, bandLength);
        }
        // East and west: horizontal stripes across the horizontal road.
        for (int i = 0; i < count; i++) {
            int y = (int) (cy - half) + i * step;
            g.fillRect((int) (cx - outer), y, bandLength, stripe);
            g.fillRect((int) (cx + inner), y, bandLength, stripe);
        }
    }
        private void drawSignals(Graphics2D g, double cx, double cy, double half, double s) {
        double along = half + 26 * s;   // jarak lampu dari tengah simpang (di atas zebra cross)
        double across = 52 * s;         // geser dari garis tengah jalan ke tengah lajur datang
        double edge = half + 6 * s;     // posisi tiang di rumput pojok, tepat di luar tepi jalan
        // Tiap lampu digantung di lengan tiang yang menyilang jalan dari pojok rumput sisi lajur datangnya.
        // (lampu, arah lengan tiang ke pojok, arah ujung tiang menjauhi simpang)
        drawSignal(g, cx + across, cy - along, cx + edge,   cy - along,   0, -1, Vehicle.Direction.NORTH, s);
        drawSignal(g, cx + along,  cy + across, cx + along,  cy + edge,   1,  0, Vehicle.Direction.EAST,  s);
        drawSignal(g, cx - across, cy + along,  cx - edge,   cy + along,  0,  1, Vehicle.Direction.SOUTH, s);
        drawSignal(g, cx - along,  cy - across, cx - along,  cy - edge,  -1,  0, Vehicle.Direction.WEST,  s);
    }

    /**
     * Satu lampu dengan tiang berbentuk L: dari lampu menyamping ke pojok rumput (bx, by), lalu ke luar (ox, oy)
     * dan berakhir di dudukan bulat. Lampu diputar agar lampu merah ada di ujung yang menjauhi tiang.
     */
    private void drawSignal(Graphics2D g0, double x, double y, double bx, double by,
                            int ox, int oy, Vehicle.Direction approach, double s) {
        double width = 12 * s, height = 30 * s;   // ukuran kotak lampu (mobil = 25 x 48)
        double radius = 3.3 * s;                  // jari-jari tiap lampu
        double first = 6 * s, gap = 9 * s;        // posisi lampu pertama dan jarak antarlampu

        // Arah lengan tiang: dari lampu menuju (bx, by).
        double ax = Math.signum(bx - x), ay = Math.signum(by - y);
        double sx = x + ax * height / 2, sy = y + ay * height / 2;  // titik tiang keluar dari kotak lampu
        double stub = 14 * s;                                         // panjang ujung tiang ke luar
        double ex = bx + ox * stub, ey = by + oy * stub;
        double br = Math.max(3, 4.5 * s);

        Graphics2D m = (Graphics2D) g0.create();
        m.setStroke(new BasicStroke(Math.max(2f, (float) (3 * s)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        m.setColor(new Color(0, 0, 0, 55));                           // bayangan
        m.draw(new java.awt.geom.Path2D.Double() {{ moveTo(sx + 2 * s, sy + 2 * s); lineTo(bx + 2 * s, by + 2 * s); lineTo(ex + 2 * s, ey + 2 * s); }});
        m.setColor(new Color(78, 85, 93));                            // tiang
        m.draw(new java.awt.geom.Path2D.Double() {{ moveTo(sx, sy); lineTo(bx, by); lineTo(ex, ey); }});
        m.setColor(new Color(52, 58, 64));                            // dudukan
        m.fill(new Ellipse2D.Double(ex - br, ey - br, br * 2, br * 2));
        m.setColor(new Color(150, 158, 165));
        m.setStroke(new BasicStroke(Math.max(0.8f, (float) (0.9 * s))));
        m.draw(new Ellipse2D.Double(ex - br, ey - br, br * 2, br * 2));
        m.dispose();

        // Kotak lampu: merah -> kuning -> hijau searah lengan tiang (merah paling jauh dari tiang).
        Graphics2D g = (Graphics2D) g0.create();
        g.translate(x, y);
        switch (approach) {
            case NORTH: g.rotate(-Math.PI / 2); break;  // merah di kiri, hijau di kanan (dekat tiang)
            case SOUTH: g.rotate(Math.PI / 2); break;   // merah di kanan, hijau di kiri
            case WEST:  g.rotate(Math.PI); break;       // merah di bawah, hijau di atas
            default: break;                              // EAST: merah di atas, hijau di bawah
        }
        double left = -width / 2, top = -height / 2;
        g.setColor(new Color(17, 21, 25));
        g.fillRoundRect((int) left, (int) top, (int) Math.ceil(width), (int) Math.ceil(height),
                (int) (5 * s), (int) (5 * s));
        g.setColor(new Color(92, 101, 108));
        g.setStroke(new BasicStroke(Math.max(0.8f, (float) (1 * s))));
        g.drawRoundRect((int) left, (int) top, (int) Math.ceil(width), (int) Math.ceil(height),
                (int) (5 * s), (int) (5 * s));

        Color[] active = {
            isRed(approach) ? new Color(255, 65, 65) : new Color(86, 36, 39),
            isYellow(approach) ? new Color(255, 207, 58) : new Color(83, 69, 37),
            isGreen(approach) ? new Color(62, 235, 119) : new Color(34, 75, 49)
        };
        for (int i = 0; i < active.length; i++) {
            double lampY = top + first + i * gap;
            g.setColor(active[i]);
            g.fillOval((int) (-radius), (int) (lampY - radius), (int) (radius * 2), (int) (radius * 2));
            boolean lit = (i == 0 && isRed(approach))
                    || (i == 1 && isYellow(approach))
                    || (i == 2 && isGreen(approach));
            if (lit) {
                g.setColor(new Color(255, 255, 255, 105));
                g.fillOval((int) (-radius * .48), (int) (lampY - radius * .62),
                        (int) (radius * .62), (int) (radius * .42));
            }
        }
        g.dispose();
    }

    /** Pembatas jalan (median) berumput dan berpohon di garis tengah tiap jalan, mulai di luar zebra cross sampai tepi layar. */
    private void drawMedians(Graphics2D g, int w, int h, double cx, double cy, double half, double s) {
        double t = 24 * s;            // lebar pembatas
        double gap = half + 48 * s;   // jarak ujung dalam pembatas dari tengah simpang
        double over = 40 * s;         // diperpanjang melewati tepi layar agar ujung luar tidak membulat
        drawMedian(g, cx - t / 2, -over, t, (cy - gap) + over, true, true, s);              // utara
        drawMedian(g, cx - t / 2, cy + gap, t, (h - (cy + gap)) + over, true, false, s);    // selatan
        drawMedian(g, -over, cy - t / 2, (cx - gap) + over, t, false, true, s);             // barat
        drawMedian(g, cx + gap, cy - t / 2, (w - (cx + gap)) + over, t, false, false, s);   // timur
    }

    /** @param innerAtMax true jika ujung dalam (dekat simpang) ada di koordinat terbesar (utara dan barat). */
    private void drawMedian(Graphics2D g, double x, double y, double mw, double mh,
                            boolean vertical, boolean innerAtMax, double s) {
        double arc = Math.min(mw, mh);
        java.awt.geom.RoundRectangle2D.Double shape = new java.awt.geom.RoundRectangle2D.Double(x, y, mw, mh, arc, arc);
        g.setColor(new Color(92, 168, 62));                                   // rumput pembatas
        g.fill(shape);
        // Tepi trotoar: putih bergantian hitam.
        g.setStroke(new BasicStroke(Math.max(2f, (float) (3 * s))));
        g.setColor(new Color(235, 235, 225));
        g.draw(shape);
        g.setStroke(new BasicStroke(Math.max(2f, (float) (3 * s)), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                10f, new float[]{(float) (5 * s), (float) (5 * s)}, 0f));
        g.setColor(new Color(20, 20, 20));
        g.draw(shape);

        // Pohon-pohon di sepanjang pembatas, dihitung dari ujung dalam ke arah tepi layar.
        double min = vertical ? y : x, len = vertical ? mh : mw;
        double mid = vertical ? x + mw / 2 : y + mh / 2;
        for (double d = 16 * s; d < len - 8 * s; d += 22 * s) {
            double along = innerAtMax ? min + len - d : min + d;
            double px = vertical ? mid : along, py = vertical ? along : mid;
            double r = 8 * s;
            g.setColor(new Color(40, 104, 38));
            g.fill(new Ellipse2D.Double(px - r, py - r, 2 * r, 2 * r));
            g.setColor(new Color(66, 140, 48));
            g.fill(new Ellipse2D.Double(px - r * 0.75, py - r * 0.85, r * 1.1, r * 1.1));
            g.fill(new Ellipse2D.Double(px - r * 0.15, py - r * 0.1, r * 0.95, r * 0.95));
            g.setColor(new Color(110, 180, 70));
            g.fill(new Ellipse2D.Double(px - r * 0.5, py - r * 0.6, r * 0.45, r * 0.45));
        }
    }

    private boolean isRed(Vehicle.Direction d) {
        return !isGreen(d) && !isYellow(d);
    }
    private void drawVehicle(Graphics2D g, Vehicle v, double cx, double cy, double s) {
        Rectangle2D b = v.bounds(cx,cy,s);
        g.setColor(new Color(0,0,0,65)); g.fillRoundRect((int)b.getX()+2,(int)b.getY()+3,(int)b.getWidth(),(int)b.getHeight(),8,8);
        if (v.type == Vehicle.Type.MOTORCYCLE) {
            Image sprite = BikeSprites.get(v.motorcycleVariant, v.identity.getColor());
            if (sprite != null) {
                Graphics2D bikeGraphics = (Graphics2D) g.create();
                bikeGraphics.translate(b.getCenterX(), b.getCenterY());
                switch (v.direction) {
                    case NORTH: bikeGraphics.rotate(Math.PI); break;       // travels down
                    case WEST: bikeGraphics.rotate(Math.PI / 2); break;   // travels right
                    case EAST: bikeGraphics.rotate(-Math.PI / 2); break;  // travels left
                    default: break;                                      // travels up
                }
                int bikeWidth = Math.max(1, (int) (23 * s));
                int bikeLength = Math.max(1, (int) (38 * s));
                bikeGraphics.drawImage(sprite, -bikeWidth / 2, -bikeLength / 2, bikeWidth, bikeLength, null);
                bikeGraphics.dispose();
                return;
            }
        }
        if (v.type == Vehicle.Type.CAR) {
            boolean horizontal = v.direction == Vehicle.Direction.EAST || v.direction == Vehicle.Direction.WEST;
            Image sprite = CarSprites.get(v.identity, horizontal);
            if (sprite != null) {
                Graphics2D carGraphics = (Graphics2D) g.create();
                carGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                carGraphics.translate(b.getCenterX(), b.getCenterY());
                // The vertical sheet faces down and the horizontal sheet faces right.
                if (v.direction == Vehicle.Direction.SOUTH || v.direction == Vehicle.Direction.EAST) {
                    carGraphics.rotate(Math.PI);
                }
                int carWidth = Math.max(1, (int) Math.round((horizontal ? 48 : 25) * s));
                int carHeight = Math.max(1, (int) Math.round((horizontal ? 25 : 48) * s));
                carGraphics.drawImage(sprite, -carWidth / 2, -carHeight / 2,
                        carWidth, carHeight, null);
                carGraphics.dispose();
                return;
            }
        }
        g.setColor(v.color); g.fillRoundRect((int)b.getX(),(int)b.getY(),(int)b.getWidth(),(int)b.getHeight(),8,8);
        g.setColor(new Color(220,238,243));
        if (v.type == Vehicle.Type.CAR) {
            if (v.direction == Vehicle.Direction.NORTH || v.direction == Vehicle.Direction.SOUTH) g.fillRoundRect((int)b.getX()+3,(int)(b.getY()+b.getHeight()*.28),(int)b.getWidth()-6,(int)(b.getHeight()*.40),5,5);
            else g.fillRoundRect((int)(b.getX()+b.getWidth()*.28),(int)b.getY()+3,(int)(b.getWidth()*.40),(int)b.getHeight()-6,5,5);
        } else {
            g.setColor(Color.WHITE); g.fillOval((int)(b.getCenterX()-3*s),(int)(b.getCenterY()-3*s),(int)(6*s),(int)(6*s));
        }
    }

    /**
     * Menampilkan hasil pemeriksaan dalam jendela NON-modal: simulasi, form input, dan tombol lain tetap bisa dipakai
     * selagi jendela terbuka. Memeriksa kendaraan lain mengganti jendela sebelumnya (tidak menumpuk).
     */
    private void showInspection(String message) {
        if (inspectDialog != null) inspectDialog.dispose();
        JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = pane.createDialog(this, "Pemeriksaan kendaraan");
        dialog.setModalityType(Dialog.ModalityType.MODELESS);
        dialog.setAlwaysOnTop(false);
        // Diletakkan di sudut kiri atas area simulasi agar tidak menutupi form di panel kanan.
        if (isShowing()) {
            Point origin = getLocationOnScreen();
            dialog.setLocation(origin.x + 16, origin.y + 56);
        }
        inspectDialog = dialog;
        dialog.setVisible(true);
    }

    private void showVehicleAt(int x, int y) {
        double s = Math.min(getWidth()/900.0,getHeight()/540.0), cx = getWidth()*.50, cy = getHeight()*.52;
        for (int i=vehicles.size()-1;i>=0;i--) {
            Vehicle v=vehicles.get(i);
            if (v.bounds(cx,cy,s).contains(x,y)) {
                String type=v.type==Vehicle.Type.CAR?"Mobil":"Sepeda motor";
                String movement=v.movement==Vehicle.Movement.RIGHT?"belok kanan":v.movement==Vehicle.Movement.LEFT?"belok kiri":"lurus";
                String plate=v.hasVisiblePlate()?v.identity.getPlate():"Tidak terlihat (tanpa plat nomor)";
                String report=v.violations.isEmpty()?"Tidak terdeteksi":"• "+String.join("\n• ",v.violations);
                showInspection(type+"\n"
                        +"Plat nomor: "+plate+"\n"
                        +"Merek: "+v.identity.getBrand()+"\n"
                        +"Warna: "+v.identity.getColor().getLabel()+"\n\n"
                        +"Arah datang: "+namaArah(v.approach)+"\n"
                        +"Tujuan: "+namaArah(v.destination())+" ("+movement+")\n\n"
                        +"Pelanggaran:\n"+report);
                return;
            }
        }
    }
}