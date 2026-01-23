package org.example.synjiserver.repository;

import org.example.synjiserver.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    // 查询某个用户的所有日程，按日期和时间排序
    List<Schedule> findByUserIdOrderByDateAscTimeAsc(Long userId);
    
    // 查找指定ID且属于指定用户的日程（用于鉴权）
    Optional<Schedule> findByIdAndUserId(Long id, Long userId);
}
