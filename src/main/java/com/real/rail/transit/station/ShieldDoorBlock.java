package com.real.rail.transit.station;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;

/**
 * 屏蔽门方块
 * 站台屏蔽门，与列车车门联动
 */
public class ShieldDoorBlock extends Block {
    /**
     * 屏蔽门开关状态：true为打开，false为关闭
     */
    public static final BooleanProperty IS_OPEN = BooleanProperty.of("is_open");
    
    /**
     * 屏蔽门类型：true为高架屏蔽门，false为半高屏蔽门
     */
    public static final BooleanProperty IS_UPPER = BooleanProperty.of("is_upper");
    
    public ShieldDoorBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
            .with(IS_OPEN, false)
            .with(IS_UPPER, false));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(IS_OPEN, IS_UPPER);
    }
    
    /**
     * 打开屏蔽门
     * @param world 世界对象
     * @param pos 位置
     */
    public void open(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof ShieldDoorBlock && !state.get(IS_OPEN)) {
            world.setBlockState(pos, state.with(IS_OPEN, true));
            // 播放开关门音效（客户端处理）
            // Note: Sound is handled client-side via TrainSoundController
        }
    }
    
    /**
     * 关闭屏蔽门
     * @param world 世界对象
     * @param pos 位置
     */
    public void close(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof ShieldDoorBlock && state.get(IS_OPEN)) {
            // TODO: 检查是否有障碍物
            world.setBlockState(pos, state.with(IS_OPEN, false));
            // 播放开关门音效（客户端处理）
            // Note: Sound is handled client-side via TrainSoundController
        }
    }
    
    public net.minecraft.util.ActionResult onUse(net.minecraft.block.BlockState state, net.minecraft.world.World world, 
                                                 net.minecraft.util.math.BlockPos pos, net.minecraft.entity.player.PlayerEntity player, 
                                                 net.minecraft.util.Hand hand, net.minecraft.util.hit.BlockHitResult hit) {
        if (!world.isClient) {
            // 检查玩家是否有屏蔽门钥匙
            net.minecraft.item.ItemStack stack = player.getStackInHand(hand);
            if (stack.getItem() instanceof com.real.rail.transit.item.ShieldDoorKeyItem) {
                if (state.get(IS_OPEN)) {
                    close(world, pos);
                } else {
                    open(world, pos);
                }
                return net.minecraft.util.ActionResult.SUCCESS;
            }
        }
        return net.minecraft.util.ActionResult.PASS;
    }
}

