package com.real.rail.transit.registry;

import com.real.rail.transit.RealRailTransitMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * 模组物品组（创造模式物品栏分类）
 */
public class ModItemGroups {
    /**
     * 线路建设类物品组
     */
    public static final ItemGroup TRACK_CONSTRUCTION = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(RealRailTransitMod.MOD_ID, "track_construction"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.real-rail-transit-mod.track_construction"))
            .icon(() -> new ItemStack(ModBlocks.TRACK))
            .entries((displayContext, entries) -> {
                entries.add(ModBlocks.TRACK);
                entries.add(ModBlocks.SIGNAL);
                entries.add(ModBlocks.TURNOUT);
                entries.add(ModBlocks.THIRD_RAIL);
                entries.add(ModBlocks.CONTACT_NETWORK);
                entries.add(ModBlocks.CONNECTION_PART);
                entries.add(ModBlocks.SWITCH);
                entries.add(ModBlocks.GUARD_TRACK);
                entries.add(ModBlocks.CABLE);
                entries.add(ModBlocks.TRAIN_POWER_SETTING_CONTROLLER);
                entries.add(ModBlocks.DAOJI_MACHINE);
                entries.add(ModBlocks.CLEANER);
                entries.add(ModBlocks.SIGNAL_LAYOUT_CONTROLLER);
                entries.add(ModItems.TRACK_CONSTRUCTION_CONTROL_PANEL);
            })
            .build()
    );
    
    /**
     * 车站设施类物品组
     */
    public static final ItemGroup STATION_FACILITIES = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(RealRailTransitMod.MOD_ID, "station_facilities"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.real-rail-transit-mod.station_facilities"))
            .icon(() -> new ItemStack(ModBlocks.UPPER_SHIELD_DOOR))
            .entries((displayContext, entries) -> {
                entries.add(ModBlocks.UPPER_SHIELD_DOOR);
                entries.add(ModBlocks.LOWER_SHIELD_DOOR);
                entries.add(ModBlocks.ELEVATOR);
                entries.add(ModBlocks.AUTOMATIC_STAIRCASE);
                entries.add(ModBlocks.DISPLAY_SCREEN);
                entries.add(ModBlocks.ARRIVAL_DISPLAY_SCREEN);
                entries.add(ModBlocks.TICKET_MACHINE);
                entries.add(ModBlocks.GATE);
                entries.add(ModBlocks.SMALL_TV);
                entries.add(ModBlocks.STATION_RADIO);
                entries.add(ModBlocks.STATION_MARKER);
                entries.add(ModBlocks.ELEVATOR_TRACK);
                entries.add(ModBlocks.ELECTRONIC_DOOR_CONTROLLER);
                entries.add(ModBlocks.SENSOR_CONTROLLER);
                entries.add(ModBlocks.SENSOR_LAYOUT_CONTROLLER);
                entries.add(ModItems.STATION_CONSTRUCTION_CONTROL_PANEL);
            })
            .build()
    );
    
    /**
     * 车站建筑类物品组
     */
    public static final ItemGroup STATION_BUILDING = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(RealRailTransitMod.MOD_ID, "station_building"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.real-rail-transit-mod.station_building"))
            .icon(() -> new ItemStack(ModBlocks.BARRICADE))
            .entries((displayContext, entries) -> {
                entries.add(ModBlocks.BARRICADE);
                entries.add(ModBlocks.STAIRCASE_STEP);
                entries.add(ModBlocks.GLASS_WALL);
                entries.add(ModBlocks.TILE_WALL);
                entries.add(ModBlocks.CEILING);
                entries.add(ModBlocks.LIT_CEILING);
                entries.add(ModBlocks.POSTAGE);
                entries.add(ModBlocks.FIRE_EXTINGUISHER);
                entries.add(ModBlocks.FIRE_WATER);
            })
            .build()
    );
    
    /**
     * 控制面板类物品组
     */
    public static final ItemGroup CONTROL_PANELS = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(RealRailTransitMod.MOD_ID, "control_panels"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.real-rail-transit-mod.control_panels"))
            .icon(() -> new ItemStack(ModItems.CONTROL_PANEL))
            .entries((displayContext, entries) -> {
                entries.add(ModItems.TRACK_CONSTRUCTION_CONTROL_PANEL);
                entries.add(ModItems.STATION_CONSTRUCTION_CONTROL_PANEL);
                entries.add(ModItems.TRACK_CONTROL_PANEL);
                entries.add(ModItems.TRAIN_PANEL);
                entries.add(ModItems.CONTROL_PANEL);
            })
            .build()
    );
    
    /**
     * 工具物品类物品组
     */
    public static final ItemGroup TOOLS = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(RealRailTransitMod.MOD_ID, "tools"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.real-rail-transit-mod.tools"))
            .icon(() -> new ItemStack(ModItems.PRESET))
            .entries((displayContext, entries) -> {
                entries.add(ModItems.PRESET);
                entries.add(ModItems.TRACK_BRUSH);
                entries.add(ModItems.STATION_BRUSH);
                entries.add(ModItems.SETTING_CONTROLLER);
                entries.add(ModItems.SHIELD_DOOR_KEY);
            })
            .build()
    );
    
    /**
     * 其他物品类物品组
     */
    public static final ItemGroup OTHER_ITEMS = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(RealRailTransitMod.MOD_ID, "other_items"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.real-rail-transit-mod.other_items"))
            .icon(() -> new ItemStack(ModItems.TICKET_CARD))
            .entries((displayContext, entries) -> {
                entries.add(ModItems.TICKET_CARD);
            })
            .build()
    );
    
    /**
     * 初始化物品组（在模组初始化时调用）
     */
    public static void registerModItemGroups() {
        RealRailTransitMod.LOGGER.info("正在注册 Real Rail Transit 模组物品组...");
    }
}

