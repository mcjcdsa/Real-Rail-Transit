package com.real.rail.transit.station;

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
 * 车站建设控制面板方块
 * 用于管理车站建设相关设置
 */
public class StationConstructionControlPanelBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 2.0);
    
    public StationConstructionControlPanelBlock(Settings settings) {
        super(settings);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            // 显示车站建设选项
            player.sendMessage(Text.translatable("block.real-rail-transit-mod.station_construction_control_panel.info"), false);
            RealRailTransitMod.LOGGER.info("车站建设控制面板 {} 被使用", pos);
            
            // TODO: 打开车站建设GUI，包含：
            // - 车站设施选择
            // - 屏蔽门配置
            // - 显示屏设置
            // - 批量操作选项
        }
        return ActionResult.SUCCESS;
    }
}

