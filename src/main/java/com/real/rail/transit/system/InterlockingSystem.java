package com.real.rail.transit.system;

import com.real.rail.transit.block.SignalBlock;
import com.real.rail.transit.block.TurnoutBlock;
import com.real.rail.transit.track.TrackNetwork;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

/**
 * 联锁系统
 * 实现信号机与道岔之间的联锁逻辑，确保列车运行安全
 */
public class InterlockingSystem {
    private static final InterlockingSystem INSTANCE = new InterlockingSystem();
    
    /**
     * 进路：从起点到终点的路径
     */
    public static class Route {
        private BlockPos startSignal;      // 起始信号机
        private BlockPos endSignal;        // 结束信号机
        private List<BlockPos> trackPath;  // 轨道路径
        private List<BlockPos> turnouts;   // 路径上的道岔
        private boolean locked;             // 是否锁定
        
        public Route(BlockPos startSignal, BlockPos endSignal) {
            this.startSignal = startSignal;
            this.endSignal = endSignal;
            this.trackPath = new ArrayList<>();
            this.turnouts = new ArrayList<>();
            this.locked = false;
        }
        
        // Getters and Setters
        public BlockPos getStartSignal() { return startSignal; }
        public BlockPos getEndSignal() { return endSignal; }
        public List<BlockPos> getTrackPath() { return trackPath; }
        public void setTrackPath(List<BlockPos> trackPath) { this.trackPath = trackPath; }
        public List<BlockPos> getTurnouts() { return turnouts; }
        public void setTurnouts(List<BlockPos> turnouts) { this.turnouts = turnouts; }
        public boolean isLocked() { return locked; }
        public void setLocked(boolean locked) { this.locked = locked; }
    }
    
    // 存储所有进路
    private final Map<String, Route> routes = new HashMap<>();
    
    // 存储道岔与信号机的关联关系
    private final Map<BlockPos, Set<BlockPos>> turnoutSignalMap = new HashMap<>();
    
    // 存储信号机与进路的关联关系
    private final Map<BlockPos, Route> signalRouteMap = new HashMap<>();
    
    private InterlockingSystem() {
    }
    
    public static InterlockingSystem getInstance() {
        return INSTANCE;
    }
    
    /**
     * 排列进路
     * 根据起点和终点信号机排列进路，并锁定相关道岔
     */
    public boolean setRoute(World world, BlockPos startSignal, BlockPos endSignal) {
        // 检查起点和终点信号机是否存在
        if (!(world.getBlockState(startSignal).getBlock() instanceof SignalBlock) ||
            !(world.getBlockState(endSignal).getBlock() instanceof SignalBlock)) {
            if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                com.real.rail.transit.util.ModRuntimeLog.warn(
                    "进路排列失败：起点或终点不是信号机 start=" + startSignal + ", end=" + endSignal,
                    serverWorld
                );
            }
            return false;
        }
        
