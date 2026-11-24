package de.wirezat.realisticores.indicator;

import com.mojang.logging.LogUtils;
import de.wirezat.realisticores.kimberlite.Plume;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

/** Plays the ghostly trails that lead from a panned spot toward the source of the grains, one step at a time. */
public final class TrailEffects {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int STEP_TICKS = 10;
    private static final int LINGER_TICKS = 100;
    private static final int REPEAT_TICKS = 6;
    private static final double HEIGHT_ABOVE_GROUND = 0.3;
    private static final double RISE_SPEED = 0.01;

    private static final List<Trail> ACTIVE = new ArrayList<>();

    private TrailEffects() {
    }

    private static final class Trail {
        private final ServerPlayer player;
        private final ServerLevel level;
        private final List<Plume.GuidePoint> points;
        private int age;
        private int emitted;
        private int skipped;

        Trail(ServerPlayer player, ServerLevel level, List<Plume.GuidePoint> points) {
            this.player = player;
            this.level = level;
            this.points = points;
        }
    }

    /**
     * Starts a trail for a player; a ghostly flame appears at one point after the other, half a second apart,
     * leading from the player toward the source; each flame stays lit for several steps. A new trail replaces the running trail of the same player.
     *
     * @param player the player who sees the trail
     * @param level the world of the trail
     * @param points guide points ordered from the panned spot toward the pipe
     */
    public static void start(ServerPlayer player, ServerLevel level, List<Plume.GuidePoint> points) {
        LOGGER.info("TRAIL start player={} at {},{},{} points={}", player.getGameProfile().getName(),
                (int) player.getX(), (int) player.getY(), (int) player.getZ(), points.size());
        ACTIVE.removeIf(trail -> trail.player == player);
        if (!points.isEmpty()) {
            ACTIVE.add(new Trail(player, level, points));
        }
    }

    /**
     * Emits the next step of every running trail when its pause is over.
     *
     * @param event the server tick event
     */
    public static void tick(ServerTickEvent.Post event) {
        Iterator<Trail> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            Trail trail = iterator.next();
            int lastStep = (trail.points.size() - 1) * STEP_TICKS;
            if (trail.player.isRemoved() || trail.player.serverLevel() != trail.level
                    || trail.age > lastStep + LINGER_TICKS) {
                LOGGER.info("TRAIL end age={} emitted={} skipped={}", trail.age, trail.emitted, trail.skipped);
                iterator.remove();
                continue;
            }
            int newest = Math.min(trail.points.size() - 1, trail.age / STEP_TICKS);
            int oldest = Math.max(0, (trail.age - LINGER_TICKS) / STEP_TICKS);
            for (int i = oldest; i <= newest; i++) {
                int sinceReveal = trail.age - i * STEP_TICKS;
                if (sinceReveal >= 0 && sinceReveal < LINGER_TICKS && sinceReveal % REPEAT_TICKS == 0) {
                    emit(trail, trail.points.get(i));
                }
            }
            trail.age++;
        }
    }

    private static void emit(Trail trail, Plume.GuidePoint point) {
        BlockPos column = BlockPos.containing(point.x(), 0, point.z());
        if (!trail.level.hasChunkAt(column)) {
            trail.skipped++;
            return;
        }
        double y = trail.level.getHeight(Heightmap.Types.MOTION_BLOCKING, column.getX(), column.getZ())
                + HEIGHT_ABOVE_GROUND;
        if (trail.level.sendParticles(trail.player, ParticleTypes.SOUL_FIRE_FLAME, true, point.x(), y, point.z(), 0,
                0.0, 1.0, 0.0, RISE_SPEED)) {
            trail.emitted++;
        } else {
            trail.skipped++;
        }
    }
}
