package io.github.hanyuworm.lld.parking;

import java.util.Objects;
import java.util.Optional;

public final class ParkingSpot {
    private final String id;
    private final SpotType type;
    private Vehicle vehicle;

    public ParkingSpot(String id, SpotType type) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id");
        this.id = id;
        this.type = Objects.requireNonNull(type);
    }

    public String id() { return id; }
    public SpotType type() { return type; }
    public Optional<Vehicle> vehicle() { return Optional.ofNullable(vehicle); }
    public boolean isAvailable() { return vehicle == null; }

    public boolean canFit(Vehicle candidate) {
        return switch (type) {
            case MOTORCYCLE -> candidate.type() == Vehicle.VehicleType.MOTORCYCLE;
            case COMPACT -> candidate.type() != Vehicle.VehicleType.TRUCK;
            case LARGE -> true;
        };
    }

    void occupy(Vehicle candidate) {
        if (!isAvailable()) throw new IllegalStateException("Spot is occupied");
        if (!canFit(candidate)) throw new IllegalArgumentException("Vehicle does not fit");
        vehicle = candidate;
    }

    void release(String expectedPlate) {
        if (vehicle == null || !vehicle.licensePlate().equals(expectedPlate)) {
            throw new IllegalStateException("Vehicle does not own this spot");
        }
        vehicle = null;
    }

    public enum SpotType { MOTORCYCLE, COMPACT, LARGE }
}
