package com.real.rail.transit.system;

import com.real.rail.transit.entity.TrainEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

/**
 * 调度系统
 * 中央调度系统，管理列车运行图和调度指令
 */
public class DispatchSystem {
    private static final DispatchSystem INSTANCE = new DispatchSystem();
    
    /**
     * 运行图条目
     */
    public static class ScheduleEntry {
        private String trainId;           // 车次号
        private String startStation;      // 始发站
        private String endStation;        // 终点站
        private long departureTime;       // 发车时间（Minecraft tick）
        private int interval;             // 发车间隔（tick）
        private List<String> stations;    // 停靠站列表
        private Map<String, Integer> stopTimes; // 各站停站时间（tick）
        
        public ScheduleEntry(String trainId, String startStation, String endStation) {
            this.trainId = trainId;
            this.startStation = startStation;
            this.endStation = endStation;
            this.stations = new ArrayList<>();
            this.stopTimes = new HashMap<>();
        }
        
        // Getters and Setters
        public String getTrainId() { return trainId; }
        public void setTrainId(String trainId) { this.trainId = trainId; }
        
        public String getStartStation() { return startStation; }
        public void setStartStation(String startStation) { this.startStation = startStation; }
        
        public String getEndStation() { return endStation; }
        public void setEndStation(String endStation) { this.endStation = endStation; }
        
        public long getDepartureTime() { return departureTime; }
        public void setDepartureTime(long departureTime) { this.departureTime = departureTime; }
        
        public int getInterval() { return interval; }
        public void setInterval(int interval) { this.interval = interval; }
        
        public List<String> getStations() { return stations; }
        public void setStations(List<String> stations) { this.stations = stations; }
        
        public Map<String, Integer> getStopTimes() { return stopTimes; }
        public void setStopTimes(Map<String, Integer> stopTimes) { this.stopTimes = stopTimes; }
    }
    
    // 存储运行图
    private final Map<String, ScheduleEntry> schedules = new HashMap<>();
    
    // 存储活跃列车
    private final Map<String, TrainEntity> activeTrains = new HashMap<>();
    
    /**
     * 停站状态：记录列车当前停靠的车站以及开始停站的时间
     */
    private final Map<String, Long> trainStopStartTimes = new HashMap<>();
    private final Map<String, String> trainCurrentStations = new HashMap<>();
    
    private DispatchSystem() {
    }
    
    public static DispatchSystem getInstance() {
        return INSTANCE;
    }
    
    /**
     * 添加运行图条目
     */
    public void addSchedule(ScheduleEntry schedule) {
        schedules.put(schedule.getTrainId(), schedule);
    }
    
    /**
     * 获取运行图条目
     */
    public ScheduleEntry getSchedule(String trainId) {
        return schedules.get(trainId);
    }
    
    /**
     * 移除运行图条目
     */
    public void removeSchedule(String trainId) {
        schedules.remove(trainId);
    }
    
    /**
     * 注册活跃列车
     */
    public void registerTrain(String trainId, TrainEntity train) {
        activeTrains.put(trainId, train);
    }
    
    /**
     * 取消注册列车
     */
    public void unregisterTrain(String trainId) {
        activeTrains.remove(trainId);
    }
    
    /**
     * 获取活跃列车
     */
    public TrainEntity getTrain(String trainId) {
        return activeTrains.get(trainId);
    }
    
    /**
     * 更新调度系统（每tick调用）
     */
    public void update(World world) {
        long currentTime = world.getTime();
        
        // 检查是否需要发车
        for (ScheduleEntry schedule : schedules.values()) {
            if (shouldDispatch(currentTime, schedule)) {
                dispatchTrain(world, schedule);
            }
        }
        
        // 更新所有活跃列车的状态
        for (TrainEntity train : activeTrains.values()) {
            updateTrainStatus(world, train, currentTime);
        }
    }
    
    /**
     * 判断是否应该发车
     */
    private boolean shouldDispatch(long currentTime, ScheduleEntry schedule) {
        if (currentTime < schedule.getDepartureTime()) {
            return false;
        }
        
        // 检查发车间隔
        long timeSinceLastDispatch = (currentTime - schedule.getDepartureTime()) % schedule.getInterval();
        return timeSinceLastDispatch == 0;
    }
    
