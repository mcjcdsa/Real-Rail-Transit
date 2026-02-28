package com.real.rail.transit.client.screen;

import com.real.rail.transit.block.screen.ControlPanelScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * 控制面板 GUI 界面
 * 用于设置车站范围、车站信息、车厂、线路走向等
 */
public class ControlPanelScreen extends HandledScreen<ControlPanelScreenHandler> {

    // 区域数据
    public static class Area {
        public String name;
        public String type; // "station" 或 "depot"
        public int color;
        public List<int[]> points; // [x, z] 坐标列表
        
        public Area(String name, String type, int color) {
            this.name = name;
            this.type = type;
            this.color = color;
            this.points = new ArrayList<>();
        }
    }

    private List<Area> areas = new ArrayList<>();
    private Area currentDrawingArea = null;
    private String currentTab = "station"; // "station", "depot", "route"
    
    // UI组件
    private ButtonWidget stationTabButton;
    private ButtonWidget depotTabButton;
    private ButtonWidget routeTabButton;
    private TextFieldWidget nameField;
    private ButtonWidget colorButton;
    private ButtonWidget startDrawingButton;
    private ButtonWidget finishDrawingButton;
    private ButtonWidget clearButton;
    private ButtonWidget saveButton;
    private ButtonWidget closeButton;
    
    // 地图绘制区域
    private static final int MAP_X = 20;
    private static final int MAP_Y = 50;
    private static final int MAP_WIDTH = 200;
    private static final int MAP_HEIGHT = 150;
    
    // 颜色选择
    private int[] colors = {
        0xFF0000FF, // 蓝色
        0xFF00FF00, // 绿色
        0xFFFF0000, // 红色
        0xFFFFFF00, // 黄色
        0xFFFF00FF, // 紫色
        0xFF00FFFF, // 青色
        0xFFFFA500, // 橙色
        0xFF800080  // 紫色
    };
    private int selectedColorIndex = 0;

    public ControlPanelScreen(ControlPanelScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 240;
        this.backgroundWidth = 360;
    }

    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int leftX = MAP_X + MAP_WIDTH + 20;
        int startY = 50;
        
