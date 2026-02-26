package com.real.rail.transit.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

/**
 * 票卡物品
 * 用于刷闸机进站，不能放置，只能手持使用
 * 存储目的地车站信息
 */
public class TicketCardItem extends Item {
    public TicketCardItem(Settings settings) {
        super(settings);
    }
    
    /**
     * 设置票卡的目的地
     */
    public static void setDestination(ItemStack stack, String destination) {
        // 使用CustomData组件存储NBT数据
        var customData = stack.get(net.minecraft.component.DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbt = customData != null ? customData.copyNbt() : new NbtCompound();
        nbt.putString("destination", destination);
        // 创建CustomData组件 - 使用正确的API
        var newCustomData = net.minecraft.component.types.CustomData.of(nbt);
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA, newCustomData);
    }
    
    /**
     * 获取票卡的目的地
     */
    public static String getDestination(ItemStack stack) {
        var customData = stack.get(net.minecraft.component.DataComponentTypes.CUSTOM_DATA);
        if (customData != null) {
            NbtCompound nbt = customData.copyNbt();
            if (nbt != null && nbt.contains("destination")) {
                return nbt.getString("destination");
            }
        }
        return "";
    }
    
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        
        if (world.isClient || player == null) {
            return ActionResult.PASS;
        }
        
        // 检查点击的是否是闸机
        if (world.getBlockState(context.getBlockPos()).getBlock() instanceof com.real.rail.transit.station.GateBlock) {
            // 闸机处理逻辑在GateBlock中实现
            // 这里只返回PASS，让闸机自己处理
            return ActionResult.PASS;
        }
        
        return ActionResult.PASS;
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, net.minecraft.util.Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        
        if (!world.isClient) {
            // 显示票卡信息（包括目的地）
            String destination = getDestination(itemStack);
            if (!destination.isEmpty()) {
                user.sendMessage(Text.translatable("item.real-rail-transit-mod.ticket_card.info_with_destination", destination), false);
            } else {
                user.sendMessage(Text.translatable("item.real-rail-transit-mod.ticket_card.info"), false);
            }
        }
        
        return TypedActionResult.pass(itemStack);
    }
    
    @Override
    public Text getName(ItemStack stack) {
        String destination = getDestination(stack);
        if (!destination.isEmpty()) {
            return Text.translatable("item.real-rail-transit-mod.ticket_card.with_destination", destination);
        }
        return super.getName(stack);
    }
}
