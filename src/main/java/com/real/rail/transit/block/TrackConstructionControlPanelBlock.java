package com.real.rail.transit.block;

import com.real.rail.transit.RealRailTransitMod;
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
 * 线路建设控制面板方块
 * 用于管理线路建设相关设置
 */
public class TrackConstructionControlPanelBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 2.0);
    
    public TrackConstructionControlPanelBlock(Settings settings) {
        super(settings);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            // 显示线路建设选项
            player.sendMessage(Text.translatable("block.real-rail-transit-mod.track_construction_control_panel.info"), false);
            RealRailTransitMod.LOGGER.info("线路建设控制面板 {} 被使用", pos);
            
            // TODO: 打开线路建设GUI，包含：
            // - 轨道类型选择
            // - 供电方式选择
            // - 信号机配置
            // - 批量操作选项
        }
        return ActionResult.SUCCESS;
    }
}

