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
        
        // 在客户端和服务端同时更新预设数据：
        // - 客户端用于渲染虚线路径
        // - 服务端用于自动铺轨时的路径判定
        if (data == null || data.firstPos == null) {
            // 设置第一个点
            presetManager.setFirstPoint(player.getUuid(), targetPos);
            if (!world.isClient) {
                player.sendMessage(Text.translatable("item.real-rail-transit-mod.preset.first_point",
                    targetPos.getX(), targetPos.getY(), targetPos.getZ()), true);
            }
        } else if (data.secondPos == null) {
            // 设置第二个点
            presetManager.setSecondPoint(player.getUuid(), targetPos);
            if (!world.isClient) {
                player.sendMessage(Text.translatable("item.real-rail-transit-mod.preset.second_point",
                    targetPos.getX(), targetPos.getY(), targetPos.getZ()), true);
                player.sendMessage(Text.translatable("item.real-rail-transit-mod.preset.complete"), true);
            }
        } else {
            // 已选择两个点，清除重新选择
            presetManager.clearPresetData(player.getUuid());
            presetManager.setFirstPoint(player.getUuid(), targetPos);
            if (!world.isClient) {
                player.sendMessage(Text.translatable("item.real-rail-transit-mod.preset.cleared"), true);
                player.sendMessage(Text.translatable("item.real-rail-transit-mod.preset.first_point",
                    targetPos.getX(), targetPos.getY(), targetPos.getZ()), true);
            }
        }
        
        return ActionResult.SUCCESS;
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        
        PresetManager presetManager = PresetManager.getInstance();
        PresetManager.PresetData data = presetManager.getPresetData(user.getUuid());
        
        // 清除或提示，既在客户端也在服务端同步数据；聊天提示只在服务端发送一次
        if (data != null && data.firstPos != null && data.secondPos != null) {
            presetManager.clearPresetData(user.getUuid());
            if (!world.isClient) {
                user.sendMessage(Text.translatable("item.real-rail-transit-mod.preset.cleared"), false);
            }
        } else {
            if (!world.isClient) {
                // 显示预设器使用说明
                user.sendMessage(Text.translatable("item.real-rail-transit-mod.preset.info"), false);
            }
        }
        
        return TypedActionResult.pass(itemStack);
    }
}

