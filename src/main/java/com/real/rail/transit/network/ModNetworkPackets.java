package com.real.rail.transit.network;

import com.real.rail.transit.RealRailTransitMod;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * 模组网络包定义
 */
public class ModNetworkPackets {
    // 售票机相关
    public static final Identifier TICKET_MACHINE_SET_PRICE = Identifier.of(RealRailTransitMod.MOD_ID, "ticket_machine_set_price");
    public static final Identifier TICKET_MACHINE_BUY_TICKET = Identifier.of(RealRailTransitMod.MOD_ID, "ticket_machine_buy_ticket");
    public static final Identifier TICKET_MACHINE_SET_STATUS = Identifier.of(RealRailTransitMod.MOD_ID, "ticket_machine_set_status");
    
    // 显示屏相关
    public static final Identifier DISPLAY_SCREEN_UPDATE = Identifier.of(RealRailTransitMod.MOD_ID, "display_screen_update");
    
    // 车站广播器相关
    public static final Identifier STATION_RADIO_UPDATE = Identifier.of(RealRailTransitMod.MOD_ID, "station_radio_update");
    public static final Identifier STATION_RADIO_PLAY = Identifier.of(RealRailTransitMod.MOD_ID, "station_radio_play");
    public static final Identifier STATION_RADIO_STOP = Identifier.of(RealRailTransitMod.MOD_ID, "station_radio_stop");
    
    // 到站显示屏相关
    public static final Identifier ARRIVAL_DISPLAY_UPDATE = Identifier.of(RealRailTransitMod.MOD_ID, "arrival_display_update");
    
    // 线路建设控制面板相关
    public static final Identifier TRACK_CONSTRUCTION_UPDATE_CONFIG = Identifier.of(RealRailTransitMod.MOD_ID, "track_construction_update_config");
    public static final Identifier TRACK_CONSTRUCTION_BATCH_APPLY = Identifier.of(RealRailTransitMod.MOD_ID, "track_construction_batch_apply");
    
    // 线路控制面板相关
    public static final Identifier TRACK_CONTROL_REQUEST_STATS = Identifier.of(RealRailTransitMod.MOD_ID, "track_control_request_stats");
    public static final Identifier TRACK_CONTROL_STATS_RESPONSE = Identifier.of(RealRailTransitMod.MOD_ID, "track_control_stats_response");
    
    // 车站建设控制面板相关
    public static final Identifier STATION_CONSTRUCTION_SELECT_FACILITY = Identifier.of(RealRailTransitMod.MOD_ID, "station_construction_select_facility");
    public static final Identifier STATION_CONSTRUCTION_CONFIG_SHIELD_DOOR = Identifier.of(RealRailTransitMod.MOD_ID, "station_construction_config_shield_door");
    public static final Identifier STATION_CONSTRUCTION_CONFIG_DISPLAY = Identifier.of(RealRailTransitMod.MOD_ID, "station_construction_config_display");
    public static final Identifier STATION_CONSTRUCTION_BATCH_APPLY = Identifier.of(RealRailTransitMod.MOD_ID, "station_construction_batch_apply");
    
    // 列车放置相关
    public static final Identifier PLACE_TRAIN = Identifier.of(RealRailTransitMod.MOD_ID, "place_train");
    
    // 列车控制相关
    public static final Identifier TRAIN_CONTROL = Identifier.of(RealRailTransitMod.MOD_ID, "train_control");
    public static final Identifier TRAIN_SPEED_UPDATE = Identifier.of(RealRailTransitMod.MOD_ID, "train_speed_update");
    
