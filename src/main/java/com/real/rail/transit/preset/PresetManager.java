package com.real.rail.transit.preset;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 预设器管理器
 * 管理每个玩家的预设器两点选择状态
 */
public class PresetManager {
    private static final PresetManager INSTANCE = new PresetManager();
    
    /**
     * 存储每个玩家的预设器状态
     * Key: 玩家UUID, Value: 预设器数据
     */
    private final Map<UUID, PresetData> playerPresets = new HashMap<>();
    
    private PresetManager() {
    }
    
    public static PresetManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 设置第一个点
     */
    public void setFirstPoint(UUID playerId, BlockPos pos) {
        PresetData data = playerPresets.computeIfAbsent(playerId, k -> new PresetData());
        data.firstPos = pos;
        data.secondPos = null;
    }
    
    /**
     * 设置第二个点
     */
    public void setSecondPoint(UUID playerId, BlockPos pos) {
        PresetData data = playerPresets.computeIfAbsent(playerId, k -> new PresetData());
        data.secondPos = pos;
    }
    
    /**
     * 获取预设器数据
     */
    public PresetData getPresetData(UUID playerId) {
        return playerPresets.get(playerId);
    }
    
    /**
     * 清除预设器数据
     */
    public void clearPresetData(UUID playerId) {
        playerPresets.remove(playerId);
    }
    
    /**
     * 检查是否已选择两个点
     */
    public boolean isComplete(UUID playerId) {
        PresetData data = playerPresets.get(playerId);
        return data != null && data.firstPos != null && data.secondPos != null;
    }
    
    /**
     * 预设器数据类
     */
    public static class PresetData {
        public BlockPos firstPos;
        public BlockPos secondPos;
        
        /**
         * 检查两点是否在同一条直线上
         */
        private boolean isStraightLine() {
            if (firstPos == null || secondPos == null) {
                return false;
            }
            
            // 计算三个方向的差值
            int dx = Math.abs(secondPos.getX() - firstPos.getX());
            int dy = Math.abs(secondPos.getY() - firstPos.getY());
            int dz = Math.abs(secondPos.getZ() - firstPos.getZ());
            
            // 如果至少有两个方向为0，或者三个方向的比例相同，则为直线
            int nonZeroCount = 0;
            if (dx > 0) nonZeroCount++;
            if (dy > 0) nonZeroCount++;
            if (dz > 0) nonZeroCount++;
            
            // 如果只有一个或零个方向非零，肯定是直线
            if (nonZeroCount <= 1) {
                return true;
            }
            
            // 如果三个方向都非零，检查是否成比例（允许一定误差）
            if (nonZeroCount == 3) {
                // 计算比例
                double ratioX = dx > 0 ? (double) dx : 0;
                double ratioY = dy > 0 ? (double) dy : 0;
                double ratioZ = dz > 0 ? (double) dz : 0;
                
                // 归一化
                double max = Math.max(Math.max(ratioX, ratioY), ratioZ);
                if (max > 0) {
                    ratioX /= max;
                    ratioY /= max;
                    ratioZ /= max;
                    
                    // 检查是否接近整数比例（允许0.1的误差）
                    return Math.abs(ratioX - Math.round(ratioX)) < 0.1 &&
                           Math.abs(ratioY - Math.round(ratioY)) < 0.1 &&
                           Math.abs(ratioZ - Math.round(ratioZ)) < 0.1;
                }
            }
            
            // 两个方向非零的情况，检查是否在同一平面上
            return nonZeroCount == 2;
        }
        
        /**
         * 计算二次贝塞尔曲线上的点
         * @param t 参数，范围 [0, 1]
         * @param p0 起点
         * @param p1 控制点
         * @param p2 终点
         */
        private BlockPos bezierPoint(double t, BlockPos p0, BlockPos p1, BlockPos p2) {
            double u = 1 - t;
            double tt = t * t;
            double uu = u * u;
            
            double x = uu * p0.getX() + 2 * u * t * p1.getX() + tt * p2.getX();
            double y = uu * p0.getY() + 2 * u * t * p1.getY() + tt * p2.getY();
            double z = uu * p0.getZ() + 2 * u * t * p1.getZ() + tt * p2.getZ();
            
            return new BlockPos((int) Math.round(x), (int) Math.round(y), (int) Math.round(z));
        }
        
