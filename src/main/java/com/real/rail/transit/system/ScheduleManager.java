package com.real.rail.transit.system;

import com.real.rail.transit.system.DispatchSystem.ScheduleEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * 运行图管理器
 * 负责运行图的保存、加载和管理
 */
public class ScheduleManager {
    private static final ScheduleManager INSTANCE = new ScheduleManager();
    private static final String SCHEDULE_DIR = "rrt_schedules";
    
    private ScheduleManager() {
    }
    
    public static ScheduleManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 保存运行图到文件
     */
    public boolean saveSchedule(String scheduleName, ScheduleEntry schedule) {
        try {
            Path scheduleDir = Paths.get(SCHEDULE_DIR);
            if (!Files.exists(scheduleDir)) {
                Files.createDirectories(scheduleDir);
            }
            
            Path scheduleFile = scheduleDir.resolve(scheduleName + ".json");
            
            // 将运行图转换为JSON格式
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"train_id\": \"").append(schedule.getTrainId()).append("\",\n");
            json.append("  \"train_name\": \"").append(schedule.getTrainId()).append("\",\n");
            json.append("  \"start_station\": \"").append(schedule.getStartStation()).append("\",\n");
            json.append("  \"end_station\": \"").append(schedule.getEndStation()).append("\",\n");
            json.append("  \"departure_time\": ").append(schedule.getDepartureTime()).append(",\n");
            json.append("  \"interval\": ").append(schedule.getInterval()).append(",\n");
            json.append("  \"stations\": [\n");
            
            List<String> stations = schedule.getStations();
            for (int i = 0; i < stations.size(); i++) {
                json.append("    \"").append(stations.get(i)).append("\"");
                if (i < stations.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }
            
            json.append("  ],\n");
            json.append("  \"stop_times\": {\n");
            
            int index = 0;
            for (var entry : schedule.getStopTimes().entrySet()) {
                json.append("    \"").append(entry.getKey()).append("\": ").append(entry.getValue());
                if (index < schedule.getStopTimes().size() - 1) {
                    json.append(",");
                }
                json.append("\n");
                index++;
            }
            
            json.append("  }\n");
            json.append("}\n");
            
            Files.writeString(scheduleFile, json.toString());
            
            com.real.rail.transit.RealRailTransitMod.LOGGER.info("保存运行图: {}", scheduleName);
            return true;
        } catch (IOException e) {
            com.real.rail.transit.RealRailTransitMod.LOGGER.error("保存运行图失败: " + scheduleName, e);
            return false;
        }
    }
    
    /**
     * 从文件加载运行图
     */
    public ScheduleEntry loadSchedule(String scheduleName) {
        try {
            Path scheduleFile = Paths.get(SCHEDULE_DIR, scheduleName + ".json");
            if (!Files.exists(scheduleFile)) {
                com.real.rail.transit.RealRailTransitMod.LOGGER.warn("运行图文件不存在: {}", scheduleName);
                return null;
            }
            
            String jsonContent = Files.readString(scheduleFile);
            
            // 由于当前项目未引入 JSON 解析库，这里采用“约定格式 + 简单解析”的方式
            // 解析字段：train_id, start_station, end_station, departure_time, interval, stations[], stop_times{}
            Map<String, Object> parsed = parseScheduleJson(jsonContent);
            if (parsed == null) {
                com.real.rail.transit.RealRailTransitMod.LOGGER.warn("解析运行图失败: {}", scheduleName);
                return null;
            }

            String trainId = (String) parsed.getOrDefault("train_id", "");
            String startStation = (String) parsed.getOrDefault("start_station", "");
            String endStation = (String) parsed.getOrDefault("end_station", "");
            if (trainId.isEmpty() || startStation.isEmpty() || endStation.isEmpty()) {
                com.real.rail.transit.RealRailTransitMod.LOGGER.warn("运行图字段不完整: {}", scheduleName);
                return null;
            }

            ScheduleEntry entry = new ScheduleEntry(trainId, startStation, endStation);
            Long departureTime = (Long) parsed.get("departure_time");
            if (departureTime != null) {
                entry.setDepartureTime(departureTime);
            }
            Integer interval = (Integer) parsed.get("interval");
            if (interval != null) {
                entry.setInterval(interval);
            }
            @SuppressWarnings("unchecked")
            List<String> stations = (List<String>) parsed.get("stations");
            if (stations != null) {
                entry.setStations(stations);
            }
            @SuppressWarnings("unchecked")
            Map<String, Integer> stopTimes = (Map<String, Integer>) parsed.get("stop_times");
            if (stopTimes != null) {
                entry.setStopTimes(stopTimes);
            }

            com.real.rail.transit.RealRailTransitMod.LOGGER.info("成功加载运行图: {}", scheduleName);
            return entry;
        } catch (IOException e) {
            com.real.rail.transit.RealRailTransitMod.LOGGER.error("加载运行图失败: " + scheduleName, e);
            return null;
        }
    }
    
