# Real Rail Transit Mod - 建模需求文档

本文档列出了模组中所有需要3D模型和纹理的物品和方块，按优先级和类别分批组织。

---

## 📋 建模优先级说明

- **第一批（核心功能）**：游戏核心玩法必需，优先完成
- **第二批（基础设施）**：车站建设基础，重要但可延后
- **第三批（装饰美化）**：提升视觉效果，最后完成

---

## 🚂 第一批：核心功能物品（优先级：最高）

### 线路建设类（5个）
这些是模组的核心功能方块，必须优先完成。

| 物品ID | 中文名 | 英文名 | 模型需求 | 纹理需求 | 备注 |
|--------|--------|--------|----------|----------|------|
| `track` | 轨道 | Track | ✅ 需要 | ✅ 需要 | 基础轨道方块，已有基础模型 |
| `signal` | 信号机 | Signal Machine | ✅ 需要 | ✅ 需要 | 多状态（红/黄/绿/引导），已有部分模型 |
| `turnout` | 转辙器 | Turnout | ✅ 需要 | ✅ 需要 | 道岔，支持方向切换 |
| `third_rail` | 第三轨 | Third Rail | ✅ 需要 | ✅ 需要 | 供电系统，侧面安装 |
| `contact_network` | 接触网 | Contact Network | ✅ 需要 | ✅ 需要 | 架空供电，需要透明/半透明纹理 |

### 工具物品（3个）
玩家操作必需的工具。

| 物品ID | 中文名 | 英文名 | 模型需求 | 纹理需求 | 备注 |
|--------|--------|--------|----------|----------|------|
| `preset` | 预设器 | Preset | ✅ 需要 | ✅ 需要 | 手持工具，类似刷子 |
| `track_brush` | 线路刷子 | Track Brush | ✅ 需要 | ✅ 需要 | 批量修改工具 |
| `shield_door_key` | 屏蔽门钥匙 | Shield Door Key | ✅ 需要 | ✅ 需要 | 钥匙物品，小型手持物品 |

**第一批总计：8个物品/方块**

---

## 🏢 第二批：车站基础设施（优先级：高）

### 车站设施类（11个）
车站运营必需的功能方块。

| 物品ID | 中文名 | 英文名 | 模型需求 | 纹理需求 | 备注 |
|--------|--------|--------|----------|----------|------|
| `upper_shield_door` | 高架屏蔽门 | Upper Shield Door | ✅ 需要 | ✅ 需要 | 全高屏蔽门，支持开关动画 |
| `lower_shield_door` | 半高屏蔽门 | Lower Shield Door | ✅ 需要 | ✅ 需要 | 半高屏蔽门，支持开关动画 |
| `elevator` | 电梯 | Elevator | ✅ 需要 | ✅ 需要 | 垂直运输，可能需要多层模型 |
| `automatic_staircase` | 自动扶梯 | Automatic Staircase | ✅ 需要 | ✅ 需要 | 斜向运输，可能需要动画纹理 |
| `display_screen` | 显示屏 | Display Screen | ✅ 需要 | ✅ 需要 | 信息显示，需要发光纹理 |
| `arrival_display_screen` | 到站显示屏 | Arrival Display Screen | ✅ 需要 | ✅ 需要 | 到站信息，需要发光纹理 |
| `ticket_machine` | 售票机 | Ticket Machine | ✅ 需要 | ✅ 需要 | 交互设备，需要按钮细节 |
| `gate` | 闸机 | Gate | ✅ 需要 | ✅ 需要 | 检票设备，支持开关状态 |
| `small_tv` | 小电视 | Small TV | ✅ 需要 | ✅ 需要 | 小型显示设备 |
| `station_radio` | 车站广播器 | Station Radio | ✅ 需要 | ✅ 需要 | 广播设备，需要扬声器细节 |

**第二批总计：11个方块**

---

## 🎨 第三批：车站装饰建筑（优先级：中）

### 建筑装饰类（9个）
车站装饰和建筑用方块。

