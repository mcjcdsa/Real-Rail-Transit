package com.real.rail.transit.block.entity;

import com.real.rail.transit.RealRailTransitMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * 盾构机方块实体
 * 存储盾构机的配置和状态
 */
public class DaojiMachineBlockEntity extends BlockEntity {
    public static BlockEntityType<DaojiMachineBlockEntity> TYPE;
    
    // 隧道配置
    private int tunnelWidth = 3;      // 隧道宽度（方块数）
    private int tunnelHeight = 3;     // 隧道高度（方块数）
    private String tunnelType = "circular"; // 隧道类型：circular（圆形）、rectangular（矩形）
    private BlockState replaceBlock = Blocks.AIR.getDefaultState(); // 替换成的方块（默认空气）
    private BlockState wallBlock = Blocks.STONE_BRICKS.getDefaultState(); // 隧道壁材质（默认石砖）
    private Direction boringDirection = Direction.NORTH; // 盾构方向
    
    // 路径配置
    private BlockPos startPos = null; // 起点坐标
    private BlockPos endPos = null;   // 终点坐标
    private String pathType = "straight"; // 路径类型：straight（直线）、curve（曲线）
    private double curveRadius = 10.0; // 曲线半径（仅用于曲线路径）
    
    // 工作状态
    private boolean isWorking = false; // 是否正在工作
    private int progress = 0; // 进度（已挖掘的方块数，表示路径上的位置）
    private java.util.List<BlockPos> pathPoints = null; // 计算好的路径点列表
    
