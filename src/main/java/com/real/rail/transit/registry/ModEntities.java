package com.real.rail.transit.registry;

import com.real.rail.transit.RealRailTransitMod;
import com.real.rail.transit.entity.TrainEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * 模组实体注册类
 */
public class ModEntities {
    public static final EntityType<TrainEntity> TRAIN = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(RealRailTransitMod.MOD_ID, "train"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, TrainEntity::new)
            .dimensions(EntityDimensions.fixed(2.5f, 3.5f))
            .build()
    );
    
    /**
     * 初始化注册（在模组初始化时调用）
     */
    public static void registerModEntities() {
        RealRailTransitMod.LOGGER.info("正在注册 Real Rail Transit 模组实体...");
    }
}