        /**
         * 计算控制点（用于贝塞尔曲线）
         */
        private BlockPos calculateControlPoint() {
            // 计算两点之间的中点
            int midX = (firstPos.getX() + secondPos.getX()) / 2;
            int midY = (firstPos.getY() + secondPos.getY()) / 2;
            int midZ = (firstPos.getZ() + secondPos.getZ()) / 2;
            
            // 计算方向向量
            int dx = secondPos.getX() - firstPos.getX();
            int dy = secondPos.getY() - firstPos.getY();
            int dz = secondPos.getZ() - firstPos.getZ();
            
            // 计算距离
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            
            // 如果距离太短，直接使用中点
            if (distance < 3) {
                return new BlockPos(midX, midY, midZ);
            }
            
            // 计算垂直方向（用于创建曲线）
            // 选择一个垂直于方向向量的方向
            int perpX = -dz;
            int perpY = 0;
            int perpZ = dx;
            
            // 如果垂直向量为零，尝试另一个方向
            if (perpX == 0 && perpZ == 0) {
                perpX = -dy;
                perpY = dx;
                perpZ = 0;
            }
            
            // 归一化垂直向量
            double perpLength = Math.sqrt(perpX * perpX + perpY * perpY + perpZ * perpZ);
            if (perpLength > 0) {
                perpX = (int) Math.round(perpX / perpLength * distance * 0.3);
                perpY = (int) Math.round(perpY / perpLength * distance * 0.3);
                perpZ = (int) Math.round(perpZ / perpLength * distance * 0.3);
            }
            
            // 控制点在中点附近，稍微偏移以创建曲线
            return new BlockPos(midX + perpX, midY + perpY, midZ + perpZ);
        }
        
        /**
         * 计算预设器路径上的所有方块位置
         * 如果两点在同一直线上，使用直线；否则使用贝塞尔曲线
         */
        public java.util.List<BlockPos> calculatePath() {
            if (firstPos == null || secondPos == null) {
                return java.util.Collections.emptyList();
            }
            
            java.util.List<BlockPos> path = new java.util.ArrayList<>();
            
            // 检查是否在同一直线上
            if (isStraightLine()) {
                // 使用直线路径
                int dx = secondPos.getX() - firstPos.getX();
                int dy = secondPos.getY() - firstPos.getY();
                int dz = secondPos.getZ() - firstPos.getZ();
                
                int steps = Math.max(Math.max(Math.abs(dx), Math.abs(dy)), Math.abs(dz));
                
                if (steps == 0) {
                    path.add(firstPos);
                    return path;
                }
                
                for (int i = 0; i <= steps; i++) {
                    double t = (double) i / steps;
                    int x = (int) Math.round(firstPos.getX() + dx * t);
                    int y = (int) Math.round(firstPos.getY() + dy * t);
                    int z = (int) Math.round(firstPos.getZ() + dz * t);
                    path.add(new BlockPos(x, y, z));
                }
            } else {
                // 使用贝塞尔曲线
                BlockPos controlPoint = calculateControlPoint();
                
                // 计算曲线上的点数（基于距离）
                double distance = Math.sqrt(
                    Math.pow(secondPos.getX() - firstPos.getX(), 2) +
                    Math.pow(secondPos.getY() - firstPos.getY(), 2) +
                    Math.pow(secondPos.getZ() - firstPos.getZ(), 2)
                );
                
                int steps = Math.max((int) Math.ceil(distance), 10);
                
                BlockPos lastPos = null;
                for (int i = 0; i <= steps; i++) {
                    double t = (double) i / steps;
                    BlockPos pos = bezierPoint(t, firstPos, controlPoint, secondPos);
                    
                    // 避免重复添加相同位置
                    if (lastPos == null || !pos.equals(lastPos)) {
                        path.add(pos);
                        lastPos = pos;
                    }
                }
            }
            
            return path;
        }
        
        /**
         * 检查位置是否在预设器路径上（考虑贝塞尔曲线）
         */
        public boolean isOnPath(BlockPos pos) {
            java.util.List<BlockPos> path = calculatePath();
            
            // 直接检查是否在路径中
            if (path.contains(pos)) {
                return true;
            }
            
            // 检查是否在路径附近（允许1格误差）
            for (BlockPos pathPos : path) {
                if (pos.isWithinDistance(pathPos, 1.5)) {
                    return true;
                }
            }
            
            return false;
        }
    }
}

