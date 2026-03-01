package com.real.rail.transit.client.screen;

import com.real.rail.transit.RealRailTransitMod;
import com.real.rail.transit.block.entity.DaojiMachineBlockEntity;
import com.real.rail.transit.block.screen.DaojiMachineScreenHandler;
import com.real.rail.transit.network.ModNetworkPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * 盾构机配置 GUI 界面
 */
public class DaojiMachineScreen extends HandledScreen<DaojiMachineScreenHandler> {
    private BlockPos machinePos;
    private DaojiMachineBlockEntity blockEntity;
    
    // 配置字段
    private TextFieldWidget widthField;
    private TextFieldWidget heightField;
    private TextFieldWidget startXField, startYField, startZField;
    private TextFieldWidget endXField, endYField, endZField;
    private ButtonWidget typeButton;
    private ButtonWidget pathTypeButton;
    private ButtonWidget replaceBlockButton;
    private ButtonWidget wallBlockButton;
    private ButtonWidget directionButton;
    private ButtonWidget startButton;
    private ButtonWidget stopButton;
    
    private String tunnelType = "circular";
    private String pathType = "straight";
    private BlockState replaceBlock = Blocks.AIR.getDefaultState();
    private BlockState wallBlock = Blocks.STONE_BRICKS.getDefaultState();
    private Direction boringDirection = Direction.NORTH;
    
    public DaojiMachineScreen(DaojiMachineScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 280;
        this.backgroundWidth = 300;
        this.machinePos = handler.getPos();
    }
    
    @Override
    protected void init() {
        super.init();
        
        // 获取 BlockEntity
        if (this.client != null && this.client.world != null) {
            if (this.client.world.getBlockEntity(machinePos) instanceof DaojiMachineBlockEntity be) {
                this.blockEntity = be;
                this.tunnelType = be.getTunnelType();
                this.pathType = be.getPathType();
                this.replaceBlock = be.getReplaceBlock();
                this.wallBlock = be.getWallBlock();
                this.boringDirection = be.getBoringDirection();
            }
        }
        
        int centerX = this.width / 2;
        int startY = 30;
        int spacing = 20;
        
        // 隧道宽度
        widthField = new TextFieldWidget(
            this.textRenderer,
            centerX - 140,
            startY + 10,
            60,
            18,
            Text.literal("3")
        );
        widthField.setMaxLength(2);
        widthField.setTextPredicate(text -> text.matches("[0-9]*"));
        if (blockEntity != null) {
            widthField.setText(String.valueOf(blockEntity.getTunnelWidth()));
        }
        this.addDrawableChild(widthField);
        
        // 隧道高度
        heightField = new TextFieldWidget(
            this.textRenderer,
            centerX - 60,
            startY + 10,
            60,
            18,
            Text.literal("3")
        );
        heightField.setMaxLength(2);
        heightField.setTextPredicate(text -> text.matches("[0-9]*"));
        if (blockEntity != null) {
            heightField.setText(String.valueOf(blockEntity.getTunnelHeight()));
        }
        this.addDrawableChild(heightField);
        
        // 隧道类型
        typeButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.daoji_machine.type." + tunnelType),
            button -> {
                tunnelType = tunnelType.equals("circular") ? "rectangular" : "circular";
                typeButton.setMessage(Text.translatable("gui.real-rail-transit-mod.daoji_machine.type." + tunnelType));
                saveConfig();
            }
        ).dimensions(centerX + 20, startY + 10, 100, 18).build();
        this.addDrawableChild(typeButton);
        
        // 替换方块
        replaceBlockButton = ButtonWidget.builder(
            Text.literal(getBlockName(replaceBlock)),
            button -> {
                // 简单的方块选择（可以扩展为更复杂的界面）
                cycleReplaceBlock();
            }
        ).dimensions(centerX - 140, startY + 40, 120, 18).build();
        this.addDrawableChild(replaceBlockButton);
        
        // 隧道壁材质
        wallBlockButton = ButtonWidget.builder(
            Text.literal(getBlockName(wallBlock)),
            button -> {
                cycleWallBlock();
            }
        ).dimensions(centerX + 20, startY + 40, 120, 18).build();
        this.addDrawableChild(wallBlockButton);
        
