package de.wirezat.realisticores.coal;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Up to four pieces of coal lying on the ground. */
public class CoalPiecesBlock extends Block {
    public static final int MAX_PIECES = 4;
    public static final IntegerProperty PIECES = IntegerProperty.create("pieces", 1, MAX_PIECES);
    private static final VoxelShape[] SHAPES = {box(5.0, 0.0, 5.0, 11.0, 4.0, 11.0), box(3.0, 0.0, 3.0, 13.0, 4.0, 13.0),
            box(2.0, 0.0, 2.0, 14.0, 4.0, 14.0), box(2.0, 0.0, 2.0, 14.0, 4.0, 14.0)};

    private final Supplier<? extends Item> item;

    /**
     * Creates the block.
     *
     * @param item item that lays down one piece and drops from it
     * @param properties block properties
     */
    public CoalPiecesBlock(Supplier<? extends Item> item, BlockBehaviour.Properties properties) {
        super(properties);
        this.item = item;
        registerDefaultState(stateDefinition.any().setValue(PIECES, 1));
    }

    /**
     * Gives the item of one piece.
     *
     * @return item that lays down one piece
     */
    public Item item() {
        return item.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PIECES);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(PIECES) - 1];
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !canSurvive(state, level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return context.getItemInHand().is(item()) && state.getValue(PIECES) < MAX_PIECES;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState old = context.getLevel().getBlockState(context.getClickedPos());
        return old.is(this) ? old.setValue(PIECES, Math.min(MAX_PIECES, old.getValue(PIECES) + 1))
                : defaultBlockState();
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos,
            Player player) {
        return new ItemStack(item());
    }
}
