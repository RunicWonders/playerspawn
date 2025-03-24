# PlayerSpawn 插件

一个轻量级的玩家出生点插件，支持识别 Geyser/Floodgate 基岩版玩家。

## 功能特点

- 为 Java 版玩家设置服务器和世界出生点
- 为玩家组设置服务器和世界出生点 
- 为基岩版玩家设置特殊的出生点和重生点
- 为新玩家设置特殊的出生点（包括基岩版新玩家）
- 控制玩家死亡后的重生位置
- 支持多世界设置

## 命令

- `/ps set [~|x y z] [玩家名] [<世界>|server]` - 设置玩家的出生点
- `/ps group <组名> set [~|x y z] [玩家名] [<世界>|server]` - 设置组的出生点
- `/ps floodgate <newplayer|respawn> set [~|x y z] [<世界>|server]` - 设置基岩版玩家的出生点或重生点
- `/ps reload` - 重新加载配置文件
- `/ps help` - 显示帮助信息

## 权限

- `playerspawn.set` - 允许使用 `/ps set` 命令
- `playerspawn.group` - 允许使用 `/ps group` 命令
- `playerspawn.floodgate` - 允许使用 `/ps floodgate` 命令
- `playerspawn.reload` - 允许使用 `/ps reload` 命令
- `playerspawn.group.<组名>` - 玩家属于指定的组

## 配置文件

插件包含两个配置文件：

1. `config.yml` - 主配置文件，包含所有出生点和重生点设置
2. `messages.yml` - 消息配置文件，包含所有插件消息

## 依赖

- **必需**: Paper 1.21+（Java 21）
- **可选**: Floodgate（用于识别基岩版玩家）

## 编译

使用 Maven 构建插件：

```bash
mvn clean package
```

编译后的插件将在 `target` 目录中生成。

## 作者

- 柠枺 