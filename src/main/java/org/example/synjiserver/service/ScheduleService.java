package org.example.synjiserver.service;

import org.example.synjiserver.entity.Schedule;
import org.example.synjiserver.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    // 获取用户日程列表
    public List<Schedule> getUserSchedules(Long userId) {
        return scheduleRepository.findByUserIdOrderByDateAscTimeAsc(userId);
    }

    // 新增日程
    @Transactional
    public Schedule addSchedule(Long userId, Schedule schedule) {
        schedule.setUserId(userId);
        
        // 确保非空字段有默认值
        if (schedule.getTime() == null) {
            schedule.setTime(LocalTime.of(0, 0, 0));
        }
        if (schedule.getBelonging() == null || schedule.getBelonging().trim().isEmpty()) {
            schedule.setBelonging("默认");
        }
        
        return scheduleRepository.save(schedule);
    }

    // 更新日程
    @Transactional
    public Schedule updateSchedule(Long userId, Schedule updatedSchedule) {
        Schedule existing = scheduleRepository.findByIdAndUserId(updatedSchedule.getId(), userId)
                .orElseThrow(() -> new RuntimeException("日程不存在或无权修改"));

        existing.setTitle(updatedSchedule.getTitle());
        existing.setDate(updatedSchedule.getDate());
        
        // 确保非空字段有默认值
        if (updatedSchedule.getTime() == null) {
            existing.setTime(LocalTime.of(0, 0, 0));
        } else {
            existing.setTime(updatedSchedule.getTime());
        }
        
        existing.setAllDay(updatedSchedule.isAllDay());
        existing.setLocation(updatedSchedule.getLocation());
        
        if (updatedSchedule.getBelonging() == null || updatedSchedule.getBelonging().trim().isEmpty()) {
            existing.setBelonging("默认");
        } else {
            existing.setBelonging(updatedSchedule.getBelonging());
        }

        existing.setImportant(updatedSchedule.isImportant());
        existing.setNotes(updatedSchedule.getNotes());
        
        // 更新 AI 相关字段
        existing.setAiGenerated(updatedSchedule.isAiGenerated());
        existing.setViewed(updatedSchedule.isViewed());
        
        return scheduleRepository.save(existing);
    }

    // 删除日程
    @Transactional
    public void deleteSchedule(Long userId, Long scheduleId) {
        Schedule existing = scheduleRepository.findByIdAndUserId(scheduleId, userId)
                .orElseThrow(() -> new RuntimeException("日程不存在或无权删除"));
        scheduleRepository.delete(existing);
    }
}
