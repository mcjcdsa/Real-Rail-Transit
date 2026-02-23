package com.real.rail.transit.station;

import com.real.rail.transit.RealRailTransitMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * 电子门控制器方块
 * 控制电子门的开关
 */
public class ElectronicDoorControllerBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 2.0);
    
    /**
     * 门状态：true为开启，false为关闭
     */
    public static final BooleanProperty OPEN = BooleanProperty.of("open");
    
    public ElectronicDoorControllerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(OPEN, false));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            // 切换门的状态
            boolean isOpen = state.get(OPEN);
            world.setBlockState(pos, state.with(OPEN, !isOpen));
            
            // 播放开关门音效
            world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 0.5f, 1.0f);
            
            String status = !isOpen ? "开启" : "关闭";
            player.sendMessage(Text.translatable("block.real-rail-transit-mod.electronic_door_controller.switched", status), false);
            RealRailTransitMod.LOGGER.info("电子门控制器 {} 状态切换为: {}", pos, status);
            
            return ActionResult.SUCCESS;
        }
        return ActionResult.SUCCESS;
    }
    
    /**
     * 打开门
     */
    public void open(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!state.get(OPEN)) {
            world.setBlockState(pos, state.with(OPEN, true));
            world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 0.5f, 1.0f);
        }
    }
    
    /**
     * 关闭门
     */
    public void close(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.get(OPEN)) {
            world.setBlockState(pos, state.with(OPEN, false));
            world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 0.5f, 1.0f);
        }
    }
}

