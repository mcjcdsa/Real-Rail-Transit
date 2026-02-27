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
 * 站点标记方块实体
 * 存储站点 ID 与显示名称，供调度/显示屏/统计等系统使用。
 */
public class StationMarkerBlockEntity extends BlockEntity {

    public static BlockEntityType<StationMarkerBlockEntity> TYPE;

    private String stationId = "";
    private String stationName = "";

    public static void register() {
        TYPE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(RealRailTransitMod.MOD_ID, "station_marker"),
            BlockEntityType.Builder.create(StationMarkerBlockEntity::new,
                com.real.rail.transit.registry.ModBlocks.STATION_MARKER).build()
        );
    }

    public StationMarkerBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
        this.stationId = "st_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
    }

    public String getStationId() {
        return stationId != null ? stationId : "";
    }

    public void setStationId(String stationId) {
        this.stationId = stationId != null ? stationId : "";
        this.markDirty();
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName != null ? stationName : "";
        this.markDirty();
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putString("stationId", stationId);
        nbt.putString("stationName", stationName);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if (nbt.contains("stationId")) {
            stationId = nbt.getString("stationId");
        }
        if (nbt.contains("stationName")) {
            stationName = nbt.getString("stationName");
        }
    }
}


