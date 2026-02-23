package com.real.rail.transit.block;

import com.real.rail.transit.RealRailTransitMod;
import com.real.rail.transit.preset.PresetManager;
import com.real.rail.transit.registry.ModBlocks;
import com.real.rail.transit.track.TrackNetwork;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * 轨道方块
 * 基础轨道方块，支持直线、曲线、坡道铺设
 */
public class TrackBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    
    public TrackBlock(Settings settings) {
        super(settings);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        // 注册到轨道网络
        TrackNetwork.getInstance().registerTrack(world, pos);
    }
    
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        // 从轨道网络移除
        TrackNetwork.getInstance().unregisterTrack(pos);
    }
    
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            // 检查玩家是否拿着轨道方块，并且点击的是预设器路径上的位置
            ItemStack heldItem = player.getStackInHand(hand);
            if (heldItem.getItem() == net.minecraft.item.Items.AIR) {
                heldItem = player.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
            }
            
            // 检查是否是轨道方块物品
            boolean isTrackItem = heldItem.getItem() instanceof net.minecraft.item.BlockItem blockItem &&
                                 blockItem.getBlock() == ModBlocks.TRACK;
            
            if (isTrackItem) {
                PresetManager presetManager = PresetManager.getInstance();
                PresetManager.PresetData data = presetManager.getPresetData(player.getUuid());
                
                // 检查点击的位置是否在预设器路径上
                if (data != null && data.isOnPath(pos)) {
                    // 在预设器路径上，自动放置轨道
                    BlockState currentState = world.getBlockState(pos);
                    if (currentState.isAir() || currentState.getBlock() != ModBlocks.TRACK) {
                        // 放置轨道
                        world.setBlockState(pos, ModBlocks.TRACK.getDefaultState());
                        
                        // 消耗物品
                        if (!player.getAbilities().creativeMode) {
                            heldItem.decrement(1);
                        }
                        
                        player.sendMessage(Text.translatable("block.real-rail-transit-mod.track.placed_on_preset",
                            pos.getX(), pos.getY(), pos.getZ()), false);
                        RealRailTransitMod.LOGGER.info("在预设器路径 {} 放置轨道", pos);
                        return ActionResult.SUCCESS;
                    }
                }
            }
            
            // 显示轨道信息
            TrackNetwork network = TrackNetwork.getInstance();
            var connections = network.trackConnections.get(pos);
            
            if (connections != null && !connections.isEmpty()) {
                player.sendMessage(Text.translatable("block.real-rail-transit-mod.track.info", 
                    pos.getX(), pos.getY(), pos.getZ(), connections.size()), false);
                RealRailTransitMod.LOGGER.info("轨道位置: {}, 连接数: {}", pos, connections.size());
            } else {
                player.sendMessage(Text.translatable("block.real-rail-transit-mod.track.info_no_connections", 
                    pos.getX(), pos.getY(), pos.getZ()), false);
            }
        }
        return ActionResult.SUCCESS;
    }
}
