package org.example.synjiserver.dto;

import java.time.LocalDateTime;

public class GroupMemberDto {
    private String userId;
    private String nickname;
    private String phoneNumber;
    private String role;
    private LocalDateTime joinedAt;

    public GroupMemberDto() {}

    public GroupMemberDto(String userId, String nickname, String phoneNumber, String role, LocalDateTime joinedAt) {
        this.userId = userId;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