| 物品ID | 中文名 | 英文名 | 模型需求 | 纹理需求 | 备注 |
|--------|--------|--------|----------|----------|------|
| `barricade` | 栏杆 | Barricade | ✅ 需要 | ✅ 需要 | 护栏，可能需要连接模型 |
| `staircase_step` | 楼梯台阶 | Staircase Step | ✅ 需要 | ✅ 需要 | 楼梯，可能需要多方向模型 |
| `glass_wall` | 玻璃墙 | Glass Wall | ✅ 需要 | ✅ 需要 | 透明/半透明纹理 |
| `tile_wall` | 瓷砖墙 | Tile Wall | ✅ 需要 | ✅ 需要 | 装饰墙面 |
| `ceiling` | 天花板 | Ceiling | ✅ 需要 | ✅ 需要 | 基础天花板 |
| `lit_ceiling` | 带灯天花板 | Lit Ceiling | ✅ 需要 | ✅ 需要 | 发光天花板，需要发光纹理 |
| `postage` | 广告牌 | Postage | ✅ 需要 | ✅ 需要 | 广告展示，可能需要自定义内容 |
| `fire_extinguisher` | 灭火器 | Fire Extinguisher | ✅ 需要 | ✅ 需要 | 安全设备，小型方块 |
| `fire_water` | 消防水 | Fire Water | ✅ 需要 | ✅ 需要 | 消防设备 |

**第三批总计：9个方块**

---

## 📊 建模统计

| 批次 | 类别 | 数量 | 优先级 |
|------|------|------|--------|
| 第一批 | 核心功能 | 8个 | ⭐⭐⭐ 最高 |
| 第二批 | 基础设施 | 11个 | ⭐⭐ 高 |
| 第三批 | 装饰建筑 | 9个 | ⭐ 中 |
| **总计** | | **28个** | |

---

## 🎯 建模规范要求

### 模型格式
- **格式**：Minecraft JSON Block Model
- **位置**：`src/main/resources/assets/real-rail-transit-mod/models/block/` 或 `models/item/`
- **命名**：使用小写下划线命名（如 `signal_green.json`）

### 纹理要求
- **格式**：PNG，16x16 或 32x32 像素（推荐32x32）
- **位置**：`src/main/resources/assets/real-rail-transit-mod/textures/block/` 或 `textures/item/`
- **命名**：与模型文件对应

### 特殊要求

#### 信号机（Signal）
- 需要4种状态模型：`signal_red.json`, `signal_yellow.json`, `signal_green.json`, `signal_guiding.json`
- 每种状态需要对应的发光纹理

#### 屏蔽门（Shield Door）
- 需要支持开关状态的模型变化
- 可能需要动画纹理或状态模型

#### 显示屏类
- 需要发光纹理（emissive texture）
- 可能需要动态内容显示

#### 接触网（Contact Network）
- 需要透明/半透明纹理
- 可能需要多方向连接模型

---

## 📝 当前已有模型

根据项目文件检查，以下模型已存在：

✅ **已有模型**：
- `track.json` - 轨道模型
- `signal_red.json` - 红色信号机
- `signal_yellow.json` - 黄色信号机
- `signal_green.json` - 绿色信号机
- `signal_guiding.json` - 引导信号机

❌ **缺失模型**：其余23个物品/方块均需要创建模型

---

## 🔄 建模工作流程建议

### 第一批建模顺序
1. **轨道（track）** - 已有基础，完善即可
2. **信号机（signal）** - 已有部分，补充完整
3. **转辙器（turnout）** - 核心功能，优先完成
4. **第三轨（third_rail）** - 供电系统必需
5. **接触网（contact_network）** - 供电系统必需
6. **预设器（preset）** - 工具物品
7. **线路刷子（track_brush）** - 工具物品
8. **屏蔽门钥匙（shield_door_key）** - 工具物品

### 第二批建模顺序
1. **高架屏蔽门（upper_shield_door）**
2. **半高屏蔽门（lower_shield_door）**
3. **电梯（elevator）**
4. **自动扶梯（automatic_staircase）**
5. **显示屏（display_screen）**
6. **到站显示屏（arrival_display_screen）**
7. **售票机（ticket_machine）**
8. **闸机（gate）**
9. **小电视（small_tv）**
10. **车站广播器（station_radio）**

### 第三批建模顺序
1. **栏杆（barricade）**
2. **楼梯台阶（staircase_step）**
3. **玻璃墙（glass_wall）**
4. **瓷砖墙（tile_wall）**
5. **天花板（ceiling）**
6. **带灯天花板（lit_ceiling）**
7. **广告牌（postage）**
8. **灭火器（fire_extinguisher）**
9. **消防水（fire_water）**

---

## 📌 注意事项

1. **纹理路径**：确保所有纹理文件路径正确，否则会显示紫色/黑色方块
2. **模型引用**：确保模型JSON文件正确引用纹理路径
3. **状态模型**：需要多状态的方块（如信号机）需要多个模型文件
4. **物品模型**：方块物品通常可以复用方块模型，使用 `"parent": "block/xxx"` 引用
5. **发光纹理**：需要发光的方块（显示屏、带灯天花板）需要特殊处理

---

**文档版本**：v1.0  
**最后更新**：2026-02-22  
**维护者**：Real Rail Transit 模组制作工作室

