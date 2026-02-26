package com.real.rail.transit.client.screen;

import com.real.rail.transit.block.screen.TrackConstructionControlPanelScreenHandler;
import com.real.rail.transit.network.ModNetworkPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * 线路建设控制面板GUI界面
 */
public class TrackConstructionControlPanelScreen extends HandledScreen<TrackConstructionControlPanelScreenHandler> {
    
    // 配置选项
    private String selectedTrackType = "standard"; // standard, high_speed, freight
    private String selectedPowerType = "none"; // none, third_rail, catenary
    private String selectedSignalConfig = "auto"; // auto, manual, disabled
    
    // UI组件
    private ButtonWidget trackTypeButton;
    private ButtonWidget powerTypeButton;
    private ButtonWidget signalConfigButton;
    private ButtonWidget applyButton;
    private ButtonWidget batchApplyButton;
    private ButtonWidget closeButton;
    
    // 批量操作状态
    private BlockPos batchStartPos = null;
    private boolean batchMode = false;
    
    // 滚动相关
    private int scrollOffset = 0;
    private static final int SCROLL_SPEED = 20;
    
    public TrackConstructionControlPanelScreen(TrackConstructionControlPanelScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int startY = 60;
        int buttonWidth = 150;
        int buttonHeight = 20;
        int spacing = 25;
        
        // 轨道类型选择按钮（应用滚动偏移）
        trackTypeButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.track_construction_control_panel.track_type." + selectedTrackType),
            button -> cycleTrackType()
        ).dimensions(centerX - buttonWidth / 2, startY - scrollOffset, buttonWidth, buttonHeight).build();
        this.addDrawableChild(trackTypeButton);
        
