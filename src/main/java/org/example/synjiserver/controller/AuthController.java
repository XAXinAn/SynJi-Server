package org.example.synjiserver.controller;

import org.example.synjiserver.common.ApiResponse;
import org.example.synjiserver.dto.LoginRequest;
import org.example.synjiserver.dto.LoginResponse;
import org.example.synjiserver.dto.SendCodeRequest;
import org.example.synjiserver.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/send-code")
    public ApiResponse<String> sendCode(@RequestBody SendCodeRequest request) {
        try {
            String code = authService.sendCode(request.getPhoneNumber());
            // 内测期间，直接将验证码返回给前端
            return ApiResponse.success("验证码发送成功", code);
        } catch (Exception e) {
            return ApiResponse.error(500, "发送失败: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request.getPhoneNumber(), request.getVerifyCode());
            return ApiResponse.success("登录成功", response);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "系统错误: " + e.getMessage());
        }
    }
}
