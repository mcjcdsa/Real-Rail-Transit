package com.real.rail.transit.client;

import com.real.rail.transit.entity.TrainEntity;
import com.real.rail.transit.network.ModNetworkPackets;
import com.real.rail.transit.registry.ModEntities;
import com.real.rail.transit.registry.ModItems;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;

/**
 * 列车控制处理器
 * 处理客户端键盘输入并发送到服务器
 */
public class TrainControlHandler {
    private static boolean lastForwardState = false;
    private static boolean lastBackwardState = false;
    private static boolean lastThrottleUpState = false;
    private static boolean lastThrottleDownState = false;
    private static boolean lastDoorLeftState = false;
    private static boolean lastDoorRightState = false;
    
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                return;
            }
            
            ClientPlayerEntity player = client.player;
            
            // 检查玩家是否手持钥匙
            if (!hasTrainKey(player)) {
                return;
            }
            
            // 查找附近的列车
            TrainEntity train = findNearbyTrain(player);
            if (train == null) {
                return;
            }
            
            // 检查键位状态变化并发送网络包
            checkAndSendControlInput(client, train);
        });
    }
    
    /**
     * 检查玩家是否手持列车钥匙
     */
    private static boolean hasTrainKey(PlayerEntity player) {
        // 检查主手和副手
        ItemStack mainHand = player.getStackInHand(Hand.MAIN_HAND);
        ItemStack offHand = player.getStackInHand(Hand.OFF_HAND);
        
        return mainHand.isOf(ModItems.SHIELD_DOOR_KEY) || offHand.isOf(ModItems.SHIELD_DOOR_KEY);
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
     * 检查键位状态并发送控制输入
     */
    private static void checkAndSendControlInput(MinecraftClient client, TrainEntity train) {
        // 检查键位绑定是否已初始化
        if (TrainControlConfig.FORWARD_KEY == null || TrainControlConfig.BACKWARD_KEY == null ||
            TrainControlConfig.THROTTLE_UP_KEY == null || TrainControlConfig.THROTTLE_DOWN_KEY == null ||
            TrainControlConfig.DOOR_LEFT_KEY == null || TrainControlConfig.DOOR_RIGHT_KEY == null) {
            return;
        }
        
        boolean forwardPressed = TrainControlConfig.FORWARD_KEY.isPressed();
        boolean backwardPressed = TrainControlConfig.BACKWARD_KEY.isPressed();
        boolean throttleUpPressed = TrainControlConfig.THROTTLE_UP_KEY.isPressed();
        boolean throttleDownPressed = TrainControlConfig.THROTTLE_DOWN_KEY.isPressed();
        boolean doorLeftPressed = TrainControlConfig.DOOR_LEFT_KEY.isPressed();
        boolean doorRightPressed = TrainControlConfig.DOOR_RIGHT_KEY.isPressed();
        
        // 前进/后退
        if (forwardPressed != lastForwardState) {
            sendControlPacket(train, ModNetworkPackets.TrainControlType.FORWARD, forwardPressed);
            lastForwardState = forwardPressed;
        }
        
        if (backwardPressed != lastBackwardState) {
            sendControlPacket(train, ModNetworkPackets.TrainControlType.BACKWARD, backwardPressed);
            lastBackwardState = backwardPressed;
        }
        
        // 油门调整
        if (throttleUpPressed != lastThrottleUpState) {
            sendControlPacket(train, ModNetworkPackets.TrainControlType.THROTTLE_UP, throttleUpPressed);
            lastThrottleUpState = throttleUpPressed;
        }
        
        if (throttleDownPressed != lastThrottleDownState) {
            sendControlPacket(train, ModNetworkPackets.TrainControlType.THROTTLE_DOWN, throttleDownPressed);
            lastThrottleDownState = throttleDownPressed;
        }
        
        // 车门控制
        if (doorLeftPressed && !lastDoorLeftState) {
            sendControlPacket(train, ModNetworkPackets.TrainControlType.DOOR_LEFT, true);
        }
        lastDoorLeftState = doorLeftPressed;
        
        if (doorRightPressed && !lastDoorRightState) {
            sendControlPacket(train, ModNetworkPackets.TrainControlType.DOOR_RIGHT, true);
        }
        lastDoorRightState = doorRightPressed;
    }
    
    /**
     * 发送控制数据包
     */
    private static void sendControlPacket(TrainEntity train, ModNetworkPackets.TrainControlType type, boolean pressed) {
        if (train != null) {
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                new ModNetworkPackets.TrainControlPayload(train.getId(), type, pressed)
            );
        }
    }
}

