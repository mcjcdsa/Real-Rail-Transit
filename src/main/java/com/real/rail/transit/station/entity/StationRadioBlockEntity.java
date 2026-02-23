package com.real.rail.transit.station.entity;

import com.real.rail.transit.RealRailTransitMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * 车站广播器方块实体
 * 存储广播内容和播放状态
 */
public class StationRadioBlockEntity extends BlockEntity {
    public static BlockEntityType<StationRadioBlockEntity> TYPE;
    
    private String broadcastMessage = "欢迎乘坐轨道交通，请注意安全";
    private String soundId = ""; // 音频资源ID，例如 "real-rail-transit-mod:station_announcement"
    private boolean isPlaying = false;
    private float volume = 1.0f;
    
    public static void register() {
        TYPE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(RealRailTransitMod.MOD_ID, "station_radio"),
            BlockEntityType.Builder.create(StationRadioBlockEntity::new, com.real.rail.transit.registry.ModBlocks.STATION_RADIO).build()
        );
    }
    
    public StationRadioBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }
    
    public String getBroadcastMessage() {
        return broadcastMessage;
    }
    
    public void setBroadcastMessage(String message) {
        this.broadcastMessage = message;
        this.markDirty();
    }
    
    public boolean isPlaying() {
        return isPlaying;
    }
    
    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
        this.markDirty();
    }
    
    public float getVolume() {
        return volume;
    }
    
    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        this.markDirty();
    }
    
    public String getSoundId() {
        return soundId;
    }
    
    public void setSoundId(String soundId) {
        this.soundId = soundId != null ? soundId : "";
        this.markDirty();
    }
    
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putString("broadcastMessage", broadcastMessage);
        nbt.putString("soundId", soundId);
        nbt.putBoolean("isPlaying", isPlaying);
        nbt.putFloat("volume", volume);
    }
    
    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if (nbt.contains("broadcastMessage")) {
            broadcastMessage = nbt.getString("broadcastMessage");
        }
        if (nbt.contains("soundId")) {
            soundId = nbt.getString("soundId");
        }
        if (nbt.contains("isPlaying")) {
            isPlaying = nbt.getBoolean("isPlaying");
        }
        if (nbt.contains("volume")) {
            volume = nbt.getFloat("volume");
        }
    }
}

