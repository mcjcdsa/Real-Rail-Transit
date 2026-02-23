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
import net.minecraft.world.World;

/**
 * 传感器方块实体
 * 检测列车通过并更新状态
 */
public class SensorControllerBlockEntity extends BlockEntity {
    public static BlockEntityType<SensorControllerBlockEntity> TYPE;
    
    private long lastDetectionTime = 0;
    private String lastTrainId = "";
    
    public static void register() {
        TYPE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(RealRailTransitMod.MOD_ID, "sensor_controller"),
            BlockEntityType.Builder.create(SensorControllerBlockEntity::new, com.real.rail.transit.registry.ModBlocks.SENSOR_CONTROLLER).build()
        );
    }
    
    public SensorControllerBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }
    
    /**
     * 检测列车
     * 在服务器端每tick调用
     */
    public static void tick(World world, BlockPos pos, BlockState state, SensorControllerBlockEntity blockEntity) {
        if (world.isClient) {
            return;
        }
        
        // 检测范围内的列车实体
        net.minecraft.util.math.Box detectionBox = new net.minecraft.util.math.Box(pos).expand(0.5);
        boolean detected = false;
        String trainId = "";
        
        for (net.minecraft.entity.Entity entity : world.getOtherEntities(null, detectionBox)) {
            if (entity instanceof com.real.rail.transit.entity.TrainEntity train) {
                detected = true;
                trainId = train.getUuidAsString();
                blockEntity.lastTrainId = trainId;
                blockEntity.lastDetectionTime = world.getTime();
                break;
            }
        }
        
        // 更新方块状态
        boolean currentDetected = state.get(com.real.rail.transit.block.SensorControllerBlock.DETECTED);
        if (detected != currentDetected) {
            world.setBlockState(pos, state.with(com.real.rail.transit.block.SensorControllerBlock.DETECTED, detected));
            blockEntity.markDirty();
        }
    }
    
    public long getLastDetectionTime() {
        return lastDetectionTime;
    }
    
    public String getLastTrainId() {
        return lastTrainId;
    }
    
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putLong("lastDetectionTime", lastDetectionTime);
        nbt.putString("lastTrainId", lastTrainId);
    }
    
    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if (nbt.contains("lastDetectionTime")) {
            lastDetectionTime = nbt.getLong("lastDetectionTime");
        }
        if (nbt.contains("lastTrainId")) {
            lastTrainId = nbt.getString("lastTrainId");
        }
    }
}

