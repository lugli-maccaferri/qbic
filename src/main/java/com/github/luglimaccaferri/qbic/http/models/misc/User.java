package com.github.luglimaccaferri.qbic.http.models.misc;

public class User {

    private String username;
    private String password;

    public String print(){
        return this.username + ", " + this.password;
    }

    public void setUsername(String username){ this.username = username; }
    public void setPassword(String password){ this.password = password; }
    public String getPassword(){ return this.password; }
    public String getUsername(){ return this.username; }

}
