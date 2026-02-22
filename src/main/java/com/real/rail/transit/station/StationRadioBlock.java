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
 * 车站广播器方块
 * 播放到站提示、安全广播、调度广播
 */
public class StationRadioBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 8.0, 14.0);
    
    /**
     * 广播状态：true为播放中，false为停止
     */
    public static final BooleanProperty PLAYING = BooleanProperty.of("playing");
    
    public StationRadioBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(PLAYING, false));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PLAYING);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    /**
     * 播放广播
     */
    public void playBroadcast(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, String message) {
        BlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.with(PLAYING, true));
        // TODO: 播放音效和显示文本
    }
    
    /**
     * 停止广播
     */
    public void stopBroadcast(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.with(PLAYING, false));
    }
}

