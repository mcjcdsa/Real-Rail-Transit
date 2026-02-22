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
        // TODO: 实现信号机状态更新逻辑
        // 需要考虑：前方轨道占用情况、道岔状态、调度指令等
    }
}

