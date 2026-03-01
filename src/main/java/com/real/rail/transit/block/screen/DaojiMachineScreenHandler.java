package com.real.rail.transit.block.screen;

import com.real.rail.transit.RealRailTransitMod;
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
 * 盾构机 GUI 处理器
 */
public class DaojiMachineScreenHandler extends ScreenHandler {
    public static ScreenHandlerType<DaojiMachineScreenHandler> TYPE;

    private final BlockPos pos;

    public static void register() {
        TYPE = Registry.register(
            Registries.SCREEN_HANDLER,
            Identifier.of(RealRailTransitMod.MOD_ID, "daoji_machine"),
            new ScreenHandlerType<>((syncId, inventory) ->
                new DaojiMachineScreenHandler(syncId, inventory, BlockPos.ORIGIN), null)
        );
    }

    public DaojiMachineScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, buf != null && buf.isReadable() ? buf.readBlockPos() : BlockPos.ORIGIN);
    }

    public DaojiMachineScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        super(TYPE, syncId);
        if (TYPE == null) {
            throw new IllegalStateException("DaojiMachineScreenHandler.TYPE 未注册！请确保 ModScreenHandlers.register() 已调用。");
        }
        this.pos = pos != null ? pos : BlockPos.ORIGIN;
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

