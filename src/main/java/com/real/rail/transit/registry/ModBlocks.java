package com.real.rail.transit.registry;

import com.real.rail.transit.RealRailTransitMod;
import com.real.rail.transit.block.CableBlock;
import com.real.rail.transit.block.CleanerBlock;
import com.real.rail.transit.block.ConnectionPartBlock;
import com.real.rail.transit.block.ContactNetworkBlock;
import com.real.rail.transit.block.DaojiMachineBlock;
import com.real.rail.transit.block.GuardTrackBlock;
import com.real.rail.transit.block.SensorControllerBlock;
import com.real.rail.transit.block.SensorLayoutControllerBlock;
import com.real.rail.transit.block.SignalBlock;
import com.real.rail.transit.block.SignalLayoutControllerBlock;
import com.real.rail.transit.block.SwitchBlock;
import com.real.rail.transit.block.ThirdRailBlock;
import com.real.rail.transit.block.TrackBlock;
import com.real.rail.transit.block.ControlPanelBlock;
import com.real.rail.transit.block.DriverKeyBlock;
import com.real.rail.transit.block.TrackConstructionControlPanelBlock;
import com.real.rail.transit.block.TrackControlPanelBlock;
import com.real.rail.transit.block.TrainPanelBlock;
import com.real.rail.transit.block.TrainPowerSettingControllerBlock;
import com.real.rail.transit.block.TurnoutBlock;
import com.real.rail.transit.station.ArrivalDisplayScreenBlock;
import com.real.rail.transit.station.AutomaticStaircaseBlock;
import com.real.rail.transit.station.BarricadeBlock;
import com.real.rail.transit.station.CeilingBlock;
import com.real.rail.transit.station.DisplayScreenBlock;
import com.real.rail.transit.station.ElectronicDoorControllerBlock;
import com.real.rail.transit.station.ElevatorBlock;
import com.real.rail.transit.station.ElevatorTrackBlock;
import com.real.rail.transit.station.FireExtinguisherBlock;
import com.real.rail.transit.station.FireWaterBlock;
import com.real.rail.transit.station.GateBlock;
import com.real.rail.transit.station.GlassWallBlock;
import com.real.rail.transit.station.LitCeilingBlock;
import com.real.rail.transit.station.PostageBlock;
import com.real.rail.transit.station.ShieldDoorBlock;
import com.real.rail.transit.station.StationMarkerBlock;
import com.real.rail.transit.station.SmallTVBlock;
import com.real.rail.transit.station.StaircaseStepBlock;
import com.real.rail.transit.station.StationConstructionControlPanelBlock;
import com.real.rail.transit.station.StationRadioBlock;
import com.real.rail.transit.station.TicketMachineBlock;
import com.real.rail.transit.station.TileWallBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * 模组方块注册类
 */
public class ModBlocks {
    // ========== 线路建设类 ==========
    // 轨道方块
    public static final Block TRACK = registerBlock("track",
        new TrackBlock(FabricBlockSettings.create().strength(3.5f)),
        new Item.Settings());
    
    // 信号机方块
    public static final Block SIGNAL = registerBlock("signal",
        new SignalBlock(FabricBlockSettings.create().strength(2.5f).nonOpaque()),
        new Item.Settings());
    
    // 转辙器（道岔）方块
    public static final Block TURNOUT = registerBlock("turnout",
        new TurnoutBlock(FabricBlockSettings.create().strength(3.0f)),
        new Item.Settings());
    
    // 第三轨供电方块
    public static final Block THIRD_RAIL = registerBlock("third_rail",
        new ThirdRailBlock(FabricBlockSettings.create().strength(3.0f)),
        new Item.Settings());
    
    // 接触网供电方块
    public static final Block CONTACT_NETWORK = registerBlock("contact_network",
        new ContactNetworkBlock(FabricBlockSettings.create().strength(2.5f).nonOpaque()),
        new Item.Settings());
    
    // 连接部分
    public static final Block CONNECTION_PART = registerBlock("connection_part",
        new ConnectionPartBlock(FabricBlockSettings.create().strength(3.0f)),
        new Item.Settings());
    
    // 辙叉
    public static final Block SWITCH = registerBlock("switch",
        new SwitchBlock(FabricBlockSettings.create().strength(3.0f)),
        new Item.Settings());
    
    // 护轨
    public static final Block GUARD_TRACK = registerBlock("guard_track",
        new GuardTrackBlock(FabricBlockSettings.create().strength(3.0f)),
        new Item.Settings());
    
    // 线缆
    public static final Block CABLE = registerBlock("cable",
        new CableBlock(FabricBlockSettings.create().strength(1.5f)),
        new Item.Settings());
    
    // 列车供电设置器
    public static final Block TRAIN_POWER_SETTING_CONTROLLER = registerBlock("train_power_setting_controller",
        new TrainPowerSettingControllerBlock(FabricBlockSettings.create().strength(2.5f)),
        new Item.Settings());
    
