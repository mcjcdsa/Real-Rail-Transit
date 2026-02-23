package com.real.rail.transit.system;

import com.real.rail.transit.block.SignalBlock;
import com.real.rail.transit.entity.TrainEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * 列车移动系统
 * 处理列车的物理移动、轨道检测、速度控制等
 */
public class TrainMovementSystem {
    private static final TrainMovementSystem INSTANCE = new TrainMovementSystem();
    
    private TrainMovementSystem() {
    }
    
    public static TrainMovementSystem getInstance() {
        return INSTANCE;
    }
    
    /**
     * 更新列车移动（每tick调用）
     */
    public void updateTrainMovement(World world, TrainEntity train) {
        // 检查前方信号
        checkSignalAhead(world, train);
        
        // 检查供电状态
        checkPowerSupply(world, train);
        
        // 检查超速
        checkSpeedLimit(world, train);
        
        // 应用移动
        applyMovement(world, train);
    }
    
    /**
     * 检查前方信号
     */
    private void checkSignalAhead(World world, TrainEntity train) {
        BlockPos trainPos = train.getBlockPos();
        Vec3d rotationVec = train.getRotationVector();
        BlockPos aheadPos = trainPos.add(
            (int) Math.round(rotationVec.x * 5),
            (int) Math.round(rotationVec.y * 5),
            (int) Math.round(rotationVec.z * 5)
        );
        
        // 检查前方是否有信号机
        if (world.getBlockState(aheadPos).getBlock() instanceof SignalBlock) {
            SignalBlock.SignalState signalState = world.getBlockState(aheadPos).get(SignalBlock.SIGNAL_STATE);
            
            // 在ATP/ATC/CBTC模式下，红灯自动触发制动
            if (signalState == SignalBlock.SignalState.RED) {
                TrainEntity.DrivingMode mode = train.getDrivingMode();
                if (mode == TrainEntity.DrivingMode.ATP ||
                    mode == TrainEntity.DrivingMode.ATC ||
                    mode == TrainEntity.DrivingMode.CBTC) {
                    train.triggerEmergencyBrake();
                }
            }
        }
    }
    
    /**
     * 检查供电状态
     */
    private void checkPowerSupply(World world, TrainEntity train) {
        BlockPos trainPos = train.getBlockPos();
        PowerSystem powerSystem = PowerSystem.getInstance();
        
        // 检查列车当前位置是否有供电
        // 简化实现：检查当前位置和周围位置的供电状态
        boolean hasPower = false;
        PowerSystem.PowerType trainPowerType = PowerSystem.PowerType.THIRD_RAIL; // 默认第三轨
        
        // 检查当前位置及周围位置
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = trainPos.add(x, 0, z);
                if (powerSystem.canTrainGetPower(checkPos, trainPowerType)) {
                    hasPower = true;
                    break;
                }
            }
            if (hasPower) break;
        }
        
        // 如果无电，列车失去动力（减速）
        if (!hasPower && train.getCurrentSpeed() > 0) {
            // 逐渐减速
            double currentSpeed = train.getCurrentSpeed();
            train.setCurrentSpeed(Math.max(0, currentSpeed - 0.5)); // 每秒减速0.5 m/s
        }
    }
    
    /**
     * 检查速度限制
     */
    private void checkSpeedLimit(World world, TrainEntity train) {
        double currentSpeed = train.getCurrentSpeed();
        double maxSpeed = getSpeedLimitForMode(train.getDrivingMode());
        
        if (currentSpeed > maxSpeed) {
            // 超速检测，触发紧急制动
            train.triggerEmergencyBrake();
        }
    }
    
    /**
     * 获取不同驾驶模式下的速度限制
     */
    private double getSpeedLimitForMode(TrainEntity.DrivingMode mode) {
        switch (mode) {
            case RM:
                return 30.0; // 限制人工驾驶模式限速30 m/s (约108 km/h)
            case ATP:
            case ATC:
            case CBTC:
                return 97.2; // 自动模式最高速度 (约350 km/h)
            case ATO:
                return 97.2;
            case MANUAL:
            default:
                return 97.2; // 手动模式理论上可以开到最高速度
        }
    }
    
    /**
     * 应用移动
     */
    private void applyMovement(World world, TrainEntity train) {
        // 移动逻辑在TrainEntity中实现
        // 这里可以添加额外的移动处理逻辑
    }
}

