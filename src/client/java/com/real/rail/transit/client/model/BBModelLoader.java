package com.real.rail.transit.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.real.rail.transit.RealRailTransitMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * BBModel 模型加载器
 * 用于加载和解析 Blockbench 模型格式（.bbmodel）
 */
public class BBModelLoader {
    
    /**
     * BBModel 元素（立方体）
     */
    public static class BBElement {
        public String name;
        public double[] from = new double[3];
        public double[] to = new double[3];
        public double[] origin = new double[3];
        public double[] rotation = new double[3];
        public BBElementFaces faces;
        public List<BBElement> children = new ArrayList<>();
    }
    
    /**
     * BBModel 面的 UV 信息
     */
    public static class BBElementFaces {
        public BBFace north;
        public BBFace south;
        public BBFace east;
        public BBFace west;
        public BBFace up;
        public BBFace down;
    }
    
    /**
     * BBModel 面的 UV 坐标
     */
    public static class BBFace {
        public double[] uv;
        public int texture;
    }
    
    /**
     * BBModel 数据
     */
    public static class BBModelData {
        public String name;
        public int[] resolution = new int[]{64, 64};
        public List<BBElement> elements = new ArrayList<>();
        public JsonObject meta;
    }
    
    /**
     * 从资源加载 BBModel
     * 优先从文件系统加载（rrt_addons目录），如果失败则尝试从资源管理器加载
     */
    public static BBModelData loadBBModel(Identifier modelId) {
        // 首先尝试从文件系统加载（追加包目录）
        BBModelData fileData = loadBBModelFromFileSystem(modelId);
        if (fileData != null) {
            return fileData;
        }
        
        // 如果文件系统加载失败，尝试从资源管理器加载
        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
        
        // 尝试多个可能的路径
        Identifier[] possibleIds = {
            modelId, // 原始路径 (例如: mtr:kml_1_js/kml_1.bbmodel)
            Identifier.of(modelId.getNamespace(), modelId.getPath()), // 确保路径正确
            Identifier.of("real-rail-transit-mod", modelId.getPath()) // 模组命名空间
        };
        
        for (Identifier tryId : possibleIds) {
            try {
                Optional<Resource> resource = resourceManager.getResource(tryId);
                
                if (resource.isPresent()) {
                    RealRailTransitMod.LOGGER.info("找到模型资源: {}", tryId);
                    com.real.rail.transit.util.ModRuntimeLog.info("找到模型资源: " + tryId);
                    try (InputStream stream = resource.get().getInputStream();
                         InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                        
                        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                        BBModelData data = parseBBModel(json);
                        if (data != null && !data.elements.isEmpty()) {
                            String msg = String.format("成功解析模型: %s (%d 个元素)", tryId, data.elements.size());
                            RealRailTransitMod.LOGGER.info(msg);
                            com.real.rail.transit.util.ModRuntimeLog.info(msg);
                        } else {
                            String msg = "模型解析成功但无元素: " + tryId;
                            RealRailTransitMod.LOGGER.warn(msg);
                            com.real.rail.transit.util.ModRuntimeLog.warn(msg);
                        }
                        return data;
                    }
                } else {
                    RealRailTransitMod.LOGGER.debug("资源不存在: {}", tryId);
                }
            } catch (Exception e) {
                RealRailTransitMod.LOGGER.debug("尝试加载模型失败: {} - {}", tryId, e.getMessage());
                // 继续尝试下一个路径
            }
        }
        
        String msg = "无法找到模型资源，已尝试所有路径。原始路径: " + modelId;
        RealRailTransitMod.LOGGER.warn(msg);
        com.real.rail.transit.util.ModRuntimeLog.warn(msg);
        return null;
    }
    
