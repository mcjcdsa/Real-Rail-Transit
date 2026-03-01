package com.real.rail.transit.client.screen;

import com.real.rail.transit.RealRailTransitMod;
import com.real.rail.transit.addon.AddonManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * 追加包管理界面
 * 支持从网站加载模型和拖放文件添加
 */
public class AddonManagerScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget urlField;
    private ButtonWidget downloadButton;
    private ButtonWidget refreshButton;
    private ButtonWidget openFolderButton;
    private int scrollOffset = 0;
    private static final int LINE_HEIGHT = 20;
    private static final int VISIBLE_ROWS = 10;
    private String statusMessage = "";
    private int statusMessageTicks = 0;
    
    public AddonManagerScreen(Screen parent) {
        super(Text.translatable("screen.real-rail-transit-mod.addon_manager"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        super.init();
        
        // URL 输入框
        this.urlField = new TextFieldWidget(
            this.textRenderer,
            this.width / 2 - 200, 40, 350, 20,
            Text.translatable("screen.real-rail-transit-mod.addon_manager.url_hint")
        );
        this.urlField.setPlaceholder(Text.literal("输入模型文件URL或拖放文件到窗口"));
        this.addDrawableChild(this.urlField);
        
        // 下载按钮
        this.downloadButton = ButtonWidget.builder(
            Text.translatable("screen.real-rail-transit-mod.addon_manager.download"),
            button -> downloadFromUrl()
        ).dimensions(this.width / 2 + 160, 40, 80, 20).build();
        this.addDrawableChild(this.downloadButton);
        
        // 刷新按钮
        this.refreshButton = ButtonWidget.builder(
            Text.translatable("screen.real-rail-transit-mod.addon_manager.refresh"),
            button -> refreshAddons()
        ).dimensions(this.width / 2 - 100, this.height - 30, 80, 20).build();
        this.addDrawableChild(this.refreshButton);
        
        // 打开文件夹按钮
        this.openFolderButton = ButtonWidget.builder(
            Text.translatable("screen.real-rail-transit-mod.addon_manager.open_folder"),
            button -> openAddonFolder()
        ).dimensions(this.width / 2 + 20, this.height - 30, 80, 20).build();
        this.addDrawableChild(this.openFolderButton);
        
        // 导入文件按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("screen.real-rail-transit-mod.addon_manager.import_file"),
            button -> importFile()
        ).dimensions(this.width / 2 - 100, 70, 200, 20).build());
        
        // 返回按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.back"),
            button -> this.client.setScreen(this.parent)
        ).dimensions(this.width / 2 - 100, this.height - 60, 200, 20).build());
        
        // 加载追加包列表
        refreshAddons();
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // 绘制标题
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            this.title,
            this.width / 2,
            20,
            0xFFFFFF
        );
        
        // 绘制状态消息
        if (statusMessageTicks > 0) {
            Integer colorValue = statusMessage.contains("成功") || statusMessage.contains("完成") 
                ? Formatting.GREEN.getColorValue() 
                : Formatting.RED.getColorValue();
            int color = colorValue != null ? colorValue : 0xFFFFFF;
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                statusMessage,
                this.width / 2,
                this.height - 90,
                color
            );
            statusMessageTicks--;
        }
        
        // 绘制提示文本
        context.drawTextWithShadow(
            this.textRenderer,
            Text.translatable("screen.real-rail-transit-mod.addon_manager.hint"),
            10,
            this.height - 20,
            0xAAAAAA
        );
        
        // 绘制追加包列表
        renderAddonList(context, mouseX, mouseY);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    /**
     * 渲染追加包列表
     */
    private void renderAddonList(DrawContext context, int mouseX, int mouseY) {
        int listTop = 100;
        int listLeft = this.width / 2 - 200;
        int listWidth = 400;
        int listHeight = LINE_HEIGHT * VISIBLE_ROWS;
        
        // 绘制列表背景
        context.fill(listLeft, listTop, listLeft + listWidth, listTop + listHeight, 0x80000000);
        
        List<AddonManager.TrainConfig> addons = AddonManager.getInstance().getLoadedAddons();
        int maxScroll = Math.max(0, addons.size() - VISIBLE_ROWS);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        
        for (int i = 0; i < VISIBLE_ROWS; i++) {
            int idx = scrollOffset + i;
            if (idx >= addons.size()) break;
            
            AddonManager.TrainConfig config = addons.get(idx);
            int y = listTop + i * LINE_HEIGHT;
            boolean hovered = mouseX >= listLeft && mouseX <= listLeft + listWidth
                && mouseY >= y && mouseY <= y + LINE_HEIGHT;
            
            if (hovered) {
                context.fill(listLeft, y, listLeft + listWidth, y + LINE_HEIGHT, 0x40303030);
            }
            
            // 绘制追加包名称
            String name = config.train_name != null ? config.train_name : config.train_id;
            if (config.is_builtin) {
                name += " [内置]";
            }
            context.drawTextWithShadow(
                this.textRenderer,
                name,
                listLeft + 5,
                y + 2,
                0xFFFFFF
            );
            
            // 绘制追加包ID
            context.drawTextWithShadow(
                this.textRenderer,
                "ID: " + config.train_id,
                listLeft + 5,
                y + 12,
                0xAAAAAA
            );
        }
    }
    
    /**
     * 从URL下载模型文件
     */
    private void downloadFromUrl() {
        String url = this.urlField.getText().trim();
        if (url.isEmpty()) {
            setStatusMessage("请输入URL", 60);
            return;
        }
        
        // 在新线程中下载，避免阻塞UI
        new Thread(() -> {
            try {
                setStatusMessage("正在下载...", 0);
                
                URL fileUrl = new URL(url);
                String fileName = getFileNameFromUrl(url);
                
                // 创建临时文件
                Path tempFile = Files.createTempFile("rrt_addon_", "_" + fileName);
                
                // 下载文件
                try (var inputStream = fileUrl.openStream()) {
                    Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
                }
                
                // 安装追加包
                installAddon(tempFile.toFile(), fileName);
                
                // 删除临时文件
                Files.deleteIfExists(tempFile);
                
                setStatusMessage("下载并安装成功: " + fileName, 120);
                refreshAddons();
            } catch (Exception e) {
                RealRailTransitMod.LOGGER.error("下载追加包失败", e);
                setStatusMessage("下载失败: " + e.getMessage(), 120);
            }
        }).start();
    }
    
    /**
     * 从URL获取文件名
     */
    private String getFileNameFromUrl(String url) {
        try {
            String path = new URL(url).getPath();
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            if (fileName.isEmpty() || !fileName.contains(".")) {
                fileName = "addon_" + System.currentTimeMillis() + ".zip";
            }
            return fileName;
        } catch (Exception e) {
            return "addon_" + System.currentTimeMillis() + ".zip";
        }
    }
    
    /**
     * 导入文件（使用文件选择对话框）
     */
    private void importFile() {
        // 注意：Minecraft 不直接支持文件选择对话框
        // 这里使用系统文件选择对话框
        java.awt.EventQueue.invokeLater(() -> {
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setDialogTitle("选择追加包文件");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || 
                           f.getName().endsWith(".zip") || 
                           f.getName().endsWith(".bbmodel") ||
                           f.getName().endsWith(".json");
                }
                
                @Override
                public String getDescription() {
                    return "追加包文件 (*.zip, *.bbmodel, *.json)";
                }
            });
            fileChooser.setMultiSelectionEnabled(true);
            
            int result = fileChooser.showOpenDialog(null);
            if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = fileChooser.getSelectedFiles();
                for (File file : selectedFiles) {
                    installAddonFile(file.toPath());
                }
            }
        });
    }
    
    /**
     * 安装追加包文件
     */
    private void installAddonFile(Path filePath) {
        new Thread(() -> {
            try {
                setStatusMessage("正在安装: " + filePath.getFileName(), 0);
                
                boolean success = AddonManager.getInstance().installAddon(filePath);
                
                if (success) {
                    setStatusMessage("安装成功: " + filePath.getFileName(), 120);
                    refreshAddons();
                } else {
                    setStatusMessage("安装失败: " + filePath.getFileName(), 120);
                }
            } catch (Exception e) {
                RealRailTransitMod.LOGGER.error("安装追加包失败", e);
                setStatusMessage("安装失败: " + e.getMessage(), 120);
            }
        }).start();
    }
    
    /**
     * 安装追加包文件（从下载的临时文件）
     */
    private void installAddon(File file, String fileName) {
        installAddonFile(file.toPath());
    }
    
    /**
     * 刷新追加包列表
     */
    private void refreshAddons() {
        AddonManager.getInstance().loadAllAddons();
        setStatusMessage("已刷新追加包列表", 60);
    }
    
    /**
     * 打开追加包文件夹
     */
    private void openAddonFolder() {
        try {
            Path addonDir = net.fabricmc.loader.api.FabricLoader.getInstance()
                .getGameDir().resolve("rrt_addons");
            
            if (!Files.exists(addonDir)) {
                Files.createDirectories(addonDir);
            }
            
            // 使用系统默认程序打开文件夹
            java.awt.Desktop.getDesktop().open(addonDir.toFile());
            setStatusMessage("已打开追加包文件夹", 60);
        } catch (Exception e) {
            RealRailTransitMod.LOGGER.error("打开文件夹失败", e);
            setStatusMessage("打开文件夹失败: " + e.getMessage(), 120);
        }
    }
    
    /**
     * 设置状态消息
     */
    private void setStatusMessage(String message, int ticks) {
        this.statusMessage = message;
        this.statusMessageTicks = ticks;
    }
    
    /**
     * 处理文件拖放
     */
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        // 注意：Minecraft 的拖放支持有限，主要依赖文件选择对话框
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}

