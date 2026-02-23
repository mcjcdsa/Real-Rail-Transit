package com.real.rail.transit.item;

import com.real.rail.transit.RealRailTransitMod;
import com.real.rail.transit.block.TrackBlock;
import com.real.rail.transit.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 线路刷子
 * 批量修改轨道样式、属性、标识
 */
public class TrackBrushItem extends Item {
    private BlockPos firstPos = null;
    
    public TrackBrushItem(Settings settings) {
        super(settings);
    }
    
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        
        if (world.isClient || player == null) {
            return ActionResult.PASS;
        }
        
        BlockState state = world.getBlockState(pos);
        
        // 如果点击的是轨道，开始选择区域
        if (state.getBlock() instanceof TrackBlock) {
            if (player.isSneaking()) {
                // 按住Shift点击：清除选择
                firstPos = null;
                player.sendMessage(Text.translatable("item.real-rail-transit-mod.track_brush.cleared"), false);
                return ActionResult.SUCCESS;
            } else {
                // 普通点击：设置第一个点或执行批量操作
                if (firstPos == null) {
                    firstPos = pos;
                    player.sendMessage(Text.translatable("item.real-rail-transit-mod.track_brush.first_point", 
                        pos.getX(), pos.getY(), pos.getZ()), false);
                } else {
                    // 执行批量替换
                    int count = replaceTracksInArea(world, firstPos, pos, ModBlocks.TRACK.getDefaultState());
                    player.sendMessage(Text.translatable("item.real-rail-transit-mod.track_brush.replaced", count), false);
                    firstPos = null;
                }
                return ActionResult.SUCCESS;
            }
        }
        
        return ActionResult.PASS;
    }
    
    /**
     * 在指定区域内批量替换轨道
     * @param world 世界
     * @param pos1 第一个位置
     * @param pos2 第二个位置
     * @param newState 新的方块状态
     * @return 替换的方块数量
     */
    private int replaceTracksInArea(World world, BlockPos pos1, BlockPos pos2, BlockState newState) {
        int count = 0;
        
        // 计算区域范围
        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());
        
        // 限制区域大小（防止过大）
        if ((maxX - minX) * (maxY - minY) * (maxZ - minZ) > 1000) {
            RealRailTransitMod.LOGGER.warn("区域过大，操作已取消");
            return 0;
        }
        
        // 遍历区域内的所有方块
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState currentState = world.getBlockState(pos);
                    
                    // 如果是轨道，则替换
                    if (currentState.getBlock() instanceof TrackBlock) {
                        world.setBlockState(pos, newState);
                        count++;
                    }
                }
            }
        }
        
        return count;
    }
}

