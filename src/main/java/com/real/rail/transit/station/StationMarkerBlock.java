package com.real.rail.transit.station;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
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
 * 站点标记方块
 * 用于标记车站位置，存储站点 ID/名称，供调度/到站显示等系统使用。
 * 站点 ID 存储在方块实体中（1.20.6 已移除 StringProperty）。
 */
public class StationMarkerBlock extends Block {

    private static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public StationMarkerBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new com.real.rail.transit.station.entity.StationMarkerBlockEntity(pos, state);
    }

    public boolean hasBlockEntity() {
        return true;
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
                              Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof com.real.rail.transit.station.entity.StationMarkerBlockEntity marker) {
                String id = marker.getStationId();
                String name = marker.getStationName();
                player.sendMessage(
                    Text.translatable("block.real-rail-transit-mod.station_marker.info",
                        id,
                        name.isEmpty() ? Text.translatable("block.real-rail-transit-mod.station_marker.no_name") : name,
                        pos.getX(), pos.getY(), pos.getZ()
                    ),
                    false
                );
            }
        }
        return ActionResult.SUCCESS;
    }
}
