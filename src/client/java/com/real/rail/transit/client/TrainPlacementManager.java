package com.real.rail.transit.client;

import com.real.rail.transit.addon.AddonManager;
import net.minecraft.util.math.BlockPos;

/**
 * 列车放置管理器
 * 管理客户端列车放置模式的状态
 */
public class TrainPlacementManager {
    private static TrainPlacementManager instance;
    
    private boolean isPlacing = false;
    private String selectedTrainId = null;
    private BlockPos previewPos = null;
    private boolean canPlace = false;
    
    private TrainPlacementManager() {}
    
    public static TrainPlacementManager getInstance() {
        if (instance == null) {
            instance = new TrainPlacementManager();
        }
        return instance;
    }
    
    /**
     * 开始放置模式
     */
    public void startPlacing(String trainId) {
        this.isPlacing = true;
        this.selectedTrainId = trainId;
    }
    
    /**
     * 取消放置模式
     */
    public void cancelPlacing() {
        this.isPlacing = false;
        this.selectedTrainId = null;
        this.previewPos = null;
        this.canPlace = false;
    }
    
    /**
     * 是否正在放置
     */
    public boolean isPlacing() {
        return isPlacing;
    }
    
    /**
     * 获取选中的列车ID
     */
    public String getSelectedTrainId() {
        return selectedTrainId;
    }
    
    /**
     * 获取选中的列车配置
     */
    public AddonManager.TrainConfig getSelectedTrain() {
        if (selectedTrainId == null) {
            return null;
        }
        return AddonManager.getInstance().getLoadedAddons().stream()
            .filter(train -> train.train_id.equals(selectedTrainId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * 设置预览位置
     */
    public void setPreviewPos(BlockPos pos) {
        this.previewPos = pos;
    }
    
    /**
     * 获取预览位置
     */
    public BlockPos getPreviewPos() {
        return previewPos;
    }
    
    /**
     * 设置是否可以放置
     */
    public void setCanPlace(boolean canPlace) {
        this.canPlace = canPlace;
    }
    
    /**
     * 是否可以放置
     */
    public boolean canPlace() {
        return canPlace;
    }
}

