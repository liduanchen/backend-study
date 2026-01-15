@echo off
echo ========================================
echo 启动前端服务
echo ========================================
cd shopping-frontend-vue
call npm install
call npm run dev
pause
