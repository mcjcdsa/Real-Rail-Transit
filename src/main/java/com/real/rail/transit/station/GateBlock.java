package com.real.rail.transit.station;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
 * 闸机方块
 * 检票通行设备，支持票卡验证
 */
public class GateBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    
    /**
     * 闸门状态：true为打开，false为关闭
     */
    public static final BooleanProperty OPEN = BooleanProperty.of("open");
    
    public GateBlock(Settings settings) {
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
    
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, 
                             Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            ItemStack heldItem = player.getStackInHand(hand);
            
            // 检查玩家是否持有票卡
            if (heldItem.getItem() == com.real.rail.transit.registry.ModItems.TICKET_CARD) {
                // 打开闸门
                if (!state.get(OPEN)) {
                    world.setBlockState(pos, state.with(OPEN, true));
                    player.sendMessage(Text.translatable("block.real-rail-transit-mod.gate.opened"), false);
                    
                    // 消耗一张票卡
                    heldItem.decrement(1);
                    
                    // 播放提示音（如果有的话）
                    // world.playSound(null, pos, ModSounds.GATE_BEEP, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    
                    return ActionResult.SUCCESS;
                } else {
                    player.sendMessage(Text.translatable("block.real-rail-transit-mod.gate.already_open"), false);
                }
            } else {
                player.sendMessage(Text.translatable("block.real-rail-transit-mod.gate.no_ticket"), false);
            }
        }
        return ActionResult.SUCCESS;
    }
    
    /**
     * 打开闸门
     */
    public void open(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof GateBlock && !state.get(OPEN)) {
            world.setBlockState(pos, state.with(OPEN, true));
        }
    }
    
    /**
     * 关闭闸门
     */
    public void close(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof GateBlock && state.get(OPEN)) {
            world.setBlockState(pos, state.with(OPEN, false));
        }
    }
}

