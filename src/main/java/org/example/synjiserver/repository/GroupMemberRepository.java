package org.example.synjiserver.repository;

import org.example.synjiserver.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> { // 注意：主键类型这里填 Long 可能不准确，但 JPA 方法通常能泛型处理
    
    // 查找用户加入的所有群组ID
    @Query("SELECT gm.groupId FROM GroupMember gm WHERE gm.userId = :userId")
    List<Long> findGroupIdsByUserId(Long userId);
    
    // 统计群组成员数
    long countByGroupId(Long groupId);
    
    // 检查用户是否已在群组中
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    // 查找群组的所有成员
    List<GroupMember> findByGroupId(Long groupId);

    // 查找特定成员
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    // 统计群组内管理员数量
    long countByGroupIdAndRole(Long groupId, GroupMember.Role role);
}
