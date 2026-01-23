package org.example.synjiserver.service;

import org.example.synjiserver.entity.Schedule;
import org.example.synjiserver.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // 如果是全天日程，时间可以设为 null 或 00:00
        if (schedule.isAllDay()) {
            schedule.setTime(null);
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
        existing.setTime(updatedSchedule.getTime());
        existing.setAllDay(updatedSchedule.isAllDay());
        existing.setLocation(updatedSchedule.getLocation());
        existing.setBelonging(updatedSchedule.getBelonging());
        existing.setImportant(updatedSchedule.isImportant());
        
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
