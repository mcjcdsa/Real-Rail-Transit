package com.real.rail.transit.station;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 带灯天花板方块
 * 自带光源的车站照明天花板
 */
public class LitCeilingBlock extends Block {
    /**
     * 灯光状态：true为开启，false为关闭
     */
    public static final BooleanProperty LIT = BooleanProperty.of("lit");
    
    public LitCeilingBlock(Settings settings) {
        super(settings.luminance(state -> state.get(LIT) ? 15 : 0));
        this.setDefaultState(this.stateManager.getDefaultState().with(LIT, true));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }
    
    /**
     * 切换灯光状态
     */
    public void toggleLight(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.with(LIT, !state.get(LIT)));
    }
}



