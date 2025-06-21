Write-Host "正在编译贪吃蛇游戏..." -ForegroundColor Green

# 创建bin目录
if (!(Test-Path "bin")) {
    New-Item -ItemType Directory -Name "bin"
}

# 检查Java是否可用
try {
    $javaVersion = java -version 2>&1
    Write-Host "Java版本: $javaVersion" -ForegroundColor Yellow
} catch {
    Write-Host "错误: 未找到Java，请先安装Java JDK" -ForegroundColor Red
    Write-Host "请参考 INSTALL.md 文件进行安装" -ForegroundColor Yellow
    Read-Host "按任意键退出"
    exit 1
}

# 编译所有Java文件
$javaFiles = @(
    "src/main/java/MainMenu.java",
    "src/main/java/SnakeGameWindow.java", 
    "src/main/java/SaveData.java",
    "src/main/java/controller/SnakeController.java",
    "src/main/java/model/SnakeModel.java",
    "src/main/java/model/EnemySnake.java",
    "src/main/java/view/SnakePanel.java",
    "src/main/java/view/SettingsDialog.java",
    "src/main/java/view/GearIcon.java",
    "src/main/java/view/StyledButton.java",
    "src/main/java/sound/SoundClip.java",
    "src/main/java/sound/SoundManager.java",
    "src/main/java/util/Constants.java",
    "src/main/java/util/Direction.java",
    "src/main/java/util/GameState.java",
    "src/main/java/util/GameConfig.java",
    "src/main/java/util/GameEvent.java",
    "src/main/java/util/GameEventListener.java",
    "src/main/java/util/EventManager.java",
    "src/main/java/util/PathFinder.java"
)

$compileArgs = @("-d", "bin") + $javaFiles

try {
    javac @compileArgs
    if ($LASTEXITCODE -eq 0) {
        Write-Host "编译成功！" -ForegroundColor Green
        Write-Host "运行游戏..." -ForegroundColor Yellow
        java -cp bin snake.MainMenu
    } else {
        Write-Host "编译失败！" -ForegroundColor Red
    }
} catch {
    Write-Host "编译过程中出现错误: $_" -ForegroundColor Red
}

Read-Host "按任意键退出" 