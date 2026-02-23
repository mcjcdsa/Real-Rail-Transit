package com.real.rail.transit.client.screen;

import com.real.rail.transit.network.ModNetworkPackets;
import com.real.rail.transit.station.entity.ArrivalDisplayScreenBlockEntity;
import com.real.rail.transit.station.screen.ArrivalDisplayScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

/**
 * 到站显示屏GUI界面
 */
public class ArrivalDisplayScreenScreen extends HandledScreen<ArrivalDisplayScreenHandler> {
    private TextFieldWidget trainIdField;
    private TextFieldWidget destinationField;
    private TextFieldWidget arrivalTimeField;
    private TextFieldWidget platformField;
    private TextFieldWidget customTextField;
    private TextFieldWidget colorField;
    private ButtonWidget saveButton;
    private ArrivalDisplayScreenBlockEntity blockEntity;
    
    public ArrivalDisplayScreenScreen(ArrivalDisplayScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.blockEntity = handler.getBlockEntity();
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // 列车ID输入框
        this.trainIdField = new TextFieldWidget(
            this.textRenderer,
            centerX - 150, centerY - 60, 300, 20,
            Text.translatable("gui.real-rail-transit-mod.arrival_display_screen.train_id_field")
        );
        this.trainIdField.setMaxLength(20);
        if (blockEntity != null) {
            this.trainIdField.setText(blockEntity.getTrainId());
        }
        this.addDrawableChild(this.trainIdField);
        
        // 目的地输入框
        this.destinationField = new TextFieldWidget(
            this.textRenderer,
            centerX - 150, centerY - 30, 300, 20,
            Text.translatable("gui.real-rail-transit-mod.arrival_display_screen.destination_field")
        );
        this.destinationField.setMaxLength(30);
        if (blockEntity != null) {
            this.destinationField.setText(blockEntity.getDestination());
        }
        this.addDrawableChild(this.destinationField);
        
        // 到站时间输入框
        this.arrivalTimeField = new TextFieldWidget(
            this.textRenderer,
            centerX - 150, centerY, 140, 20,
            Text.translatable("gui.real-rail-transit-mod.arrival_display_screen.arrival_time_field")
        );
        this.arrivalTimeField.setMaxLength(3);
        if (blockEntity != null) {
            this.arrivalTimeField.setText(String.valueOf(blockEntity.getNextArrivalTime()));
        }
        this.addDrawableChild(this.arrivalTimeField);
        
        // 站台输入框
        this.platformField = new TextFieldWidget(
            this.textRenderer,
            centerX + 10, centerY, 140, 20,
            Text.translatable("gui.real-rail-transit-mod.arrival_display_screen.platform_field")
        );
        this.platformField.setMaxLength(20);
        if (blockEntity != null) {
            this.platformField.setText(blockEntity.getPlatform());
        }
        this.addDrawableChild(this.platformField);
        
        // 自定义文本输入框
        this.customTextField = new TextFieldWidget(
            this.textRenderer,
            centerX - 150, centerY + 30, 300, 20,
            Text.translatable("gui.real-rail-transit-mod.arrival_display_screen.custom_text_field")
        );
        this.customTextField.setMaxLength(200);
        if (blockEntity != null) {
            this.customTextField.setText(blockEntity.getCustomText());
        }
        this.addDrawableChild(this.customTextField);
        
        // 颜色输入框
        this.colorField = new TextFieldWidget(
            this.textRenderer,
            centerX - 150, centerY + 60, 140, 20,
            Text.translatable("gui.real-rail-transit-mod.arrival_display_screen.color_field")
        );
        this.colorField.setMaxLength(8);
        if (blockEntity != null) {
            this.colorField.setText(String.format("#%06X", blockEntity.getTextColor() & 0xFFFFFF));
        }
        this.addDrawableChild(this.colorField);
        
        // 保存按钮
        this.saveButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.arrival_display_screen.save"),
            button -> this.saveData()
        ).dimensions(centerX + 10, centerY + 60, 140, 20).build();
        this.addDrawableChild(this.saveButton);
        
