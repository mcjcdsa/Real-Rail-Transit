package com.real.rail.transit.station;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

/**
 * 自动扶梯方块
 * 车站倾斜式自动乘用扶梯
 */
public class AutomaticStaircaseBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    
    /**
     * 运行方向
     */
    public enum Direction implements StringIdentifiable {
        UP("up"),
        DOWN("down");
        
        private final String name;
        
        Direction(String name) {
            this.name = name;
        }
        
        @Override
        public String asString() {
            return this.name;
        }
    }
    
    /**
     * 运行状态：true为运行，false为停止
     */
    public static final BooleanProperty RUNNING = BooleanProperty.of("running");
    
    /**
     * 运行方向
     */
    public static final EnumProperty<Direction> DIRECTION = EnumProperty.of("direction", Direction.class);
    
    /**
     * 方块朝向
     */
    public static final DirectionProperty FACING = DirectionProperty.of("facing");
    
    public AutomaticStaircaseBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
            .with(RUNNING, true)
            .with(DIRECTION, Direction.UP));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(RUNNING, DIRECTION);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
}

