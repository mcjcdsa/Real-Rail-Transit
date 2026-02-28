package com.real.rail.transit.client.renderer;

import com.real.rail.transit.addon.AddonManager;
import com.real.rail.transit.block.TrackBlock;
import com.real.rail.transit.client.TrainPlacementManager;
import com.real.rail.transit.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * 列车放置预览渲染器
 * 渲染列车放置时的轮廓预览
 */
public class TrainPlacementRenderer {
    /**
     * 渲染列车放置预览
     */
    public static void render(MatrixStack matrices, Camera camera) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }
        
        TrainPlacementManager manager = TrainPlacementManager.getInstance();
        if (!manager.isPlacing()) {
            return;
        }
        
        AddonManager.TrainConfig train = manager.getSelectedTrain();
        if (train == null) {
            return;
        }
        
        // 获取玩家看向的方块
        HitResult hitResult = client.crosshairTarget;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            manager.setPreviewPos(null);
            return;
        }
        
        BlockHitResult blockHit = (BlockHitResult) hitResult;
        BlockPos pos = blockHit.getBlockPos();
        World world = client.world;
        
        // 检查是否是轨道方块
        BlockState state = world.getBlockState(pos);
        boolean isValid = state.getBlock() instanceof TrackBlock || state.isOf(ModBlocks.TRACK);
        
        // 检查上方是否有空间
        BlockPos trainPos = pos.up();
        if (!world.getBlockState(trainPos).isAir() || !world.getBlockState(trainPos.up()).isAir()) {
            isValid = false;
        }
        
        BlockPos previewPos = isValid ? trainPos : null;
        manager.setPreviewPos(previewPos);
        manager.setCanPlace(isValid);
        
        if (previewPos == null) {
            return;
        }
        
        // 计算列车尺寸
        // 列车长度 = 车厢数量 * 车厢长度（米）
        double trainLength = train.car_count * train.car_length;
        // 转换为方块单位（1米 = 1方块）
        double trainWidth = 2.5; // 列车宽度（米）
        double trainHeight = 3.5; // 列车高度（米）
        
        // 计算列车中心位置（在轨道上方）
        Vec3d trainCenter = Vec3d.ofCenter(trainPos);
        
        // 根据玩家朝向确定列车方向
        float playerYaw = client.player.getYaw();
        // 将yaw转换为方向向量
        double rad = Math.toRadians(playerYaw);
        Vec3d forward = new Vec3d(-Math.sin(rad), 0, Math.cos(rad));
        
        // 创建列车的边界框
        Vec3d start = trainCenter.add(forward.multiply(-trainLength / 2));
        Vec3d end = trainCenter.add(forward.multiply(trainLength / 2));
        
        Box trainBox = new Box(
            Math.min(start.x, end.x) - trainWidth / 2,
            previewPos.getY(),
            Math.min(start.z, end.z) - trainWidth / 2,
            Math.max(start.x, end.x) + trainWidth / 2,
            previewPos.getY() + trainHeight,
            Math.max(start.z, end.z) + trainWidth / 2
        );
        
        // 获取相机位置
        Vec3d cameraPos = camera.getPos();
        
        // 设置渲染状态
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        
        // 根据是否有效设置颜色
        float r, g, b, a;
        if (isValid) {
            r = 0.0f; g = 1.0f; b = 0.0f; a = 0.3f; // 绿色，可放置
        } else {
            r = 1.0f; g = 0.0f; b = 0.0f; a = 0.3f; // 红色，不可放置
        }
        RenderSystem.setShaderColor(r, g, b, a);
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        
        // 渲染填充的盒子（半透明）
        renderBox(matrices, buffer, cameraPos, trainBox, r, g, b, a);
        
        // 渲染边框（实线）
        a = 0.8f;
        RenderSystem.setShaderColor(r, g, b, a);
        renderBoxOutline(matrices, buffer, cameraPos, trainBox, r, g, b, a);
        
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
    
    /**
     * 渲染填充的盒子
     */
    private static void renderBox(MatrixStack matrices, BufferBuilder buffer, Vec3d cameraPos, Box box, float r, float g, float b, float a) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        double minX = box.minX - cameraPos.x;
        double minY = box.minY - cameraPos.y;
        double minZ = box.minZ - cameraPos.z;
        double maxX = box.maxX - cameraPos.x;
        double maxY = box.maxY - cameraPos.y;
        double maxZ = box.maxZ - cameraPos.z;
        
        buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        
        // 底面
        buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(r, g, b, a);
        
        // 顶面
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(r, g, b, a);
        
        // 前面
        buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(r, g, b, a);
        
        // 后面
        buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(r, g, b, a);
        
        // 左面
        buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(r, g, b, a);
        
        // 右面
        buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(r, g, b, a);
        
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
    
    /**
     * 渲染盒子边框
     */
    private static void renderBoxOutline(MatrixStack matrices, BufferBuilder buffer, Vec3d cameraPos, Box box, float r, float g, float b, float a) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        double minX = box.minX - cameraPos.x;
        double minY = box.minY - cameraPos.y;
        double minZ = box.minZ - cameraPos.z;
        double maxX = box.maxX - cameraPos.x;
        double maxY = box.maxY - cameraPos.y;
        double maxZ = box.maxZ - cameraPos.z;
        
        buffer.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        // 底面的四条边
        buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(r, g, b, a);
        
        buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(r, g, b, a);
        
        buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(r, g, b, a);
        
        buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(r, g, b, a);
        
        // 顶面的四条边
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(r, g, b, a);
        
        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(r, g, b, a);
        
        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(r, g, b, a);
        
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(r, g, b, a);
        
        // 四条垂直边
        buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(r, g, b, a);
        
        buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(r, g, b, a);
        
        buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(r, g, b, a);
        
        buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(r, g, b, a);
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(r, g, b, a);
        
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
}

