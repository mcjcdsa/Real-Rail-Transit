package com.real.rail.transit.client;

import com.real.rail.transit.client.screen.AddonManagerScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

/**
 * 追加包管理处理器
 * 处理打开追加包管理界面的按键
 */
public class AddonManagerHandler {
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (AddonManagerConfig.OPEN_ADDON_MANAGER_KEY == null) {
                return;
            }
            
            if (AddonManagerConfig.OPEN_ADDON_MANAGER_KEY.wasPressed()) {
                MinecraftClient.getInstance().setScreen(
                    new AddonManagerScreen(MinecraftClient.getInstance().currentScreen)
                );
            }
        });
    }
}

