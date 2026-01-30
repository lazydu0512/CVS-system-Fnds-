@echo off
echo 正在清理前端项目依赖...

REM 删除 node_modules 和锁定文件
if exist node_modules rmdir /s /q node_modules
if exist package-lock.json del /f /q package-lock.json

echo 重新安装依赖...
npm install

echo 依赖安装完成！
pause