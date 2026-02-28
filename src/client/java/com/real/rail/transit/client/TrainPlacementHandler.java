package com.real.rail.transit.client;

import com.real.rail.transit.network.ModNetworkPackets;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

/**
 * 列车放置处理器
 * 处理客户端放置模式的点击事件
 */
public class TrainPlacementHandler {
    private static boolean lastPlaceKeyState = false;
    
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                return;
            }
            
            TrainPlacementManager manager = TrainPlacementManager.getInstance();
            if (!manager.isPlacing()) {
                lastPlaceKeyState = false;
                return;
            }
            
            ClientPlayerEntity player = client.player;
            
            // 检查键位绑定是否已初始化
            if (TrainPlacementConfig.PLACE_KEY == null || TrainPlacementConfig.CANCEL_KEY == null) {
                return;
            }
            
            // 检查放置按键（支持鼠标右键键位绑定，也支持直接检测 useKey）
            boolean placeKeyPressed = TrainPlacementConfig.PLACE_KEY.isPressed() || 
                                     (client.options.useKey.isPressed() && client.currentScreen == null);
            
            // 检测按键按下（而不是持续按下）
            if (placeKeyPressed && !lastPlaceKeyState) {
                // 获取预览位置（轨道上方）
                var previewPos = manager.getPreviewPos();
                
                if (previewPos != null && manager.getSelectedTrainId() != null && manager.canPlace()) {
                    // 阻止默认的右键操作（使用物品/与方块交互）
                    // 发送放置请求到服务器
                    net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                        new ModNetworkPackets.PlaceTrainPayload(previewPos, manager.getSelectedTrainId())
                    );
                    
                    // 取消放置模式
                    manager.cancelPlacing();
                }
            }
            lastPlaceKeyState = placeKeyPressed;
            
            // 检查取消键（默认潜行键，可自定义）
            if (TrainPlacementConfig.CANCEL_KEY.isPressed() || 
                (client.options.sneakKey.isPressed() && client.currentScreen == null)) {
                manager.cancelPlacing();
            }
        });
    }
}