    /**
     * 获取所有已保存的运行图名称列表
     */
    public List<String> listSchedules() {
        List<String> schedules = new ArrayList<>();
        try {
            Path scheduleDir = Paths.get(SCHEDULE_DIR);
            if (!Files.exists(scheduleDir)) {
                return schedules;
            }
            
            Files.list(scheduleDir)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(path -> {
                    String fileName = path.getFileName().toString();
                    schedules.add(fileName.substring(0, fileName.length() - 5)); // 移除.json扩展名
                });
        } catch (IOException e) {
            com.real.rail.transit.RealRailTransitMod.LOGGER.error("列出运行图失败", e);
        }
        return schedules;
    }
    
    /**
     * 删除运行图文件
     */
    public boolean deleteSchedule(String scheduleName) {
        try {
            Path scheduleFile = Paths.get(SCHEDULE_DIR, scheduleName + ".json");
            if (Files.exists(scheduleFile)) {
                Files.delete(scheduleFile);
                return true;
            }
            return false;
        } catch (IOException e) {
            com.real.rail.transit.RealRailTransitMod.LOGGER.error("删除运行图失败: " + scheduleName, e);
            return false;
        }
    }
    
    /**
     * 简单解析 saveSchedule 写出的 JSON 文本。
     * 由于格式是本类自己写出的，可以安全地基于固定格式做解析，避免引入额外依赖。
     */
    private Map<String, Object> parseScheduleJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        
        Map<String, Object> result = new HashMap<>();
        
        // 逐行解析，忽略空行和大括号
        String[] lines = json.split("\\r?\\n");
        boolean inStations = false;
        boolean inStopTimes = false;
        List<String> stations = new ArrayList<>();
        Map<String, Integer> stopTimes = new HashMap<>();
        
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || "{".equals(line) || "}".equals(line)) {
                continue;
            }
            
            if (line.startsWith("\"stations\"")) {
                inStations = true;
                inStopTimes = false;
                continue;
            }
            if (line.startsWith("\"stop_times\"")) {
                inStations = false;
                inStopTimes = true;
                continue;
            }
            
            if (inStations) {
                if (line.startsWith("]")) {
                    inStations = false;
                    continue;
                }
                // 形如: "    "StationA","
                String value = extractStringValue(line);
                if (value != null) {
                    stations.add(value);
                }
                continue;
            }
            
            if (inStopTimes) {
                if (line.startsWith("}")) {
                    inStopTimes = false;
                    continue;
                }
                // 形如: "    "StationA": 100,"
                int colonIndex = line.indexOf(':');
                if (colonIndex > 0) {
                    String keyPart = line.substring(0, colonIndex).trim();
                    String valuePart = line.substring(colonIndex + 1).trim();
                    String key = extractStringValue(keyPart);
                    if (key != null) {
                        int value = parseIntSafe(trimTrailingComma(valuePart));
                        stopTimes.put(key, value);
                    }
                }
                continue;
            }
            
            // 顶层简单键值对，如 "train_id": "XXX", 或 "departure_time": 123,
            int colonIndex = line.indexOf(':');
            if (colonIndex <= 0) {
                continue;
            }
            String keyPart = line.substring(0, colonIndex).trim();
            String valuePart = line.substring(colonIndex + 1).trim();
            String key = extractStringValue(keyPart);
            if (key == null) {
                continue;
            }
            valuePart = trimTrailingComma(valuePart);
            
            switch (key) {
                case "train_id":
                case "train_name":
                case "start_station":
                case "end_station":
                    result.put(key, stripQuotes(valuePart));
                    break;
                case "departure_time":
                    result.put(key, parseLongSafe(valuePart));
                    break;
                case "interval":
                    result.put(key, parseIntSafe(valuePart));
                    break;
                default:
                    break;
            }
        }
        
        if (!stations.isEmpty()) {
            result.put("stations", stations);
        }
        if (!stopTimes.isEmpty()) {
            result.put("stop_times", stopTimes);
        }
        
        return result;
    }
    
    private String trimTrailingComma(String value) {
        if (value.endsWith(",")) {
            return value.substring(0, value.length() - 1).trim();
        }
        return value;
    }
    
    private String stripQuotes(String value) {
        String v = value.trim();
        if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
            return v.substring(1, v.length() - 1);
        }
        return v;
    }
    
    private String extractStringValue(String part) {
        String v = part.trim();
        int firstQuote = v.indexOf('"');
        int lastQuote = v.lastIndexOf('"');
        if (firstQuote >= 0 && lastQuote > firstQuote) {
            return v.substring(firstQuote + 1, lastQuote);
        }
        return null;
    }
    
    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private long parseLongSafe(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}