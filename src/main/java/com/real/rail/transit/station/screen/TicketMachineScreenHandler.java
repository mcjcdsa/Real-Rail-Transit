package com.real.rail.transit.station.screen;

import com.real.rail.transit.RealRailTransitMod;
import com.real.rail.transit.station.entity.TicketMachineBlockEntity;
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
 * 售票机GUI处理器
 */
public class TicketMachineScreenHandler extends ScreenHandler {
    public static ScreenHandlerType<TicketMachineScreenHandler> TYPE;
    
    private final TicketMachineBlockEntity blockEntity;
    private final BlockPos pos;
    
    public static void register() {
        TYPE = Registry.register(
            Registries.SCREEN_HANDLER,
            Identifier.of(RealRailTransitMod.MOD_ID, "ticket_machine"),
            new ScreenHandlerType<>((syncId, inventory) -> {
                throw new UnsupportedOperationException("Use constructor with PacketByteBuf or BlockPos");
            }, null)
        );
    }
    
    public TicketMachineScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, buf.readBlockPos());
    }
    
    public TicketMachineScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        super(TYPE, syncId);
        this.pos = pos;
        if (playerInventory.player.getWorld() != null) {
            this.blockEntity = (TicketMachineBlockEntity) playerInventory.player.getWorld().getBlockEntity(pos);
        } else {
            this.blockEntity = null;
        }
    }
    
    public TicketMachineBlockEntity getBlockEntity() {
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

