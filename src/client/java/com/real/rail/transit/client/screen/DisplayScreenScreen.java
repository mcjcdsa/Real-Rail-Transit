package com.real.rail.transit.client.screen;

import com.real.rail.transit.network.ModNetworkPackets;
import com.real.rail.transit.station.entity.DisplayScreenBlockEntity;
import com.real.rail.transit.station.screen.DisplayScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

/**
 * 显示屏GUI界面
 */
public class DisplayScreenScreen extends HandledScreen<DisplayScreenHandler> {
    private TextFieldWidget textField;
    private TextFieldWidget colorField;
    private TextFieldWidget scaleField;
    private ButtonWidget saveButton;
    private DisplayScreenBlockEntity blockEntity;
    
    public DisplayScreenScreen(DisplayScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.blockEntity = handler.getBlockEntity();
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // 文本输入框（支持长文本）
        this.textField = new TextFieldWidget(
            this.textRenderer,
            centerX - 150, centerY - 40, 300, 20,
            Text.translatable("gui.real-rail-transit-mod.display_screen.text_field")
        );
        this.textField.setMaxLength(500);
        this.textField.setEditableColor(0xFFFFFF);
        if (blockEntity != null) {
            this.textField.setText(blockEntity.getDisplayText());
        }
        this.addDrawableChild(this.textField);
        
        // 颜色输入框（十六进制RGB）
        this.colorField = new TextFieldWidget(
            this.textRenderer,
            centerX - 150, centerY + 30, 140, 20,
            Text.translatable("gui.real-rail-transit-mod.display_screen.color_field")
        );
        this.colorField.setMaxLength(8);
        if (blockEntity != null) {
            this.colorField.setText(String.format("#%06X", blockEntity.getTextColor() & 0xFFFFFF));
        }
        this.addDrawableChild(this.colorField);
        
        // 缩放输入框
        this.scaleField = new TextFieldWidget(
            this.textRenderer,
            centerX + 10, centerY + 30, 140, 20,
            Text.translatable("gui.real-rail-transit-mod.display_screen.scale_field")
        );
        this.scaleField.setMaxLength(4);
        if (blockEntity != null) {
            this.scaleField.setText(String.format("%.2f", blockEntity.getTextScale()));
        }
        this.addDrawableChild(this.scaleField);
        
        // 保存按钮
        this.saveButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.display_screen.save"),
            button -> this.saveSettings()
        ).dimensions(centerX - 50, centerY + 60, 100, 20).build();
        this.addDrawableChild(this.saveButton);
        
        // 设置焦点
        this.setInitialFocus(this.textField);
    }
    
    private void saveSettings() {
        if (blockEntity != null) {
            String text = this.textField != null ? this.textField.getText() : null;
            Integer color = null;
            Float scale = null;
            
            if (this.colorField != null) {
                try {
                    String colorStr = this.colorField.getText().replace("#", "");
                    color = Integer.parseInt(colorStr, 16) | 0xFF000000; // 添加alpha通道
                } catch (NumberFormatException e) {
                    // 无效输入，使用默认颜色
                }
            }
            if (this.scaleField != null) {
                try {
                    scale = Float.parseFloat(this.scaleField.getText());
                } catch (NumberFormatException e) {
                    // 无效输入，忽略
                }
            }
            
            // 更新本地BlockEntity
            if (text != null) blockEntity.setDisplayText(text);
            if (color != null) blockEntity.setTextColor(color);
            if (scale != null) blockEntity.setTextScale(scale);
            
            // 发送数据包到服务器同步
            if (this.client != null && this.client.player != null) {
                ClientPlayNetworking.send(new ModNetworkPackets.DisplayScreenUpdatePayload(
                    blockEntity.getPos(), text, color, scale
                ));
            }
            
            this.close();
        }
    }
    
    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // 绘制半透明背景
        context.fill(centerX - 180, centerY - 80, centerX + 180, centerY + 100, 0xC0101010);
        context.drawBorder(centerX - 180, centerY - 80, 360, 180, 0xFF404040);
    }
    
    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        int centerX = this.width / 2;
        
        // 绘制标题
        String title = Text.translatable("gui.real-rail-transit-mod.display_screen.title").getString();
        int titleWidth = this.textRenderer.getWidth(title);
        context.drawText(this.textRenderer, title, centerX - titleWidth / 2, this.height / 2 - 50, 0xFFFFFF, false);
        
        // 绘制标签
        String textLabel = Text.translatable("gui.real-rail-transit-mod.display_screen.text_label").getString();
        context.drawText(this.textRenderer, textLabel, centerX - 150, this.height / 2 - 95, 0xCCCCCC, false);
        
        String colorLabel = Text.translatable("gui.real-rail-transit-mod.display_screen.color_label").getString();
        context.drawText(this.textRenderer, colorLabel, centerX - 150, this.height / 2 + 15, 0xCCCCCC, false);
        
        String scaleLabel = Text.translatable("gui.real-rail-transit-mod.display_screen.scale_label").getString();
        context.drawText(this.textRenderer, scaleLabel, centerX + 10, this.height / 2 + 15, 0xCCCCCC, false);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}

