package com.real.rail.transit.item;

import com.real.rail.transit.block.screen.ControlPanelScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 控制面板物品
 * 只能右键打开GUI，不能放置
 */
public class ControlPanelItem extends Item {
    
    public ControlPanelItem(Settings settings) {
        super(settings);
    }
    
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null || context.getWorld().isClient) {
            return ActionResult.PASS;
        }
        
        try {
            BlockPos pos = context.getBlockPos();
            NamedScreenHandlerFactory screenHandlerFactory = new NamedScreenHandlerFactory() {
                @Override
                public Text getDisplayName() {
                    return Text.translatable("gui.real-rail-transit-mod.control_panel.title");
                }
                
                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
                    return new ControlPanelScreenHandler(syncId, inventory, pos);
                }
                
                public void writeScreenOpeningData(PlayerEntity player, PacketByteBuf buf) {
                    buf.writeBlockPos(pos);
                }
            };
            player.openHandledScreen(screenHandlerFactory);
        } catch (Exception e) {
            com.real.rail.transit.RealRailTransitMod.LOGGER.error("打开控制面板GUI时出错", e);
        }
        
        return ActionResult.SUCCESS;
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        
        if (!world.isClient) {
            try {
                BlockPos pos = user.getBlockPos();
                NamedScreenHandlerFactory screenHandlerFactory = new NamedScreenHandlerFactory() {
                    @Override
                    public Text getDisplayName() {
                        return Text.translatable("gui.real-rail-transit-mod.control_panel.title");
                    }
                    
                    @Override
                    public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
                        return new ControlPanelScreenHandler(syncId, inventory, pos);
                    }
                    
                    public void writeScreenOpeningData(PlayerEntity player, PacketByteBuf buf) {
                        buf.writeBlockPos(pos);
                    }
                };
                user.openHandledScreen(screenHandlerFactory);
            } catch (Exception e) {
                com.real.rail.transit.RealRailTransitMod.LOGGER.error("打开控制面板GUI时出错", e);
            }
        }
        
        return TypedActionResult.success(itemStack, world.isClient());
    }
}

