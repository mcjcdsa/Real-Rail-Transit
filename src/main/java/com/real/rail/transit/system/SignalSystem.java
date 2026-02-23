package com.real.rail.transit.system;

import com.real.rail.transit.block.SignalBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

/**
 * 信号系统
 * 管理信号机的状态和联锁逻辑
 */
public class SignalSystem {
    private static final SignalSystem INSTANCE = new SignalSystem();
    
    // 存储信号机状态
    private final Map<BlockPos, SignalBlock.SignalState> signalStates = new HashMap<>();
    
    private SignalSystem() {
    }
    
    public static SignalSystem getInstance() {
        return INSTANCE;
    }
    
    /**
     * 设置信号机状态
     */
    public void setSignalState(World world, BlockPos pos, SignalBlock.SignalState state) {
        signalStates.put(pos, state);
        
        // 更新方块状态
        if (world.getBlockState(pos).getBlock() instanceof SignalBlock) {
            world.setBlockState(pos, world.getBlockState(pos)
                .with(SignalBlock.SIGNAL_STATE, state));
        }
    }
    
    /**
     * 获取信号机状态
     */
    public SignalBlock.SignalState getSignalState(World world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() instanceof SignalBlock) {
            return world.getBlockState(pos).get(SignalBlock.SIGNAL_STATE);
        }
        return SignalBlock.SignalState.RED; // 默认返回红灯
    }
    
    /**
     * 检查列车是否可以通行
     */
    public boolean canTrainPass(World world, BlockPos signalPos) {
        SignalBlock.SignalState state = getSignalState(world, signalPos);
        return state == SignalBlock.SignalState.GREEN || 
               state == SignalBlock.SignalState.GUIDING;
    }
    
    /**
     * 更新信号机状态（根据列车位置和道岔状态）
     */
    public void updateSignalState(World world, BlockPos signalPos) {
        if (!(world.getBlockState(signalPos).getBlock() instanceof SignalBlock)) {
            return;
        }
        
        // 检查前方轨道占用情况
        boolean trackOccupied = checkTrackAhead(world, signalPos);
        
        // 检查道岔状态（如果有道岔）
        boolean turnoutClear = checkTurnoutState(world, signalPos);
        
        // 根据检查结果设置信号状态
        SignalBlock.SignalState newState;
        if (trackOccupied) {
            // 轨道被占用，显示红灯
            newState = SignalBlock.SignalState.RED;
        } else if (!turnoutClear) {
            // 道岔未就位，显示黄灯
            newState = SignalBlock.SignalState.YELLOW;
        } else {
            // 轨道空闲且道岔就位，显示绿灯
            newState = SignalBlock.SignalState.GREEN;
        }
        
        // 更新信号状态
        setSignalState(world, signalPos, newState);
    }
    
    /**
     * 检查前方轨道是否被占用
     */
    private boolean checkTrackAhead(World world, BlockPos signalPos) {
        // 检查前方5格内的轨道是否有列车
        for (int i = 1; i <= 5; i++) {
            BlockPos checkPos = signalPos.add(i, 0, 0); // 简化：假设信号机朝向X轴正方向
            // 检查是否有列车实体
            net.minecraft.util.math.Box box = new net.minecraft.util.math.Box(checkPos);
            var entities = world.getEntitiesByType(
                com.real.rail.transit.registry.ModEntities.TRAIN,
                box,
                entity -> true
            );
            if (!entities.isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查道岔状态
     */
    private boolean checkTurnoutState(World world, BlockPos signalPos) {
        // 检查附近是否有道岔，并检查其状态
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos checkPos = signalPos.add(x, 0, z);
                var block = world.getBlockState(checkPos).getBlock();
                if (block instanceof com.real.rail.transit.block.TurnoutBlock) {
                    // 检查道岔是否就位（简化实现）
                    return true; // 假设道岔就位
                }
            }
        }
        return true; // 没有道岔，视为就位
    }
}

