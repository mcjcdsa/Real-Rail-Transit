package com.real.rail.transit.client.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

/**
 * 世界地图渲染器
 * 用于在控制面板中显示当前存档的世界地图
 */
public class WorldMapRenderer {
    private static final int CHUNK_SIZE = 16;
    private static final int MAP_SCALE = 1; // 每个像素代表多少方块
    
    // 地图视图参数
    private int centerX = 0;
    private int centerZ = 0;
    private float zoom = 1.0f;
    
    /**
     * 渲染世界地图到指定区域
     * 
     * @param context 绘制上下文
     * @param x 地图区域左上角X坐标
     * @param y 地图区域左上角Y坐标
     * @param width 地图区域宽度
     * @param height 地图区域高度
     * @param world 客户端世界
     * @param playerPos 玩家位置（用于居中显示）
     */
    public void renderMap(DrawContext context, int x, int y, int width, int height, 
                         ClientWorld world, BlockPos playerPos) {
        if (world == null) {
            // 如果没有世界，绘制占位符
            context.fill(x, y, x + width, y + height, 0xFF404040);
            return;
        }
        
        // 如果玩家位置有效，更新地图中心
        if (playerPos != null) {
            centerX = playerPos.getX();
            centerZ = playerPos.getZ();
        }
        
        // 计算可见的区块范围
        int visibleChunksX = (int) Math.ceil((width / zoom) / CHUNK_SIZE) + 2;
        int visibleChunksZ = (int) Math.ceil((height / zoom) / CHUNK_SIZE) + 2;
        
        int startChunkX = (int) Math.floor((centerX - (width / zoom / 2)) / CHUNK_SIZE);
        int startChunkZ = (int) Math.floor((centerZ - (height / zoom / 2)) / CHUNK_SIZE);
        
        // 绘制地图背景
        context.fill(x, y, x + width, y + height, 0xFF1a1a1a);
        
        // 绘制网格线（可选）
        drawGrid(context, x, y, width, height);
        
        // 渲染区块
        for (int chunkX = startChunkX; chunkX < startChunkX + visibleChunksX; chunkX++) {
            for (int chunkZ = startChunkZ; chunkZ < startChunkZ + visibleChunksZ; chunkZ++) {
                renderChunk(context, x, y, width, height, world, chunkX, chunkZ);
            }
        }
        
        // 绘制玩家位置标记
        if (playerPos != null) {
            drawPlayerMarker(context, x, y, width, height, playerPos);
        }
    }
    
    /**
     * 绘制网格线
     */
    private void drawGrid(DrawContext context, int x, int y, int width, int height) {
        int gridSpacing = (int) (CHUNK_SIZE * zoom);
        
        // 计算起始网格位置
        int startGridX = (int) ((centerX - width / zoom / 2) / CHUNK_SIZE) * gridSpacing;
        int startGridZ = (int) ((centerZ - height / zoom / 2) / CHUNK_SIZE) * gridSpacing;
        
        // 绘制垂直线
        for (int gridX = startGridX; gridX < startGridX + width + gridSpacing; gridX += gridSpacing) {
            int screenX = x + (int) ((gridX * CHUNK_SIZE - (centerX - width / zoom / 2)) * zoom);
            if (screenX >= x && screenX <= x + width) {
                context.drawVerticalLine(screenX, y, y + height, 0x30FFFFFF);
            }
        }
        
        // 绘制水平线
        for (int gridZ = startGridZ; gridZ < startGridZ + height + gridSpacing; gridZ += gridSpacing) {
            int screenZ = y + (int) ((gridZ * CHUNK_SIZE - (centerZ - height / zoom / 2)) * zoom);
            if (screenZ >= y && screenZ <= y + height) {
                context.drawHorizontalLine(x, x + width, screenZ, 0x30FFFFFF);
            }
        }
    }
    
    /**
     * 渲染单个区块
     */
    private void renderChunk(DrawContext context, int mapX, int mapY, int mapWidth, int mapHeight,
                              ClientWorld world, int chunkX, int chunkZ) {
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        Chunk chunk = world.getChunk(chunkX, chunkZ);
        
        if (chunk == null) {
            return; // 区块未加载
        }
        
        // 计算区块在屏幕上的位置
        int chunkWorldX = chunkX * CHUNK_SIZE;
        int chunkWorldZ = chunkZ * CHUNK_SIZE;
        
        int screenX = mapX + (int) ((chunkWorldX - (centerX - mapWidth / zoom / 2)) * zoom);
        int screenZ = mapY + (int) ((chunkWorldZ - (centerZ - mapHeight / zoom / 2)) * zoom);
        int chunkScreenSize = (int) (CHUNK_SIZE * zoom);
        
        // 如果区块在可见区域外，跳过
        if (screenX + chunkScreenSize < mapX || screenX > mapX + mapWidth ||
            screenZ + chunkScreenSize < mapY || screenZ > mapY + mapHeight) {
            return;
        }
        
        // 获取区块的主要方块类型（简化：使用最高非空气方块）
        int color = getChunkColor(chunk, chunkWorldX, chunkWorldZ, world);
        
        // 绘制区块
        context.fill(screenX, screenZ, screenX + chunkScreenSize, screenZ + chunkScreenSize, color);
        
        // 绘制区块边界
        if (zoom >= 0.5f) {
            context.drawBorder(screenX, screenZ, chunkScreenSize, chunkScreenSize, 0x40000000);
        }
    }
    
