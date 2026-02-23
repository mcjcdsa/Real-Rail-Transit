package com.real.rail.transit.block;

import com.real.rail.transit.RealRailTransitMod;
import com.real.rail.transit.registry.ModBlocks;
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
 * 信号铺设器方块
 * 用于批量铺设信号机
 */
public class SignalLayoutControllerBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 8.0, 14.0);
    
    public SignalLayoutControllerBlock(Settings settings) {
        super(settings);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            // 获取目标位置（铺设器前方的方块）
            BlockPos targetPos = pos.offset(hit.getSide());
            BlockState targetState = world.getBlockState(targetPos);
            
            // 如果目标是轨道，则在轨道上铺设信号机
            if (targetState.getBlock() instanceof TrackBlock || targetState.getBlock() == ModBlocks.TRACK) {
                // 在轨道上方放置信号机
                BlockPos signalPos = targetPos.up();
                if (world.getBlockState(signalPos).isAir()) {
                    world.setBlockState(signalPos, ModBlocks.SIGNAL.getDefaultState());
                    player.sendMessage(Text.translatable("block.real-rail-transit-mod.signal_layout_controller.placed",
                        signalPos.getX(), signalPos.getY(), signalPos.getZ()), false);
                    RealRailTransitMod.LOGGER.info("信号铺设器 {} 在 {} 放置了信号机", pos, signalPos);
                    return ActionResult.SUCCESS;
                } else {
                    player.sendMessage(Text.translatable("block.real-rail-transit-mod.signal_layout_controller.blocked"), false);
                }
            } else {
                player.sendMessage(Text.translatable("block.real-rail-transit-mod.signal_layout_controller.not_track"), false);
            }
        }
        return ActionResult.SUCCESS;
    }
}

