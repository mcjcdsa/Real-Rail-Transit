package com.real.rail.transit.client.screen;

import com.real.rail.transit.network.ModNetworkPackets;
import com.real.rail.transit.station.screen.StationConstructionControlPanelScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * 车站建设控制面板GUI界面
 */
public class StationConstructionControlPanelScreen extends HandledScreen<StationConstructionControlPanelScreenHandler> {
    
    // 当前选择的设施类型
    private String selectedFacilityType = "shield_door"; // shield_door, display_screen, ticket_machine
    
    // 屏蔽门配置
    private boolean shieldDoorIsUpper = false;
    
    // 显示屏配置
    private String displayText = "欢迎乘坐轨道交通";
    private String displayColor = "#FFFFFF";
    private String displayScale = "1.0";
    
    // UI组件
    private ButtonWidget facilityTypeButton;
    private ButtonWidget shieldDoorTypeButton;
    private ButtonWidget shieldDoorConfigButton;
    private TextFieldWidget displayTextField;
    private TextFieldWidget displayColorField;
    private TextFieldWidget displayScaleField;
    private ButtonWidget displayConfigButton;
    private ButtonWidget batchApplyButton;
    private ButtonWidget closeButton;
    
    // 批量操作状态
    private BlockPos batchStartPos = null;
    private boolean batchMode = false;
    
    // 当前标签页：facility_select, shield_door, display_screen, batch
    private String currentTab = "facility_select";
    
    // 滚动相关
    private int scrollOffset = 0;
    private static final int SCROLL_SPEED = 20;
    
    public StationConstructionControlPanelScreen(StationConstructionControlPanelScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int startY = 50;
        int buttonWidth = 150;
        int buttonHeight = 20;
        int spacing = 25;
        
        // 标签页按钮
        ButtonWidget facilityTabButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.tab.facility"),
            button -> switchTab("facility_select")
        ).dimensions(centerX - 200, 30, 100, 20).build();
        this.addDrawableChild(facilityTabButton);
        
