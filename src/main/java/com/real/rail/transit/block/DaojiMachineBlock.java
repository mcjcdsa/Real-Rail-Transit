package com.real.rail.transit.block;

import com.real.rail.transit.RealRailTransitMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
 * 盾构机方块
 * 用于挖掘隧道
 */
public class DaojiMachineBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    
    /**
     * 工作状态：true为工作中，false为停止
     */
    public static final BooleanProperty WORKING = BooleanProperty.of("working");
    
    public DaojiMachineBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(WORKING, false));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WORKING);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            boolean working = state.get(WORKING);
            
            if (working) {
                // 停止挖掘
                world.setBlockState(pos, state.with(WORKING, false));
                player.sendMessage(Text.translatable("block.real-rail-transit-mod.daoji_machine.stopped"), false);
            } else {
                // 开始挖掘（向前挖掘）
                BlockPos digPos = pos.offset(hit.getSide());
                if (canDig(world, digPos)) {
                    world.setBlockState(pos, state.with(WORKING, true));
                    digTunnel(world, digPos, hit.getSide());
                    world.playSound(null, pos, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    player.sendMessage(Text.translatable("block.real-rail-transit-mod.daoji_machine.digging",
                        digPos.getX(), digPos.getY(), digPos.getZ()), false);
                } else {
                    player.sendMessage(Text.translatable("block.real-rail-transit-mod.daoji_machine.cannot_dig"), false);
                }
            }
            
            return ActionResult.SUCCESS;
        }
        return ActionResult.SUCCESS;
    }
    
    /**
     * 检查是否可以挖掘
     */
    private boolean canDig(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        // 可以挖掘石头、泥土等常见方块
        return state.getBlock() == Blocks.STONE ||
               state.getBlock() == Blocks.DIRT ||
               state.getBlock() == Blocks.GRASS_BLOCK ||
               state.getBlock() == Blocks.COBBLESTONE ||
               state.getHardness(world, pos) >= 0;
    }
    
    /**
     * 挖掘隧道
     */
    private void digTunnel(World world, BlockPos pos, net.minecraft.util.math.Direction direction) {
        // 挖掘一个3x3的隧道区域
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos tunnelPos = pos.add(x, y, z);
                    if (canDig(world, tunnelPos)) {
                        world.breakBlock(tunnelPos, false);
                    }
                }
            }
        }
        
        RealRailTransitMod.LOGGER.info("盾构机在 {} 挖掘了隧道", pos);
    }
}

