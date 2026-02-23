package com.real.rail.transit.block;

import com.real.rail.transit.RealRailTransitMod;
import com.real.rail.transit.track.TrackNetwork;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    
    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        // 注册到轨道网络
        TrackNetwork.getInstance().registerTrack(world, pos);
    }
    
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        // 从轨道网络移除
        TrackNetwork.getInstance().unregisterTrack(pos);
    }
    
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            // 检查是否锁定
            if (state.get(IS_LOCKED)) {
                player.sendMessage(Text.translatable("block.real-rail-transit-mod.turnout.locked"), false);
                return ActionResult.SUCCESS;
            }
            
            // 切换道岔方向
            boolean isStraight = state.get(IS_STRAIGHT);
            world.setBlockState(pos, state.with(IS_STRAIGHT, !isStraight));
            
            // 更新轨道网络连接
            TrackNetwork.getInstance().registerTrack(world, pos);
            
            String direction = !isStraight ? "直向" : "侧向";
            player.sendMessage(Text.translatable("block.real-rail-transit-mod.turnout.switched", direction), false);
            RealRailTransitMod.LOGGER.info("道岔 {} 切换为: {}", pos, direction);
            
            return ActionResult.SUCCESS;
        }
        return ActionResult.SUCCESS;
    }
    
    /**
     * 锁定/解锁道岔
     * @param world 世界
     * @param pos 位置
     * @param locked 是否锁定
     */
    public void setLocked(World world, BlockPos pos, boolean locked) {
        BlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.with(IS_LOCKED, locked));
    }
    
    /**
     * 检查道岔是否锁定
     * @param state 方块状态
     * @return 是否锁定
     */
    public boolean isLocked(BlockState state) {
        return state.get(IS_LOCKED);
    }
    
    /**
     * 检查道岔是否为直向
     * @param state 方块状态
     * @return 是否为直向
     */
    public boolean isStraight(BlockState state) {
        return state.get(IS_STRAIGHT);
    }
}

