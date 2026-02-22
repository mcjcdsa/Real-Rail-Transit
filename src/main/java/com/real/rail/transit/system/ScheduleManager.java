package com.real.rail.transit.system;

import com.real.rail.transit.system.DispatchSystem.ScheduleEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
                return null;
            }
            
            String jsonContent = Files.readString(scheduleFile);
            // TODO: 使用JSON解析库解析文件内容
            // 这里简化处理，实际应该使用Gson等库
            
            return null; // 临时返回null
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
}



