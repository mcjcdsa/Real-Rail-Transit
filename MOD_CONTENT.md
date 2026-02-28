# Real Rail Transit Mod - 方块和物品列表

## 📦 方块列表

### 🛤️ 线路建设类 (13个)

| 方块名称 | 注册ID | 说明 |
|---------|--------|------|
| 轨道 | `track` | 基础轨道方块 |
| 信号机 | `signal` | 轨道信号机，控制列车运行 |
| 转辙器 | `turnout` | 道岔，用于切换轨道方向 |
| 第三轨 | `third_rail` | 第三轨供电系统 |
| 接触网 | `contact_network` | 接触网供电系统 |
| 连接部分 | `connection_part` | 轨道连接部分 |
| 辙叉 | `switch` | 轨道辙叉 |
| 护轨 | `guard_track` | 护轨方块 |
| 线缆 | `cable` | 信号线缆 |
| 列车供电设置器 | `train_power_setting_controller` | 设置列车供电参数 |
| 盾构机 | `daoji_machine` | 自动挖掘隧道的机器 |
| 清除器 | `cleaner` | 清除轨道相关方块 |
| 信号铺设器 | `signal_layout_controller` | 用于快速铺设信号机 |

### 🏢 车站设施类 (15个)

| 方块名称 | 注册ID | 说明 |
|---------|--------|------|
| 高架屏蔽门 | `upper_shield_door` | 高架站台屏蔽门 |
| 半高屏蔽门 | `lower_shield_door` | 半高站台屏蔽门 |
| 电梯 | `elevator` | 垂直电梯 |
| 自动扶梯 | `automatic_staircase` | 自动扶梯 |
| 显示屏 | `display_screen` | 信息显示屏 |
| 到站显示屏 | `arrival_display_screen` | 显示列车到站信息 |
| 售票机 | `ticket_machine` | 自动售票机 |
| 闸机 | `gate` | 进出站闸机 |
| 小电视 | `small_tv` | 小型电视屏幕 |
| 车站广播器 | `station_radio` | 车站广播系统 |
| 站点标记 | `station_marker` | 标记车站位置 |
| 电梯轨道 | `elevator_track` | 电梯运行轨道 |
| 电子门控制器 | `electronic_door_controller` | 控制电子门开关 |
| 传感器 | `sensor_controller` | 检测列车位置 |
| 传感器铺设器 | `sensor_layout_controller` | 用于快速铺设传感器 |

### 🏗️ 车站建筑类 (8个)

| 方块名称 | 注册ID | 说明 |
|---------|--------|------|
| 栏杆 | `barricade` | 车站栏杆 |
| 楼梯台阶 | `staircase_step` | 楼梯台阶 |
| 玻璃墙 | `glass_wall` | 透明玻璃墙 |
| 瓷砖墙 | `tile_wall` | 瓷砖装饰墙 |
| 天花板 | `ceiling` | 车站天花板 |
| 带灯天花板 | `lit_ceiling` | 带照明的天花板 |
| 广告牌 | `postage` | 广告展示牌 |
| 灭火器 | `fire_extinguisher` | 消防设备 |
| 消防水 | `fire_water` | 消防水源 |

### 🎛️ 控制面板类 (5个，仅作为物品使用)

| 方块名称 | 注册ID | 说明 | 使用方式 |
|---------|--------|------|---------|
| 线路建设控制面板 | `track_construction_control_panel` | 线路建设设置 | 右键打开GUI，不可放置 |
| 车站建设控制面板 | `station_construction_control_panel` | 车站建设设置 | 右键打开GUI，不可放置 |
| 线路控制面板 | `track_control_panel` | 线路运行控制 | 右键打开GUI，不可放置 |
| 列车面板 | `train_panel` | 选择列车、查看编组 | 右键打开GUI，不可放置 |
| 控制面板 | `control_panel` | 设置车站范围、车厂、线路 | 右键打开GUI，不可放置 |

---

## 🎒 物品列表

### 🛠️ 工具物品 (5个)

| 物品名称 | 注册ID | 说明 |
|---------|--------|------|
| 预设器 | `preset` | 生成轨道预设路径 |
| 线路刷子 | `track_brush` | 批量操作轨道 |
| 车站刷子 | `station_brush` | 批量操作车站设施 |
| 设置器 | `setting_controller` | 查看和设置方块属性 |
| 屏蔽门钥匙 | `shield_door_key` | 手动开关屏蔽门 |

### 🎛️ 控制面板物品 (5个，不可放置)

| 物品名称 | 注册ID | 说明 |
|---------|--------|------|
| 线路建设控制面板 | `track_construction_control_panel` | 右键打开线路建设GUI |
| 车站建设控制面板 | `station_construction_control_panel` | 右键打开车站建设GUI |
| 线路控制面板 | `track_control_panel` | 右键打开线路控制GUI |
| 列车面板 | `train_panel` | 右键打开列车选择GUI |
| 控制面板 | `control_panel` | 右键打开控制面板GUI（设置车站、车厂、线路） |

### 🎫 其他物品 (1个)

| 物品名称 | 注册ID | 说明 |
|---------|--------|------|
| 票卡 | `ticket_card` | 用于刷闸机进站（可堆叠64个） |

---

## 📊 统计信息

- **可放置方块总数**: 36个
- **控制面板类（仅物品）**: 5个
- **工具物品**: 5个
- **其他物品**: 1个
- **物品总数**: 11个

---

## 🔍 特殊说明

### 控制面板类方块
以下5个方块**只能作为物品使用，不能放置**：
- 线路建设控制面板
- 车站建设控制面板
- 线路控制面板
- 列车面板
- 控制面板

这些方块在游戏中只能通过右键点击打开GUI界面，无法在世界中放置。

### 功能分类

**线路建设类**: 用于建设和管理轨道系统
**车站设施类**: 用于建设和管理车站设施
**车站建筑类**: 用于装饰和建设车站建筑
**控制面板类**: 用于系统设置和管理
**工具物品**: 用于辅助建设和操作

---

*最后更新: 2026年2月28日*

