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
 * /rrtlang                显示当前语言和可用语言列表
 * /rrtlang <code>         切换模组语言（客户端指令）
 * /rrtstation list        列出当前维度的站点标记
 * /rrtstation info <id>   查看指定站点标记信息
 * /rrtdaoji direction <north|south|east|west|up|down> 设置盾构机方向
 * /rrtdaoji setstart <x> <y> <z> 设置盾构机起点坐标
 * /rrtdaoji setend <x> <y> <z> 设置盾构机终点坐标
 * /rrtdaoji path <straight|curve> 设置路径类型（直线或曲线）
 * /rrtdaoji start 启动盾构机
 * /rrtdaoji stop 停止盾构机
 */
public class ModCommands {

    // 追加包创建网站 URL（可按需修改）
    private static final String ADDON_SITE_URL = "https://mtr.mrzhousf.com/addon"; // 示例地址

    public static void register() {
        CommandRegistrationCallback.EVENT.register(ModCommands::registerCommands);
        RealRailTransitMod.LOGGER.info("Real Rail Transit: 已注册自定义指令 /rrtlog, /rrtaddon, /rrtlang");
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

        // /rrtlang 语言切换指令
        dispatcher.register(
            CommandManager.literal("rrtlang")
                .requires(source -> source.hasPermissionLevel(0))
                .executes(ctx -> executeShowLanguage(ctx.getSource()))
                .then(CommandManager.argument("code", StringArgumentType.string())
                    .suggests((context, builder) -> {
                        // 提供语言代码建议
                        builder.suggest("zh_cn");
                        builder.suggest("en_us");
                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        String langCode = StringArgumentType.getString(ctx, "code");
                        return executeSetLanguage(ctx.getSource(), langCode);
                    })
                )
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
        
        // /rrtdaoji 子命令
        dispatcher.register(
            CommandManager.literal("rrtdaoji")
                .requires(source -> source.hasPermissionLevel(2)) // 需要OP权限
                .then(CommandManager.literal("direction")
                    .then(CommandManager.argument("direction", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            builder.suggest("north");
                            builder.suggest("south");
                            builder.suggest("east");
                            builder.suggest("west");
                            builder.suggest("up");
                            builder.suggest("down");
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String dirStr = StringArgumentType.getString(ctx, "direction");
                            return executeSetDaojiDirection(ctx.getSource(), dirStr);
                        })
                    )
                )
                .then(CommandManager.literal("setstart")
                    .then(CommandManager.argument("x", com.mojang.brigadier.arguments.IntegerArgumentType.integer())
                        .then(CommandManager.argument("y", com.mojang.brigadier.arguments.IntegerArgumentType.integer())
                            .then(CommandManager.argument("z", com.mojang.brigadier.arguments.IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    int x = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "x");
                                    int y = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "y");
                                    int z = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "z");
                                    return executeSetDaojiStart(ctx.getSource(), x, y, z);
                                })
                            )
                        )
                    )
                )
                .then(CommandManager.literal("setend")
                    .then(CommandManager.argument("x", com.mojang.brigadier.arguments.IntegerArgumentType.integer())
                        .then(CommandManager.argument("y", com.mojang.brigadier.arguments.IntegerArgumentType.integer())
                            .then(CommandManager.argument("z", com.mojang.brigadier.arguments.IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    int x = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "x");
                                    int y = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "y");
                                    int z = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "z");
                                    return executeSetDaojiEnd(ctx.getSource(), x, y, z);
                                })
                            )
                        )
                    )
                )
                .then(CommandManager.literal("path")
                    .then(CommandManager.argument("type", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            builder.suggest("straight");
                            builder.suggest("curve");
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String type = StringArgumentType.getString(ctx, "type");
                            return executeSetDaojiPathType(ctx.getSource(), type);
                        })
                    )
                )
                .then(CommandManager.literal("start")
                    .executes(ctx -> {
                        return executeStartDaoji(ctx.getSource());
                    })
                )
                .then(CommandManager.literal("stop")
                    .executes(ctx -> {
                        return executeStopDaoji(ctx.getSource());
                    })
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

    /**
     * 显示当前语言和可用语言列表
     */
    private static int executeShowLanguage(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendFeedback(
                () -> Text.literal("[RRT] 此指令只能由玩家在游戏内执行。")
                    .formatted(Formatting.RED),
                false
            );
            return 0;
        }

        // 获取当前语言代码
        String currentLang = getCurrentLanguageCode();
        String currentLangName = getLanguageName(currentLang);

        player.sendMessage(
            Text.literal("[RRT] 模组语言设置")
                .formatted(Formatting.GOLD),
            false
        );

        player.sendMessage(
            Text.literal("当前语言: " + currentLangName + " (" + currentLang + ")")
                .formatted(Formatting.GREEN),
            false
        );

        player.sendMessage(
            Text.literal("可用语言:").formatted(Formatting.YELLOW),
            false
        );

        // 列出所有可用语言
        String[] availableLangs = {"zh_cn", "en_us"};
        String[] langNames = {"简体中文", "English"};
        
        for (int i = 0; i < availableLangs.length; i++) {
            String langCode = availableLangs[i];
            String langName = langNames[i];
            boolean isCurrent = langCode.equals(currentLang);
            
            Text langText;
            if (!isCurrent) {
                // 添加点击切换功能
                langText = Text.literal("  - " + langName + " (" + langCode + ")")
                    .formatted(Formatting.WHITE)
                    .styled(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rrtlang " + langCode))
                        .withHoverEvent(new net.minecraft.text.HoverEvent(
                            net.minecraft.text.HoverEvent.Action.SHOW_TEXT,
                            Text.literal("点击切换到 " + langName)
                        ))
                    );
            } else {
                langText = Text.literal("  - " + langName + " (" + langCode + ")")
                    .formatted(Formatting.GREEN);
            }
            
            player.sendMessage(langText, false);
        }

        player.sendMessage(
            Text.literal("提示: 使用 /rrtlang <语言代码> 切换语言")
                .formatted(Formatting.GRAY),
            false
        );

        return 1;
    }

    /**
     * 设置语言
     */
    private static int executeSetLanguage(ServerCommandSource source, String langCode) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendFeedback(
                () -> Text.literal("[RRT] 此指令只能由玩家在游戏内执行。")
                    .formatted(Formatting.RED),
                false
            );
            return 0;
        }

        // 验证语言代码
        String[] availableLangs = {"zh_cn", "en_us"};
        boolean isValid = false;
        for (String lang : availableLangs) {
            if (lang.equals(langCode)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            player.sendMessage(
                Text.literal("[RRT] 无效的语言代码: " + langCode)
                    .formatted(Formatting.RED),
                false
            );
            player.sendMessage(
                Text.literal("可用语言代码: zh_cn, en_us")
                    .formatted(Formatting.GRAY),
                false
            );
            return 0;
        }

        // 保存语言设置
        // 注意：由于语言切换是客户端功能，这里我们提示用户
        // 实际的语言切换需要通过客户端配置或Minecraft设置来完成
        String langName = getLanguageName(langCode);
        
        player.sendMessage(
            Text.literal("[RRT] 语言切换")
                .formatted(Formatting.GOLD),
            false
        );
        player.sendMessage(
            Text.literal("已选择语言: " + langName + " (" + langCode + ")")
                .formatted(Formatting.GREEN),
            false
        );
        player.sendMessage(
            Text.literal("提示: 模组语言跟随 Minecraft 客户端语言设置。")
                .formatted(Formatting.YELLOW),
            false
        );
        player.sendMessage(
            Text.literal("请在游戏设置（选项 -> 语言）中切换语言，或重启客户端。")
                .formatted(Formatting.GRAY),
            false
        );
        player.sendMessage(
            Text.literal("当前模组支持的语言: 简体中文 (zh_cn), English (en_us)")
                .formatted(Formatting.GRAY),
            false
        );

        // 记录到运行日志
        var world = player.getServerWorld();
        ModRuntimeLog.info("玩家 " + player.getName().getString() + " 请求切换语言为: " + langName + " (" + langCode + ")", world);

        return 1;
    }

    /**
     * 获取当前语言代码
     */
    private static String getCurrentLanguageCode() {
        // 在服务器端无法直接获取客户端语言配置
        // 这里返回默认值，实际语言由客户端配置决定
        // 客户端可以通过 ModLanguageConfig 获取实际语言
        return "zh_cn"; // 默认返回中文
    }

    /**
     * 获取语言名称
     */
    private static String getLanguageName(String langCode) {
        return switch (langCode) {
            case "zh_cn" -> "简体中文";
            case "en_us" -> "English";
            default -> langCode;
        };
    }
    
    /**
     * 设置盾构机方向
     */
    private static int executeSetDaojiDirection(ServerCommandSource source, String directionStr) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendFeedback(
                () -> Text.literal("[RRT] 此指令只能由玩家在游戏内执行。")
                    .formatted(Formatting.RED),
                false
            );
            return 0;
        }
        
        net.minecraft.util.math.Direction direction;
        try {
            direction = net.minecraft.util.math.Direction.byName(directionStr.toLowerCase());
        } catch (Exception e) {
            direction = null;
        }
        
        if (direction == null) {
            player.sendMessage(
                Text.literal("[RRT] 无效的方向: " + directionStr + "。可用方向: north, south, east, west, up, down")
                    .formatted(Formatting.RED),
                false
            );
            return 0;
        }
        
        // 查找玩家附近的盾构机
        var world = player.getServerWorld();
        net.minecraft.util.math.BlockPos playerPos = player.getBlockPos();
        com.real.rail.transit.block.entity.DaojiMachineBlockEntity found = null;
        net.minecraft.util.math.BlockPos foundPos = null;
        
        // 在玩家周围5格内查找盾构机
        for (int x = -5; x <= 5 && found == null; x++) {
            for (int y = -5; y <= 5 && found == null; y++) {
                for (int z = -5; z <= 5 && found == null; z++) {
                    net.minecraft.util.math.BlockPos checkPos = playerPos.add(x, y, z);
                    var blockEntity = world.getBlockEntity(checkPos);
                    if (blockEntity instanceof com.real.rail.transit.block.entity.DaojiMachineBlockEntity entity) {
                        found = entity;
                        foundPos = checkPos;
                    }
                }
            }
        }
        
        if (found == null) {
            player.sendMessage(
                Text.literal("[RRT] 未找到附近的盾构机。请站在盾构机5格范围内。")
                    .formatted(Formatting.RED),
                false
            );
            return 0;
        }
        
        found.setBoringDirection(direction);
        found.markDirty();
        
        player.sendMessage(
            Text.literal("[RRT] 已设置盾构机方向为: " + direction.getName() + " (位置: " + 
                foundPos.getX() + ", " + foundPos.getY() + ", " + foundPos.getZ() + ")")
                .formatted(Formatting.GREEN),
            false
        );
        
        return 1;
    }
    
    /**
     * 设置盾构机起点
     */
    private static int executeSetDaojiStart(ServerCommandSource source, int x, int y, int z) {
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
        net.minecraft.util.math.BlockPos playerPos = player.getBlockPos();
        com.real.rail.transit.block.entity.DaojiMachineBlockEntity found = null;
        net.minecraft.util.math.BlockPos foundPos = null;
        
        // 在玩家周围5格内查找盾构机
        for (int dx = -5; dx <= 5 && found == null; dx++) {
            for (int dy = -5; dy <= 5 && found == null; dy++) {
                for (int dz = -5; dz <= 5 && found == null; dz++) {
                    net.minecraft.util.math.BlockPos checkPos = playerPos.add(dx, dy, dz);
                    var blockEntity = world.getBlockEntity(checkPos);
                    if (blockEntity instanceof com.real.rail.transit.block.entity.DaojiMachineBlockEntity entity) {
                        found = entity;
                        foundPos = checkPos;
                    }
                }
            }
        }
        
        if (found == null) {
            player.sendMessage(
                Text.literal("[RRT] 未找到附近的盾构机。请站在盾构机5格范围内。")
                    .formatted(Formatting.RED),
                false
            );
            return 0;
        }
        
        net.minecraft.util.math.BlockPos startPos = new net.minecraft.util.math.BlockPos(x, y, z);
        found.setStartPos(startPos);
        found.markDirty();
        
        player.sendMessage(
            Text.literal("[RRT] 已设置盾构机起点为: (" + x + ", " + y + ", " + z + ") (位置: " + 
                foundPos.getX() + ", " + foundPos.getY() + ", " + foundPos.getZ() + ")")
                .formatted(Formatting.GREEN),
            false
        );
        
        return 1;
    }
    
    /**
     * 设置盾构机终点
     */
    private static int executeSetDaojiEnd(ServerCommandSource source, int x, int y, int z) {
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
        net.minecraft.util.math.BlockPos playerPos = player.getBlockPos();
        com.real.rail.transit.block.entity.DaojiMachineBlockEntity found = null;
        net.minecraft.util.math.BlockPos foundPos = null;
        
        // 在玩家周围5格内查找盾构机
        for (int dx = -5; dx <= 5 && found == null; dx++) {
            for (int dy = -5; dy <= 5 && found == null; dy++) {
                for (int dz = -5; dz <= 5 && found == null; dz++) {
                    net.minecraft.util.math.BlockPos checkPos = playerPos.add(dx, dy, dz);
                    var blockEntity = world.getBlockEntity(checkPos);
                    if (blockEntity instanceof com.real.rail.transit.block.entity.DaojiMachineBlockEntity entity) {
                        found = entity;
                        foundPos = checkPos;
                    }
                }
            }
        }
        
        if (found == null) {
            player.sendMessage(
                Text.literal("[RRT] 未找到附近的盾构机。请站在盾构机5格范围内。")
                    .formatted(Formatting.RED),
                false
            );
            return 0;
        }
        
        net.minecraft.util.math.BlockPos endPos = new net.minecraft.util.math.BlockPos(x, y, z);
        found.setEndPos(endPos);
        found.markDirty();
        
        player.sendMessage(
            Text.literal("[RRT] 已设置盾构机终点为: (" + x + ", " + y + ", " + z + ") (位置: " + 
                foundPos.getX() + ", " + foundPos.getY() + ", " + foundPos.getZ() + ")")
                .formatted(Formatting.GREEN),
            false
        );
        
        return 1;
    }
    
    /**
     * 设置盾构机路径类型
     */
    private static int executeSetDaojiPathType(ServerCommandSource source, String pathType) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendFeedback(
                () -> Text.literal("[RRT] 此指令只能由玩家在游戏内执行。")
                    .formatted(Formatting.RED),
                false
            );
            return 0;
        }
        
        if (!pathType.equals("straight") && !pathType.equals("curve")) {
            player.sendMessage(
                Text.literal("[RRT] 无效的路径类型: " + pathType + "。可用类型: straight, curve")
                    .formatted(Formatting.RED),
                false
            );
            return 0;
        }
        
        var world = player.getServerWorld();
        net.minecraft.util.math.BlockPos playerPos = player.getBlockPos();
        com.real.rail.transit.block.entity.DaojiMachineBlockEntity found = null;
        net.minecraft.util.math.BlockPos foundPos = null;
        
        // 在玩家周围5格内查找盾构机
        for (int dx = -5; dx <= 5 && found == null; dx++) {
            for (int dy = -5; dy <= 5 && found == null; dy++) {
                for (int dz = -5; dz <= 5 && found == null; dz++) {
                    net.minecraft.util.math.BlockPos checkPos = playerPos.add(dx, dy, dz);
                    var blockEntity = world.getBlockEntity(checkPos);
                    if (blockEntity instanceof com.real.rail.transit.block.entity.DaojiMachineBlockEntity entity) {
                        found = entity;
                        foundPos = checkPos;
                    }
                }
            }
        }
        
        if (found == null) {
            player.sendMessage(
                Text.literal("[RRT] 未找到附近的盾构机。请站在盾构机5格范围内。")
                    .formatted(Formatting.RED),
                false
            );
            return 0;
        }
        
        found.setPathType(pathType);
        found.markDirty();
        
        player.sendMessage(
            Text.literal("[RRT] 已设置盾构机路径类型为: " + pathType + " (位置: " + 
                foundPos.getX() + ", " + foundPos.getY() + ", " + foundPos.getZ() + ")")
                .formatted(Formatting.GREEN),
            false
        );
        
        return 1;
    }
    
    /**
     * 启动盾构机
     */
    private static int executeStartDaoji(ServerCommandSource source) {
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
        net.minecraft.util.math.BlockPos playerPos = player.getBlockPos();
        com.real.rail.transit.block.entity.DaojiMachineBlockEntity found = null;
        net.minecraft.util.math.BlockPos foundPos = null;
        
        // 在玩家周围5格内查找盾构机
        for (int dx = -5; dx <= 5 && found == null; dx++) {
            for (int dy = -5; dy <= 5 && found == null; dy++) {
                for (int dz = -5; dz <= 5 && found == null; dz++) {
                    net.minecraft.util.math.BlockPos checkPos = playerPos.add(dx, dy, dz);
                    var blockEntity = world.getBlockEntity(checkPos);
                    if (blockEntity instanceof com.real.rail.transit.block.entity.DaojiMachineBlockEntity entity) {
                        found = entity;
                        foundPos = checkPos;
                    }
                }
            }
        }
        
        if (found == null) {
            player.sendMessage(
                Text.literal("[RRT] 未找到附近的盾构机。请站在盾构机5格范围内。")
                    .formatted(Formatting.RED),
                false
            );
            return 0;
        }
        
        // 如果设置了起点和终点，重新计算路径
        if (found.getStartPos() != null && found.getEndPos() != null) {
            found.setPathPoints(null); // 清除旧路径，触发重新计算
            found.setProgress(0); // 重置进度
        }
        
        found.setWorking(true);
        found.markDirty();
        
        // 更新方块状态
        net.minecraft.block.BlockState state = world.getBlockState(foundPos);
        if (state.getBlock() instanceof com.real.rail.transit.block.DaojiMachineBlock) {
            world.setBlockState(foundPos, state.with(com.real.rail.transit.block.DaojiMachineBlock.WORKING, true));
        }
        
        // 通知 WorldTickHandler
        com.real.rail.transit.world.WorldTickHandler.notifyDaojiStarted(world, foundPos);
        
        player.sendMessage(
            Text.literal("[RRT] 已启动盾构机 (位置: " + 
                foundPos.getX() + ", " + foundPos.getY() + ", " + foundPos.getZ() + ")")
                .formatted(Formatting.GREEN),
            false
        );
        
        if (found.getStartPos() != null && found.getEndPos() != null) {
            player.sendMessage(
                Text.literal("起点: (" + found.getStartPos().getX() + ", " + found.getStartPos().getY() + ", " + found.getStartPos().getZ() + 
                    ") -> 终点: (" + found.getEndPos().getX() + ", " + found.getEndPos().getY() + ", " + found.getEndPos().getZ() + ")")
                    .formatted(Formatting.AQUA),
                false
            );
        }
        
        return 1;
    }
    
    /**
     * 停止盾构机
     */
    private static int executeStopDaoji(ServerCommandSource source) {
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
        net.minecraft.util.math.BlockPos playerPos = player.getBlockPos();
        com.real.rail.transit.block.entity.DaojiMachineBlockEntity found = null;
        net.minecraft.util.math.BlockPos foundPos = null;
        
        // 在玩家周围5格内查找盾构机
        for (int dx = -5; dx <= 5 && found == null; dx++) {
            for (int dy = -5; dy <= 5 && found == null; dy++) {
                for (int dz = -5; dz <= 5 && found == null; dz++) {
                    net.minecraft.util.math.BlockPos checkPos = playerPos.add(dx, dy, dz);
                    var blockEntity = world.getBlockEntity(checkPos);
                    if (blockEntity instanceof com.real.rail.transit.block.entity.DaojiMachineBlockEntity entity) {
                        found = entity;
                        foundPos = checkPos;
                    }
                }
            }
        }
        
        if (found == null) {
            player.sendMessage(
                Text.literal("[RRT] 未找到附近的盾构机。请站在盾构机5格范围内。")
                    .formatted(Formatting.RED),
                false
            );
            return 0;
        }
        
        found.setWorking(false);
        found.markDirty();
        
        // 更新方块状态
        net.minecraft.block.BlockState state = world.getBlockState(foundPos);
        if (state.getBlock() instanceof com.real.rail.transit.block.DaojiMachineBlock) {
            world.setBlockState(foundPos, state.with(com.real.rail.transit.block.DaojiMachineBlock.WORKING, false));
        }
        
        // 通知 WorldTickHandler
        com.real.rail.transit.world.WorldTickHandler.notifyDaojiStopped(foundPos);
        
        player.sendMessage(
            Text.literal("[RRT] 已停止盾构机 (位置: " + 
                foundPos.getX() + ", " + foundPos.getY() + ", " + foundPos.getZ() + ")")
                .formatted(Formatting.YELLOW),
            false
        );
        
        return 1;
    }
}


