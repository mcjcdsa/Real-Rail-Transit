package com.real.rail.transit;

import com.real.rail.transit.client.renderer.ModBlockEntityRenderers;
import com.real.rail.transit.client.renderer.PresetRenderer;
import com.real.rail.transit.client.renderer.TrainEntityRenderer;
import com.real.rail.transit.client.renderer.TrainPlacementRenderer;
import com.real.rail.transit.client.TrainPlacementHandler;
import com.real.rail.transit.client.TrainPlacementConfig;
import com.real.rail.transit.client.AddonManagerConfig;
import com.real.rail.transit.client.AddonManagerHandler;
import com.real.rail.transit.client.ModLanguageConfig;
import com.real.rail.transit.client.TrainControlConfig;
import com.real.rail.transit.client.TrainControlHandler;
import com.real.rail.transit.client.hud.TrainSpeedHUD;
import com.real.rail.transit.client.hud.TrainPlacementHUD;
import com.real.rail.transit.client.screen.ModScreens;
import com.real.rail.transit.network.ModNetworkPackets;
import com.real.rail.transit.registry.ModEntities;
import com.real.rail.transit.sound.SoundSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
		
		// 注册客户端网络包接收器
		registerClientNetworkHandlers();
		
		// 注册预设器渲染器
		WorldRenderEvents.AFTER_TRANSLUCENT.register((WorldRenderContext context) -> {
			PresetRenderer.render(context.matrixStack(), context.camera());
		});
		
		// 注册列车放置预览渲染器
		WorldRenderEvents.AFTER_TRANSLUCENT.register((WorldRenderContext context) -> {
			TrainPlacementRenderer.render(context.matrixStack(), context.camera());
		});
		
		// 注册列车放置键位绑定
		TrainPlacementConfig.register();
		
		// 注册列车放置处理器
		TrainPlacementHandler.register();
		
		// 注册列车放置HUD
		TrainPlacementHUD.register();
		
		// 注册列车控制键位绑定
		TrainControlConfig.register();
		
		// 注册列车控制处理器
		TrainControlHandler.register();
		
		// 注册列车速度HUD
		TrainSpeedHUD.register();
		
		// 注册追加包管理键位绑定
		AddonManagerConfig.register();
		
		// 注册追加包管理处理器
		AddonManagerHandler.register();
		
		// 初始化音效系统
		SoundSystem.initialize();
		
		// 加载语言配置
		ModLanguageConfig.load();
		
		RealRailTransitMod.LOGGER.info("Real Rail Transit 模组客户端初始化完成！");
	}
	
	/**
	 * 注册客户端网络包处理器
	 */
	private static void registerClientNetworkHandlers() {
		// 注册线路控制面板统计数据接收器
		// 注意：实际的接收器在TrackControlPanelScreen中注册，因为需要访问Screen实例
		// 这里可以注册全局的接收器
		ClientPlayNetworking.registerGlobalReceiver(ModNetworkPackets.TrackControlStatsResponsePayload.ID, 
			(payload, context) -> {
				// 这个接收器会在TrackControlPanelScreen中重新注册以访问Screen实例
				// 这里可以处理全局的统计数据更新
			});
		
		// 注册列车速度更新接收器
		ClientPlayNetworking.registerGlobalReceiver(ModNetworkPackets.TrainSpeedUpdatePayload.ID,
			(payload, context) -> {
				TrainSpeedHUD.updateSpeed(payload.trainId(), payload.currentSpeed(), payload.targetSpeed(), payload.maxSpeed());
			});
	}
}
