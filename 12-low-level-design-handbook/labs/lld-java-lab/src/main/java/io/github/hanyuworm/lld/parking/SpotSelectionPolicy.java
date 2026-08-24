package io.github.hanyuworm.lld.parking;

import java.util.Collection;
import java.util.Optional;

@FunctionalInterface
public interface SpotSelectionPolicy {
    Optional<ParkingSpot> select(Collection<ParkingSpot> spots, Vehicle vehicle);
}
