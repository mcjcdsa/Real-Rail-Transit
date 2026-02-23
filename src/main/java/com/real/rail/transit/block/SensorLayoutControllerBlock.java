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
 * 传感器铺设器方块
 * 用于批量铺设传感器
 */
public class SensorLayoutControllerBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 8.0, 14.0);
    
    public SensorLayoutControllerBlock(Settings settings) {
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
            
            // 如果目标是轨道，则在轨道位置放置传感器
            if (targetState.getBlock() instanceof TrackBlock || targetState.getBlock() == ModBlocks.TRACK) {
                // 替换轨道为传感器（传感器可以放在轨道位置）
                world.setBlockState(targetPos, ModBlocks.SENSOR_CONTROLLER.getDefaultState());
                player.sendMessage(Text.translatable("block.real-rail-transit-mod.sensor_layout_controller.placed",
                    targetPos.getX(), targetPos.getY(), targetPos.getZ()), false);
                RealRailTransitMod.LOGGER.info("传感器铺设器 {} 在 {} 放置了传感器", pos, targetPos);
                return ActionResult.SUCCESS;
            } else if (targetState.isAir()) {
                // 如果目标是空气，直接放置传感器
                world.setBlockState(targetPos, ModBlocks.SENSOR_CONTROLLER.getDefaultState());
                player.sendMessage(Text.translatable("block.real-rail-transit-mod.sensor_layout_controller.placed",
                    targetPos.getX(), targetPos.getY(), targetPos.getZ()), false);
                return ActionResult.SUCCESS;
            } else {
                player.sendMessage(Text.translatable("block.real-rail-transit-mod.sensor_layout_controller.blocked"), false);
            }
        }
        return ActionResult.SUCCESS;
    }
}

