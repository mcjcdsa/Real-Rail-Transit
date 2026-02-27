# 驾驶室按钮 API 参考

本文档描述 RRT 模组驾驶室所有按钮的 API，供追加包创建器与积木编程使用。

## 一、按钮类型 (CabInteractionSystem.ButtonType)

| ID | 名称 | 描述 | 对应效果 |
|----|------|------|----------|
| `START_ENGINE` | 启动引擎 | 开启列车动力系统 | `train.setEngineOn(true)` |
| `STOP_ENGINE` | 停止引擎 | 关闭动力并设目标速度为 0 | `train.setEngineOn(false); train.setTargetSpeed(0)` |
| `OPEN_DOORS` | 开门 | 打开附近屏蔽门 | `handleDoors(train, true)` |
| `CLOSE_DOORS` | 关门 | 关闭附近屏蔽门 | `handleDoors(train, false)` |
| `EMERGENCY_BRAKE` | 紧急制动 | 触发紧急制动 | `train.triggerEmergencyBrake()` |
| `MODE_SWITCH` | 模式切换 | 循环切换驾驶模式 | `train.setDrivingMode(nextMode)` |
| `SPEED_UP` | 加速 | 目标速度 +5 m/s | `train.setTargetSpeed(...+ 5.0)` |
| `SPEED_DOWN` | 减速 | 目标速度 -5 m/s | `train.setTargetSpeed(...- 5.0)` |
| `HORN` | 鸣笛 | 播放鸣笛音效 | `SoundSystem.TRAIN_HORN` |
| `CUSTOM` | 自定义 | 追加包自定义逻辑 | 由 `buttonLogic` 配置 |

## 二、按钮详细说明

详见 `addon-editor/CAB_BUTTON_API.md` 或追加包创建器内的「API 参考」面板。

## 三、Java 调用示例

```java
// 处理按钮点击
CabInteractionSystem.getInstance().handleButtonClick(player, CabInteractionSystem.ButtonType.START_ENGINE);

// 获取玩家绑定的列车
TrainEntity train = CabInteractionSystem.getInstance().getPlayerTrain(player);

// 检查驾驶权限
boolean hasPermission = CabInteractionSystem.getInstance().hasDrivingPermission(player, train);
```

