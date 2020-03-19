package com.diablo.jayson.kicksv1.Models;

import java.io.Serializable;

public class Host implements Serializable {

    private String userName;
    private String uid;

    public Host(String userName, String uid) {
        this.userName = userName;
        this.uid = uid;
    }

    public Host() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
