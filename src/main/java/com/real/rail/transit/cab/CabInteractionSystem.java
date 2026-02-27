package com.real.rail.transit.cab;

import com.real.rail.transit.entity.TrainEntity;
import com.real.rail.transit.sound.SoundSystem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 驾驶室交互系统
 * 管理驾驶室内的按钮、开关、仪表盘等交互元素
 */
public class CabInteractionSystem {
    private static final CabInteractionSystem INSTANCE = new CabInteractionSystem();
    
    /**
     * 按钮类型枚举（参考中国地铁驾驶室标准 GB/T 7928）
     * 分类：运行控制、安全保障、设备控制、通信、辅助功能
     */
    public enum ButtonType {
        // ========== 运行控制 ==========
        START_ENGINE,       // 启动引擎
        STOP_ENGINE,        // 停止引擎
        ATO_START,          // ATO 启动（绿色按钮）
        MODE_SWITCH,        // 模式切换
        DIRECTION_FORWARD,  // 方向：前进
        DIRECTION_NEUTRAL,  // 方向：零位
        DIRECTION_BACKWARD, // 方向：后退
        SPEED_UP,           // 加速（牵引）
        SPEED_DOWN,         // 减速（制动）
        COAST,              // 惰行（主控手柄零位）
        // ========== 安全保障 ==========
        EMERGENCY_BRAKE,    // 紧急制动（红色蘑菇头）
        QUICK_BRAKE,        // 快速制动
        FORCE_RELEASE,      // 强迫缓解/远程缓解
        VIGILANCE,          // 警惕按钮
        // ========== 车门控制 ==========
        OPEN_DOORS,         // 开门（双侧）
        CLOSE_DOORS,        // 关门（双侧）
        OPEN_LEFT_DOOR,     // 开左门
        OPEN_RIGHT_DOOR,    // 开右门
        CLOSE_LEFT_DOOR,    // 关左门
        CLOSE_RIGHT_DOOR,   // 关右门
        REOPEN_CLOSE_DOOR,  // 再开闭门（黄色）
        // ========== 照明控制 ==========
        CAB_LIGHT,          // 司机室灯
        GAUGE_LIGHT,        // 仪表灯
        HEADLIGHT,           // 前照灯
        HEADLIGHT_DIM,      // 头灯明暗调节
        PASSENGER_LIGHT,    // 客室灯
        LIGHT_TEST,         // 灯测试
        // ========== 辅助功能 ==========
        HORN,               // 鸣笛（电笛/风笛）
        PANTOGRAPH_UP,      // 受电弓升
        PANTOGRAPH_DOWN,    // 受电弓降
        SANDING,            // 撒砂
        WIPER,              // 雨刷
        PA_BROADCAST,       // PA 广播
        SLEEP,              // 休眠
        WAKE,               // 唤醒
        UNCOUPLE,           // 解钩
        // ========== 自定义 ==========
        CUSTOM              // 追加包自定义
    }
    
    /**
     * 存储玩家与列车的绑定关系
     */
    private final Map<UUID, TrainEntity> playerTrainBindings = new HashMap<>();
    
    /**
     * 存储列车的驾驶权限
     */
    private final Map<TrainEntity, UUID> trainDriverBindings = new HashMap<>();
    
    private CabInteractionSystem() {
    }
    
    public static CabInteractionSystem getInstance() {
        return INSTANCE;
    }
    
    /**
     * 绑定玩家到列车（进入驾驶模式）
     */
    public boolean bindPlayerToTrain(PlayerEntity player, TrainEntity train) {
        // TODO: 检查玩家是否有权限（钥匙等）
        UUID playerId = player.getUuid();
        playerTrainBindings.put(playerId, train);
        trainDriverBindings.put(train, playerId);
        return true;
    }
    
    /**
     * 解除玩家与列车的绑定（退出驾驶模式）
     */
    public void unbindPlayerFromTrain(PlayerEntity player) {
        UUID playerId = player.getUuid();
        TrainEntity train = playerTrainBindings.remove(playerId);
        if (train != null) {
            trainDriverBindings.remove(train);
        }
    }
    
