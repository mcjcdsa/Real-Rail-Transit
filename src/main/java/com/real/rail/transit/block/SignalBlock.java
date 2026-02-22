package com.real.rail.transit.block;

import com.real.rail.transit.block.entity.SignalBlockEntity;
import com.real.rail.transit.system.SignalSystem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 信号机方块
 * 轨道信号控制设备，多状态灯光显示
 */
public class SignalBlock extends Block {
    /**
     * 信号状态枚举
     */
    public enum SignalState implements StringIdentifiable {
        RED("red"),           // 红灯 - 禁止通行
        YELLOW("yellow"),     // 黄灯 - 减速
        GREEN("green"),       // 绿灯 - 允许通行
        GUIDING("guiding");   // 引导信号
        
        private final String name;
        
        SignalState(String name) {
            this.name = name;
        }
        
        @Override
        public String asString() {
            return this.name;
        }
    }
    
    public static final EnumProperty<SignalState> SIGNAL_STATE = EnumProperty.of("signal_state", SignalState.class);
    
    public SignalBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(SIGNAL_STATE, SignalState.RED));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(SIGNAL_STATE);
    }
    
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SignalBlockEntity(pos, state);
    }
    
    public boolean hasBlockEntity() {
        return true;
    }
    
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            // 右键点击切换信号状态（临时功能，实际应该由联锁系统控制）
            SignalState currentState = state.get(SIGNAL_STATE);
            SignalState nextState = switch (currentState) {
                case RED -> SignalState.YELLOW;
                case YELLOW -> SignalState.GREEN;
                case GREEN -> SignalState.RED;
                case GUIDING -> SignalState.RED;
            };
            
            SignalSystem.getInstance().setSignalState(world, pos, nextState);
            com.real.rail.transit.RealRailTransitMod.LOGGER.info("信号机 {} 状态切换为: {}", pos, nextState);
        }
        return ActionResult.SUCCESS;
    }
}