    /**
     * 获取区块的颜色（基于主要方块类型）
     */
    private int getChunkColor(Chunk chunk, int chunkWorldX, int chunkWorldZ, ClientWorld world) {
        // 简化实现：根据区块中的主要方块类型返回颜色
        // 这里使用一个简化的方法，实际可以采样多个方块
        
        int sampleY = 64; // 采样高度
        int sampleX = chunkWorldX + 8;
        int sampleZ = chunkWorldZ + 8;
        
        if (sampleY < 0 || sampleY >= 320) {
            return 0xFF000000; // 黑色（无效高度）
        }
        
        BlockPos samplePos = new BlockPos(sampleX, sampleY, sampleZ);
        var blockState = world.getBlockState(samplePos);
        var block = blockState.getBlock();
        
        // 根据方块类型返回颜色
        String blockName = block.getTranslationKey();
        
        // 水
        if (blockName.contains("water")) {
            return 0xFF1E3A8A; // 深蓝色
        }
        // 草方块
        if (blockName.contains("grass")) {
            return 0xFF4A7C59; // 绿色
        }
        // 沙子
        if (blockName.contains("sand")) {
            return 0xFFD4A574; // 沙色
        }
        // 石头
        if (blockName.contains("stone")) {
            return 0xFF808080; // 灰色
        }
        // 雪
        if (blockName.contains("snow")) {
            return 0xFFFFFFFF; // 白色
        }
        // 冰
        if (blockName.contains("ice")) {
            return 0xFFB0E0E6; // 浅蓝色
        }
        // 泥土
        if (blockName.contains("dirt")) {
            return 0xFF8B4513; // 棕色
        }
        // 默认：根据亮度
        float brightness = world.getBrightness(samplePos);
        int gray = (int) (brightness * 255);
        return (0xFF << 24) | (gray << 16) | (gray << 8) | gray;
    }
    
    /**
     * 绘制玩家位置标记
     */
    private void drawPlayerMarker(DrawContext context, int mapX, int mapY, int mapWidth, int mapHeight,
                                  BlockPos playerPos) {
        int screenX = mapX + (int) ((playerPos.getX() - (centerX - mapWidth / zoom / 2)) * zoom);
        int screenZ = mapY + (int) ((playerPos.getZ() - (centerZ - mapHeight / zoom / 2)) * zoom);
        
        // 确保标记在可见区域内
        if (screenX < mapX - 5 || screenX > mapX + mapWidth + 5 ||
            screenZ < mapY - 5 || screenZ > mapY + mapHeight + 5) {
            return;
        }
        
        // 绘制玩家位置标记（白色箭头）
        int markerSize = Math.max(3, (int) (5 * zoom));
        
        // 绘制圆形标记
        context.fill(screenX - markerSize, screenZ - markerSize, 
                    screenX + markerSize, screenZ + markerSize, 0xFFFFFFFF);
        context.fill(screenX - markerSize + 1, screenZ - markerSize + 1, 
                    screenX + markerSize - 1, screenZ + markerSize - 1, 0xFFFF0000);
    }
    
    /**
     * 设置地图中心位置
     */
    public void setCenter(int x, int z) {
        this.centerX = x;
        this.centerZ = z;
    }
    
    /**
     * 设置缩放级别
     */
    public void setZoom(float zoom) {
        this.zoom = Math.max(0.1f, Math.min(5.0f, zoom)); // 限制在0.1-5.0之间
    }
    
    /**
     * 获取当前缩放级别
     */
    public float getZoom() {
        return zoom;
    }
    
    /**
     * 将屏幕坐标转换为世界坐标
     */
    public BlockPos screenToWorld(int screenX, int screenZ, int mapX, int mapY, int mapWidth, int mapHeight) {
        int worldX = (int) ((screenX - mapX) / zoom + (centerX - mapWidth / zoom / 2));
        int worldZ = (int) ((screenZ - mapY) / zoom + (centerZ - mapHeight / zoom / 2));
        return new BlockPos(worldX, 0, worldZ);
    }
    
    /**
     * 将世界坐标转换为屏幕坐标
     */
    public int[] worldToScreen(BlockPos worldPos, int mapX, int mapY, int mapWidth, int mapHeight) {
        int screenX = mapX + (int) ((worldPos.getX() - (centerX - mapWidth / zoom / 2)) * zoom);
        int screenZ = mapY + (int) ((worldPos.getZ() - (centerZ - mapHeight / zoom / 2)) * zoom);
        return new int[]{screenX, screenZ};
    }
}

