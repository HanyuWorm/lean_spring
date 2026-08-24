package io.github.hanyuworm.lld.parking;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ParkingLot {
    private final Map<String, ParkingSpot> spots;
    private final Map<String, ParkingTicket> activeTickets = new LinkedHashMap<>();
    private final SpotSelectionPolicy selectionPolicy;
    private final Clock clock;

    public ParkingLot(List<ParkingSpot> spots, SpotSelectionPolicy selectionPolicy, Clock clock) {
        this.spots = new LinkedHashMap<>();
        spots.forEach(spot -> {
            if (this.spots.put(spot.id(), spot) != null) throw new IllegalArgumentException("Duplicate spot");
        });
        this.selectionPolicy = Objects.requireNonNull(selectionPolicy);
        this.clock = Objects.requireNonNull(clock);
    }

    public synchronized ParkingTicket park(String ticketId, Vehicle vehicle) {
        if (ticketId == null || ticketId.isBlank()) throw new IllegalArgumentException("ticketId");
        if (activeTickets.containsKey(ticketId)) throw new DuplicateTicketException();
        if (activeTickets.values().stream().anyMatch(ticket -> ticket.licensePlate().equals(vehicle.licensePlate()))) {
            throw new AlreadyParkedException();
        }
        var spot = selectionPolicy.select(spots.values(), vehicle).orElseThrow(LotFullException::new);
        spot.occupy(vehicle);
        var ticket = new ParkingTicket(ticketId, spot.id(), vehicle.licensePlate(), Instant.now(clock));
        activeTickets.put(ticketId, ticket);
        return ticket;
    }

    public synchronized void exit(String ticketId) {
        var ticket = activeTickets.remove(ticketId);
        if (ticket == null) throw new UnknownTicketException();
        spots.get(ticket.spotId()).release(ticket.licensePlate());
    }

    public synchronized int activeTicketCount() { return activeTickets.size(); }

    public static final class LotFullException extends RuntimeException {}
    public static final class AlreadyParkedException extends RuntimeException {}
    public static final class DuplicateTicketException extends RuntimeException {}
    public static final class UnknownTicketException extends RuntimeException {}
}
