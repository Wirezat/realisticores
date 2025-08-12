package wirezat.realisticores.blocks.machines.cubicpress;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wirezat.realisticores.ModBlocks; // Import für ModBlocks

public class CubicPress extends Block implements BlockEntityProvider {

    public CubicPress(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CubicPressBlockEntity) {
                player.openHandledScreen((ExtendedScreenHandlerFactory) blockEntity);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        // Stellt sicher, dass das BlockEntity korrekt erstellt wird
        return new CubicPressBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        // Diese Methode teilt dem Spiel mit, welche Tick-Methode für Ihre Block-Entität aufgerufen werden soll.
        // Sie stellt sicher, dass nur die korrekte Block-Entität getickt wird.
        return checkType(type, ModBlocks.CUBIC_PRESS_BLOCK_ENTITY, (world1, pos, state1, blockEntity) -> CubicPressBlockEntity.tick(world1, pos, state1, (CubicPressBlockEntity) blockEntity));
    }

    // Hilfsmethode, um den BlockEntity-Typ zu überprüfen.
    // Dies ist eine gängige Methode in Minecraft, um sicherzustellen, dass der Ticker
    // nur für den richtigen BlockEntity-Typ registriert wird.
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> checkType(BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
    }
}