        // 供电方式选择按钮
        powerTypeButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.track_construction_control_panel.power_type." + selectedPowerType),
            button -> cyclePowerType()
        ).dimensions(centerX - buttonWidth / 2, startY + spacing - scrollOffset, buttonWidth, buttonHeight).build();
        this.addDrawableChild(powerTypeButton);
        
        // 信号机配置选择按钮
        signalConfigButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.track_construction_control_panel.signal_config." + selectedSignalConfig),
            button -> cycleSignalConfig()
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 2 - scrollOffset, buttonWidth, buttonHeight).build();
        this.addDrawableChild(signalConfigButton);
        
        // 应用配置按钮
        applyButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.track_construction_control_panel.apply"),
            button -> applyConfiguration()
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 3 + 10 - scrollOffset, buttonWidth, buttonHeight).build();
        this.addDrawableChild(applyButton);
        
        // 批量应用按钮
        batchApplyButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.track_construction_control_panel.batch_apply"),
            button -> toggleBatchMode()
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 4 + 10 - scrollOffset, buttonWidth, buttonHeight).build();
        this.addDrawableChild(batchApplyButton);
        
        // 关闭按钮（不滚动）
        closeButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.close"),
            button -> this.close()
        ).dimensions(centerX - 50, this.height - 30, 100, 20).build();
        this.addDrawableChild(closeButton);
    }
    
    private void updateButtonPositions() {
        int centerX = this.width / 2;
        int startY = 60;
        int spacing = 25;
        
        if (trackTypeButton != null) trackTypeButton.setY(startY - scrollOffset);
        if (powerTypeButton != null) powerTypeButton.setY(startY + spacing - scrollOffset);
        if (signalConfigButton != null) signalConfigButton.setY(startY + spacing * 2 - scrollOffset);
        if (applyButton != null) applyButton.setY(startY + spacing * 3 + 10 - scrollOffset);
        if (batchApplyButton != null) batchApplyButton.setY(startY + spacing * 4 + 10 - scrollOffset);
    }
    
    private void cycleTrackType() {
        String[] types = {"standard", "high_speed", "freight"};
        int currentIndex = -1;
        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(selectedTrackType)) {
                currentIndex = i;
                break;
            }
        }
        selectedTrackType = types[(currentIndex + 1) % types.length];
        trackTypeButton.setMessage(Text.translatable("gui.real-rail-transit-mod.track_construction_control_panel.track_type." + selectedTrackType));
    }
    
    private void cyclePowerType() {
        String[] types = {"none", "third_rail", "catenary"};
        int currentIndex = -1;
        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(selectedPowerType)) {
                currentIndex = i;
                break;
            }
        }
        selectedPowerType = types[(currentIndex + 1) % types.length];
        powerTypeButton.setMessage(Text.translatable("gui.real-rail-transit-mod.track_construction_control_panel.power_type." + selectedPowerType));
    }
    
    private void cycleSignalConfig() {
        String[] configs = {"auto", "manual", "disabled"};
        int currentIndex = -1;
        for (int i = 0; i < configs.length; i++) {
            if (configs[i].equals(selectedSignalConfig)) {
                currentIndex = i;
                break;
            }
        }
        selectedSignalConfig = configs[(currentIndex + 1) % configs.length];
        signalConfigButton.setMessage(Text.translatable("gui.real-rail-transit-mod.track_construction_control_panel.signal_config." + selectedSignalConfig));
    }
    
    private void applyConfiguration() {
        BlockPos pos = handler.getPos();
        ClientPlayNetworking.send(new ModNetworkPackets.TrackConstructionUpdateConfigPayload(
            pos,
            selectedTrackType,
            selectedPowerType,
            selectedSignalConfig
        ));
    }
    
    private void toggleBatchMode() {
        batchMode = !batchMode;
        if (batchMode) {
            batchStartPos = this.client.player != null ? this.client.player.getBlockPos() : null;
            batchApplyButton.setMessage(Text.translatable("gui.real-rail-transit-mod.track_construction_control_panel.batch_apply_confirm"));
        } else {
            if (batchStartPos != null && this.client.player != null) {
                BlockPos endPos = this.client.player.getBlockPos();
                ClientPlayNetworking.send(new ModNetworkPackets.TrackConstructionBatchApplyPayload(
                    batchStartPos,
                    endPos,
                    selectedTrackType,
                    selectedPowerType,
                    selectedSignalConfig
                ));
            }
            batchStartPos = null;
            batchApplyButton.setMessage(Text.translatable("gui.real-rail-transit-mod.track_construction_control_panel.batch_apply"));
        }
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
        
        // 绘制标题
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.translatable("gui.real-rail-transit-mod.track_construction_control_panel.title"),
            centerX,
            20,
            0xFFFFFF
        );
        
        // 使用scissor来裁剪超出屏幕的内容
        int scissorY = 40;
        int scissorHeight = this.height - 100;
        context.enableScissor(0, scissorY, this.width, scissorY + scissorHeight);
        
        // 绘制标签（应用滚动偏移）
        int labelY = 45 - scrollOffset;
        context.drawText(
            this.textRenderer,
            Text.translatable("gui.real-rail-transit-mod.track_construction_control_panel.track_type.label"),
            centerX - 75,
            labelY,
            0xCCCCCC,
            false
        );
        
        context.drawText(
            this.textRenderer,
            Text.translatable("gui.real-rail-transit-mod.track_construction_control_panel.power_type.label"),
            centerX - 75,
            labelY + 25,
            0xCCCCCC,
            false
        );
        
        context.drawText(
            this.textRenderer,
            Text.translatable("gui.real-rail-transit-mod.track_construction_control_panel.signal_config.label"),
            centerX - 75,
            labelY + 50,
            0xCCCCCC,
            false
        );
        
        context.disableScissor();
        
        // 绘制批量操作提示（不滚动）
        if (batchMode) {
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.track_construction_control_panel.batch_mode_hint"),
                centerX,
                this.height - 60,
                0xFFFF00
            );
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // 处理滚动事件（允许在整个屏幕范围内滚动）
        if (verticalAmount != 0) {
            scrollOffset -= (int) (verticalAmount * SCROLL_SPEED);
            scrollOffset = Math.max(0, scrollOffset);
            updateButtonPositions();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    
    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // 不绘制物品栏标签
    }
}
