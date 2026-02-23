package org.example.synjiserver.service;

import org.example.synjiserver.dto.GroupInfo;
import org.example.synjiserver.entity.Schedule;
import org.example.synjiserver.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private GroupService groupService;

    // 获取用户日程列表 (包含个人日程 + 所在群组的共享日程)
    public List<Schedule> getUserSchedules(Long userId) {
        // 1. 获取用户加入的所有群组
        List<GroupInfo> userGroups = groupService.getUserGroups(userId);

        // 2. 提取群组名称列表
        List<String> groupNames = userGroups.stream()
                .map(GroupInfo::getName)
                .collect(Collectors.toList());

        // 3. 查询：(userId = 当前用户) OR (belonging IN 用户群组列表)
        if (groupNames.isEmpty()) {
            return scheduleRepository.findByUserIdOrderByDateAscTimeAsc(userId);
        } else {
            return scheduleRepository.findByUserIdOrBelongingInOrderByDateAscTimeAsc(userId, groupNames);
        }
    }

    // 搜索日程
    public List<Schedule> searchSchedules(Long userId, String keyword) {
        // 1. 获取用户加入的所有群组
        List<GroupInfo> userGroups = groupService.getUserGroups(userId);

        // 2. 提取群组名称列表
        List<String> groupNames = userGroups.stream()
                .map(GroupInfo::getName)
                .collect(Collectors.toList());

        // 3. 执行搜索
        if (groupNames.isEmpty()) {
            return scheduleRepository.searchSchedulesNoGroup(userId, keyword);
        } else {
            return scheduleRepository.searchSchedules(userId, groupNames, keyword);
        }
    }

    // 新增日程
    @Transactional
    public Schedule addSchedule(Long userId, Schedule schedule) {
        schedule.setUserId(userId);

        // 确保非空字段有默认值
        if (schedule.getTime() == null) {
            schedule.setTime(LocalTime.of(0, 0, 0));
        }
        schedule.setBelonging(normalizeBelonging(schedule.getBelonging()));

        return scheduleRepository.save(schedule);
    }

    // 归属值规范化：null、空串、"默认" 等均视为个人日程
    private String normalizeBelonging(String belonging) {
        if (belonging == null || belonging.trim().isEmpty() || "默认".equals(belonging.trim())) {
            return "个人";
        }
        return belonging.trim();
    }

    // 更新日程
    @Transactional
    public Schedule updateSchedule(Long userId, Schedule updatedSchedule) {
        // 先查找日程是否存在
        Schedule existing = scheduleRepository.findById(updatedSchedule.getId())
                .orElseThrow(() -> new RuntimeException("日程不存在"));

        // 鉴权逻辑
        boolean isCreator = existing.getUserId().equals(userId);
        boolean isInGroup = false;

        if (!isCreator) {
            // 检查用户是否在日程所属的群组中
            List<GroupInfo> userGroups = groupService.getUserGroups(userId);
            isInGroup = userGroups.stream().anyMatch(g -> g.getName().equals(existing.getBelonging()));
        }

        if (!isCreator && !isInGroup) {
            throw new RuntimeException("无权修改此日程");
        }

        // 执行更新
        existing.setTitle(updatedSchedule.getTitle());
        existing.setDate(updatedSchedule.getDate());

        if (updatedSchedule.getTime() == null) {
            existing.setTime(LocalTime.of(0, 0, 0));
        } else {
            existing.setTime(updatedSchedule.getTime());
        }

        existing.setAllDay(updatedSchedule.isAllDay());
        existing.setLocation(updatedSchedule.getLocation());

        if (updatedSchedule.getBelonging() != null && !updatedSchedule.getBelonging().trim().isEmpty()) {
            existing.setBelonging(normalizeBelonging(updatedSchedule.getBelonging()));
        }

        existing.setImportant(updatedSchedule.isImportant());
        existing.setNotes(updatedSchedule.getNotes());
        if (updatedSchedule.getOcrText() != null) {
            existing.setOcrText(updatedSchedule.getOcrText());
        }

        existing.setAiGenerated(updatedSchedule.isAiGenerated());
        existing.setViewed(updatedSchedule.isViewed());

        return scheduleRepository.save(existing);
    }

    // 删除日程
    @Transactional
    public void deleteSchedule(Long userId, Long scheduleId) {
        Schedule existing = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("日程不存在"));

        // 仅允许创建者删除
        if (!existing.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此日程");
        }

        scheduleRepository.delete(existing);
    }
}
