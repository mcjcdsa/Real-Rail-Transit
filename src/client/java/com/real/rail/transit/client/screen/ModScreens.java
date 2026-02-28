package com.real.rail.transit.client.screen;

import com.real.rail.transit.block.screen.ControlPanelScreenHandler;
import com.real.rail.transit.block.screen.TrackConstructionControlPanelScreenHandler;
import com.real.rail.transit.block.screen.TrackControlPanelScreenHandler;
import com.real.rail.transit.block.screen.TrainPanelScreenHandler;
import com.real.rail.transit.station.screen.ArrivalDisplayScreenHandler;
import com.real.rail.transit.station.screen.DisplayScreenHandler;
import com.real.rail.transit.station.screen.StationConstructionControlPanelScreenHandler;
import com.real.rail.transit.station.screen.StationRadioScreenHandler;
import com.real.rail.transit.station.screen.TicketMachineScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

/**
 * GUI界面注册类
 */
public class ModScreens {
    public static void register() {
        // 使用Minecraft原生API注册Screen
        HandledScreens.register(DisplayScreenHandler.TYPE, DisplayScreenScreen::new);
        HandledScreens.register(TicketMachineScreenHandler.TYPE, TicketMachineScreen::new);
        HandledScreens.register(StationRadioScreenHandler.TYPE, StationRadioScreen::new);
        HandledScreens.register(ArrivalDisplayScreenHandler.TYPE, ArrivalDisplayScreenScreen::new);
        HandledScreens.register(TrackConstructionControlPanelScreenHandler.TYPE, TrackConstructionControlPanelScreen::new);
        HandledScreens.register(StationConstructionControlPanelScreenHandler.TYPE, StationConstructionControlPanelScreen::new);
        HandledScreens.register(TrackControlPanelScreenHandler.TYPE, TrackControlPanelScreen::new);
        HandledScreens.register(TrainPanelScreenHandler.TYPE, TrainPanelScreen::new);
        HandledScreens.register(ControlPanelScreenHandler.TYPE, ControlPanelScreen::new);
    }
}

