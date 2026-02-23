package com.real.rail.transit.client.renderer;

import com.real.rail.transit.entity.TrainEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

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
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        
        matrices.push();
        
        // 旋转列车模型以匹配移动方向
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
        
        // TODO: 渲染列车模型
        // 这里需要根据追加包加载的模型进行渲染
        // 可以使用 BlockEntityRenderer 或自定义模型渲染
        
        matrices.pop();
    }
}




