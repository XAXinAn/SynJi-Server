package org.example.synjiserver.controller;

import org.example.synjiserver.common.ApiResponse;
import org.example.synjiserver.dto.GroupInfo;
import org.example.synjiserver.dto.GroupMemberDto;
import org.example.synjiserver.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/group")
public class GroupController {

    @Autowired
    private GroupService groupService;

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

    @GetMapping("/list")
    public ApiResponse<List<GroupInfo>> list(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            List<GroupInfo> groups = groupService.getUserGroups(userId);
            return ApiResponse.success("获取成功", groups);
        } catch (RuntimeException e) {
            return ApiResponse.error(401, e.getMessage());
        }
    }

    @PostMapping("/create")
    public ApiResponse<GroupInfo> create(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> request) {
        try {
            Long userId = getUserIdFromToken(token);
            String name = request.get("name");
            
            if (name == null || name.trim().isEmpty()) {
                return ApiResponse.error(400, "群组名称不能为空");
            }

            GroupInfo newGroup = groupService.createGroup(userId, name);
            return ApiResponse.success("创建成功", newGroup);
        } catch (RuntimeException e) {
            return ApiResponse.error(401, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "创建失败: " + e.getMessage());
        }
    }

    @PostMapping("/join")
    public ApiResponse<GroupInfo> join(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> request) {
        try {
            Long userId = getUserIdFromToken(token);
            String inviteCode = request.get("inviteCode");
            
            if (inviteCode == null || inviteCode.trim().isEmpty()) {
                return ApiResponse.error(400, "邀请码不能为空");
            }

            GroupInfo group = groupService.joinGroup(userId, inviteCode);
            return ApiResponse.success("加入成功", group);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "加入失败: " + e.getMessage());
        }
    }

    @GetMapping("/members")
    public ApiResponse<List<GroupMemberDto>> getMembers(@RequestHeader("Authorization") String token, @RequestParam Long groupId) {
        try {
            Long userId = getUserIdFromToken(token);
            // 传递 userId 给 Service 进行鉴权
            List<GroupMemberDto> members = groupService.getGroupMembers(userId, groupId);
            return ApiResponse.success("获取成功", members);
        } catch (RuntimeException e) {
            return ApiResponse.error(401, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "获取失败: " + e.getMessage());
        }
    }

    @PostMapping("/set-admin")
    public ApiResponse<Void> setAdmin(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> request) {
        try {
            Long requesterId = getUserIdFromToken(token);
            
            // 安全转换参数
            Long groupId = Long.valueOf(request.get("groupId").toString());
            Long targetUserId = Long.valueOf(request.get("userId").toString());
            boolean isAdmin = Boolean.parseBoolean(request.get("isAdmin").toString());

            groupService.setAdmin(requesterId, groupId, targetUserId, isAdmin);
            return ApiResponse.success("设置成功", null);
        } catch (NumberFormatException e) {
            return ApiResponse.error(400, "参数格式错误");
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "操作失败: " + e.getMessage());
        }
    }
}
