package com.example.trafficlightsim;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    final List<String> violations = new ArrayList<>();
    final Movement movement;
    final MotorcycleVariant motorcycleVariant;
    double progress = -500; // starts outside the intersection; increases toward the center
    boolean passed;
    boolean turned;

    Vehicle(Direction direction, Type type, Random random) {
        this.direction = direction;
        this.approach = direction;
        this.type = type;
        Color[] colors = { new Color(225, 82, 70), new Color(50, 133, 204),
            new Color(244, 181, 55), new Color(94, 171, 112),
            new Color(139, 104, 190), new Color(239, 125, 64) };
        color = colors[random.nextInt(colors.length)];
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
