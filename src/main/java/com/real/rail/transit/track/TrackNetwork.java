package com.real.rail.transit.track;

import com.real.rail.transit.block.TrackBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

/**
 * 轨道网络系统
 * 管理轨道的连接关系和路径查找
 */
public class TrackNetwork {
    private static final TrackNetwork INSTANCE = new TrackNetwork();
    
    /**
     * 轨道连接关系：存储每个轨道方块连接的相邻轨道位置
     */
    public final Map<BlockPos, Set<BlockPos>> trackConnections = new HashMap<>();
    
    /**
     * 轨道段：存储连续的轨道段
     */
    private final Map<BlockPos, TrackSegment> trackSegments = new HashMap<>();
    
    private TrackNetwork() {
    }
    
    public static TrackNetwork getInstance() {
        return INSTANCE;
    }
    
    /**
     * 注册轨道方块
     */
    public void registerTrack(World world, BlockPos pos) {
        if (!trackConnections.containsKey(pos)) {
            trackConnections.put(pos, new HashSet<>());
            updateConnections(world, pos);
        }
    }
    
    /**
     * 移除轨道方块
     */
    public void unregisterTrack(BlockPos pos) {
        Set<BlockPos> connections = trackConnections.remove(pos);
        if (connections != null) {
            // 更新相邻轨道的连接关系
            for (BlockPos connected : connections) {
                Set<BlockPos> connectedSet = trackConnections.get(connected);
                if (connectedSet != null) {
                    connectedSet.remove(pos);
                }
            }
        }
        trackSegments.remove(pos);
    }
    
    /**
     * 更新轨道的连接关系
     */
    private void updateConnections(World world, BlockPos pos) {
        Set<BlockPos> connections = trackConnections.get(pos);
        if (connections == null) {
            return;
        }
        
        connections.clear();
        
        // 检查六个方向的相邻方块
        BlockPos[] directions = {
            pos.north(),
            pos.south(),
            pos.east(),
            pos.west(),
            pos.up(),
            pos.down()
        };
        
        for (BlockPos neighbor : directions) {
            if (world.getBlockState(neighbor).getBlock() instanceof TrackBlock) {
                connections.add(neighbor);
                
                // 确保双向连接
                Set<BlockPos> neighborConnections = trackConnections.computeIfAbsent(neighbor, k -> new HashSet<>());
                neighborConnections.add(pos);
            }
        }
    }
    
    /**
     * 查找从起点到终点的路径
     * 使用A*算法
     */
    public List<BlockPos> findPath(World world, BlockPos start, BlockPos end) {
        if (!trackConnections.containsKey(start) || !trackConnections.containsKey(end)) {
            return Collections.emptyList();
        }
        
        // A*算法实现
        PriorityQueue<PathNode> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fCost));
        Map<BlockPos, PathNode> allNodes = new HashMap<>();
        
        PathNode startNode = new PathNode(start, null, 0, heuristic(start, end));
        openSet.add(startNode);
        allNodes.put(start, startNode);
        
        Set<BlockPos> closedSet = new HashSet<>();
        
        while (!openSet.isEmpty()) {
            PathNode current = openSet.poll();
            
            if (current.pos.equals(end)) {
                // 找到路径，回溯
                List<BlockPos> path = new ArrayList<>();
                PathNode node = current;
                while (node != null) {
                    path.add(node.pos);
                    node = node.parent;
                }
                Collections.reverse(path);
                return path;
            }
            
            closedSet.add(current.pos);
            
            Set<BlockPos> neighbors = trackConnections.get(current.pos);
            if (neighbors == null) {
                continue;
            }
            
            for (BlockPos neighbor : neighbors) {
                if (closedSet.contains(neighbor)) {
                    continue;
                }
                
                double gCost = current.gCost + current.pos.getManhattanDistance(neighbor);
                double hCost = heuristic(neighbor, end);
                double fCost = gCost + hCost;
                
                PathNode neighborNode = allNodes.get(neighbor);
                if (neighborNode == null) {
                    neighborNode = new PathNode(neighbor, current, gCost, hCost);
                    allNodes.put(neighbor, neighborNode);
                    openSet.add(neighborNode);
                } else if (gCost < neighborNode.gCost) {
                    neighborNode.gCost = gCost;
                    neighborNode.fCost = fCost;
                    neighborNode.parent = current;
                    openSet.remove(neighborNode);
                    openSet.add(neighborNode);
                }
            }
        }
        
        return Collections.emptyList(); // 未找到路径
    }
    
    /**
     * 启发式函数（曼哈顿距离）
     */
    private double heuristic(BlockPos a, BlockPos b) {
        return a.getManhattanDistance(b);
    }
    
    /**
     * 路径节点（用于A*算法）
     */
    private static class PathNode {
        BlockPos pos;
        PathNode parent;
        double gCost; // 从起点到当前节点的实际代价
        double hCost; // 从当前节点到终点的估计代价
        double fCost; // fCost = gCost + hCost
        
        PathNode(BlockPos pos, PathNode parent, double gCost, double hCost) {
            this.pos = pos;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
        }
    }
    
    /**
     * 轨道段
     */
    public static class TrackSegment {
        private final Set<BlockPos> positions = new HashSet<>();
        private BlockPos start;
        private BlockPos end;
        
        public void addPosition(BlockPos pos) {
            positions.add(pos);
        }
        
        public Set<BlockPos> getPositions() {
            return positions;
        }
        
        public BlockPos getStart() {
            return start;
        }
        
        public void setStart(BlockPos start) {
            this.start = start;
        }
        
        public BlockPos getEnd() {
            return end;
        }
        
        public void setEnd(BlockPos end) {
            this.end = end;
        }
    }
}

