package com.real.rail.transit.item;

import com.real.rail.transit.block.entity.SensorControllerBlockEntity;
import com.real.rail.transit.block.entity.TrainPowerSettingControllerBlockEntity;
import com.real.rail.transit.station.entity.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 设置器物品
 * 用于配置各种设施的通用设置工具
 */
public class SettingControllerItem extends Item {
    public SettingControllerItem(Settings settings) {
        super(settings);
    }
    
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        
        if (world.isClient || player == null) {
            return ActionResult.PASS;
        }
        
        BlockEntity blockEntity = world.getBlockEntity(pos);
        
        if (blockEntity != null) {
            // 根据不同的BlockEntity类型显示不同的信息
            if (blockEntity instanceof DisplayScreenBlockEntity displayEntity) {
                player.sendMessage(Text.translatable("item.real-rail-transit-mod.setting_controller.display_screen",
                    displayEntity.getDisplayText(), 
                    String.format("#%06X", displayEntity.getTextColor() & 0xFFFFFF),
                    displayEntity.getTextScale()), false);
                return ActionResult.SUCCESS;
            } else if (blockEntity instanceof TicketMachineBlockEntity ticketEntity) {
                player.sendMessage(Text.translatable("item.real-rail-transit-mod.setting_controller.ticket_machine",
                    ticketEntity.getTicketPrice(),
                    ticketEntity.getTicketCount(),
                    ticketEntity.isWorking() ? "正常" : "故障"), false);
                return ActionResult.SUCCESS;
            } else if (blockEntity instanceof StationRadioBlockEntity radioEntity) {
                player.sendMessage(Text.translatable("item.real-rail-transit-mod.setting_controller.station_radio",
                    radioEntity.getBroadcastMessage(),
                    radioEntity.getSoundId().isEmpty() ? "无" : radioEntity.getSoundId(),
                    radioEntity.getVolume(),
                    radioEntity.isPlaying() ? "播放中" : "停止"), false);
                return ActionResult.SUCCESS;
            } else if (blockEntity instanceof ArrivalDisplayScreenBlockEntity arrivalEntity) {
                player.sendMessage(Text.translatable("item.real-rail-transit-mod.setting_controller.arrival_display",
                    arrivalEntity.getTrainId(),
                    arrivalEntity.getDestination(),
                    arrivalEntity.getNextArrivalTime(),
                    arrivalEntity.getPlatform()), false);
                return ActionResult.SUCCESS;
            } else if (blockEntity instanceof SensorControllerBlockEntity sensorEntity) {
                player.sendMessage(Text.translatable("item.real-rail-transit-mod.setting_controller.sensor",
                    sensorEntity.getLastTrainId().isEmpty() ? "无" : sensorEntity.getLastTrainId(),
                    sensorEntity.getLastDetectionTime()), false);
                return ActionResult.SUCCESS;
            } else if (blockEntity instanceof TrainPowerSettingControllerBlockEntity powerEntity) {
                player.sendMessage(Text.translatable("item.real-rail-transit-mod.setting_controller.power_setting",
                    powerEntity.getPowerType(),
                    powerEntity.getVoltage(),
                    powerEntity.getMaxCurrent()), false);
                return ActionResult.SUCCESS;
            }
        }
        
        // 如果没有BlockEntity，显示方块基本信息
        BlockState state = world.getBlockState(pos);
        player.sendMessage(Text.translatable("item.real-rail-transit-mod.setting_controller.block_info",
            state.getBlock().getName().getString(),
            pos.getX(), pos.getY(), pos.getZ()), false);
        
        return ActionResult.SUCCESS;
    }
}

