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
 * 到站显示屏方块实体
 * 存储到站信息
 */
public class ArrivalDisplayScreenBlockEntity extends BlockEntity {
    public static BlockEntityType<ArrivalDisplayScreenBlockEntity> TYPE;
    
    private String trainId = "KML-001";
    private String destination = "昆明南站";
    private int nextArrivalTime = 5; // 分钟
    private String platform = "1号站台";
    private String customText = ""; // 自定义显示文本
    private int textColor = 0xFFFFFF; // 文本颜色
    
    public static void register() {
        TYPE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(RealRailTransitMod.MOD_ID, "arrival_display_screen"),
            BlockEntityType.Builder.create(ArrivalDisplayScreenBlockEntity::new, com.real.rail.transit.registry.ModBlocks.ARRIVAL_DISPLAY_SCREEN).build()
        );
    }
    
    public ArrivalDisplayScreenBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }
    
    public String getTrainId() {
        return trainId;
    }
    
    public void setTrainId(String trainId) {
        this.trainId = trainId;
        this.markDirty();
    }
    
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
        this.markDirty();
    }
    
    public int getNextArrivalTime() {
        return nextArrivalTime;
    }
    
    public void setNextArrivalTime(int minutes) {
        this.nextArrivalTime = minutes;
        this.markDirty();
    }
    
    public String getPlatform() {
        return platform;
    }
    
    public void setPlatform(String platform) {
        this.platform = platform;
        this.markDirty();
    }
    
    public String getCustomText() {
        return customText;
    }
    
    public void setCustomText(String text) {
        this.customText = text != null ? text : "";
        this.markDirty();
    }
    
    public int getTextColor() {
        return textColor;
    }
    
    public void setTextColor(int color) {
        this.textColor = color;
        this.markDirty();
    }
    
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putString("trainId", trainId);
        nbt.putString("destination", destination);
        nbt.putInt("nextArrivalTime", nextArrivalTime);
        nbt.putString("platform", platform);
        nbt.putString("customText", customText);
        nbt.putInt("textColor", textColor);
    }
    
    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if (nbt.contains("trainId")) {
            trainId = nbt.getString("trainId");
        }
        if (nbt.contains("destination")) {
            destination = nbt.getString("destination");
        }
        if (nbt.contains("nextArrivalTime")) {
            nextArrivalTime = nbt.getInt("nextArrivalTime");
        }
        if (nbt.contains("platform")) {
            platform = nbt.getString("platform");
        }
        if (nbt.contains("customText")) {
            customText = nbt.getString("customText");
        }
        if (nbt.contains("textColor")) {
            textColor = nbt.getInt("textColor");
        }
    }
}

