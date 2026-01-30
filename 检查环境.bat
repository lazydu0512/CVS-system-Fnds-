@echo off
chcp 65001 >nul
echo ====================================
echo  环境检查工具
echo ====================================
echo.

echo [1/5] 检查Java环境...
java -version 2>nul
if %errorlevel% neq 0 (
    echo ❌ Java未安装或未配置环境变量
    echo    请安装JDK 17+
    goto :error
) else (
    echo ✅ Java环境正常
)
echo.

echo [2/5] 检查Maven环境...
mvn -version 2>nul
if %errorlevel% neq 0 (
    echo ❌ Maven未安装或未配置环境变量
    echo    请安装Maven 3.6+
    goto :error
) else (
    echo ✅ Maven环境正常
)
echo.

echo [3/5] 检查Node.js环境...
node -v 2>nul
if %errorlevel% neq 0 (
    echo ❌ Node.js未安装或未配置环境变量
    echo    请安装Node.js 16+
    goto :error
) else (
    echo ✅ Node.js环境正常
)
echo.

echo [4/5] 检查MySQL服务...
sc query MySQL80 | find "RUNNING" >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ MySQL服务未运行
    echo    请启动MySQL服务
    echo.
    echo    方法1: 服务管理器中启动MySQL80
    echo    方法2: 命令行执行: net start MySQL80
    goto :error
) else (
    echo ✅ MySQL服务正常运行
)
echo.

echo [5/5] 检查端口占用...
echo 检查8080端口（后端）...
netstat -ano | findstr :8080 >nul 2>&1
if %errorlevel% equ 0 (
    echo ⚠️  端口8080已被占用
    echo    如果不是后端服务占用，请关闭占用该端口的程序
    netstat -ano | findstr :8080
) else (
    echo ✅ 端口8080可用
)
echo.

echo 检查5173端口（前端）...
netstat -ano | findstr :5173 >nul 2>&1
if %errorlevel% equ 0 (
    echo ⚠️  端口5173已被占用
    echo    如果不是前端服务占用，请关闭占用该端口的程序
    netstat -ano | findstr :5173
) else (
    echo ✅ 端口5173可用
)
echo.

echo ====================================
echo  ✅ 环境检查完成！
echo ====================================
echo.
echo 📌 下一步操作：
echo    1. 确保MySQL中已创建cvs_db数据库
echo    2. 确保已导入init.sql数据
echo    3. 修改application.properties中的数据库密码
echo    4. 启动后端: cd CVS-port && mvn spring-boot:run
echo    5. 启动前端: cd CVS-view && npm run dev
echo.
pause
exit /b 0

:error
echo.
echo ====================================
echo  ❌ 环境检查失败！
echo ====================================
echo.
echo 请先解决上述问题，然后重新运行此脚本。
echo.
pause
exit /b 1