        // 设置焦点
        this.setInitialFocus(this.trainIdField);
    }
    
    private void saveData() {
        if (blockEntity != null) {
            String trainId = this.trainIdField != null ? this.trainIdField.getText() : null;
            String destination = this.destinationField != null ? this.destinationField.getText() : null;
            Integer arrivalTime = null;
            String platform = this.platformField != null ? this.platformField.getText() : null;
            String customText = this.customTextField != null ? this.customTextField.getText() : null;
            Integer color = null;
            
            if (this.arrivalTimeField != null) {
                try {
                    arrivalTime = Integer.parseInt(this.arrivalTimeField.getText());
                } catch (NumberFormatException e) {
                    // 无效输入，忽略
                }
            }
            if (this.colorField != null) {
                try {
                    String colorStr = this.colorField.getText().replace("#", "");
                    color = Integer.parseInt(colorStr, 16) | 0xFF000000;
                } catch (NumberFormatException e) {
                    // 无效输入，使用默认颜色
                }
            }
            
            // 更新本地BlockEntity
            if (trainId != null) blockEntity.setTrainId(trainId);
            if (destination != null) blockEntity.setDestination(destination);
            if (arrivalTime != null) blockEntity.setNextArrivalTime(arrivalTime);
            if (platform != null) blockEntity.setPlatform(platform);
            if (customText != null) blockEntity.setCustomText(customText);
            if (color != null) blockEntity.setTextColor(color);
            
            // 发送数据包到服务器同步
            if (this.client != null && this.client.player != null) {
                ClientPlayNetworking.send(new ModNetworkPackets.ArrivalDisplayUpdatePayload(
                    blockEntity.getPos(), trainId, destination, arrivalTime, platform, customText, color
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
        context.fill(centerX - 180, centerY - 90, centerX + 180, centerY + 100, 0xC0101010);
        context.drawBorder(centerX - 180, centerY - 90, 360, 190, 0xFF404040);
    }
    
    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        int centerX = this.width / 2;
        
        // 绘制标题
        String title = Text.translatable("gui.real-rail-transit-mod.arrival_display_screen.title").getString();
        int titleWidth = this.textRenderer.getWidth(title);
        context.drawText(this.textRenderer, title, centerX - titleWidth / 2, this.height / 2 - 75, 0xFFFFFF, false);
        
        // 绘制标签
        String trainIdLabel = Text.translatable("gui.real-rail-transit-mod.arrival_display_screen.train_id_label").getString();
        context.drawText(this.textRenderer, trainIdLabel, centerX - 150, this.height / 2 - 75, 0xCCCCCC, false);
        
        String destinationLabel = Text.translatable("gui.real-rail-transit-mod.arrival_display_screen.destination_label").getString();
        context.drawText(this.textRenderer, destinationLabel, centerX - 150, this.height / 2 - 45, 0xCCCCCC, false);
        
        String arrivalTimeLabel = Text.translatable("gui.real-rail-transit-mod.arrival_display_screen.arrival_time_label").getString();
        context.drawText(this.textRenderer, arrivalTimeLabel, centerX - 150, this.height / 2 - 15, 0xCCCCCC, false);
        
        String platformLabel = Text.translatable("gui.real-rail-transit-mod.arrival_display_screen.platform_label").getString();
        context.drawText(this.textRenderer, platformLabel, centerX + 10, this.height / 2 - 15, 0xCCCCCC, false);
        
        String customTextLabel = Text.translatable("gui.real-rail-transit-mod.arrival_display_screen.custom_text_label").getString();
        context.drawText(this.textRenderer, customTextLabel, centerX - 150, this.height / 2 + 15, 0xCCCCCC, false);
        
        String colorLabel = Text.translatable("gui.real-rail-transit-mod.arrival_display_screen.color_label").getString();
        context.drawText(this.textRenderer, colorLabel, centerX - 150, this.height / 2 + 45, 0xCCCCCC, false);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}

