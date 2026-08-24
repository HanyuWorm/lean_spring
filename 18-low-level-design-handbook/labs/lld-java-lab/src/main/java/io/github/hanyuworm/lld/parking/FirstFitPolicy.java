package io.github.hanyuworm.lld.parking;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

public final class FirstFitPolicy implements SpotSelectionPolicy {
    @Override
    public Optional<ParkingSpot> select(Collection<ParkingSpot> spots, Vehicle vehicle) {
        return spots.stream()
                .filter(ParkingSpot::isAvailable)
                .filter(spot -> spot.canFit(vehicle))
                .min(Comparator.comparing(ParkingSpot::id));
    }
}
