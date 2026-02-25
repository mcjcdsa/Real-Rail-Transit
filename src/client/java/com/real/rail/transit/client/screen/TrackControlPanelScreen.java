package com.real.rail.transit.client.screen;

import com.real.rail.transit.block.screen.TrackControlPanelScreenHandler;
import com.real.rail.transit.network.ModNetworkPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * 线路控制面板GUI界面
 */
public class TrackControlPanelScreen extends HandledScreen<TrackControlPanelScreenHandler> {
    
    // 统计数据
    private int trackCount = 0;
    private int signalCount = 0;
    private int trainCount = 0;
    private int powerSectionCount = 0;
    private int activeTrains = 0;
    
    private boolean statsLoaded = false;
    private static boolean receiverRegistered = false;
    
    private ButtonWidget refreshButton;
    private ButtonWidget closeButton;
    
    public TrackControlPanelScreen(TrackControlPanelScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        
        // 注册数据包接收器（只注册一次）
        if (!receiverRegistered) {
            ClientPlayNetworking.registerGlobalReceiver(ModNetworkPackets.TrackControlStatsResponsePayload.ID, 
                (payload, context) -> {
                    context.client().execute(() -> {
                        // 更新所有打开的TrackControlPanelScreen实例
                        if (context.client().currentScreen instanceof TrackControlPanelScreen screen) {
                            screen.updateStats(payload);
                        }
                    });
                });
            receiverRegistered = true;
        }
    }
    
    private void updateStats(ModNetworkPackets.TrackControlStatsResponsePayload payload) {
        this.trackCount = payload.trackCount();
        this.signalCount = payload.signalCount();
        this.trainCount = payload.trainCount();
        this.powerSectionCount = payload.powerSectionCount();
        this.activeTrains = payload.activeTrains();
        this.statsLoaded = true;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        
        // 刷新按钮
        refreshButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.track_control_panel.refresh"),
            button -> requestStats()
        ).dimensions(centerX - 75, this.height - 50, 70, 20).build();
        this.addDrawableChild(refreshButton);
        
        // 关闭按钮
        closeButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.close"),
            button -> this.close()
        ).dimensions(centerX + 5, this.height - 50, 70, 20).build();
        this.addDrawableChild(closeButton);
        
        // 自动请求统计数据
        requestStats();
    }
    
    private void requestStats() {
        BlockPos pos = handler.getPos();
        ClientPlayNetworking.send(new ModNetworkPackets.TrackControlRequestStatsPayload(pos));
        statsLoaded = false;
    }
    
    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        // 绘制半透明黑色背景
        context.fill(0, 0, this.width, this.height, 0xC0101010);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        
        int centerX = this.width / 2;
        int startY = 50;
        int lineHeight = 20;
        
        // 绘制标题
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.translatable("gui.real-rail-transit-mod.track_control_panel.title"),
            centerX,
            20,
            0xFFFFFF
        );
        
        // 绘制线路统计信息
        int y = startY;
        context.drawText(
            this.textRenderer,
            Text.translatable("gui.real-rail-transit-mod.track_control_panel.stats.title"),
            centerX - 150,
            y,
            0xFFFF00,
            false
        );
        y += lineHeight + 5;
        
        if (statsLoaded) {
            context.drawText(
                this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.track_control_panel.stats.track_count", trackCount),
                centerX - 150,
                y,
                0xCCCCCC,
                false
            );
            y += lineHeight;
            
            context.drawText(
                this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.track_control_panel.stats.signal_count", signalCount),
                centerX - 150,
                y,
                0xCCCCCC,
                false
            );
            y += lineHeight;
            
            context.drawText(
                this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.track_control_panel.stats.train_count", trainCount),
                centerX - 150,
                y,
                0xCCCCCC,
                false
            );
            y += lineHeight;
            
            context.drawText(
                this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.track_control_panel.stats.active_trains", activeTrains),
                centerX - 150,
                y,
                0xCCCCCC,
                false
            );
            y += lineHeight;
            
            context.drawText(
                this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.track_control_panel.stats.power_section_count", powerSectionCount),
                centerX - 150,
                y,
                0xCCCCCC,
                false
            );
        } else {
            context.drawText(
                this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.track_control_panel.loading"),
                centerX - 150,
                y,
                0x888888,
                false
            );
        }
        
        // 绘制信号机状态标题
        y = startY + 150;
        context.drawText(
            this.textRenderer,
            Text.translatable("gui.real-rail-transit-mod.track_control_panel.signal_status.title"),
            centerX - 150,
            y,
            0xFFFF00,
            false
        );
        y += lineHeight + 5;
        
        if (statsLoaded && signalCount > 0) {
            context.drawText(
                this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.track_control_panel.signal_status.info", signalCount),
                centerX - 150,
                y,
                0xCCCCCC,
                false
            );
        } else if (statsLoaded) {
            context.drawText(
                this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.track_control_panel.signal_status.no_signals"),
                centerX - 150,
                y,
                0x888888,
                false
            );
        }
        
        // 绘制列车运行状态标题
        y = startY + 220;
        context.drawText(
            this.textRenderer,
            Text.translatable("gui.real-rail-transit-mod.track_control_panel.train_status.title"),
            centerX - 150,
            y,
            0xFFFF00,
            false
        );
        y += lineHeight + 5;
        
        if (statsLoaded) {
            if (trainCount > 0) {
                context.drawText(
                    this.textRenderer,
                    Text.translatable("gui.real-rail-transit-mod.track_control_panel.train_status.running", activeTrains, trainCount),
                    centerX - 150,
                    y,
                    0x00FF00,
                    false
                );
            } else {
                context.drawText(
                    this.textRenderer,
                    Text.translatable("gui.real-rail-transit-mod.track_control_panel.train_status.no_trains"),
                    centerX - 150,
                    y,
                    0x888888,
                    false
                );
            }
        }
    }
    
    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // 不绘制物品栏标签
    }
}
