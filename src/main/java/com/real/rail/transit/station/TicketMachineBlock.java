package com.real.rail.transit.station;

import com.real.rail.transit.station.entity.TicketMachineBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.entity.player.PlayerInventory;
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
 * 售票机方块
 * 模拟购票、出票、支付交互设备
 */
public class TicketMachineBlock extends Block implements BlockEntityProvider {
    private static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
    
    /**
     * 机器状态：true为正常，false为故障
     */
    public static final BooleanProperty WORKING = BooleanProperty.of("working");
    
    public TicketMachineBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(WORKING, true));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WORKING);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TicketMachineBlockEntity(pos, state);
    }
    
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, 
                             Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            try {
                NamedScreenHandlerFactory screenHandlerFactory = createScreenHandlerFactory(state, world, pos);
                if (screenHandlerFactory != null) {
                    player.openHandledScreen(screenHandlerFactory);
                } else {
                    com.real.rail.transit.RealRailTransitMod.LOGGER.warn("无法创建售票机GUI：BlockEntity为null或类型不匹配，位置: {}", pos);
                }
            } catch (Exception e) {
                com.real.rail.transit.RealRailTransitMod.LOGGER.error("打开售票机GUI时出错，位置: {}", pos, e);
            }
        }
        return ActionResult.SUCCESS;
    }
    
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof TicketMachineBlockEntity) {
            BlockPos finalPos = pos.toImmutable();
            return new NamedScreenHandlerFactory() {
                @Override
                public Text getDisplayName() {
                    return Text.translatable("gui.real-rail-transit-mod.ticket_machine.title");
                }
                
                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
                    return new com.real.rail.transit.station.screen.TicketMachineScreenHandler(syncId, inventory, finalPos);
                }
                
                public void writeScreenOpeningData(PlayerEntity player, PacketByteBuf buf) {
                    buf.writeBlockPos(finalPos);
                }
            };
        }
        com.real.rail.transit.RealRailTransitMod.LOGGER.warn("售票机BlockEntity不存在或类型不匹配，位置: {}, BlockEntity类型: {}", 
            pos, blockEntity != null ? blockEntity.getClass().getName() : "null");
        return null;
    }
}

