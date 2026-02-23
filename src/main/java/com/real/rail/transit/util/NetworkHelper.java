package com.real.rail.transit.util;

import com.real.rail.transit.track.TrackNetwork;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * 网络辅助工具类
 * 提供轨道网络相关的实用方法
 */
public class NetworkHelper {
    /**
     * 检查两个位置是否通过轨道连接
     */
    public static boolean areConnected(World world, BlockPos pos1, BlockPos pos2) {
        TrackNetwork network = TrackNetwork.getInstance();
        List<BlockPos> path = network.findPath(world, pos1, pos2);
        return !path.isEmpty();
    }
    
    /**
     * 获取两个位置之间的最短距离（轨道距离）
     */
    public static int getTrackDistance(World world, BlockPos pos1, BlockPos pos2) {
        TrackNetwork network = TrackNetwork.getInstance();
        List<BlockPos> path = network.findPath(world, pos1, pos2);
        return path.size();
    }
    
    /**
     * 检查位置是否在轨道网络上
     */
    public static boolean isOnTrack(BlockPos pos) {
        TrackNetwork network = TrackNetwork.getInstance();
        return network.trackConnections.containsKey(pos);
    }
}




