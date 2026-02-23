package com.real.rail.transit.item;

import com.real.rail.transit.RealRailTransitMod;
import com.real.rail.transit.preset.PresetManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 预设器
 * 用于快速生成标准轨道、道岔、线路结构预设方案
 * 需要选择两个点来确定预设器的走向，然后显示虚线边框，用轨道点击边框可自动放置轨道
 */
public class PresetItem extends Item {
    public PresetItem(Settings settings) {
        super(settings);
    }
    
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos targetPos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        
        if (player == null) {
            return ActionResult.PASS;
        }
        
        PresetManager presetManager = PresetManager.getInstance();
        PresetManager.PresetData data = presetManager.getPresetData(player.getUuid());
        
        if (world.isClient) {
            // 客户端：选择点
            if (data == null || data.firstPos == null) {
                // 设置第一个点
                presetManager.setFirstPoint(player.getUuid(), targetPos);
                player.sendMessage(Text.translatable("item.real-rail-transit-mod.preset.first_point",
                    targetPos.getX(), targetPos.getY(), targetPos.getZ()), true);
                return ActionResult.SUCCESS;
            } else if (data.secondPos == null) {
                // 设置第二个点
                presetManager.setSecondPoint(player.getUuid(), targetPos);
                player.sendMessage(Text.translatable("item.real-rail-transit-mod.preset.second_point",
                    targetPos.getX(), targetPos.getY(), targetPos.getZ()), true);
                player.sendMessage(Text.translatable("item.real-rail-transit-mod.preset.complete"), true);
                return ActionResult.SUCCESS;
            } else {
                // 已选择两个点，清除重新选择
                presetManager.clearPresetData(player.getUuid());
                presetManager.setFirstPoint(player.getUuid(), targetPos);
                player.sendMessage(Text.translatable("item.real-rail-transit-mod.preset.cleared"), true);
                player.sendMessage(Text.translatable("item.real-rail-transit-mod.preset.first_point",
                    targetPos.getX(), targetPos.getY(), targetPos.getZ()), true);
                return ActionResult.SUCCESS;
            }
        }
        
        return ActionResult.SUCCESS;
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        
        if (!world.isClient) {
            PresetManager presetManager = PresetManager.getInstance();
            PresetManager.PresetData data = presetManager.getPresetData(user.getUuid());
            
            if (data != null && data.firstPos != null && data.secondPos != null) {
                // 清除预设器数据
                presetManager.clearPresetData(user.getUuid());
                user.sendMessage(Text.translatable("item.real-rail-transit-mod.preset.cleared"), false);
            } else {
                // 显示预设器使用说明
                user.sendMessage(Text.translatable("item.real-rail-transit-mod.preset.info"), false);
            }
        }
        
        return TypedActionResult.pass(itemStack);
    }
}

