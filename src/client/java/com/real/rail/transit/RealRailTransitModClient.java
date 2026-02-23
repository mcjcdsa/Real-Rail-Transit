package com.real.rail.transit;

import com.real.rail.transit.client.renderer.ModBlockEntityRenderers;
import com.real.rail.transit.client.renderer.PresetRenderer;
import com.real.rail.transit.client.renderer.TrainEntityRenderer;
import com.real.rail.transit.client.screen.ModScreens;
import com.real.rail.transit.registry.ModEntities;
import com.real.rail.transit.sound.SoundSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class RealRailTransitModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		
		RealRailTransitMod.LOGGER.info("Real Rail Transit 模组客户端正在初始化...");
		
		// 注册实体渲染器
		EntityRendererRegistry.register(ModEntities.TRAIN, TrainEntityRenderer::new);
		
		// 注册方块实体渲染器
		ModBlockEntityRenderers.register();
		
		// 注册GUI界面
		ModScreens.register();
		
		// 注册预设器渲染器
		WorldRenderEvents.AFTER_TRANSLUCENT.register((WorldRenderContext context) -> {
			PresetRenderer.render(context.matrixStack(), context.camera());
		});
		
		// 初始化音效系统
		SoundSystem.initialize();
		
		RealRailTransitMod.LOGGER.info("Real Rail Transit 模组客户端初始化完成！");
	}
}
