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
 * 控制面板 GUI 处理器
 */
public class ControlPanelScreenHandler extends ScreenHandler {
    public static ScreenHandlerType<ControlPanelScreenHandler> TYPE;

    private final BlockPos pos;

    public static void register() {
        TYPE = Registry.register(
            Registries.SCREEN_HANDLER,
            Identifier.of(RealRailTransitMod.MOD_ID, "control_panel"),
            new ScreenHandlerType<>((syncId, inventory) ->
                new ControlPanelScreenHandler(syncId, inventory, BlockPos.ORIGIN), null)
        );
    }

    public ControlPanelScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, buf != null && buf.isReadable() ? buf.readBlockPos() : BlockPos.ORIGIN);
    }

    public ControlPanelScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        super(TYPE, syncId);
        if (TYPE == null) {
            throw new IllegalStateException("ControlPanelScreenHandler.TYPE 未注册！请确保 ModScreenHandlers.register() 已调用。");
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

