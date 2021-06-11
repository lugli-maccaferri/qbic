package com.github.luglimaccaferri.qbic.http.models.misc;

import java.util.HashMap;
import java.util.UUID;

public class User {

    protected final UUID uuid;
    protected String username;
    protected String hash;
    protected final HashMap<String, Boolean> permissions = new HashMap<String, Boolean>();

    public User(UUID uuid){
        this.uuid = uuid;
    }
    public UUID getUUID() { return this.uuid; }

}
