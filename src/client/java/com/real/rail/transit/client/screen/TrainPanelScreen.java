package com.real.rail.transit.client.screen;

import com.real.rail.transit.addon.AddonManager;
import com.real.rail.transit.block.screen.TrainPanelScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 列车面板 GUI 界面
 * 显示从追加包导入的列车列表，可选择列车、查看编组信息
 * 与追加包创建器网站对应
 */
public class TrainPanelScreen extends HandledScreen<TrainPanelScreenHandler> {

    private List<AddonManager.TrainConfig> allTrains = List.of();
    private List<AddonManager.TrainConfig> filteredTrains = List.of();
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private static final int LINE_HEIGHT = 26;
    private static final int VISIBLE_ROWS = 7;
    private static final int PANEL_WIDTH = 320;
    private static final int DETAIL_PANEL_WIDTH = 300;
    private static final int SCROLLBAR_WIDTH = 4;

    private ButtonWidget refreshButton;
    private ButtonWidget closeButton;
    private ButtonWidget placeButton;
    private TextFieldWidget searchField;
    private String searchText = "";
    private boolean isDraggingScrollbar = false;

    public TrainPanelScreen(TrainPanelScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 280;
        this.backgroundWidth = 360;
    }

    @Override
    protected void init() {
        super.init();
        loadTrains();

        int centerX = this.width / 2;
        int buttonY = this.height - 28;

        // 搜索框
        searchField = new TextFieldWidget(
            this.textRenderer,
            centerX - PANEL_WIDTH / 2 + 4,
            42,
            PANEL_WIDTH - 8,
            16,
            Text.translatable("gui.real-rail-transit-mod.train_panel.search")
        );
        searchField.setChangedListener(text -> {
            searchText = text;
            filterTrains();
        });
        searchField.setMaxLength(50);
        this.addDrawableChild(searchField);

        refreshButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.train_panel.refresh"),
            button -> {
                loadTrains();
                searchField.setText("");
                searchText = "";
            }
        ).dimensions(centerX - 85, buttonY, 80, 20).build();
        this.addDrawableChild(refreshButton);

        closeButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.close"),
            button -> this.close()
        ).dimensions(centerX + 5, buttonY, 80, 20).build();
        this.addDrawableChild(closeButton);
        
        // 放置列车按钮
        placeButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.train_panel.place_train"),
            button -> {
                if (selectedIndex >= 0 && selectedIndex < filteredTrains.size()) {
                    AddonManager.TrainConfig train = filteredTrains.get(selectedIndex);
                    com.real.rail.transit.client.TrainPlacementManager.getInstance().startPlacing(train.train_id);
                    this.close();
                }
            }
        ).dimensions(centerX - 170, buttonY, 80, 20).build();
        this.addDrawableChild(placeButton);
    }

    private void loadTrains() {
        allTrains = AddonManager.getInstance().getLoadedAddons();
        filterTrains();
    }

    private void filterTrains() {
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredTrains = new ArrayList<>(allTrains);
        } else {
            String searchLower = searchText.toLowerCase();
            filteredTrains = allTrains.stream()
                .filter(train -> 
                    (train.train_name != null && train.train_name.toLowerCase().contains(searchLower)) ||
                    (train.train_id != null && train.train_id.toLowerCase().contains(searchLower))
                )
                .collect(Collectors.toList());
        }
        // 重置选中索引
        if (selectedIndex >= filteredTrains.size()) {
            selectedIndex = filteredTrains.isEmpty() ? -1 : filteredTrains.size() - 1;
        }
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, filteredTrains.size() - VISIBLE_ROWS)));
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
        int panelLeft = centerX - PANEL_WIDTH / 2;
        int startY = 62;

        // 标题
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.translatable("gui.real-rail-transit-mod.train_panel.title"),
            centerX,
            18,
            0xFFFFFF
        );

        // 副标题
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.translatable("gui.real-rail-transit-mod.train_panel.subtitle"),
            centerX,
            30,
            0xAAAAAA
        );

        // 列车列表区域背景
        int listTop = startY;
        int listHeight = LINE_HEIGHT * VISIBLE_ROWS;
        context.fill(panelLeft, listTop, panelLeft + PANEL_WIDTH, listTop + listHeight, 0x80000000);
        context.drawBorder(panelLeft, listTop, PANEL_WIDTH, listHeight, 0xFF404040);

        if (filteredTrains.isEmpty()) {
            String message = allTrains.isEmpty() 
                ? Text.translatable("gui.real-rail-transit-mod.train_panel.no_trains").getString()
                : Text.translatable("gui.real-rail-transit-mod.train_panel.no_search_results").getString();
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(message),
                centerX,
                listTop + listHeight / 2 - 5,
                0x888888
            );
            if (allTrains.isEmpty()) {
                context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.translatable("gui.real-rail-transit-mod.train_panel.import_hint"),
                    centerX,
                    listTop + listHeight / 2 + 12,
                    0x666666
                );
            }
        } else {
            int maxScroll = Math.max(0, filteredTrains.size() - VISIBLE_ROWS);
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

            for (int i = 0; i < VISIBLE_ROWS; i++) {
                int idx = scrollOffset + i;
                if (idx >= filteredTrains.size()) break;

                AddonManager.TrainConfig train = filteredTrains.get(idx);
                int y = listTop + i * LINE_HEIGHT + 2;
                boolean hovered = mouseX >= panelLeft && mouseX <= panelLeft + PANEL_WIDTH
                    && mouseY >= y && mouseY <= y + LINE_HEIGHT;
                boolean selected = idx == selectedIndex;

                int bgColor = selected ? 0x6040A0FF : (hovered ? 0x40303030 : 0);
                if (bgColor != 0) {
                    context.fill(panelLeft + 2, y, panelLeft + PANEL_WIDTH - 2, y + LINE_HEIGHT - 2, bgColor);
                }

                // 内置标签
                if (train.is_builtin) {
                    context.fill(panelLeft + 2, y, panelLeft + 4, y + LINE_HEIGHT - 2, 0xFF00FF00);
                }

                // 列车名称
                String displayName = train.train_name != null ? train.train_name : train.train_id;
                Text nameText = Text.literal(displayName)
                    .formatted(selected ? Formatting.YELLOW : Formatting.WHITE);
                int nameX = panelLeft + (train.is_builtin ? 10 : 6);
                context.drawText(this.textRenderer, nameText, nameX, y + 2, 0xFFFFFF, false);

                // 内置标签文本
                if (train.is_builtin) {
                    Text builtinText = Text.literal("[内置]").formatted(Formatting.GREEN);
                    int builtinWidth = this.textRenderer.getWidth(builtinText);
                    context.drawText(this.textRenderer, builtinText, nameX + this.textRenderer.getWidth(displayName) + 4, y + 2, 0x00FF00, false);
                }

                // 列车 ID 和编组信息
                String info = String.format("ID: %s | %d节 | %.0f km/h", train.train_id, train.car_count, train.max_speed);
                context.drawText(this.textRenderer, info, nameX, y + 13, 0xAAAAAA, false);
            }

            // 滚动条
            if (maxScroll > 0) {
                int scrollBarX = panelLeft + PANEL_WIDTH - SCROLLBAR_WIDTH - 2;
                // 计算滚动条滑块高度（根据可见行数和总行数的比例）
                float visibleRatio = (float) VISIBLE_ROWS / filteredTrains.size();
                int thumbHeight = Math.max(20, (int) (listHeight * visibleRatio));
                // 计算滑块位置
                float scrollRatio = maxScroll > 0 ? (float) scrollOffset / maxScroll : 0;
                int thumbY = listTop + (int) (scrollRatio * (listHeight - thumbHeight));
                
                // 绘制滚动条背景
                context.fill(scrollBarX, listTop, scrollBarX + SCROLLBAR_WIDTH, listTop + listHeight, 0x80000000);
                // 绘制滚动条滑块
                int thumbColor = isDraggingScrollbar ? 0xFFA0A0A0 : 0xFF808080;
                context.fill(scrollBarX, thumbY, scrollBarX + SCROLLBAR_WIDTH, thumbY + thumbHeight, thumbColor);
            }
        }

        // 更新放置按钮状态
        if (placeButton != null) {
            placeButton.active = selectedIndex >= 0 && selectedIndex < filteredTrains.size();
        }
        
        // 选中列车详情面板
        if (selectedIndex >= 0 && selectedIndex < filteredTrains.size()) {
            AddonManager.TrainConfig train = filteredTrains.get(selectedIndex);
            int detailPanelLeft = centerX - DETAIL_PANEL_WIDTH / 2;
            int detailY = listTop + listHeight + 8;
            int detailPanelHeight = 80;
            
            // 详情面板背景
            context.fill(detailPanelLeft, detailY, detailPanelLeft + DETAIL_PANEL_WIDTH, detailY + detailPanelHeight, 0x90000000);
            context.drawBorder(detailPanelLeft, detailY, DETAIL_PANEL_WIDTH, detailPanelHeight, 0xFF404040);
            
            // 详情标题
            context.drawText(this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.train_panel.detail_title").formatted(Formatting.YELLOW), 
                detailPanelLeft + 6, detailY + 4, 0xFFFF00, false);
            
            detailY += 16;
            // 最高速度
            context.drawText(this.textRenderer, 
                Text.translatable("gui.real-rail-transit-mod.train_panel.detail_speed", train.max_speed), 
                detailPanelLeft + 6, detailY, 0xCCCCCC, false);
            
            // 编组信息
            context.drawText(this.textRenderer, 
                Text.translatable("gui.real-rail-transit-mod.train_panel.detail_cars", train.car_count, train.car_length), 
                detailPanelLeft + 6, detailY + 12, 0xCCCCCC, false);
            
            // 供电方式
            Text powerText = "catenary".equals(train.power_type)
                ? Text.translatable("gui.real-rail-transit-mod.train_panel.power_catenary")
                : Text.translatable("gui.real-rail-transit-mod.train_panel.power_third_rail");
            context.drawText(this.textRenderer, 
                Text.translatable("gui.real-rail-transit-mod.train_panel.detail_power", powerText.getString()), 
                detailPanelLeft + 6, detailY + 24, 0xCCCCCC, false);
            
            // 加速和减速
            context.drawText(this.textRenderer, 
                String.format("加速: %.2f m/s² | 减速: %.2f m/s²", train.acceleration, train.deceleration), 
                detailPanelLeft + 6, detailY + 36, 0xAAAAAA, false);
            
            // 列车ID
            context.drawText(this.textRenderer, 
                String.format("ID: %s", train.train_id), 
                detailPanelLeft + 6, detailY + 48, 0x888888, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = this.width / 2;
        int panelLeft = centerX - PANEL_WIDTH / 2;
        int listTop = 62;
        int listHeight = LINE_HEIGHT * VISIBLE_ROWS;

        // 检查是否点击滚动条
        if (!filteredTrains.isEmpty()) {
            int maxScroll = Math.max(0, filteredTrains.size() - VISIBLE_ROWS);
            if (maxScroll > 0) {
                int scrollBarX = panelLeft + PANEL_WIDTH - SCROLLBAR_WIDTH - 2;
                float visibleRatio = (float) VISIBLE_ROWS / filteredTrains.size();
                int thumbHeight = Math.max(20, (int) (listHeight * visibleRatio));
                float scrollRatio = maxScroll > 0 ? (float) scrollOffset / maxScroll : 0;
                int thumbY = listTop + (int) (scrollRatio * (listHeight - thumbHeight));
                
                if (mouseX >= scrollBarX && mouseX <= scrollBarX + SCROLLBAR_WIDTH
                    && mouseY >= listTop && mouseY <= listTop + listHeight) {
                    // 点击滚动条区域
                    if (mouseY < thumbY) {
                        // 点击滑块上方，向上滚动一页
                        scrollOffset = Math.max(0, scrollOffset - VISIBLE_ROWS);
                    } else if (mouseY > thumbY + thumbHeight) {
                        // 点击滑块下方，向下滚动一页
                        scrollOffset = Math.min(maxScroll, scrollOffset + VISIBLE_ROWS);
                    } else {
                        // 点击滑块，开始拖动
                        isDraggingScrollbar = true;
                    }
                    return true;
                }
            }
        }

        // 检查是否点击列表项
        if (mouseX >= panelLeft && mouseX <= panelLeft + PANEL_WIDTH - SCROLLBAR_WIDTH - 4
            && mouseY >= listTop && mouseY <= listTop + listHeight && !filteredTrains.isEmpty()) {
            int row = (int) ((mouseY - listTop) / LINE_HEIGHT);
            int idx = scrollOffset + row;
            if (idx >= 0 && idx < filteredTrains.size()) {
                selectedIndex = idx;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int centerX = this.width / 2;
        int panelLeft = centerX - PANEL_WIDTH / 2;
        int listTop = 62;
        int listHeight = LINE_HEIGHT * VISIBLE_ROWS;

        // 鼠标在列表区域内时，支持滚轮滚动
        if (mouseX >= panelLeft && mouseX <= panelLeft + PANEL_WIDTH
            && mouseY >= listTop && mouseY <= listTop + listHeight && !filteredTrains.isEmpty()) {
            int maxScroll = Math.max(0, filteredTrains.size() - VISIBLE_ROWS);
            if (maxScroll > 0 && verticalAmount != 0) {
                // 使用更平滑的滚动，每次滚动3行
                int scrollAmount = (int) Math.signum(verticalAmount) * 3;
                scrollOffset -= scrollAmount;
                scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDraggingScrollbar && !filteredTrains.isEmpty()) {
            int centerX = this.width / 2;
            int panelLeft = centerX - PANEL_WIDTH / 2;
            int listTop = 62;
            int listHeight = LINE_HEIGHT * VISIBLE_ROWS;
            int maxScroll = Math.max(0, filteredTrains.size() - VISIBLE_ROWS);
            
            if (maxScroll > 0) {
                // 根据鼠标Y位置计算滚动位置
                float visibleRatio = (float) VISIBLE_ROWS / filteredTrains.size();
                int thumbHeight = Math.max(20, (int) (listHeight * visibleRatio));
                int scrollableHeight = listHeight - thumbHeight;
                
                if (scrollableHeight > 0) {
                    double relativeY = mouseY - listTop - thumbHeight / 2.0;
                    double ratio = Math.max(0, Math.min(1, relativeY / scrollableHeight));
                    scrollOffset = (int) (ratio * maxScroll);
                }
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isDraggingScrollbar) {
            isDraggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // 不绘制物品栏标签
    }
}

