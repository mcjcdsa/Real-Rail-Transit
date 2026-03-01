package com.real.rail.transit.system;

import com.real.rail.transit.entity.TrainEntity;
import net.minecraft.world.World;

/**
 * 地铁列车照明系统
 * 根据中国地铁照明系统技术标准（GB/T 7928-2025、TB/T 2325.1-2019等）实现
 * 
 * 功能包括：
 * - 头灯和尾灯控制
 * - 运行灯系统
 * - 应急照明系统
 * - 不同运营阶段的灯光工作模式
 * - 与ATO/ATP系统的联动
 * - 智能光敏控制
 */
public class TrainLightingSystem {
    
    /**
     * 运营阶段枚举
     * 定义列车在不同运营阶段的状态
     */
    public enum OperatingStage {
        PREPARATION,      // 启动前准备阶段
        STARTING,        // 启动加速阶段
        NORMAL_OPERATION, // 正常运营阶段
        DECELERATING,    // 减速停车阶段
        STOPPED,         // 停车待命
        END_OF_SERVICE,  // 停止运营阶段
        EMERGENCY,       // 应急状态
        MAINTENANCE      // 维护状态
    }
    
    /**
     * 头灯模式枚举
     */
    public enum HeadlightMode {
        OFF,      // 关闭
        DIM,      // 近光（暗位）
        BRIGHT    // 远光（亮位）
    }
    
    /**
     * 运行灯状态枚举
     */
    public enum RunningLightState {
        OFF,           // 关闭
        WHITE_FRONT,   // 前端白色运行灯
        RED_REAR,      // 后端红色运行灯
        ALL_RED,       // 两端红色（待命状态）
        ALL_WHITE      // 两端白色（退行状态）
    }
    
    /**
     * 应急照明模式枚举
     */
    public enum EmergencyLightMode {
        NORMAL,        // 正常模式
        MOONLIGHT,     // 月光模式（30%亮度）
        FULL_EMERGENCY // 全应急模式
    }
    
    /**
     * 照明系统状态
     */
    public static class LightingState {
        // 头灯状态
        public HeadlightMode headlightMode = HeadlightMode.OFF;
        public boolean headlightEnabled = false;
        
        // 尾灯状态（标志灯）
        public boolean tailLightOn = false;
        
        // 运行灯状态
        public RunningLightState runningLightState = RunningLightState.OFF;
        
        // 客室照明
        public boolean passengerLightOn = true;
        public float passengerLightBrightness = 1.0f; // 0.0-1.0
        
        // 司机室照明
        public boolean cabLightOn = true;
        
        // 应急照明
        public EmergencyLightMode emergencyMode = EmergencyLightMode.NORMAL;
        public boolean emergencyLightActive = false;
        
        // 智能控制
        public boolean autoDimEnabled = true; // 自动调光
        public float ambientLightLevel = 0.0f; // 环境光强度 (lux)
        
        // 运营阶段
        public OperatingStage currentStage = OperatingStage.PREPARATION;
    }
    