        // 查找路径
        List<BlockPos> path = TrackNetwork.getInstance().findPath(world, startSignal, endSignal);
        if (path.isEmpty()) {
            if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                com.real.rail.transit.util.ModRuntimeLog.warn(
                    "进路排列失败：找不到从 " + startSignal + " 到 " + endSignal + " 的轨道路径",
                    serverWorld
                );
            }
            return false; // 无法找到路径
        }
        
        // 检查路径上的道岔
        List<BlockPos> turnouts = new ArrayList<>();
        for (BlockPos pos : path) {
            if (world.getBlockState(pos).getBlock() instanceof TurnoutBlock) {
                turnouts.add(pos);
            }
        }
        
        // 检查道岔是否可以锁定
        for (BlockPos turnoutPos : turnouts) {
            if (!canLockTurnout(world, turnoutPos)) {
                if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                    com.real.rail.transit.util.ModRuntimeLog.warn(
                        "进路排列失败：道岔 " + turnoutPos + " 已被锁定或不可用",
                        serverWorld
                    );
                }
                return false; // 道岔无法锁定（可能已被其他进路占用）
            }
        }
        
        // 创建进路
        String routeId = generateRouteId(startSignal, endSignal);
        Route route = new Route(startSignal, endSignal);
        route.setTrackPath(path);
        route.setTurnouts(turnouts);
        
        // 锁定道岔
        for (BlockPos turnoutPos : turnouts) {
            lockTurnout(world, turnoutPos);
        }
        route.setLocked(true);
        
        // 设置信号机为绿灯
        SignalSystem.getInstance().setSignalState(world, startSignal, SignalBlock.SignalState.GREEN);
        
        // 保存进路
        routes.put(routeId, route);
        signalRouteMap.put(startSignal, route);
        
        if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            com.real.rail.transit.util.ModRuntimeLog.info(
                "进路排列成功：" + routeId + "，道岔数量=" + turnouts.size(),
                serverWorld
            );
        }
        
        return true;
    }
    
    /**
     * 取消进路
     */
    public void cancelRoute(World world, String routeId) {
        Route route = routes.remove(routeId);
        if (route == null) {
            return;
        }
        
        // 解锁道岔
        for (BlockPos turnoutPos : route.getTurnouts()) {
            unlockTurnout(world, turnoutPos);
        }
        route.setLocked(false);
        
        // 设置信号机为红灯
        SignalSystem.getInstance().setSignalState(world, route.getStartSignal(), SignalBlock.SignalState.RED);
        
        // 移除关联
        signalRouteMap.remove(route.getStartSignal());
        
        if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            com.real.rail.transit.util.ModRuntimeLog.info(
                "进路已取消：" + routeId,
                serverWorld
            );
        }
    }
    
    /**
     * 检查道岔是否可以锁定
     */
    private boolean canLockTurnout(World world, BlockPos turnoutPos) {
        BlockState state = world.getBlockState(turnoutPos);
        if (!(state.getBlock() instanceof TurnoutBlock)) {
            return false;
        }
        
        // 检查道岔是否已被锁定
        return !state.get(TurnoutBlock.IS_LOCKED);
    }
    
    /**
     * 锁定道岔
     */
    private void lockTurnout(World world, BlockPos turnoutPos) {
        BlockState state = world.getBlockState(turnoutPos);
        if (state.getBlock() instanceof TurnoutBlock) {
            world.setBlockState(turnoutPos, state.with(TurnoutBlock.IS_LOCKED, true));
        }
    }
    
    /**
     * 解锁道岔
     */
    private void unlockTurnout(World world, BlockPos turnoutPos) {
        BlockState state = world.getBlockState(turnoutPos);
        if (state.getBlock() instanceof TurnoutBlock) {
            world.setBlockState(turnoutPos, state.with(TurnoutBlock.IS_LOCKED, false));
        }
    }
    
    /**
     * 生成进路ID
     */
    private String generateRouteId(BlockPos start, BlockPos end) {
        return start.getX() + "," + start.getY() + "," + start.getZ() + 
               "->" + end.getX() + "," + end.getY() + "," + end.getZ();
    }
    
    /**
     * 检查信号机是否可以开放
     */
    public boolean canSignalOpen(World world, BlockPos signalPos) {
        // 检查是否有进路占用
        Route route = signalRouteMap.get(signalPos);
        if (route != null && route.isLocked()) {
            return false; // 进路已被占用
        }
        
        // 检查前方轨道是否被占用
        // 简化实现：在信号机前方一定范围内查找列车实体
        int checkDistance = 10;
        net.minecraft.util.math.Box box = new net.minecraft.util.math.Box(
            signalPos.getX() - 1, signalPos.getY() - 2, signalPos.getZ() - 1,
            signalPos.getX() + checkDistance, signalPos.getY() + 2, signalPos.getZ() + 1
        );
        
        List<com.real.rail.transit.entity.TrainEntity> trains = world.getEntitiesByType(
            com.real.rail.transit.registry.ModEntities.TRAIN,
            box,
            entity -> true
        );
        
        if (!trains.isEmpty()) {
            return false; // 前方区段存在列车
        }
        
        return true;
    }
}

