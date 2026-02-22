package org.example.synjiserver.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class LoginRequest {
    private String phoneNumber;

    // Accept both "verifyCode" and legacy "code" from different clients.
    @JsonAlias({"verifyCode", "code"})
    private String verifyCode;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }
}
