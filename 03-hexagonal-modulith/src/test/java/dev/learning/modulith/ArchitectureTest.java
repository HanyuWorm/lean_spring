package dev.learning.modulith;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ArchitectureTest {

    @Test
    void verifiesModuleBoundariesAndCycles() {
        ApplicationModules.of(CommerceApplication.class).verify();
    }
}

