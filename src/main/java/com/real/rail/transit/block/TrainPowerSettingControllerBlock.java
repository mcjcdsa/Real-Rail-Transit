package com.real.rail.transit.block;

import com.real.rail.transit.block.entity.TrainPowerSettingControllerBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * 列车供电设置器方块
 * 用于配置列车的供电参数
 */
public class TrainPowerSettingControllerBlock extends Block implements BlockEntityProvider {
    private static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);
    
    public TrainPowerSettingControllerBlock(Settings settings) {
        super(settings);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TrainPowerSettingControllerBlockEntity(pos, state);
    }
    
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            // TODO: 打开供电设置GUI
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof TrainPowerSettingControllerBlockEntity) {
                TrainPowerSettingControllerBlockEntity powerEntity = (TrainPowerSettingControllerBlockEntity) blockEntity;
                player.sendMessage(Text.translatable("block.real-rail-transit-mod.train_power_setting_controller.info",
                    powerEntity.getPowerType(), powerEntity.getVoltage(), powerEntity.getMaxCurrent()), false);
            }
        }
        return ActionResult.SUCCESS;
    }
}

