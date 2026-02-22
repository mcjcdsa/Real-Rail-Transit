package com.real.rail.transit.client.renderer;

import com.real.rail.transit.block.SignalBlock;
import com.real.rail.transit.block.entity.SignalBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 信号机方块实体渲染器
 * 渲染信号机的灯光效果
 */
public class SignalBlockEntityRenderer implements BlockEntityRenderer<SignalBlockEntity> {
    public SignalBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }
    
    @Override
    public void render(SignalBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        World world = entity.getWorld();
        BlockPos pos = entity.getPos();
        BlockState state = world.getBlockState(pos);
        
        if (!(state.getBlock() instanceof SignalBlock)) {
            return;
        }
        
        SignalBlock.SignalState signalState = state.get(SignalBlock.SIGNAL_STATE);
        
        matrices.push();
        
        // 根据信号状态渲染不同颜色的灯光
        float red = 0.0f;
        float green = 0.0f;
        float blue = 0.0f;
        float alpha = 1.0f;
        
        switch (signalState) {
            case RED:
                red = 1.0f;
                break;
            case YELLOW:
                red = 1.0f;
                green = 0.8f;
                break;
            case GREEN:
                green = 1.0f;
                break;
            case GUIDING:
                red = 0.5f;
                green = 0.5f;
                blue = 1.0f;
                break;
        }
        
        // TODO: 使用发光方块或自定义模型渲染信号灯
        // 可以使用 BlockEntityModel 或直接渲染发光方块
        
        matrices.pop();
    }
}

