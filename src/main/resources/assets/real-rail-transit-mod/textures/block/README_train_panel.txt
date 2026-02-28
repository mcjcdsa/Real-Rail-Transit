列车面板贴图说明
==================

列车面板方块需要自定义贴图文件。

当前模型文件使用 Minecraft 原版命令方块贴图作为临时方案。

要添加自定义贴图，请执行以下步骤：

1. 创建贴图文件 train_panel.png（16x16像素）
   - 位置：textures/block/train_panel.png
   - 建议设计：控制面板样式，可以显示列车图标或面板界面

2. 更新模型文件 models/block/train_panel.json：
   将 "minecraft:block/command_block_*" 替换为 "real-rail-transit-mod:block/train_panel"

示例模型配置：
{
  "parent": "block/cube",
  "textures": {
    "particle": "real-rail-transit-mod:block/train_panel",
    "down": "minecraft:block/iron_block",
    "up": "real-rail-transit-mod:block/train_panel",
    "north": "real-rail-transit-mod:block/train_panel",
    "south": "real-rail-transit-mod:block/train_panel",
    "east": "minecraft:block/iron_block",
    "west": "minecraft:block/iron_block"
  }
}

注意：由于列车面板是一个薄面板（VoxelShape: 16x12x2），
建议贴图设计为控制面板或显示屏样式。

