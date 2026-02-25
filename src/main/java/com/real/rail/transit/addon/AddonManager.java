package com.real.rail.transit.addon;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.real.rail.transit.RealRailTransitMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
        public boolean is_builtin = false; // 是否为模组内置追加包
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
        // 首先加载内置追加包（从模组资源中直接加载）
        loadBuiltinAddons();
        
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
        
        // 从模组资源中复制追加包到游戏目录（用于资源访问）
        copyAddonsFromResources(addonDir);
        
        // 遍历所有追加包目录（加载用户自定义追加包）
        File[] addonFolders = addonDir.toFile().listFiles(File::isDirectory);
        if (addonFolders != null) {
            for (File addonFolder : addonFolders) {
                // 跳过已加载的内置追加包
                if (!isBuiltinAddon(addonFolder.getName())) {
                    loadAddon(addonFolder.toPath(), false);
                }
            }
        }
        
        RealRailTransitMod.LOGGER.info("已加载 {} 个追加包（其中 {} 个为内置追加包）", 
            loadedAddons.size(), 
            loadedAddons.stream().filter(c -> c.is_builtin).count());
    }
    
    /**
     * 加载内置追加包（从模组资源中直接加载）
     */
    private void loadBuiltinAddons() {
        try {
            var modContainer = FabricLoader.getInstance().getModContainer(RealRailTransitMod.MOD_ID);
            if (modContainer.isEmpty()) {
                return;
            }
            
            var rootPaths = modContainer.get().getRootPaths();
            
            for (Path modPath : rootPaths) {
                // 优先从 builtin_trains 目录加载（专门的内置列车目录）
                Path builtinTrainsPath = modPath.resolve("builtin_trains");
                if (Files.exists(builtinTrainsPath)) {
                    RealRailTransitMod.LOGGER.info("从 builtin_trains 目录加载内置列车...");
                    loadAddonsFromDirectory(builtinTrainsPath, true);
                    // 如果从 builtin_trains 加载成功，就不再尝试其他路径
                    if (!loadedAddons.isEmpty() && loadedAddons.stream().anyMatch(c -> c.is_builtin)) {
                        break;
                    }
                }
                
                // 兼容性：尝试从 resources/addons 目录加载（开发环境）
                Path addonsResourcePath = modPath.resolve("addons");
                if (Files.exists(addonsResourcePath)) {
                    RealRailTransitMod.LOGGER.debug("从 addons 目录加载内置列车（兼容模式）...");
                    loadAddonsFromDirectory(addonsResourcePath, true);
                    break;
                }
                
                // 尝试从 data/modid/addons 目录加载
                Path dataAddonsPath = modPath.resolve("data").resolve(RealRailTransitMod.MOD_ID).resolve("addons");
                if (Files.exists(dataAddonsPath)) {
                    RealRailTransitMod.LOGGER.debug("从 data 目录加载内置列车...");
                    loadAddonsFromDirectory(dataAddonsPath, true);
                    break;
                }
            }
        } catch (Exception e) {
            RealRailTransitMod.LOGGER.warn("加载内置追加包时出错: {}", e.getMessage());
        }
    }
    
    /**
     * 从目录加载追加包
     */
    private void loadAddonsFromDirectory(Path addonsDir, boolean isBuiltin) {
        try {
            if (!Files.exists(addonsDir)) {
                return;
            }
            
            Files.list(addonsDir).forEach(addonPath -> {
                if (Files.isDirectory(addonPath)) {
                    loadAddon(addonPath, isBuiltin);
                }
            });
        } catch (IOException e) {
            RealRailTransitMod.LOGGER.warn("从目录加载追加包失败: {}", addonsDir, e);
        }
    }
    
    /**
     * 检查是否为内置追加包
     */
    private boolean isBuiltinAddon(String addonName) {
        return loadedAddons.stream()
            .anyMatch(config -> config.is_builtin && 
                addonName.contains(config.train_id) || 
                addonName.contains(config.train_name));
    }
    
    /**
     * 从模组资源中复制追加包到游戏目录
     */
    private void copyAddonsFromResources(Path targetDir) {
        try {
            // 尝试使用 FabricLoader 获取模组路径
            var modContainer = FabricLoader.getInstance().getModContainer(RealRailTransitMod.MOD_ID);
            if (modContainer.isEmpty()) {
                return;
            }
            
            // 获取模组的所有根路径（支持开发环境和打包后的环境）
            var rootPaths = modContainer.get().getRootPaths();
            
            for (Path modPath : rootPaths) {
                // 优先从 builtin_trains 目录复制（专门的内置列车目录）
                Path builtinTrainsPath = modPath.resolve("builtin_trains");
                if (Files.exists(builtinTrainsPath)) {
                    copyAddonDirectory(builtinTrainsPath, targetDir);
                    // 如果从 builtin_trains 复制成功，就不再尝试其他路径
                    break;
                }
                
                // 兼容性：尝试从 resources/addons 目录复制（开发环境）
                Path addonsResourcePath = modPath.resolve("addons");
                if (Files.exists(addonsResourcePath)) {
                    copyAddonDirectory(addonsResourcePath, targetDir);
                    break; // 找到后退出循环
                }
                
                // 尝试从 data/modid/addons 目录复制
                Path dataAddonsPath = modPath.resolve("data").resolve(RealRailTransitMod.MOD_ID).resolve("addons");
                if (Files.exists(dataAddonsPath)) {
                    copyAddonDirectory(dataAddonsPath, targetDir);
                    break;
                }
            }
        } catch (Exception e) {
            RealRailTransitMod.LOGGER.warn("从模组资源复制追加包时出错: {}", e.getMessage());
        }
    }
    
    /**
     * 复制追加包目录
     */
    private void copyAddonDirectory(Path sourceDir, Path targetDir) {
        try {
            if (!Files.exists(sourceDir)) {
                return;
            }
            
            Files.list(sourceDir).forEach(sourcePath -> {
                try {
                    Path targetPath = targetDir.resolve(sourcePath.getFileName());
                    
                    if (Files.isDirectory(sourcePath)) {
                        // 如果是目录，递归复制
                        if (!Files.exists(targetPath)) {
                            Files.createDirectories(targetPath);
                        }
                        copyDirectoryRecursive(sourcePath, targetPath);
                    } else {
                        // 如果是文件，直接复制（如果目标文件不存在）
                        if (!Files.exists(targetPath)) {
                            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } catch (IOException e) {
                    RealRailTransitMod.LOGGER.warn("复制追加包文件失败: {}", sourcePath.getFileName(), e);
                }
            });
        } catch (IOException e) {
            RealRailTransitMod.LOGGER.warn("读取追加包资源目录失败", e);
        }
    }
    
    /**
     * 递归复制目录
     */
    private void copyDirectoryRecursive(Path source, Path target) {
        try {
            if (!Files.exists(target)) {
                Files.createDirectories(target);
            }
            
            Files.list(source).forEach(sourcePath -> {
                try {
                    Path targetPath = target.resolve(sourcePath.getFileName());
                    
                    if (Files.isDirectory(sourcePath)) {
                        copyDirectoryRecursive(sourcePath, targetPath);
                    } else {
                        if (!Files.exists(targetPath)) {
                            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } catch (IOException e) {
                    RealRailTransitMod.LOGGER.warn("复制文件失败: {}", sourcePath.getFileName(), e);
                }
            });
        } catch (IOException e) {
            RealRailTransitMod.LOGGER.warn("复制目录失败: {}", source.getFileName(), e);
        }
    }
    
    /**
     * 加载单个追加包
     */
    private void loadAddon(Path addonPath, boolean isBuiltin) {
        Path configFile = addonPath.resolve("train_config.json");
        
        if (!Files.exists(configFile)) {
            RealRailTransitMod.LOGGER.warn("追加包 {} 缺少 train_config.json 文件", addonPath.getFileName());
            return;
        }
        
        try (FileReader reader = new FileReader(configFile.toFile())) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            TrainConfig config = GSON.fromJson(json, TrainConfig.class);
            
            // 设置内置标志
            config.is_builtin = isBuiltin || (json.has("is_builtin") && json.get("is_builtin").getAsBoolean());
            
            // 检查是否已加载（避免重复加载）
            if (loadedAddons.stream().anyMatch(c -> c.train_id.equals(config.train_id))) {
                RealRailTransitMod.LOGGER.debug("追加包 {} 已加载，跳过", config.train_id);
                return;
            }
            
            // 验证配置
            if (validateConfig(config)) {
                // 内置追加包优先添加到列表前面
                if (config.is_builtin) {
                    loadedAddons.add(0, config);
                } else {
                    loadedAddons.add(config);
                }
                RealRailTransitMod.LOGGER.info("成功加载{}追加包: {} (ID: {})", 
                    config.is_builtin ? "内置" : "", 
                    config.train_name, 
                    config.train_id);
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
    
    /**
     * 获取默认内置列车（第一个内置追加包）
     */
    public TrainConfig getDefaultTrain() {
        return loadedAddons.stream()
            .filter(config -> config.is_builtin)
            .findFirst()
            .orElse(loadedAddons.isEmpty() ? null : loadedAddons.get(0));
    }
    
    /**
     * 获取所有内置追加包
     */
    public List<TrainConfig> getBuiltinAddons() {
        return loadedAddons.stream()
            .filter(config -> config.is_builtin)
            .toList();
    }
}

