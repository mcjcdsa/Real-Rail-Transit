package com.real.rail.transit.world;

import com.real.rail.transit.RealRailTransitMod;
import com.real.rail.transit.block.entity.DaojiMachineBlockEntity;
import com.real.rail.transit.system.DispatchSystem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.Map;

/**
 * 世界Tick处理器
 * 处理每tick需要更新的系统
 */
public class WorldTickHandler {
    // 盾构机挖掘计数器（每N tick挖掘一次）
    private static final java.util.Map<BlockPos, Integer> daojiTickCounters = new java.util.HashMap<>();
    private static final int DIG_INTERVAL = 5; // 每5 tick（0.25秒）挖掘一次，加快速度
    
    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> onWorldTick(world));
    }
    
    private static void onWorldTick(ServerWorld world) {
        // 更新调度系统
        DispatchSystem.getInstance().update(world);
        
        // 更新盾构机
        updateDaojiMachines(world);
    }
    
    private static void updateDaojiMachines(ServerWorld world) {
        // 遍历所有加载的区块，查找工作中的盾构机
        // 每tick都检查，确保及时响应
        
        // 简化实现：只检查玩家附近的盾构机（实际应该用更高效的方法）
        // 这里使用一个简单的计数器来限制每tick处理的盾构机数量
        int processed = 0;
        int maxPerTick = 10;
        
        for (java.util.Map.Entry<BlockPos, Integer> entry : new java.util.ArrayList<>(daojiTickCounters.entrySet())) {
            if (processed >= maxPerTick) break;
            
            BlockPos pos = entry.getKey();
            if (!world.isChunkLoaded(pos)) {
                RealRailTransitMod.LOGGER.debug("盾构机 {} 的区块未加载，从队列移除", pos);
                daojiTickCounters.remove(pos);
                continue;
            }
            
            var blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof DaojiMachineBlockEntity entity) {
                if (entity.isWorking()) {
                    int counter = entry.getValue() + 1;
                    if (counter >= DIG_INTERVAL) {
                        RealRailTransitMod.LOGGER.debug("盾构机 {} 开始挖掘（计数器: {}）", pos, counter);
                        digTunnel(world, pos, entity);
                        counter = 0;
                    }
                    daojiTickCounters.put(pos, counter);
                    processed++;
                } else {
                    RealRailTransitMod.LOGGER.info("盾构机 {} 已停止工作，从队列移除", pos);
                    daojiTickCounters.remove(pos);
                }
            } else {
                RealRailTransitMod.LOGGER.warn("盾构机 {} 的方块实体不存在，从队列移除", pos);
                daojiTickCounters.remove(pos);
            }
        }
        
        // 查找新的工作中的盾构机（更频繁地扫描，确保及时响应）
        if (world.getServer().getTicks() % 20 == 0) {
            // 每1秒扫描一次新盾构机
            scanForDaojiMachines(world);
        }
    }
    
    private static void scanForDaojiMachines(ServerWorld world) {
        // 简化实现：只检查玩家附近的区块
        // 实际应该用更高效的方法，比如维护一个活跃盾构机列表
        for (var player : world.getPlayers()) {
            BlockPos playerPos = player.getBlockPos();
            int radius = 64;
            for (int x = -radius; x <= radius; x += 16) {
                for (int z = -radius; z <= radius; z += 16) {
                    BlockPos checkPos = playerPos.add(x, 0, z);
                    if (world.isChunkLoaded(checkPos)) {
                        var blockEntity = world.getBlockEntity(checkPos);
                        if (blockEntity instanceof DaojiMachineBlockEntity entity && entity.isWorking()) {
                            daojiTickCounters.putIfAbsent(checkPos, 0);
                        }
                    }
                }
            }
        }
    }
    
    private static void digTunnel(ServerWorld world, BlockPos machinePos, DaojiMachineBlockEntity entity) {
        // 验证工作状态
        if (!entity.isWorking()) {
            RealRailTransitMod.LOGGER.warn("盾构机 {} 的工作状态为 false，停止挖掘", machinePos);
            daojiTickCounters.remove(machinePos);
            return;
        }
        
        int width = entity.getTunnelWidth();
        int height = entity.getTunnelHeight();
        String tunnelType = entity.getTunnelType();
        BlockState replaceBlock = entity.getReplaceBlock();
        BlockState wallBlock = entity.getWallBlock();
        
        RealRailTransitMod.LOGGER.debug("盾构机 {} 开始挖掘：宽度={}, 高度={}, 类型={}", machinePos, width, height, tunnelType);
        
        // 获取路径点列表
        java.util.List<BlockPos> pathPoints = entity.getPathPoints();
        if (pathPoints == null || pathPoints.isEmpty()) {
            // 如果没有路径点，计算路径
            RealRailTransitMod.LOGGER.info("盾构机 {} 开始计算路径（起点: {}, 终点: {}）...", 
                machinePos, entity.getStartPos(), entity.getEndPos());
            pathPoints = calculatePath(entity);
            if (pathPoints == null || pathPoints.isEmpty()) {
                RealRailTransitMod.LOGGER.warn("盾构机 {} 路径计算失败或为空，使用方向挖掘模式", machinePos);
                // 如果没有起点和终点，使用旧的方式（按方向挖掘）
                digTunnelByDirection(world, machinePos, entity);
                return;
            }
            RealRailTransitMod.LOGGER.info("盾构机 {} 路径计算完成，共 {} 个路径点", machinePos, pathPoints.size());
            entity.setPathPoints(pathPoints);
        }
        
        // 获取当前进度对应的路径位置
        int progress = entity.getProgress();
        
        // 如果起点是盾构机位置，跳过第一个点
        if (progress == 0 && pathPoints.size() > 0) {
            BlockPos firstPos = pathPoints.get(0);
            if (firstPos.equals(machinePos)) {
                RealRailTransitMod.LOGGER.info("盾构机 {} 起点与盾构机位置相同，跳过第一个路径点", machinePos);
                progress = 1;
                entity.setProgress(progress);
            }
        }
        
        if (progress >= pathPoints.size()) {
            // 已经到达终点，停止挖掘
            entity.setWorking(false);
            entity.markDirty();
            daojiTickCounters.remove(machinePos);
            RealRailTransitMod.LOGGER.info("盾构机 {} 已完成挖掘，到达终点", machinePos);
            return;
        }
        
        // 获取当前要挖掘的路径点
        BlockPos currentPathPos = pathPoints.get(progress);
        
        // 如果当前路径点就是盾构机位置，跳过这个点
        if (currentPathPos.equals(machinePos)) {
            RealRailTransitMod.LOGGER.warn("盾构机 {} 当前路径点与盾构机位置相同，跳过此点", machinePos);
            entity.setProgress(progress + 1);
            entity.markDirty();
            return;
        }
        
        // 计算挖掘方向（用于确定隧道横截面）
        Direction forward = Direction.NORTH;
        if (progress < pathPoints.size() - 1) {
            BlockPos nextPos = pathPoints.get(progress + 1);
            forward = getDirection(currentPathPos, nextPos);
        } else if (progress > 0) {
            BlockPos prevPos = pathPoints.get(progress - 1);
            forward = getDirection(prevPos, currentPathPos);
        }
        
        // 根据方向计算偏移
        Direction right = forward.rotateYClockwise();
        Direction up = Direction.UP;
        
        int halfWidth = width / 2;
        int halfHeight = height / 2;
        
        // 挖掘隧道横截面
        for (int w = -halfWidth; w <= halfWidth; w++) {
            for (int h = -halfHeight; h <= halfHeight; h++) {
                BlockPos digPos = currentPathPos;
                if (w != 0) digPos = digPos.offset(right, w);
                if (h != 0) digPos = digPos.offset(up, h);
                
                // 保护盾构机本身：如果挖掘位置是盾构机位置，则跳过
                if (digPos.equals(machinePos)) {
                    continue;
                }
                
                // 检查是否在隧道范围内（圆形或矩形）
                boolean inRange = false;
                if ("circular".equals(tunnelType)) {
                    double dist = Math.sqrt(w * w + h * h);
                    inRange = dist <= Math.min(width, height) / 2.0;
                } else {
                    inRange = Math.abs(w) <= halfWidth && Math.abs(h) <= halfHeight;
                }
                
                if (inRange) {
                    BlockState currentState = world.getBlockState(digPos);
                    // 如果不是空气且可以挖掘，则替换
                    if (!currentState.isAir() && canDig(world, digPos)) {
                        world.setBlockState(digPos, replaceBlock);
                        world.playSound(null, digPos, net.minecraft.sound.SoundEvents.BLOCK_STONE_BREAK, 
                            net.minecraft.sound.SoundCategory.BLOCKS, 0.5f, 1.0f);
                    }
                } else {
                    // 在隧道壁位置放置壁材质
                    BlockState currentState = world.getBlockState(digPos);
                    if (currentState.isAir() || canDig(world, digPos)) {
                        world.setBlockState(digPos, wallBlock);
                    }
                }
            }
        }
        
        // 更新进度
        entity.setProgress(progress + 1);
        entity.markDirty();
        
        RealRailTransitMod.LOGGER.info("盾构机在 {} 挖掘隧道（进度: {}/{}, 位置: {})", 
            machinePos, progress + 1, pathPoints.size(), currentPathPos);
    }
    
    /**
     * 按方向挖掘（旧方式，用于向后兼容）
     */
    private static void digTunnelByDirection(ServerWorld world, BlockPos machinePos, DaojiMachineBlockEntity entity) {
        Direction direction = entity.getBoringDirection();
        int width = entity.getTunnelWidth();
        int height = entity.getTunnelHeight();
        String tunnelType = entity.getTunnelType();
        BlockState replaceBlock = entity.getReplaceBlock();
        BlockState wallBlock = entity.getWallBlock();
        
        // 计算挖掘位置（盾构机前方）
        BlockPos digStart = machinePos.offset(direction);
        
        // 根据方向计算偏移
        Direction right = direction.rotateYClockwise();
        Direction up = Direction.UP;
        
        int halfWidth = width / 2;
        int halfHeight = height / 2;
        
        // 挖掘隧道
        for (int w = -halfWidth; w <= halfWidth; w++) {
            for (int h = -halfHeight; h <= halfHeight; h++) {
                BlockPos digPos = digStart;
                if (w != 0) digPos = digPos.offset(right, w);
                if (h != 0) digPos = digPos.offset(up, h);
                
                // 保护盾构机本身：如果挖掘位置是盾构机位置，则跳过
                if (digPos.equals(machinePos)) {
                    continue;
                }
                
                // 检查是否在隧道范围内（圆形或矩形）
                boolean inRange = false;
                if ("circular".equals(tunnelType)) {
                    double dist = Math.sqrt(w * w + h * h);
                    inRange = dist <= Math.min(width, height) / 2.0;
                } else {
                    inRange = Math.abs(w) <= halfWidth && Math.abs(h) <= halfHeight;
                }
                
                if (inRange) {
                    BlockState currentState = world.getBlockState(digPos);
                    // 如果不是空气且可以挖掘，则替换
                    if (!currentState.isAir() && canDig(world, digPos)) {
                        world.setBlockState(digPos, replaceBlock);
                        world.playSound(null, digPos, net.minecraft.sound.SoundEvents.BLOCK_STONE_BREAK, 
                            net.minecraft.sound.SoundCategory.BLOCKS, 0.5f, 1.0f);
                    }
                } else {
                    // 在隧道壁位置放置壁材质
                    BlockState currentState = world.getBlockState(digPos);
                    if (currentState.isAir() || canDig(world, digPos)) {
                        world.setBlockState(digPos, wallBlock);
                    }
                }
            }
        }
        
        // 更新进度
        entity.setProgress(entity.getProgress() + 1);
        entity.markDirty();
        
        RealRailTransitMod.LOGGER.debug("盾构机在 {} 挖掘隧道（方向: {}）", machinePos, direction);
    }
    
    /**
     * 计算路径点列表
     */
    private static java.util.List<BlockPos> calculatePath(DaojiMachineBlockEntity entity) {
        BlockPos startPos = entity.getStartPos();
        BlockPos endPos = entity.getEndPos();
        
        if (startPos == null || endPos == null) {
            RealRailTransitMod.LOGGER.warn("盾构机路径计算失败：起点或终点为空 (起点: {}, 终点: {})", startPos, endPos);
            return new java.util.ArrayList<>();
        }
        
        String pathType = entity.getPathType();
        java.util.List<BlockPos> pathPoints = new java.util.ArrayList<>();
        
        RealRailTransitMod.LOGGER.info("计算盾构机路径：起点 {}, 终点 {}, 类型 {}", startPos, endPos, pathType);
        
        if ("straight".equals(pathType)) {
            // 直线路径
            pathPoints = calculateStraightPath(startPos, endPos);
        } else if ("curve".equals(pathType)) {
            // 曲线路径
            double radius = entity.getCurveRadius();
            pathPoints = calculateCurvePath(startPos, endPos, radius);
        } else {
            RealRailTransitMod.LOGGER.warn("未知的路径类型: {}，使用直线路径", pathType);
            pathPoints = calculateStraightPath(startPos, endPos);
        }
        
        RealRailTransitMod.LOGGER.info("路径计算完成，共 {} 个路径点", pathPoints.size());
        return pathPoints;
    }
    
    /**
     * 计算直线路径
     */
    private static java.util.List<BlockPos> calculateStraightPath(BlockPos start, BlockPos end) {
        java.util.List<BlockPos> path = new java.util.ArrayList<>();
        
        int dx = end.getX() - start.getX();
        int dy = end.getY() - start.getY();
        int dz = end.getZ() - start.getZ();
        
        int steps = Math.max(Math.max(Math.abs(dx), Math.abs(dy)), Math.abs(dz));
        
        for (int i = 0; i <= steps; i++) {
            double t = steps > 0 ? (double) i / steps : 0;
            int x = (int) Math.round(start.getX() + dx * t);
            int y = (int) Math.round(start.getY() + dy * t);
            int z = (int) Math.round(start.getZ() + dz * t);
            path.add(new BlockPos(x, y, z));
        }
        
        return path;
    }
    
    /**
     * 计算曲线路径（使用贝塞尔曲线）
     */
    private static java.util.List<BlockPos> calculateCurvePath(BlockPos start, BlockPos end, double radius) {
        java.util.List<BlockPos> path = new java.util.ArrayList<>();
        
        // 计算中点作为控制点
        BlockPos mid = new BlockPos(
            (start.getX() + end.getX()) / 2,
            (start.getY() + end.getY()) / 2,
            (start.getZ() + end.getZ()) / 2
        );
        
        // 计算垂直于起点-终点方向的向量
        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();
        double dz = end.getZ() - start.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        if (dist < 0.1) {
            // 起点和终点太近，返回直线路径
            return calculateStraightPath(start, end);
        }
        
        // 归一化方向向量
        dx /= dist;
        dy /= dist;
        dz /= dist;
        
        // 计算垂直向量（使用叉积）
        double perpX, perpY, perpZ;
        if (Math.abs(dy) < 0.9) {
            // 使用 (0,1,0) 作为参考向量
            perpX = -dz;
            perpY = 0;
            perpZ = dx;
        } else {
            // 使用 (1,0,0) 作为参考向量
            perpX = 0;
            perpY = -dz;
            perpZ = dy;
        }
        
        // 归一化垂直向量
        double perpLen = Math.sqrt(perpX * perpX + perpY * perpY + perpZ * perpZ);
        if (perpLen > 0.1) {
            perpX /= perpLen;
            perpY /= perpLen;
            perpZ /= perpLen;
        }
        
        // 计算控制点（中点偏移）
        double offset = Math.min(radius, dist / 2.0);
        BlockPos controlPoint = new BlockPos(
            (int) Math.round(mid.getX() + perpX * offset),
            (int) Math.round(mid.getY() + perpY * offset),
            (int) Math.round(mid.getZ() + perpZ * offset)
        );
        
        // 使用二次贝塞尔曲线生成路径点
        int steps = (int) Math.max(10, dist);
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double x = (1 - t) * (1 - t) * start.getX() + 2 * (1 - t) * t * controlPoint.getX() + t * t * end.getX();
            double y = (1 - t) * (1 - t) * start.getY() + 2 * (1 - t) * t * controlPoint.getY() + t * t * end.getY();
            double z = (1 - t) * (1 - t) * start.getZ() + 2 * (1 - t) * t * controlPoint.getZ() + t * t * end.getZ();
            path.add(new BlockPos((int) Math.round(x), (int) Math.round(y), (int) Math.round(z)));
        }
        
        return path;
    }
    
    /**
     * 获取从起点到终点的方向
     */
    private static Direction getDirection(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();
        
        // 找到最大的变化量
        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);
        int absDz = Math.abs(dz);
        
        if (absDx >= absDy && absDx >= absDz) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        } else if (absDy >= absDx && absDy >= absDz) {
            return dy > 0 ? Direction.UP : Direction.DOWN;
        } else {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }
    
    private static boolean canDig(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        // 可以挖掘大部分方块（除了基岩等）
        return state.getHardness(world, pos) >= 0 && !state.isAir();
    }
    
    /**
     * 通知盾构机开始工作（从外部调用）
     */
    public static void notifyDaojiStarted(ServerWorld world, BlockPos pos) {
        // 验证区块是否加载
        if (!world.isChunkLoaded(pos)) {
            RealRailTransitMod.LOGGER.warn("盾构机 {} 的区块未加载，无法启动", pos);
            return;
        }
        
        // 验证方块实体是否存在且正在工作
        var blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof DaojiMachineBlockEntity entity) {
            if (!entity.isWorking()) {
                RealRailTransitMod.LOGGER.warn("盾构机 {} 的工作状态为 false，无法启动", pos);
                return;
            }
            RealRailTransitMod.LOGGER.info("盾构机 {} 已注册到挖掘队列（工作状态: {}, 起点: {}, 终点: {}）", 
                pos, entity.isWorking(), entity.getStartPos(), entity.getEndPos());
        } else {
            RealRailTransitMod.LOGGER.warn("盾构机 {} 的方块实体不存在，无法启动", pos);
            return;
        }
        
        daojiTickCounters.put(pos, DIG_INTERVAL - 1); // 设置为即将挖掘，下一tick就开始
    }
    
    /**
     * 通知盾构机停止工作（从外部调用）
     */
    public static void notifyDaojiStopped(BlockPos pos) {
        daojiTickCounters.remove(pos);
    }
}

