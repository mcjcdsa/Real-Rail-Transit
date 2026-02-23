package com.real.rail.transit.station.screen;

import com.real.rail.transit.RealRailTransitMod;
import com.real.rail.transit.station.entity.ArrivalDisplayScreenBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * 到站显示屏GUI处理器
 */
public class ArrivalDisplayScreenHandler extends ScreenHandler {
    public static ScreenHandlerType<ArrivalDisplayScreenHandler> TYPE;
    
    private final ArrivalDisplayScreenBlockEntity blockEntity;
    private final BlockPos pos;
    
    public static void register() {
        TYPE = Registry.register(
            Registries.SCREEN_HANDLER,
            Identifier.of(RealRailTransitMod.MOD_ID, "arrival_display_screen"),
            new ScreenHandlerType<>((syncId, inventory) -> {
                throw new UnsupportedOperationException("Use constructor with PacketByteBuf or BlockPos");
            }, null)
        );
    }
    
    public ArrivalDisplayScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, buf.readBlockPos());
    }
    
    public ArrivalDisplayScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        super(TYPE, syncId);
        this.pos = pos;
        if (playerInventory.player.getWorld() != null) {
            this.blockEntity = (ArrivalDisplayScreenBlockEntity) playerInventory.player.getWorld().getBlockEntity(pos);
        } else {
            this.blockEntity = null;
        }
    }
    
    public ArrivalDisplayScreenBlockEntity getBlockEntity() {
        return blockEntity;
    }
    
    public BlockPos getPos() {
        return pos;
    }
    
    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}

