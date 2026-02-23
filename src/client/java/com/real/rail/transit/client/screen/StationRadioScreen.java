package com.real.rail.transit.client.screen;

import com.real.rail.transit.network.ModNetworkPackets;
import com.real.rail.transit.station.entity.StationRadioBlockEntity;
import com.real.rail.transit.station.screen.StationRadioScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * 车站广播器GUI界面
 */
public class StationRadioScreen extends HandledScreen<StationRadioScreenHandler> {
    private TextFieldWidget messageField;
    private TextFieldWidget soundIdField;
    private TextFieldWidget volumeField;
    private ButtonWidget playButton;
    private ButtonWidget stopButton;
    private ButtonWidget saveButton;
    private ButtonWidget testSoundButton;
    private StationRadioBlockEntity blockEntity;
    
    public StationRadioScreen(StationRadioScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.blockEntity = handler.getBlockEntity();
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // 广播消息输入框
        this.messageField = new TextFieldWidget(
            this.textRenderer,
            centerX - 150, centerY - 50, 300, 20,
            Text.translatable("gui.real-rail-transit-mod.station_radio.message_field")
        );
        this.messageField.setMaxLength(200);
        if (blockEntity != null) {
            this.messageField.setText(blockEntity.getBroadcastMessage());
        }
        this.addDrawableChild(this.messageField);
        
        // 音频ID输入框
        this.soundIdField = new TextFieldWidget(
            this.textRenderer,
            centerX - 150, centerY - 20, 220, 20,
            Text.translatable("gui.real-rail-transit-mod.station_radio.sound_id_field")
        );
        this.soundIdField.setMaxLength(100);
        if (blockEntity != null) {
            this.soundIdField.setText(blockEntity.getSoundId());
        }
        this.addDrawableChild(this.soundIdField);
        
        // 试听按钮（在音频ID输入框旁边，使用更醒目的样式）
        this.testSoundButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.station_radio.test_sound"),
            button -> this.testSound()
        ).dimensions(centerX + 80, centerY - 20, 70, 20).build();
        this.addDrawableChild(this.testSoundButton);
        
        // 为音频ID输入框添加双击试听功能（可选）
        // 注意：TextFieldWidget不直接支持双击事件，所以使用按钮更直观
        
        // 音量输入框
        this.volumeField = new TextFieldWidget(
            this.textRenderer,
            centerX - 150, centerY + 10, 140, 20,
            Text.translatable("gui.real-rail-transit-mod.station_radio.volume_field")
        );
        this.volumeField.setMaxLength(4);
        if (blockEntity != null) {
            this.volumeField.setText(String.format("%.2f", blockEntity.getVolume()));
        }
        this.addDrawableChild(this.volumeField);
        
