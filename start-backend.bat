@echo off
echo ========================================
echo 启动后端服务
echo ========================================
cd demo
call mvnw.cmd spring-boot:run
pause
