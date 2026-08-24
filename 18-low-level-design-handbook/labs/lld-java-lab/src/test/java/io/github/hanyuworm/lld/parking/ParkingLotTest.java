package io.github.hanyuworm.lld.parking;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParkingLotTest {
    private final Instant now = Instant.parse("2026-08-25T00:00:00Z");

    @Test
    void allocatesTheFirstCompatibleSpotAndReleasesItOnExit() {
        var lot = lot();
        var ticket = lot.park("T-1", new Vehicle("51A-1", Vehicle.VehicleType.CAR));
        assertEquals("C-1", ticket.spotId());
        assertEquals(now, ticket.enteredAt());
        lot.exit(ticket.ticketId());
        assertEquals(0, lot.activeTicketCount());
    }

    @Test
    void preventsOneVehicleFromOwningTwoActiveTickets() {
        var lot = lot();
        var car = new Vehicle("51A-1", Vehicle.VehicleType.CAR);
        lot.park("T-1", car);
        assertThrows(ParkingLot.AlreadyParkedException.class, () -> lot.park("T-2", car));
    }

    @Test
    void duplicateTicketIdCannotReplaceAnExistingTicket() {
        var lot = lot();
        lot.park("T-1", new Vehicle("51A-1", Vehicle.VehicleType.CAR));
        assertThrows(ParkingLot.DuplicateTicketException.class,
                () -> lot.park("T-1", new Vehicle("BIKE-1", Vehicle.VehicleType.MOTORCYCLE)));
        assertEquals(1, lot.activeTicketCount());
    }

    @Test
    void rejectsTruckWhenNoLargeSpotExists() {
        var lot = lot();
        assertThrows(ParkingLot.LotFullException.class,
                () -> lot.park("T-1", new Vehicle("TRUCK-1", Vehicle.VehicleType.TRUCK)));
    }

    private ParkingLot lot() {
        return new ParkingLot(List.of(
                new ParkingSpot("M-1", ParkingSpot.SpotType.MOTORCYCLE),
                new ParkingSpot("C-1", ParkingSpot.SpotType.COMPACT)
        ), new FirstFitPolicy(), Clock.fixed(now, ZoneOffset.UTC));
    }
}
