package org.example.synjiserver.service;

import org.example.synjiserver.dto.GroupInfo;
import org.example.synjiserver.entity.Schedule;
import org.example.synjiserver.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
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
        
        // 2. 提取群组名称列表 (belonging 字段存储的是群组名称)
        // 注意：文档 4.1 说 belonging 是 "个人" 或 "群组名称/ID"。
        // 假设前端传的是群组名称。为了稳健，我们应该匹配名称。
        // 如果前端传的是 ID，这里需要调整。根据 3.2 创建群组返回的数据，前端拿到的是 name。
        // 假设 belonging 存的是 Group Name。
        List<String> groupNames = userGroups.stream()
                .map(GroupInfo::getName)
                .collect(Collectors.toList());

        // 3. 查询：(userId = 当前用户) OR (belonging IN 用户群组列表)
        // 这样既包含了用户自己创建的 "个人" 日程，也包含了用户自己创建的 "群组" 日程，
        // 还包含了别人创建的但属于该用户所在群组的日程。
        
        if (groupNames.isEmpty()) {
            return scheduleRepository.findByUserIdOrderByDateAscTimeAsc(userId);
        } else {
            return scheduleRepository.findByUserIdOrBelongingInOrderByDateAscTimeAsc(userId, groupNames);
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
        if (schedule.getBelonging() == null || schedule.getBelonging().trim().isEmpty()) {
            schedule.setBelonging("个人"); // 默认为个人
        }
        
        return scheduleRepository.save(schedule);
    }

    // 更新日程
    @Transactional
    public Schedule updateSchedule(Long userId, Schedule updatedSchedule) {
        // 先查找日程是否存在
        Schedule existing = scheduleRepository.findById(updatedSchedule.getId())
                .orElseThrow(() -> new RuntimeException("日程不存在"));

        // 鉴权逻辑：
        // 1. 如果是创建者，可以修改。
        // 2. 如果不是创建者，但该日程属于用户所在的群组，是否允许修改？
        //    文档 5.2 提到 "用户查看详情后，前端调用 update 接口将 isViewed 改为 true"。
        //    这意味着非创建者也需要能修改 isViewed 状态。
        //    但是，isViewed 是用户私有的状态吗？
        //    数据库设计中 is_viewed 是 Schedule 表的一个字段。
        //    如果 A1 创建了共享日程，A2 查看了，把 is_viewed 改为 true，那么 A3 看到的是什么？
        //    A3 看到的也是 true。这在共享场景下可能不太合理（通常已读状态是 per-user 的）。
        //    但根据当前简单的数据库设计（单表），我们只能修改这条记录。
        //    所以，如果 A2 改了，所有人都会看到已读。
        //    为了满足文档需求，我们允许群组成员修改共享日程。

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
        
        if (updatedSchedule.getBelonging() == null || updatedSchedule.getBelonging().trim().isEmpty()) {
            // 保持原样或设为默认
        } else {
            existing.setBelonging(updatedSchedule.getBelonging());
        }

        existing.setImportant(updatedSchedule.isImportant());
        existing.setNotes(updatedSchedule.getNotes());
        
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
