package com.real.rail.transit.addon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.real.rail.transit.RealRailTransitMod;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * MTR 追加包格式转换器
 * 将 MTR 追加包格式转换为模组格式
 */
public class MTRAddonConverter {
    
    /**
     * 检测追加包类型
     * @param addonPath 追加包路径
     * @return 追加包类型：1=模组格式，2=MTR格式，0=未知
     */
    public static int detectAddonType(Path addonPath) {
        // 检查是否有 train_config.json（模组格式）
        if (Files.exists(addonPath.resolve("train_config.json"))) {
            return 1; // 模组格式
        }
        
        // 检查是否有 pack.mcmeta 和 assets/mtr/（MTR格式）
        if (Files.exists(addonPath.resolve("pack.mcmeta")) && 
            Files.exists(addonPath.resolve("assets/mtr"))) {
            return 2; // MTR格式
        }
        
        // 检查是否有 mtr_custom_resources.json（MTR格式的另一种标识）
        if (Files.exists(addonPath.resolve("assets/mtr/mtr_custom_resources.json"))) {
            return 2; // MTR格式
        }
        
        return 0; // 未知格式
    }
    
    /**
     * 将 MTR 追加包转换为模组格式
     * @param addonPath MTR追加包路径
     * @return 转换后的 TrainConfig 列表
     */
    public static List<AddonManager.TrainConfig> convertMTRAddon(Path addonPath) {
        List<AddonManager.TrainConfig> configs = new ArrayList<>();
        
        try {
            // 读取 MTR 自定义资源文件
            Path mtrResourcesFile = addonPath.resolve("assets/mtr/mtr_custom_resources.json");
            if (!Files.exists(mtrResourcesFile)) {
                RealRailTransitMod.LOGGER.warn("MTR追加包缺少 mtr_custom_resources.json: {}", addonPath);
                return configs;
            }
            
            JsonObject mtrResources;
            try (FileReader reader = new FileReader(mtrResourcesFile.toFile())) {
                mtrResources = JsonParser.parseReader(reader).getAsJsonObject();
            }
            
            // 解析 MTR 格式的列车配置
            // MTR 格式可能有多种结构：
            // 1. custom_trains 对象（常见格式，key为train_id，value为train配置）
            // 2. trains 数组
            // 3. 其他变体
            
            if (mtrResources.has("custom_trains")) {
                // MTR 常见格式：custom_trains 对象
                JsonObject customTrains = mtrResources.getAsJsonObject("custom_trains");
                RealRailTransitMod.LOGGER.info("检测到 custom_trains 格式，包含 {} 个列车", customTrains.size());
                for (String trainId : customTrains.keySet()) {
                    JsonObject trainObj = customTrains.getAsJsonObject(trainId);
                    RealRailTransitMod.LOGGER.debug("转换列车: {}", trainId);
                    AddonManager.TrainConfig config = convertMTRTrain(trainObj, addonPath, trainId);
                    if (config != null) {
                        configs.add(config);
                    }
                }
            } else if (mtrResources.has("trains")) {
                // trains 数组格式
                JsonArray trainsArray = mtrResources.getAsJsonArray("trains");
                for (JsonElement trainElement : trainsArray) {
                    JsonObject trainObj = trainElement.getAsJsonObject();
                    String trainId = trainObj.has("id") ? trainObj.get("id").getAsString() : 
                                   (trainObj.has("train_id") ? trainObj.get("train_id").getAsString() : null);
                    AddonManager.TrainConfig config = convertMTRTrain(trainObj, addonPath, trainId);
                    if (config != null) {
                        configs.add(config);
                    }
                }
            } else {
                // 如果没有找到标准格式，尝试其他字段
                RealRailTransitMod.LOGGER.debug("MTR追加包格式可能不同，尝试其他解析方式: {}", addonPath);
            }
            
        } catch (Exception e) {
            RealRailTransitMod.LOGGER.error("转换MTR追加包失败: {}", addonPath, e);
        }
        
        return configs;
    }
    
