package com.example.trafficlightsim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

/** Java2D intersection, signals, and animated traffic. */
public class SimulationPanel extends JPanel {
    private static final Color ASPHALT = new Color(57, 65, 74);
    private static final Color ROAD_MARK = new Color(236, 232, 208);
    private static final Image MOTORCYCLE_HELMET = loadMotorcycleImage("motorcycle-helmet.png");
    private static final Image MOTORCYCLE_NO_HELMET = loadMotorcycleImage("motorcycle-no-helmet.png");
    private static final Image MOTORCYCLE_NOISY_EXHAUST = loadMotorcycleImage("motorcycle-noisy-exhaust.png");
    private final List<Vehicle> vehicles = new ArrayList<>();
    private final Random random = new Random();
    private final MusicPlayer musicPlayer = new MusicPlayer();
    private final Timer timer;
    private long pausedAt;
    private long phaseStarted = System.currentTimeMillis();
    private int phase = 0; // for each approach: yellow-before-green, green, yellow-before-red

    private static Image loadMotorcycleImage(String fileName) {
        String resource = "/com/example/trafficlightsim/assets/" + fileName;
        try {
            return ImageIO.read(SimulationPanel.class.getResource(resource));
        } catch (IOException | IllegalArgumentException ex) {
            return null; // Keep the simple Java2D motorcycle fallback if an asset is missing.
        }
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

    private void updateSimulation() {
        long now = System.currentTimeMillis();
        long duration = (phase % 3 == 1) ? 20_000 : 1_000;
        if (now - phaseStarted >= duration) {
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
            boolean occupied = vehicles.stream().anyMatch(v -> v.approach == d && !v.passed
                    && v.progress < -(half + 12 * scale) + 100 * scale);
            if (!occupied) {
                Vehicle vehicle = new Vehicle(d, random.nextInt(3) == 0 ? Vehicle.Type.MOTORCYCLE : Vehicle.Type.CAR, random);
                vehicle.progress = -Math.max(w, h) * 0.78;
                vehicles.add(vehicle);
            }
        }
        for (Vehicle v : vehicles) {
            double max = Math.max(w, h) * 0.65;
            // Left turns may proceed on red; straight and right-turn traffic wait for green.
            boolean mayEnter = isGreen(v.approach) || v.movement == Vehicle.Movement.LEFT;
            double vehicleLength = (v.type == Vehicle.Type.CAR ? 48 : 38) * scale;
            // Align the vehicle's front bumper exactly with the painted stop line.
            double stopAt = -(half + 12 * scale) - vehicleLength;
            double speed = (v.type == Vehicle.Type.CAR ? 2.0 : 2.6) * scale;
            if (!v.passed && !mayEnter) {
                if (v.progress < stopAt) v.progress = Math.min(stopAt, v.progress + speed);
                continue;
            }
            v.progress += speed;
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
        drawStopLines(g, cx, cy, half, scale);
        drawSignals(g, cx, cy, half, scale);
        for (Vehicle v : vehicles) drawVehicle(g, v, cx, cy, scale);
        g.setColor(new Color(255,255,255,225)); g.setFont(new Font("SansSerif", Font.BOLD, Math.max(12,(int)(14*scale))));
        g.drawString("FOUR-WAY INTERSECTION", 18, 28);
        Vehicle.Direction active = Vehicle.Direction.values()[phase / 3];
        String stage = active + " APPROACH  •  " + (phase % 3 == 1 ? "GREEN (20s)" : "YELLOW (1s)");
        g.setFont(new Font("SansSerif", Font.PLAIN, Math.max(11,(int)(12*scale)))); g.drawString(stage, 18, 47);
        g.setColor(new Color(255,255,255,190)); g.drawString("Click a vehicle to view its inspection", 18, h-16);
        g.dispose();
    }

    private void drawStopLines(Graphics2D g, double cx, double cy, double half, double s) {
        g.setColor(new Color(244,244,232));
        int thickness = Math.max(4,(int)(6*s));
        int lineLength = (int)(roadWidth(half) * 0.92);
        int offset = (int)(half + 12 * s);
        // Stop lines run across each incoming lane, just before the intersection.
        g.fillRect((int)(cx-lineLength/2.0), (int)(cy-offset-thickness/2.0), lineLength, thickness);
        g.fillRect((int)(cx-lineLength/2.0), (int)(cy+offset-thickness/2.0), lineLength, thickness);
        g.fillRect((int)(cx-offset-thickness/2.0), (int)(cy-lineLength/2.0), thickness, lineLength);
        g.fillRect((int)(cx+offset-thickness/2.0), (int)(cy-lineLength/2.0), thickness, lineLength);
    }
    private double roadWidth(double half) { return half * 2; }

    private void drawSignals(Graphics2D g, double cx, double cy, double half, double s) {
        // Signal arrows are painted directly on each incoming lane, pointing straight ahead.
        double offset = 27 * s;
        // Keep the arrows ahead of the stop line so waiting vehicles remain behind them.
        double distance = Math.max(20 * s, half - 24 * s);
        double size = 14 * s;
        drawSignal(g, cx + offset, cy - distance, Vehicle.Direction.NORTH, Vehicle.Direction.SOUTH, size);
        drawSignal(g, cx + distance, cy + offset, Vehicle.Direction.EAST, Vehicle.Direction.WEST, size);
        drawSignal(g, cx - offset, cy + distance, Vehicle.Direction.SOUTH, Vehicle.Direction.NORTH, size);
        drawSignal(g, cx - distance, cy - offset, Vehicle.Direction.WEST, Vehicle.Direction.EAST, size);
    }
    private void drawSignal(Graphics2D g, double x, double y, Vehicle.Direction approach, Vehicle.Direction arrow, double size) {
        Color color = isGreen(approach) ? new Color(58, 230, 115)
                : isYellow(approach) ? new Color(255, 202, 54) : new Color(244, 72, 72);
        // Dark outline keeps the colored pavement signal visible against vehicles and road markings.
        g.setColor(new Color(20, 25, 29, 190));
        drawArrow(g, x, y, size + 2, arrow);
        g.setColor(color);
        drawArrow(g, x, y, size, arrow);
    }
    private void drawArrow(Graphics2D g, double x, double y, double r, Vehicle.Direction d) {
        Path2D p = new Path2D.Double();
        switch(d) {
            case NORTH: p.moveTo(x,y-r);p.lineTo(x+r*.72,y);p.lineTo(x+r*.25,y);p.lineTo(x+r*.25,y+r*.62);p.lineTo(x-r*.25,y+r*.62);p.lineTo(x-r*.25,y);p.lineTo(x-r*.72,y);break;
            case SOUTH: p.moveTo(x,y+r);p.lineTo(x+r*.72,y);p.lineTo(x+r*.25,y);p.lineTo(x+r*.25,y-r*.62);p.lineTo(x-r*.25,y-r*.62);p.lineTo(x-r*.25,y);p.lineTo(x-r*.72,y);break;
            case EAST: p.moveTo(x+r,y);p.lineTo(x,y-r*.72);p.lineTo(x,y-r*.25);p.lineTo(x-r*.62,y-r*.25);p.lineTo(x-r*.62,y+r*.25);p.lineTo(x,y+r*.25);p.lineTo(x,y+r*.72);break;
            default: p.moveTo(x-r,y);p.lineTo(x,y-r*.72);p.lineTo(x,y-r*.25);p.lineTo(x+r*.62,y-r*.25);p.lineTo(x+r*.62,y+r*.25);p.lineTo(x,y+r*.25);p.lineTo(x,y+r*.72);
        }
        p.closePath(); g.fill(p);
    }

    private void drawVehicle(Graphics2D g, Vehicle v, double cx, double cy, double s) {
        Rectangle2D b = v.bounds(cx,cy,s);
        g.setColor(new Color(0,0,0,65)); g.fillRoundRect((int)b.getX()+2,(int)b.getY()+3,(int)b.getWidth(),(int)b.getHeight(),8,8);
        if (v.type == Vehicle.Type.MOTORCYCLE) {
            Image sprite = v.motorcycleVariant == Vehicle.MotorcycleVariant.NO_HELMET ? MOTORCYCLE_NO_HELMET
                    : v.motorcycleVariant == Vehicle.MotorcycleVariant.NOISY_EXHAUST ? MOTORCYCLE_NOISY_EXHAUST
                    : MOTORCYCLE_HELMET;
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

    private void showVehicleAt(int x, int y) {
        double s = Math.min(getWidth()/900.0,getHeight()/540.0), cx = getWidth()*.50, cy = getHeight()*.52;
        for (int i=vehicles.size()-1;i>=0;i--) {
            Vehicle v=vehicles.get(i);
            if (v.bounds(cx,cy,s).contains(x,y)) {
                String type=v.type==Vehicle.Type.CAR?"Mobil":"Sepeda motor";
                String direction=v.approach.toString().toLowerCase();
                String movement=v.movement==Vehicle.Movement.RIGHT?"belok kanan":v.movement==Vehicle.Movement.LEFT?"belok kiri":"lurus";
                String report=v.violations.isEmpty()?"Tidak terdeteksi":"• "+String.join("\n• ",v.violations);
                JOptionPane.showMessageDialog(this,type+" dari arah "+direction+" ("+movement+")\n\nPelanggaran:\n"+report,"Pemeriksaan kendaraan",JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
    }
}
