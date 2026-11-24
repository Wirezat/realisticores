package de.wirezat.realisticores.survey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.junit.jupiter.api.Test;

class KimberliteMapItemTest {
    @Test
    void mapIsCentredOnTheCrossAndKeepsItsScaleAcrossASave() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        MapItemSavedData data = KimberliteMapItem.mapData(-5003, 12345, RegistryAccess.EMPTY);
        assertTrue(Math.abs(data.centerX + 5003) < 16);
        assertTrue(Math.abs(data.centerZ - 12345) < 16);
        MapItemSavedData reloaded = MapItemSavedData.load(data.save(new CompoundTag(), RegistryAccess.EMPTY),
                RegistryAccess.EMPTY);
        assertEquals(data.scale, reloaded.scale);
        assertEquals(data.centerX, reloaded.centerX);
    }
}
