package com.real.rail.transit.registry;

import com.real.rail.transit.RealRailTransitMod;
import com.real.rail.transit.item.PresetItem;
import com.real.rail.transit.item.ShieldDoorKeyItem;
import com.real.rail.transit.item.TrackBrushItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * 模组物品注册类
 */
public class ModItems {
    // 预设器
    public static final Item PRESET = registerItem("preset",
        new PresetItem(new Item.Settings()));
    
    // 线路刷子
    public static final Item TRACK_BRUSH = registerItem("track_brush",
        new TrackBrushItem(new Item.Settings()));
    
    // 屏蔽门钥匙
    public static final Item SHIELD_DOOR_KEY = registerItem("shield_door_key",
        new ShieldDoorKeyItem(new Item.Settings()));
    
    /**
     * 注册物品
     */
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM,
            Identifier.of(RealRailTransitMod.MOD_ID, name), item);
    }
    
    /**
     * 初始化注册（在模组初始化时调用）
     */
    public static void registerModItems() {
        RealRailTransitMod.LOGGER.info("正在注册 Real Rail Transit 模组物品...");
    }
}

