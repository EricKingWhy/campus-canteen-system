@echo off
chcp 65001
echo ========================================================
echo   正在执行：强制修正文件名大小写 (WorkSpace -> Workspace)
echo ========================================================

cd server\src\main\java\fun\cyhgraph\service

REM 1. 检查是否存在错误命名的大写 S 文件
if exist "WorkSpaceService.java" (
    echo [发现] 找到 WorkSpaceService.java，正在重命名...
    
    REM 2. 先重命名为临时文件 (这是骗过 Windows 的关键)
    ren "WorkSpaceService.java" "temp_file.java"
    
    REM 3. 再重命名为正确的小写 s 文件
    ren "temp_file.java" "WorkspaceService.java"
    
    echo [成功] 文件名已修正为 WorkspaceService.java
) else (
    echo [提示] 没发现大写 S 文件，尝试直接确认目标文件...
)

REM 4. 再次确认最终文件存在
if exist "WorkspaceService.java" (
    echo [确认] WorkspaceService.java 就绪。
) else (
    echo [错误] 文件丢失，请联系 AI 重新创建。
)

echo.
echo ========================================================
echo   修正完成！请重新编译。
echo ========================================================
