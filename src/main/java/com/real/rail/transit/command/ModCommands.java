package com.real.rail.transit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.real.rail.transit.RealRailTransitMod;
import com.real.rail.transit.util.ModRuntimeLog;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * 模组自定义指令
 *
 * /rrtlog [limit]         查看最近 N 条模组运行日志（颜色区分等级）
 * /rrtlog clear           清空当前会话的运行日志
 * /rrtaddon               打开追加包创建网站（点击聊天中的链接）
     * /rrtstation list        列出当前维度的站点标记
     * /rrtstation info <id>   查看指定站点标记信息
 */
public class ModCommands {

    // 追加包创建网站 URL（可按需修改）
    private static final String ADDON_SITE_URL = "https://mtr.mrzhousf.com/addon"; // 示例地址

    public static void register() {
        CommandRegistrationCallback.EVENT.register(ModCommands::registerCommands);
        RealRailTransitMod.LOGGER.info("Real Rail Transit: 已注册自定义指令 /rrtlog 和 /rrtaddon");
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
                                         CommandRegistryAccess registryAccess,
                                         CommandManager.RegistrationEnvironment environment) {
        // /rrtlog [limit]
        dispatcher.register(
            CommandManager.literal("rrtlog")
                .requires(source -> source.hasPermissionLevel(0)) // 所有人可用
                .executes(ctx -> {
                    return executeShowLog(ctx.getSource(), 50);
                })
                .then(CommandManager.literal("clear")
                    .requires(source -> source.hasPermissionLevel(2)) // 仅 OP 可清空
                    .executes(ctx -> {
                        // 清空日志：通过重新创建日志容器的方式实现
                        // 这里简单发送提示，真正的清空通过重启世界实现，避免并发问题
                        ctx.getSource().sendFeedback(
                            () -> Text.literal("[RRT] 当前版本暂不支持完全清空日志，重启服务器即可清空。")
                                .formatted(Formatting.YELLOW),
                            false
                        );
                        return 1;
                    })
                )
                .then(CommandManager.argument("limit", IntegerArgumentType.integer(1, 200))
                    .executes(ctx -> {
                        int limit = IntegerArgumentType.getInteger(ctx, "limit");
                        return executeShowLog(ctx.getSource(), limit);
                    })
                )
        );

        // /rrtaddon
        dispatcher.register(
            CommandManager.literal("rrtaddon")
                .requires(source -> source.hasPermissionLevel(0))
                .executes(ctx -> {
                    ServerCommandSource source = ctx.getSource();
                    ServerPlayerEntity player = source.getPlayer();
                    if (player == null) {
                        source.sendFeedback(
                            () -> Text.literal("[RRT] 此指令只能由玩家在游戏内执行。")
                                .formatted(Formatting.RED),
                            false
                        );
                        return 0;
                    }

                    Text linkText = Text.literal("点击打开 Real Rail Transit 追加包创建网站")
                        .formatted(Formatting.AQUA, Formatting.UNDERLINE)
                        .styled(style -> style.withClickEvent(
                            new ClickEvent(ClickEvent.Action.OPEN_URL, ADDON_SITE_URL)
                        ));

                    player.sendMessage(
                        Text.literal("[RRT] ").formatted(Formatting.GREEN).append(linkText),
                        false
                    );
                    return 1;
                })
        );

        // /rrtstation 子命令
        dispatcher.register(
            CommandManager.literal("rrtstation")
                .requires(source -> source.hasPermissionLevel(0))
                .then(CommandManager.literal("list")
                    .executes(ctx -> executeListStations(ctx.getSource()))
                )
                .then(CommandManager.literal("info")
                    .then(CommandManager.argument("id", StringArgumentType.string())
                        .executes(ctx -> {
                            String id = StringArgumentType.getString(ctx, "id");
                            return executeStationInfo(ctx.getSource(), id);
                        })
                    )
                )
        );
    }

