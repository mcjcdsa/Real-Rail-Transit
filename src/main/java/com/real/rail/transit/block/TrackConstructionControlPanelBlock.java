package com.real.rail.transit.block;

import com.real.rail.transit.block.screen.TrackConstructionControlPanelScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * 线路建设控制面板方块
 * 用于管理线路建设相关设置
 */
public class TrackConstructionControlPanelBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 2.0);
    
    public TrackConstructionControlPanelBlock(Settings settings) {
        super(settings);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            try {
                NamedScreenHandlerFactory screenHandlerFactory = createScreenHandlerFactory(state, world, pos);
                if (screenHandlerFactory != null) {
                    player.openHandledScreen(screenHandlerFactory);
                }
            } catch (Exception e) {
                com.real.rail.transit.RealRailTransitMod.LOGGER.error("打开线路建设控制面板GUI时出错", e);
            }
        }
        return ActionResult.SUCCESS;
    }
    
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        BlockPos finalPos = pos.toImmutable();
        return new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return Text.translatable("gui.real-rail-transit-mod.track_construction_control_panel.title");
            }
            
            @Override
            public net.minecraft.screen.ScreenHandler createMenu(int syncId, net.minecraft.entity.player.PlayerInventory inventory, net.minecraft.entity.player.PlayerEntity player) {
                return new TrackConstructionControlPanelScreenHandler(syncId, inventory, finalPos);
            }
            
            public void writeScreenOpeningData(PlayerEntity player, PacketByteBuf buf) {
                buf.writeBlockPos(finalPos);
            }
        };
    }
}

