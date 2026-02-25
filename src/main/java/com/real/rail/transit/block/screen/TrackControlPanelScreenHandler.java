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
 * 线路控制面板GUI处理器
 */
public class TrackControlPanelScreenHandler extends ScreenHandler {
    public static ScreenHandlerType<TrackControlPanelScreenHandler> TYPE;
    
    private final BlockPos pos;
    
    public static void register() {
        TYPE = Registry.register(
            Registries.SCREEN_HANDLER,
            Identifier.of(RealRailTransitMod.MOD_ID, "track_control_panel"),
            new ScreenHandlerType<>((syncId, inventory) -> {
                // 这个工厂方法不应该被调用，因为客户端会使用 PacketByteBuf 构造函数
                // 但如果被调用，返回一个使用默认位置的 ScreenHandler
                return new TrackControlPanelScreenHandler(syncId, inventory, BlockPos.ORIGIN);
            }, null)
        );
    }
    
    public TrackControlPanelScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, buf != null && buf.isReadable() ? buf.readBlockPos() : BlockPos.ORIGIN);
    }
    
    public TrackControlPanelScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        super(TYPE, syncId);
        if (TYPE == null) {
            throw new IllegalStateException("TrackControlPanelScreenHandler.TYPE 未注册！请确保 ModScreenHandlers.register() 已调用。");
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

