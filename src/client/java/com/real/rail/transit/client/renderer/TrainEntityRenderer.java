package com.real.rail.transit.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.real.rail.transit.addon.AddonManager;
import com.real.rail.transit.entity.TrainEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * 列车实体渲染器
 * 负责渲染列车模型
 */
public class TrainEntityRenderer extends EntityRenderer<TrainEntity> {
    private static final Identifier TEXTURE = new Identifier("real-rail-transit-mod", "textures/entity/train.png");
    
    public TrainEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }
    
    @Override
    public Identifier getTexture(TrainEntity entity) {
        return TEXTURE;
    }
    
    @Override
    public void render(TrainEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        // 获取列车配置
        String trainId = entity.getTrainId();
        AddonManager.TrainConfig trainConfig = null;
        if (trainId != null && !trainId.isEmpty()) {
            trainConfig = AddonManager.getInstance().getLoadedAddons().stream()
                .filter(train -> train.train_id.equals(trainId))
                .findFirst()
                .orElse(null);
        }
        
        // 计算列车尺寸
        double trainLength = trainConfig != null ? (trainConfig.car_count * trainConfig.car_length) : 20.0;
        double trainWidth = 2.5;
        double trainHeight = 3.5;
        
        // 获取实体位置（相对于相机）
        Vec3d entityPos = entity.getLerpedPos(tickDelta);
        net.minecraft.client.render.Camera camera = net.minecraft.client.MinecraftClient.getInstance().gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        
        // 计算相对于相机的位置
        double relX = entityPos.x - cameraPos.x;
        double relY = entityPos.y - cameraPos.y;
        double relZ = entityPos.z - cameraPos.z;
        
        // 渲染简单的列车模型（使用线框框表示）
        renderTrainBox(matrices, relX, relY, relZ, trainLength, trainWidth, trainHeight, entity.getYaw());
        
        // 调试：在控制台输出渲染信息（仅开发时使用）
        // RealRailTransitMod.LOGGER.debug("Rendering train at: {}, size: {}x{}x{}", entityPos, trainLength, trainWidth, trainHeight);
    }
    
    /**
     * 渲染列车边界框（临时实现，用于显示列车位置）
     */
    private void renderTrainBox(MatrixStack matrices, double relX, double relY, double relZ,
                                double length, double width, double height, float yaw) {
        // 设置渲染状态
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        // 使用 Tessellator 和 BufferBuilder 来绘制线条
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        // 开始绘制线条
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        // 计算旋转后的边界框（简化：不考虑旋转，先显示基本框）
        double halfLength = length / 2;
        double halfWidth = width / 2;
        
        // 计算边界框的8个顶点（相对于实体中心）
        double minX = relX - halfLength;
        double maxX = relX + halfLength;
        double minY = relY;
        double maxY = relY + height;
        double minZ = relZ - halfWidth;
        double maxZ = relZ + halfWidth;
        
        // 绘制边界框的12条边（绿色）
        float r = 0.0f, g = 1.0f, b = 0.0f, a = 1.0f;
        org.joml.Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        // 底部4条边
        drawLine(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        drawLine(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        drawLine(buffer, matrix, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        drawLine(buffer, matrix, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);
        
        // 顶部4条边
        drawLine(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        drawLine(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        drawLine(buffer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        drawLine(buffer, matrix, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);
        
        // 垂直4条边
        drawLine(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        drawLine(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        drawLine(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        drawLine(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
        
        // 结束绘制
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        
        // 恢复渲染状态
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
    
    /**
     * 绘制一条线
     * 注意：在 DEBUG_LINES 模式下，每两个顶点形成一条线，不需要调用 .next()
     */
    private void drawLine(BufferBuilder buffer, org.joml.Matrix4f matrix,
                         double x1, double y1, double z1, double x2, double y2, double z2,
                         float r, float g, float b, float a) {
        // 第一个顶点
        buffer.vertex(matrix, (float)x1, (float)y1, (float)z1)
            .color(r, g, b, a);
        // 第二个顶点（与第一个顶点形成一条线）
        buffer.vertex(matrix, (float)x2, (float)y2, (float)z2)
            .color(r, g, b, a);
    }
    
    // 始终渲染列车（不需要覆盖 shouldRender，使用默认实现即可）
}