    /**
     * 转换单个 MTR 列车配置
     */
    private static AddonManager.TrainConfig convertMTRTrain(JsonObject mtrTrain, Path addonPath, String defaultTrainId) {
        try {
            AddonManager.TrainConfig config = new AddonManager.TrainConfig();
            
            // 基本字段映射
            if (mtrTrain.has("id")) {
                config.train_id = mtrTrain.get("id").getAsString();
            } else if (mtrTrain.has("train_id")) {
                config.train_id = mtrTrain.get("train_id").getAsString();
            } else if (defaultTrainId != null) {
                config.train_id = defaultTrainId;
            } else {
                // 如果没有ID，使用文件名或时间戳
                config.train_id = "mtr_train_" + System.currentTimeMillis();
            }
            
            if (mtrTrain.has("name")) {
                config.train_name = mtrTrain.get("name").getAsString();
            } else if (mtrTrain.has("train_name")) {
                config.train_name = mtrTrain.get("train_name").getAsString();
            } else {
                config.train_name = config.train_id; // 使用ID作为名称
            }
            
            // 速度相关
            if (mtrTrain.has("maxSpeed")) {
                config.max_speed = mtrTrain.get("maxSpeed").getAsDouble();
            } else if (mtrTrain.has("max_speed")) {
                config.max_speed = mtrTrain.get("max_speed").getAsDouble();
            } else {
                config.max_speed = 120.0; // 默认值
            }
            
            // 加速度和减速度（MTR可能没有这些字段，使用默认值）
            config.acceleration = mtrTrain.has("acceleration") 
                ? mtrTrain.get("acceleration").getAsDouble() 
                : 0.8;
            config.deceleration = mtrTrain.has("deceleration")
                ? mtrTrain.get("deceleration").getAsDouble()
                : 1.2;
            
            // 供电类型
            if (mtrTrain.has("powerType")) {
                String powerType = mtrTrain.get("powerType").getAsString();
                config.power_type = powerType.equalsIgnoreCase("third_rail") ? "third_rail" : "catenary";
            } else {
                config.power_type = "catenary"; // 默认值
            }
            
            // 车厢信息
            if (mtrTrain.has("carLength")) {
                config.car_length = mtrTrain.get("carLength").getAsDouble();
            } else if (mtrTrain.has("car_length")) {
                config.car_length = mtrTrain.get("car_length").getAsDouble();
            } else {
                config.car_length = 21.0; // 默认值
            }
            
            if (mtrTrain.has("carCount")) {
                config.car_count = mtrTrain.get("carCount").getAsInt();
            } else if (mtrTrain.has("car_count")) {
                config.car_count = mtrTrain.get("car_count").getAsInt();
            } else if (mtrTrain.has("formation")) {
                // 从编组信息计算车厢数量
                JsonArray formation = mtrTrain.getAsJsonArray("formation");
                config.car_count = formation.size();
            } else {
                config.car_count = 6; // 默认值
            }
            
            // 模型路径（MTR格式）
            if (mtrTrain.has("model")) {
                String modelPath = mtrTrain.get("model").getAsString();
                // MTR 使用 mtr: 命名空间或 assets/mtr/ 路径
                if (modelPath.startsWith("mtr:")) {
                    // 转换 mtr:namespace/path 为 assets/mtr/namespace/path
                    String path = modelPath.substring(4); // 移除 "mtr:" 前缀
                    config.source_path = "assets/mtr/" + path;
                } else if (!modelPath.startsWith("assets/")) {
                    config.source_path = "assets/mtr/" + modelPath;
                } else {
                    config.source_path = modelPath;
                }
            } else if (mtrTrain.has("source_path")) {
                config.source_path = mtrTrain.get("source_path").getAsString();
            }
            
            // 纹理路径
            if (mtrTrain.has("texture_id")) {
                // MTR 使用 texture_id 字段
                String textureId = mtrTrain.get("texture_id").getAsString();
                if (textureId.startsWith("mtr:")) {
                    String path = textureId.substring(4);
                    // 通常纹理是 PNG 文件
                    if (!path.endsWith(".png")) {
                        path = path + ".png";
                    }
                    config.resource_path = "assets/mtr/" + path;
                } else {
                    config.resource_path = textureId;
                }
            } else if (mtrTrain.has("texture")) {
                String texturePath = mtrTrain.get("texture").getAsString();
                if (texturePath.startsWith("mtr:")) {
                    String path = texturePath.substring(4);
                    config.resource_path = "assets/mtr/" + path;
                } else if (!texturePath.startsWith("assets/")) {
                    config.resource_path = "assets/mtr/" + texturePath;
                } else {
                    config.resource_path = texturePath;
                }
            } else if (mtrTrain.has("resource_path")) {
                config.resource_path = mtrTrain.get("resource_path").getAsString();
            }
            
            // 注意：is_builtin 标志会在 AddonManager.loadMTRFormatAddon() 中根据参数设置
            // 这里不设置，让调用者决定
            
            RealRailTransitMod.LOGGER.info("成功转换MTR列车: {} (ID: {}, 模型: {}, 纹理: {})", 
                config.train_name, config.train_id, 
                config.source_path != null ? config.source_path : "未设置",
                config.resource_path != null ? config.resource_path : "未设置");
            return config;
            
        } catch (Exception e) {
            RealRailTransitMod.LOGGER.error("转换MTR列车配置失败", e);
            return null;
        }
    }
    
    /**
     * 检查路径是否为 MTR 追加包
     */
    public static boolean isMTRAddon(Path addonPath) {
        return detectAddonType(addonPath) == 2;
    }
    
    /**
     * 检查路径是否为模组格式追加包
     */
    public static boolean isModAddon(Path addonPath) {
        return detectAddonType(addonPath) == 1;
    }
}

