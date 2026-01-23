package org.example.synjiserver.controller;

import org.example.synjiserver.common.ApiResponse;
import org.example.synjiserver.dto.UserDto;
import org.example.synjiserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前登录用户信息
     * 前端需要在 Header 中携带 Authorization (Token)
     * 这里为了演示，暂时假设 Token 解析逻辑已在网关或拦截器完成，
     * 并将 userId 放入了名为 "X-User-Id" 的 Header 中，或者直接解析 Token (简化版直接传ID模拟)
     * 
     * 实际对接：前端传 Authorization: Bearer <token>
     * 后端解析 Token -> 拿到 userId -> 查询数据库
     */
    @GetMapping("/info")
    public ApiResponse<UserDto> getUserInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        // TODO: 实际开发中这里需要解析 JWT Token 获取 userId
        // 这里为了方便联调，我们假设 Token 格式为 "mock-token-{userId}-{timestamp}"
        // 从而直接提取 userId。如果 Token 格式不对，则视为未登录。
        
        if (token == null || !token.startsWith("mock-token-")) {
            return ApiResponse.error(401, "未登录或 Token 无效");
        }

        try {
            // 简单的 Mock Token 解析逻辑
            String[] parts = token.split("-");
            if (parts.length < 3) {
                return ApiResponse.error(401, "Token 格式错误");
            }
            Long userId = Long.parseLong(parts[2]);

            UserDto userDto = userService.getUserById(userId);
            if (userDto == null) {
                return ApiResponse.error(404, "用户不存在");
            }

            return ApiResponse.success("获取成功", userDto);

        } catch (Exception e) {
            return ApiResponse.error(401, "Token 解析失败");
        }
    }
}
