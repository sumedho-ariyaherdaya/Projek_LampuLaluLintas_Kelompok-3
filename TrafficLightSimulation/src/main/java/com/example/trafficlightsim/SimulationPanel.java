package com.example.trafficlightsim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
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
        setToolTipText("Click a car or motorcycle to inspect it");
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
    
    private void updateSimulation() {
        long now = System.currentTimeMillis();
        if (now - phaseStarted >= phaseDuration()) {
        phase = (phase + 1) % 12;     
        phaseStarted = now;
    }

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
                    vehicle.progress = firstProgress - i * (length + 8 * scale);
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
                if (other != v && other.approach == v.approach && !other.passed
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

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        double scale = Math.min(w / 900.0, h / 540.0);
        double cx = w * 0.50, cy = h * 0.52;
        double road = Math.min(w * 0.25, h * 0.42);
        double half = road / 2;
        g.setColor(new Color(76, 128, 79)); g.fillRect(0, 0, w, h);
        g.setColor(new Color(255, 255, 255, 18));
        for (int x = 15; x < w; x += 46) for (int y = 15; y < h; y += 46) g.fillOval(x, y, 2, 2);
        g.setColor(ASPHALT); g.fillRect((int)(cx-half), 0, (int)road, h); g.fillRect(0, (int)(cy-half), w, (int)road);
        g.setColor(ROAD_MARK); g.setStroke(new BasicStroke(Math.max(1.5f, (float)(2*scale))));
        g.drawLine((int)cx, 0, (int)cx, (int)(cy-half-10*scale)); g.drawLine((int)cx, (int)(cy+half+10*scale), (int)cx, h);
        g.drawLine(0, (int)cy, (int)(cx-half-10*scale), (int)cy); g.drawLine((int)(cx+half+10*scale), (int)cy, w, (int)cy);
        drawCrosswalks(g, cx, cy, half, scale);
        drawSignals(g, cx, cy, half, scale);
        for (Vehicle v : vehicles) drawVehicle(g, v, cx, cy, scale);
        g.setColor(new Color(255,255,255,225)); g.setFont(new Font("SansSerif", Font.BOLD, Math.max(12,(int)(14*scale))));
        g.drawString("PERSIMPANGAN EMPAT ARAH", 18, 28);
        Vehicle.Direction active = Vehicle.Direction.values()[phase / 3];
        String stage = "ARAH " + namaArah(active).toUpperCase() + "  •  "
                 + (phase % 3 == 1 ? "HIJAU (durasi 20 dtk)" : "KUNING (durasi 1 dtk)");
        g.setFont(new Font("SansSerif", Font.PLAIN, Math.max(11,(int)(12*scale)))); g.drawString(stage, 18, 47);
        g.setColor(new Color(255,255,255,190)); g.drawString("Klik kendaraan untuk melihat hasil pemeriksaannya", 18, h-16);
        g.dispose();
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
        // Match the annotated compass positions in the simulation: N upper-left,
        // E upper-right, W lower-left, and S lower-right.
        double side = half + 14 * s;
        double shoulder = half + 40 * s;
        drawSignal(g, cx - side, cy - shoulder, Vehicle.Direction.NORTH, s);
        drawSignal(g, cx + shoulder, cy - shoulder, Vehicle.Direction.EAST, s);
        drawSignal(g, cx - side, cy + shoulder, Vehicle.Direction.WEST, s);
        drawSignal(g, cx + shoulder, cy + shoulder, Vehicle.Direction.SOUTH, s);
    }

    private void drawSignal(Graphics2D g, double x, double y, Vehicle.Direction approach, double s) {
        double width = 21 * s, height = 56 * s;
        double radius = 6.2 * s;
        double left = x - width / 2, top = y - height / 2;
        g.setColor(new Color(17, 21, 25));
        g.fillRoundRect((int) left, (int) top, (int) width, (int) height,
                (int) (7 * s), (int) (7 * s));
        g.setColor(new Color(92, 101, 108));
        g.setStroke(new BasicStroke(Math.max(1f, (float) (1.2 * s))));
        g.drawRoundRect((int) left, (int) top, (int) width, (int) height,
                (int) (7 * s), (int) (7 * s));

        Color[] active = {
            isRed(approach) ? new Color(255, 65, 65) : new Color(86, 36, 39),
            isYellow(approach) ? new Color(255, 207, 58) : new Color(83, 69, 37),
            isGreen(approach) ? new Color(62, 235, 119) : new Color(34, 75, 49)
        };
        for (int i = 0; i < active.length; i++) {
            double lampY = top + (12 + i * 16) * s;
            g.setColor(active[i]);
            g.fillOval((int) (x - radius), (int) (lampY - radius),
                    (int) (radius * 2), (int) (radius * 2));
            boolean lit = (i == 0 && isRed(approach))
                    || (i == 1 && isYellow(approach))
                    || (i == 2 && isGreen(approach));
            if (lit) {
                g.setColor(new Color(255, 255, 255, 105));
                g.fillOval((int) (x - radius * .48), (int) (lampY - radius * .62),
                        (int) (radius * .62), (int) (radius * .42));
            }
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