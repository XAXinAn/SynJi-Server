package org.example.synjiserver.repository;

import org.example.synjiserver.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    // 查询某个用户的所有日程，按日期和时间排序 (旧接口，保留以防万一)
    List<Schedule> findByUserIdOrderByDateAscTimeAsc(Long userId);

    // 新接口：查询用户自己的日程 OR 属于用户所在群组的日程
    List<Schedule> findByUserIdOrBelongingInOrderByDateAscTimeAsc(Long userId, Collection<String> belongings);
    
    // 查找指定ID且属于指定用户的日程（用于鉴权 - 修改/删除）
    Optional<Schedule> findByIdAndUserId(Long id, Long userId);

    // 搜索日程：(creator_id = ? OR belonging IN (?)) AND title LIKE %keyword%
    @Query("SELECT s FROM Schedule s WHERE (s.userId = :userId OR s.belonging IN :belongings) AND s.title LIKE %:keyword% ORDER BY s.date DESC")
    List<Schedule> searchSchedules(@Param("userId") Long userId, @Param("belongings") Collection<String> belongings, @Param("keyword") String keyword);

    // 搜索日程（无群组情况）：creator_id = ? AND title LIKE %keyword%
    @Query("SELECT s FROM Schedule s WHERE s.userId = :userId AND s.title LIKE %:keyword% ORDER BY s.date DESC")
    List<Schedule> searchSchedulesNoGroup(@Param("userId") Long userId, @Param("keyword") String keyword);
}