    /**
     * 处理按钮点击
     */
    public void handleButtonClick(PlayerEntity player, ButtonType buttonType) {
        TrainEntity train = playerTrainBindings.get(player.getUuid());
        if (train == null) {
            return; // 玩家未绑定列车
        }
        
        switch (buttonType) {
            // ========== 运行控制 ==========
            case START_ENGINE:
                train.setEngineOn(true);
                break;
            case STOP_ENGINE:
                train.setEngineOn(false);
                train.setTargetSpeed(0);
                break;
            case ATO_START:
                train.setEngineOn(true);
                train.setDrivingMode(TrainEntity.DrivingMode.ATO);
                break;
            case MODE_SWITCH:
                cycleDrivingMode(train);
                break;
            case DIRECTION_FORWARD:
                train.setDirection(TrainEntity.Direction.FORWARD);
                break;
            case DIRECTION_NEUTRAL:
                train.setDirection(TrainEntity.Direction.NEUTRAL);
                break;
            case DIRECTION_BACKWARD:
                train.setDirection(TrainEntity.Direction.BACKWARD);
                break;
            case SPEED_UP:
                train.setTargetSpeed(train.getTargetSpeed() + 5.0);
                break;
            case SPEED_DOWN:
                train.setTargetSpeed(Math.max(0.0, train.getTargetSpeed() - 5.0));
                break;
            case COAST:
                train.setTargetSpeed(train.getCurrentSpeed()); // 惰行：保持当前速度
                break;
            // ========== 安全保障 ==========
            case EMERGENCY_BRAKE:
                train.triggerEmergencyBrake();
                break;
            case QUICK_BRAKE:
                train.setBrakeState(TrainEntity.BrakeState.QUICK);
                break;
            case FORCE_RELEASE:
                train.releaseEmergencyBrake();
                train.setBrakeState(TrainEntity.BrakeState.NORMAL);
                break;
            case VIGILANCE:
                // 警惕按钮：重置警惕计时器（模组可扩展）
                break;
            // ========== 车门控制 ==========
            case OPEN_DOORS:
            case OPEN_LEFT_DOOR:
            case OPEN_RIGHT_DOOR:
                handleDoors(train, true);
                break;
            case CLOSE_DOORS:
            case CLOSE_LEFT_DOOR:
            case CLOSE_RIGHT_DOOR:
                handleDoors(train, false);
                break;
            case REOPEN_CLOSE_DOOR:
                handleDoors(train, true);
                // 再开闭门：先开再关，简化为先开门（关门由玩家再次操作）
                break;
            // ========== 照明控制 ==========
            case CAB_LIGHT:
                train.setCabLightOn(!train.isCabLightOn());
                break;
            case GAUGE_LIGHT:
                // 仪表灯（可与司机室灯联动，暂独立）
                break;
            case HEADLIGHT:
                train.setHeadlightOn(!train.isHeadlightOn());
                break;
            case HEADLIGHT_DIM:
                train.setHeadlightDim(!train.isHeadlightDim());
                break;
            case PASSENGER_LIGHT:
                train.setPassengerLightOn(!train.isPassengerLightOn());
                break;
            case LIGHT_TEST:
                // 灯测试：短暂点亮所有灯（可扩展）
                break;
            // ========== 辅助功能 ==========
            case HORN:
                playHorn(train);
                break;
            case PANTOGRAPH_UP:
                train.setPantographUp(true);
                break;
            case PANTOGRAPH_DOWN:
                train.setPantographUp(false);
                train.setEngineOn(false);
                break;
            case SANDING:
                // 撒砂：增加粘着（可扩展音效）
                break;
            case WIPER:
                // 雨刷（可扩展视觉效果）
                break;
            case PA_BROADCAST:
                // PA 广播（可扩展）
                break;
            case SLEEP:
                train.setEngineOn(false);
                train.setPantographUp(false);
                break;
            case WAKE:
                train.setPantographUp(true);
                break;
            case UNCOUPLE:
                // 解钩（多编组时使用，可扩展）
                break;
            case CUSTOM:
                // 由追加包 buttonLogic 配置处理
                break;
        }
    }
    
    /**
     * 检查玩家是否有驾驶权限
     */
    public boolean hasDrivingPermission(PlayerEntity player, TrainEntity train) {
        UUID playerId = player.getUuid();
        return trainDriverBindings.get(train) == playerId;
    }
    
    /**
     * 获取玩家当前绑定的列车
     */
    public TrainEntity getPlayerTrain(PlayerEntity player) {
        return playerTrainBindings.get(player.getUuid());
    }
    
    /**
     * 处理开关车门/屏蔽门：在列车附近查找屏蔽门并开关，同时播放音效
     */
    private void handleDoors(TrainEntity train, boolean open) {
        World world = train.getWorld();
        if (world.isClient) {
            return;
        }
        
        BlockPos center = train.getBlockPos();
        int radius = 5;
        int affected = 0;
        
        for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
            for (int y = center.getY() - 2; y <= center.getY() + 2; y++) {
                for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    var state = world.getBlockState(pos);
                    if (state.getBlock() instanceof com.real.rail.transit.station.ShieldDoorBlock shieldDoor) {
                        if (open) {
                            shieldDoor.open(world, pos);
                        } else {
                            shieldDoor.close(world, pos);
                        }
                        affected++;
                    }
                }
            }
        }
        
        if (affected > 0) {
            world.playSound(
                null,
                train.getX(),
                train.getY(),
                train.getZ(),
                open ? SoundSystem.DOOR_OPEN : SoundSystem.DOOR_CLOSE,
                SoundCategory.BLOCKS,
                1.0f,
                1.0f
            );
        }
    }
    
    /**
     * 循环切换驾驶模式：ATO → ATB → IATP → CBTC → ATP → ATC → RM → MANUAL → OFF → ATO
     */
    private void cycleDrivingMode(TrainEntity train) {
        TrainEntity.DrivingMode currentMode = train.getDrivingMode();
        TrainEntity.DrivingMode[] modes = TrainEntity.DrivingMode.values();
        int nextIndex = (currentMode.ordinal() + 1) % modes.length;
        train.setDrivingMode(modes[nextIndex]);
    }

    /**
     * 播放列车鸣笛音效
     */
    private void playHorn(TrainEntity train) {
        World world = train.getWorld();
        if (world.isClient) {
            return;
        }
        world.playSound(
            null,
            train.getX(),
            train.getY(),
            train.getZ(),
            SoundSystem.TRAIN_HORN,
            SoundCategory.BLOCKS,
            1.5f,
            1.0f
        );
    }
}

