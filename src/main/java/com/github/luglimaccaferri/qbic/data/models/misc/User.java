package com.github.luglimaccaferri.qbic.data.models.misc;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.luglimaccaferri.qbic.data.models.Server;

import java.util.HashMap;
import java.util.UUID;

public class User {

    protected final UUID uuid;
    protected String username;
    protected String emitter;

    protected final HashMap<String, Byte> permissions = new HashMap<String, Byte>();

    public User(String username, UUID uuid, HashMap<String, Byte> permissions){
        this.uuid = uuid;
        this.username = username;
        this.permissions.put("admin", permissions.get("admin"));
        this.permissions.put("edit_fs", permissions.get("edit_fs"));
        this.permissions.put("edit_others", permissions.get("edit_others"));
    }

    public String getUsername() { return username; }
    public HashMap<String, Byte> getPermissions() { return permissions; }
    public UUID getUUID() { return uuid; }
    public String getEmitter(){ return this.emitter; }
    public void resetEmitter(){ this.emitter = ""; }
    public boolean canEditThis(Server server){
        return isAdmin() || getUUID().toString().equals(server.getOwner());
    }

    public void setEmitter(String server_id){ this.emitter = server_id; }

    public static User fromJWT(DecodedJWT jwt){
        HashMap<String, Byte> permissions = new HashMap<String, Byte>();

        permissions.put("admin", Byte.valueOf(jwt.getClaim("is_admin").asString()));
        permissions.put("edit_fs", Byte.valueOf(jwt.getClaim("edit_fs").asString()));
        permissions.put("edit_others", Byte.valueOf(jwt.getClaim("edit_others").asString()));

        System.out.println(permissions);

        return new User(
                jwt.getClaim("username").asString(),
                UUID.fromString(jwt.getClaim("user_id").asString()),
                permissions
        );
    }

    public boolean canEditFs(){
        return this.permissions.get("edit_fs") > 0;
    }
    public boolean isAdmin(){ return this.permissions.get("admin") > 0; }
    public boolean canEditOthers(){
        return this.permissions.get("edit_others") > 0;
    }
}