    /**
     * 根据运营阶段更新照明系统
     * 
     * @param train 列车实体
     * @param stage 当前运营阶段
     */
    public static void updateLightingForStage(TrainEntity train, OperatingStage stage) {
        LightingState state = getLightingState(train);
        state.currentStage = stage;
        
        switch (stage) {
            case PREPARATION:
                // 启动前准备阶段：两端标志灯和红色运行灯点亮
                state.tailLightOn = true;
                state.runningLightState = RunningLightState.ALL_RED;
                state.headlightMode = HeadlightMode.OFF;
                state.headlightEnabled = false;
                state.passengerLightOn = true;
                state.passengerLightBrightness = 1.0f;
                break;
                
            case STARTING:
                // 启动加速阶段：激活端头灯和白色运行灯点亮，非激活端尾灯和红色运行灯点亮
                TrainEntity.Direction direction = train.getDirection();
                if (direction == TrainEntity.Direction.FORWARD) {
                    state.headlightMode = HeadlightMode.DIM; // 低速时使用近光
                    state.headlightEnabled = true;
                    state.runningLightState = RunningLightState.WHITE_FRONT;
                    state.tailLightOn = true; // 后端尾灯
                } else if (direction == TrainEntity.Direction.BACKWARD) {
                    // 退行时：前后端全部点亮
                    state.headlightMode = HeadlightMode.BRIGHT;
                    state.headlightEnabled = true;
                    state.runningLightState = RunningLightState.ALL_WHITE;
                    state.tailLightOn = true;
                }
                state.passengerLightOn = true;
                state.passengerLightBrightness = 1.0f;
                break;
                
            case NORMAL_OPERATION:
                // 正常运营阶段：根据速度自动切换远近光
                double speed = train.getCurrentSpeed();
                if (speed > 5.0) { // 速度大于18km/h时使用远光
                    state.headlightMode = HeadlightMode.BRIGHT;
                } else {
                    state.headlightMode = HeadlightMode.DIM;
                }
                state.headlightEnabled = true;
                
                direction = train.getDirection();
                if (direction == TrainEntity.Direction.FORWARD) {
                    state.runningLightState = RunningLightState.WHITE_FRONT;
                } else if (direction == TrainEntity.Direction.BACKWARD) {
                    state.runningLightState = RunningLightState.ALL_WHITE;
                }
                state.tailLightOn = true;
                
                // 智能调光
                if (state.autoDimEnabled) {
                    adjustPassengerLighting(state, train);
                }
                break;
                
            case DECELERATING:
                // 减速停车阶段：保持远光，确保司机观察前方
                state.headlightMode = HeadlightMode.BRIGHT;
                state.headlightEnabled = true;
                state.tailLightOn = true;
                state.passengerLightOn = true;
                // 到站前5秒自动调至100%亮度
                state.passengerLightBrightness = 1.0f;
                break;
                
            case STOPPED:
                // 停车待命：保持基本照明
                state.headlightMode = HeadlightMode.OFF;
                state.headlightEnabled = false;
                state.tailLightOn = true;
                state.runningLightState = RunningLightState.ALL_RED;
                state.passengerLightOn = true;
                state.passengerLightBrightness = 1.0f;
                break;
                
            case END_OF_SERVICE:
                // 停止运营：所有外部照明关闭
                state.headlightMode = HeadlightMode.OFF;
                state.headlightEnabled = false;
                state.tailLightOn = false;
                state.runningLightState = RunningLightState.OFF;
                state.passengerLightOn = false;
                state.cabLightOn = false;
                break;
                
            case EMERGENCY:
                // 应急状态：进入月光模式
                state.emergencyMode = EmergencyLightMode.MOONLIGHT;
                state.emergencyLightActive = true;
                state.passengerLightBrightness = 0.3f; // 30%亮度
                state.passengerLightOn = true;
                state.headlightMode = HeadlightMode.BRIGHT; // 应急时头灯保持远光
                state.headlightEnabled = true;
                break;
                
            case MAINTENANCE:
                // 维护状态：仅保留必要照明
                state.headlightMode = HeadlightMode.DIM;
                state.headlightEnabled = true;
                state.tailLightOn = true;
                state.passengerLightOn = true;
                state.passengerLightBrightness = 0.5f;
                break;
        }
        
        // 应用状态到列车实体
        applyLightingState(train, state);
    }
    
    /**
     * 智能调整客室照明
     * 根据环境光强度自动调节
     */
    private static void adjustPassengerLighting(LightingState state, TrainEntity train) {
        // 模拟环境光检测（实际应通过光敏传感器）
        // 这里简化为根据时间判断
        World world = train.getWorld();
        long timeOfDay = world.getTimeOfDay() % 24000;
        
        // 白天（6:00-18:00）
        if (timeOfDay >= 1000 && timeOfDay < 13000) {
            // 高架线路：根据外部光照降低亮度
            if (state.ambientLightLevel > 300.0f) {
                state.passengerLightBrightness = Math.max(0.5f, 1.0f - (state.ambientLightLevel - 300.0f) / 1000.0f);
            } else {
                state.passengerLightBrightness = 1.0f;
            }
        } else {
            // 夜间模式：30%亮度，暖光
            state.passengerLightBrightness = 0.3f;
        }
    }
    
