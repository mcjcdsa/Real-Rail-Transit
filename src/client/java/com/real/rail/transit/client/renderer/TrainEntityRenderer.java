package com.real.rail.transit.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import com.real.rail.transit.RealRailTransitMod;
import com.real.rail.transit.addon.AddonManager;
import com.real.rail.transit.client.model.BBModelLoader;
import com.real.rail.transit.client.model.BBModelRenderer;
import com.real.rail.transit.entity.TrainEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

/**
 * 列车实体渲染器
 * 负责渲染列车模型
 */
public class TrainEntityRenderer extends EntityRenderer<TrainEntity> {
    private static final Identifier TEXTURE = new Identifier("real-rail-transit-mod", "textures/entity/train.png");
    
    // 模型缓存
    private static final Map<String, BBModelLoader.BBModelData> modelCache = new HashMap<>();
    
    public TrainEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }
    
    @Override
    public Identifier getTexture(TrainEntity entity) {
        // 尝试从配置获取纹理
        String trainId = entity.getTrainId();
        if (trainId != null && !trainId.isEmpty()) {
            AddonManager.TrainConfig trainConfig = AddonManager.getInstance().getLoadedAddons().stream()
                .filter(train -> train.train_id.equals(trainId))
                .findFirst()
                .orElse(null);
            
            if (trainConfig != null && trainConfig.resource_path != null) {
                // 解析资源路径（例如 "assets/mtr/kml_1_js/kml_1.png"）
                String[] parts = trainConfig.resource_path.split(":");
                if (parts.length == 2) {
                    return Identifier.of(parts[0], parts[1]);
                } else if (parts.length == 1) {
                    // 假设是模组资源
                    return Identifier.of("real-rail-transit-mod", trainConfig.resource_path);
                }
            }
        }
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
        // 注意：TrainConfig 中没有 width 属性，使用合理的默认值
        // 标准地铁列车宽度约为 2.5-3.0 米，这里使用 2.5 米作为默认值
        double trainLength = trainConfig != null ? (trainConfig.car_count * trainConfig.car_length) : 20.0;
        double trainWidth = 2.5; // 标准地铁列车宽度（米）
        double trainHeight = 3.5; // 标准地铁列车高度（米）
        
        // 应用变换矩阵
        matrices.push();
        
        // 应用旋转（yaw是绕Y轴的旋转）
        float entityYaw = MathHelper.lerpAngleDegrees(tickDelta, entity.prevYaw, entity.getYaw());
        matrices.multiply(new Quaternionf(new AxisAngle4f((float)Math.toRadians(-entityYaw), 0, 1, 0)));
        
        // 尝试渲染实际模型
        boolean modelRendered = renderTrainModel(matrices, vertexConsumers, light, trainConfig, entity);
        
        // 如果模型渲染失败，使用调试渲染
        if (!modelRendered) {
            // 渲染列车边界框
            renderTrainBox(matrices, trainLength, trainWidth, trainHeight, entity);
        }
        
        // 渲染灯光效果（头灯、尾灯）
        renderLights(matrices, trainLength, trainWidth, trainHeight, entity);
        
        matrices.pop();
        
        // 调试：在控制台输出渲染信息（仅开发时使用）
        // RealRailTransitMod.LOGGER.debug("Rendering train at: {}, size: {}x{}x{}, yaw: {}", 
        //     entity.getPos(), trainLength, trainWidth, trainHeight, entityYaw);
    }
    
    /**
     * 渲染列车边界框（临时实现，用于显示列车位置和方向）
     * 使用MatrixStack进行变换，支持旋转
     * 
     * 注意：在 Minecraft 坐标系中：
     * - X 轴：东西方向（东为正）
     * - Y 轴：高度（上为正）
     * - Z 轴：南北方向（南为正）
     * 列车长度沿 Z 轴（前后方向），宽度沿 X 轴（左右方向）
     */
    private void renderTrainBox(MatrixStack matrices, double length, double width, double height, TrainEntity entity) {
        // 设置渲染状态（启用混合，保持深度测试以正确遮挡）
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest(); // 保持深度测试，避免线框穿透地形
        RenderSystem.disableCull(); // 禁用背面剔除，确保从各个角度都能看到
        
        try {
            
            // 使用 Tessellator 和 BufferBuilder 来绘制
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
            // 计算边界框的半尺寸（相对于实体中心）
            double halfLength = length / 2.0;
            double halfWidth = width / 2.0;
            
            // 获取变换矩阵
            org.joml.Matrix4f matrix = matrices.peek().getPositionMatrix();
            
            // 根据列车类型选择颜色（动态材质）
            float[] color = getTrainColor(entity);
            float r = color[0], g = color[1], b = color[2];
            
            // 渲染半透明的实体盒子（更明显可见）
            float boxAlpha = 0.3f; // 半透明
            
            // 开始绘制实心盒子（使用 QUADS 模式）
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            
            // 绘制盒子的6个面
            // 前面（-Z方向）
            drawQuad(buffer, matrix, -halfWidth, 0.0, -halfLength,
                     halfWidth, 0.0, -halfLength,
                     halfWidth, height, -halfLength,
                     -halfWidth, height, -halfLength,
                     r, g, b, boxAlpha);
            
            // 后面（+Z方向）
            drawQuad(buffer, matrix, halfWidth, 0.0, halfLength,
                     -halfWidth, 0.0, halfLength,
                     -halfWidth, height, halfLength,
                     halfWidth, height, halfLength,
                     r, g, b, boxAlpha);
            
            // 左面（-X方向）
            drawQuad(buffer, matrix, -halfWidth, 0.0, halfLength,
                     -halfWidth, 0.0, -halfLength,
                     -halfWidth, height, -halfLength,
                     -halfWidth, height, halfLength,
                     r, g, b, boxAlpha);
            
            // 右面（+X方向）
            drawQuad(buffer, matrix, halfWidth, 0.0, -halfLength,
                     halfWidth, 0.0, halfLength,
                     halfWidth, height, halfLength,
                     halfWidth, height, -halfLength,
                     r, g, b, boxAlpha);
            
            // 顶面（+Y方向）
            drawQuad(buffer, matrix, -halfWidth, height, -halfLength,
                     halfWidth, height, -halfLength,
                     halfWidth, height, halfLength,
                     -halfWidth, height, halfLength,
                     r * 1.2f, g * 1.2f, b * 1.2f, boxAlpha);
            
            // 底面（-Y方向）
            drawQuad(buffer, matrix, -halfWidth, 0.0, halfLength,
                     halfWidth, 0.0, halfLength,
                     halfWidth, 0.0, -halfLength,
                     -halfWidth, 0.0, -halfLength,
                     r * 0.8f, g * 0.8f, b * 0.8f, boxAlpha);
            
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            
            // 绘制线框边框（更明显）
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
            float lineAlpha = 1.0f;
            float lineWidth = 1.5f; // 更粗的线条
            
            // 边界框的8个顶点
            double[] bottomVertices = {
                -halfWidth, 0.0, -halfLength,
                 halfWidth, 0.0, -halfLength,
                 halfWidth, 0.0,  halfLength,
                -halfWidth, 0.0,  halfLength
            };
            
            double[] topVertices = {
                -halfWidth, height, -halfLength,
                 halfWidth, height, -halfLength,
                 halfWidth, height,  halfLength,
                -halfWidth, height,  halfLength
            };
        
        // 底部4条边
            drawLine(buffer, matrix, bottomVertices[0], bottomVertices[1], bottomVertices[2],
                     bottomVertices[3], bottomVertices[4], bottomVertices[5], r, g, b, lineAlpha);
            drawLine(buffer, matrix, bottomVertices[3], bottomVertices[4], bottomVertices[5],
                     bottomVertices[6], bottomVertices[7], bottomVertices[8], r, g, b, lineAlpha);
            drawLine(buffer, matrix, bottomVertices[6], bottomVertices[7], bottomVertices[8],
                     bottomVertices[9], bottomVertices[10], bottomVertices[11], r, g, b, lineAlpha);
            drawLine(buffer, matrix, bottomVertices[9], bottomVertices[10], bottomVertices[11],
                     bottomVertices[0], bottomVertices[1], bottomVertices[2], r, g, b, lineAlpha);
        
        // 顶部4条边
            drawLine(buffer, matrix, topVertices[0], topVertices[1], topVertices[2],
                     topVertices[3], topVertices[4], topVertices[5], r, g, b, lineAlpha);
            drawLine(buffer, matrix, topVertices[3], topVertices[4], topVertices[5],
                     topVertices[6], topVertices[7], topVertices[8], r, g, b, lineAlpha);
            drawLine(buffer, matrix, topVertices[6], topVertices[7], topVertices[8],
                     topVertices[9], topVertices[10], topVertices[11], r, g, b, lineAlpha);
            drawLine(buffer, matrix, topVertices[9], topVertices[10], topVertices[11],
                     topVertices[0], topVertices[1], topVertices[2], r, g, b, lineAlpha);
        
        // 垂直4条边
            drawLine(buffer, matrix, bottomVertices[0], bottomVertices[1], bottomVertices[2],
                     topVertices[0], topVertices[1], topVertices[2], r, g, b, lineAlpha);
            drawLine(buffer, matrix, bottomVertices[3], bottomVertices[4], bottomVertices[5],
                     topVertices[3], topVertices[4], topVertices[5], r, g, b, lineAlpha);
            drawLine(buffer, matrix, bottomVertices[6], bottomVertices[7], bottomVertices[8],
                     topVertices[6], topVertices[7], topVertices[8], r, g, b, lineAlpha);
            drawLine(buffer, matrix, bottomVertices[9], bottomVertices[10], bottomVertices[11],
                     topVertices[9], topVertices[10], topVertices[11], r, g, b, lineAlpha);
            
            // 绘制方向指示线（从中心向前，红色，更粗）
            float dirR = 1.0f, dirG = 0.0f, dirB = 0.0f;
            drawLine(buffer, matrix, 0.0, height / 2.0, 0.0,
                     0.0, height / 2.0, -halfLength, dirR, dirG, dirB, lineAlpha);
        
        // 结束绘制
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        } finally {
        // 恢复渲染状态
            RenderSystem.disableBlend();
        RenderSystem.enableCull();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
    
    /**
     * 绘制一个四边形面
     */
    private void drawQuad(BufferBuilder buffer, org.joml.Matrix4f matrix,
                         double x1, double y1, double z1,
                         double x2, double y2, double z2,
                         double x3, double y3, double z3,
                         double x4, double y4, double z4,
                         float r, float g, float b, float a) {
        // 按逆时针顺序添加4个顶点
        buffer.vertex(matrix, (float)x1, (float)y1, (float)z1).color(r, g, b, a);
        buffer.vertex(matrix, (float)x2, (float)y2, (float)z2).color(r, g, b, a);
        buffer.vertex(matrix, (float)x3, (float)y3, (float)z3).color(r, g, b, a);
        buffer.vertex(matrix, (float)x4, (float)y4, (float)z4).color(r, g, b, a);
    }
    
    /**
     * 绘制一条线
     * 在 DEBUG_LINES 模式下，每两个顶点形成一条线
     * 注意：虽然 DEBUG_LINES 模式下可以不调用 next()，但为了兼容性和规范性，
     * 我们确保每个顶点都正确添加到缓冲区
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
        // 注意：在 DEBUG_LINES 模式下，BufferBuilder 会自动处理顶点配对
        // 不需要手动调用 next()，但确保顶点数据正确添加
    }
    
    /**
     * 渲染灯光效果（头灯、尾灯、运行灯）
     * 根据中国地铁照明标准（GB/T 7928-2025、TB/T 2325.1-2019）实现
     */
    private void renderLights(MatrixStack matrices, double length, double width, double height, TrainEntity entity) {
        // 设置渲染状态（灯光需要混合和深度测试）
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        
        try {
            
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            
            double halfLength = length / 2.0;
            double halfWidth = width / 2.0;
            double headlightHeight = height * 0.3; // 头灯位置在列车高度的30%处
            double taillightHeight = height * 0.25; // 尾灯位置稍低
            double runningLightHeight = height * 0.2; // 运行灯位置更低
            double headlightSize = 0.2; // 头灯大小
            double taillightSize = 0.15; // 尾灯大小
            double runningLightSize = 0.1; // 运行灯大小
            
            org.joml.Matrix4f matrix = matrices.peek().getPositionMatrix();
            
            // 根据列车方向确定前后
            TrainEntity.Direction direction = entity.getDirection();
            boolean isForward = direction == TrainEntity.Direction.FORWARD;
            boolean isBackward = direction == TrainEntity.Direction.BACKWARD;
            
            // 获取运行灯状态
            int runningLightState = entity.getRunningLightState();
            
            // 渲染头灯（前照灯，白色）
            boolean headlightOn = entity.isHeadlightOn();
            boolean headlightDim = entity.isHeadlightDim();
            
            if (headlightOn) {
                // 根据远近光模式设置颜色和亮度
                // 远光：亮白色，近光：稍暗的白色
                float headR = 1.0f;
                float headG = headlightDim ? 0.95f : 1.0f;
                float headB = headlightDim ? 0.9f : 1.0f;
                float headA = headlightDim ? 0.85f : 1.0f;
                
                // 前进时前端头灯，后退时后端头灯
                if (isForward) {
                    // 前端头灯（左右各一个）
                    renderLightQuad(buffer, matrix, -halfWidth * 0.6, headlightHeight, -halfLength - 0.01,
                                   headlightSize, headlightSize, headR, headG, headB, headA);
                    renderLightQuad(buffer, matrix, halfWidth * 0.6, headlightHeight, -halfLength - 0.01,
                                   headlightSize, headlightSize, headR, headG, headB, headA);
                } else if (isBackward) {
                    // 后退时后端头灯
                    renderLightQuad(buffer, matrix, -halfWidth * 0.6, headlightHeight, halfLength + 0.01,
                                   headlightSize, headlightSize, headR, headG, headB, headA);
                    renderLightQuad(buffer, matrix, halfWidth * 0.6, headlightHeight, halfLength + 0.01,
                                   headlightSize, headlightSize, headR, headG, headB, headA);
                }
            }
            
            // 渲染尾灯（标志灯，红色）
            boolean tailLightOn = entity.isTailLightOn();
            
            if (tailLightOn) {
                float tailR = 1.0f, tailG = 0.2f, tailB = 0.2f, tailA = 0.9f;
                
                // 前进时后端尾灯，后退时前端尾灯
                if (isForward) {
                    // 后端尾灯（左右各一个）
                    renderLightQuad(buffer, matrix, -halfWidth * 0.6, taillightHeight, halfLength + 0.01,
                                   taillightSize, taillightSize, tailR, tailG, tailB, tailA);
                    renderLightQuad(buffer, matrix, halfWidth * 0.6, taillightHeight, halfLength + 0.01,
                                   taillightSize, taillightSize, tailR, tailG, tailB, tailA);
                } else if (isBackward) {
                    // 前端尾灯
                    renderLightQuad(buffer, matrix, -halfWidth * 0.6, taillightHeight, -halfLength - 0.01,
                                   taillightSize, taillightSize, tailR, tailG, tailB, tailA);
                    renderLightQuad(buffer, matrix, halfWidth * 0.6, taillightHeight, -halfLength - 0.01,
                                   taillightSize, taillightSize, tailR, tailG, tailB, tailA);
                } else {
                    // 零位时两端都显示尾灯
                    renderLightQuad(buffer, matrix, -halfWidth * 0.6, taillightHeight, -halfLength - 0.01,
                                   taillightSize, taillightSize, tailR, tailG, tailB, tailA);
                    renderLightQuad(buffer, matrix, halfWidth * 0.6, taillightHeight, -halfLength - 0.01,
                                   taillightSize, taillightSize, tailR, tailG, tailB, tailA);
                    renderLightQuad(buffer, matrix, -halfWidth * 0.6, taillightHeight, halfLength + 0.01,
                                   taillightSize, taillightSize, tailR, tailG, tailB, tailA);
                    renderLightQuad(buffer, matrix, halfWidth * 0.6, taillightHeight, halfLength + 0.01,
                                   taillightSize, taillightSize, tailR, tailG, tailB, tailA);
                }
            }
            
            // 渲染运行灯
            // 1=前端白色, 2=后端红色, 3=两端红色, 4=两端白色
            if (runningLightState == 1 || runningLightState == 4) {
                // 白色运行灯（前端或两端）
                float whiteR = 1.0f, whiteG = 1.0f, whiteB = 1.0f, whiteA = 0.8f;
                if (runningLightState == 1) {
                    // 仅前端
                    renderLightQuad(buffer, matrix, -halfWidth * 0.4, runningLightHeight, -halfLength - 0.01,
                                   runningLightSize, runningLightSize, whiteR, whiteG, whiteB, whiteA);
                    renderLightQuad(buffer, matrix, halfWidth * 0.4, runningLightHeight, -halfLength - 0.01,
                                   runningLightSize, runningLightSize, whiteR, whiteG, whiteB, whiteA);
                } else {
                    // 两端
                    renderLightQuad(buffer, matrix, -halfWidth * 0.4, runningLightHeight, -halfLength - 0.01,
                                   runningLightSize, runningLightSize, whiteR, whiteG, whiteB, whiteA);
                    renderLightQuad(buffer, matrix, halfWidth * 0.4, runningLightHeight, -halfLength - 0.01,
                                   runningLightSize, runningLightSize, whiteR, whiteG, whiteB, whiteA);
                    renderLightQuad(buffer, matrix, -halfWidth * 0.4, runningLightHeight, halfLength + 0.01,
                                   runningLightSize, runningLightSize, whiteR, whiteG, whiteB, whiteA);
                    renderLightQuad(buffer, matrix, halfWidth * 0.4, runningLightHeight, halfLength + 0.01,
                                   runningLightSize, runningLightSize, whiteR, whiteG, whiteB, whiteA);
                }
            }
            
            if (runningLightState == 2 || runningLightState == 3) {
                // 红色运行灯（后端或两端）
                float redR = 1.0f, redG = 0.2f, redB = 0.2f, redA = 0.8f;
                if (runningLightState == 2) {
                    // 仅后端
                    renderLightQuad(buffer, matrix, -halfWidth * 0.4, runningLightHeight, halfLength + 0.01,
                                   runningLightSize, runningLightSize, redR, redG, redB, redA);
                    renderLightQuad(buffer, matrix, halfWidth * 0.4, runningLightHeight, halfLength + 0.01,
                                   runningLightSize, runningLightSize, redR, redG, redB, redA);
                } else {
                    // 两端
                    renderLightQuad(buffer, matrix, -halfWidth * 0.4, runningLightHeight, -halfLength - 0.01,
                                   runningLightSize, runningLightSize, redR, redG, redB, redA);
                    renderLightQuad(buffer, matrix, halfWidth * 0.4, runningLightHeight, -halfLength - 0.01,
                                   runningLightSize, runningLightSize, redR, redG, redB, redA);
                    renderLightQuad(buffer, matrix, -halfWidth * 0.4, runningLightHeight, halfLength + 0.01,
                                   runningLightSize, runningLightSize, redR, redG, redB, redA);
                    renderLightQuad(buffer, matrix, halfWidth * 0.4, runningLightHeight, halfLength + 0.01,
                                   runningLightSize, runningLightSize, redR, redG, redB, redA);
                }
            }
            
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        } finally {
            // 恢复渲染状态
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
        }
    }
    
    /**
     * 渲染一个灯光四边形（面向Z轴方向）
     */
    private void renderLightQuad(BufferBuilder buffer, org.joml.Matrix4f matrix,
                                 double centerX, double centerY, double centerZ,
                                 double sizeX, double sizeY,
                                 float r, float g, float b, float a) {
        double halfX = sizeX / 2.0;
        double halfY = sizeY / 2.0;
        
        // 绘制一个面向Z轴的四边形（四个顶点）
        // 顶点顺序：左上、右上、右下、左下
        buffer.vertex(matrix, (float)(centerX - halfX), (float)(centerY + halfY), (float)centerZ)
            .color(r, g, b, a);
        buffer.vertex(matrix, (float)(centerX + halfX), (float)(centerY + halfY), (float)centerZ)
            .color(r, g, b, a);
        buffer.vertex(matrix, (float)(centerX + halfX), (float)(centerY - halfY), (float)centerZ)
            .color(r, g, b, a);
        buffer.vertex(matrix, (float)(centerX - halfX), (float)(centerY - halfY), (float)centerZ)
            .color(r, g, b, a);
    }
    
    /**
     * 根据列车类型获取颜色（动态材质）
     * 可以根据列车ID、配置等返回不同的颜色
     */
    private float[] getTrainColor(TrainEntity entity) {
        String trainId = entity.getTrainId();
        
        // 根据列车ID返回不同颜色
        if (trainId != null && !trainId.isEmpty()) {
            // 可以根据列车ID的哈希值生成不同颜色
            int hash = trainId.hashCode();
            float r = ((hash & 0xFF0000) >> 16) / 255.0f;
            float g = ((hash & 0x00FF00) >> 8) / 255.0f;
            float b = (hash & 0x0000FF) / 255.0f;
            
            // 确保颜色不会太暗
            r = Math.max(0.3f, Math.min(1.0f, r));
            g = Math.max(0.3f, Math.min(1.0f, g));
            b = Math.max(0.3f, Math.min(1.0f, b));
            
            return new float[]{r, g, b};
        }
        
        // 默认绿色
        return new float[]{0.0f, 1.0f, 0.0f};
    }
    
    /**
     * 渲染乘客（占位实现）
     * 未来可以渲染实际的乘客模型
     */
    private void renderPassengers(MatrixStack matrices, double length, double width, double height, TrainEntity entity) {
        // TODO: 实现乘客渲染
        // 可以根据列车配置、乘客数量等渲染乘客模型
        // 目前为占位实现
    }
    
    /**
     * 渲染轨道连接（占位实现）
     * 显示列车与轨道的连接点
     */
    private void renderTrackConnections(MatrixStack matrices, double length, double width, double height, TrainEntity entity) {
        // TODO: 实现轨道连接渲染
        // 可以显示转向架位置、轨道连接点等
        // 目前为占位实现
    }
    
    // 注意：EntityRenderer 的渲染距离由 Minecraft 自动管理
    // 如果需要自定义渲染距离，可以在实体类型注册时设置
    
    /**
     * 渲染列车模型（BBModel）
     * @return 是否成功渲染模型
     */
    private boolean renderTrainModel(MatrixStack matrices, VertexConsumerProvider vertexConsumers, 
                                     int light, AddonManager.TrainConfig trainConfig, TrainEntity entity) {
        if (trainConfig == null || trainConfig.source_path == null || trainConfig.source_path.isEmpty()) {
            RealRailTransitMod.LOGGER.debug("列车配置为空或缺少模型路径，使用调试渲染");
            return false;
        }
        
        try {
            // 解析模型路径
            Identifier modelId = parseModelIdentifier(trainConfig.source_path);
            if (modelId == null) {
                RealRailTransitMod.LOGGER.warn("无法解析模型路径: {}", trainConfig.source_path);
                return false;
            }
            
            String loadMsg = String.format("尝试加载列车模型: %s (路径: %s)", trainConfig.train_id, modelId);
            RealRailTransitMod.LOGGER.info(loadMsg);
            com.real.rail.transit.util.ModRuntimeLog.info(loadMsg);
            
            // 从缓存获取或加载模型
            BBModelLoader.BBModelData modelData = modelCache.get(modelId.toString());
            if (modelData == null) {
                RealRailTransitMod.LOGGER.debug("从资源加载模型: {}", modelId);
                com.real.rail.transit.util.ModRuntimeLog.info("从资源加载模型: " + modelId);
                modelData = BBModelLoader.loadBBModel(modelId);
                if (modelData == null) {
                    String failMsg = "模型加载失败: " + modelId;
                    RealRailTransitMod.LOGGER.warn(failMsg);
                    com.real.rail.transit.util.ModRuntimeLog.warn(failMsg);
                    return false;
                }
                modelCache.put(modelId.toString(), modelData);
                String successMsg = String.format("成功加载模型: %s (%d 个元素)", modelId, modelData.elements.size());
                RealRailTransitMod.LOGGER.info(successMsg);
                com.real.rail.transit.util.ModRuntimeLog.info(successMsg);
            } else {
                RealRailTransitMod.LOGGER.debug("使用缓存的模型: {}", modelId);
            }
            
            // 获取纹理
            Identifier textureId = getTexture(entity);
            RealRailTransitMod.LOGGER.debug("使用纹理: {}", textureId);
            
            // 检查纹理是否存在
            try {
                var resourceManager = MinecraftClient.getInstance().getResourceManager();
                var textureResource = resourceManager.getResource(textureId);
                if (textureResource.isEmpty()) {
                    RealRailTransitMod.LOGGER.warn("纹理不存在: {}，使用默认纹理", textureId);
                    textureId = TEXTURE; // 使用默认纹理
                }
            } catch (Exception e) {
                RealRailTransitMod.LOGGER.warn("检查纹理失败: {}，使用默认纹理", textureId, e);
                textureId = TEXTURE; // 使用默认纹理
            }
            
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(textureId));
            
            // 渲染模型的所有元素
            matrices.push();
            // BBModel 使用像素单位（1像素 = 1/16方块），需要转换为方块单位
            // 同时需要将模型中心对齐到实体位置
            matrices.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
            
            // 计算模型中心偏移（如果需要）
            // BBModel 的原点通常在 (0,0,0)，但模型可能不在原点
            // 这里假设模型已经正确对齐，如果需要可以添加偏移
            
            RealRailTransitMod.LOGGER.debug("开始渲染模型元素，共 {} 个", modelData.elements.size());
            int renderedCount = 0;
            for (BBModelLoader.BBElement element : modelData.elements) {
                try {
                    BBModelRenderer.renderElement(element, matrices, vertexConsumer, light, 0,
                        1.0f, 1.0f, 1.0f, 1.0f,
                        modelData.resolution[0], modelData.resolution[1]);
                    renderedCount++;
                } catch (Exception e) {
                    String errorMsg = "渲染元素失败: " + (element.name != null ? element.name : "未知");
                    RealRailTransitMod.LOGGER.warn(errorMsg, e);
                    com.real.rail.transit.util.ModRuntimeLog.warn(errorMsg);
                }
            }
            RealRailTransitMod.LOGGER.debug("成功渲染 {} 个元素", renderedCount);
            
            matrices.pop();
            String renderCompleteMsg = String.format("列车模型渲染完成: %s (%d 个元素已渲染)", trainConfig.train_id, renderedCount);
            RealRailTransitMod.LOGGER.info(renderCompleteMsg);
            com.real.rail.transit.util.ModRuntimeLog.info(renderCompleteMsg);
            return renderedCount > 0;
        } catch (Exception e) {
            String errorMsg = "渲染列车模型失败: " + trainConfig.source_path;
            RealRailTransitMod.LOGGER.error(errorMsg, e);
            com.real.rail.transit.util.ModRuntimeLog.error(errorMsg, e);
            return false;
        }
    }
    
    /**
     * 解析模型标识符
     */
    private Identifier parseModelIdentifier(String sourcePath) {
        // 处理不同的路径格式
        // 例如: "assets/mtr/kml_1_js/kml_1.bbmodel" 或 "mtr:kml_1_js/kml_1.bbmodel"
        if (sourcePath.contains(":")) {
            String[] parts = sourcePath.split(":", 2);
            return Identifier.of(parts[0], parts[1]);
        } else {
            // 处理 "assets/namespace/path" 格式
            String path = sourcePath;
            if (path.startsWith("assets/")) {
                path = path.substring(7); // 移除 "assets/" 前缀
            }
            
            // 查找第一个 "/" 作为命名空间分隔符
            int firstSlash = path.indexOf('/');
            if (firstSlash > 0) {
                String namespace = path.substring(0, firstSlash);
                String resourcePath = path.substring(firstSlash + 1);
                // 对于内置追加包，资源可能在 builtin_trains 目录下
                // 尝试多个可能的路径
                Identifier[] possibleIds = {
                    Identifier.of(namespace, resourcePath), // 标准路径
                    Identifier.of("real-rail-transit-mod", namespace + "/" + resourcePath), // 模组命名空间
                    Identifier.of("real-rail-transit-mod", "builtin_trains/KunMing Line 1 Train/" + sourcePath) // 内置追加包路径
                };
                
                // 返回第一个可能的路径（实际检查会在加载时进行）
                return possibleIds[0];
            }
            // 默认使用模组命名空间
            return Identifier.of("real-rail-transit-mod", path);
        }
    }
}




