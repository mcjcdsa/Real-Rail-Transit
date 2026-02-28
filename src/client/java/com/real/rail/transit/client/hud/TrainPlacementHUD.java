package com.real.rail.transit.client.hud;

import com.real.rail.transit.addon.AddonManager;
import com.real.rail.transit.client.TrainPlacementConfig;
import com.real.rail.transit.client.TrainPlacementManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * 列车放置HUD显示
 * 在放置模式下显示提示信息
 */
public class TrainPlacementHUD {
    public static void register() {
        HudRenderCallback.EVENT.register((DrawContext context, float tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) {
                return;
            }
            
            TrainPlacementManager manager = TrainPlacementManager.getInstance();
            if (!manager.isPlacing()) {
                return;
            }
            
            ClientPlayerEntity player = client.player;
            TextRenderer textRenderer = client.textRenderer;
            int screenWidth = context.getScaledWindowWidth();
            int screenHeight = context.getScaledWindowHeight();
            
            // 获取选中的列车信息
            AddonManager.TrainConfig train = manager.getSelectedTrain();
            if (train == null) {
                return;
            }
            
            // 显示在屏幕中央下方
            int x = screenWidth / 2;
            int y = screenHeight - 80;
            
            // 绘制背景
            String trainName = train.train_name != null ? train.train_name : train.train_id;
            String statusText = manager.canPlace() 
                ? "可以放置" 
                : "无法放置";
            int statusColor = manager.canPlace() ? 0x00FF00 : 0xFF0000;
            
            int textWidth = Math.max(
                textRenderer.getWidth("列车: " + trainName),
                textRenderer.getWidth(statusText)
            );
            int padding = 10;
            int boxWidth = textWidth + padding * 2;
            int boxHeight = 60;
            int boxX = x - boxWidth / 2;
            int boxY = y;
            
            // 绘制半透明背景
            context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0x80000000);
            context.drawBorder(boxX, boxY, boxWidth, boxHeight, 0xFF404040);
            
            // 绘制列车名称
            String trainText = "列车: " + trainName;
            int trainTextWidth = textRenderer.getWidth(trainText);
            context.drawTextWithShadow(textRenderer, trainText, x - trainTextWidth / 2, boxY + 8, 0xFFFFFF);
            
            // 绘制状态
            int statusTextWidth = textRenderer.getWidth(statusText);
            context.drawTextWithShadow(textRenderer, statusText, x - statusTextWidth / 2, boxY + 22, statusColor);
            
            // 绘制提示
            String placeKeyName = "右键";
            String cancelKeyName = "Shift";
            
            if (TrainPlacementConfig.PLACE_KEY != null) {
                placeKeyName = TrainPlacementConfig.PLACE_KEY.getBoundKeyLocalizedText().getString();
                if (placeKeyName.isEmpty()) {
                    placeKeyName = "右键";
                }
            }
            
            if (TrainPlacementConfig.CANCEL_KEY != null) {
                cancelKeyName = TrainPlacementConfig.CANCEL_KEY.getBoundKeyLocalizedText().getString();
                if (cancelKeyName.isEmpty()) {
                    cancelKeyName = "Shift";
                }
            }
            
            if (manager.canPlace()) {
                String placeText = "按 " + placeKeyName + " 放置";
                int placeTextWidth = textRenderer.getWidth(placeText);
                context.drawTextWithShadow(textRenderer, placeText, x - placeTextWidth / 2, boxY + 36, 0xAAAAAA);
            }
            String cancelText = "按 " + cancelKeyName + " 取消";
            int cancelTextWidth = textRenderer.getWidth(cancelText);
            context.drawTextWithShadow(textRenderer, cancelText, x - cancelTextWidth / 2, boxY + 48, 0x888888);
        });
    }
}

