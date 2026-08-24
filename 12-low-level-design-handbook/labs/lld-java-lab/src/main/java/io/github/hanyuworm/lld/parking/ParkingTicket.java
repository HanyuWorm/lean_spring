package io.github.hanyuworm.lld.parking;

import java.time.Instant;

public record ParkingTicket(String ticketId, String spotId, String licensePlate, Instant enteredAt) {
}
