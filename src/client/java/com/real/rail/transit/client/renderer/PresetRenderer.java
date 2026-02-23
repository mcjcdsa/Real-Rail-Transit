package com.real.rail.transit.client.renderer;

import com.real.rail.transit.preset.PresetManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.List;

/**
 * 预设器渲染器
 * 渲染预设器的灰色虚线边框
 */
public class PresetRenderer {
    /**
     * 渲染预设器边框
     * 在WorldRenderEvents.AFTER_TRANSLUCENT中调用
     */
    public static void render(MatrixStack matrices, Camera camera) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }
        
        PlayerEntity player = client.player;
        PresetManager presetManager = PresetManager.getInstance();
        PresetManager.PresetData data = presetManager.getPresetData(player.getUuid());
        
        if (data == null || data.firstPos == null) {
            return;
        }
        
        // 获取相机位置
        Vec3d cameraPos = camera.getPos();
        
        // 设置渲染状态
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        // 渲染第一个点
        if (data.firstPos != null) {
            renderPoint(matrices, buffer, cameraPos, data.firstPos, 0.5f, 0.5f, 0.5f, 0.8f);
        }
        
        // 如果已选择两个点，渲染路径
        if (data.firstPos != null && data.secondPos != null) {
            List<BlockPos> path = data.calculatePath();
            
            // 渲染路径上的所有点
            for (BlockPos pos : path) {
                renderPoint(matrices, buffer, cameraPos, pos, 0.7f, 0.7f, 0.7f, 0.6f);
            }
            
            // 渲染连接线（虚线效果）
            // 检查是否使用贝塞尔曲线
            if (isStraightLine(data.firstPos, data.secondPos)) {
                // 直线：直接渲染虚线
                renderDashedLine(matrices, buffer, cameraPos, data.firstPos, data.secondPos, 
                    0.5f, 0.5f, 0.5f, 0.8f);
            } else {
                // 贝塞尔曲线：渲染曲线路径
                renderBezierCurve(matrices, buffer, cameraPos, data.firstPos, data.secondPos, 
                    0.5f, 0.5f, 0.5f, 0.8f);
            }
        }
        
        // 完成渲染
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        
        // 恢复渲染状态
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    /**
     * 渲染一个点（方块边框）
     */
    private static void renderPoint(MatrixStack matrices, BufferBuilder buffer, Vec3d cameraPos, 
                                    BlockPos pos, float r, float g, float b, float a) {
        double x = pos.getX() - cameraPos.x;
        double y = pos.getY() - cameraPos.y;
        double z = pos.getZ() - cameraPos.z;
        
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        // 绘制方块的12条边（虚线效果）
        float size = 0.01f; // 线条粗细
        
        // 底部4条边
        drawLine(buffer, matrix, x, y, z, x + 1, y, z, r, g, b, a, size);
        drawLine(buffer, matrix, x + 1, y, z, x + 1, y, z + 1, r, g, b, a, size);
        drawLine(buffer, matrix, x + 1, y, z + 1, x, y, z + 1, r, g, b, a, size);
        drawLine(buffer, matrix, x, y, z + 1, x, y, z, r, g, b, a, size);
        
        // 顶部4条边
        drawLine(buffer, matrix, x, y + 1, z, x + 1, y + 1, z, r, g, b, a, size);
        drawLine(buffer, matrix, x + 1, y + 1, z, x + 1, y + 1, z + 1, r, g, b, a, size);
        drawLine(buffer, matrix, x + 1, y + 1, z + 1, x, y + 1, z + 1, r, g, b, a, size);
        drawLine(buffer, matrix, x, y + 1, z + 1, x, y + 1, z, r, g, b, a, size);
        
        // 垂直4条边
        drawLine(buffer, matrix, x, y, z, x, y + 1, z, r, g, b, a, size);
        drawLine(buffer, matrix, x + 1, y, z, x + 1, y + 1, z, r, g, b, a, size);
        drawLine(buffer, matrix, x + 1, y, z + 1, x + 1, y + 1, z + 1, r, g, b, a, size);
        drawLine(buffer, matrix, x, y, z + 1, x, y + 1, z + 1, r, g, b, a, size);
    }
    
    /**
     * 绘制虚线
     */
    private static void renderDashedLine(MatrixStack matrices, BufferBuilder buffer, Vec3d cameraPos,
                                        BlockPos start, BlockPos end, float r, float g, float b, float a) {
        double x1 = start.getX() + 0.5 - cameraPos.x;
        double y1 = start.getY() + 0.5 - cameraPos.y;
        double z1 = start.getZ() + 0.5 - cameraPos.z;
        
        double x2 = end.getX() + 0.5 - cameraPos.x;
        double y2 = end.getY() + 0.5 - cameraPos.y;
        double z2 = end.getZ() + 0.5 - cameraPos.z;
        
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        // 虚线效果：分段绘制
        int segments = 20;
        for (int i = 0; i < segments; i += 2) {
            double t1 = (double) i / segments;
            double t2 = (double) (i + 1) / segments;
            
            double px1 = x1 + (x2 - x1) * t1;
            double py1 = y1 + (y2 - y1) * t1;
            double pz1 = z1 + (z2 - z1) * t1;
            
            double px2 = x1 + (x2 - x1) * t2;
            double py2 = y1 + (y2 - y1) * t2;
            double pz2 = z1 + (z2 - z1) * t2;
            
            drawLine(buffer, matrix, px1, py1, pz1, px2, py2, pz2, r, g, b, a, 0.02f);
        }
    }
    
    /**
     * 检查两点是否在同一直线上
     */
    private static boolean isStraightLine(BlockPos p1, BlockPos p2) {
        int dx = Math.abs(p2.getX() - p1.getX());
        int dy = Math.abs(p2.getY() - p1.getY());
        int dz = Math.abs(p2.getZ() - p1.getZ());
        
        int nonZeroCount = 0;
        if (dx > 0) nonZeroCount++;
        if (dy > 0) nonZeroCount++;
        if (dz > 0) nonZeroCount++;
        
        return nonZeroCount <= 1;
    }
    
    /**
     * 计算二次贝塞尔曲线上的点
     */
    private static Vec3d bezierPoint(double t, Vec3d p0, Vec3d p1, Vec3d p2) {
        double u = 1 - t;
        double tt = t * t;
        double uu = u * u;
        
        double x = uu * p0.x + 2 * u * t * p1.x + tt * p2.x;
        double y = uu * p0.y + 2 * u * t * p1.y + tt * p2.y;
        double z = uu * p0.z + 2 * u * t * p1.z + tt * p2.z;
        
        return new Vec3d(x, y, z);
    }
    
    /**
     * 计算控制点（用于贝塞尔曲线）
     */
    private static Vec3d calculateControlPoint(Vec3d start, Vec3d end) {
        // 中点
        double midX = (start.x + end.x) / 2;
        double midY = (start.y + end.y) / 2;
        double midZ = (start.z + end.z) / 2;
        
        // 方向向量
        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double dz = end.z - start.z;
        
        // 距离
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        if (distance < 3) {
            return new Vec3d(midX, midY, midZ);
        }
        
        // 垂直方向
        double perpX = -dz;
        double perpY = 0;
        double perpZ = dx;
        
        if (Math.abs(perpX) < 0.001 && Math.abs(perpZ) < 0.001) {
            perpX = -dy;
            perpY = dx;
            perpZ = 0;
        }
        
        // 归一化
        double perpLength = Math.sqrt(perpX * perpX + perpY * perpY + perpZ * perpZ);
        if (perpLength > 0.001) {
            perpX = perpX / perpLength * distance * 0.3;
            perpY = perpY / perpLength * distance * 0.3;
            perpZ = perpZ / perpLength * distance * 0.3;
        }
        
        return new Vec3d(midX + perpX, midY + perpY, midZ + perpZ);
    }
    
    /**
     * 渲染贝塞尔曲线（虚线效果）
     */
    private static void renderBezierCurve(MatrixStack matrices, BufferBuilder buffer, Vec3d cameraPos,
                                         BlockPos start, BlockPos end, float r, float g, float b, float a) {
        Vec3d startVec = new Vec3d(start.getX() + 0.5, start.getY() + 0.5, start.getZ() + 0.5);
        Vec3d endVec = new Vec3d(end.getX() + 0.5, end.getY() + 0.5, end.getZ() + 0.5);
        Vec3d controlPoint = calculateControlPoint(startVec, endVec);
        
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        // 计算曲线上的点
        int segments = 40; // 增加分段数以获得更平滑的曲线
        Vec3d lastPoint = null;
        
        for (int i = 0; i <= segments; i++) {
            double t = (double) i / segments;
            Vec3d point = bezierPoint(t, startVec, controlPoint, endVec);
            
            // 转换为相对于相机的位置
            double px = point.x - cameraPos.x;
            double py = point.y - cameraPos.y;
            double pz = point.z - cameraPos.z;
            
            // 绘制虚线效果（每两段绘制一段）
            if (lastPoint != null && i % 2 == 0) {
                double lx = lastPoint.x - cameraPos.x;
                double ly = lastPoint.y - cameraPos.y;
                double lz = lastPoint.z - cameraPos.z;
                
                drawLine(buffer, matrix, lx, ly, lz, px, py, pz, r, g, b, a, 0.02f);
            }
            
            lastPoint = point;
        }
    }
    
    /**
     * 绘制一条线
     */
    private static void drawLine(BufferBuilder buffer, Matrix4f matrix, 
                                 double x1, double y1, double z1,
                                 double x2, double y2, double z2,
                                 float r, float g, float b, float a, float width) {
        // 简化实现：直接绘制线段
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z1)
            .color(r, g, b, a);
        buffer.vertex(matrix, (float) x2, (float) y2, (float) z2)
            .color(r, g, b, a);
    }
}

