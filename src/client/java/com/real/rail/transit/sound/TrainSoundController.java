package com.real.rail.transit.sound;

import com.real.rail.transit.entity.TrainEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;

/**
 * 列车音效控制器
 * 根据列车状态播放相应的音效
 * 客户端专用类
 */
public class TrainSoundController {
    private static final float BASE_VOLUME = 0.5f;
    private static final float BASE_PITCH = 1.0f;
    
    /**
     * 播放列车运行音效
     */
    public static void playRunningSound(TrainEntity train) {
        if (train.getWorld().isClient) {
            Vec3d pos = train.getPos();
            float volume = BASE_VOLUME * (float) Math.min(train.getCurrentSpeed() / 30.0, 1.0);
            float pitch = BASE_PITCH + (float) (train.getCurrentSpeed() / 100.0);
            
            MinecraftClient.getInstance().getSoundManager().play(
                new PositionedSoundInstance(
                    SoundSystem.TRAIN_RUNNING,
                    SoundCategory.NEUTRAL,
                    volume,
                    pitch,
                    train.getWorld().getRandom(),
                    pos.x,
                    pos.y,
                    pos.z
                )
            );
        }
    }
    
    /**
     * 播放制动音效
     */
    public static void playBrakeSound(TrainEntity train, TrainEntity.BrakeState brakeState) {
        if (train.getWorld().isClient) {
            Vec3d pos = train.getPos();
            SoundEvent soundEvent;
            
            switch (brakeState) {
                case EMERGENCY:
                    soundEvent = SoundSystem.BRAKE_EMERGENCY;
                    break;
                case QUICK:
                    soundEvent = SoundSystem.BRAKE_QUICK;
                    break;
                case NORMAL:
                default:
                    soundEvent = SoundSystem.BRAKE_NORMAL;
                    break;
            }
            
            MinecraftClient.getInstance().getSoundManager().play(
                new PositionedSoundInstance(
                    soundEvent,
                    SoundCategory.NEUTRAL,
                    BASE_VOLUME,
                    BASE_PITCH,
                    train.getWorld().getRandom(),
                    pos.x,
                    pos.y,
                    pos.z
                )
            );
        }
    }
    
    /**
     * 播放开关门音效
     */
    public static void playDoorSound(net.minecraft.util.math.BlockPos pos, boolean open) {
        if (MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().world.isClient) {
            SoundEvent soundEvent = open ? SoundSystem.DOOR_OPEN : SoundSystem.DOOR_CLOSE;
            MinecraftClient.getInstance().getSoundManager().play(
                new PositionedSoundInstance(
                    soundEvent,
                    SoundCategory.BLOCKS,
                    BASE_VOLUME,
                    BASE_PITCH,
                    MinecraftClient.getInstance().world.getRandom(),
                    pos.getX(),
                    pos.getY(),
                    pos.getZ()
                )
            );
        }
    }
    
    /**
     * 播放按钮点击音效
     */
    public static void playButtonClickSound(net.minecraft.util.math.BlockPos pos) {
        if (MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().world.isClient) {
            MinecraftClient.getInstance().getSoundManager().play(
                new PositionedSoundInstance(
                    SoundSystem.BUTTON_CLICK,
                    SoundCategory.BLOCKS,
                    0.3f,
                    BASE_PITCH,
                    MinecraftClient.getInstance().world.getRandom(),
                    pos.getX(),
                    pos.getY(),
                    pos.getZ()
                )
            );
        }
    }
    
    /**
     * 播放鸣笛音效
     */
    public static void playHornSound(TrainEntity train) {
        if (train.getWorld().isClient) {
            Vec3d pos = train.getPos();
            MinecraftClient.getInstance().getSoundManager().play(
                new PositionedSoundInstance(
                    SoundSystem.TRAIN_HORN,
                    SoundCategory.NEUTRAL,
                    1.0f,
                    BASE_PITCH,
                    train.getWorld().getRandom(),
                    pos.x,
                    pos.y,
                    pos.z
                )
            );
        }
    }
}

