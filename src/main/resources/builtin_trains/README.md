# 模组内置列车目录

此目录用于存放模组自带的内置列车追加包。

## 目录结构

```
builtin_trains/
  └── [列车名称]/
      ├── train_config.json    # 列车配置文件（必须）
      ├── pack.mcmeta          # 资源包元数据
      ├── pack.png             # 资源包图标
      └── assets/              # 资源文件（模型、纹理、声音等）
```

## 配置要求

每个内置列车必须包含 `train_config.json` 文件，且必须设置 `"is_builtin": true`。

## 加载顺序

1. 模组会优先从 `builtin_trains` 目录加载内置列车
2. 内置列车会在用户自定义追加包之前加载
3. 内置列车会被自动标记为 `is_builtin: true`

## 添加新的内置列车

1. 在 `builtin_trains` 目录下创建新的列车文件夹
2. 确保包含完整的 `train_config.json` 配置文件
3. 设置 `"is_builtin": true` 标志
4. 重新构建模组

## 注意事项

- 内置列车会被打包到模组 jar 文件中
- 运行时，内置列车会被复制到游戏目录的 `rrt_addons` 文件夹
- 用户自定义追加包不会覆盖内置列车