        // 播放按钮
        this.playButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.station_radio.play"),
            button -> this.playBroadcast()
        ).dimensions(centerX + 10, centerY + 10, 60, 20).build();
        this.addDrawableChild(this.playButton);
        
        // 停止按钮
        this.stopButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.station_radio.stop"),
            button -> this.stopBroadcast()
        ).dimensions(centerX + 80, centerY + 10, 60, 20).build();
        this.addDrawableChild(this.stopButton);
        
        // 保存按钮
        this.saveButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.station_radio.save"),
            button -> this.saveSettings()
        ).dimensions(centerX - 50, centerY + 40, 100, 20).build();
        this.addDrawableChild(this.saveButton);
        
        // 设置焦点
        this.setInitialFocus(this.messageField);
    }
    
    private void playBroadcast() {
        if (blockEntity != null && this.client != null && this.client.player != null) {
            blockEntity.setPlaying(true);
            // 发送数据包到服务器同步
            ClientPlayNetworking.send(new ModNetworkPackets.StationRadioPlayPayload(
                blockEntity.getPos()
            ));
        }
    }
    
    private void stopBroadcast() {
        if (blockEntity != null && this.client != null && this.client.player != null) {
            blockEntity.setPlaying(false);
            // 发送数据包到服务器同步
            ClientPlayNetworking.send(new ModNetworkPackets.StationRadioStopPayload(
                blockEntity.getPos()
            ));
        }
    }
    
    /**
     * 试听音频功能
     * 根据音频ID输入框中的内容播放音频，用于预览效果
     */
    private void testSound() {
        if (this.soundIdField == null || this.soundIdField.getText().isEmpty()) {
            return;
        }
        
        String soundIdStr = this.soundIdField.getText().trim();
        if (soundIdStr.isEmpty()) {
            return;
        }
        
        try {
            // 解析音频ID（支持格式：modid:sound_name 或 sound_name）
            Identifier soundId;
            if (soundIdStr.contains(":")) {
                String[] parts = soundIdStr.split(":", 2);
                if (parts.length == 2) {
                    soundId = Identifier.of(parts[0], parts[1]);
                } else {
                    // 格式错误，使用默认命名空间
                    soundId = Identifier.of("real-rail-transit-mod", soundIdStr);
                }
            } else {
                // 如果没有命名空间，使用模组ID作为默认命名空间
                soundId = Identifier.of("real-rail-transit-mod", soundIdStr);
            }
            
            // 从注册表获取音效事件
            SoundEvent soundEvent = Registries.SOUND_EVENT.get(soundId);
            if (soundEvent == null) {
                // 如果找不到，尝试创建新的SoundEvent（用于动态音频）
                soundEvent = SoundEvent.of(soundId);
            }
            
            // 获取音量（从音量输入框或使用默认值）
            float volume = 1.0f;
            if (this.volumeField != null && !this.volumeField.getText().isEmpty()) {
                try {
                    volume = Float.parseFloat(this.volumeField.getText());
                    volume = Math.max(0.0f, Math.min(1.0f, volume));
                } catch (NumberFormatException e) {
                    // 使用默认音量
                    volume = blockEntity != null ? blockEntity.getVolume() : 1.0f;
                }
            } else if (blockEntity != null) {
                volume = blockEntity.getVolume();
            }
            
            // 获取播放位置（优先使用方块位置，否则使用玩家位置）
            BlockPos pos;
            if (blockEntity != null) {
                pos = blockEntity.getPos();
            } else if (MinecraftClient.getInstance().player != null) {
                pos = MinecraftClient.getInstance().player.getBlockPos();
            } else {
                pos = BlockPos.ORIGIN;
            }
            
            // 播放试听音效
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.getSoundManager() != null && client.world != null) {
                client.getSoundManager().play(
                    new PositionedSoundInstance(
                        soundEvent,
                        SoundCategory.BLOCKS,
                        volume,
                        1.0f, // 音调
                        client.world.getRandom(),
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5
                    )
                );
            }
        } catch (Exception e) {
            // 音频ID无效或播放失败，静默处理
            // 在实际使用中，可以在这里添加错误提示（如显示Toast消息）
        }
    }
    
    private void saveSettings() {
        if (blockEntity != null) {
            String message = this.messageField != null ? this.messageField.getText() : null;
            String soundId = this.soundIdField != null ? this.soundIdField.getText() : null;
            Float volume = null;
            
            if (this.volumeField != null) {
                try {
                    volume = Float.parseFloat(this.volumeField.getText());
                } catch (NumberFormatException e) {
                    // 无效输入，忽略
                }
            }
            
            // 更新本地BlockEntity
            if (message != null) blockEntity.setBroadcastMessage(message);
            if (soundId != null) blockEntity.setSoundId(soundId);
            if (volume != null) blockEntity.setVolume(volume);
            
            // 发送数据包到服务器同步
            if (this.client != null && this.client.player != null) {
                ClientPlayNetworking.send(new com.real.rail.transit.network.ModNetworkPackets.StationRadioUpdatePayload(
                    blockEntity.getPos(), message, soundId, volume
                ));
            }
            
            this.close();
        }
    }
    
    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // 绘制半透明背景
        context.fill(centerX - 180, centerY - 80, centerX + 180, centerY + 80, 0xC0101010);
        context.drawBorder(centerX - 180, centerY - 80, 360, 160, 0xFF404040);
    }
    
    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        int centerX = this.width / 2;
        
        // 绘制标题
        String title = Text.translatable("gui.real-rail-transit-mod.station_radio.title").getString();
        int titleWidth = this.textRenderer.getWidth(title);
        context.drawText(this.textRenderer, title, centerX - titleWidth / 2, this.height / 2 - 50, 0xFFFFFF, false);
        
        // 绘制标签
        String messageLabel = Text.translatable("gui.real-rail-transit-mod.station_radio.message_label").getString();
        context.drawText(this.textRenderer, messageLabel, centerX - 150, this.height / 2 - 65, 0xCCCCCC, false);
        
        String soundLabel = Text.translatable("gui.real-rail-transit-mod.station_radio.sound_id_label").getString();
        context.drawText(this.textRenderer, soundLabel, centerX - 150, this.height / 2 - 35, 0xCCCCCC, false);
        
        String volumeLabel = Text.translatable("gui.real-rail-transit-mod.station_radio.volume_label").getString();
        context.drawText(this.textRenderer, volumeLabel, centerX - 150, this.height / 2 - 5, 0xCCCCCC, false);
        
        // 绘制状态信息
        if (blockEntity != null) {
            String statusText = blockEntity.isPlaying()
                ? Text.translatable("gui.real-rail-transit-mod.station_radio.status_playing").getString()
                : Text.translatable("gui.real-rail-transit-mod.station_radio.status_stopped").getString();
            int statusColor = blockEntity.isPlaying() ? 0xFF00FF00 : 0xFFFF0000;
            context.drawText(this.textRenderer, statusText, centerX - 150, this.height / 2 + 25, statusColor, false);
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}

