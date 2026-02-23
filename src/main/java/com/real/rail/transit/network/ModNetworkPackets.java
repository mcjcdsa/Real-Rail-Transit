package com.real.rail.transit.network;

import com.real.rail.transit.RealRailTransitMod;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
    }
    
    /**
     * 处理购票逻辑
     */
    private static void handleBuyTicket(net.minecraft.entity.player.PlayerEntity player, 
                                       com.real.rail.transit.station.entity.TicketMachineBlockEntity ticketEntity) {
        if (!ticketEntity.isWorking()) {
            player.sendMessage(net.minecraft.text.Text.translatable("gui.real-rail-transit-mod.ticket_machine.broken"), false);
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
        
        // 给予车票（使用纸作为车票）
        var ticketStack = new net.minecraft.item.ItemStack(net.minecraft.item.Items.PAPER, 1);
        ticketStack.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME, 
            net.minecraft.text.Text.translatable("item.real-rail-transit-mod.ticket"));
        player.getInventory().offerOrDrop(ticketStack);
        
        // 增加售票计数
        ticketEntity.incrementTicketCount();
        
        player.sendMessage(net.minecraft.text.Text.translatable("gui.real-rail-transit-mod.ticket_machine.bought", price), false);
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
}

