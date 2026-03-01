package com.real.rail.transit.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * 追加包管理键位配置
 */
public class AddonManagerConfig {
    public static KeyBinding OPEN_ADDON_MANAGER_KEY;
    
    /**
     * 初始化键位绑定
     */
    public static void register() {
        OPEN_ADDON_MANAGER_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.real-rail-transit-mod.open_addon_manager",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F10, // F10 键
            "category.real-rail-transit-mod.addon_manager"
        ));
    }
}

