package de.wirezat.realisticores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.wirezat.realisticores.coal.CoalBasin;
import de.wirezat.realisticores.kimberlite.KimberlitePipe;
import de.wirezat.realisticores.ore.ActiveMineral;
import de.wirezat.realisticores.ore.DepositRegistry;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OreConfigTest {
    @Test
    void depositWithOnlyAnAccompanyingOreEnabledGeneratesEvenWithoutSeamReplacements() {
        OreConfig.Resolved resolved = OreConfig.resolve(DepositRegistry.load(), List.of(), List.of("uranium"),
                List.of(), Map.of(), Map.of(CoalBasin.ID, Map.of("coal", "", "lignite", "", "anthracite", "")));
        assertTrue(resolved.active(CoalBasin.ID));
        assertFalse(resolved.active(KimberlitePipe.ID));
        assertEquals(List.of("uranium"), resolved.minerals(CoalBasin.ID).stream().map(ActiveMineral::ore).toList());
    }
}
