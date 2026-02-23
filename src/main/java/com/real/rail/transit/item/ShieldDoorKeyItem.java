package com.real.rail.transit.item;

import com.real.rail.transit.station.ShieldDoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 屏蔽门钥匙
 * 控制屏蔽门开关、权限操作的道具
 */
public class ShieldDoorKeyItem extends Item {
    public ShieldDoorKeyItem(Settings settings) {
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
        
        // 检查点击的是否是屏蔽门
        if (world.getBlockState(pos).getBlock() instanceof ShieldDoorBlock shieldDoor) {
            ShieldDoorBlock doorBlock = (ShieldDoorBlock) world.getBlockState(pos).getBlock();
            boolean isOpen = world.getBlockState(pos).get(ShieldDoorBlock.IS_OPEN);
            
            if (isOpen) {
                doorBlock.close(world, pos);
                player.sendMessage(Text.translatable("item.real-rail-transit-mod.shield_door_key.closed"), false);
            } else {
                doorBlock.open(world, pos);
                player.sendMessage(Text.translatable("item.real-rail-transit-mod.shield_door_key.opened"), false);
            }
            
            return ActionResult.SUCCESS;
        }
        
        return ActionResult.PASS;
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        
        if (!world.isClient) {
            // 显示使用说明
            user.sendMessage(Text.translatable("item.real-rail-transit-mod.shield_door_key.info"), false);
        }
        
        return TypedActionResult.pass(itemStack);
    }
}

