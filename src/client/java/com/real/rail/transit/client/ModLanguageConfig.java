package com.real.rail.transit.client;

import com.real.rail.transit.RealRailTransitMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * 模组语言配置
 * 客户端配置，用于保存用户的语言偏好
 */
public class ModLanguageConfig {
    private static final String CONFIG_FILE_NAME = "real_rail_transit_language.properties";
    private static final String LANGUAGE_KEY = "language";
    private static String currentLanguage = "zh_cn"; // 默认语言

    /**
     * 加载语言配置
     */
    public static void load() {
        Path configFile = getConfigFile();
        if (Files.exists(configFile)) {
            try {
                Properties props = new Properties();
                props.load(Files.newInputStream(configFile));
                String lang = props.getProperty(LANGUAGE_KEY, "zh_cn");
                if (isValidLanguageCode(lang)) {
                    currentLanguage = lang;
                    RealRailTransitMod.LOGGER.info("加载语言配置: {}", currentLanguage);
                }
            } catch (IOException e) {
                RealRailTransitMod.LOGGER.warn("加载语言配置失败", e);
            }
        } else {
            // 如果配置文件不存在，使用默认语言
            // 注意：Minecraft的语言设置由游戏本身管理，模组语言跟随游戏语言
            currentLanguage = "zh_cn";
        }
    }

    /**
     * 保存语言配置
     */
    public static void save(String languageCode) {
        if (!isValidLanguageCode(languageCode)) {
            RealRailTransitMod.LOGGER.warn("无效的语言代码: {}", languageCode);
            return;
        }

        currentLanguage = languageCode;
        Path configFile = getConfigFile();

        try {
            Properties props = new Properties();
            props.setProperty(LANGUAGE_KEY, languageCode);
            
            // 确保配置目录存在
            Files.createDirectories(configFile.getParent());
            
            // 保存配置
            props.store(Files.newOutputStream(configFile), 
                "Real Rail Transit Mod Language Configuration");
            
            RealRailTransitMod.LOGGER.info("保存语言配置: {}", languageCode);
        } catch (IOException e) {
            RealRailTransitMod.LOGGER.error("保存语言配置失败", e);
        }
    }

    /**
     * 获取当前语言代码
     */
    public static String getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * 获取配置文件路径
     */
    private static Path getConfigFile() {
        return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
    }

    /**
     * 验证语言代码是否有效
     */
    private static boolean isValidLanguageCode(String langCode) {
        return langCode != null && (langCode.equals("zh_cn") || langCode.equals("en_us"));
    }
}

