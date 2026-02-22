package com.real.rail.transit.station;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

/**
 * 小电视方块
 * 车站小型信息显示终端，播放广告、新闻等公共信息
 */
public class SmallTVBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 10.0, 15.0);
    
    /**
     * 播放状态：true为播放中，false为关闭
     */
    public static final BooleanProperty PLAYING = BooleanProperty.of("playing");
    
    public SmallTVBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
            .with(PLAYING, false));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PLAYING);
        // Content URL stored in BlockEntity (to be implemented)
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    /**
     * 播放内容
     * TODO: Store URL in BlockEntity
     */
    public void playContent(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, String url) {
        BlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.with(PLAYING, true));
        // TODO: Store URL in BlockEntity and implement content playback logic
    }
    
    /**
     * 停止播放
     */
    public void stopPlaying(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.with(PLAYING, false));
    }
}

