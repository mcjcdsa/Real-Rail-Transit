package com.real.rail.transit.block;

import com.real.rail.transit.RealRailTransitMod;
import com.real.rail.transit.track.TrackNetwork;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
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
 * 辙叉方块
 * 道岔的核心部分，用于切换轨道方向
 */
public class SwitchBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    
    /**
     * 切换状态：true为左转，false为直行
     */
    public static final BooleanProperty SWITCHED = BooleanProperty.of("switched");
    
    public SwitchBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(SWITCHED, false));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(SWITCHED);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        // 注册到轨道网络
        TrackNetwork.getInstance().registerTrack(world, pos);
    }
    
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        // 从轨道网络移除
        TrackNetwork.getInstance().unregisterTrack(pos);
    }
    
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            // 切换辙叉方向
            boolean switched = state.get(SWITCHED);
            world.setBlockState(pos, state.with(SWITCHED, !switched));
            
            // 更新轨道网络连接
            TrackNetwork.getInstance().registerTrack(world, pos);
            
            String direction = !switched ? "左转" : "直行";
            player.sendMessage(Text.translatable("block.real-rail-transit-mod.switch.switched", direction), false);
            RealRailTransitMod.LOGGER.info("辙叉 {} 切换为: {}", pos, direction);
            
            return ActionResult.SUCCESS;
        }
        return ActionResult.SUCCESS;
    }
    
    /**
     * 切换辙叉方向
     */
    public void switchDirection(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        boolean switched = state.get(SWITCHED);
        world.setBlockState(pos, state.with(SWITCHED, !switched));
        TrackNetwork.getInstance().registerTrack(world, pos);
    }
}

