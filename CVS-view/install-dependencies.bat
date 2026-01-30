@echo off
echo ====================================
echo  演唱会视频分享平台 - 前端依赖安装
echo ====================================

cd /d %~dp0

echo 步骤1: 清理旧的依赖...
if exist node_modules rmdir /s /q node_modules
if exist package-lock.json del /f /q package-lock.json

echo.
echo 步骤2: 安装基础依赖...
npm install

echo.
echo 步骤3: 安装路由管理...
npm install vue-router@^4.2.5

echo.
echo 步骤4: 安装状态管理...
npm install pinia@^2.1.7

echo.
echo 步骤5: 安装HTTP客户端...
npm install axios@^1.6.0

echo.
echo 步骤6: 安装UI组件库...
npm install element-plus@^2.4.0 @element-plus/icons-vue@^2.1.0

echo.
echo ====================================
echo  ✅ 所有依赖安装完成！
echo ====================================
echo.
echo 现在你可以运行以下命令启动项目：
echo npm run dev
echo.
echo 然后访问: http://localhost:5173
echo.
pause