    /**
     * 处理会车情况
     * 两车会车时，将远光切换为近光，避免对向司机眩光
     */
    public static void handleTrainMeeting(TrainEntity train) {
        LightingState state = getLightingState(train);
        if (state.headlightMode == HeadlightMode.BRIGHT) {
            state.headlightMode = HeadlightMode.DIM;
            applyLightingState(train, state);
        }
    }
    
    /**
     * 会车结束，恢复远光
     */
    public static void handleTrainMeetingEnd(TrainEntity train) {
        LightingState state = getLightingState(train);
        if (state.currentStage == OperatingStage.NORMAL_OPERATION) {
            double speed = train.getCurrentSpeed();
            if (speed > 5.0) {
                state.headlightMode = HeadlightMode.BRIGHT;
                applyLightingState(train, state);
            }
        }
    }
    
    /**
     * 触发应急照明
     * 当正常照明失效时自动启动
     */
    public static void activateEmergencyLighting(TrainEntity train) {
        LightingState state = getLightingState(train);
        state.emergencyMode = EmergencyLightMode.MOONLIGHT;
        state.emergencyLightActive = true;
        state.passengerLightBrightness = 0.3f;
        state.passengerLightOn = true;
        state.currentStage = OperatingStage.EMERGENCY;
        
        // 应急照明转换时间不应大于0.5秒
        // 这里立即切换（实际应通过定时器控制）
        applyLightingState(train, state);
    }
    
    /**
     * 与ATO系统联动
     * 根据ATO指令调整照明
     */
    public static void updateFromATO(TrainEntity train, TrainEntity.DrivingMode atoMode) {
        LightingState state = getLightingState(train);
        
        if (atoMode == TrainEntity.DrivingMode.ATO) {
            // ATO模式下，根据运行状态自动控制
            double speed = train.getCurrentSpeed();
            if (speed < 0.1) {
                if (train.getDirection() == TrainEntity.Direction.NEUTRAL) {
                    updateLightingForStage(train, OperatingStage.STOPPED);
                } else {
                    updateLightingForStage(train, OperatingStage.PREPARATION);
                }
            } else if (speed < train.getMaxSpeed() * 0.1) {
                updateLightingForStage(train, OperatingStage.STARTING);
            } else {
                updateLightingForStage(train, OperatingStage.NORMAL_OPERATION);
            }
        }
    }
    
    /**
     * 与ATP系统联动
     * 当ATP触发紧急制动时，照明系统进入应急模式
     */
    public static void updateFromATP(TrainEntity train, TrainEntity.BrakeState brakeState) {
        if (brakeState == TrainEntity.BrakeState.EMERGENCY) {
            activateEmergencyLighting(train);
        }
    }
    
    /**
     * 根据速度自动切换远近光
     */
    public static void updateHeadlightBySpeed(TrainEntity train) {
        LightingState state = getLightingState(train);
        if (state.currentStage == OperatingStage.NORMAL_OPERATION && state.headlightEnabled) {
            double speed = train.getCurrentSpeed();
            if (speed > 5.0) { // 约18km/h
                state.headlightMode = HeadlightMode.BRIGHT;
            } else if (speed > 0.1) {
                state.headlightMode = HeadlightMode.DIM;
            }
            applyLightingState(train, state);
        }
    }
    