        // 标签页按钮
        stationTabButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.control_panel.tab.station"),
            button -> switchTab("station")
        ).dimensions(leftX, startY, 60, 20).build();
        this.addDrawableChild(stationTabButton);
        
        depotTabButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.control_panel.tab.depot"),
            button -> switchTab("depot")
        ).dimensions(leftX + 65, startY, 60, 20).build();
        this.addDrawableChild(depotTabButton);
        
        routeTabButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.control_panel.tab.route"),
            button -> switchTab("route")
        ).dimensions(leftX + 130, startY, 60, 20).build();
        this.addDrawableChild(routeTabButton);
        
        startY += 30;
        
        // 名称输入框
        nameField = new TextFieldWidget(
            this.textRenderer,
            leftX,
            startY,
            120,
            20,
            Text.translatable("gui.real-rail-transit-mod.control_panel.name")
        );
        nameField.setMaxLength(32);
        this.addDrawableChild(nameField);
        
        startY += 30;
        
        // 颜色选择按钮
        colorButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.control_panel.color"),
            button -> cycleColor()
        ).dimensions(leftX, startY, 120, 20).build();
        this.addDrawableChild(colorButton);
        updateColorButton();
        
        startY += 30;
        
        // 开始绘制按钮
        startDrawingButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.control_panel.start_drawing"),
            button -> startDrawing()
        ).dimensions(leftX, startY, 120, 20).build();
        this.addDrawableChild(startDrawingButton);
        
        startY += 25;
        
        // 完成绘制按钮
        finishDrawingButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.control_panel.finish_drawing"),
            button -> finishDrawing()
        ).dimensions(leftX, startY, 120, 20).build();
        this.addDrawableChild(finishDrawingButton);
        finishDrawingButton.active = false;
        
        startY += 25;
        
        // 清除按钮
        clearButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.control_panel.clear"),
            button -> clearCurrentArea()
        ).dimensions(leftX, startY, 120, 20).build();
        this.addDrawableChild(clearButton);
        
        startY += 30;
        
        // 保存按钮
        saveButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.control_panel.save"),
            button -> saveArea()
        ).dimensions(leftX, startY, 120, 20).build();
        this.addDrawableChild(saveButton);
        
        // 关闭按钮
        closeButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.close"),
            button -> this.close()
        ).dimensions(centerX - 40, this.height - 28, 80, 20).build();
        this.addDrawableChild(closeButton);
        
        updateTabButtons();
    }
    
    private void switchTab(String tab) {
        currentTab = tab;
        updateTabButtons();
        if (currentDrawingArea != null) {
            finishDrawing();
        }
    }
    
    private void updateTabButtons() {
        stationTabButton.active = !currentTab.equals("station");
        depotTabButton.active = !currentTab.equals("depot");
        routeTabButton.active = !currentTab.equals("route");
    }
    
    private void cycleColor() {
        selectedColorIndex = (selectedColorIndex + 1) % colors.length;
        updateColorButton();
    }
    
    private void updateColorButton() {
        int color = colors[selectedColorIndex];
        String colorName = String.format("#%06X", color & 0xFFFFFF);
        colorButton.setMessage(Text.translatable("gui.real-rail-transit-mod.control_panel.color").append(": " + colorName));
    }
    
    private void startDrawing() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            name = currentTab.equals("station") ? "车站" : (currentTab.equals("depot") ? "车厂" : "线路");
        }
        
        currentDrawingArea = new Area(name, currentTab, colors[selectedColorIndex]);
        startDrawingButton.active = false;
        finishDrawingButton.active = true;
        nameField.setEditable(false);
    }
    
    private void finishDrawing() {
        if (currentDrawingArea != null && currentDrawingArea.points.size() >= 3) {
            areas.add(currentDrawingArea);
            currentDrawingArea = null;
            startDrawingButton.active = true;
            finishDrawingButton.active = false;
            nameField.setEditable(true);
            nameField.setText("");
        }
    }
    
    private void clearCurrentArea() {
        if (currentDrawingArea != null) {
            currentDrawingArea.points.clear();
        }
    }
    
    private void saveArea() {
        // TODO: 保存区域数据到服务器
        if (currentDrawingArea != null && currentDrawingArea.points.size() >= 3) {
            finishDrawing();
        }
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

        // 标题
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.translatable("gui.real-rail-transit-mod.control_panel.title"),
            centerX,
            18,
            0xFFFFFF
        );

        // 绘制地图区域背景
        context.fill(MAP_X, MAP_Y, MAP_X + MAP_WIDTH, MAP_Y + MAP_HEIGHT, 0x80000000);
        context.drawBorder(MAP_X, MAP_Y, MAP_WIDTH, MAP_HEIGHT, 0xFF404040);
        
        // 绘制地图标题
        String mapTitle = currentTab.equals("station") ? "车站范围" : 
                         (currentTab.equals("depot") ? "车厂范围" : "线路走向");
        context.drawText(this.textRenderer, mapTitle, MAP_X + 5, MAP_Y - 12, 0xFFFFFF, false);
        
        // 绘制已保存的区域
        for (Area area : areas) {
            drawArea(context, area);
        }
        
        // 绘制当前正在绘制的区域
        if (currentDrawingArea != null) {
            drawArea(context, currentDrawingArea);
        }
        
        // 绘制说明文本
        int infoY = MAP_Y + MAP_HEIGHT + 10;
        context.drawText(this.textRenderer, 
            Text.translatable("gui.real-rail-transit-mod.control_panel.instruction"), 
            MAP_X, infoY, 0xAAAAAA, false);
    }
    
    private void drawArea(DrawContext context, Area area) {
        if (area.points.size() < 2) return;
        
        int color = area.color | 0x80000000; // 添加透明度
        
        // 绘制区域边界
        for (int i = 0; i < area.points.size(); i++) {
            int[] p1 = area.points.get(i);
            int[] p2 = area.points.get((i + 1) % area.points.size());
            
            // 将世界坐标转换为屏幕坐标（简化版本）
            int x1 = MAP_X + MAP_WIDTH / 2 + p1[0] / 10;
            int z1 = MAP_Y + MAP_HEIGHT / 2 + p1[1] / 10;
            int x2 = MAP_X + MAP_WIDTH / 2 + p2[0] / 10;
            int z2 = MAP_Y + MAP_HEIGHT / 2 + p2[1] / 10;
            
            // 确保坐标在范围内
            x1 = Math.max(MAP_X, Math.min(MAP_X + MAP_WIDTH, x1));
            z1 = Math.max(MAP_Y, Math.min(MAP_Y + MAP_HEIGHT, z1));
            x2 = Math.max(MAP_X, Math.min(MAP_X + MAP_WIDTH, x2));
            z2 = Math.max(MAP_Y, Math.min(MAP_Y + MAP_HEIGHT, z2));
            
            context.drawHorizontalLine(x1, x2, z1, color);
            context.drawVerticalLine(x1, z1, z2, color);
        }
        
        // 绘制区域名称
        if (!area.points.isEmpty()) {
            int[] firstPoint = area.points.get(0);
            int textX = MAP_X + MAP_WIDTH / 2 + firstPoint[0] / 10;
            int textY = MAP_Y + MAP_HEIGHT / 2 + firstPoint[1] / 10;
            if (textX >= MAP_X && textX <= MAP_X + MAP_WIDTH && 
                textY >= MAP_Y && textY <= MAP_Y + MAP_HEIGHT) {
                context.drawText(this.textRenderer, area.name, textX, textY, area.color, false);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 检查是否在地图区域内点击
        if (mouseX >= MAP_X && mouseX <= MAP_X + MAP_WIDTH &&
            mouseY >= MAP_Y && mouseY <= MAP_Y + MAP_HEIGHT &&
            currentDrawingArea != null && button == 0) {
            
            // 将屏幕坐标转换为世界坐标（简化版本）
            int worldX = (int) ((mouseX - MAP_X - MAP_WIDTH / 2) * 10);
            int worldZ = (int) ((mouseY - MAP_Y - MAP_HEIGHT / 2) * 10);
            
            currentDrawingArea.points.add(new int[]{worldX, worldZ});
            return true;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // 不绘制物品栏标签
    }
}