    private static int executeShowLog(ServerCommandSource source, int limit) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendFeedback(
                () -> Text.literal("[RRT] 此指令只能由玩家在游戏内执行。")
                    .formatted(Formatting.RED),
                false
            );
            return 0;
        }

        var world = player.getServerWorld();
        var entries = ModRuntimeLog.getRecentEntries(limit);
        if (entries.isEmpty()) {
            player.sendMessage(
                Text.literal("[RRT] 当前没有可显示的模组运行日志。")
                    .formatted(Formatting.GRAY),
                false
            );
            return 1;
        }

        player.sendMessage(
            Text.literal("[RRT] 显示最近 " + entries.size() + " 条模组运行日志：")
                .formatted(Formatting.GOLD),
            false
        );

        for (ModRuntimeLog.Entry entry : entries) {
            player.sendMessage(ModRuntimeLog.formatEntry(entry), false);
        }
        return entries.size();
    }

    private static int executeListStations(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendFeedback(
                () -> Text.literal("[RRT] 此指令只能由玩家在游戏内执行。")
                    .formatted(Formatting.RED),
                false
            );
            return 0;
        }

        var world = player.getServerWorld();
        java.util.List<com.real.rail.transit.station.entity.StationMarkerBlockEntity> markers = new java.util.ArrayList<>();

        net.minecraft.util.math.ChunkPos center = player.getChunkPos();
        net.minecraft.util.math.ChunkPos.stream(center, 64).forEach(chunkPos -> {
            if (world.isChunkLoaded(chunkPos.x, chunkPos.z)) {
                int startX = chunkPos.getStartX();
                int startZ = chunkPos.getStartZ();
                for (int y = world.getBottomY(); y < world.getTopY(); y++) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            var pos = new net.minecraft.util.math.BlockPos(startX + x, y, startZ + z);
                            var be = world.getBlockEntity(pos);
                            if (be instanceof com.real.rail.transit.station.entity.StationMarkerBlockEntity marker) {
                                markers.add(marker);
                            }
                        }
                    }
                }
            }
        });

        if (markers.isEmpty()) {
            player.sendMessage(
                Text.literal("[RRT] 当前维度没有找到站点标记。")
                    .formatted(Formatting.GRAY),
                false
            );
            return 0;
        }

        player.sendMessage(
            Text.literal("[RRT] 站点标记列表（" + markers.size() + " 个）：")
                .formatted(Formatting.GOLD),
            false
        );

        for (var marker : markers) {
            var pos = marker.getPos();
            String id = marker.getStationId();
            String name = marker.getStationName();
            player.sendMessage(
                Text.literal("- ID=" + id + ", 名称=" + (name.isEmpty() ? "<未命名>" : name) +
                    ", 位置=(" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")")
                    .formatted(Formatting.AQUA),
                false
            );
        }
        return markers.size();
    }

    private static int executeStationInfo(ServerCommandSource source, String stationIdOrName) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendFeedback(
                () -> Text.literal("[RRT] 此指令只能由玩家在游戏内执行。")
                    .formatted(Formatting.RED),
                false
            );
            return 0;
        }

        var world = player.getServerWorld();
        com.real.rail.transit.station.entity.StationMarkerBlockEntity[] foundRef = new com.real.rail.transit.station.entity.StationMarkerBlockEntity[1];

        net.minecraft.util.math.ChunkPos center = player.getChunkPos();
        net.minecraft.util.math.ChunkPos.stream(center, 64).forEach(chunkPos -> {
            if (foundRef[0] != null) return;
            if (world.isChunkLoaded(chunkPos.x, chunkPos.z)) {
                int startX = chunkPos.getStartX();
                int startZ = chunkPos.getStartZ();
                for (int y = world.getBottomY(); y < world.getTopY() && foundRef[0] == null; y++) {
                    for (int x = 0; x < 16 && foundRef[0] == null; x++) {
                        for (int z = 0; z < 16; z++) {
                            var pos = new net.minecraft.util.math.BlockPos(startX + x, y, startZ + z);
                            var be = world.getBlockEntity(pos);
                            if (be instanceof com.real.rail.transit.station.entity.StationMarkerBlockEntity marker) {
                                String id = marker.getStationId();
                                String name = marker.getStationName();
                                if (stationIdOrName.equalsIgnoreCase(id) || stationIdOrName.equalsIgnoreCase(name)) {
                                    foundRef[0] = marker;
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        });
        com.real.rail.transit.station.entity.StationMarkerBlockEntity found = foundRef[0];

        if (found == null) {
            player.sendMessage(
                Text.literal("[RRT] 未找到站点标记: " + stationIdOrName)
                    .formatted(Formatting.RED),
                false
            );
            return 0;
        }

        var pos = found.getPos();
        String id = found.getStationId();
        String name = found.getStationName();
        player.sendMessage(
            Text.literal("[RRT] 站点标记信息：")
                .formatted(Formatting.GOLD),
            false
        );
        player.sendMessage(
            Text.literal("ID=" + id + ", 名称=" + (name.isEmpty() ? "<未命名>" : name) +
                ", 位置=(" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")")
                .formatted(Formatting.AQUA),
            false
        );
        return 1;
    }
}


