package com.clavicusoft.wumpus.Database;


public class UserDetails {
    public String userName;
    public String cave;
    public String status;

    public UserDetails (String userName, String cave, String status) {
        this.userName = userName;
        this.status = status;
        this.cave = cave;
    }

    public String getUserName() {
        return userName;
    }

    public String getCave() {
        return cave;
    }

    public String getStatus() {
        return status;
    }
}
