package com.real.rail.transit.station;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * 到站显示屏方块
 * 列车到站时间实时显示屏幕，搭载 API 接口
 */
public class ArrivalDisplayScreenBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 2.0);
    
    /**
     * 预计到站时间（秒）
     */
    public static final IntProperty ARRIVAL_TIME = IntProperty.of("arrival_time", 0, 3600);
    
    /**
     * 站台号
     */
    public static final IntProperty PLATFORM = IntProperty.of("platform", 1, 10);
    
    public ArrivalDisplayScreenBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
            .with(ARRIVAL_TIME, 0)
            .with(PLATFORM, 1));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ARRIVAL_TIME, PLATFORM);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    /**
     * 更新到站信息
     * Note: trainId and destination are stored in BlockEntity (to be implemented)
     */
    public void updateArrivalInfo(World world, BlockPos pos, String trainId, String destination, int arrivalTime, int platform) {
        BlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state
            .with(ARRIVAL_TIME, arrivalTime)
            .with(PLATFORM, platform));
        // TODO: Store trainId and destination in BlockEntity
    }
    
    public net.minecraft.util.ActionResult onUse(net.minecraft.block.BlockState state, World world, BlockPos pos, 
                                                 net.minecraft.entity.player.PlayerEntity player, 
                                                 net.minecraft.util.Hand hand, net.minecraft.util.hit.BlockHitResult hit) {
        if (world.isClient) {
            // 客户端打开配置界面
            // TODO: 打开到站显示屏配置界面
            com.real.rail.transit.RealRailTransitMod.LOGGER.info("配置到站显示屏: {}", pos);
        }
        return net.minecraft.util.ActionResult.SUCCESS;
    }
    
    /**
     * 获取下一班列车的预计到站时间（API接口）
     */
    public int getNextArrivalTime(BlockState state) {
        return state.get(ARRIVAL_TIME);
    }
    
    /**
     * 获取列车车次号（API接口）
     * TODO: Retrieve from BlockEntity
     */
    public String getTrainId(BlockState state) {
        return ""; // TODO: Get from BlockEntity
    }
    
    /**
     * 获取终点站信息（API接口）
     * TODO: Retrieve from BlockEntity
     */
    public String getDestination(BlockState state) {
        return ""; // TODO: Get from BlockEntity
    }
    
    /**
     * 获取站台号（API接口）
     */
    public int getPlatform(BlockState state) {
        return state.get(PLATFORM);
    }
}

