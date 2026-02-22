package com.real.rail.transit.client.screen;

import com.real.rail.transit.system.DispatchSystem;
import com.real.rail.transit.system.ScheduleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

/**
 * 运行图管理界面
 * 用于创建、编辑和管理列车运行图
 */
public class ScheduleScreen extends Screen {
    private final Screen parent;
    
    private TextFieldWidget trainIdField;
    private TextFieldWidget startStationField;
    private TextFieldWidget endStationField;
    private TextFieldWidget departureTimeField;
    private TextFieldWidget intervalField;
    
    private ButtonWidget saveButton;
    private ButtonWidget loadButton;
    private ButtonWidget deleteButton;
    
    public ScheduleScreen(Screen parent) {
        super(Text.translatable("screen.real-rail-transit-mod.schedule"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int startY = 50;
        int fieldHeight = 20;
        int fieldSpacing = 25;
        
        // 车次号输入框
        this.trainIdField = new TextFieldWidget(
            this.textRenderer,
            centerX - 100, startY,
            200, fieldHeight,
            Text.translatable("field.real-rail-transit-mod.train_id")
        );
        this.addSelectableChild(trainIdField);
        
        // 始发站输入框
        this.startStationField = new TextFieldWidget(
            this.textRenderer,
            centerX - 100, startY + fieldSpacing,
            200, fieldHeight,
            Text.translatable("field.real-rail-transit-mod.start_station")
        );
        this.addSelectableChild(startStationField);
        
        // 终点站输入框
        this.endStationField = new TextFieldWidget(
            this.textRenderer,
            centerX - 100, startY + fieldSpacing * 2,
            200, fieldHeight,
            Text.translatable("field.real-rail-transit-mod.end_station")
        );
        this.addSelectableChild(endStationField);
        
        // 发车时间输入框
        this.departureTimeField = new TextFieldWidget(
            this.textRenderer,
            centerX - 100, startY + fieldSpacing * 3,
            200, fieldHeight,
            Text.translatable("field.real-rail-transit-mod.departure_time")
        );
        this.addSelectableChild(departureTimeField);
        
        // 发车间隔输入框
        this.intervalField = new TextFieldWidget(
            this.textRenderer,
            centerX - 100, startY + fieldSpacing * 4,
            200, fieldHeight,
            Text.translatable("field.real-rail-transit-mod.interval")
        );
        this.addSelectableChild(intervalField);
        
        // 保存按钮
        this.saveButton = ButtonWidget.builder(
            Text.translatable("button.real-rail-transit-mod.save"),
            button -> saveSchedule()
        ).dimensions(centerX - 150, startY + fieldSpacing * 6, 90, 20).build();
        this.addDrawableChild(saveButton);
        
        // 加载按钮
        this.loadButton = ButtonWidget.builder(
            Text.translatable("button.real-rail-transit-mod.load"),
            button -> loadSchedule()
        ).dimensions(centerX - 50, startY + fieldSpacing * 6, 90, 20).build();
        this.addDrawableChild(loadButton);
        
        // 删除按钮
        this.deleteButton = ButtonWidget.builder(
            Text.translatable("button.real-rail-transit-mod.delete"),
            button -> deleteSchedule()
        ).dimensions(centerX + 50, startY + fieldSpacing * 6, 90, 20).build();
        this.addDrawableChild(deleteButton);
        
        // 返回按钮
        ButtonWidget backButton = ButtonWidget.builder(
            Text.translatable("button.real-rail-transit-mod.back"),
            button -> this.client.setScreen(parent)
        ).dimensions(centerX - 50, this.height - 30, 100, 20).build();
        this.addDrawableChild(backButton);
    }
    
    private void saveSchedule() {
        String trainId = trainIdField.getText();
        String startStation = startStationField.getText();
        String endStation = endStationField.getText();
        
        if (trainId.isEmpty() || startStation.isEmpty() || endStation.isEmpty()) {
            return; // TODO: 显示错误消息
        }
        
        DispatchSystem.ScheduleEntry schedule = new DispatchSystem.ScheduleEntry(trainId, startStation, endStation);
        
        try {
            schedule.setDepartureTime(Long.parseLong(departureTimeField.getText()));
            schedule.setInterval(Integer.parseInt(intervalField.getText()));
        } catch (NumberFormatException e) {
            return; // TODO: 显示错误消息
        }
        
        ScheduleManager.getInstance().saveSchedule(trainId, schedule);
        DispatchSystem.getInstance().addSchedule(schedule);
    }
    
    private void loadSchedule() {
        String trainId = trainIdField.getText();
        if (trainId.isEmpty()) {
            return;
        }
        
        DispatchSystem.ScheduleEntry schedule = ScheduleManager.getInstance().loadSchedule(trainId);
        if (schedule != null) {
            trainIdField.setText(schedule.getTrainId());
            startStationField.setText(schedule.getStartStation());
            endStationField.setText(schedule.getEndStation());
            departureTimeField.setText(String.valueOf(schedule.getDepartureTime()));
            intervalField.setText(String.valueOf(schedule.getInterval()));
        }
    }
    
    private void deleteSchedule() {
        String trainId = trainIdField.getText();
        if (trainId.isEmpty()) {
            return;
        }
        
        ScheduleManager.getInstance().deleteSchedule(trainId);
        DispatchSystem.getInstance().removeSchedule(trainId); // 从调度系统移除
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        
        int centerX = this.width / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, 20, 0xFFFFFF);
        
        // 绘制已保存的运行图列表
        List<String> schedules = ScheduleManager.getInstance().listSchedules();
        int listY = 200;
        context.drawText(this.textRenderer, Text.translatable("text.real-rail-transit-mod.saved_schedules"), centerX - 100, listY, 0xFFFFFF, false);
        
        for (int i = 0; i < Math.min(schedules.size(), 10); i++) {
            context.drawText(this.textRenderer, Text.literal(schedules.get(i)), centerX - 100, listY + 15 + i * 12, 0xCCCCCC, false);
        }
    }
}

