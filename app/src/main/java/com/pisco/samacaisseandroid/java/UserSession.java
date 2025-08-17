package com.pisco.samacaisseandroid.java;

public class UserSession {
    private String username;
    private String loginTime;
    private String logoutTime;

    public UserSession(String username, String loginTime, String logoutTime) {
        this.setUsername(username);
        this.setLoginTime(loginTime);
        this.setLogoutTime(logoutTime);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public String getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(String logoutTime) {
        this.logoutTime = logoutTime;
    }
}

