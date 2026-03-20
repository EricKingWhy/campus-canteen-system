@echo off
chcp 65001
echo ==============================================
echo   正在启动自动修复程序 (By Antigravity)
echo   目标：绕过中文用户名导致的路径乱码问题
echo ==============================================
echo.
echo [1/3] 正在创建项目内临时仓库...
if not exist "local_repo" mkdir "local_repo"

echo [2/3] 正在强制编译并安装依赖 (这可能需要几分钟)...
echo       请勿关闭窗口...
echo.

call mvn clean install -Dmaven.repo.local=./local_repo

echo.
echo ==============================================
if %errorlevel% equ 0 (
    echo [成功] 编译完成！显示 BUILD SUCCESS。
    echo 现在你可以直接点击 IDEA 顶部的“运行”按钮启动项目了！
) else (
    echo [失败] 编译出错。
    echo 请检查是否安装了 Maven 并配置了环境变量，
    echo 或者截图发给 AI 助手寻求帮助。
)
echo ==============================================
pause
