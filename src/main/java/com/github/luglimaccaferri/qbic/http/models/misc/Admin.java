package com.github.luglimaccaferri.qbic.http.models.misc;

import java.util.UUID;

public class Admin extends User{

    public Admin(UUID uuid){ super(uuid); }
    public static Admin from(User user){
        return new Admin(user.getUUID());
    }

}
