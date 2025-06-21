# 贪吃蛇游戏 - 优化版本

一个功能丰富的Java贪吃蛇游戏，具有现代化的界面和强大的功能。

## 🎮 游戏特性

### 核心功能
- **经典贪吃蛇玩法** - 控制蛇吃食物，避免撞墙和撞到自己
- **多种食物类型** - 小食物(1分)和大食物(4分)
- **动态墙壁系统** - 随着分数增加，墙壁会逐渐生成
- **穿墙机制** - 蛇可以从一边穿到另一边
- **智能敌人系统** - 具有不同AI策略的敌人蛇，使用A*寻路算法

### AI敌人系统
- **A*寻路算法** - 敌人能够智能地寻找最短路径
- **多种AI策略**:
  - 🔴 **攻击性(AGGRESSIVE)** - 主动追击玩家
  - 🔵 **防御性(DEFENSIVE)** - 躲避玩家，寻找安全区域
  - 🟡 **食物猎人(FOOD_HUNTER)** - 专注于寻找食物
  - ⚪ **随机(RANDOM)** - 随机移动
- **动态策略调整** - 高分时增加攻击性敌人的生成概率
- **视觉识别** - 不同策略的敌人显示不同颜色
- **智能碰撞避免** - 敌人能够避免撞墙和相互碰撞

### 性能优化
- **双缓冲渲染** - 消除画面闪烁，提升渲染性能
- **优化的碰撞检测** - 使用Set数据结构提高查找效率
- **智能重绘机制** - 只在必要时重绘画面
- **内存管理优化** - 减少对象创建和销毁

### 用户体验优化
- **自定义按键支持** - 支持WASD和方向键控制
- **多种游戏速度** - 慢速、正常、快速、极速四种模式
- **全屏模式** - 按F11切换全屏
- **暂停功能** - 按ESC或P键暂停/恢复游戏
- **快速操作** - F5快速保存，F9快速加载

### 存档系统增强
- **多存档槽支持** - 支持5个存档槽位
- **自动存档** - 可配置的自动存档功能
- **存档压缩** - 使用GZIP压缩存档文件
- **数据校验** - MD5校验和防止存档损坏
- **存档元数据** - 显示存档时间、分数、蛇长度等信息

### 界面优化
- **现代化UI设计** - 统一的颜色主题和字体
- **渐变蛇身效果** - 蛇头黄色，蛇身绿色渐变
- **半透明游戏结束界面** - 更美观的游戏结束显示
- **状态信息显示** - 实时显示分数和蛇长度
- **响应式设计** - 支持不同分辨率

### 代码结构优化
- **配置管理系统** - 集中的游戏配置管理
- **事件驱动架构** - 使用事件系统解耦组件
- **单例模式** - 配置和事件管理器使用单例
- **工厂模式** - 食物和障碍物生成
- **状态机** - 清晰的游戏状态管理

## 🚀 快速开始

### 运行游戏
```bash
# 编译项目
javac -d bin src/main/java/*.java src/main/java/*/*.java src/main/java/*/*/*.java

# 运行游戏
java -cp bin src.main.java.MainMenu
```

### 控制说明
- **方向控制**: WASD 或 方向键
- **暂停/恢复**: ESC 或 P
- **重启游戏**: R (游戏结束时)
- **快速保存**: F5
- **快速加载**: F9
- **全屏切换**: F11
- **打开设置**: 点击右上角齿轮图标

## ⚙️ 配置选项

游戏配置保存在 `snake_config.properties` 文件中：

```properties
# 游戏速度 (毫秒)
game.speed=100

# 音效设置
sound.enabled=true
sound.volume=0.7

# 自动存档设置
auto.save=true
auto.save.interval=30

# 控制键设置
controls.up=UP
controls.down=DOWN
controls.left=LEFT
controls.right=RIGHT
controls.pause=ESCAPE
controls.restart=SPACE
```

## 📁 项目结构

```
Snake/
├── src/main/java/
│   ├── MainMenu.java              # 主菜单界面
│   ├── SnakeGameWindow.java       # 游戏主窗口
│   ├── SaveData.java              # 存档系统
│   ├── controller/
│   │   └── SnakeController.java   # 游戏控制器
│   ├── model/
│   │   ├── SnakeModel.java        # 游戏逻辑模型
│   │   └── EnemySnake.java        # 敌人AI系统
│   ├── view/
│   │   ├── SnakePanel.java        # 游戏绘制面板
│   │   ├── SettingsDialog.java    # 设置对话框
│   │   ├── StyledButton.java      # 自定义按钮组件
│   │   └── GearIcon.java          # 齿轮图标
│   ├── sound/
│   │   ├── SoundManager.java      # 音效管理器
│   │   └── SoundClip.java         # 音效剪辑
│   └── util/
│       ├── Constants.java         # 游戏常量
│       ├── Direction.java         # 方向枚举
│       ├── GameState.java         # 游戏状态枚举
│       ├── GameConfig.java        # 配置管理
│       ├── GameEvent.java         # 事件定义
│       ├── GameEventListener.java # 事件监听器接口
│       ├── EventManager.java      # 事件管理器
│       └── PathFinder.java        # A*寻路算法
├── saves/                         # 存档目录
├── snake_config.properties        # 配置文件
└── README.md                      # 说明文档
```

## 🎯 游戏规则

1. **目标**: 控制蛇吃食物，获得尽可能高的分数，同时避免被敌人追击
2. **移动**: 使用方向键或WASD控制蛇的移动方向
3. **食物**: 
   - 红色小食物: 1分，蛇身增长1格
   - 橙色大食物: 4分，蛇身增长4格
4. **墙壁**: 每得10分会生成新的墙壁
5. **敌人系统**: 
   - 每得10分会生成一个新的敌人
   - 敌人具有不同的AI策略和颜色
   - 被敌人撞到会导致游戏结束
6. **游戏结束**: 蛇撞到墙壁、自己的身体或被敌人撞到时游戏结束

## 🔧 技术特性

- **Java Swing** - 图形界面框架
- **事件驱动架构** - 松耦合的组件通信
- **配置管理** - 外部配置文件支持
- **数据持久化** - 序列化和压缩存档
- **性能优化** - 双缓冲和智能重绘
- **错误处理** - 完善的异常处理机制
- **AI系统** - A*寻路算法和多种AI策略
- **音效系统** - 游戏音效和背景音乐支持

## 📈 性能指标

- **渲染性能**: 60FPS稳定运行
- **内存使用**: 优化的内存管理
- **存档大小**: 压缩后通常小于1KB
- **启动时间**: 快速启动，无延迟

## 🎨 自定义主题

游戏支持主题自定义，可以通过修改 `Constants.java` 中的颜色常量来改变游戏外观：

```java
public static final Color COLOR_BACKGROUND = Color.BLACK;
public static final Color COLOR_TEXT = Color.WHITE;
public static final Color COLOR_SNAKE_HEAD = Color.YELLOW;
public static final Color COLOR_SNAKE_BODY = Color.GREEN;
```

## 🐛 已知问题

- 暂无已知问题

## 🔮 未来计划

- [x] 智能敌人系统 (A*寻路算法)
- [x] 音效系统
- [ ] 特殊食物类型
- [ ] 多人模式
- [ ] 在线排行榜
- [ ] 更多主题
- [ ] 成就系统
- [ ] 敌人AI难度调节
- [ ] 特殊道具系统

## 📄 许可证

本项目采用 MIT 许可证。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

享受游戏！🐍 