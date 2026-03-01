package com.real.rail.transit.util;

import com.real.rail.transit.RealRailTransitMod;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 模组运行日志
 * 在游戏内以彩色文本形式查看模组运行时的关键信息 / 警告 / 错误。
 */
public class ModRuntimeLog {

    public enum Level {
        INFO,
        WARN,
        ERROR
    }

    public record Entry(long gameTime, long epochMillis, Level level, String message) {}

    private static final int MAX_ENTRIES = 500;
    private static final Deque<Entry> ENTRIES = new ArrayDeque<>();

    private static final DateTimeFormatter TIME_FORMATTER =
        DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private ModRuntimeLog() {
    }

    private static void addEntry(Level level, String message, ServerWorld world) {
        long gameTime = world != null ? world.getTime() : -1;
        Entry entry = new Entry(gameTime, System.currentTimeMillis(), level, message);
        synchronized (ENTRIES) {
            ENTRIES.addLast(entry);
            while (ENTRIES.size() > MAX_ENTRIES) {
                ENTRIES.removeFirst();
            }
        }
    }

    public static void info(String message, ServerWorld world) {
        RealRailTransitMod.LOGGER.info(message);
        addEntry(Level.INFO, message, world);
    }

    public static void warn(String message, ServerWorld world) {
        RealRailTransitMod.LOGGER.warn(message);
        addEntry(Level.WARN, message, world);
    }

    public static void error(String message, Throwable throwable, ServerWorld world) {
        RealRailTransitMod.LOGGER.error(message, throwable);
        addEntry(Level.ERROR, message, world);
    }

    /**
     * 客户端版本的日志方法（world 为 null）
     * 用于客户端代码（如模型加载、渲染等）记录日志
     */
    public static void info(String message) {
        RealRailTransitMod.LOGGER.info(message);
        addEntry(Level.INFO, message, null);
    }

    public static void warn(String message) {
        RealRailTransitMod.LOGGER.warn(message);
        addEntry(Level.WARN, message, null);
    }

    public static void error(String message, Throwable throwable) {
        RealRailTransitMod.LOGGER.error(message, throwable);
        addEntry(Level.ERROR, message, null);
    }

    public static java.util.List<Entry> getRecentEntries(int limit) {
        java.util.List<Entry> list;
        synchronized (ENTRIES) {
            list = new java.util.ArrayList<>(ENTRIES);
        }
        int size = list.size();
        if (limit <= 0 || limit >= size) {
            return list;
        }
        return list.subList(size - limit, size);
    }

    /**
     * 将日志条目格式化为带有颜色的聊天文本。
     */
    public static Text formatEntry(Entry entry) {
        String timeStr = TIME_FORMATTER.format(Instant.ofEpochMilli(entry.epochMillis()));
        String prefix = "[" + timeStr + "] ";
        Formatting color = switch (entry.level) {
            case INFO -> Formatting.GREEN;
            case WARN -> Formatting.YELLOW;
            case ERROR -> Formatting.RED;
        };
        return Text.literal(prefix)
            .formatted(Formatting.GRAY)
            .append(Text.literal("[" + entry.level().name() + "] ").formatted(color))
            .append(Text.literal(entry.message()).formatted(color));
    }
}


