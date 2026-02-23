package com.real.rail.transit.item;

import com.real.rail.transit.RealRailTransitMod;
import com.real.rail.transit.registry.ModBlocks;
import com.real.rail.transit.station.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 车站刷子物品
 * 用于批量修改车站设施
 */
public class StationBrushItem extends Item {
    private BlockPos firstPos = null;
    
    public StationBrushItem(Settings settings) {
        super(settings);
    }
    
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        
        if (world.isClient || player == null) {
            return ActionResult.PASS;
        }
        
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        
        // 检查是否是车站设施
        if (isStationFacility(block)) {
            if (player.isSneaking()) {
                // 按住Shift点击：清除选择
                firstPos = null;
                player.sendMessage(Text.translatable("item.real-rail-transit-mod.station_brush.cleared"), false);
                return ActionResult.SUCCESS;
            } else {
                // 普通点击：设置第一个点或执行批量操作
                if (firstPos == null) {
                    firstPos = pos;
                    player.sendMessage(Text.translatable("item.real-rail-transit-mod.station_brush.first_point", 
                        pos.getX(), pos.getY(), pos.getZ()), false);
                } else {
                    // 执行批量操作（复制第一个方块的属性到区域内的同类方块）
                    int count = copyPropertiesInArea(world, firstPos, pos, block);
                    player.sendMessage(Text.translatable("item.real-rail-transit-mod.station_brush.copied", count), false);
                    firstPos = null;
                }
                return ActionResult.SUCCESS;
            }
        }
        
        return ActionResult.PASS;
    }
    
    /**
     * 检查是否是车站设施
     */
    private boolean isStationFacility(Block block) {
        return block instanceof ShieldDoorBlock ||
               block instanceof ElevatorBlock ||
               block instanceof AutomaticStaircaseBlock ||
               block instanceof DisplayScreenBlock ||
               block instanceof ArrivalDisplayScreenBlock ||
               block instanceof TicketMachineBlock ||
               block instanceof GateBlock ||
               block instanceof SmallTVBlock ||
               block instanceof StationRadioBlock ||
               block instanceof BarricadeBlock ||
               block instanceof GlassWallBlock ||
               block instanceof TileWallBlock ||
               block instanceof CeilingBlock ||
               block instanceof LitCeilingBlock ||
               block instanceof PostageBlock ||
               block instanceof ElectronicDoorControllerBlock ||
               block == ModBlocks.UPPER_SHIELD_DOOR ||
               block == ModBlocks.LOWER_SHIELD_DOOR ||
               block == ModBlocks.ELEVATOR ||
               block == ModBlocks.AUTOMATIC_STAIRCASE ||
               block == ModBlocks.DISPLAY_SCREEN ||
               block == ModBlocks.ARRIVAL_DISPLAY_SCREEN ||
               block == ModBlocks.TICKET_MACHINE ||
               block == ModBlocks.GATE ||
               block == ModBlocks.SMALL_TV ||
               block == ModBlocks.STATION_RADIO ||
               block == ModBlocks.BARRICADE ||
               block == ModBlocks.GLASS_WALL ||
               block == ModBlocks.TILE_WALL ||
               block == ModBlocks.CEILING ||
               block == ModBlocks.LIT_CEILING ||
               block == ModBlocks.POSTAGE ||
               block == ModBlocks.ELECTRONIC_DOOR_CONTROLLER;
    }
    
    /**
     * 在指定区域内复制属性
     */
    private int copyPropertiesInArea(World world, BlockPos pos1, BlockPos pos2, Block targetBlock) {
        int count = 0;
        
        // 计算区域范围
        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());
        
        // 限制区域大小
        if ((maxX - minX) * (maxY - minY) * (maxZ - minZ) > 1000) {
            RealRailTransitMod.LOGGER.warn("区域过大，操作已取消");
            return 0;
        }
        
        BlockState sourceState = world.getBlockState(pos1);
        
        // 遍历区域内的所有方块
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState currentState = world.getBlockState(pos);
                    
                    // 如果是同类方块，复制属性
                    if (currentState.getBlock() == targetBlock && !pos.equals(pos1)) {
                        // 尝试复制状态属性（如果兼容）
                        try {
                            BlockState newState = copyCompatibleProperties(sourceState, currentState);
                            if (newState != currentState) {
                                world.setBlockState(pos, newState);
                                count++;
                            }
                        } catch (Exception e) {
                            // 忽略不兼容的属性
                        }
                    }
                }
            }
        }
        
        return count;
    }
    
    /**
     * 复制兼容的属性
     * 注意：由于Minecraft的Property类型系统限制，此方法可能无法完全复制所有属性
     */
    private BlockState copyCompatibleProperties(BlockState source, BlockState target) {
        // 简化实现：只复制布尔属性
        BlockState result = target;
        
        // 尝试复制布尔属性
        for (net.minecraft.state.property.Property<?> property : source.getProperties()) {
            if (target.contains(property) && property instanceof net.minecraft.state.property.BooleanProperty) {
                try {
                    net.minecraft.state.property.BooleanProperty boolProp = (net.minecraft.state.property.BooleanProperty) property;
                    boolean value = source.get(boolProp);
                    result = result.with(boolProp, value);
                } catch (Exception e) {
                    // 忽略不兼容的属性
                }
            }
        }
        
        return result;
    }
}

