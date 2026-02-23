package com.real.rail.transit.block;

import com.real.rail.transit.system.PowerSystem;
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
 * 线缆方块
 * 用于传输电力和信号
 */
public class CableBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 2.0, 10.0);
    
    public CableBlock(Settings settings) {
        super(settings);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        // 注册到电力系统
        PowerSystem.getInstance().registerCable(world, pos);
    }
    
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        // 从电力系统移除
        PowerSystem.getInstance().unregisterCable(pos);
    }
    
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            // 显示线缆连接信息
            PowerSystem powerSystem = PowerSystem.getInstance();
            boolean connected = powerSystem.isCableConnected(pos);
            player.sendMessage(Text.translatable("block.real-rail-transit-mod.cable.info",
                pos.getX(), pos.getY(), pos.getZ(), connected ? "已连接" : "未连接"), false);
        }
        return ActionResult.SUCCESS;
    }
}

