package com.real.rail.transit.sound;

import com.real.rail.transit.RealRailTransitMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * 音效系统
 * 管理模组的所有音效资源
 */
public class SoundSystem {
    // 列车运行音效
    public static final SoundEvent TRAIN_RUNNING = registerSoundEvent("train_running");
    public static final SoundEvent TRAIN_RAIL_IMPACT = registerSoundEvent("train_rail_impact");
    public static final SoundEvent TRAIN_POWER_COLLECT = registerSoundEvent("train_power_collect");
    
    // 操作音效
    public static final SoundEvent DOOR_OPEN = registerSoundEvent("door_open");
    public static final SoundEvent DOOR_CLOSE = registerSoundEvent("door_close");
    public static final SoundEvent BRAKE_NORMAL = registerSoundEvent("brake_normal");
    public static final SoundEvent BRAKE_QUICK = registerSoundEvent("brake_quick");
    public static final SoundEvent BRAKE_EMERGENCY = registerSoundEvent("brake_emergency");
    public static final SoundEvent BUTTON_CLICK = registerSoundEvent("button_click");
    
    // 提示音效
    public static final SoundEvent ALARM = registerSoundEvent("alarm");
    public static final SoundEvent ARRIVAL_NOTIFICATION = registerSoundEvent("arrival_notification");
    public static final SoundEvent SIGNAL_CHANGE = registerSoundEvent("signal_change");
    public static final SoundEvent TRAIN_HORN = registerSoundEvent("train_horn");
    
    // 车站音效
    public static final SoundEvent STATION_BROADCAST = registerSoundEvent("station_broadcast");
    public static final SoundEvent GATE_BEEP = registerSoundEvent("gate_beep");
    
    /**
     * 注册音效事件
     */
    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.of(RealRailTransitMod.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
    
    /**
     * 初始化音效系统
     */
    public static void initialize() {
        RealRailTransitMod.LOGGER.info("正在注册 Real Rail Transit 模组音效...");
    }
}




