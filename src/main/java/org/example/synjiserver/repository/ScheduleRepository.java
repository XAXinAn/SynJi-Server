package org.example.synjiserver.repository;

import org.example.synjiserver.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
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
    // 注意：对于共享日程，非创建者可能也需要有权限修改吗？
    // 文档未明确说明共享日程的修改权限。通常只允许创建者修改，或者群组成员都能修改。
    // 鉴于文档说 "客户端点击带红点的日程时...发送 isViewed: true"，这意味着查看者(非创建者)需要能更新 isViewed 字段。
    // 如果 findByIdAndUserId 限制了只能查自己的，那么 A2 无法更新 A1 创建的共享日程的 isViewed。
    // 所以我们需要放宽 update 的查找条件，或者在 Service 层做逻辑判断。
    // 暂时保留此方法用于 strict 鉴权，但在 Service 中可能需要先 findById 再判断权限。
    Optional<Schedule> findByIdAndUserId(Long id, Long userId);
}
