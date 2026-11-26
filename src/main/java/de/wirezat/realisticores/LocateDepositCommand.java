package de.wirezat.realisticores;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.wirezat.realisticores.coal.BasinType;
import de.wirezat.realisticores.coal.Coal;
import de.wirezat.realisticores.kimberlite.Kimberlite;
import de.wirezat.realisticores.kimberlite.Pipe;
import de.wirezat.realisticores.worldgen.LevelTerrainProbe;
import de.wirezat.realisticores.worldgen.Surveys;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/** Operator command that finds the nearest deposit, in the style of the vanilla locate command. */
public final class LocateDepositCommand {
    private static final int PIPE_START_RADIUS = 5000;
    private static final int BASIN_SEARCH_LIMIT = 100000;

    private LocateDepositCommand() {
    }

    private interface Search {
        /**
         * Finds the nearest deposit of one kind.
         *
         * @param level the world
         * @param origin position of the command source
         * @return centre of the deposit, Y has no meaning
         */
        Optional<BlockPos> nearest(ServerLevel level, BlockPos origin);
    }

    /**
     * Registers {@code /locate realisticores <deposit>}.
     *
     * @param dispatcher the server command dispatcher
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("locate")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal(RealisticOres.MOD_ID)
                        .then(target("kimberlite_pipe", LocateDepositCommand::nearestPipe))
                        .then(target("hard_coal", basin(BasinType.HARD_COAL)))
                        .then(target("lignite", basin(BasinType.LIGNITE)))
                        .then(target("anthracite", basin(BasinType.FOLDED)))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> target(String name, Search search) {
        Component title = Component.translatable("command.realisticores.locate." + name);
        return Commands.literal(name).executes(context -> locate(context, title, search));
    }

    private static int locate(CommandContext<CommandSourceStack> context, Component title, Search search) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        BlockPos origin = BlockPos.containing(source.getPosition());
        CompletableFuture.supplyAsync(() -> search.nearest(level, origin), Surveys.executor())
                .thenAcceptAsync(found -> found.ifPresentOrElse(pos -> success(source, title, origin, pos),
                        () -> source.sendFailure(
                                Component.translatable("commands.locate.structure.not_found", title))),
                        level.getServer())
                .whenComplete(Surveys::logFailure);
        return 1;
    }

    private static void success(CommandSourceStack source, Component title, BlockPos origin, BlockPos pos) {
        Component coordinates = ComponentUtils.wrapInSquareBrackets(
                Component.translatable("chat.coordinates", pos.getX(), "~", pos.getZ()))
                .withStyle(style -> style.withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                "/tp @s " + pos.getX() + " ~ " + pos.getZ()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("chat.coordinates.tooltip"))));
        int distance = (int) distance(origin, pos.getX(), pos.getZ());
        source.sendSuccess(() -> Component.translatable("commands.locate.structure.success", title, coordinates,
                distance), false);
    }

    private static Optional<BlockPos> nearestPipe(ServerLevel level, BlockPos origin) {
        if (level.dimension() != Level.OVERWORLD || !OreConfig.resolved().kimberlitePipeActive()) {
            return Optional.empty();
        }
        Optional<Pipe> nearest = Optional.empty();
        for (int radius = PIPE_START_RADIUS; nearest.isEmpty() && radius <= OreConfig.commandSearchLimit();
                radius *= 2) {
            int limit = radius;
            nearest = Kimberlite.generator().pipesNear(level.getSeed(), origin.getX() - radius,
                    origin.getZ() - radius, origin.getX() + radius, origin.getZ() + radius).stream()
                    .filter(pipe -> distance(origin, pipe.x(), pipe.z()) <= limit)
                    .min(Comparator.comparingDouble(pipe -> distance(origin, pipe.x(), pipe.z())));
        }
        return nearest.map(pipe -> new BlockPos(pipe.x(), 0, pipe.z()));
    }

    private static Search basin(BasinType type) {
        return (level, origin) -> {
            if (level.dimension() != Level.OVERWORLD || !OreConfig.resolved().coalBasinActive()) {
                return Optional.empty();
            }
            return LevelTerrainProbe.of(level).flatMap(probe -> Coal.nearest(level.getSeed(), probe, type,
                    origin.getX(), origin.getZ(), Math.min(BASIN_SEARCH_LIMIT, OreConfig.commandSearchLimit())))
                    .map(found -> new BlockPos(found.basin().x(), 0, found.basin().z()));
        };
    }

    private static double distance(BlockPos origin, int x, int z) {
        return Math.hypot(x - origin.getX(), z - origin.getZ());
    }
}
