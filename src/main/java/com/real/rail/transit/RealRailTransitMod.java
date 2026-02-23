package com.real.rail.transit;

import com.real.rail.transit.addon.AddonManager;
import com.real.rail.transit.network.ModNetworkPackets;
import com.real.rail.transit.registry.ModBlockEntities;
import com.real.rail.transit.registry.ModBlocks;
import com.real.rail.transit.registry.ModEntities;
import com.real.rail.transit.registry.ModItemGroups;
import com.real.rail.transit.registry.ModItems;
import com.real.rail.transit.registry.ModScreenHandlers;
import com.real.rail.transit.world.WorldTickHandler;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RealRailTransitMod implements ModInitializer {
	public static final String MOD_ID = "real-rail-transit-mod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Real Rail Transit 模组正在初始化...");
		
		// 注册方块
		ModBlocks.registerModBlocks();
		
		// 注册方块实体
		ModBlockEntities.register();
		
		// 注册GUI处理器
		ModScreenHandlers.register();
		
		// 注册网络包
		ModNetworkPackets.register();
		
		// 注册物品
		ModItems.registerModItems();
		
		// 注册物品组（创造模式物品栏）
		ModItemGroups.registerModItemGroups();
		
		// 注册实体
		ModEntities.registerModEntities();
		
		// 注册世界Tick处理器
		WorldTickHandler.register();
		
		// 初始化音效系统
		com.real.rail.transit.sound.SoundSystem.initialize();
		
		// 加载追加包
		AddonManager.getInstance().loadAllAddons();
		
		// 初始化各个子系统
		initializeSystems();
		
		LOGGER.info("Real Rail Transit 模组初始化完成！");
	}
	
	private void initializeSystems() {
		LOGGER.info("正在初始化核心系统...");
		// 各个系统会在需要时自动初始化（单例模式）
		// SignalSystem.getInstance()
		// PowerSystem.getInstance()
		// DispatchSystem.getInstance()
		// CabInteractionSystem.getInstance()
		// TrainMovementSystem.getInstance()
	}
}