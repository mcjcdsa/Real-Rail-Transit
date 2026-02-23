package com.real.rail.transit.block;

import com.real.rail.transit.RealRailTransitMod;
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
 * 线路控制面板方块
 * 用于控制线路运行
 */
public class TrackControlPanelBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 2.0);
    
    public TrackControlPanelBlock(Settings settings) {
        super(settings);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            // 显示线路统计信息
            TrackNetwork network = TrackNetwork.getInstance();
            int trackCount = network.trackConnections.size();
            
            // TODO: 获取信号机数量
            
            player.sendMessage(Text.translatable("block.real-rail-transit-mod.track_control_panel.info",
                trackCount), false);
            RealRailTransitMod.LOGGER.info("线路控制面板 {} 显示信息: 轨道数={}", pos, trackCount);
            
            // TODO: 打开线路控制GUI
        }
        return ActionResult.SUCCESS;
    }
}

