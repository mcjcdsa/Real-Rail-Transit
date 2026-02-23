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
 * 显示屏方块实体
 * 存储显示屏的文本内容
 */
public class DisplayScreenBlockEntity extends BlockEntity {
    public static BlockEntityType<DisplayScreenBlockEntity> TYPE;
    
    private String displayText = "欢迎乘坐轨道交通";
    private int textColor = 0xFFFFFF; // 文本颜色（RGB）
    private float textScale = 1.0f; // 文本缩放
    
    public static void register() {
        TYPE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(RealRailTransitMod.MOD_ID, "display_screen"),
            BlockEntityType.Builder.create(DisplayScreenBlockEntity::new, com.real.rail.transit.registry.ModBlocks.DISPLAY_SCREEN).build()
        );
    }
    
    public DisplayScreenBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }
    
    public String getDisplayText() {
        return displayText;
    }
    
    public void setDisplayText(String text) {
        this.displayText = text;
        this.markDirty();
    }
    
    public int getTextColor() {
        return textColor;
    }
    
    public void setTextColor(int color) {
        this.textColor = color;
        this.markDirty();
    }
    
    public float getTextScale() {
        return textScale;
    }
    
    public void setTextScale(float scale) {
        this.textScale = Math.max(0.5f, Math.min(2.0f, scale));
        this.markDirty();
    }
    
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putString("displayText", displayText);
        nbt.putInt("textColor", textColor);
        nbt.putFloat("textScale", textScale);
    }
    
    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if (nbt.contains("displayText")) {
            displayText = nbt.getString("displayText");
        }
        if (nbt.contains("textColor")) {
            textColor = nbt.getInt("textColor");
        }
        if (nbt.contains("textScale")) {
            textScale = nbt.getFloat("textScale");
        }
    }
}

