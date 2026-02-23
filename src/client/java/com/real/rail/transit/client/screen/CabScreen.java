package com.real.rail.transit.client.screen;

import com.real.rail.transit.cab.CabInteractionSystem;
import com.real.rail.transit.entity.TrainEntity;
import com.real.rail.transit.sound.TrainSoundController;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

/**
 * 驾驶室GUI界面
 * 提供列车驾驶控制界面
 */
public class CabScreen extends Screen {
    private final PlayerEntity player;
    private final TrainEntity train;
    
    // 控制按钮
    private ButtonWidget startEngineButton;
    private ButtonWidget stopEngineButton;
    private ButtonWidget openDoorsButton;
    private ButtonWidget closeDoorsButton;
    private ButtonWidget emergencyBrakeButton;
    private ButtonWidget modeSwitchButton;
    private ButtonWidget speedUpButton;
    private ButtonWidget speedDownButton;
    private ButtonWidget hornButton;
    
    public CabScreen(PlayerEntity player, TrainEntity train) {
        super(Text.translatable("screen.real-rail-transit-mod.cab"));
        this.player = player;
        this.train = train;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // 启动/停止引擎按钮
        this.startEngineButton = ButtonWidget.builder(
            Text.translatable("button.real-rail-transit-mod.start_engine"),
            button -> {
                CabInteractionSystem.getInstance().handleButtonClick(player, CabInteractionSystem.ButtonType.START_ENGINE);
                TrainSoundController.playButtonClickSound(train.getBlockPos());
            }
        ).dimensions(centerX - 100, centerY - 80, 90, 20).build();
        
        this.stopEngineButton = ButtonWidget.builder(
            Text.translatable("button.real-rail-transit-mod.stop_engine"),
            button -> {
                CabInteractionSystem.getInstance().handleButtonClick(player, CabInteractionSystem.ButtonType.STOP_ENGINE);
                TrainSoundController.playButtonClickSound(train.getBlockPos());
            }
        ).dimensions(centerX + 10, centerY - 80, 90, 20).build();
        
        // 开关门按钮
        this.openDoorsButton = ButtonWidget.builder(
            Text.translatable("button.real-rail-transit-mod.open_doors"),
            button -> {
                CabInteractionSystem.getInstance().handleButtonClick(player, CabInteractionSystem.ButtonType.OPEN_DOORS);
                TrainSoundController.playButtonClickSound(train.getBlockPos());
            }
        ).dimensions(centerX - 100, centerY - 50, 90, 20).build();
        
        this.closeDoorsButton = ButtonWidget.builder(
            Text.translatable("button.real-rail-transit-mod.close_doors"),
            button -> {
                CabInteractionSystem.getInstance().handleButtonClick(player, CabInteractionSystem.ButtonType.CLOSE_DOORS);
                TrainSoundController.playButtonClickSound(train.getBlockPos());
            }
        ).dimensions(centerX + 10, centerY - 50, 90, 20).build();
        
        // 紧急制动按钮
        this.emergencyBrakeButton = ButtonWidget.builder(
            Text.translatable("button.real-rail-transit-mod.emergency_brake"),
            button -> {
                CabInteractionSystem.getInstance().handleButtonClick(player, CabInteractionSystem.ButtonType.EMERGENCY_BRAKE);
                TrainSoundController.playButtonClickSound(train.getBlockPos());
            }
        ).dimensions(centerX - 100, centerY - 20, 200, 20).build();
        
        // 模式切换按钮
        this.modeSwitchButton = ButtonWidget.builder(
            Text.translatable("button.real-rail-transit-mod.mode_switch"),
            button -> {
                CabInteractionSystem.getInstance().handleButtonClick(player, CabInteractionSystem.ButtonType.MODE_SWITCH);
                TrainSoundController.playButtonClickSound(train.getBlockPos());
            }
        ).dimensions(centerX - 100, centerY + 10, 90, 20).build();
        
        // 速度控制按钮
        this.speedUpButton = ButtonWidget.builder(
            Text.translatable("button.real-rail-transit-mod.speed_up"),
            button -> {
                CabInteractionSystem.getInstance().handleButtonClick(player, CabInteractionSystem.ButtonType.SPEED_UP);
                TrainSoundController.playButtonClickSound(train.getBlockPos());
            }
        ).dimensions(centerX - 100, centerY + 40, 90, 20).build();
        
        this.speedDownButton = ButtonWidget.builder(
            Text.translatable("button.real-rail-transit-mod.speed_down"),
            button -> {
                CabInteractionSystem.getInstance().handleButtonClick(player, CabInteractionSystem.ButtonType.SPEED_DOWN);
                TrainSoundController.playButtonClickSound(train.getBlockPos());
            }
        ).dimensions(centerX + 10, centerY + 40, 90, 20).build();
        
        // 鸣笛按钮
        this.hornButton = ButtonWidget.builder(
            Text.translatable("button.real-rail-transit-mod.horn"),
            button -> {
                CabInteractionSystem.getInstance().handleButtonClick(player, CabInteractionSystem.ButtonType.HORN);
                TrainSoundController.playHornSound(train);
            }
        ).dimensions(centerX - 100, centerY + 70, 200, 20).build();
        
        // 添加所有按钮到界面
        this.addDrawableChild(startEngineButton);
        this.addDrawableChild(stopEngineButton);
        this.addDrawableChild(openDoorsButton);
        this.addDrawableChild(closeDoorsButton);
        this.addDrawableChild(emergencyBrakeButton);
        this.addDrawableChild(modeSwitchButton);
        this.addDrawableChild(speedUpButton);
        this.addDrawableChild(speedDownButton);
        this.addDrawableChild(hornButton);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        
        // 绘制列车信息
        int centerX = this.width / 2;
        int infoY = 30;
        
        // 当前速度
        String speedText = String.format("速度: %.1f km/h", train.getCurrentSpeed() * 3.6);
        context.drawText(this.textRenderer, Text.literal(speedText), centerX - 100, infoY, 0xFFFFFF, false);
        
        // 驾驶模式
        String modeText = "模式: " + getModeText(train.getDrivingMode());
        context.drawText(this.textRenderer, Text.literal(modeText), centerX - 100, infoY + 15, 0xFFFFFF, false);
        
        // 制动状态
        String brakeText = "制动: " + getBrakeText(train.getBrakeState());
        context.drawText(this.textRenderer, Text.literal(brakeText), centerX - 100, infoY + 30, 0xFFFFFF, false);
    }
    
    private String getModeText(TrainEntity.DrivingMode mode) {
        return switch (mode) {
            case ATO -> "自动驾驶";
            case CBTC -> "CBTC";
            case ATP -> "ATP";
            case ATC -> "ATC";
            case RM -> "限制人工";
            case MANUAL -> "手动";
        };
    }
    
    private String getBrakeText(TrainEntity.BrakeState brakeState) {
        return switch (brakeState) {
            case NORMAL -> "正常";
            case QUICK -> "快速";
            case EMERGENCY -> "紧急";
        };
    }
    
    @Override
    public boolean shouldPause() {
        return false; // 驾驶时游戏不暂停
    }
}




