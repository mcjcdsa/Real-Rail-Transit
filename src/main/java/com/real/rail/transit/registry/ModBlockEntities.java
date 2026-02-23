package com.real.rail.transit.registry;

import com.real.rail.transit.block.entity.SensorControllerBlockEntity;
import com.real.rail.transit.block.entity.SignalBlockEntity;
import com.real.rail.transit.block.entity.TrainPowerSettingControllerBlockEntity;
import com.real.rail.transit.station.entity.ArrivalDisplayScreenBlockEntity;
import com.real.rail.transit.station.entity.DisplayScreenBlockEntity;
import com.real.rail.transit.station.entity.StationRadioBlockEntity;
import com.real.rail.transit.station.entity.TicketMachineBlockEntity;

/**
 * 方块实体注册类
 */
public class ModBlockEntities {
    public static void register() {
        SignalBlockEntity.register();
        DisplayScreenBlockEntity.register();
        TicketMachineBlockEntity.register();
        StationRadioBlockEntity.register();
        ArrivalDisplayScreenBlockEntity.register();
        SensorControllerBlockEntity.register();
        TrainPowerSettingControllerBlockEntity.register();
    }
}

