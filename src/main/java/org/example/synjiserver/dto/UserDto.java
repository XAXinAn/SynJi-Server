package org.example.synjiserver.dto;

public class UserDto {
    private String userId;
    private String phoneNumber;
    private String nickname;
    private boolean isNewUser;

    public UserDto() {
    }

    public UserDto(String userId, String phoneNumber, String nickname, boolean isNewUser) {
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.nickname = nickname;
        this.isNewUser = isNewUser;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isNewUser() {
        return isNewUser;
    }

    public void setNewUser(boolean newUser) {
        isNewUser = newUser;
    }
}
