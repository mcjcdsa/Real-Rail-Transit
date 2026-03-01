package com.real.rail.transit.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * BBModel 渲染器
 * 将 BBModel 数据渲染为 Minecraft 实体
 */
public class BBModelRenderer {
    
    /**
     * 渲染 BBModel 元素
     */
    public static void renderElement(BBModelLoader.BBElement element, MatrixStack matrices, 
                                     VertexConsumer vertexConsumer, int light, int overlay,
                                     float red, float green, float blue, float alpha,
                                     int textureWidth, int textureHeight) {
        matrices.push();
        
        // BBModel 坐标系统：from 和 to 是像素坐标，origin 是旋转中心（像素坐标）
        // 注意：调用此方法时，matrices 已经应用了 scale(1/16)，所以这里直接使用像素值
        // scale 会自动将其转换为方块单位
        
        // 计算元素中心相对于原点的偏移（像素单位）
        double centerX = (element.from[0] + element.to[0]) / 2.0 - element.origin[0];
        double centerY = (element.from[1] + element.to[1]) / 2.0 - element.origin[1];
        double centerZ = (element.from[2] + element.to[2]) / 2.0 - element.origin[2];
        
        // 移动到元素中心（使用像素值，scale 会自动转换）
        matrices.translate(centerX, centerY, centerZ);
        
        // 应用旋转（如果有）
        if (element.rotation != null && 
            (element.rotation[0] != 0 || element.rotation[1] != 0 || element.rotation[2] != 0)) {
            // 旋转是相对于 origin 的，但我们已经移动到中心了
            // 应用旋转（BBModel 使用度，需要转换为弧度）
            if (element.rotation[0] != 0) {
                matrices.multiply(new Quaternionf().rotateX((float)Math.toRadians(element.rotation[0])));
            }
            if (element.rotation[1] != 0) {
                matrices.multiply(new Quaternionf().rotateY((float)Math.toRadians(element.rotation[1])));
            }
            if (element.rotation[2] != 0) {
                matrices.multiply(new Quaternionf().rotateZ((float)Math.toRadians(element.rotation[2])));
            }
        }
        
        // 计算立方体尺寸（像素单位，将在渲染时转换为方块单位）
        // 注意：由于 matrices 已经应用了 scale(1/16)，这里直接使用像素值
        double width = (element.to[0] - element.from[0]);
        double height = (element.to[1] - element.from[1]);
        double depth = (element.to[2] - element.from[2]);
        
        // 渲染立方体的6个面
        // 注意：坐标已经是像素单位，但由于 matrices 已经应用了 scale(1/16)，这里直接使用像素值
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        // 渲染所有面（即使没有UV信息也渲染，使用默认UV）
        // 前面（north，-Z方向，在Minecraft中north是-Z）
        renderFace(vertexConsumer, matrix, 
                   -width/2, -height/2, -depth/2,
                   width/2, -height/2, -depth/2,
                   width/2, height/2, -depth/2,
                   -width/2, height/2, -depth/2,
                   element.faces != null ? element.faces.north : null, textureWidth, textureHeight,
                   red, green, blue, alpha, light, overlay);
        
        // 后面（south，+Z方向）
        renderFace(vertexConsumer, matrix,
                   width/2, -height/2, depth/2,
                   -width/2, -height/2, depth/2,
                   -width/2, height/2, depth/2,
                   width/2, height/2, depth/2,
                   element.faces != null ? element.faces.south : null, textureWidth, textureHeight,
                   red, green, blue, alpha, light, overlay);
        
        // 右面（east，+X方向）
        renderFace(vertexConsumer, matrix,
                   width/2, -height/2, -depth/2,
                   width/2, -height/2, depth/2,
                   width/2, height/2, depth/2,
                   width/2, height/2, -depth/2,
                   element.faces != null ? element.faces.east : null, textureWidth, textureHeight,
                   red, green, blue, alpha, light, overlay);
        
        // 左面（west，-X方向）
        renderFace(vertexConsumer, matrix,
                   -width/2, -height/2, depth/2,
                   -width/2, -height/2, -depth/2,
                   -width/2, height/2, -depth/2,
                   -width/2, height/2, depth/2,
                   element.faces != null ? element.faces.west : null, textureWidth, textureHeight,
                   red, green, blue, alpha, light, overlay);
        
        // 顶面（up，+Y方向）
        renderFace(vertexConsumer, matrix,
                   -width/2, height/2, -depth/2,
                   width/2, height/2, -depth/2,
                   width/2, height/2, depth/2,
                   -width/2, height/2, depth/2,
                   element.faces != null ? element.faces.up : null, textureWidth, textureHeight,
                   red, green, blue, alpha, light, overlay);
        
        // 底面（down，-Y方向）
        renderFace(vertexConsumer, matrix,
                   -width/2, -height/2, depth/2,
                   width/2, -height/2, depth/2,
                   width/2, -height/2, -depth/2,
                   -width/2, -height/2, -depth/2,
                   element.faces != null ? element.faces.down : null, textureWidth, textureHeight,
                   red, green, blue, alpha, light, overlay);
        
        // 渲染子元素
        for (BBModelLoader.BBElement child : element.children) {
            renderElement(child, matrices, vertexConsumer, light, overlay,
                         red, green, blue, alpha, textureWidth, textureHeight);
        }
        
        matrices.pop();
    }
    
