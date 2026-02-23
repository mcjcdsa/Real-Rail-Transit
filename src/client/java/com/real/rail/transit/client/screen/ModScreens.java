package com.real.rail.transit.client.screen;

import com.real.rail.transit.station.screen.ArrivalDisplayScreenHandler;
import com.real.rail.transit.station.screen.DisplayScreenHandler;
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
    }
}

