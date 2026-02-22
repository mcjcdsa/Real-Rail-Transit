package com.real.rail.transit.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;

/**
 * 转辙器（道岔）方块
 * 道岔主体，包含连接部分、辙叉、护轨等组件
 */
public class TurnoutBlock extends Block {
    /**
     * 道岔位置状态：true为直向，false为侧向
     */
    public static final BooleanProperty IS_STRAIGHT = BooleanProperty.of("is_straight");
    
    /**
     * 道岔锁定状态：true为锁定，false为未锁定
     */
    public static final BooleanProperty IS_LOCKED = BooleanProperty.of("is_locked");
    
    public TurnoutBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
            .with(IS_STRAIGHT, true)
            .with(IS_LOCKED, false));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(IS_STRAIGHT, IS_LOCKED);
    }
}

