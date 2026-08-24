package io.github.hanyuworm.lld.vending;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class VendingMachineTest {
    @Test
    void dispensesAndReturnsChangeThroughAnExplicitTransition() {
        var machine = new VendingMachine();
        machine.refill("WATER", 100, 2);
        machine.insert(150);
        var result = assertInstanceOf(VendingMachine.Dispensed.class, machine.select("WATER"));
        assertEquals(50, result.changeCents());
        assertEquals(1, machine.stockOf("WATER"));
        assertEquals(VendingMachine.State.IDLE, machine.state());
    }

    @Test
    void rejectionDoesNotMutateStockOrCreditState() {
        var machine = new VendingMachine();
        machine.refill("WATER", 100, 1);
        machine.insert(50);
        var result = assertInstanceOf(VendingMachine.Rejected.class, machine.select("WATER"));
        assertEquals("INSUFFICIENT_CREDIT", result.reason());
        assertEquals(1, machine.stockOf("WATER"));
        assertEquals(VendingMachine.State.HAS_CREDIT, machine.state());
    }
}