    // 盾构机
    public static final Block DAOJI_MACHINE = registerBlock("daoji_machine",
        new DaojiMachineBlock(FabricBlockSettings.create().strength(5.0f)),
        new Item.Settings());
    
    // 线路建设控制面板（不注册为方块，只作为物品使用）
    // 注意：此方块不注册BlockItem，只注册Block（保留类定义），实际使用Item版本
    public static final Block TRACK_CONSTRUCTION_CONTROL_PANEL = Registry.register(Registries.BLOCK,
        Identifier.of(RealRailTransitMod.MOD_ID, "track_construction_control_panel"),
        new TrackConstructionControlPanelBlock(FabricBlockSettings.create().strength(2.0f).nonOpaque()));
    
    // 清除器
    public static final Block CLEANER = registerBlock("cleaner",
        new CleanerBlock(FabricBlockSettings.create().strength(1.0f)),
        new Item.Settings());
    
    // ========== 车站设施类 ==========
    // 高架屏蔽门
    public static final Block UPPER_SHIELD_DOOR = registerBlock("upper_shield_door",
        new ShieldDoorBlock(FabricBlockSettings.create().strength(2.0f).nonOpaque()),
        new Item.Settings());
    
    // 半高屏蔽门
    public static final Block LOWER_SHIELD_DOOR = registerBlock("lower_shield_door",
        new ShieldDoorBlock(FabricBlockSettings.create().strength(2.0f).nonOpaque()),
        new Item.Settings());
    
    // 电梯
    public static final Block ELEVATOR = registerBlock("elevator",
        new ElevatorBlock(FabricBlockSettings.create().strength(3.0f)),
        new Item.Settings());
    
    // 自动扶梯
    public static final Block AUTOMATIC_STAIRCASE = registerBlock("automatic_staircase",
        new AutomaticStaircaseBlock(FabricBlockSettings.create().strength(2.5f)),
        new Item.Settings());
    
    // 显示屏
    public static final Block DISPLAY_SCREEN = registerBlock("display_screen",
        new DisplayScreenBlock(FabricBlockSettings.create().strength(1.5f).nonOpaque()),
        new Item.Settings());
    
    // 到站显示屏
    public static final Block ARRIVAL_DISPLAY_SCREEN = registerBlock("arrival_display_screen",
        new ArrivalDisplayScreenBlock(FabricBlockSettings.create().strength(1.5f).nonOpaque()),
        new Item.Settings());
    
    // 售票机
    public static final Block TICKET_MACHINE = registerBlock("ticket_machine",
        new TicketMachineBlock(FabricBlockSettings.create().strength(2.0f)),
        new Item.Settings());
    
    // 闸机
    public static final Block GATE = registerBlock("gate",
        new GateBlock(FabricBlockSettings.create().strength(2.0f)),
        new Item.Settings());
    
    // 小电视
    public static final Block SMALL_TV = registerBlock("small_tv",
        new SmallTVBlock(FabricBlockSettings.create().strength(1.5f).nonOpaque()),
        new Item.Settings());
    
    // 车站广播器
    public static final Block STATION_RADIO = registerBlock("station_radio",
        new StationRadioBlock(FabricBlockSettings.create().strength(2.0f)),
        new Item.Settings());
    
    // 站点标记
    public static final Block STATION_MARKER = registerBlock("station_marker",
        new StationMarkerBlock(FabricBlockSettings.create().strength(1.0f).nonOpaque()),
        new Item.Settings());
    
    // 电梯轨道
    public static final Block ELEVATOR_TRACK = registerBlock("elevator_track",
        new ElevatorTrackBlock(FabricBlockSettings.create().strength(3.0f)),
        new Item.Settings());
    
    // 电子门控制器
    public static final Block ELECTRONIC_DOOR_CONTROLLER = registerBlock("electronic_door_controller",
        new ElectronicDoorControllerBlock(FabricBlockSettings.create().strength(2.0f).nonOpaque()),
        new Item.Settings());
    
    // 车站建设控制面板（不注册为方块，只作为物品使用）
    // 注意：此方块不注册BlockItem，只注册Block（保留类定义），实际使用Item版本
    public static final Block STATION_CONSTRUCTION_CONTROL_PANEL = Registry.register(Registries.BLOCK,
        Identifier.of(RealRailTransitMod.MOD_ID, "station_construction_control_panel"),
        new StationConstructionControlPanelBlock(FabricBlockSettings.create().strength(2.0f).nonOpaque()));
    
    // ========== 线路设施类 ==========
    // 线路控制面板（不注册为方块，只作为物品使用）
    // 注意：此方块不注册BlockItem，只注册Block（保留类定义），实际使用Item版本
    public static final Block TRACK_CONTROL_PANEL = Registry.register(Registries.BLOCK,
        Identifier.of(RealRailTransitMod.MOD_ID, "track_control_panel"),
        new TrackControlPanelBlock(FabricBlockSettings.create().strength(2.0f).nonOpaque()));

