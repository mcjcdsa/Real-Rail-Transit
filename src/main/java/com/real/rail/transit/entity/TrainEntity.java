package com.real.rail.transit.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * 列车实体
 * 核心列车实体类，负责列车的移动、控制等功能
 */
public class TrainEntity extends Entity {
    // 列车基本属性
    private double currentSpeed = 0.0; // 当前速度 (m/s)
    private double targetSpeed = 0.0; // 目标速度 (m/s)
    private double maxSpeed = 97.2; // 最高速度 (m/s, 约350 km/h)
    private double acceleration = 0.8; // 加速度 (m/s²)
    private double deceleration = 1.2; // 减速度 (m/s²)
    
    // 驾驶模式
    private DrivingMode drivingMode = DrivingMode.MANUAL;
    
    // 制动状态
    private BrakeState brakeState = BrakeState.NORMAL;
    
    /**
     * 驾驶模式枚举
     */
    public enum DrivingMode {
        ATO,      // 自动驾驶模式
        CBTC,     // 基于通信的列车控制
        ATP,      // 列车自动防护
        ATC,      // 列车自动控制
        RM,       // 限制人工驾驶
        MANUAL    // 手动模式
    }
    
    /**
     * 制动状态枚举
     */
    public enum BrakeState {
        NORMAL,      // 正常制动
        QUICK,       // 快速制动
        EMERGENCY    // 紧急制动
    }
    
    public TrainEntity(EntityType<?> type, World world) {
        super(type, world);
    }
    
    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        // 初始化数据追踪器
    }
    
    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        // 从NBT读取数据
        this.currentSpeed = nbt.getDouble("currentSpeed");
        this.maxSpeed = nbt.getDouble("maxSpeed");
        this.drivingMode = DrivingMode.valueOf(nbt.getString("drivingMode"));
        this.brakeState = BrakeState.valueOf(nbt.getString("brakeState"));
    }
    
    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        // 写入NBT数据
        nbt.putDouble("currentSpeed", this.currentSpeed);
        nbt.putDouble("maxSpeed", this.maxSpeed);
        nbt.putString("drivingMode", this.drivingMode.name());
        nbt.putString("brakeState", this.brakeState.name());
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 更新列车物理状态
        updateMovement();
        
        // 更新移动系统
        com.real.rail.transit.system.TrainMovementSystem.getInstance()
            .updateTrainMovement(this.getWorld(), this);
    }
    
    /**
     * 更新列车移动
     */
    private void updateMovement() {
        // 根据驾驶模式和制动状态计算速度变化
        double speedChange = calculateSpeedChange();
        this.currentSpeed = Math.max(0, Math.min(this.maxSpeed, this.currentSpeed + speedChange));
        
        // 应用移动
        if (this.currentSpeed > 0) {
            Vec3d movement = this.getRotationVector().multiply(this.currentSpeed * 0.05);
            this.move(MovementType.SELF, movement);
        }
    }
    
    /**
     * 计算速度变化
     */
    private double calculateSpeedChange() {
        switch (this.brakeState) {
            case EMERGENCY:
                return -this.deceleration * 2.0; // 紧急制动
            case QUICK:
                return -this.deceleration * 1.5; // 快速制动
            case NORMAL:
                // 根据目标速度和当前速度计算速度变化
                double speedDiff = this.targetSpeed - this.currentSpeed;
                
                if (Math.abs(speedDiff) < 0.1) {
                    return 0; // 已达到目标速度
                }
                
                // 根据驾驶模式决定加速或减速
                if (this.drivingMode == DrivingMode.ATO || 
                    this.drivingMode == DrivingMode.ATC ||
                    this.drivingMode == DrivingMode.CBTC) {
                    // 自动模式：自动调整速度到目标速度
                    if (speedDiff > 0) {
                        return Math.min(this.acceleration * 0.1, speedDiff);
                    } else {
                        return Math.max(-this.deceleration * 0.1, speedDiff);
                    }
                } else if (this.drivingMode == DrivingMode.MANUAL || this.drivingMode == DrivingMode.RM) {
                    // 手动模式：根据目标速度调整
                    if (speedDiff > 0) {
                        return Math.min(this.acceleration * 0.05, speedDiff);
                    } else {
                        return Math.max(-this.deceleration * 0.05, speedDiff);
                    }
                }
                return 0;
            default:
                return 0;
        }
    }
    
    /**
     * 设置目标速度
     */
    public void setTargetSpeed(double speed) {
        this.targetSpeed = Math.max(0, Math.min(speed, this.maxSpeed));
    }
    
    /**
     * 获取目标速度
     */
    public double getTargetSpeed() {
        return this.targetSpeed;
    }
    
    /**
     * 触发紧急制动
     */
    public void triggerEmergencyBrake() {
        this.brakeState = BrakeState.EMERGENCY;
    }
    
    /**
     * 获取当前速度
     */
    public double getCurrentSpeed() {
        return this.currentSpeed;
    }
    
    /**
     * 设置当前速度
     */
    public void setCurrentSpeed(double speed) {
        this.currentSpeed = Math.max(0, Math.min(speed, this.maxSpeed));
    }
    
    /**
     * 设置驾驶模式
     */
    public void setDrivingMode(DrivingMode mode) {
        this.drivingMode = mode;
    }
    
    private String trainId = "";
    
    /**
     * 设置列车ID
     */
    public void setTrainId(String trainId) {
        this.trainId = trainId != null ? trainId : "";
    }
    
    /**
     * 获取列车ID
     */
    public String getTrainId() {
        return this.trainId;
    }
    
    /**
     * 获取驾驶模式
     */
    public DrivingMode getDrivingMode() {
        return this.drivingMode;
    }
    
    /**
     * 获取制动状态
     */
    public BrakeState getBrakeState() {
        return this.brakeState;
    }
    
    /**
     * 设置制动状态
     */
    public void setBrakeState(BrakeState brakeState) {
        this.brakeState = brakeState;
    }
}

