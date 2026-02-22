package com.real.rail.transit.station;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * 电梯方块
 * 垂直升降乘客电梯，支持多层运行
 */
public class ElevatorBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    
    /**
     * 电梯当前楼层
     */
    public static final IntProperty FLOOR = IntProperty.of("floor", 0, 100);
    
    /**
     * 电梯运行状态：0=停止，1=上升，2=下降
     */
    public static final IntProperty STATE = IntProperty.of("state", 0, 2);
    
    public ElevatorBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
            .with(FLOOR, 0)
            .with(STATE, 0));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FLOOR, STATE);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    /**
     * 调用电梯到指定楼层
     */
    public void callToFloor(World world, BlockPos pos, int targetFloor) {
        BlockState state = world.getBlockState(pos);
        int currentFloor = state.get(FLOOR);
        
        if (targetFloor > currentFloor) {
            world.setBlockState(pos, state.with(STATE, 1)); // 上升
        } else if (targetFloor < currentFloor) {
            world.setBlockState(pos, state.with(STATE, 2)); // 下降
        }
        // TODO: 实现电梯移动逻辑
    }
}