    /**
     * 渲染一个面（四边形）
     */
    private static void renderFace(VertexConsumer vertexConsumer, Matrix4f matrix,
                                   double x1, double y1, double z1,
                                   double x2, double y2, double z2,
                                   double x3, double y3, double z3,
                                   double x4, double y4, double z4,
                                   BBModelLoader.BBFace face, int textureWidth, int textureHeight,
                                   float red, float green, float blue, float alpha,
                                   int light, int overlay) {
        // 计算 UV 坐标
        // BBModel UV 格式: [u1, v1, u2, v2] 表示两个对角点（通常是左上角和右下角）
        // 对于四边形，四个顶点的UV应该是：
        // 顶点1 (左上): u1, v1
        // 顶点2 (右上): u2, v1
        // 顶点3 (右下): u2, v2
        // 顶点4 (左下): u1, v2
        float u1 = 0, v1 = 0, u2 = 1, v2 = 0, u3 = 1, v3 = 1, u4 = 0, v4 = 1;
        
        if (face.uv != null && face.uv.length >= 4) {
            // BBModel 的 UV 坐标是相对于纹理的像素坐标
            // 注意：V坐标需要翻转（BBModel使用从上到下的坐标系，OpenGL/Minecraft使用从下到上）
            float rawU1 = (float)(face.uv[0] / textureWidth);
            float rawV1 = 1.0f - (float)(face.uv[1] / textureHeight); // 翻转V坐标
            float rawU2 = (float)(face.uv[2] / textureWidth);
            float rawV2 = 1.0f - (float)(face.uv[3] / textureHeight); // 翻转V坐标
            
            // 设置四个顶点的UV坐标
            u1 = rawU1; // 左上
            v1 = rawV1;
            u2 = rawU2; // 右上
            v2 = rawV1;
            u3 = rawU2; // 右下
            v3 = rawV2;
            u4 = rawU1; // 左下
            v4 = rawV2;
        }
        
        // 计算法向量（用于光照）
        Vector3f normal = calculateNormal(x1, y1, z1, x2, y2, z2, x3, y3, z3);
        
        // 渲染两个三角形组成四边形
        // 第一个三角形
        vertexConsumer.vertex(matrix, (float)x1, (float)y1, (float)z1)
            .color(red, green, blue, alpha)
            .texture(u1, v1)
            .overlay(overlay)
            .light(light)
            .normal(normal.x, normal.y, normal.z);
        
        vertexConsumer.vertex(matrix, (float)x2, (float)y2, (float)z2)
            .color(red, green, blue, alpha)
            .texture(u2, v2)
            .overlay(overlay)
            .light(light)
            .normal(normal.x, normal.y, normal.z);
        
        vertexConsumer.vertex(matrix, (float)x3, (float)y3, (float)z3)
            .color(red, green, blue, alpha)
            .texture(u3, v3)
            .overlay(overlay)
            .light(light)
            .normal(normal.x, normal.y, normal.z);
        
        // 第二个三角形
        vertexConsumer.vertex(matrix, (float)x1, (float)y1, (float)z1)
            .color(red, green, blue, alpha)
            .texture(u1, v1)
            .overlay(overlay)
            .light(light)
            .normal(normal.x, normal.y, normal.z);
        
        vertexConsumer.vertex(matrix, (float)x3, (float)y3, (float)z3)
            .color(red, green, blue, alpha)
            .texture(u3, v3)
            .overlay(overlay)
            .light(light)
            .normal(normal.x, normal.y, normal.z);
        
        vertexConsumer.vertex(matrix, (float)x4, (float)y4, (float)z4)
            .color(red, green, blue, alpha)
            .texture(u4, v4)
            .overlay(overlay)
            .light(light)
            .normal(normal.x, normal.y, normal.z);
    }
    
    /**
     * 计算面的法向量
     */
    private static Vector3f calculateNormal(double x1, double y1, double z1,
                                           double x2, double y2, double z2,
                                           double x3, double y3, double z3) {
        // 计算两个边的向量
        double v1x = x2 - x1;
        double v1y = y2 - y1;
        double v1z = z2 - z1;
        
        double v2x = x3 - x1;
        double v2y = y3 - y1;
        double v2z = z3 - z1;
        
        // 叉积得到法向量
        double nx = v1y * v2z - v1z * v2y;
        double ny = v1z * v2x - v1x * v2z;
        double nz = v1x * v2y - v1y * v2x;
        
        // 归一化
        double length = Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (length > 0) {
            nx /= length;
            ny /= length;
            nz /= length;
        }
        
        return new Vector3f((float)nx, (float)ny, (float)nz);
    }
}

