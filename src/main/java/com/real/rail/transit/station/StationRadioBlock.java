package com.real.rail.transit.station;

import com.real.rail.transit.station.entity.StationRadioBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * 车站广播器方块
 * 播放到站提示、安全广播、调度广播
 */
public class StationRadioBlock extends Block implements BlockEntityProvider {
    private static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 8.0, 14.0);
    
    /**
     * 广播状态：true为播放中，false为停止
     */
    public static final BooleanProperty PLAYING = BooleanProperty.of("playing");
    
    public StationRadioBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(PLAYING, false));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PLAYING);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new StationRadioBlockEntity(pos, state);
    }
    
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, 
                             net.minecraft.util.Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            NamedScreenHandlerFactory screenHandlerFactory = createScreenHandlerFactory(state, world, pos);
            if (screenHandlerFactory != null) {
                player.openHandledScreen(screenHandlerFactory);
            }
        }
        return ActionResult.SUCCESS;
    }
    
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof StationRadioBlockEntity) {
            BlockPos finalPos = pos.toImmutable();
            return new NamedScreenHandlerFactory() {
                @Override
                public Text getDisplayName() {
                    return Text.translatable("gui.real-rail-transit-mod.station_radio.title");
                }
                
                @Override
                public net.minecraft.screen.ScreenHandler createMenu(int syncId, net.minecraft.entity.player.PlayerInventory inventory, net.minecraft.entity.player.PlayerEntity player) {
                    return new com.real.rail.transit.station.screen.StationRadioScreenHandler(syncId, inventory, finalPos);
                }
            };
        }
        return null;
    }
    
    /**
     * 播放广播
     */
    public void playBroadcast(World world, BlockPos pos, String message) {
        BlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.with(PLAYING, true));
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof StationRadioBlockEntity) {
            ((StationRadioBlockEntity) blockEntity).setBroadcastMessage(message);
            ((StationRadioBlockEntity) blockEntity).setPlaying(true);
        }
        // TODO: 播放音效和显示文本
    }
    
    /**
     * 停止广播
     */
    public void stopBroadcast(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.with(PLAYING, false));
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof StationRadioBlockEntity) {
            ((StationRadioBlockEntity) blockEntity).setPlaying(false);
        }
    }
}

