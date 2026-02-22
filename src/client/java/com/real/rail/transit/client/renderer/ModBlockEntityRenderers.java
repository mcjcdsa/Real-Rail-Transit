package com.real.rail.transit.client.renderer;

import com.real.rail.transit.block.entity.SignalBlockEntity;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

/**
 * 方块实体渲染器注册类
 */
public class ModBlockEntityRenderers {
    public static void register() {
        BlockEntityRendererRegistry.register(SignalBlockEntity.TYPE, SignalBlockEntityRenderer::new);
    }
}



