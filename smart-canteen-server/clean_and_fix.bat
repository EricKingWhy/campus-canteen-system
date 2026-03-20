@echo off
chcp 65001
echo ========================================================
echo   正在执行：清理冗余代码文件夹 (serviceImpl)
echo ========================================================

set "TARGET_DIR=server\src\main\java\fun\cyhgraph\service\serviceImpl"

if exist "%TARGET_DIR%" (
    echo [发现目标] 正在删除: %TARGET_DIR%
    rd /s /q "%TARGET_DIR%"
    if not exist "%TARGET_DIR%" (
        echo [成功] 冗余文件夹已彻底清除！
    ) else (
        echo [失败] 无法删除，请检查文件是否被占用。
    )
) else (
    echo [提示] 目标文件夹不存在，无需清理。
)

echo.
echo ========================================================
echo   清理完成！请回到 IDEA 等待 Maven 重新加载...
echo ========================================================
pause
