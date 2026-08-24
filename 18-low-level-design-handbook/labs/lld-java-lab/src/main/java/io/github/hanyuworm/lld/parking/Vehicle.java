package io.github.hanyuworm.lld.parking;

import java.util.Objects;

public record Vehicle(String licensePlate, VehicleType type) {
    public Vehicle {
        if (licensePlate == null || licensePlate.isBlank()) throw new IllegalArgumentException("licensePlate");
        Objects.requireNonNull(type, "type");
    }

    public enum VehicleType { MOTORCYCLE, CAR, TRUCK }
}