    /**
     * 发车
     */
    private void dispatchTrain(World world, ScheduleEntry schedule) {
        // 检查是否已经有相同车次的列车在运行
        if (activeTrains.containsKey(schedule.getTrainId())) {
            return; // 已有列车在运行，不发车
        }
        
        // 查找始发站位置（简化：使用调度系统的车站位置映射）
        BlockPos startPos = findStationPosition(world, schedule.getStartStation());
        if (startPos == null) {
            com.real.rail.transit.RealRailTransitMod.LOGGER.warn("找不到始发站: {}", schedule.getStartStation());
            return;
        }
        
        // 生成列车实体
        TrainEntity train = new TrainEntity(
            com.real.rail.transit.registry.ModEntities.TRAIN,
            world
        );
        train.setPosition(startPos.getX() + 0.5, startPos.getY() + 1, startPos.getZ() + 0.5);
        
        // 设置列车运行参数
        train.setTrainId(schedule.getTrainId());
        train.setDrivingMode(TrainEntity.DrivingMode.ATO); // 使用自动驾驶模式
        
        // 生成列车到世界
        world.spawnEntity(train);
        
        // 注册到活跃列车列表
        registerTrain(schedule.getTrainId(), train);
        
        String logMsg = String.format("列车 %s 已发车，从 %s 到 %s",
            schedule.getTrainId(), schedule.getStartStation(), schedule.getEndStation());
        com.real.rail.transit.RealRailTransitMod.LOGGER.info(logMsg);
        if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            com.real.rail.transit.util.ModRuntimeLog.info(logMsg, serverWorld);
        }
    }
    
    /**
     * 更新列车状态
     */
    private void updateTrainStatus(World world, TrainEntity train, long currentTime) {
        // 检查列车是否还在世界中
        if (train.isRemoved() || !train.isAlive()) {
            // 列车已移除，取消注册
            String trainId = train.getTrainId();
            if (trainId != null) {
                unregisterTrain(trainId);
                trainStopStartTimes.remove(trainId);
                trainCurrentStations.remove(trainId);
            }
            return;
        }
        
        // 检查是否到达车站
        BlockPos trainPos = train.getBlockPos();
        ScheduleEntry schedule = getSchedule(train.getTrainId());
        if (schedule != null) {
            String trainId = train.getTrainId();
            if (trainId == null) {
                return;
            }
            
            // 检查是否正在某站停靠
            String currentStation = trainCurrentStations.get(trainId);
            Long stopStartTime = trainStopStartTimes.get(trainId);
            
            if (currentStation != null && stopStartTime != null) {
                // 正在停站，判断是否到了发车时间
                Integer stopDuration = schedule.getStopTimes().get(currentStation);
                if (stopDuration == null) {
                    // 未配置停站时间则默认 100 tick
                    stopDuration = 100;
                }
                
                if (currentTime - stopStartTime >= stopDuration) {
                    // 停站时间已到，重新启动列车
                    // 简化：设定一个默认巡航速度
                    train.setTargetSpeed(20.0);
                    trainCurrentStations.remove(trainId);
                    trainStopStartTimes.remove(trainId);
                } else {
                    // 继续保持停车状态
                    train.setTargetSpeed(0);
                }
                return;
            }
            
            // 未处于停站状态，检查是否到达新的停靠站
            for (String station : schedule.getStations()) {
                BlockPos stationPos = findStationPosition(world, station);
                if (stationPos != null && trainPos.isWithinDistance(stationPos, 3.0)) {
                    // 到达车站，开始停站
                    train.setTargetSpeed(0);
                    trainCurrentStations.put(trainId, station);
                    trainStopStartTimes.put(trainId, currentTime);
                    
                    // 这里可以触发开门、广播等逻辑，留作后续扩展
                    break;
                }
            }
        }
    }
    
    /**
     * 查找车站位置
     * 优先查找带有对应站点ID/名称的站点标记方块，找不到时回退到旧的哈希坐标方案。
     */
    private BlockPos findStationPosition(World world, String stationName) {
        if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            // 在一定范围内搜索 StationMarker（以世界中心为圆心，半径 64 区块）
            // 注意：这是简化实现，未来建议改为持久化站点索引
            net.minecraft.util.math.ChunkPos center = new net.minecraft.util.math.ChunkPos(0, 0);
            final BlockPos[] result = new BlockPos[1];
            net.minecraft.util.math.ChunkPos.stream(center, 64).forEach(chunkPos -> {
                if (result[0] != null) return;
                if (serverWorld.isChunkLoaded(chunkPos.x, chunkPos.z)) {
                    int startX = chunkPos.getStartX();
                    int startZ = chunkPos.getStartZ();
                    for (int y = serverWorld.getBottomY(); y < serverWorld.getTopY() && result[0] == null; y++) {
                        for (int x = 0; x < 16 && result[0] == null; x++) {
                            for (int z = 0; z < 16; z++) {
                                BlockPos pos = new BlockPos(startX + x, y, startZ + z);
                                var state = serverWorld.getBlockState(pos);
                                if (state.getBlock() instanceof com.real.rail.transit.station.StationMarkerBlock) {
                                    var be = serverWorld.getBlockEntity(pos);
                                    if (be instanceof com.real.rail.transit.station.entity.StationMarkerBlockEntity marker) {
                                        String id = marker.getStationId();
                                        String name = marker.getStationName();
                                        if (stationName.equalsIgnoreCase(id) || stationName.equalsIgnoreCase(name)) {
                                            result[0] = pos;
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
            if (result[0] != null) return result[0];
        }
        
        // 回退实现：使用车站名称的哈希值作为坐标偏移（保持与旧版本兼容）
        int hash = stationName.hashCode();
        int x = (hash & 0xFFFF) - 32768;
        int z = ((hash >> 16) & 0xFFFF) - 32768;
        int y = 64; // 默认高度
        
        return new BlockPos(x, y, z);
    }
}

