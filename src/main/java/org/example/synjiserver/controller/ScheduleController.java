package org.example.synjiserver.controller;

import org.example.synjiserver.common.ApiResponse;
import org.example.synjiserver.entity.Schedule;
import org.example.synjiserver.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

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
    public ApiResponse<List<Schedule>> list(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            System.out.println("查询日程列表, userId: " + userId);
            List<Schedule> list = scheduleService.getUserSchedules(userId);
            System.out.println("查询结果数量: " + list.size());
            return ApiResponse.success("获取成功", list);
        } catch (RuntimeException e) {
            System.err.println("查询失败: " + e.getMessage());
            return ApiResponse.error(401, e.getMessage());
        }
    }

    @PostMapping("/add")
    public ApiResponse<Schedule> add(@RequestHeader("Authorization") String token, @RequestBody Schedule schedule) {
        try {
            Long userId = getUserIdFromToken(token);
            System.out.println("收到新增日程请求, userId: " + userId);
            System.out.println("日程标题: " + schedule.getTitle());
            System.out.println("日程日期: " + schedule.getDate());
            
            Schedule created = scheduleService.addSchedule(userId, schedule);
            System.out.println("新增成功, ID: " + created.getId());
            return ApiResponse.success("添加成功", created);
        } catch (RuntimeException e) {
            System.err.println("新增业务异常: " + e.getMessage());
            return ApiResponse.error(401, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // 打印完整堆栈
            return ApiResponse.error(500, "添加失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ApiResponse<Schedule> update(@RequestHeader("Authorization") String token, @RequestBody Schedule schedule) {
        try {
            Long userId = getUserIdFromToken(token);
            if (schedule.getId() == null) {
                return ApiResponse.error(400, "日程ID不能为空");
            }
            Schedule updated = scheduleService.updateSchedule(userId, schedule);
            return ApiResponse.success("修改成功", updated);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Token")) {
                return ApiResponse.error(401, e.getMessage());
            }
            return ApiResponse.error(404, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "修改失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> delete(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        try {
            Long userId = getUserIdFromToken(token);
            scheduleService.deleteSchedule(userId, id);
            return ApiResponse.success("删除成功", null);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Token")) {
                return ApiResponse.error(401, e.getMessage());
            }
            return ApiResponse.error(404, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }
}
