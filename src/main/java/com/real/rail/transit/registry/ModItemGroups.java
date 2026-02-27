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
     * Real Rail Transit 主物品组
     */
    public static final ItemGroup REAL_RAIL_TRANSIT = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(RealRailTransitMod.MOD_ID, "main"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.real-rail-transit-mod.main"))
            .icon(() -> new ItemStack(ModBlocks.TRACK))
            .entries((displayContext, entries) -> {
                // ========== 线路建设类 ==========
                entries.add(ModBlocks.TRACK);
                entries.add(ModBlocks.SIGNAL);
                entries.add(ModBlocks.TURNOUT);
                entries.add(ModBlocks.CONNECTION_PART);
                entries.add(ModBlocks.SWITCH);
                entries.add(ModBlocks.GUARD_TRACK);
                entries.add(ModBlocks.CABLE);
                entries.add(ModBlocks.THIRD_RAIL);
                entries.add(ModBlocks.CONTACT_NETWORK);
                entries.add(ModBlocks.TRAIN_POWER_SETTING_CONTROLLER);
                entries.add(ModBlocks.DAOJI_MACHINE);
                entries.add(ModBlocks.TRACK_CONSTRUCTION_CONTROL_PANEL);
                entries.add(ModBlocks.CLEANER);
                
                // ========== 车站设施类 ==========
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
                entries.add(ModBlocks.ELEVATOR_TRACK);
                entries.add(ModBlocks.ELECTRONIC_DOOR_CONTROLLER);
                entries.add(ModBlocks.STATION_CONSTRUCTION_CONTROL_PANEL);
                
                // ========== 线路设施类 ==========
                // 线路控制面板使用 Item 版本（方块未注册 BlockItem）
                entries.add(ModItems.TRACK_CONTROL_PANEL);
                entries.add(ModBlocks.TRAIN_PANEL);
                entries.add(ModBlocks.SIGNAL_LAYOUT_CONTROLLER);
                entries.add(ModBlocks.SENSOR_CONTROLLER);
                entries.add(ModBlocks.SENSOR_LAYOUT_CONTROLLER);
                
                // ========== 车站建筑类 ==========
                entries.add(ModBlocks.BARRICADE);
                entries.add(ModBlocks.STAIRCASE_STEP);
                entries.add(ModBlocks.GLASS_WALL);
                entries.add(ModBlocks.TILE_WALL);
                entries.add(ModBlocks.CEILING);
                entries.add(ModBlocks.LIT_CEILING);
                entries.add(ModBlocks.POSTAGE);
                entries.add(ModBlocks.FIRE_EXTINGUISHER);
                entries.add(ModBlocks.FIRE_WATER);
                
                // ========== 工具物品 ==========
                entries.add(ModItems.PRESET);
                entries.add(ModItems.TRACK_BRUSH);
                entries.add(ModItems.STATION_BRUSH);
                entries.add(ModItems.SETTING_CONTROLLER);
                entries.add(ModItems.SHIELD_DOOR_KEY);
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

