package com.real.rail.transit.item;

import com.real.rail.transit.entity.TrainEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * 司机钥匙
 * 用于启动列车的钥匙，不可放置
 * 右键点击列车实体可以启动/关闭列车引擎
 */
public class DriverKeyItem extends Item {
    
    public DriverKeyItem(Settings settings) {
        super(settings);
    }
    
    /**
     * 右键点击实体时调用
     * 用于启动/关闭列车引擎
     */
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, Entity entity, Hand hand) {
        World world = user.getWorld();
        
        // 只在服务端处理
        if (world.isClient) {
            return ActionResult.PASS;
        }
        
        // 检查是否是列车实体
        if (entity instanceof TrainEntity train) {
            // 切换引擎状态
            boolean currentState = train.isEngineOn();
            train.setEngineOn(!currentState);
            
            // 发送消息给玩家
            if (!currentState) {
                user.sendMessage(Text.translatable("item.real-rail-transit-mod.driver_key.train_started"), false);
            } else {
                user.sendMessage(Text.translatable("item.real-rail-transit-mod.driver_key.train_stopped"), false);
            }
            
            return ActionResult.SUCCESS;
        }
        
        return ActionResult.PASS;
    }
    
    /**
     * 空手右键时调用
     * 显示使用说明
     */
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        
        if (!world.isClient) {
            // 显示使用说明
            user.sendMessage(Text.translatable("item.real-rail-transit-mod.driver_key.info"), false);
        }
        
        return TypedActionResult.pass(itemStack);
    }
}

