@echo off
chcp 65001 >nul
echo ====================================
echo  演唱会视频分享平台 - 快速启动
echo ====================================
echo.

echo 📋 启动前检查...
echo.

REM 检查Java
echo [1/4] 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 未检测到Java，请先安装JDK 17+
    pause
    exit /b 1
)
echo ✅ Java环境正常

REM 检查Maven
echo [2/4] 检查Maven环境...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 未检测到Maven，请先安装Maven 3.6+
    pause
    exit /b 1
)
echo ✅ Maven环境正常

REM 检查Node.js
echo [3/4] 检查Node.js环境...
node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 未检测到Node.js，请先安装Node.js 16+
    pause
    exit /b 1
)
echo ✅ Node.js环境正常

REM 检查MySQL
echo [4/4] 检查MySQL服务...
sc query MySQL80 | find "RUNNING" >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  MySQL服务未运行，请先启动MySQL
    echo.
    echo 是否继续启动项目？(Y/N)
    set /p continue=
    if /i not "%continue%"=="Y" (
        exit /b 1
    )
) else (
    echo ✅ MySQL服务正常
)

echo.
echo ====================================
echo  开始启动项目...
echo ====================================
echo.

REM 启动后端
echo 🚀 正在启动后端服务...
echo.
start "CVS后端服务" cmd /k "cd CVS-port && mvn spring-boot:run"

echo ⏳ 等待后端服务启动（预计30秒）...
timeout /t 30 /nobreak >nul

echo.
echo 🚀 正在启动前端服务...
echo.

REM 检查前端依赖
if not exist "CVS-view\node_modules" (
    echo 📦 检测到前端依赖未安装，正在安装...
    cd CVS-view
    call install-dependencies.bat
    cd ..
)

start "CVS前端服务" cmd /k "cd CVS-view && npm run dev"

echo.
echo ====================================
echo  ✅ 项目启动完成！
echo ====================================
echo.
echo 📌 访问地址：
echo    前端：http://localhost:5173
echo    后端：http://localhost:8080
echo.
echo 📌 测试账号：
echo    管理员：admin / 123456
echo    用户1：user1 / 123456
echo    用户2：user2 / 123456
echo.
echo 💡 提示：
echo    - 两个命令行窗口将保持打开状态
echo    - 关闭窗口即可停止对应服务
echo    - 如需重启，请先关闭所有窗口再运行此脚本
echo.
pause

