package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "root";

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Map<String, Object> response = new HashMap<>();

        // 验证用户名和密码
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            // 登录成功，设置session
            session.setAttribute("admin", true);
            session.setAttribute("username", username);
            
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("username", username);
            return ResponseEntity.ok(response);
        } else {
            // 登录失败
            response.put("success", false);
            response.put("message", "用户名或密码错误");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * 检查登录状态
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAuth(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Boolean isAdmin = (Boolean) session.getAttribute("admin");
        
        if (isAdmin != null && isAdmin) {
            response.put("authenticated", true);
            response.put("username", session.getAttribute("username"));
        } else {
            response.put("authenticated", false);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "已退出登录");
        return ResponseEntity.ok(response);
    }
}
