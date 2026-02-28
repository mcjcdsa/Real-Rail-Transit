package com.real.rail.transit.registry;

import com.real.rail.transit.RealRailTransitMod;
import com.real.rail.transit.item.ControlPanelItem;
import com.real.rail.transit.item.PresetItem;
import com.real.rail.transit.item.SettingControllerItem;
import com.real.rail.transit.item.ShieldDoorKeyItem;
import com.real.rail.transit.item.StationBrushItem;
import com.real.rail.transit.item.StationConstructionControlPanelItem;
import com.real.rail.transit.item.TicketCardItem;
import com.real.rail.transit.item.TrackBrushItem;
import com.real.rail.transit.item.TrackConstructionControlPanelItem;
import com.real.rail.transit.item.TrackControlPanelItem;
import com.real.rail.transit.item.TrainPanelItem;
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
    
    // 车站刷子
    public static final Item STATION_BRUSH = registerItem("station_brush",
        new StationBrushItem(new Item.Settings()));
    
    // 设置器
    public static final Item SETTING_CONTROLLER = registerItem("setting_controller",
        new SettingControllerItem(new Item.Settings()));
    
    // 线路建设控制面板（只能右键打开GUI，不能放置）
    public static final Item TRACK_CONSTRUCTION_CONTROL_PANEL = registerItem("track_construction_control_panel",
        new TrackConstructionControlPanelItem(new Item.Settings()));
    
    // 车站建设控制面板（只能右键打开GUI，不能放置）
    public static final Item STATION_CONSTRUCTION_CONTROL_PANEL = registerItem("station_construction_control_panel",
        new StationConstructionControlPanelItem(new Item.Settings()));
    
    // 线路控制面板（只能右键打开GUI，不能放置）
    public static final Item TRACK_CONTROL_PANEL = registerItem("track_control_panel",
        new TrackControlPanelItem(new Item.Settings()));
    
    // 列车面板（只能右键打开GUI，不能放置）
    public static final Item TRAIN_PANEL = registerItem("train_panel",
        new TrainPanelItem(new Item.Settings()));
    
    // 控制面板（只能右键打开GUI，不能放置）
    public static final Item CONTROL_PANEL = registerItem("control_panel",
        new ControlPanelItem(new Item.Settings()));
    
    // 票卡（用于刷闸机进站，不能放置）
    public static final Item TICKET_CARD = registerItem("ticket_card",
        new TicketCardItem(new Item.Settings().maxCount(64)));
    
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

