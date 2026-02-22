package com.real.rail.transit.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * 第三轨供电方块
 * 第三轨供电设备，适用于地下和地面线路
 */
public class ThirdRailBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);
    
    /**
     * 供电状态：true为有电，false为断电
     */
    public static final BooleanProperty POWERED = BooleanProperty.of("powered");
    
    public ThirdRailBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(POWERED, true));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    /**
     * 设置供电状态
     */
    public void setPowered(World world, BlockPos pos, boolean powered) {
        world.setBlockState(pos, world.getBlockState(pos).with(POWERED, powered));
    }
    
    /**
     * 检查是否有电
     */
    public boolean isPowered(BlockState state) {
        return state.get(POWERED);
    }
}

