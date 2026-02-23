package com.real.rail.transit.system;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 供电系统
 * 管理第三轨和接触网的供电状态
 */
public class PowerSystem {
    private static final PowerSystem INSTANCE = new PowerSystem();
    
    /**
     * 供电类型枚举
     */
    public enum PowerType {
        THIRD_RAIL,    // 第三轨供电
        CATENARY,      // 接触网供电
        NONE           // 无供电
    }
    
    // 存储供电区段状态
    private final Map<BlockPos, PowerType> powerSections = new HashMap<>();
    private final Map<BlockPos, Boolean> powerStates = new HashMap<>(); // true为有电，false为断电
    private final Set<BlockPos> cables = new java.util.HashSet<>(); // 存储线缆位置
    
    private PowerSystem() {
    }
    
    public static PowerSystem getInstance() {
        return INSTANCE;
    }
    
    /**
     * 设置供电区段类型
     */
    public void setPowerSectionType(BlockPos pos, PowerType type) {
        powerSections.put(pos, type);
        powerStates.put(pos, true); // 默认有电
    }
    
    /**
     * 设置供电区段状态
     */
    public void setPowerState(World world, BlockPos pos, boolean powered) {
        powerStates.put(pos, powered);
        // TODO: 更新方块状态和视觉效果
    }
    
    /**
     * 检查位置是否有电
     */
    public boolean isPowered(BlockPos pos) {
        return powerStates.getOrDefault(pos, false);
    }
    
    /**
     * 获取位置的供电类型
     */
    public PowerType getPowerType(BlockPos pos) {
        return powerSections.getOrDefault(pos, PowerType.NONE);
    }
    
    /**
     * 检查列车是否可以在此位置获取电力
     */
    public boolean canTrainGetPower(BlockPos pos, PowerType trainPowerType) {
        PowerType sectionType = getPowerType(pos);
        return isPowered(pos) && sectionType == trainPowerType;
    }
    
    /**
     * 注册线缆
     */
    public void registerCable(World world, BlockPos pos) {
        cables.add(pos);
    }
    
    /**
     * 移除线缆
     */
    public void unregisterCable(BlockPos pos) {
        cables.remove(pos);
    }
    
    /**
     * 检查线缆是否连接到供电系统
     */
    public boolean isCableConnected(BlockPos pos) {
        // 检查线缆是否在供电区段附近
        for (BlockPos powerPos : powerSections.keySet()) {
            if (pos.isWithinDistance(powerPos, 5.0)) {
                return true;
            }
        }
        return false;
    }
}

