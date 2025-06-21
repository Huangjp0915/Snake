@echo off
echo 正在编译贪吃蛇游戏...

if not exist bin mkdir bin

javac -d bin ^
    src/main/java/MainMenu.java ^
    src/main/java/SnakeGameWindow.java ^
    src/main/java/SaveData.java ^
    src/main/java/controller/SnakeController.java ^
    src/main/java/model/SnakeModel.java ^
    src/main/java/model/EnemySnake.java ^
    src/main/java/view/SnakePanel.java ^
    src/main/java/view/SettingsDialog.java ^
    src/main/java/view/GearIcon.java ^
    src/main/java/view/StyledButton.java ^
    src/main/java/sound/SoundClip.java ^
    src/main/java/sound/SoundManager.java ^
    src/main/java/util/Constants.java ^
    src/main/java/util/Direction.java ^
    src/main/java/util/GameState.java ^
    src/main/java/util/GameConfig.java ^
    src/main/java/util/GameEvent.java ^
    src/main/java/util/GameEventListener.java ^
    src/main/java/util/EventManager.java ^
    src/main/java/util/PathFinder.java

if %ERRORLEVEL% EQU 0 (
    echo 编译成功！
    echo 运行游戏...
    java -cp bin snake.MainMenu
) else (
    echo 编译失败！
    pause
) 