    // 列车面板（不注册为方块，只作为物品使用）
    // 注意：此方块不注册BlockItem，只注册Block（保留类定义），实际使用Item版本
    public static final Block TRAIN_PANEL = Registry.register(Registries.BLOCK,
        Identifier.of(RealRailTransitMod.MOD_ID, "train_panel"),
        new TrainPanelBlock(FabricBlockSettings.create().strength(2.0f).nonOpaque()));
    
    // 控制面板（不注册为方块，只作为物品使用）
    // 注意：此方块不注册BlockItem，只注册Block（保留类定义），实际使用Item版本
    public static final Block CONTROL_PANEL = Registry.register(Registries.BLOCK,
        Identifier.of(RealRailTransitMod.MOD_ID, "control_panel"),
        new ControlPanelBlock(FabricBlockSettings.create().strength(2.0f).nonOpaque()));
    
    // 司机钥匙（不注册为方块，只作为物品使用，不可放置）
    // 注意：此方块不注册BlockItem，只注册Block（保留类定义），实际使用Item版本
    public static final Block DRIVER_KEY = Registry.register(Registries.BLOCK,
        Identifier.of(RealRailTransitMod.MOD_ID, "driver_key"),
        new DriverKeyBlock(FabricBlockSettings.create().strength(2.0f).nonOpaque()));
    
    // 信号铺设器
    public static final Block SIGNAL_LAYOUT_CONTROLLER = registerBlock("signal_layout_controller",
        new SignalLayoutControllerBlock(FabricBlockSettings.create().strength(2.0f)),
        new Item.Settings());
    
    // 传感器
    public static final Block SENSOR_CONTROLLER = registerBlock("sensor_controller",
        new SensorControllerBlock(FabricBlockSettings.create().strength(2.0f)),
        new Item.Settings());
    
    // 传感器铺设器
    public static final Block SENSOR_LAYOUT_CONTROLLER = registerBlock("sensor_layout_controller",
        new SensorLayoutControllerBlock(FabricBlockSettings.create().strength(2.0f)),
        new Item.Settings());
    
    // ========== 车站建筑类 ==========
    // 栏杆
    public static final Block BARRICADE = registerBlock("barricade",
        new BarricadeBlock(FabricBlockSettings.create().strength(2.0f).nonOpaque()),
        new Item.Settings());
    
    // 楼梯台阶
    public static final Block STAIRCASE_STEP = registerBlock("staircase_step",
        new StaircaseStepBlock(FabricBlockSettings.create().strength(2.5f)),
        new Item.Settings());
    
    // 玻璃墙
    public static final Block GLASS_WALL = registerBlock("glass_wall",
        new GlassWallBlock(FabricBlockSettings.create().strength(1.5f).nonOpaque()),
        new Item.Settings());
    
    // 瓷砖墙
    public static final Block TILE_WALL = registerBlock("tile_wall",
        new TileWallBlock(FabricBlockSettings.create().strength(2.0f)),
        new Item.Settings());
    
    // 天花板
    public static final Block CEILING = registerBlock("ceiling",
        new CeilingBlock(FabricBlockSettings.create().strength(2.0f)),
        new Item.Settings());
    
    // 带灯天花板
    public static final Block LIT_CEILING = registerBlock("lit_ceiling",
        new LitCeilingBlock(FabricBlockSettings.create().strength(2.0f)),
        new Item.Settings());
    
    // 广告牌
    public static final Block POSTAGE = registerBlock("postage",
        new PostageBlock(FabricBlockSettings.create().strength(1.5f).nonOpaque()),
        new Item.Settings());
    
    // 灭火器
    public static final Block FIRE_EXTINGUISHER = registerBlock("fire_extinguisher",
        new FireExtinguisherBlock(FabricBlockSettings.create().strength(2.0f)),
        new Item.Settings());
    
    // 消防水
    public static final Block FIRE_WATER = registerBlock("fire_water",
        new FireWaterBlock(FabricBlockSettings.create().strength(2.0f)),
        new Item.Settings());
    
    /**
     * 注册方块和对应的物品
     */
    private static Block registerBlock(String name, Block block, Item.Settings itemSettings) {
        Block registeredBlock = Registry.register(Registries.BLOCK,
            Identifier.of(RealRailTransitMod.MOD_ID, name), block);
        Registry.register(Registries.ITEM, Identifier.of(RealRailTransitMod.MOD_ID, name),
            new BlockItem(registeredBlock, itemSettings));
        return registeredBlock;
    }
    
    /**
     * 初始化注册（在模组初始化时调用）
     */
    public static void registerModBlocks() {
        RealRailTransitMod.LOGGER.info("正在注册 Real Rail Transit 模组方块...");
        
        // 注册方块实体
        com.real.rail.transit.block.entity.SignalBlockEntity.register();
    }
}

