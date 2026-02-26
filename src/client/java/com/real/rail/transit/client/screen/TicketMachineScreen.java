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
    
    // 滚动相关
    private int scrollOffset = 0;
    private static final int SCROLL_SPEED = 20;
    
    public TicketMachineScreen(TicketMachineScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.blockEntity = handler.getBlockEntity();
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int startY = 50;
        
        // 票价输入框（支持更大的价格范围，应用滚动偏移）
        this.priceField = new TextFieldWidget(
            this.textRenderer,
            centerX - 100, startY + 20 - scrollOffset, 200, 20,
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
        ).dimensions(centerX - 100, startY + 50 - scrollOffset, 90, 20).build();
        this.addDrawableChild(this.setPriceButton);
        
        // 购买车票按钮
        this.buyTicketButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.ticket_machine.buy_ticket"),
            button -> this.buyTicket()
        ).dimensions(centerX + 10, startY + 50 - scrollOffset, 90, 20).build();
        this.addDrawableChild(this.buyTicketButton);
        
        // 状态切换按钮
        ButtonWidget statusButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.ticket_machine.status"),
            button -> this.toggleStatus()
        ).dimensions(centerX - 50, startY + 140 - scrollOffset, 100, 20).build();
        this.addDrawableChild(statusButton);
        
        // 关闭按钮
        ButtonWidget closeButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.close"),
            button -> this.close()
        ).dimensions(centerX - 50, this.height - 30, 100, 20).build();
        this.addDrawableChild(closeButton);
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
        // 绘制半透明黑色背景
        context.fill(0, 0, this.width, this.height, 0xC0101010);
    }
    
    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // 不绘制物品栏标签
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        
        int centerX = this.width / 2;
        int startY = 50;
        
        // 绘制标题
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.translatable("gui.real-rail-transit-mod.ticket_machine.title"),
            centerX,
            20,
            0xFFFFFF
        );
        
        // 使用scissor来裁剪超出屏幕的内容
        int scissorY = 40;
        int scissorHeight = this.height - 100;
        context.enableScissor(0, scissorY, this.width, scissorY + scissorHeight);
        
        // 绘制票价标签（应用滚动偏移）
        context.drawText(
            this.textRenderer,
            Text.translatable("gui.real-rail-transit-mod.ticket_machine.price_label"),
            centerX - 100,
            startY - scrollOffset,
            0xCCCCCC,
            false
        );
        
        // 绘制状态信息
        if (blockEntity != null) {
            String statusText = blockEntity.isWorking() 
                ? Text.translatable("gui.real-rail-transit-mod.ticket_machine.status_working").getString()
                : Text.translatable("gui.real-rail-transit-mod.ticket_machine.status_broken").getString();
            int statusColor = blockEntity.isWorking() ? 0x00FF00 : 0xFF0000;
            context.drawText(
                this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.ticket_machine.status_label"),
                centerX - 100,
                startY + 80 - scrollOffset,
                0xCCCCCC,
                false
            );
            context.drawText(
                this.textRenderer,
                statusText,
                centerX - 100,
                startY + 95 - scrollOffset,
                statusColor,
                false
            );
            
            // 绘制已售出票数
            context.drawText(
                this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.ticket_machine.sold_count", 
                    blockEntity.getTicketCount()),
                centerX - 100,
                startY + 115 - scrollOffset,
                0xCCCCCC,
                false
            );
        }
        
        context.disableScissor();
        
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // 处理滚动事件（允许在整个屏幕范围内滚动）
        if (verticalAmount != 0) {
            scrollOffset -= (int) (verticalAmount * SCROLL_SPEED);
            scrollOffset = Math.max(0, scrollOffset);
            
            // 更新组件位置
            int centerX = this.width / 2;
            int startY = 50;
            if (priceField != null) priceField.setY(startY + 20 - scrollOffset);
            if (setPriceButton != null) setPriceButton.setY(startY + 50 - scrollOffset);
            if (buyTicketButton != null) buyTicketButton.setY(startY + 50 - scrollOffset);
            
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}