        // 路径类型
        pathTypeButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.daoji_machine.path_type." + pathType),
            button -> {
                pathType = pathType.equals("straight") ? "curve" : "straight";
                pathTypeButton.setMessage(Text.translatable("gui.real-rail-transit-mod.daoji_machine.path_type." + pathType));
                saveConfig();
            }
        ).dimensions(centerX - 140, startY + 70, 120, 18).build();
        this.addDrawableChild(pathTypeButton);
        
        // 起点坐标
        int coordY = startY + 100;
        startXField = new TextFieldWidget(this.textRenderer, centerX - 140, coordY, 40, 16, Text.literal(""));
        startYField = new TextFieldWidget(this.textRenderer, centerX - 95, coordY, 40, 16, Text.literal(""));
        startZField = new TextFieldWidget(this.textRenderer, centerX - 50, coordY, 40, 16, Text.literal(""));
        if (blockEntity != null && blockEntity.getStartPos() != null) {
            BlockPos start = blockEntity.getStartPos();
            startXField.setText(String.valueOf(start.getX()));
            startYField.setText(String.valueOf(start.getY()));
            startZField.setText(String.valueOf(start.getZ()));
        }
        startXField.setMaxLength(8);
        startYField.setMaxLength(8);
        startZField.setMaxLength(8);
        this.addDrawableChild(startXField);
        this.addDrawableChild(startYField);
        this.addDrawableChild(startZField);
        
        // 终点坐标
        coordY += 25;
        endXField = new TextFieldWidget(this.textRenderer, centerX - 140, coordY, 40, 16, Text.literal(""));
        endYField = new TextFieldWidget(this.textRenderer, centerX - 95, coordY, 40, 16, Text.literal(""));
        endZField = new TextFieldWidget(this.textRenderer, centerX - 50, coordY, 40, 16, Text.literal(""));
        if (blockEntity != null && blockEntity.getEndPos() != null) {
            BlockPos end = blockEntity.getEndPos();
            endXField.setText(String.valueOf(end.getX()));
            endYField.setText(String.valueOf(end.getY()));
            endZField.setText(String.valueOf(end.getZ()));
        }
        endXField.setMaxLength(8);
        endYField.setMaxLength(8);
        endZField.setMaxLength(8);
        this.addDrawableChild(endXField);
        this.addDrawableChild(endYField);
        this.addDrawableChild(endZField);
        
        // 盾构方向
        directionButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.daoji_machine.direction." + boringDirection.getName()),
            button -> {
                // 方向通过指令设置，这里只显示
                this.client.player.sendMessage(Text.translatable("gui.real-rail-transit-mod.daoji_machine.direction.use_command"), false);
            }
        ).dimensions(centerX - 140, coordY + 25, 120, 18).build();
        this.addDrawableChild(directionButton);
        
        // 启动/停止按钮
        startButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.daoji_machine.start"),
            button -> {
                saveConfig();
                if (this.client != null && this.client.world != null) {
                    ClientPlayNetworking.send(new ModNetworkPackets.DaojiMachineStartPayload(machinePos));
                }
            }
        ).dimensions(centerX - 140, coordY + 50, 80, 20).build();
        this.addDrawableChild(startButton);
        
        stopButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.daoji_machine.stop"),
            button -> {
                if (this.client != null && this.client.world != null) {
                    ClientPlayNetworking.send(new ModNetworkPackets.DaojiMachineStopPayload(machinePos));
                }
            }
        ).dimensions(centerX + 20, coordY + 50, 80, 20).build();
        this.addDrawableChild(stopButton);
    }
    
    private void saveConfig() {
        if (blockEntity == null || this.client == null || this.client.world == null) return;
        
        try {
            int width = Integer.parseInt(widthField.getText());
            int height = Integer.parseInt(heightField.getText());
            
            blockEntity.setTunnelWidth(width);
            blockEntity.setTunnelHeight(height);
            blockEntity.setTunnelType(tunnelType);
            blockEntity.setPathType(pathType);
            blockEntity.setReplaceBlock(replaceBlock);
            blockEntity.setWallBlock(wallBlock);
            blockEntity.setBoringDirection(boringDirection);
            
            // 保存起点和终点
            try {
                if (!startXField.getText().isEmpty() && !startYField.getText().isEmpty() && !startZField.getText().isEmpty()) {
                    int sx = Integer.parseInt(startXField.getText());
                    int sy = Integer.parseInt(startYField.getText());
                    int sz = Integer.parseInt(startZField.getText());
                    blockEntity.setStartPos(new BlockPos(sx, sy, sz));
                }
            } catch (NumberFormatException e) {
                // 忽略无效输入
            }
            
            try {
                if (!endXField.getText().isEmpty() && !endYField.getText().isEmpty() && !endZField.getText().isEmpty()) {
                    int ex = Integer.parseInt(endXField.getText());
                    int ey = Integer.parseInt(endYField.getText());
                    int ez = Integer.parseInt(endZField.getText());
                    blockEntity.setEndPos(new BlockPos(ex, ey, ez));
                }
            } catch (NumberFormatException e) {
                // 忽略无效输入
            }
            
            blockEntity.markDirty();
            
            // 同步到服务器
            Identifier replaceId = Registries.BLOCK.getId(replaceBlock.getBlock());
            Identifier wallId = Registries.BLOCK.getId(wallBlock.getBlock());
            ClientPlayNetworking.send(new ModNetworkPackets.DaojiMachineConfigPayload(
                machinePos, width, height, tunnelType, 
                replaceId.toString(), wallId.toString(), boringDirection.getName()
            ));
        } catch (NumberFormatException e) {
            // 忽略无效输入
        }
    }
    
    private void cycleReplaceBlock() {
        Block[] commonBlocks = {
            Blocks.AIR, Blocks.STONE, Blocks.DIRT, Blocks.COBBLESTONE,
            Blocks.STONE_BRICKS, Blocks.BRICKS, Blocks.GLASS
        };
        int currentIndex = -1;
        for (int i = 0; i < commonBlocks.length; i++) {
            if (commonBlocks[i] == replaceBlock.getBlock()) {
                currentIndex = i;
                break;
            }
        }
        currentIndex = (currentIndex + 1) % commonBlocks.length;
        replaceBlock = commonBlocks[currentIndex].getDefaultState();
        replaceBlockButton.setMessage(Text.literal(getBlockName(replaceBlock)));
        saveConfig();
    }
    
    private void cycleWallBlock() {
        Block[] wallBlocks = {
            Blocks.STONE_BRICKS, Blocks.BRICKS, Blocks.COBBLESTONE,
            Blocks.STONE, Blocks.DEEPSLATE_BRICKS, Blocks.NETHER_BRICKS
        };
        int currentIndex = -1;
        for (int i = 0; i < wallBlocks.length; i++) {
            if (wallBlocks[i] == wallBlock.getBlock()) {
                currentIndex = i;
                break;
            }
        }
        currentIndex = (currentIndex + 1) % wallBlocks.length;
        wallBlock = wallBlocks[currentIndex].getDefaultState();
        wallBlockButton.setMessage(Text.literal(getBlockName(wallBlock)));
        saveConfig();
    }
    
    private String getBlockName(BlockState state) {
        Identifier id = Registries.BLOCK.getId(state.getBlock());
        String path = id.getPath();
        // 简单的名称格式化
        return path.replace("_", " ").substring(0, 1).toUpperCase() + 
               path.replace("_", " ").substring(1);
    }
    
    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.fill(0, 0, this.width, this.height, 0xC0101010);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        
        int centerX = this.width / 2;
        int startY = 30;
        
        // 标题
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.translatable("gui.real-rail-transit-mod.daoji_machine.title"),
            centerX,
            18,
            0xFFFFFF
        );
        
        // 标签
        context.drawText(this.textRenderer, Text.translatable("gui.real-rail-transit-mod.daoji_machine.width"), 
            centerX - 140, startY, 0xFFFFFF, false);
        context.drawText(this.textRenderer, Text.translatable("gui.real-rail-transit-mod.daoji_machine.height"), 
            centerX - 60, startY, 0xFFFFFF, false);
        context.drawText(this.textRenderer, Text.translatable("gui.real-rail-transit-mod.daoji_machine.start_pos"), 
            centerX - 140, startY + 100, 0xFFFFFF, false);
        context.drawText(this.textRenderer, Text.translatable("gui.real-rail-transit-mod.daoji_machine.end_pos"), 
            centerX - 140, startY + 125, 0xFFFFFF, false);
        
        // 状态信息
        if (blockEntity != null && blockEntity.isWorking()) {
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.daoji_machine.working")
                    .formatted(Formatting.GREEN),
                centerX,
                250,
                0xFFFFFF
            );
        }
    }
    
    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        // 更新 BlockEntity 引用
        if (this.client != null && this.client.world != null) {
            if (this.client.world.getBlockEntity(machinePos) instanceof DaojiMachineBlockEntity be) {
                this.blockEntity = be;
                if (directionButton != null) {
                    directionButton.setMessage(Text.translatable(
                        "gui.real-rail-transit-mod.daoji_machine.direction." + be.getBoringDirection().getName()));
                }
            }
        }
    }
}

