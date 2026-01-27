package org.example.synjiserver.controller;

import org.example.synjiserver.common.ApiResponse;
import org.example.synjiserver.dto.UserDto;
import org.example.synjiserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 辅助方法：从 Token 中提取 UserId (模拟)
    private Long getUserIdFromToken(String token) {
        if (token == null || !token.startsWith("mock-token-")) {
            throw new RuntimeException("未登录或 Token 无效");
        }
        try {
            String[] parts = token.split("-");
            return Long.parseLong(parts[2]);
        } catch (Exception e) {
            throw new RuntimeException("Token 格式错误");
        }
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public ApiResponse<UserDto> getUserInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = getUserIdFromToken(token);
            UserDto userDto = userService.getUserById(userId);
            if (userDto == null) {
                return ApiResponse.error(404, "用户不存在");
            }
            return ApiResponse.success("获取成功", userDto);
        } catch (RuntimeException e) {
            return ApiResponse.error(401, e.getMessage());
        }
    }

    /**
     * 更新用户资料
     */
    @PutMapping("/update")
    public ApiResponse<UserDto> updateUser(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> updates) {
        try {
            Long userId = getUserIdFromToken(token);
            String nickname = updates.get("nickname");
            
            if (nickname == null || nickname.trim().isEmpty()) {
                return ApiResponse.error(400, "昵称不能为空");
            }

            UserDto updatedUser = userService.updateUser(userId, nickname);
            return ApiResponse.success("更新成功", updatedUser);
        } catch (RuntimeException e) {
            return ApiResponse.error(401, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "更新失败: " + e.getMessage());
        }
    }
}
