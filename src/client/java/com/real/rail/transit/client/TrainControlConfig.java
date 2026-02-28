package com.real.rail.transit.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * 列车控制键位配置
 * 可自定义的键位绑定
 */
public class TrainControlConfig {
    // 默认键位绑定
    public static KeyBinding FORWARD_KEY;
    public static KeyBinding BACKWARD_KEY;
    public static KeyBinding THROTTLE_UP_KEY;
    public static KeyBinding THROTTLE_DOWN_KEY;
    public static KeyBinding DOOR_LEFT_KEY;
    public static KeyBinding DOOR_RIGHT_KEY;
    
    /**
     * 初始化键位绑定
     */
    public static void register() {
        FORWARD_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.real-rail-transit-mod.train_forward",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_EQUAL, // = 键
            "category.real-rail-transit-mod.train_control"
        ));
        
        BACKWARD_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.real-rail-transit-mod.train_backward",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_MINUS, // - 键
            "category.real-rail-transit-mod.train_control"
        ));
        
        THROTTLE_UP_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.real-rail-transit-mod.train_throttle_up",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UP, // 上方向键
            "category.real-rail-transit-mod.train_control"
        ));
        
        THROTTLE_DOWN_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.real-rail-transit-mod.train_throttle_down",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_DOWN, // 下方向键
            "category.real-rail-transit-mod.train_control"
        ));
        
        DOOR_LEFT_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.real-rail-transit-mod.train_door_left",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT, // 左方向键
            "category.real-rail-transit-mod.train_control"
        ));
        
        DOOR_RIGHT_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.real-rail-transit-mod.train_door_right",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT, // 右方向键
            "category.real-rail-transit-mod.train_control"
        ));
    }
}

