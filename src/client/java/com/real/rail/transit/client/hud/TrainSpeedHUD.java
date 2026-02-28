package com.real.rail.transit.client.hud;

import com.real.rail.transit.entity.TrainEntity;
import com.real.rail.transit.registry.ModEntities;
import com.real.rail.transit.registry.ModItems;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;

/**
 * 列车速度HUD显示
 * 在屏幕上显示列车当前速度、目标速度等信息
 */
public class TrainSpeedHUD {
    private static TrainEntity currentTrain = null;
    private static double currentSpeed = 0.0;
    private static double targetSpeed = 0.0;
    private static double maxSpeed = 0.0;
    
    public static void register() {
        HudRenderCallback.EVENT.register((DrawContext context, float tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) {
                return;
            }
            
            ClientPlayerEntity player = client.player;
            
            // 检查玩家是否手持钥匙
            if (!hasTrainKey(player)) {
                currentTrain = null;
                return;
            }
            
            // 查找附近的列车
            TrainEntity train = findNearbyTrain(player);
            if (train == null) {
                currentTrain = null;
                return;
            }
            
            currentTrain = train;
            renderSpeedHUD(context, client.textRenderer, train);
        });
    }
    
    /**
     * 检查玩家是否手持列车钥匙
     */
    private static boolean hasTrainKey(PlayerEntity player) {
        return player.getStackInHand(Hand.MAIN_HAND).isOf(ModItems.SHIELD_DOOR_KEY) ||
               player.getStackInHand(Hand.OFF_HAND).isOf(ModItems.SHIELD_DOOR_KEY);
    }
    
    /**
     * 查找玩家附近的列车
     */
    private static TrainEntity findNearbyTrain(PlayerEntity player) {
        // 检查玩家是否在列车上
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof TrainEntity train) {
            return train;
        }
        
        // 检查附近是否有列车（5格范围内）
        Box searchBox = new Box(
            player.getX() - 5, player.getY() - 2, player.getZ() - 5,
            player.getX() + 5, player.getY() + 3, player.getZ() + 5
        );
        
        var trains = player.getWorld().getEntitiesByType(
            ModEntities.TRAIN,
            searchBox,
            entity -> true
        );
        
        if (!trains.isEmpty()) {
            return trains.get(0);
        }
        
        return null;
    }
    
    /**
     * 渲染速度HUD
     */
    private static void renderSpeedHUD(DrawContext context, TextRenderer textRenderer, TrainEntity train) {
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        
        // HUD位置（右上角）
        int x = screenWidth - 200;
        int y = 20;
        
        // 获取速度信息（转换为 km/h）
        double currentSpeedKmh = train.getCurrentSpeed() * 3.6;
        double targetSpeedKmh = train.getTargetSpeed() * 3.6;
        double maxSpeedKmh = train.getMaxSpeed() * 3.6;
        
        // 绘制背景
        context.fill(x - 5, y - 5, x + 190, y + 80, 0x80000000);
        context.drawBorder(x - 5, y - 5, 195, 85, 0xFF404040);
        
        // 标题
        context.drawText(textRenderer, "列车速度", x, y, 0xFFFFFF, false);
        
        y += 15;
        
        // 当前速度（大号字体）
        String speedText = String.format("%.1f km/h", currentSpeedKmh);
        int speedColor = 0xFFFFFF;
        if (currentSpeedKmh > maxSpeedKmh * 0.9) {
            speedColor = 0xFFFF00; // 黄色：接近最高速度
        } else if (currentSpeedKmh > 0) {
            speedColor = 0x00FF00; // 绿色：正常运行
        } else {
            speedColor = 0x808080; // 灰色：停止
        }
        context.drawText(textRenderer, speedText, x, y, speedColor, false);
        
        y += 20;
        
        // 目标速度
        context.drawText(textRenderer, String.format("目标: %.1f km/h", targetSpeedKmh), x, y, 0xAAAAAA, false);
        
        y += 15;
        
        // 最高速度
        context.drawText(textRenderer, String.format("最高: %.1f km/h", maxSpeedKmh), x, y, 0x888888, false);
        
        y += 15;
        
        // 速度条
        int barWidth = 180;
        int barHeight = 8;
        int barX = x;
        int barY = y;
        
        // 背景
        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF202020);
        
        // 当前速度条（防止除零）
        if (maxSpeedKmh > 0.01) {
            int speedBarWidth = (int) ((currentSpeedKmh / maxSpeedKmh) * barWidth);
            speedBarWidth = Math.min(Math.max(0, speedBarWidth), barWidth);
            int speedBarColor = speedColor;
            context.fill(barX, barY, barX + speedBarWidth, barY + barHeight, speedBarColor | 0xFF000000);
            
            // 目标速度标记
            int targetX = (int) (barX + (targetSpeedKmh / maxSpeedKmh) * barWidth);
            targetX = Math.min(Math.max(barX, targetX), barX + barWidth);
            context.fill(targetX - 1, barY - 2, targetX + 1, barY + barHeight + 2, 0xFFFFFFFF);
        }
    }
    
    /**
     * 更新速度信息（从网络包）
     */
    public static void updateSpeed(int trainId, double currentSpeed, double targetSpeed, double maxSpeed) {
        if (currentTrain != null && currentTrain.getId() == trainId) {
            TrainSpeedHUD.currentSpeed = currentSpeed;
            TrainSpeedHUD.targetSpeed = targetSpeed;
            TrainSpeedHUD.maxSpeed = maxSpeed;
        }
    }
}