    /**
     * 从文件系统加载 BBModel（从追加包目录）
     */
    private static BBModelData loadBBModelFromFileSystem(Identifier modelId) {
        try {
            Path addonDir = net.fabricmc.loader.api.FabricLoader.getInstance()
                .getGameDir().resolve("rrt_addons");
            
            // 尝试在所有追加包目录中查找模型文件
            if (Files.exists(addonDir)) {
                File[] addonFolders = addonDir.toFile().listFiles(File::isDirectory);
                if (addonFolders != null) {
                    for (File addonFolder : addonFolders) {
                        // 构建可能的模型文件路径
                        // 例如：assets/mtr/kml_1_js/kml_1.bbmodel
                        String modelPath = modelId.getPath();
                        
                        // 尝试多种路径组合
                        Path[] possiblePaths = {
                            // 标准路径：assets/namespace/path
                            addonFolder.toPath().resolve("assets").resolve(modelId.getNamespace()).resolve(modelPath),
                            // 如果路径已经包含 assets/
                            addonFolder.toPath().resolve(modelPath.startsWith("assets/") ? modelPath : "assets/" + modelPath),
                            // 直接路径
                            addonFolder.toPath().resolve(modelPath),
                            // 如果路径包含命名空间，尝试移除命名空间部分
                            addonFolder.toPath().resolve("assets").resolve(modelPath)
                        };
                        
                        for (Path modelFile : possiblePaths) {
                            if (Files.exists(modelFile) && Files.isRegularFile(modelFile)) {
                                String fileMsg = "从文件系统找到模型: " + modelFile;
                                RealRailTransitMod.LOGGER.info(fileMsg);
                                com.real.rail.transit.util.ModRuntimeLog.info(fileMsg);
                                try (InputStreamReader inputReader = new InputStreamReader(
                                        Files.newInputStream(modelFile), StandardCharsets.UTF_8)) {
                                    
                                    JsonObject json = JsonParser.parseReader(inputReader).getAsJsonObject();
                                    BBModelData data = parseBBModel(json);
                                    if (data != null && !data.elements.isEmpty()) {
                                        String successMsg = String.format("成功从文件系统解析模型: %s (%d 个元素)", modelFile, data.elements.size());
                                        RealRailTransitMod.LOGGER.info(successMsg);
                                        com.real.rail.transit.util.ModRuntimeLog.info(successMsg);
                                        return data;
                                    } else if (data != null) {
                                        String warnMsg = "模型文件解析成功但无元素: " + modelFile;
                                        RealRailTransitMod.LOGGER.warn(warnMsg);
                                        com.real.rail.transit.util.ModRuntimeLog.warn(warnMsg);
                                    }
                                } catch (Exception e) {
                                    RealRailTransitMod.LOGGER.debug("解析模型文件失败: {} - {}", modelFile, e.getMessage());
                                    com.real.rail.transit.util.ModRuntimeLog.warn("解析模型文件失败: " + modelFile + " - " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            RealRailTransitMod.LOGGER.debug("从文件系统加载模型失败: {} - {}", modelId, e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 解析 BBModel JSON
     */
    private static BBModelData parseBBModel(JsonObject json) {
        BBModelData data = new BBModelData();
        
        // 解析基本信息
        if (json.has("name")) {
            data.name = json.get("name").getAsString();
        }
        
        // 解析分辨率
        if (json.has("resolution")) {
            JsonObject res = json.getAsJsonObject("resolution");
            data.resolution[0] = JsonHelper.getInt(res, "width", 64);
            data.resolution[1] = JsonHelper.getInt(res, "height", 64);
        }
        
        // 解析元数据
        if (json.has("meta")) {
            data.meta = json.getAsJsonObject("meta");
        }
        
        // 解析元素
        if (json.has("elements")) {
            JsonArray elementsArray = json.getAsJsonArray("elements");
            for (JsonElement element : elementsArray) {
                BBElement elementObj = parseElement(element.getAsJsonObject());
                if (elementObj != null) {
                    data.elements.add(elementObj);
                }
            }
        }
        
        return data;
    }
    
    /**
     * 解析单个元素
     */
    private static BBElement parseElement(JsonObject json) {
        BBElement element = new BBElement();
        
        // 解析名称
        if (json.has("name")) {
            element.name = json.get("name").getAsString();
        }
        
        // 解析 from 和 to（立方体的两个对角点）
        if (json.has("from")) {
            JsonArray fromArray = json.getAsJsonArray("from");
            for (int i = 0; i < 3 && i < fromArray.size(); i++) {
                element.from[i] = fromArray.get(i).getAsDouble();
            }
        }
        
        if (json.has("to")) {
            JsonArray toArray = json.getAsJsonArray("to");
            for (int i = 0; i < 3 && i < toArray.size(); i++) {
                element.to[i] = toArray.get(i).getAsDouble();
            }
        }
        
        // 解析原点（旋转中心）
        if (json.has("origin")) {
            JsonArray originArray = json.getAsJsonArray("origin");
            for (int i = 0; i < 3 && i < originArray.size(); i++) {
                element.origin[i] = originArray.get(i).getAsDouble();
            }
        }
        
        // 解析旋转
        if (json.has("rotation")) {
            JsonArray rotationArray = json.getAsJsonArray("rotation");
            for (int i = 0; i < 3 && i < rotationArray.size(); i++) {
                element.rotation[i] = rotationArray.get(i).getAsDouble();
            }
        }
        
        // 解析面
        if (json.has("faces")) {
            element.faces = parseFaces(json.getAsJsonObject("faces"));
        }
        
        // 解析子元素
        if (json.has("children")) {
            JsonArray childrenArray = json.getAsJsonArray("children");
            for (JsonElement child : childrenArray) {
                BBElement childObj = parseElement(child.getAsJsonObject());
                if (childObj != null) {
                    element.children.add(childObj);
                }
            }
        }
        
        return element;
    }
    
    /**
     * 解析面的 UV 信息
     */
    private static BBElementFaces parseFaces(JsonObject json) {
        BBElementFaces faces = new BBElementFaces();
        
        String[] faceNames = {"north", "south", "east", "west", "up", "down"};
        BBFace[] faceArray = {faces.north, faces.south, faces.east, faces.west, faces.up, faces.down};
        
        for (int i = 0; i < faceNames.length; i++) {
            if (json.has(faceNames[i])) {
                JsonObject faceJson = json.getAsJsonObject(faceNames[i]);
                BBFace face = new BBFace();
                
                if (faceJson.has("uv")) {
                    JsonArray uvArray = faceJson.getAsJsonArray("uv");
                    face.uv = new double[4];
                    for (int j = 0; j < 4 && j < uvArray.size(); j++) {
                        face.uv[j] = uvArray.get(j).getAsDouble();
                    }
                }
                
                if (faceJson.has("texture")) {
                    face.texture = faceJson.get("texture").getAsInt();
                }
                
                // 使用反射或 switch 设置对应的面
                switch (i) {
                    case 0: faces.north = face; break;
                    case 1: faces.south = face; break;
                    case 2: faces.east = face; break;
                    case 3: faces.west = face; break;
                    case 4: faces.up = face; break;
                    case 5: faces.down = face; break;
                }
            }
        }
        
        return faces;
    }
}

