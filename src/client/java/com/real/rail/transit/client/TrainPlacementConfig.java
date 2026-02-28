package com.real.rail.transit.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * 列车放置键位配置
 * 可自定义的放置按键绑定
 */
public class TrainPlacementConfig {
    // 放置按键（默认右键）
    public static KeyBinding PLACE_KEY;
    // 取消按键（默认潜行键）
    public static KeyBinding CANCEL_KEY;
    
    /**
     * 初始化键位绑定
     */
    public static void register() {
        PLACE_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.real-rail-transit-mod.train_place",
            InputUtil.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_2, // 右键
            "category.real-rail-transit-mod.train_placement"
        ));
        
        CANCEL_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.real-rail-transit-mod.train_place_cancel",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_SHIFT, // 左Shift键
            "category.real-rail-transit-mod.train_placement"
        ));
    }
}

