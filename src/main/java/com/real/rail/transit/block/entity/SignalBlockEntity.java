package com.real.rail.transit.block.entity;

import com.real.rail.transit.RealRailTransitMod;
import com.real.rail.transit.block.SignalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * 信号机方块实体
 * 用于存储信号机的状态和渲染数据
 */
public class SignalBlockEntity extends BlockEntity {
    public static BlockEntityType<SignalBlockEntity> TYPE;
    
    public static void register() {
        TYPE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(RealRailTransitMod.MOD_ID, "signal"),
            BlockEntityType.Builder.create(SignalBlockEntity::new, com.real.rail.transit.registry.ModBlocks.SIGNAL).build()
        );
    }
    
    public SignalBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }
}

