package com.real.rail.transit.registry;

import com.real.rail.transit.block.screen.TrackConstructionControlPanelScreenHandler;
import com.real.rail.transit.block.screen.TrackControlPanelScreenHandler;
import com.real.rail.transit.block.screen.TrainPanelScreenHandler;
import com.real.rail.transit.station.screen.ArrivalDisplayScreenHandler;
import com.real.rail.transit.station.screen.DisplayScreenHandler;
import com.real.rail.transit.station.screen.StationConstructionControlPanelScreenHandler;
import com.real.rail.transit.station.screen.StationRadioScreenHandler;
import com.real.rail.transit.station.screen.TicketMachineScreenHandler;

/**
 * GUI处理器注册类
 */
public class ModScreenHandlers {
    public static void register() {
        DisplayScreenHandler.register();
        TicketMachineScreenHandler.register();
        StationRadioScreenHandler.register();
        ArrivalDisplayScreenHandler.register();
        TrackConstructionControlPanelScreenHandler.register();
        StationConstructionControlPanelScreenHandler.register();
        TrackControlPanelScreenHandler.register();
        TrainPanelScreenHandler.register();
    }
}

