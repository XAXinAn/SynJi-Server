package org.example.synjiserver.dto;

public class GroupInfo {
    private String groupId;
    private String name;
    private String ownerId;
    private String inviteCode;
    private int memberCount;

    public GroupInfo() {}

    public GroupInfo(String groupId, String name, String ownerId, String inviteCode, int memberCount) {
        this.groupId = groupId;
        this.name = name;
        this.ownerId = ownerId;
        this.inviteCode = inviteCode;
        this.memberCount = memberCount;
    }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
}
