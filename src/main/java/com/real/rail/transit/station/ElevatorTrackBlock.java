package com.real.rail.transit.station;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

/**
 * 电梯轨道方块
 * 电梯运行的轨道
 */
public class ElevatorTrackBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    
    // 存储电梯轨道网络
    private static final Set<BlockPos> elevatorTracks = new HashSet<>();
    
    public ElevatorTrackBlock(Settings settings) {
        super(settings);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        // 注册电梯轨道
        elevatorTracks.add(pos);
    }
    
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        // 移除电梯轨道
        elevatorTracks.remove(pos);
    }
    
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            // 检查是否连接到电梯
            boolean connected = isConnectedToElevator(world, pos);
            player.sendMessage(Text.translatable("block.real-rail-transit-mod.elevator_track.info",
                pos.getX(), pos.getY(), pos.getZ(), connected ? "已连接" : "未连接"), false);
        }
        return ActionResult.SUCCESS;
    }
    
    /**
     * 检查是否连接到电梯
     */
    private boolean isConnectedToElevator(World world, BlockPos pos) {
        // 检查上下方向是否有电梯方块
        BlockPos upPos = pos.up();
        BlockPos downPos = pos.down();
        
        return world.getBlockState(upPos).getBlock() instanceof ElevatorBlock ||
               world.getBlockState(downPos).getBlock() instanceof ElevatorBlock;
    }
    
    /**
     * 获取连接的电梯轨道数量
     */
    public static int getConnectedTrackCount(BlockPos pos) {
        int count = 0;
        for (BlockPos trackPos : elevatorTracks) {
            if (trackPos.isWithinDistance(pos, 1.0)) {
                count++;
            }
        }
        return count;
    }
}

