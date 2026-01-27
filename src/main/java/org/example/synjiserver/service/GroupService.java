package org.example.synjiserver.service;

import org.example.synjiserver.dto.GroupInfo;
import org.example.synjiserver.dto.GroupMemberDto;
import org.example.synjiserver.entity.Group;
import org.example.synjiserver.entity.GroupMember;
import org.example.synjiserver.entity.User;
import org.example.synjiserver.repository.GroupMemberRepository;
import org.example.synjiserver.repository.GroupRepository;
import org.example.synjiserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private UserRepository userRepository;

    // 获取用户加入的群组列表
    public List<GroupInfo> getUserGroups(Long userId) {
        List<Long> groupIds = groupMemberRepository.findGroupIdsByUserId(userId);
        List<Group> groups = groupRepository.findAllById(groupIds);
        
        List<GroupInfo> result = new ArrayList<>();
        for (Group group : groups) {
            int memberCount = (int) groupMemberRepository.countByGroupId(group.getId());
            result.add(new GroupInfo(
                    String.valueOf(group.getId()),
                    group.getName(),
                    String.valueOf(group.getOwnerId()),
                    group.getInviteCode(),
                    memberCount
            ));
        }
        return result;
    }

    // 创建群组
    @Transactional
    public GroupInfo createGroup(Long userId, String name) {
        Group group = new Group();
        group.setName(name);
        group.setOwnerId(userId);
        // 生成6位随机邀请码
        String inviteCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        group.setInviteCode(inviteCode);
        
        Group savedGroup = groupRepository.save(group);
        
        // 将创建者加入群组，并设为 OWNER
        GroupMember member = new GroupMember(savedGroup.getId(), userId, GroupMember.Role.OWNER);
        groupMemberRepository.save(member);
        
        return new GroupInfo(
                String.valueOf(savedGroup.getId()),
                savedGroup.getName(),
                String.valueOf(savedGroup.getOwnerId()),
                savedGroup.getInviteCode(),
                1
        );
    }

    // 加入群组
    @Transactional
    public GroupInfo joinGroup(Long userId, String inviteCode) {
        Group group = groupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new RuntimeException("邀请码无效或群组不存在"));
        
        if (groupMemberRepository.existsByGroupIdAndUserId(group.getId(), userId)) {
            throw new RuntimeException("您已在该群组中");
        }
        
        // 加入者默认为 MEMBER
        GroupMember member = new GroupMember(group.getId(), userId, GroupMember.Role.MEMBER);
        groupMemberRepository.save(member);
        
        int memberCount = (int) groupMemberRepository.countByGroupId(group.getId());
        
        return new GroupInfo(
                String.valueOf(group.getId()),
                group.getName(),
                String.valueOf(group.getOwnerId()),
                group.getInviteCode(),
                memberCount
        );
    }

    // 获取群组成员列表 (v1.9 更新：鉴权 + 排序)
    public List<GroupMemberDto> getGroupMembers(Long requesterId, Long groupId) {
        // 1. 鉴权：检查请求者是否在群组中，且角色为 OWNER 或 ADMIN
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)
                .orElseThrow(() -> new RuntimeException("您不在该群组中"));
        
        if (requester.getRole() == GroupMember.Role.MEMBER) {
            throw new RuntimeException("普通成员无权查看成员列表");
        }

        // 2. 获取所有成员
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        List<Long> userIds = members.stream().map(GroupMember::getUserId).collect(Collectors.toList());
        List<User> users = userRepository.findAllById(userIds);
        
        // 3. 构造 DTO 并排序
        List<GroupMemberDto> dtoList = members.stream().map(member -> {
            User user = users.stream().filter(u -> u.getUserId().equals(member.getUserId())).findFirst().orElse(null);
            if (user == null) return null;
            return new GroupMemberDto(
                    String.valueOf(user.getUserId()),
                    user.getNickname(),
                    user.getPhoneNumber(),
                    member.getRole().name(),
                    member.getJoinedAt()
            );
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());

        // 4. 按昵称字母排序 (中文拼音排序)
        Collator collator = Collator.getInstance(Locale.CHINA);
        dtoList.sort((o1, o2) -> collator.compare(o1.getNickname(), o2.getNickname()));

        return dtoList;
    }

    // 设置/取消管理员
    @Transactional
    public void setAdmin(Long requesterId, Long groupId, Long targetUserId, boolean isAdmin) {
        // 1. 鉴权：只有群主(OWNER)可以设置管理员
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)
                .orElseThrow(() -> new RuntimeException("您不在该群组中"));
        
        if (requester.getRole() != GroupMember.Role.OWNER) {
            throw new RuntimeException("只有群主可以设置管理员");
        }

        // 2. 查找目标成员
        GroupMember target = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new RuntimeException("目标用户不在该群组中"));

        if (target.getRole() == GroupMember.Role.OWNER) {
            throw new RuntimeException("无法更改群主的权限");
        }

        if (isAdmin) {
            // 3. 设为管理员：检查名额限制 (最多2名 ADMIN)
            long adminCount = groupMemberRepository.countByGroupIdAndRole(groupId, GroupMember.Role.ADMIN);
            if (adminCount >= 2) {
                throw new RuntimeException("管理员人数已达上限 (最多2名)");
            }
            target.setRole(GroupMember.Role.ADMIN);
        } else {
            // 4. 取消管理员：降级为 MEMBER
            target.setRole(GroupMember.Role.MEMBER);
        }
        
        groupMemberRepository.save(target);
    }
}
