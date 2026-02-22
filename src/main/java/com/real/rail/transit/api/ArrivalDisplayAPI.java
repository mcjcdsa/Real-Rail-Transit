package com.real.rail.transit.api;

import com.real.rail.transit.station.ArrivalDisplayScreenBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 到站显示屏 API
 * 提供RESTful风格的API接口，允许其他模组或插件获取列车到站信息
 */
public class ArrivalDisplayAPI {
    private static final ArrivalDisplayAPI INSTANCE = new ArrivalDisplayAPI();
    
    private ArrivalDisplayAPI() {
    }
    
    public static ArrivalDisplayAPI getInstance() {
        return INSTANCE;
    }
    
    /**
     * 获取下一班列车的预计到站时间（秒）
     * 
     * @param world 世界对象
     * @param pos 到站显示屏位置
     * @return 预计到站时间（秒），如果不存在则返回-1
     */
    public int getNextArrivalTime(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof ArrivalDisplayScreenBlock) {
            return ((ArrivalDisplayScreenBlock) state.getBlock()).getNextArrivalTime(state);
        }
        return -1;
    }
    
    /**
     * 获取列车车次号
     * 
     * @param world 世界对象
     * @param pos 到站显示屏位置
     * @return 车次号，如果不存在则返回空字符串
     */
    public String getTrainId(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof ArrivalDisplayScreenBlock) {
            return ((ArrivalDisplayScreenBlock) state.getBlock()).getTrainId(state);
        }
        return "";
    }
    
    /**
     * 获取终点站信息
     * 
     * @param world 世界对象
     * @param pos 到站显示屏位置
     * @return 终点站名称，如果不存在则返回空字符串
     */
    public String getDestination(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof ArrivalDisplayScreenBlock) {
            return ((ArrivalDisplayScreenBlock) state.getBlock()).getDestination(state);
        }
        return "";
    }
    
    /**
     * 获取站台号
     * 
     * @param world 世界对象
     * @param pos 到站显示屏位置
     * @return 站台号，如果不存在则返回-1
     */
    public int getPlatform(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof ArrivalDisplayScreenBlock) {
            return ((ArrivalDisplayScreenBlock) state.getBlock()).getPlatform(state);
        }
        return -1;
    }
    
    /**
     * 获取列车运行状态
     * 
     * @param world 世界对象
     * @param pos 到站显示屏位置
     * @return 运行状态（"正常"/"延误"等），如果不存在则返回空字符串
     */
    public String getTrainStatus(World world, BlockPos pos) {
        // TODO: 实现运行状态获取逻辑
        // 可以根据到站时间与实际时间的差值判断是否延误
        return "正常";
    }
}

