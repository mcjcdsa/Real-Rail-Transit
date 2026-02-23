package com.real.rail.transit.client.screen;

import com.real.rail.transit.network.ModNetworkPackets;
import com.real.rail.transit.station.entity.TicketMachineBlockEntity;
import com.real.rail.transit.station.screen.TicketMachineScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

/**
 * 售票机GUI界面
 */
public class TicketMachineScreen extends HandledScreen<TicketMachineScreenHandler> {
    private ButtonWidget buyTicketButton;
    private ButtonWidget setPriceButton;
    private TextFieldWidget priceField;
    private TicketMachineBlockEntity blockEntity;
    
    public TicketMachineScreen(TicketMachineScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.blockEntity = handler.getBlockEntity();
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // 票价输入框（支持更大的价格范围）
        this.priceField = new TextFieldWidget(
            this.textRenderer,
            centerX - 100, centerY - 40, 200, 20,
            Text.translatable("gui.real-rail-transit-mod.ticket_machine.price_field")
        );
        this.priceField.setMaxLength(5);
        this.priceField.setEditableColor(0xFFFFFF);
        if (blockEntity != null) {
            this.priceField.setText(String.valueOf(blockEntity.getTicketPrice()));
        }
        this.addDrawableChild(this.priceField);
        
        // 设置票价按钮
        this.setPriceButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.ticket_machine.set_price"),
            button -> this.setPrice()
        ).dimensions(centerX - 100, centerY - 10, 90, 20).build();
        this.addDrawableChild(this.setPriceButton);
        
        // 购买车票按钮
        this.buyTicketButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.ticket_machine.buy_ticket"),
            button -> this.buyTicket()
        ).dimensions(centerX + 10, centerY - 10, 90, 20).build();
        this.addDrawableChild(this.buyTicketButton);
        
        // 状态信息按钮
        ButtonWidget statusButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.ticket_machine.status"),
            button -> this.toggleStatus()
        ).dimensions(centerX - 50, centerY + 20, 100, 20).build();
        this.addDrawableChild(statusButton);
    }
    
    private void setPrice() {
        if (blockEntity != null && this.priceField != null) {
            try {
                int price = Integer.parseInt(this.priceField.getText());
                if (price > 0 && price <= 9999) { // 扩大价格范围
                    blockEntity.setTicketPrice(price);
                    // 发送数据包到服务器同步
                    if (this.client != null && this.client.player != null) {
                        ClientPlayNetworking.send(new ModNetworkPackets.TicketMachineSetPricePayload(
                            blockEntity.getPos(), price
                        ));
                    }
                }
            } catch (NumberFormatException e) {
                // 无效输入，忽略
            }
        }
    }
    
    private void buyTicket() {
        if (blockEntity != null && !blockEntity.isWorking()) {
            return; // 机器故障，无法购票
        }
        
        // 发送购票请求到服务器
        if (this.client != null && this.client.player != null && blockEntity != null) {
            ClientPlayNetworking.send(new ModNetworkPackets.TicketMachineBuyTicketPayload(
                blockEntity.getPos()
            ));
        }
    }
    
    private void toggleStatus() {
        if (blockEntity != null) {
            boolean newStatus = !blockEntity.isWorking();
            blockEntity.setWorking(newStatus);
            // 发送数据包到服务器同步
            if (this.client != null && this.client.player != null) {
                ClientPlayNetworking.send(new ModNetworkPackets.TicketMachineSetStatusPayload(
                    blockEntity.getPos(), newStatus
                ));
            }
        }
    }
    
    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // 绘制半透明背景
        context.fill(centerX - 200, centerY - 80, centerX + 200, centerY + 80, 0xC0101010);
        context.drawBorder(centerX - 200, centerY - 80, 400, 160, 0xFF404040);
    }
    
    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        int centerX = this.width / 2;
        
        // 绘制标题
        String title = Text.translatable("gui.real-rail-transit-mod.ticket_machine.title").getString();
        int titleWidth = this.textRenderer.getWidth(title);
        context.drawText(this.textRenderer, title, centerX - titleWidth / 2, this.height / 2 - 70, 0xFFFFFF, false);
        
        // 绘制票价标签
        String priceLabel = Text.translatable("gui.real-rail-transit-mod.ticket_machine.price_label").getString();
        context.drawText(this.textRenderer, priceLabel, centerX - 100, this.height / 2 - 55, 0xCCCCCC, false);
        
        // 绘制状态信息
        if (blockEntity != null) {
            String statusText = blockEntity.isWorking() 
                ? Text.translatable("gui.real-rail-transit-mod.ticket_machine.status_working").getString()
                : Text.translatable("gui.real-rail-transit-mod.ticket_machine.status_broken").getString();
            int statusColor = blockEntity.isWorking() ? 0xFF00FF00 : 0xFFFF0000;
            context.drawText(this.textRenderer, statusText, centerX - 100, this.height / 2 + 15, statusColor, false);
            
            // 绘制已售出票数
            String countText = Text.translatable("gui.real-rail-transit-mod.ticket_machine.sold_count", 
                blockEntity.getTicketCount()).getString();
            context.drawText(this.textRenderer, countText, centerX - 100, this.height / 2 + 30, 0xCCCCCC, false);
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}