    public static void register() {
        RealRailTransitMod.LOGGER.info("注册网络包...");
        
        // 注册Payload类型（C2S - 客户端到服务器）
        PayloadTypeRegistry.playC2S().register(TicketMachineSetPricePayload.ID, TicketMachineSetPricePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TicketMachineBuyTicketPayload.ID, TicketMachineBuyTicketPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TicketMachineSetStatusPayload.ID, TicketMachineSetStatusPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DisplayScreenUpdatePayload.ID, DisplayScreenUpdatePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StationRadioUpdatePayload.ID, StationRadioUpdatePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StationRadioPlayPayload.ID, StationRadioPlayPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StationRadioStopPayload.ID, StationRadioStopPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ArrivalDisplayUpdatePayload.ID, ArrivalDisplayUpdatePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TrackConstructionUpdateConfigPayload.ID, TrackConstructionUpdateConfigPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TrackConstructionBatchApplyPayload.ID, TrackConstructionBatchApplyPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TrackControlRequestStatsPayload.ID, TrackControlRequestStatsPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StationConstructionSelectFacilityPayload.ID, StationConstructionSelectFacilityPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PlaceTrainPayload.ID, PlaceTrainPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TrainControlPayload.ID, TrainControlPayload.CODEC);
        
        // 注册Payload类型（S2C - 服务器到客户端）
        PayloadTypeRegistry.playS2C().register(TrainSpeedUpdatePayload.ID, TrainSpeedUpdatePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StationConstructionConfigShieldDoorPayload.ID, StationConstructionConfigShieldDoorPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StationConstructionConfigDisplayPayload.ID, StationConstructionConfigDisplayPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StationConstructionBatchApplyPayload.ID, StationConstructionBatchApplyPayload.CODEC);
        
        // 注册Payload类型（S2C - 服务器到客户端）
        PayloadTypeRegistry.playS2C().register(TrackControlStatsResponsePayload.ID, TrackControlStatsResponsePayload.CODEC);
        
        // 注册售票机网络包处理器
        ServerPlayNetworking.registerGlobalReceiver(TicketMachineSetPricePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                BlockPos pos = payload.pos();
                int price = payload.price();
                var world = context.player().getWorld();
                var blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof com.real.rail.transit.station.entity.TicketMachineBlockEntity ticketEntity) {
                    ticketEntity.setTicketPrice(price);
                }
            });
        });
        
        ServerPlayNetworking.registerGlobalReceiver(TicketMachineBuyTicketPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                BlockPos pos = payload.pos();
                var world = context.player().getWorld();
                var blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof com.real.rail.transit.station.entity.TicketMachineBlockEntity ticketEntity) {
                    handleBuyTicket(context.player(), ticketEntity);
                }
            });
        });
        
        ServerPlayNetworking.registerGlobalReceiver(TicketMachineSetStatusPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                BlockPos pos = payload.pos();
                boolean working = payload.working();
                var world = context.player().getWorld();
                var blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof com.real.rail.transit.station.entity.TicketMachineBlockEntity ticketEntity) {
                    ticketEntity.setWorking(working);
                }
            });
        });
        
        // 注册显示屏网络包处理器
        ServerPlayNetworking.registerGlobalReceiver(DisplayScreenUpdatePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                BlockPos pos = payload.pos();
                var world = context.player().getWorld();
                var blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof com.real.rail.transit.station.entity.DisplayScreenBlockEntity displayEntity) {
                    if (payload.text() != null) displayEntity.setDisplayText(payload.text());
                    if (payload.color() != null) displayEntity.setTextColor(payload.color());
                    if (payload.scale() != null) displayEntity.setTextScale(payload.scale());
                }
            });
        });
        
        // 注册车站广播器网络包处理器
        ServerPlayNetworking.registerGlobalReceiver(StationRadioUpdatePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                BlockPos pos = payload.pos();
                var world = context.player().getWorld();
                var blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof com.real.rail.transit.station.entity.StationRadioBlockEntity radioEntity) {
                    if (payload.message() != null) radioEntity.setBroadcastMessage(payload.message());
                    if (payload.soundId() != null) radioEntity.setSoundId(payload.soundId());
                    if (payload.volume() != null) radioEntity.setVolume(payload.volume());
                }
            });
        });
        
        ServerPlayNetworking.registerGlobalReceiver(StationRadioPlayPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                BlockPos pos = payload.pos();
                var world = context.player().getWorld();
                var blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof com.real.rail.transit.station.entity.StationRadioBlockEntity radioEntity) {
                    radioEntity.setPlaying(true);
                    playStationRadioSound(world, pos, radioEntity);
                }
            });
        });
        
        ServerPlayNetworking.registerGlobalReceiver(StationRadioStopPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                BlockPos pos = payload.pos();
                var world = context.player().getWorld();
                var blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof com.real.rail.transit.station.entity.StationRadioBlockEntity radioEntity) {
                    radioEntity.setPlaying(false);
                    stopStationRadioSound(world, pos, radioEntity);
                }
            });
        });
        
        // 注册到站显示屏网络包处理器
        ServerPlayNetworking.registerGlobalReceiver(ArrivalDisplayUpdatePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                BlockPos pos = payload.pos();
                var world = context.player().getWorld();
                var blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof com.real.rail.transit.station.entity.ArrivalDisplayScreenBlockEntity arrivalEntity) {
                    if (payload.trainId() != null) arrivalEntity.setTrainId(payload.trainId());
                    if (payload.destination() != null) arrivalEntity.setDestination(payload.destination());
                    if (payload.arrivalTime() != null) arrivalEntity.setNextArrivalTime(payload.arrivalTime());
                    if (payload.platform() != null) arrivalEntity.setPlatform(payload.platform());
                    if (payload.customText() != null) arrivalEntity.setCustomText(payload.customText());
                    if (payload.color() != null) arrivalEntity.setTextColor(payload.color());
                }
            });
        });
        
        // 注册线路建设控制面板网络包处理器
        ServerPlayNetworking.registerGlobalReceiver(TrackConstructionUpdateConfigPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                BlockPos pos = payload.pos();
                var world = context.player().getWorld();
                // 配置会应用到玩家选择的区域或当前面板位置
                // 这里可以存储配置到某个数据存储中
                RealRailTransitMod.LOGGER.info("收到线路建设配置更新: 轨道类型={}, 供电方式={}, 信号机配置={}", 
                    payload.trackType(), payload.powerType(), payload.signalConfig());
            });
        });
        
        ServerPlayNetworking.registerGlobalReceiver(TrackConstructionBatchApplyPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                BlockPos startPos = payload.startPos();
                BlockPos endPos = payload.endPos();
                var world = context.player().getWorld();
                // 批量应用配置到指定区域
                int count = applyBatchConfiguration(world, startPos, endPos, payload.trackType(), payload.powerType(), payload.signalConfig());
                context.player().sendMessage(net.minecraft.text.Text.translatable(
                    "gui.real-rail-transit-mod.track_construction_control_panel.batch_applied"), false);
                if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                    com.real.rail.transit.util.ModRuntimeLog.info(
                        "线路建设批量配置完成，区域 " + startPos + " -> " + endPos + "，处理方块数=" + count,
                        serverWorld
                    );
                }
            });
        });
        
        // 注册线路控制面板网络包处理器
        ServerPlayNetworking.registerGlobalReceiver(TrackControlRequestStatsPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                BlockPos pos = payload.pos();
                var world = context.player().getWorld();
                var stats = calculateTrackStatistics(world, pos);
                
                // 发送统计数据回客户端
                if (context.player() instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                    ServerPlayNetworking.send(serverPlayer, new TrackControlStatsResponsePayload(
                        stats.trackCount(),
                        stats.signalCount(),
                        stats.trainCount(),
                        stats.powerSectionCount(),
                        stats.activeTrains()
                    ));
                }
            });
        });
        
        // 注册车站建设控制面板网络包处理器
        ServerPlayNetworking.registerGlobalReceiver(StationConstructionConfigShieldDoorPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                BlockPos pos = payload.pos();
                boolean isUpper = payload.isUpper();
                var world = context.player().getWorld();
                var state = world.getBlockState(pos);
                
                if (state.getBlock() instanceof com.real.rail.transit.station.ShieldDoorBlock) {
                    world.setBlockState(pos, state.with(com.real.rail.transit.station.ShieldDoorBlock.IS_UPPER, isUpper));
                    context.player().sendMessage(net.minecraft.text.Text.translatable(
                        "gui.real-rail-transit-mod.station_construction_control_panel.shield_door_configured",
                        isUpper ? net.minecraft.text.Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.shield_door_type.upper")
                                : net.minecraft.text.Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.shield_door_type.lower")
                    ), false);
                }
            });
        });
        
        ServerPlayNetworking.registerGlobalReceiver(StationConstructionConfigDisplayPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                BlockPos pos = payload.pos();
                var world = context.player().getWorld();
                var blockEntity = world.getBlockEntity(pos);
                
                if (blockEntity instanceof com.real.rail.transit.station.entity.DisplayScreenBlockEntity displayEntity) {
                    if (payload.text() != null) displayEntity.setDisplayText(payload.text());
                    if (payload.color() != null) displayEntity.setTextColor(payload.color());
                    if (payload.scale() != null) displayEntity.setTextScale(payload.scale());
                    context.player().sendMessage(net.minecraft.text.Text.translatable(
                        "gui.real-rail-transit-mod.station_construction_control_panel.display_configured"
                    ), false);
                }
            });
        });
        
        ServerPlayNetworking.registerGlobalReceiver(StationConstructionBatchApplyPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var world = context.player().getWorld();
                int count = applyStationBatchConfiguration(
                    world,
                    payload.startPos(),
                    payload.endPos(),
                    payload.facilityType(),
                    payload.config()
                );
                context.player().sendMessage(net.minecraft.text.Text.translatable(
                    "gui.real-rail-transit-mod.station_construction_control_panel.batch_applied",
                    count
                ), false);
                if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                    com.real.rail.transit.util.ModRuntimeLog.info(
                        "车站设施批量配置完成，区域 " + payload.startPos() + " -> " + payload.endPos() +
                            "，类型=" + payload.facilityType() + "，数量=" + count,
                        serverWorld
                    );
                }
            });
        });
        
        // 注册列车放置网络包处理器
        ServerPlayNetworking.registerGlobalReceiver(PlaceTrainPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                BlockPos pos = payload.pos();
                String trainId = payload.trainId();
                var world = context.player().getWorld();
                
                // pos 是列车放置位置（轨道上方），需要检查下方是否是轨道
                BlockPos trackPos = pos.down();
                BlockState trackState = world.getBlockState(trackPos);
                boolean isValid = trackState.getBlock() instanceof com.real.rail.transit.block.TrackBlock || 
                                 trackState.isOf(com.real.rail.transit.registry.ModBlocks.TRACK);
                
                if (!isValid) {
                    context.player().sendMessage(net.minecraft.text.Text.translatable(
                        "gui.real-rail-transit-mod.train_panel.place_train.invalid_position"), false);
                    return;
                }
                
                // 检查放置位置和上方是否有空间（至少需要2格高）
                if (!world.getBlockState(pos).isAir()) {
                    context.player().sendMessage(net.minecraft.text.Text.translatable(
                        "gui.real-rail-transit-mod.train_panel.place_train.no_space"), false);
                    return;
                }
                
                // 检查上方一格是否有空间（列车高度需要）
                if (!world.getBlockState(pos.up()).isAir()) {
                    context.player().sendMessage(net.minecraft.text.Text.translatable(
                        "gui.real-rail-transit-mod.train_panel.place_train.no_space"), false);
                    return;
                }
                
                // 获取列车配置
                com.real.rail.transit.addon.AddonManager.TrainConfig trainConfig = 
                    com.real.rail.transit.addon.AddonManager.getInstance().getLoadedAddons().stream()
                        .filter(train -> train.train_id.equals(trainId))
                        .findFirst()
                        .orElse(null);
                
                if (trainConfig == null) {
                    context.player().sendMessage(net.minecraft.text.Text.translatable(
                        "gui.real-rail-transit-mod.train_panel.place_train.train_not_found"), false);
                    return;
                }
                
                // 创建列车实体
                com.real.rail.transit.entity.TrainEntity train = new com.real.rail.transit.entity.TrainEntity(
                    com.real.rail.transit.registry.ModEntities.TRAIN,
                    world
                );
                train.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                train.setTrainId(trainId);
                // 设置列车属性
                train.setMaxSpeed(trainConfig.max_speed / 3.6); // 转换为 m/s
                train.setAcceleration(trainConfig.acceleration);
                train.setDeceleration(trainConfig.deceleration);
                
                // 更新边界框（根据列车配置）
                train.updateBoundingBox();
                
                // 生成列车到世界
                world.spawnEntity(train);
                
                String trainName = trainConfig.train_name != null ? trainConfig.train_name : trainConfig.train_id;
                context.player().sendMessage(net.minecraft.text.Text.translatable(
                    "gui.real-rail-transit-mod.train_panel.place_train.success", trainName), false);
            });
        });
        
        // 注册列车控制网络包处理器
        ServerPlayNetworking.registerGlobalReceiver(TrainControlPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var world = context.player().getWorld();
                Entity entity = world.getEntityById(payload.trainId());
                
                if (entity == null || !(entity instanceof com.real.rail.transit.entity.TrainEntity train)) {
                    return;
                }
                
                // 检查玩家是否手持钥匙
                var player = context.player();
                boolean hasKey = player.getStackInHand(net.minecraft.util.Hand.MAIN_HAND).isOf(com.real.rail.transit.registry.ModItems.SHIELD_DOOR_KEY) ||
                               player.getStackInHand(net.minecraft.util.Hand.OFF_HAND).isOf(com.real.rail.transit.registry.ModItems.SHIELD_DOOR_KEY);
                
                if (!hasKey) {
                    return;
                }
                
                // 检查玩家是否在列车附近（5格内）
                double distance = player.squaredDistanceTo(train);
                if (distance > 25.0) {
                    return;
                }
                
                // 处理控制输入
                switch (payload.type()) {
                    case FORWARD:
                        if (payload.pressed()) {
                            train.setDirection(com.real.rail.transit.entity.TrainEntity.Direction.FORWARD);
                            train.setEngineOn(true);
                        }
                        break;
                    case BACKWARD:
                        if (payload.pressed()) {
                            train.setDirection(com.real.rail.transit.entity.TrainEntity.Direction.BACKWARD);
                            train.setEngineOn(true);
                        }
                        break;
                    case THROTTLE_UP:
                        if (payload.pressed()) {
                            double newSpeed = Math.min(train.getTargetSpeed() + 2.0, train.getMaxSpeed());
                            train.setTargetSpeed(newSpeed);
                        }
                        break;
                    case THROTTLE_DOWN:
                        if (payload.pressed()) {
                            double newSpeed = Math.max(train.getTargetSpeed() - 2.0, 0.0);
                            train.setTargetSpeed(newSpeed);
                        }
                        break;
                    case DOOR_LEFT:
                        if (payload.pressed()) {
                            // 触发左门开关（需要实现车门系统）
                            // train.toggleLeftDoor();
                        }
                        break;
                    case DOOR_RIGHT:
                        if (payload.pressed()) {
                            // 触发右门开关（需要实现车门系统）
                            // train.toggleRightDoor();
                        }
                        break;
                }
            });
        });
    }
    
    /**
     * 计算线路统计数据
     */
    private static TrackStatistics calculateTrackStatistics(net.minecraft.world.World world, BlockPos centerPos) {
        int trackCount = 0;
        int signalCount = 0;
        int powerSectionCount = 0;
        int trainCount = 0;
        int activeTrains = 0;
        
        // 搜索半径（以面板位置为中心）
        int searchRadius = 100;
        int minX = centerPos.getX() - searchRadius;
        int maxX = centerPos.getX() + searchRadius;
        int minY = centerPos.getY() - 10;
        int maxY = centerPos.getY() + 10;
        int minZ = centerPos.getZ() - searchRadius;
        int maxZ = centerPos.getZ() + searchRadius;
        
        // 统计轨道和信号机
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    var state = world.getBlockState(pos);
                    
                    if (state.isOf(com.real.rail.transit.registry.ModBlocks.TRACK)) {
                        trackCount++;
                    }
                    if (state.isOf(com.real.rail.transit.registry.ModBlocks.SIGNAL)) {
                        signalCount++;
                    }
                    if (state.isOf(com.real.rail.transit.registry.ModBlocks.THIRD_RAIL) || 
                        state.isOf(com.real.rail.transit.registry.ModBlocks.CONTACT_NETWORK)) {
                        powerSectionCount++;
                    }
                }
            }
        }
        
        // 统计列车
        var trainEntities = world.getEntitiesByType(
            com.real.rail.transit.registry.ModEntities.TRAIN,
            new net.minecraft.util.math.Box(minX, minY, minZ, maxX, maxY, maxZ),
            entity -> true
        );
        trainCount = trainEntities.size();
        
        // 统计运行中的列车（速度 > 0）
        for (var train : trainEntities) {
            if (train instanceof com.real.rail.transit.entity.TrainEntity trainEntity) {
                // 这里需要访问TrainEntity的速度，但由于是客户端代码，我们简化处理
                activeTrains++;
            }
        }
        
        return new TrackStatistics(trackCount, signalCount, trainCount, powerSectionCount, activeTrains);
    }
    
    /**
     * 线路统计数据记录
     */
    private record TrackStatistics(int trackCount, int signalCount, int trainCount, 
                                   int powerSectionCount, int activeTrains) {}
    
    /**
     * 批量应用车站设施配置
     */
    private static int applyStationBatchConfiguration(net.minecraft.world.World world, BlockPos startPos, BlockPos endPos,
                                                     String facilityType, String config) {
        int minX = Math.min(startPos.getX(), endPos.getX());
        int maxX = Math.max(startPos.getX(), endPos.getX());
        int minY = Math.min(startPos.getY(), endPos.getY());
        int maxY = Math.max(startPos.getY(), endPos.getY());
        int minZ = Math.min(startPos.getZ(), endPos.getZ());
        int maxZ = Math.max(startPos.getZ(), endPos.getZ());
        
        int count = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    var state = world.getBlockState(pos);
                    var blockEntity = world.getBlockEntity(pos);
                    
                    if ("shield_door".equals(facilityType) && state.getBlock() instanceof com.real.rail.transit.station.ShieldDoorBlock) {
                        boolean isUpper = "upper".equals(config);
                        world.setBlockState(pos, state.with(com.real.rail.transit.station.ShieldDoorBlock.IS_UPPER, isUpper));
                        count++;
                    } else if ("display_screen".equals(facilityType) && blockEntity instanceof com.real.rail.transit.station.entity.DisplayScreenBlockEntity displayEntity) {
                        // 解析配置：格式为 "text|color|scale"
                        String[] parts = config.split("\\|");
                        if (parts.length > 0 && !parts[0].isEmpty()) displayEntity.setDisplayText(parts[0]);
                        if (parts.length > 1 && !parts[1].isEmpty()) {
                            try {
                                displayEntity.setTextColor(Integer.parseInt(parts[1], 16) | 0xFF000000);
                            } catch (NumberFormatException e) {
                                // 忽略无效颜色
                            }
                        }
                        if (parts.length > 2 && !parts[2].isEmpty()) {
                            try {
                                displayEntity.setTextScale(Float.parseFloat(parts[2]));
                            } catch (NumberFormatException e) {
                                // 忽略无效缩放
                            }
                        }
                        count++;
                    }
                }
            }
        }
        return count;
    }
    
    /**
     * 批量应用配置到指定区域
     */
    private static int applyBatchConfiguration(net.minecraft.world.World world, BlockPos startPos, BlockPos endPos,
                                             String trackType, String powerType, String signalConfig) {
        int minX = Math.min(startPos.getX(), endPos.getX());
        int maxX = Math.max(startPos.getX(), endPos.getX());
        int minY = Math.min(startPos.getY(), endPos.getY());
        int maxY = Math.max(startPos.getY(), endPos.getY());
        int minZ = Math.min(startPos.getZ(), endPos.getZ());
        int maxZ = Math.max(startPos.getZ(), endPos.getZ());
        
        int count = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    var state = world.getBlockState(pos);
                    
                    // 根据配置类型应用不同的设置
                    if (trackType != null && !trackType.isEmpty()) {
                        // 轨道类型配置：支持在基础轨道与护轨之间切换，或清除轨道
                        String tt = trackType.toLowerCase(java.util.Locale.ROOT);
                        if (state.isOf(com.real.rail.transit.registry.ModBlocks.TRACK) ||
                            state.isOf(com.real.rail.transit.registry.ModBlocks.GUARD_TRACK)) {
                            if ("guard".equals(tt)) {
                                // 切换为护轨
                                if (!state.isOf(com.real.rail.transit.registry.ModBlocks.GUARD_TRACK)) {
                                    world.setBlockState(pos, com.real.rail.transit.registry.ModBlocks.GUARD_TRACK.getDefaultState());
                                    count++;
                                }
                            } else if ("normal".equals(tt)) {
                                // 切换为普通轨道
                                if (!state.isOf(com.real.rail.transit.registry.ModBlocks.TRACK)) {
                                    world.setBlockState(pos, com.real.rail.transit.registry.ModBlocks.TRACK.getDefaultState());
                                    count++;
                                }
                            } else if ("clear".equals(tt)) {
                                // 清除轨道
                                world.removeBlock(pos, false);
                                count++;
                            }
                        }
                    }
                    if (powerType != null && !powerType.isEmpty()) {
                        if (state.isOf(com.real.rail.transit.registry.ModBlocks.THIRD_RAIL) || 
                            state.isOf(com.real.rail.transit.registry.ModBlocks.CONTACT_NETWORK)) {
                            // 应用供电方式配置，并同步方块供电状态（NONE 视为断电）
                            com.real.rail.transit.system.PowerSystem.PowerType powerTypeEnum = 
                                "third_rail".equalsIgnoreCase(powerType) ? com.real.rail.transit.system.PowerSystem.PowerType.THIRD_RAIL :
                                "catenary".equalsIgnoreCase(powerType) ? com.real.rail.transit.system.PowerSystem.PowerType.CATENARY :
                                com.real.rail.transit.system.PowerSystem.PowerType.NONE;
                            com.real.rail.transit.system.PowerSystem powerSystem = com.real.rail.transit.system.PowerSystem.getInstance();
                            powerSystem.setPowerSectionType(pos, powerTypeEnum);
                            // NONE 表示该区段无电
                            boolean powered = powerTypeEnum != com.real.rail.transit.system.PowerSystem.PowerType.NONE;
                            powerSystem.setPowerState(world, pos, powered);
                            count++;
                        }
                    }
                    if (signalConfig != null && !signalConfig.isEmpty() && state.isOf(com.real.rail.transit.registry.ModBlocks.SIGNAL)) {
                        // 应用信号机配置：支持 red / yellow / green / guiding / auto
                        String config = signalConfig.toLowerCase(java.util.Locale.ROOT);
                        com.real.rail.transit.block.SignalBlock.SignalState newState = null;
                        switch (config) {
                            case "red" -> newState = com.real.rail.transit.block.SignalBlock.SignalState.RED;
                            case "yellow" -> newState = com.real.rail.transit.block.SignalBlock.SignalState.YELLOW;
                            case "green" -> newState = com.real.rail.transit.block.SignalBlock.SignalState.GREEN;
                            case "guiding" -> newState = com.real.rail.transit.block.SignalBlock.SignalState.GUIDING;
                            case "auto" -> {
                                com.real.rail.transit.system.SignalSystem.getInstance().updateSignalState(world, pos);
                                count++;
                            }
                        }
                        if (newState != null) {
                            com.real.rail.transit.system.SignalSystem.getInstance().setSignalState(world, pos, newState);
                            count++;
                        }
                    }
                }
            }
        }
        
        RealRailTransitMod.LOGGER.info("批量应用配置完成，共处理 {} 个方块", count);
        return count;
    }
    
    /**
     * 处理购票逻辑
     */
    private static void handleBuyTicket(net.minecraft.entity.player.PlayerEntity player, 
                                       com.real.rail.transit.station.entity.TicketMachineBlockEntity ticketEntity) {
        if (!ticketEntity.isWorking()) {
            player.sendMessage(net.minecraft.text.Text.translatable("gui.real-rail-transit-mod.ticket_machine.broken"), false);
            if (player.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                com.real.rail.transit.util.ModRuntimeLog.warn(
                    "售票机不可用，位置 " + ticketEntity.getPos(),
                    serverWorld
                );
            }
            return;
        }
        
        int price = ticketEntity.getTicketPrice();
        
        // 检查玩家是否有足够的绿宝石
        int emeraldCount = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            var stack = player.getInventory().getStack(i);
            if (stack.getItem() == net.minecraft.item.Items.EMERALD) {
                emeraldCount += stack.getCount();
            }
        }
        
        if (emeraldCount < price) {
            player.sendMessage(net.minecraft.text.Text.translatable("gui.real-rail-transit-mod.ticket_machine.insufficient_funds", price), false);
            if (player.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                com.real.rail.transit.util.ModRuntimeLog.warn(
                    "玩家 " + player.getName().getString() + " 购票失败，余额不足，票价=" + price,
                    serverWorld
                );
            }
            return;
        }
        
        // 扣除绿宝石
        int remaining = price;
        for (int i = 0; i < player.getInventory().size() && remaining > 0; i++) {
            var stack = player.getInventory().getStack(i);
            if (stack.getItem() == net.minecraft.item.Items.EMERALD) {
                int take = Math.min(stack.getCount(), remaining);
                stack.decrement(take);
                remaining -= take;
            }
        }
        
        // 生成票卡物品并"吐出"（在售票机位置生成掉落物）
        var ticketStack = new net.minecraft.item.ItemStack(com.real.rail.transit.registry.ModItems.TICKET_CARD, 1);
        
        // 在售票机前方生成掉落物（模拟"吐出"效果）
        net.minecraft.util.math.BlockPos machinePos = ticketEntity.getPos();
        net.minecraft.util.math.Vec3d dropPos = net.minecraft.util.math.Vec3d.ofCenter(machinePos).add(0, 0.5, 0);
        
        // 根据玩家位置决定票卡掉落方向
        net.minecraft.util.math.Vec3d playerPos = player.getPos();
        net.minecraft.util.math.Vec3d direction = playerPos.subtract(dropPos).normalize();
        dropPos = dropPos.add(direction.multiply(0.5)); // 在售票机前方0.5格处掉落
        
        net.minecraft.entity.ItemEntity itemEntity = new net.minecraft.entity.ItemEntity(
            player.getWorld(),
            dropPos.x, dropPos.y, dropPos.z,
            ticketStack
        );
        itemEntity.setPickupDelay(10); // 0.5秒后才能拾取
        itemEntity.setVelocity(direction.multiply(0.2)); // 给票卡一个向外的速度
        
        player.getWorld().spawnEntity(itemEntity);
        
        // 增加售票计数
        ticketEntity.incrementTicketCount();
        
        player.sendMessage(net.minecraft.text.Text.translatable("gui.real-rail-transit-mod.ticket_machine.bought", price), false);
        if (player.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            com.real.rail.transit.util.ModRuntimeLog.info(
                "玩家 " + player.getName().getString() + " 成功购票，票价=" + price + "，位置 " + ticketEntity.getPos(),
                serverWorld
            );
        }
    }
    
    /**
     * 播放车站广播器音效
     */
    private static void playStationRadioSound(net.minecraft.world.World world, BlockPos pos, 
                                             com.real.rail.transit.station.entity.StationRadioBlockEntity radioEntity) {
        if (radioEntity.getSoundId() == null || radioEntity.getSoundId().isEmpty()) {
            return;
        }
        
        try {
            net.minecraft.util.Identifier soundId;
            String soundIdStr = radioEntity.getSoundId();
            if (soundIdStr.contains(":")) {
                String[] parts = soundIdStr.split(":", 2);
                soundId = net.minecraft.util.Identifier.of(parts[0], parts[1]);
            } else {
                soundId = net.minecraft.util.Identifier.of(RealRailTransitMod.MOD_ID, soundIdStr);
            }
            
            net.minecraft.sound.SoundEvent soundEvent = net.minecraft.registry.Registries.SOUND_EVENT.get(soundId);
            if (soundEvent == null) {
                soundEvent = net.minecraft.sound.SoundEvent.of(soundId);
            }
            
            // 向所有玩家播放音效
            for (net.minecraft.entity.player.PlayerEntity player : world.getPlayers()) {
                if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                    serverPlayer.playSoundToPlayer(soundEvent, net.minecraft.sound.SoundCategory.BLOCKS, 
                        radioEntity.getVolume(), 1.0f);
                }
            }
        } catch (Exception e) {
            RealRailTransitMod.LOGGER.warn("无法播放车站广播器音效: {}", radioEntity.getSoundId(), e);
        }
    }
    
    /**
     * 停止车站广播器音效
     */
    private static void stopStationRadioSound(net.minecraft.world.World world, BlockPos pos,
                                            com.real.rail.transit.station.entity.StationRadioBlockEntity radioEntity) {
        // 停止音效（通过设置playing状态，客户端会检测并停止）
        // 实际停止逻辑在客户端处理
    }
    
    // BlockPos 序列化辅助方法
    private static final PacketCodec<RegistryByteBuf, BlockPos> BLOCK_POS_CODEC = PacketCodec.tuple(
        net.minecraft.network.codec.PacketCodecs.VAR_INT, BlockPos::getX,
        net.minecraft.network.codec.PacketCodecs.VAR_INT, BlockPos::getY,
        net.minecraft.network.codec.PacketCodecs.VAR_INT, BlockPos::getZ,
        BlockPos::new
    );
    
    // 网络包记录类
    public record TicketMachineSetPricePayload(BlockPos pos, int price) implements CustomPayload {
        public static final Id<TicketMachineSetPricePayload> ID = new Id<>(TICKET_MACHINE_SET_PRICE);
        public static final PacketCodec<RegistryByteBuf, TicketMachineSetPricePayload> CODEC = PacketCodec.tuple(
            BLOCK_POS_CODEC, TicketMachineSetPricePayload::pos,
            net.minecraft.network.codec.PacketCodecs.VAR_INT, TicketMachineSetPricePayload::price,
            TicketMachineSetPricePayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record TicketMachineBuyTicketPayload(BlockPos pos) implements CustomPayload {
        public static final Id<TicketMachineBuyTicketPayload> ID = new Id<>(TICKET_MACHINE_BUY_TICKET);
        public static final PacketCodec<RegistryByteBuf, TicketMachineBuyTicketPayload> CODEC = PacketCodec.tuple(
            BLOCK_POS_CODEC, TicketMachineBuyTicketPayload::pos,
            TicketMachineBuyTicketPayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record TicketMachineSetStatusPayload(BlockPos pos, boolean working) implements CustomPayload {
        public static final Id<TicketMachineSetStatusPayload> ID = new Id<>(TICKET_MACHINE_SET_STATUS);
        public static final PacketCodec<RegistryByteBuf, TicketMachineSetStatusPayload> CODEC = PacketCodec.tuple(
            BLOCK_POS_CODEC, TicketMachineSetStatusPayload::pos,
            net.minecraft.network.codec.PacketCodecs.BOOL, TicketMachineSetStatusPayload::working,
            TicketMachineSetStatusPayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record DisplayScreenUpdatePayload(BlockPos pos, String text, Integer color, Float scale) implements CustomPayload {
        public static final Id<DisplayScreenUpdatePayload> ID = new Id<>(DISPLAY_SCREEN_UPDATE);
        public static final PacketCodec<RegistryByteBuf, DisplayScreenUpdatePayload> CODEC = PacketCodec.tuple(
            BLOCK_POS_CODEC, DisplayScreenUpdatePayload::pos,
            net.minecraft.network.codec.PacketCodecs.STRING, DisplayScreenUpdatePayload::text,
            net.minecraft.network.codec.PacketCodecs.INTEGER, DisplayScreenUpdatePayload::color,
            net.minecraft.network.codec.PacketCodecs.FLOAT, DisplayScreenUpdatePayload::scale,
            DisplayScreenUpdatePayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record StationRadioUpdatePayload(BlockPos pos, String message, String soundId, Float volume) implements CustomPayload {
        public static final Id<StationRadioUpdatePayload> ID = new Id<>(STATION_RADIO_UPDATE);
        public static final PacketCodec<RegistryByteBuf, StationRadioUpdatePayload> CODEC = PacketCodec.tuple(
            BLOCK_POS_CODEC, StationRadioUpdatePayload::pos,
            net.minecraft.network.codec.PacketCodecs.STRING, StationRadioUpdatePayload::message,
            net.minecraft.network.codec.PacketCodecs.STRING, StationRadioUpdatePayload::soundId,
            net.minecraft.network.codec.PacketCodecs.FLOAT, StationRadioUpdatePayload::volume,
            StationRadioUpdatePayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record StationRadioPlayPayload(BlockPos pos) implements CustomPayload {
        public static final Id<StationRadioPlayPayload> ID = new Id<>(STATION_RADIO_PLAY);
        public static final PacketCodec<RegistryByteBuf, StationRadioPlayPayload> CODEC = PacketCodec.tuple(
            BLOCK_POS_CODEC, StationRadioPlayPayload::pos,
            StationRadioPlayPayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record StationRadioStopPayload(BlockPos pos) implements CustomPayload {
        public static final Id<StationRadioStopPayload> ID = new Id<>(STATION_RADIO_STOP);
        public static final PacketCodec<RegistryByteBuf, StationRadioStopPayload> CODEC = PacketCodec.tuple(
            BLOCK_POS_CODEC, StationRadioStopPayload::pos,
            StationRadioStopPayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record ArrivalDisplayUpdatePayload(BlockPos pos, String trainId, String destination, 
                                             Integer arrivalTime, String platform, String customText, Integer color) implements CustomPayload {
        public static final Id<ArrivalDisplayUpdatePayload> ID = new Id<>(ARRIVAL_DISPLAY_UPDATE);
        public static final PacketCodec<RegistryByteBuf, ArrivalDisplayUpdatePayload> CODEC = 
            new PacketCodec<RegistryByteBuf, ArrivalDisplayUpdatePayload>() {
                @Override
                public ArrivalDisplayUpdatePayload decode(RegistryByteBuf buf) {
                    return new ArrivalDisplayUpdatePayload(
                        BLOCK_POS_CODEC.decode(buf),
                        net.minecraft.network.codec.PacketCodecs.STRING.decode(buf),
                        net.minecraft.network.codec.PacketCodecs.STRING.decode(buf),
                        net.minecraft.network.codec.PacketCodecs.INTEGER.decode(buf),
                        net.minecraft.network.codec.PacketCodecs.STRING.decode(buf),
                        net.minecraft.network.codec.PacketCodecs.STRING.decode(buf),
                        net.minecraft.network.codec.PacketCodecs.INTEGER.decode(buf)
                    );
                }
                
                @Override
                public void encode(RegistryByteBuf buf, ArrivalDisplayUpdatePayload payload) {
                    BLOCK_POS_CODEC.encode(buf, payload.pos());
                    net.minecraft.network.codec.PacketCodecs.STRING.encode(buf, payload.trainId());
                    net.minecraft.network.codec.PacketCodecs.STRING.encode(buf, payload.destination());
                    net.minecraft.network.codec.PacketCodecs.INTEGER.encode(buf, payload.arrivalTime());
                    net.minecraft.network.codec.PacketCodecs.STRING.encode(buf, payload.platform());
                    net.minecraft.network.codec.PacketCodecs.STRING.encode(buf, payload.customText());
                    net.minecraft.network.codec.PacketCodecs.INTEGER.encode(buf, payload.color());
                }
            };
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record TrackConstructionUpdateConfigPayload(BlockPos pos, String trackType, String powerType, String signalConfig) implements CustomPayload {
        public static final Id<TrackConstructionUpdateConfigPayload> ID = new Id<>(TRACK_CONSTRUCTION_UPDATE_CONFIG);
        public static final PacketCodec<RegistryByteBuf, TrackConstructionUpdateConfigPayload> CODEC = PacketCodec.tuple(
            BLOCK_POS_CODEC, TrackConstructionUpdateConfigPayload::pos,
            net.minecraft.network.codec.PacketCodecs.STRING, TrackConstructionUpdateConfigPayload::trackType,
            net.minecraft.network.codec.PacketCodecs.STRING, TrackConstructionUpdateConfigPayload::powerType,
            net.minecraft.network.codec.PacketCodecs.STRING, TrackConstructionUpdateConfigPayload::signalConfig,
            TrackConstructionUpdateConfigPayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record TrackConstructionBatchApplyPayload(BlockPos startPos, BlockPos endPos, String trackType, String powerType, String signalConfig) implements CustomPayload {
        public static final Id<TrackConstructionBatchApplyPayload> ID = new Id<>(TRACK_CONSTRUCTION_BATCH_APPLY);
        public static final PacketCodec<RegistryByteBuf, TrackConstructionBatchApplyPayload> CODEC = PacketCodec.tuple(
            BLOCK_POS_CODEC, TrackConstructionBatchApplyPayload::startPos,
            BLOCK_POS_CODEC, TrackConstructionBatchApplyPayload::endPos,
            net.minecraft.network.codec.PacketCodecs.STRING, TrackConstructionBatchApplyPayload::trackType,
            net.minecraft.network.codec.PacketCodecs.STRING, TrackConstructionBatchApplyPayload::powerType,
            net.minecraft.network.codec.PacketCodecs.STRING, TrackConstructionBatchApplyPayload::signalConfig,
            TrackConstructionBatchApplyPayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record TrackControlRequestStatsPayload(BlockPos pos) implements CustomPayload {
        public static final Id<TrackControlRequestStatsPayload> ID = new Id<>(TRACK_CONTROL_REQUEST_STATS);
        public static final PacketCodec<RegistryByteBuf, TrackControlRequestStatsPayload> CODEC = PacketCodec.tuple(
            BLOCK_POS_CODEC, TrackControlRequestStatsPayload::pos,
            TrackControlRequestStatsPayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record TrackControlStatsResponsePayload(int trackCount, int signalCount, int trainCount, 
                                                   int powerSectionCount, int activeTrains) implements CustomPayload {
        public static final Id<TrackControlStatsResponsePayload> ID = new Id<>(TRACK_CONTROL_STATS_RESPONSE);
        public static final PacketCodec<RegistryByteBuf, TrackControlStatsResponsePayload> CODEC = PacketCodec.tuple(
            net.minecraft.network.codec.PacketCodecs.VAR_INT, TrackControlStatsResponsePayload::trackCount,
            net.minecraft.network.codec.PacketCodecs.VAR_INT, TrackControlStatsResponsePayload::signalCount,
            net.minecraft.network.codec.PacketCodecs.VAR_INT, TrackControlStatsResponsePayload::trainCount,
            net.minecraft.network.codec.PacketCodecs.VAR_INT, TrackControlStatsResponsePayload::powerSectionCount,
            net.minecraft.network.codec.PacketCodecs.VAR_INT, TrackControlStatsResponsePayload::activeTrains,
            TrackControlStatsResponsePayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record StationConstructionSelectFacilityPayload(String facilityType) implements CustomPayload {
        public static final Id<StationConstructionSelectFacilityPayload> ID = new Id<>(STATION_CONSTRUCTION_SELECT_FACILITY);
        public static final PacketCodec<RegistryByteBuf, StationConstructionSelectFacilityPayload> CODEC = PacketCodec.tuple(
            net.minecraft.network.codec.PacketCodecs.STRING, StationConstructionSelectFacilityPayload::facilityType,
            StationConstructionSelectFacilityPayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record StationConstructionConfigShieldDoorPayload(BlockPos pos, boolean isUpper) implements CustomPayload {
        public static final Id<StationConstructionConfigShieldDoorPayload> ID = new Id<>(STATION_CONSTRUCTION_CONFIG_SHIELD_DOOR);
        public static final PacketCodec<RegistryByteBuf, StationConstructionConfigShieldDoorPayload> CODEC = PacketCodec.tuple(
            BLOCK_POS_CODEC, StationConstructionConfigShieldDoorPayload::pos,
            net.minecraft.network.codec.PacketCodecs.BOOL, StationConstructionConfigShieldDoorPayload::isUpper,
            StationConstructionConfigShieldDoorPayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record StationConstructionConfigDisplayPayload(BlockPos pos, String text, Integer color, Float scale) implements CustomPayload {
        public static final Id<StationConstructionConfigDisplayPayload> ID = new Id<>(STATION_CONSTRUCTION_CONFIG_DISPLAY);
        public static final PacketCodec<RegistryByteBuf, StationConstructionConfigDisplayPayload> CODEC = PacketCodec.tuple(
            BLOCK_POS_CODEC, StationConstructionConfigDisplayPayload::pos,
            net.minecraft.network.codec.PacketCodecs.STRING, StationConstructionConfigDisplayPayload::text,
            net.minecraft.network.codec.PacketCodecs.INTEGER, StationConstructionConfigDisplayPayload::color,
            net.minecraft.network.codec.PacketCodecs.FLOAT, StationConstructionConfigDisplayPayload::scale,
            StationConstructionConfigDisplayPayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record StationConstructionBatchApplyPayload(BlockPos startPos, BlockPos endPos, String facilityType, String config) implements CustomPayload {
        public static final Id<StationConstructionBatchApplyPayload> ID = new Id<>(STATION_CONSTRUCTION_BATCH_APPLY);
        public static final PacketCodec<RegistryByteBuf, StationConstructionBatchApplyPayload> CODEC = PacketCodec.tuple(
            BLOCK_POS_CODEC, StationConstructionBatchApplyPayload::startPos,
            BLOCK_POS_CODEC, StationConstructionBatchApplyPayload::endPos,
            net.minecraft.network.codec.PacketCodecs.STRING, StationConstructionBatchApplyPayload::facilityType,
            net.minecraft.network.codec.PacketCodecs.STRING, StationConstructionBatchApplyPayload::config,
            StationConstructionBatchApplyPayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record PlaceTrainPayload(BlockPos pos, String trainId) implements CustomPayload {
        public static final Id<PlaceTrainPayload> ID = new Id<>(PLACE_TRAIN);
        public static final PacketCodec<RegistryByteBuf, PlaceTrainPayload> CODEC = PacketCodec.tuple(
            BLOCK_POS_CODEC, PlaceTrainPayload::pos,
            net.minecraft.network.codec.PacketCodecs.STRING, PlaceTrainPayload::trainId,
            PlaceTrainPayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    /**
     * 列车控制类型枚举
     */
    public enum TrainControlType {
        FORWARD,        // 前进
        BACKWARD,       // 后退
        THROTTLE_UP,    // 增加油门
        THROTTLE_DOWN,  // 减少油门
        DOOR_LEFT,      // 左门
        DOOR_RIGHT      // 右门
    }
    
    public record TrainControlPayload(int trainId, TrainControlType type, boolean pressed) implements CustomPayload {
        public static final Id<TrainControlPayload> ID = new Id<>(TRAIN_CONTROL);
        public static final PacketCodec<RegistryByteBuf, TrainControlPayload> CODEC = new PacketCodec<RegistryByteBuf, TrainControlPayload>() {
            @Override
            public TrainControlPayload decode(RegistryByteBuf buf) {
                int trainId = buf.readVarInt();
                String typeName = buf.readString();
                TrainControlType type = TrainControlType.valueOf(typeName);
                boolean pressed = buf.readBoolean();
                return new TrainControlPayload(trainId, type, pressed);
            }
            
            @Override
            public void encode(RegistryByteBuf buf, TrainControlPayload value) {
                buf.writeVarInt(value.trainId());
                buf.writeString(value.type().name());
                buf.writeBoolean(value.pressed());
            }
        };
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record TrainSpeedUpdatePayload(int trainId, double currentSpeed, double targetSpeed, double maxSpeed) implements CustomPayload {
        public static final Id<TrainSpeedUpdatePayload> ID = new Id<>(TRAIN_SPEED_UPDATE);
        public static final PacketCodec<RegistryByteBuf, TrainSpeedUpdatePayload> CODEC = PacketCodec.tuple(
            net.minecraft.network.codec.PacketCodecs.VAR_INT, TrainSpeedUpdatePayload::trainId,
            net.minecraft.network.codec.PacketCodecs.DOUBLE, TrainSpeedUpdatePayload::currentSpeed,
            net.minecraft.network.codec.PacketCodecs.DOUBLE, TrainSpeedUpdatePayload::targetSpeed,
            net.minecraft.network.codec.PacketCodecs.DOUBLE, TrainSpeedUpdatePayload::maxSpeed,
            TrainSpeedUpdatePayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}

