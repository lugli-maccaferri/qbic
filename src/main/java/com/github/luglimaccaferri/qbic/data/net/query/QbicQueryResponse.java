package com.github.luglimaccaferri.qbic.data.net.query;

import java.nio.ByteBuffer;

public class QbicQueryResponse {

    private final byte[] content;
    private byte response_type;
    private int session_id;
    private String motd, gametype, map, online_players, max_players;

    public QbicQueryResponse(byte[] response){

        this.content = response;
        ByteBuffer parsed_content = ByteBuffer.allocate(content.length).put(content);

        parsed_content.get(response_type);
        session_id = parsed_content.getInt(1);
        String[] str_content = new String(this.content).split("\0");

        motd = str_content[4].trim();
        gametype = str_content[5].trim();
        map = str_content[6].trim();
        online_players = str_content[7].trim();
        max_players = str_content[8].trim();

    }

    public String getMotd() { return motd; }
    public String getGametype() { return gametype; }
    public String getMap() { return map; }
    public String getOnlinePlayers() { return online_players; }
    public String getMaxPlayers() { return max_players; }

}
