package com.real.rail.transit.addon;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.real.rail.transit.RealRailTransitMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 追加包管理器
 * 管理列车追加包的加载和管理
 */
public class AddonManager {
    private static final AddonManager INSTANCE = new AddonManager();
    private static final Gson GSON = new Gson();
    
    /**
     * 列车配置信息
     */
    public static class TrainConfig {
        public String train_id;
        public String train_name;
        public double max_speed;
        public double acceleration;
        public double deceleration;
        public String power_type;
        public double car_length;
        public int car_count;
        public String source_path;
        public String resource_path;
    }
    
    private final List<TrainConfig> loadedAddons = new ArrayList<>();
    
    private AddonManager() {
    }
    
    public static AddonManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 加载所有追加包
     */
    public void loadAllAddons() {
        Path addonDir = FabricLoader.getInstance().getGameDir().resolve("rrt_addons");
        
        // 创建追加包目录（如果不存在）
        if (!Files.exists(addonDir)) {
            try {
                Files.createDirectories(addonDir);
            } catch (IOException e) {
                RealRailTransitMod.LOGGER.error("无法创建追加包目录", e);
                return;
            }
        }
        
        // 遍历所有追加包目录
        File[] addonFolders = addonDir.toFile().listFiles(File::isDirectory);
        if (addonFolders == null) {
            return;
        }
        
        for (File addonFolder : addonFolders) {
            loadAddon(addonFolder.toPath());
        }
        
        RealRailTransitMod.LOGGER.info("已加载 {} 个追加包", loadedAddons.size());
    }
    
    /**
     * 加载单个追加包
     */
    private void loadAddon(Path addonPath) {
        Path configFile = addonPath.resolve("train_config.json");
        
        if (!Files.exists(configFile)) {
            RealRailTransitMod.LOGGER.warn("追加包 {} 缺少 train_config.json 文件", addonPath.getFileName());
            return;
        }
        
        try (FileReader reader = new FileReader(configFile.toFile())) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            TrainConfig config = GSON.fromJson(json, TrainConfig.class);
            
            // 验证配置
            if (validateConfig(config)) {
                loadedAddons.add(config);
                RealRailTransitMod.LOGGER.info("成功加载追加包: {}", config.train_name);
            } else {
                RealRailTransitMod.LOGGER.warn("追加包配置无效: {}", addonPath.getFileName());
            }
        } catch (IOException e) {
            RealRailTransitMod.LOGGER.error("加载追加包失败: {}", addonPath.getFileName(), e);
        }
    }
    
    /**
     * 验证配置有效性
     */
    private boolean validateConfig(TrainConfig config) {
        return config.train_id != null && !config.train_id.isEmpty()
            && config.train_name != null && !config.train_name.isEmpty()
            && config.max_speed > 0
            && config.acceleration > 0
            && config.deceleration > 0
            && (config.power_type.equals("catenary") || config.power_type.equals("third_rail"))
            && config.car_length > 0
            && config.car_count > 0;
    }
    
    /**
     * 获取所有已加载的追加包
     */
    public List<TrainConfig> getLoadedAddons() {
        return new ArrayList<>(loadedAddons);
    }
    
    /**
     * 根据ID获取追加包配置
     */
    public TrainConfig getAddonById(String trainId) {
        return loadedAddons.stream()
            .filter(config -> config.train_id.equals(trainId))
            .findFirst()
            .orElse(null);
    }
}