    /**
     * 获取照明系统状态
     */
    public static LightingState getLightingState(TrainEntity train) {
        // 从列车实体获取或创建状态
        LightingState state = new LightingState();
        
        // 从列车实体读取当前状态
        state.headlightEnabled = train.isHeadlightOn();
        state.headlightMode = train.isHeadlightDim() ? HeadlightMode.DIM : 
                             (state.headlightEnabled ? HeadlightMode.BRIGHT : HeadlightMode.OFF);
        state.passengerLightOn = train.isPassengerLightOn();
        state.passengerLightBrightness = train.getPassengerLightBrightness();
        state.cabLightOn = train.isCabLightOn();
        state.tailLightOn = train.isTailLightOn();
        
        // 读取运行灯状态
        int runningLightValue = train.getRunningLightState();
        switch (runningLightValue) {
            case 1:
                state.runningLightState = RunningLightState.WHITE_FRONT;
                break;
            case 2:
                state.runningLightState = RunningLightState.RED_REAR;
                break;
            case 3:
                state.runningLightState = RunningLightState.ALL_RED;
                break;
            case 4:
                state.runningLightState = RunningLightState.ALL_WHITE;
                break;
            default:
                state.runningLightState = RunningLightState.OFF;
                break;
        }
        
        state.emergencyLightActive = train.isEmergencyLightActive();
        state.autoDimEnabled = train.isAutoDimEnabled();
        state.ambientLightLevel = train.getAmbientLightLevel();
        
        return state;
    }
    
    /**
     * 应用照明状态到列车实体
     */
    private static void applyLightingState(TrainEntity train, LightingState state) {
        // 更新头灯
        train.setHeadlightOn(state.headlightEnabled && state.headlightMode != HeadlightMode.OFF);
        train.setHeadlightDim(state.headlightMode == HeadlightMode.DIM);
        
        // 更新客室照明
        train.setPassengerLightOn(state.passengerLightOn);
        train.setPassengerLightBrightness(state.passengerLightBrightness);
        
        // 更新司机室照明
        train.setCabLightOn(state.cabLightOn);
        
        // 更新尾灯（标志灯）
        train.setTailLightOn(state.tailLightOn);
        
        // 更新运行灯状态
        int runningLightStateValue = 0;
        switch (state.runningLightState) {
            case WHITE_FRONT:
                runningLightStateValue = 1;
                break;
            case RED_REAR:
                runningLightStateValue = 2;
                break;
            case ALL_RED:
                runningLightStateValue = 3;
                break;
            case ALL_WHITE:
                runningLightStateValue = 4;
                break;
            case OFF:
            default:
                runningLightStateValue = 0;
                break;
        }
        train.setRunningLightState(runningLightStateValue);
        
        // 更新应急照明
        train.setEmergencyLightActive(state.emergencyLightActive);
        
        // 更新智能控制参数
        train.setAutoDimEnabled(state.autoDimEnabled);
        train.setAmbientLightLevel(state.ambientLightLevel);
    }
    
    /**
     * 检查照明系统故障
     * 根据GB/T 25119-2021标准，故障诊断覆盖率需达95%以上
     */
    public static boolean checkLightingFaults(TrainEntity train) {
        LightingState state = getLightingState(train);
        
        // 检查头灯故障
        if (state.headlightEnabled && state.headlightMode == HeadlightMode.OFF) {
            return true; // 头灯应该点亮但未点亮
        }
        
        // 检查应急照明系统
        if (state.emergencyLightActive && state.passengerLightBrightness < 0.2f) {
            return true; // 应急照明亮度不足
        }
        
        return false;
    }
    
    /**
     * 获取当前运营阶段（根据列车状态推断）
     */
    public static OperatingStage inferOperatingStage(TrainEntity train) {
        double speed = train.getCurrentSpeed();
        TrainEntity.Direction direction = train.getDirection();
        boolean engineOn = train.isEngineOn();
        
        if (!engineOn && speed < 0.1) {
            return OperatingStage.END_OF_SERVICE;
        }
        
        if (speed < 0.1) {
            if (direction == TrainEntity.Direction.NEUTRAL) {
                return OperatingStage.STOPPED;
            } else {
                return OperatingStage.PREPARATION;
            }
        } else if (speed < train.getMaxSpeed() * 0.1) {
            return OperatingStage.STARTING;
        } else if (train.getBrakeState() == TrainEntity.BrakeState.EMERGENCY) {
            return OperatingStage.EMERGENCY;
        } else if (speed > train.getMaxSpeed() * 0.3) {
            return OperatingStage.NORMAL_OPERATION;
        } else {
            return OperatingStage.DECELERATING;
        }
    }
    
