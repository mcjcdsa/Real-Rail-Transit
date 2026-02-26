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
    
    // 购票状态
    private String selectedDestination = ""; // 选中的目的地
    private int insertedEmeralds = 0; // 已投入的绿宝石数量
    private java.util.UUID currentBuyer = null; // 当前购票的玩家UUID
    
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
    
    public String getSelectedDestination() {
        return selectedDestination;
    }
    
    public void setSelectedDestination(String destination) {
        this.selectedDestination = destination;
        this.markDirty();
    }
    
    public int getInsertedEmeralds() {
        return insertedEmeralds;
    }
    
    public void setInsertedEmeralds(int amount) {
        this.insertedEmeralds = amount;
        this.markDirty();
    }
    
    public void addEmeralds(int amount) {
        this.insertedEmeralds += amount;
        this.markDirty();
    }
    
    public void resetPurchase() {
        this.selectedDestination = "";
        this.insertedEmeralds = 0;
        this.currentBuyer = null;
        this.markDirty();
    }
    
    public java.util.UUID getCurrentBuyer() {
        return currentBuyer;
    }
    
    public void setCurrentBuyer(java.util.UUID buyer) {
        this.currentBuyer = buyer;
        this.markDirty();
    }
    
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putInt("ticketPrice", ticketPrice);
        nbt.putInt("ticketCount", ticketCount);
        nbt.putBoolean("isWorking", isWorking);
        nbt.putString("selectedDestination", selectedDestination);
        nbt.putInt("insertedEmeralds", insertedEmeralds);
        if (currentBuyer != null) {
            nbt.putUuid("currentBuyer", currentBuyer);
        }
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
        if (nbt.contains("selectedDestination")) {
            selectedDestination = nbt.getString("selectedDestination");
        }
        if (nbt.contains("insertedEmeralds")) {
            insertedEmeralds = nbt.getInt("insertedEmeralds");
        }
        if (nbt.containsUuid("currentBuyer")) {
            currentBuyer = nbt.getUuid("currentBuyer");
        }
    }
}

