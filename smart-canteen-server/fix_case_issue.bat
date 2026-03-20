@echo off
chcp 65001
echo ========================================================
echo   正在执行：删除命名错误的文件 WorkSpaceService.java
echo ========================================================

REM 删除错误的大写 S 文件
if exist "server\src\main\java\fun\cyhgraph\service\WorkSpaceService.java" (
    del /f /q "server\src\main\java\fun\cyhgraph\service\WorkSpaceService.java"
    echo [成功] 已删除错误的 WorkSpaceService.java
)

echo.
echo ========================================================
echo   清理完成！请继续等待文件重写...
echo ========================================================
