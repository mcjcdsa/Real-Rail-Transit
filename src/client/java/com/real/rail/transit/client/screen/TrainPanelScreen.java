package com.real.rail.transit.client.screen;

import com.real.rail.transit.addon.AddonManager;
import com.real.rail.transit.block.screen.TrainPanelScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * 列车面板 GUI 界面
 * 显示从追加包导入的列车列表，可选择列车、查看编组信息
 * 与追加包创建器网站对应
 */
public class TrainPanelScreen extends HandledScreen<TrainPanelScreenHandler> {

    private List<AddonManager.TrainConfig> trains = List.of();
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private static final int LINE_HEIGHT = 22;
    private static final int VISIBLE_ROWS = 8;
    private static final int PANEL_WIDTH = 280;

    private ButtonWidget refreshButton;
    private ButtonWidget closeButton;

    public TrainPanelScreen(TrainPanelScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 240;
        this.backgroundWidth = 320;
    }

    @Override
    protected void init() {
        super.init();
        loadTrains();

        int centerX = this.width / 2;
        int buttonY = this.height - 28;

        refreshButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.train_panel.refresh"),
            button -> loadTrains()
        ).dimensions(centerX - 85, buttonY, 80, 20).build();
        this.addDrawableChild(refreshButton);

        closeButton = ButtonWidget.builder(
            Text.translatable("gui.real-rail-transit-mod.close"),
            button -> this.close()
        ).dimensions(centerX + 5, buttonY, 80, 20).build();
        this.addDrawableChild(closeButton);
    }

    private void loadTrains() {
        trains = AddonManager.getInstance().getLoadedAddons();
        selectedIndex = trains.isEmpty() ? -1 : Math.min(selectedIndex, trains.size() - 1);
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
        int startY = 45;

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
            32,
            0xAAAAAA
        );

        // 列车列表区域
        int listTop = startY;
        int listHeight = LINE_HEIGHT * VISIBLE_ROWS;
        context.fill(panelLeft, listTop, panelLeft + PANEL_WIDTH, listTop + listHeight, 0x80000000);

        if (trains.isEmpty()) {
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.train_panel.no_trains"),
                centerX,
                listTop + listHeight / 2 - 5,
                0x888888
            );
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.train_panel.import_hint"),
                centerX,
                listTop + listHeight / 2 + 12,
                0x666666
            );
        } else {
            int maxScroll = Math.max(0, trains.size() - VISIBLE_ROWS);
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

            for (int i = 0; i < VISIBLE_ROWS; i++) {
                int idx = scrollOffset + i;
                if (idx >= trains.size()) break;

                AddonManager.TrainConfig train = trains.get(idx);
                int y = listTop + i * LINE_HEIGHT + 4;
                boolean hovered = mouseX >= panelLeft && mouseX <= panelLeft + PANEL_WIDTH
                    && mouseY >= y - 2 && mouseY <= y + LINE_HEIGHT - 2;
                boolean selected = idx == selectedIndex;

                int bgColor = selected ? 0x6040A0FF : (hovered ? 0x40303030 : 0);
                if (bgColor != 0) {
                    context.fill(panelLeft + 2, y - 2, panelLeft + PANEL_WIDTH - 2, y + LINE_HEIGHT - 6, bgColor);
                }

                // 列车名称
                Text nameText = Text.literal(train.train_name != null ? train.train_name : train.train_id)
                    .formatted(selected ? Formatting.YELLOW : Formatting.WHITE);
                context.drawText(this.textRenderer, nameText, panelLeft + 8, y, 0xFFFFFF, false);

                // 列车 ID 和编组信息
                String info = String.format("ID: %s | %d节", train.train_id, train.car_count);
                context.drawText(this.textRenderer, info, panelLeft + 8, y + 11, 0xAAAAAA, false);
            }

            // 滚动条
            if (maxScroll > 0) {
                int scrollBarX = panelLeft + PANEL_WIDTH - 6;
                int thumbHeight = Math.max(20, listHeight * VISIBLE_ROWS / Math.max(trains.size(), 1));
                float ratio = (float) scrollOffset / maxScroll;
                int thumbY = listTop + (int) (ratio * (listHeight - thumbHeight));
                context.fill(scrollBarX, listTop, scrollBarX + 4, listTop + listHeight, 0x80000000);
                context.fill(scrollBarX, thumbY, scrollBarX + 4, thumbY + thumbHeight, 0xFF808080);
            }
        }

        // 选中列车详情
        if (selectedIndex >= 0 && selectedIndex < trains.size()) {
            AddonManager.TrainConfig train = trains.get(selectedIndex);
            int detailY = listTop + listHeight + 15;
            context.drawText(this.textRenderer,
                Text.translatable("gui.real-rail-transit-mod.train_panel.detail_title"), panelLeft, detailY, 0xFFFF00, false);
            detailY += 14;
            context.drawText(this.textRenderer, Text.translatable("gui.real-rail-transit-mod.train_panel.detail_speed", train.max_speed), panelLeft, detailY, 0xCCCCCC, false);
            detailY += 12;
            context.drawText(this.textRenderer, Text.translatable("gui.real-rail-transit-mod.train_panel.detail_cars", train.car_count, train.car_length), panelLeft, detailY, 0xCCCCCC, false);
            detailY += 12;
            Text powerText = "catenary".equals(train.power_type)
                ? Text.translatable("gui.real-rail-transit-mod.train_panel.power_catenary")
                : Text.translatable("gui.real-rail-transit-mod.train_panel.power_third_rail");
            context.drawText(this.textRenderer, Text.translatable("gui.real-rail-transit-mod.train_panel.detail_power", powerText.getString()), panelLeft, detailY, 0xCCCCCC, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = this.width / 2;
        int panelLeft = centerX - PANEL_WIDTH / 2;
        int listTop = 45;
        int listHeight = LINE_HEIGHT * VISIBLE_ROWS;

        if (mouseX >= panelLeft && mouseX <= panelLeft + PANEL_WIDTH
            && mouseY >= listTop && mouseY <= listTop + listHeight && !trains.isEmpty()) {
            int row = (int) ((mouseY - listTop) / LINE_HEIGHT);
            int idx = scrollOffset + row;
            if (idx >= 0 && idx < trains.size()) {
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
        int listTop = 45;
        int listHeight = LINE_HEIGHT * VISIBLE_ROWS;

        if (mouseX >= panelLeft && mouseX <= panelLeft + PANEL_WIDTH
            && mouseY >= listTop && mouseY <= listTop + listHeight && !trains.isEmpty()) {
            int maxScroll = Math.max(0, trains.size() - VISIBLE_ROWS);
            if (maxScroll > 0 && verticalAmount != 0) {
                scrollOffset -= (int) verticalAmount;
                scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // 不绘制物品栏标签
    }
}

