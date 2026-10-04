package com.igormonasterio.samcats.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An open cardboard box. Cats walk in and sit inside it.
 * The CLOSED state is only used to draw the box worn by a sneaking player.
 */
public class CardboardBoxBlock extends Block {
    public static final BooleanProperty CLOSED = BooleanProperty.create("closed");

    private static final VoxelShape FLOOR = Block.box(0, 0, 0, 16, 1, 16);
    private static final VoxelShape OPEN = Shapes.or(FLOOR,
            Block.box(0, 0, 0, 16, 10, 1),
            Block.box(0, 0, 15, 16, 10, 16),
            Block.box(0, 0, 1, 1, 10, 15),
            Block.box(15, 0, 1, 16, 10, 15));

    public CardboardBoxBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(CLOSED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CLOSED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return state.getValue(CLOSED) ? Shapes.block() : OPEN;
    }

    // Only the floor collides, so cats (and players) can step inside.
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return state.getValue(CLOSED) ? Shapes.block() : FLOOR;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return !state.getValue(CLOSED);
    }
}
