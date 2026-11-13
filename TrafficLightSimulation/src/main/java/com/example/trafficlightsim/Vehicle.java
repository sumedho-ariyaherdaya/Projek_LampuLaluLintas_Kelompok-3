package com.example.trafficlightsim;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.example.trafficlightsim.model.JenisKendaraan;
import com.example.trafficlightsim.model.VehicleIdentity;

/** A small vehicle moving toward and through the intersection. */
final class Vehicle {
    enum Direction { NORTH, EAST, SOUTH, WEST }
    enum Type { CAR, MOTORCYCLE }
    enum Movement { STRAIGHT, RIGHT, LEFT }
    enum MotorcycleVariant { HELMET, NO_HELMET, NOISY_EXHAUST }

    Direction direction;
    final Direction approach;
    final Type type;
    final Color color;
    final VehicleIdentity identity; // identitas pasti: plat, pemilik, warna, merek
    final List<String> violations = new ArrayList<>();
    final Movement movement;
    final MotorcycleVariant motorcycleVariant;
    double progress = -500; // starts outside the intersection; increases toward the center
    boolean passed;
    boolean turned;

    Vehicle(Direction direction, VehicleIdentity identity, Random random) {
        this.direction = direction;
        this.approach = direction;
        this.identity = identity;
        this.type = identity.getKind() == JenisKendaraan.MOBIL ? Type.CAR : Type.MOTORCYCLE;
        color = identity.getColor().getColor();
        int routeChoice = random.nextInt(100);
        movement = routeChoice < 50 ? Movement.RIGHT
                : routeChoice < 80 ? Movement.STRAIGHT : Movement.LEFT;
        if (type == Type.MOTORCYCLE) {
            int variantChoice = random.nextInt(100);
            motorcycleVariant = variantChoice < 60 ? MotorcycleVariant.HELMET
                    : variantChoice < 82 ? MotorcycleVariant.NO_HELMET : MotorcycleVariant.NOISY_EXHAUST;
            if (motorcycleVariant == MotorcycleVariant.NO_HELMET) violations.add("Tidak menggunakan helm");
            if (motorcycleVariant == MotorcycleVariant.NOISY_EXHAUST) violations.add("Knalpot brong");
        } else {
            motorcycleVariant = null;
        }
        if (random.nextInt(6) == 0) violations.add("Tidak ada plat nomor");
    }

    /** Plat terlihat kecuali kendaraan ini sedang melanggar "Tidak ada plat nomor". */
    boolean hasVisiblePlate() { return !violations.contains("Tidak ada plat nomor"); }

    /** Sisi tujuan keluar persimpangan (arah datang NORTH + belok kanan = WEST, dst). */
    Direction destination() {
        int a = approach.ordinal(); // NORTH=0, EAST=1, SOUTH=2, WEST=3
        switch (movement) {
            case STRAIGHT: return Direction.values()[(a + 2) % 4];
            case RIGHT: return Direction.values()[(a + 3) % 4]; // N->W, E->N, S->E, W->S
            default: return Direction.values()[(a + 1) % 4];    // N->E, E->S, S->W, W->N
        }
    }

    Rectangle2D bounds(double cx, double cy, double scale) {
        double length = (type == Type.CAR ? 48 : 38) * scale;
        double width = (type == Type.CAR ? 25 : 23) * scale;
        double laneOffset = 27 * scale; // left-hand traffic lane, relative to direction of travel
        double x, y, w, h;
        switch (direction) {
            case NORTH: x = cx + laneOffset - width / 2; y = cy + progress; w = width; h = length; break;
            case SOUTH: x = cx - laneOffset - width / 2; y = cy - progress - length; w = width; h = length; break;
            case WEST: x = cx + progress; y = cy - laneOffset - width / 2; w = length; h = width; break;
            default: x = cx - progress - length; y = cy + laneOffset - width / 2; w = length; h = width;
        }
        return new Rectangle2D.Double(x, y, w, h);
    }
}