    public static void register() {
        TYPE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(RealRailTransitMod.MOD_ID, "daoji_machine"),
            BlockEntityType.Builder.create(DaojiMachineBlockEntity::new, 
                com.real.rail.transit.registry.ModBlocks.DAOJI_MACHINE).build()
        );
    }
    
    public DaojiMachineBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }
    
    // Getter 和 Setter 方法
    public int getTunnelWidth() {
        return tunnelWidth;
    }
    
    public void setTunnelWidth(int width) {
        this.tunnelWidth = Math.max(1, Math.min(15, width)); // 限制在1-15之间
        this.markDirty();
    }
    
    public int getTunnelHeight() {
        return tunnelHeight;
    }
    
    public void setTunnelHeight(int height) {
        this.tunnelHeight = Math.max(1, Math.min(15, height)); // 限制在1-15之间
        this.markDirty();
    }
    
    public String getTunnelType() {
        return tunnelType;
    }
    
    public void setTunnelType(String type) {
        this.tunnelType = type;
        this.markDirty();
    }
    
    public BlockState getReplaceBlock() {
        return replaceBlock;
    }
    
    public void setReplaceBlock(BlockState block) {
        this.replaceBlock = block;
        this.markDirty();
    }
    
    public BlockState getWallBlock() {
        return wallBlock;
    }
    
    public void setWallBlock(BlockState block) {
        this.wallBlock = block;
        this.markDirty();
    }
    
    public Direction getBoringDirection() {
        return boringDirection;
    }
    
    public void setBoringDirection(Direction direction) {
        this.boringDirection = direction;
        this.markDirty();
    }
    
    public boolean isWorking() {
        return isWorking;
    }
    
    public void setWorking(boolean working) {
        this.isWorking = working;
        this.markDirty();
    }
    
    public int getProgress() {
        return progress;
    }
    
    public void setProgress(int progress) {
        this.progress = progress;
        this.markDirty();
    }
    
    // 路径配置的 Getter 和 Setter
    public BlockPos getStartPos() {
        return startPos;
    }
    
    public void setStartPos(BlockPos pos) {
        this.startPos = pos;
        this.pathPoints = null; // 清除缓存的路径点
        this.markDirty();
    }
    
    public BlockPos getEndPos() {
        return endPos;
    }
    
    public void setEndPos(BlockPos pos) {
        this.endPos = pos;
        this.pathPoints = null; // 清除缓存的路径点
        this.markDirty();
    }
    
    public String getPathType() {
        return pathType;
    }
    
    public void setPathType(String type) {
        this.pathType = type;
        this.pathPoints = null; // 清除缓存的路径点
        this.markDirty();
    }
    
    public double getCurveRadius() {
        return curveRadius;
    }
    
    public void setCurveRadius(double radius) {
        this.curveRadius = Math.max(5.0, Math.min(100.0, radius)); // 限制在5-100之间
        this.pathPoints = null; // 清除缓存的路径点
        this.markDirty();
    }
    
    public java.util.List<BlockPos> getPathPoints() {
        return pathPoints;
    }
    
    public void setPathPoints(java.util.List<BlockPos> points) {
        this.pathPoints = points;
        this.markDirty();
    }
    
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putInt("tunnelWidth", tunnelWidth);
        nbt.putInt("tunnelHeight", tunnelHeight);
        nbt.putString("tunnelType", tunnelType);
        nbt.putString("boringDirection", boringDirection.getName());
        nbt.putBoolean("isWorking", isWorking);
        nbt.putInt("progress", progress);
        
        // 保存路径配置
        if (startPos != null) {
            nbt.putLong("startPos", startPos.asLong());
        }
        if (endPos != null) {
            nbt.putLong("endPos", endPos.asLong());
        }
        nbt.putString("pathType", pathType);
        nbt.putDouble("curveRadius", curveRadius);
        
        // 保存方块状态
        if (replaceBlock != null) {
            nbt.putString("replaceBlock", Registries.BLOCK.getId(replaceBlock.getBlock()).toString());
        }
        if (wallBlock != null) {
            nbt.putString("wallBlock", Registries.BLOCK.getId(wallBlock.getBlock()).toString());
        }
    }
    
    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if (nbt.contains("tunnelWidth")) {
            tunnelWidth = nbt.getInt("tunnelWidth");
        }
        if (nbt.contains("tunnelHeight")) {
            tunnelHeight = nbt.getInt("tunnelHeight");
        }
        if (nbt.contains("tunnelType")) {
            tunnelType = nbt.getString("tunnelType");
        }
        if (nbt.contains("boringDirection")) {
            try {
                boringDirection = Direction.byName(nbt.getString("boringDirection"));
            } catch (Exception e) {
                boringDirection = Direction.NORTH;
            }
        }
        if (nbt.contains("isWorking")) {
            isWorking = nbt.getBoolean("isWorking");
        }
        if (nbt.contains("progress")) {
            progress = nbt.getInt("progress");
        }
        
        // 读取路径配置
        if (nbt.contains("startPos")) {
            startPos = BlockPos.fromLong(nbt.getLong("startPos"));
        }
        if (nbt.contains("endPos")) {
            endPos = BlockPos.fromLong(nbt.getLong("endPos"));
        }
        if (nbt.contains("pathType")) {
            pathType = nbt.getString("pathType");
        }
        if (nbt.contains("curveRadius")) {
            curveRadius = nbt.getDouble("curveRadius");
        }
        pathPoints = null; // 路径点需要重新计算
        
        // 读取方块状态
        if (nbt.contains("replaceBlock")) {
            try {
                String blockIdStr = nbt.getString("replaceBlock");
                Identifier blockId;
                if (blockIdStr.contains(":")) {
                    String[] parts = blockIdStr.split(":", 2);
                    blockId = Identifier.of(parts[0], parts[1]);
                } else {
                    blockId = Identifier.of("minecraft", blockIdStr);
                }
                var wrapper = registries.getWrapperOrThrow(Registries.BLOCK.getKey());
                var blockKey = net.minecraft.registry.RegistryKey.of(Registries.BLOCK.getKey(), blockId);
                var entry = wrapper.getOptional(blockKey);
                if (entry.isPresent()) {
                    Block block = entry.get().value();
                    replaceBlock = block.getDefaultState();
                } else {
                    replaceBlock = Blocks.AIR.getDefaultState();
                }
            } catch (Exception e) {
                replaceBlock = Blocks.AIR.getDefaultState();
            }
        }
        if (nbt.contains("wallBlock")) {
            try {
                String blockIdStr = nbt.getString("wallBlock");
                Identifier blockId;
                if (blockIdStr.contains(":")) {
                    String[] parts = blockIdStr.split(":", 2);
                    blockId = Identifier.of(parts[0], parts[1]);
                } else {
                    blockId = Identifier.of("minecraft", blockIdStr);
                }
                var wrapper = registries.getWrapperOrThrow(Registries.BLOCK.getKey());
                var blockKey = net.minecraft.registry.RegistryKey.of(Registries.BLOCK.getKey(), blockId);
                var entry = wrapper.getOptional(blockKey);
                if (entry.isPresent()) {
                    Block block = entry.get().value();
                    wallBlock = block.getDefaultState();
                } else {
                    wallBlock = Blocks.STONE_BRICKS.getDefaultState();
                }
            } catch (Exception e) {
                wallBlock = Blocks.STONE_BRICKS.getDefaultState();
            }
        }
    }
}

