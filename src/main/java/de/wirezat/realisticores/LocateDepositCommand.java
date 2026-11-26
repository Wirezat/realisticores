package de.wirezat.realisticores;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.wirezat.realisticores.ore.DepositType;
import de.wirezat.realisticores.ore.Deposits;
import de.wirezat.realisticores.ore.LocateTarget;
import de.wirezat.realisticores.worldgen.LevelTerrainProbe;
import de.wirezat.realisticores.worldgen.Surveys;
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
    private LocateDepositCommand() {
    }

    /**
     * Registers {@code /locate realisticores <target>} with one target per search of every deposit.
     *
     * @param dispatcher the server command dispatcher
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> mod = Commands.literal(RealisticOres.MOD_ID);
        for (DepositType deposit : Deposits.ALL) {
            for (LocateTarget target : deposit.locateTargets()) {
                Component title = Component.translatable("command.realisticores.locate." + target.name());
                mod.then(Commands.literal(target.name())
                        .executes(context -> locate(context, title, deposit, target.search())));
            }
        }
        dispatcher.register(Commands.literal("locate").requires(source -> source.hasPermission(2)).then(mod));
    }

    /**
     * Starts a search in the background and reports its result to the command source.
     *
     * @param context the command context
     * @param title name of the target shown in the result
     * @param deposit the deposit the target belongs to
     * @param search the search of the target
     * @return command result, always 1 since the result arrives later
     */
    private static int locate(CommandContext<CommandSourceStack> context, Component title, DepositType deposit,
            LocateTarget.Search search) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        BlockPos origin = BlockPos.containing(source.getPosition());
        int limit = OreConfig.commandSearchLimit();
        CompletableFuture.supplyAsync(() -> level.dimension() == Level.OVERWORLD
                && OreConfig.resolved().active(deposit.id())
                ? LevelTerrainProbe.of(level).flatMap(probe -> search.nearest(level.getSeed(), probe, origin, limit))
                : Optional.<BlockPos>empty(), Surveys.executor())
                .thenAcceptAsync(found -> found.ifPresentOrElse(pos -> success(source, title, origin, pos),
                        () -> source.sendFailure(
                                Component.translatable("commands.locate.structure.not_found", title))),
                        level.getServer())
                .whenComplete(Surveys::logFailure);
        return 1;
    }

    /**
     * Reports a found deposit like the vanilla locate command, with clickable coordinates.
     *
     * @param source the command source
     * @param title name of the target
     * @param origin position the search started from
     * @param pos centre of the found deposit
     */
    private static void success(CommandSourceStack source, Component title, BlockPos origin, BlockPos pos) {
        Component coordinates = ComponentUtils.wrapInSquareBrackets(
                Component.translatable("chat.coordinates", pos.getX(), "~", pos.getZ()))
                .withStyle(style -> style.withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                "/tp @s " + pos.getX() + " ~ " + pos.getZ()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("chat.coordinates.tooltip"))));
        int distance = (int) Math.hypot(pos.getX() - origin.getX(), pos.getZ() - origin.getZ());
        source.sendSuccess(() -> Component.translatable("commands.locate.structure.success", title, coordinates,
                distance), false);
    }
}
