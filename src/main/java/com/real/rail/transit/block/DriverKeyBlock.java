package com.real.rail.transit.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 司机钥匙方块
 * 此方块不可放置，仅作为物品使用
 */
public class DriverKeyBlock extends Block {
    
    public DriverKeyBlock(Settings settings) {
        super(settings);
    }
    
    /**
     * 阻止方块被放置
     * 重写此方法以确保方块无法被放置在世界中
     */
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        // 此方块不可放置，所以这个方法不应该被调用
        // 但如果被调用，返回PASS让其他逻辑处理
        return ActionResult.PASS;
    }
}