    /**
     * 每tick更新照明系统
     */
    public static void tick(TrainEntity train) {
        // 推断当前运营阶段
        OperatingStage stage = inferOperatingStage(train);
        
        // 更新照明状态
        updateLightingForStage(train, stage);
        
        // 根据速度自动切换远近光
        updateHeadlightBySpeed(train);
        
        // 与ATO/ATP系统联动
        updateFromATO(train, train.getDrivingMode());
        updateFromATP(train, train.getBrakeState());
        
        // 检测会车情况（简化实现：检测前方是否有对向列车）
        checkTrainMeeting(train);
    }
    
    /**
     * 检测会车情况
     * 当检测到前方有对向列车时，自动切换为近光
     */
    private static void checkTrainMeeting(TrainEntity train) {
        if (train.getWorld().isClient) {
            return; // 仅在服务器端检测
        }
        
        // 仅在正常运营阶段检测会车
        OperatingStage stage = inferOperatingStage(train);
        if (stage != OperatingStage.NORMAL_OPERATION && stage != OperatingStage.STARTING) {
            return;
        }
        
        // 检测前方一定距离内是否有对向列车
        net.minecraft.util.math.Vec3d trainPos = train.getPos();
        net.minecraft.util.math.Vec3d forwardVec = train.getRotationVector();
        
        // 检测前方50米范围内
        double detectionDistance = 50.0;
        net.minecraft.util.math.Box detectionBox = new net.minecraft.util.math.Box(
            trainPos.x - detectionDistance,
            trainPos.y - 2.0,
            trainPos.z - detectionDistance,
            trainPos.x + detectionDistance,
            trainPos.y + 5.0,
            trainPos.z + detectionDistance
        );
        
        boolean meetingDetected = false;
        for (net.minecraft.entity.Entity entity : train.getWorld().getOtherEntities(train, detectionBox)) {
            if (entity instanceof TrainEntity otherTrain) {
                // 检查是否为对向列车（方向相反）
                TrainEntity.Direction thisDirection = train.getDirection();
                TrainEntity.Direction otherDirection = otherTrain.getDirection();
                
                if ((thisDirection == TrainEntity.Direction.FORWARD && otherDirection == TrainEntity.Direction.BACKWARD) ||
                    (thisDirection == TrainEntity.Direction.BACKWARD && otherDirection == TrainEntity.Direction.FORWARD)) {
                    
                    // 检查是否在前方
                    net.minecraft.util.math.Vec3d otherPos = otherTrain.getPos();
                    net.minecraft.util.math.Vec3d toOther = otherPos.subtract(trainPos);
                    double dot = forwardVec.dotProduct(toOther.normalize());
                    
                    if (dot > 0.5) { // 在前方方向
                        meetingDetected = true;
                        break;
                    }
                }
            }
        }
        
        // 根据检测结果切换灯光
        LightingState state = getLightingState(train);
        if (meetingDetected && state.headlightMode == HeadlightMode.BRIGHT) {
            // 会车时切换为近光
            state.headlightMode = HeadlightMode.DIM;
            applyLightingState(train, state);
        } else if (!meetingDetected && state.headlightMode == HeadlightMode.DIM && 
                   stage == OperatingStage.NORMAL_OPERATION && train.getCurrentSpeed() > 5.0) {
            // 会车结束，恢复远光
            state.headlightMode = HeadlightMode.BRIGHT;
            applyLightingState(train, state);
        }
    }
}

