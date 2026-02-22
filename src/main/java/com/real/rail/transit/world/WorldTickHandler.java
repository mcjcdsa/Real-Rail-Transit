package com.real.rail.transit.world;

import com.real.rail.transit.system.DispatchSystem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;

/**
 * 世界Tick处理器
 * 处理每tick需要更新的系统
 */
public class WorldTickHandler {
    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> onWorldTick(world));
    }
    
    private static void onWorldTick(ServerWorld world) {
        // 更新调度系统
        DispatchSystem.getInstance().update(world);
    }
}

