package com.real.rail.transit.block;

import com.real.rail.transit.RealRailTransitMod;
import com.real.rail.transit.registry.ModBlocks;
import com.real.rail.transit.track.TrackNetwork;
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

/**
 * 清除器方块
 * 用于清除已放置的轨道和设施
 */
public class CleanerBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(4.0, 0.0, 4.0, 12.0, 8.0, 12.0);
    
    public CleanerBlock(Settings settings) {
        super(settings);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            // 获取目标方块位置（清除器前方的方块）
            BlockPos targetPos = pos.offset(hit.getSide());
            BlockState targetState = world.getBlockState(targetPos);
            
            // 检查是否是轨道相关方块
            if (isTrackRelatedBlock(targetState.getBlock())) {
                // 从轨道网络移除
                TrackNetwork.getInstance().unregisterTrack(targetPos);
                
                // 清除方块
                world.breakBlock(targetPos, false);
                
                player.sendMessage(Text.translatable("block.real-rail-transit-mod.cleaner.cleared",
                    targetPos.getX(), targetPos.getY(), targetPos.getZ()), false);
                RealRailTransitMod.LOGGER.info("清除器 {} 清除了方块: {}", pos, targetPos);
                
                return ActionResult.SUCCESS;
            } else {
                player.sendMessage(Text.translatable("block.real-rail-transit-mod.cleaner.not_track"), false);
            }
        }
        return ActionResult.SUCCESS;
    }
    
    /**
     * 检查是否是轨道相关方块
     */
    private boolean isTrackRelatedBlock(net.minecraft.block.Block block) {
        return block instanceof TrackBlock ||
               block instanceof TurnoutBlock ||
               block instanceof SwitchBlock ||
               block instanceof ConnectionPartBlock ||
               block instanceof GuardTrackBlock ||
               block instanceof ThirdRailBlock ||
               block instanceof ContactNetworkBlock ||
               block instanceof SignalBlock ||
               block instanceof SensorControllerBlock ||
               block == ModBlocks.TRACK ||
               block == ModBlocks.TURNOUT ||
               block == ModBlocks.SWITCH ||
               block == ModBlocks.CONNECTION_PART ||
               block == ModBlocks.GUARD_TRACK ||
               block == ModBlocks.THIRD_RAIL ||
               block == ModBlocks.CONTACT_NETWORK ||
               block == ModBlocks.SIGNAL ||
               block == ModBlocks.SENSOR_CONTROLLER;
    }
}

