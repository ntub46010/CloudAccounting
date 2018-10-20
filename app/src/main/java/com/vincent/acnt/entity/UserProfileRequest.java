package com.vincent.acnt.entity;

public class UserProfileRequest {
    private String nickName, email, oldPwd, newPwd, newPwdConfirm;

    public UserProfileRequest() {

    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOldPwd() {
        return oldPwd;
    }

    public void setOldPwd(String oldPwd) {
        this.oldPwd = oldPwd;
    }

    public String getNewPwd() {
        return newPwd;
    }

    public void setNewPwd(String newPwd) {
        this.newPwd = newPwd;
    }

    public String getNewPwdConfirm() {
        return newPwdConfirm;
    }

    public void setNewPwdConfirm(String newPwdConfirm) {
        this.newPwdConfirm = newPwdConfirm;
    }
}
