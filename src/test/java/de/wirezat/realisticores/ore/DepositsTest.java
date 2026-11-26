package de.wirezat.realisticores.ore;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.wirezat.realisticores.kimberlite.KimberlitePipe;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DepositsTest {
    @Test
    void shippedDataMatchesTheDepositKinds() {
        assertDoesNotThrow(() -> Deposits.validate(DepositRegistry.load(), Deposits.ALL));
    }

    @Test
    void aMineralInAnUnknownZoneIsRejected() {
        DepositRegistry registry = new DepositRegistry(Map.of(), Map.of(KimberlitePipe.ID,
                new Deposit(KimberlitePipe.ID, List.of(new Mineral("diamond", List.of("BRECIA"), 0.01, true, Map.of())))));
        assertThrows(IllegalStateException.class, () -> Deposits.validate(registry, List.of(KimberlitePipe.INSTANCE)));
    }
}
