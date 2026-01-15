@echo off
echo ========================================
echo 启动前后端服务
echo ========================================
echo.
echo 提示：将打开两个命令行窗口
echo 第一个窗口：后端服务 (http://localhost:8080)
echo 第二个窗口：前端服务 (http://localhost:3000)
echo.
pause

start "后端服务" cmd /k "cd demo && mvnw.cmd spring-boot:run"
timeout /t 3 /nobreak >nul
start "前端服务" cmd /k "cd shopping-frontend-vue && npm install && npm run dev"

echo.
echo 服务已启动！
echo 后端: http://localhost:8080
echo 前端: http://localhost:3000
echo.
pause
