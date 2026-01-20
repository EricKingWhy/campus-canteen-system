@echo off
chcp 65001
echo ========================================================
echo   正在执行：暴力清理与重建 (解决代码不更新问题)
echo ========================================================

echo 1. 正在停止所有 java 进程 (防止文件被占用)...
taskkill /F /IM java.exe >nul 2>&1

echo 2. 正在暴力删除旧的 target 编译目录...
if exist "server\target" rd /s /q "server\target"
if exist "common\target" rd /s /q "common\target"
if exist "pojo\target" rd /s /q "pojo\target"

echo 3. 正在调用 Maven 进行全新编译...
call mvn clean compile -DskipTests

echo.
echo ========================================================
echo   重建完成！请查看上方是否有 "BUILD SUCCESS"。
echo   如果成功，请在 IDEA 中重新点击运行。
echo ========================================================
pause
