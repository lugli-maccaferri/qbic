package com.github.luglimaccaferri.qbic.data.net.query;

import java.util.Arrays;

public class QbicQueryResponse {

    private String motd = null;
    private String gametype = null;
    private String map = null;
    private String online_players = null;
    private String max_players = null;

    private String game_id = null;
    private String version = null;
    private String plugins = null;
    private int port = -1;
    private String[] players = {};

    public QbicQueryResponse(byte[] response, boolean is_full){

        System.out.println(Arrays.toString(response));

        if(!is_full) parseBasicStat(response);
        else parseFullStat(response);

    }

    public String getMotd() { return motd; }
    public String getGametype() { return gametype; }
    public String getMap() { return map; }
    public String getOnlinePlayers() { return online_players; }
    public String getMaxPlayers() { return max_players; }
    public int getPort() { return port; }
    public String getGameId() { return game_id; }
    public String getPlugins() { return plugins; }
    public String getVersion() { return version; }
    public String[] getPlayers() { return players; }

    private void parseBasicStat(byte[] response){

        String[] str_content = new String(response).split("\0");

        motd = str_content[4].trim();
        gametype = str_content[5].trim();
        map = str_content[6].trim();
        online_players = str_content[7].trim();
        max_players = str_content[8].trim();

    }

    private void parseFullStat(byte[] response){

        byte[] meaningful = Arrays.copyOfRange(response, 11, response.length);
        String[] sections = new String(meaningful).split("\\x00\\x01player_\\x00\\x00");
        String[] kv = sections[0].split("\0");
        String[] p = sections[1].split("\0");

        System.out.println(Arrays.toString(kv));

        motd = kv[3];
        gametype = kv[5];
        game_id = kv[7];
        version = kv[9];
        plugins = kv[11];
        map = kv[13];
        online_players = kv[15];
        max_players = kv[17];
        port = Integer.parseInt(kv[19]);
        players = p;

    }

}
