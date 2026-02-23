package org.example.synjiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.synjiserver.common.ApiResponse;
import org.example.synjiserver.dto.ScheduleExtractionData;
import org.example.synjiserver.dto.ScheduleExtractionResult;
import org.example.synjiserver.entity.Schedule;
import org.example.synjiserver.service.ScheduleExtractor;
import org.example.synjiserver.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ScheduleExtractor scheduleExtractor;

    @Autowired
    private ObjectMapper objectMapper;

    // 辅助方法：从 Token 中提取 UserId (模拟)
    private Long getUserIdFromToken(String token) {
        if (token == null || token.isBlank()) {
            throw new RuntimeException("未登录或 Token 无效");
        }
        String rawToken = token.trim();
        if (rawToken.toLowerCase().startsWith("bearer ")) {
            rawToken = rawToken.substring(7).trim();
        }
        if (!rawToken.startsWith("mock-token-")) {
            throw new RuntimeException("未登录或 Token 无效");
        }
        try {
            String[] parts = rawToken.split("-");
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

    @GetMapping("/search")
    public ApiResponse<List<Schedule>> search(@RequestHeader("Authorization") String token, @RequestParam("keyword") String keyword) {
        try {
            Long userId = getUserIdFromToken(token);
            System.out.println("搜索日程, userId: " + userId + ", keyword: " + keyword);
            
            if (keyword == null || keyword.trim().isEmpty()) {
                return ApiResponse.error(400, "搜索关键词不能为空");
            }

            List<Schedule> list = scheduleService.searchSchedules(userId, keyword);
            System.out.println("搜索结果数量: " + list.size());
            return ApiResponse.success("搜索成功", list);
        } catch (RuntimeException e) {
            System.err.println("搜索失败: " + e.getMessage());
            return ApiResponse.error(401, e.getMessage());
        }
    }

    @PostMapping("/add")
    public ApiResponse<Schedule> add(@RequestHeader("Authorization") String token, @RequestBody Schedule schedule) {
        try {
            Long userId = getUserIdFromToken(token);
            System.out.println("收到新增日程请求, userId: " + userId);
            System.out.println("日程标题: " + schedule.getTitle());
            System.out.println("日程归属: " + schedule.getBelonging());
            
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

    @PostMapping("/ai-parse")
    public ApiResponse<List<ScheduleExtractionData>> aiParse(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> request) {
        String text = request.get("text");
        
        // 打印接收到的 OCR 请求数据
        System.out.println("========== 收到 AI 解析请求 ==========");
        System.out.println("OCR 原始文本: " + text);
        
        if (text == null || text.trim().isEmpty()) {
            return ApiResponse.error(400, "文本内容不能为空");
        }

        try {
            // 验证 Token
            getUserIdFromToken(token);

            String currentDate = LocalDate.now().toString();
            // 调用 AI 提取结构化数据
            ScheduleExtractionResult result = scheduleExtractor.extract(currentDate, text);
            List<ScheduleExtractionData> dataList = result.getSchedules();
            
            // 修复客户端崩溃问题：确保每个日程的 time 不为 null
            if (dataList != null) {
                for (ScheduleExtractionData data : dataList) {
                    if (data.getTime() == null) {
                        data.setTime(LocalTime.of(0, 0, 0));
                    }
                }
            }

            // 打印处理结果
            System.out.println("========== AI 解析结果 ==========");
            try {
                String jsonResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataList);
                System.out.println(jsonResult);
            } catch (Exception e) {
                System.out.println("结果序列化失败: " + dataList);
            }
            System.out.println("=====================================");

            return ApiResponse.success("解析成功", dataList);
        } catch (RuntimeException e) {
            System.err.println("AI 解析业务异常: " + e.getMessage());
            return ApiResponse.error(401, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "解析失败: " + e.getMessage());
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
            if (e.getMessage().contains("无权")) {
                return ApiResponse.error(403, e.getMessage());
            }
            if (e.getMessage().contains("不存在")) {
                return ApiResponse.error(404, e.getMessage());
            }
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "修改失败: " + e.getMessage());
        }
    }

    // 兼容旧客户端使用 POST 更新
    @PostMapping("/update")
    public ApiResponse<Schedule> updateByPost(@RequestHeader("Authorization") String token, @RequestBody Schedule schedule) {
        return update(token, schedule);
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
            if (e.getMessage().contains("无权")) {
                return ApiResponse.error(403, e.getMessage());
            }
            if (e.getMessage().contains("不存在")) {
                return ApiResponse.error(404, e.getMessage());
            }
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }

    // 兼容旧客户端使用 POST 删除
    @PostMapping("/delete/{id}")
    public ApiResponse<Void> deleteByPost(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        return delete(token, id);
    }
}
