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
    private boolean engineOn = false; // 引擎是否开启
    
    // 驾驶模式
    private DrivingMode drivingMode = DrivingMode.MANUAL;
    
    // 制动状态
    private BrakeState brakeState = BrakeState.NORMAL;

    // 设备状态（参考中国地铁驾驶室标准）
    private boolean headlightOn = false;
    private boolean headlightDim = false;
    private boolean cabLightOn = true;
    private boolean passengerLightOn = true;
    private boolean pantographUp = true;
    private Direction direction = Direction.FORWARD;

    /**
     * 行驶方向枚举
     */
    public enum Direction {
        FORWARD,   // 前进
        NEUTRAL,   // 零位
        BACKWARD   // 后退
    }
    
    /**
     * 驾驶模式枚举（参考 GB/T 7928 及中国地铁标准）
     */
    public enum DrivingMode {
        ATO,      // 自动驾驶模式
        ATB,      // 自动折返模式
        IATP,     // 点式 ATP 模式
        CBTC,     // 基于通信的列车控制
        ATP,      // 列车自动防护
        ATC,      // 列车自动控制
        RM,       // 限制人工驾驶（最高 25 km/h）
        MANUAL,   // 手动模式
        OFF       // 关闭保护模式（极端情况）
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
        // 设置默认边界框（列车尺寸：长20米，宽2.5米，高3.5米）
        this.setBoundingBox(new net.minecraft.util.math.Box(-10, 0, -1.25, 10, 3.5, 1.25));
    }
    
    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        // 初始化数据追踪器
    }
    
    /**
     * 更新边界框（在设置列车ID后调用）
     */
    public void updateBoundingBox() {
        // 根据列车配置更新边界框
        String id = this.getTrainId();
        if (id != null && !id.isEmpty()) {
            com.real.rail.transit.addon.AddonManager.TrainConfig config = 
                com.real.rail.transit.addon.AddonManager.getInstance().getLoadedAddons().stream()
                    .filter(train -> train.train_id.equals(id))
                    .findFirst()
                    .orElse(null);
            
            if (config != null) {
                double length = config.car_count * config.car_length;
                double width = 2.5;
                double height = 3.5;
                this.setBoundingBox(new net.minecraft.util.math.Box(
                    -length / 2, 0, -width / 2,
                    length / 2, height, width / 2
                ));
            }
        }
    }
    
    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        // 从NBT读取数据
        this.currentSpeed = nbt.getDouble("currentSpeed");
        this.maxSpeed = nbt.getDouble("maxSpeed");
        String mode = nbt.getString("drivingMode");
        if (!mode.isEmpty()) {
            this.drivingMode = DrivingMode.valueOf(mode);
        }
        String brake = nbt.getString("brakeState");
        if (!brake.isEmpty()) {
            this.brakeState = BrakeState.valueOf(brake);
        }
        this.engineOn = nbt.getBoolean("engineOn");
        this.headlightOn = nbt.getBoolean("headlightOn");
        this.headlightDim = nbt.getBoolean("headlightDim");
        this.cabLightOn = nbt.getBoolean("cabLightOn");
        this.passengerLightOn = nbt.getBoolean("passengerLightOn");
        this.pantographUp = nbt.getBoolean("pantographUp");
        String dir = nbt.getString("direction");
        if (!dir.isEmpty()) {
            try {
                this.direction = Direction.valueOf(dir);
            } catch (IllegalArgumentException ignored) {}
        }
    }
    
    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        // 写入NBT数据
        nbt.putDouble("currentSpeed", this.currentSpeed);
        nbt.putDouble("maxSpeed", this.maxSpeed);
        nbt.putString("drivingMode", this.drivingMode.name());
        nbt.putString("brakeState", this.brakeState.name());
        nbt.putBoolean("engineOn", this.engineOn);
        nbt.putBoolean("headlightOn", this.headlightOn);
        nbt.putBoolean("headlightDim", this.headlightDim);
        nbt.putBoolean("cabLightOn", this.cabLightOn);
        nbt.putBoolean("passengerLightOn", this.passengerLightOn);
        nbt.putBoolean("pantographUp", this.pantographUp);
        nbt.putString("direction", this.direction.name());
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
        if (!this.engineOn) {
            // 引擎未开启，仅允许自然减速到 0
            if (this.currentSpeed > 0) {
                this.currentSpeed = Math.max(0, this.currentSpeed - this.deceleration * 0.05);
                if (this.currentSpeed > 0) {
                    Vec3d movement = this.getRotationVector().multiply(this.currentSpeed * 0.05);
                    this.move(MovementType.SELF, movement);
                }
            }
            return;
        }
        
        // 根据驾驶模式和制动状态计算速度变化
        double speedChange = calculateSpeedChange();
        double effectiveMaxSpeed = (this.drivingMode == DrivingMode.RM) ? (25.0 / 3.6) : this.maxSpeed; // RM 模式限速 25 km/h
        this.currentSpeed = Math.max(0, Math.min(effectiveMaxSpeed, this.currentSpeed + speedChange));
        
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
                    this.drivingMode == DrivingMode.ATB ||
                    this.drivingMode == DrivingMode.IATP ||
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
        double effectiveMaxSpeed = (this.drivingMode == DrivingMode.RM) ? (25.0 / 3.6) : this.maxSpeed;
        this.targetSpeed = Math.max(0, Math.min(speed, effectiveMaxSpeed));
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
     * 缓解紧急制动（需列车停稳后操作）
     */
    public void releaseEmergencyBrake() {
        if (this.currentSpeed < 0.1) {
            this.brakeState = BrakeState.NORMAL;
        }
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
    
    /**
     * 获取引擎是否开启
     */
    public boolean isEngineOn() {
        return this.engineOn;
    }
    
    /**
     * 设置引擎状态
     */
    public void setEngineOn(boolean engineOn) {
        this.engineOn = engineOn;
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
     * 设置最高速度
     */
    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = Math.max(0.1, maxSpeed); // 确保最高速度至少为 0.1 m/s
    }
    
    /**
     * 获取最高速度
     */
    public double getMaxSpeed() {
        return this.maxSpeed;
    }
    
    /**
     * 设置加速度
     */
    public void setAcceleration(double acceleration) {
        this.acceleration = Math.max(0.1, acceleration); // 确保加速度至少为 0.1 m/s²
    }
    
    /**
     * 获取加速度
     */
    public double getAcceleration() {
        return this.acceleration;
    }
    
    /**
     * 设置减速度
     */
    public void setDeceleration(double deceleration) {
        this.deceleration = Math.max(0.1, deceleration); // 确保减速度至少为 0.1 m/s²
    }
    
    /**
     * 获取减速度
     */
    public double getDeceleration() {
        return this.deceleration;
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

    public boolean isHeadlightOn() { return headlightOn; }
    public void setHeadlightOn(boolean on) { this.headlightOn = on; }
    public boolean isHeadlightDim() { return headlightDim; }
    public void setHeadlightDim(boolean dim) { this.headlightDim = dim; }
    public boolean isCabLightOn() { return cabLightOn; }
    public void setCabLightOn(boolean on) { this.cabLightOn = on; }
    public boolean isPassengerLightOn() { return passengerLightOn; }
    public void setPassengerLightOn(boolean on) { this.passengerLightOn = on; }
    public boolean isPantographUp() { return pantographUp; }
    public void setPantographUp(boolean up) { this.pantographUp = up; }
    public Direction getDirection() { return direction; }
    public void setDirection(Direction d) { this.direction = d; }
}