        ButtonWidget shieldDoorTabButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.tab.shield_door"),
            button -> switchTab("shield_door")
        ).dimensions(centerX - 100, 30, 100, 20).build();
        this.addDrawableChild(shieldDoorTabButton);
        
        ButtonWidget displayTabButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.tab.display"),
            button -> switchTab("display_screen")
        ).dimensions(centerX, 30, 100, 20).build();
        this.addDrawableChild(displayTabButton);
        
        ButtonWidget batchTabButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.tab.batch"),
            button -> switchTab("batch")
        ).dimensions(centerX + 100, 30, 100, 20).build();
        this.addDrawableChild(batchTabButton);
        
        // 设施选择标签页
        facilityTypeButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.facility_type." + selectedFacilityType),
            button -> cycleFacilityType()
        ).dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build();
        this.addDrawableChild(facilityTypeButton);
        
        // 屏蔽门配置标签页
        shieldDoorTypeButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.shield_door_type." + (shieldDoorIsUpper ? "upper" : "lower")),
            button -> cycleShieldDoorType()
        ).dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build();
        this.addDrawableChild(shieldDoorTypeButton);
        
        shieldDoorConfigButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.config_shield_door"),
            button -> configShieldDoor()
        ).dimensions(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight).build();
        this.addDrawableChild(shieldDoorConfigButton);
        
        // 显示屏配置标签页
        displayTextField = new TextFieldWidget(
            this.textRenderer,
            centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight,
            Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.display_text")
        );
        displayTextField.setMaxLength(100);
        displayTextField.setText(displayText);
        this.addDrawableChild(displayTextField);
        
        displayColorField = new TextFieldWidget(
            this.textRenderer,
            centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight,
            Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.display_color")
        );
        displayColorField.setMaxLength(8);
        displayColorField.setText(displayColor);
        this.addDrawableChild(displayColorField);
        
        displayScaleField = new TextFieldWidget(
            this.textRenderer,
            centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight,
            Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.display_scale")
        );
        displayScaleField.setMaxLength(4);
        displayScaleField.setText(displayScale);
        this.addDrawableChild(displayScaleField);
        
        displayConfigButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.config_display"),
            button -> configDisplay()
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 3, buttonWidth, buttonHeight).build();
        this.addDrawableChild(displayConfigButton);
        
        // 批量操作标签页
        batchApplyButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.batch_apply"),
            button -> toggleBatchMode()
        ).dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build();
        this.addDrawableChild(batchApplyButton);
        
        // 关闭按钮
        closeButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.close"),
            button -> this.close()
        ).dimensions(centerX - 50, this.height - 30, 100, 20).build();
        this.addDrawableChild(closeButton);
        
        updateTabVisibility();
    }
    
    private void switchTab(String tab) {
        currentTab = tab;
        updateTabVisibility();
    }
    
    private void updateTabVisibility() {
        facilityTypeButton.visible = currentTab.equals("facility_select");
        
        shieldDoorTypeButton.visible = currentTab.equals("shield_door");
        shieldDoorConfigButton.visible = currentTab.equals("shield_door");
        
        displayTextField.visible = currentTab.equals("display_screen");
        displayColorField.visible = currentTab.equals("display_screen");
        displayScaleField.visible = currentTab.equals("display_screen");
        displayConfigButton.visible = currentTab.equals("display_screen");
        
        batchApplyButton.visible = currentTab.equals("batch");
        
        // 更新位置以应用滚动
        updateComponentPositions();
    }
    
    private void updateComponentPositions() {
        int centerX = this.width / 2;
        int startY = 50;
        int spacing = 25;
        
        if (facilityTypeButton != null && currentTab.equals("facility_select")) {
            facilityTypeButton.setY(startY - scrollOffset);
        }
        
        if (shieldDoorTypeButton != null && currentTab.equals("shield_door")) {
            shieldDoorTypeButton.setY(startY - scrollOffset);
            shieldDoorConfigButton.setY(startY + spacing - scrollOffset);
        }
        
        if (displayTextField != null && currentTab.equals("display_screen")) {
            displayTextField.setY(startY - scrollOffset);
            displayColorField.setY(startY + spacing - scrollOffset);
            displayScaleField.setY(startY + spacing * 2 - scrollOffset);
            displayConfigButton.setY(startY + spacing * 3 - scrollOffset);
        }
        
        if (batchApplyButton != null && currentTab.equals("batch")) {
            batchApplyButton.setY(startY - scrollOffset);
        }
    }
    
    private void cycleFacilityType() {
        String[] types = {"shield_door", "display_screen", "ticket_machine"};
        int currentIndex = -1;
        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(selectedFacilityType)) {
                currentIndex = i;
                break;
            }
        }
        selectedFacilityType = types[(currentIndex + 1) % types.length];
        facilityTypeButton.setMessage(Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.facility_type." + selectedFacilityType));
    }
    
    private void cycleShieldDoorType() {
        shieldDoorIsUpper = !shieldDoorIsUpper;
        shieldDoorTypeButton.setMessage(Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.shield_door_type." + (shieldDoorIsUpper ? "upper" : "lower")));
    }
    
    private void configShieldDoor() {
        if (this.client != null && this.client.player != null) {
            BlockPos pos = this.client.player.getBlockPos();
            ClientPlayNetworking.send(new ModNetworkPackets.StationConstructionConfigShieldDoorPayload(pos, shieldDoorIsUpper));
        }
    }
    
    private void configDisplay() {
        if (this.client != null && this.client.player != null) {
            BlockPos pos = this.client.player.getBlockPos();
            String text = displayTextField.getText();
            Integer color = null;
            Float scale = null;
            
            try {
                String colorStr = displayColorField.getText().replace("#", "");
                color = Integer.parseInt(colorStr, 16) | 0xFF000000;
            } catch (NumberFormatException e) {
                // 忽略无效颜色
            }
            
            try {
                scale = Float.parseFloat(displayScaleField.getText());
            } catch (NumberFormatException e) {
                // 忽略无效缩放
            }
            
            ClientPlayNetworking.send(new ModNetworkPackets.StationConstructionConfigDisplayPayload(
                pos, text, color, scale
            ));
        }
    }
    
    private void toggleBatchMode() {
        batchMode = !batchMode;
        if (batchMode) {
            batchStartPos = this.client.player != null ? this.client.player.getBlockPos() : null;
            batchApplyButton.setMessage(Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.batch_apply_confirm"));
        } else {
            if (batchStartPos != null && this.client.player != null) {
                BlockPos endPos = this.client.player.getBlockPos();
                String config = "";
                
                if ("shield_door".equals(selectedFacilityType)) {
                    config = shieldDoorIsUpper ? "upper" : "lower";
                } else if ("display_screen".equals(selectedFacilityType)) {
                    config = displayTextField.getText() + "|" + displayColorField.getText().replace("#", "") + "|" + displayScaleField.getText();
                }
                
                ClientPlayNetworking.send(new ModNetworkPackets.StationConstructionBatchApplyPayload(
                    batchStartPos,
                    endPos,
                    selectedFacilityType,
                    config
                ));
            }
            batchStartPos = null;
            batchApplyButton.setMessage(Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.batch_apply"));
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
            Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.title"),
            centerX,
            10,
            0xFFFFFF
        );
        
        // 使用scissor来裁剪超出屏幕的内容
        int scissorY = 50;
        int scissorHeight = this.height - 100;
        context.enableScissor(0, scissorY, this.width, scissorY + scissorHeight);
        
        // 根据当前标签页绘制不同的标签（应用滚动偏移）
        if (currentTab.equals("facility_select")) {
            context.drawText(
                this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.facility_type.label"),
                centerX - 75,
                55 - scrollOffset,
                0xCCCCCC,
                false
            );
        } else if (currentTab.equals("shield_door")) {
            context.drawText(
                this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.shield_door_type.label"),
                centerX - 75,
                55 - scrollOffset,
                0xCCCCCC,
                false
            );
        } else if (currentTab.equals("display_screen")) {
            context.drawText(
                this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.display_text.label"),
                centerX - 75,
                45 - scrollOffset,
                0xCCCCCC,
                false
            );
            context.drawText(
                this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.display_color.label"),
                centerX - 75,
                70 - scrollOffset,
                0xCCCCCC,
                false
            );
            context.drawText(
                this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.display_scale.label"),
                centerX - 75,
                95 - scrollOffset,
                0xCCCCCC,
                false
            );
        }
        
        context.disableScissor();
        
        // 绘制批量操作提示（不滚动）
        if (batchMode && currentTab.equals("batch")) {
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.batch_mode_hint"),
                centerX,
                this.height - 60,
                0xFFFF00
            );
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // 处理滚动事件（允许在整个屏幕范围内滚动，但排除标签页按钮区域）
        if (mouseY > 50) { // 排除标签页按钮区域
            if (verticalAmount != 0) {
                scrollOffset -= (int) (verticalAmount * SCROLL_SPEED);
                scrollOffset = Math.max(0, scrollOffset);
                updateComponentPositions();
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
