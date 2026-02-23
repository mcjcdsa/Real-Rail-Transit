package com.real.rail.transit.station.entity;

import com.real.rail.transit.RealRailTransitMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * 售票机方块实体
 * 存储售票机的状态和票务信息
 */
public class TicketMachineBlockEntity extends BlockEntity {
    public static BlockEntityType<TicketMachineBlockEntity> TYPE;
    
    private int ticketPrice = 5; // 票价（绿宝石）
    private int ticketCount = 0; // 已售出票数
    private boolean isWorking = true;
    
    public static void register() {
        TYPE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(RealRailTransitMod.MOD_ID, "ticket_machine"),
            BlockEntityType.Builder.create(TicketMachineBlockEntity::new, com.real.rail.transit.registry.ModBlocks.TICKET_MACHINE).build()
        );
    }
    
    public TicketMachineBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }
    
    public int getTicketPrice() {
        return ticketPrice;
    }
    
    public void setTicketPrice(int price) {
        this.ticketPrice = price;
        this.markDirty();
    }
    
    public int getTicketCount() {
        return ticketCount;
    }
    
    public void incrementTicketCount() {
        this.ticketCount++;
        this.markDirty();
    }
    
    public boolean isWorking() {
        return isWorking;
    }
    
    public void setWorking(boolean working) {
        this.isWorking = working;
        this.markDirty();
    }
    
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putInt("ticketPrice", ticketPrice);
        nbt.putInt("ticketCount", ticketCount);
        nbt.putBoolean("isWorking", isWorking);
    }
    
    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if (nbt.contains("ticketPrice")) {
            ticketPrice = nbt.getInt("ticketPrice");
        }
        if (nbt.contains("ticketCount")) {
            ticketCount = nbt.getInt("ticketCount");
        }
        if (nbt.contains("isWorking")) {
            isWorking = nbt.getBoolean("isWorking");
        }
    }
}

