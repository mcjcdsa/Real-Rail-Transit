package com.real.rail.transit.client.screen;

import com.real.rail.transit.station.screen.StationConstructionControlPanelScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

/**
 * 车站建设控制面板GUI界面
 */
public class StationConstructionControlPanelScreen extends HandledScreen<StationConstructionControlPanelScreenHandler> {
    
    public StationConstructionControlPanelScreen(StationConstructionControlPanelScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }
    
    @Override
    protected void init() {
        super.init();
        
        // 添加关闭按钮
        ButtonWidget closeButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.close"),
            button -> this.close()
        ).dimensions(this.width / 2 - 50, this.height - 30, 100, 20).build();
        
        this.addDrawableChild(closeButton);
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
        
        // 绘制标题
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.title"),
            this.width / 2,
            20,
            0xFFFFFF
        );
        
        // 绘制提示信息
        context.drawTextWrapped(
            this.textRenderer,
            Text.translatable("gui.real-rail-transit-mod.station_construction_control_panel.info"),
            this.width / 2 - 150,
            60,
            300,
            0xCCCCCC
        );
    }
    
    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // 不绘制物品栏标签
    }
}

