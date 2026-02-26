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
    
    // 滚动相关
    private int scrollOffset = 0;
    private int contentHeight = 0;
    private static final int SCROLL_SPEED = 20;
    
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
        
        // 计算内容总高度
        contentHeight = 300; // 估算内容高度
        
        // 限制滚动范围
        int maxScroll = Math.max(0, contentHeight - (this.height - 100));
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        
        // 使用scissor来裁剪超出屏幕的内容
        int scissorY = 40;
        int scissorHeight = this.height - 100;
        context.enableScissor(0, scissorY, this.width, scissorY + scissorHeight);
        
        // 绘制标题（不滚动）
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.translatable("gui.real-rail-transit-mod.track_control_panel.title"),
            centerX,
            20,
            0xFFFFFF
        );
        
        // 绘制线路统计信息（应用滚动偏移）
        int y = startY - scrollOffset;
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
        y = startY + 150 - scrollOffset;
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
        y = startY + 220 - scrollOffset;
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
        
        context.disableScissor();
        
        // 绘制滚动条（如果需要）
        if (maxScroll > 0) {
            drawScrollBar(context, centerX + 160, scissorY, scissorHeight, maxScroll);
        }
    }
    
    private void drawScrollBar(DrawContext context, int x, int y, int height, int maxScroll) {
        // 绘制滚动条背景
        context.fill(x, y, x + 4, y + height, 0x80000000);
        
        // 计算滚动条滑块位置和大小
        float scrollRatio = scrollOffset / (float) maxScroll;
        int thumbHeight = Math.max(20, (int) (height * (height / (float) (contentHeight + height))));
        int thumbY = y + (int) (scrollRatio * (height - thumbHeight));
        
        // 绘制滚动条滑块
        context.fill(x, thumbY, x + 4, thumbY + thumbHeight, 0xFF808080);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // 检查鼠标是否在可滚动区域内（排除标题和按钮区域）
        int scissorY = 40;
        int scissorHeight = this.height - 100;
        if (mouseY >= scissorY && mouseY <= scissorY + scissorHeight && mouseX >= 0 && mouseX <= this.width) {
            if (verticalAmount != 0) {
                scrollOffset -= (int) (verticalAmount * SCROLL_SPEED);
                int maxScroll = Math.max(0, contentHeight - (this.height - 100));
                scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
                return true;
            }
        }
        // 即使不在滚动区域，也尝试处理滚动事件（但只在有内容需要滚动时）
        if (verticalAmount != 0) {
            int maxScroll = Math.max(0, contentHeight - (this.height - 100));
            if (maxScroll > 0) {
                scrollOffset -= (int) (verticalAmount * SCROLL_SPEED);
                scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    
    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // 不绘制物品栏标签
    }
}
