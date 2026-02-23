package com.real.rail.transit.block.entity;

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
 * 列车供电设置器方块实体
 * 存储供电配置信息
 */
public class TrainPowerSettingControllerBlockEntity extends BlockEntity {
    public static BlockEntityType<TrainPowerSettingControllerBlockEntity> TYPE;
    
    private String powerType = "catenary"; // 供电类型：catenary（接触网）或 third_rail（第三轨）
    private float voltage = 1500.0f; // 电压（伏特）
    private float maxCurrent = 1000.0f; // 最大电流（安培）
    
    public static void register() {
        TYPE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(RealRailTransitMod.MOD_ID, "train_power_setting_controller"),
            BlockEntityType.Builder.create(TrainPowerSettingControllerBlockEntity::new, com.real.rail.transit.registry.ModBlocks.TRAIN_POWER_SETTING_CONTROLLER).build()
        );
    }
    
    public TrainPowerSettingControllerBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }
    
    public String getPowerType() {
        return powerType;
    }
    
    public void setPowerType(String powerType) {
        this.powerType = powerType;
        this.markDirty();
    }
    
    public float getVoltage() {
        return voltage;
    }
    
    public void setVoltage(float voltage) {
        this.voltage = voltage;
        this.markDirty();
    }
    
    public float getMaxCurrent() {
        return maxCurrent;
    }
    
    public void setMaxCurrent(float maxCurrent) {
        this.maxCurrent = maxCurrent;
        this.markDirty();
    }
    
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putString("powerType", powerType);
        nbt.putFloat("voltage", voltage);
        nbt.putFloat("maxCurrent", maxCurrent);
    }
    
    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if (nbt.contains("powerType")) {
            powerType = nbt.getString("powerType");
        }
        if (nbt.contains("voltage")) {
            voltage = nbt.getFloat("voltage");
        }
        if (nbt.contains("maxCurrent")) {
            maxCurrent = nbt.getFloat("maxCurrent");
        }
    }
}

