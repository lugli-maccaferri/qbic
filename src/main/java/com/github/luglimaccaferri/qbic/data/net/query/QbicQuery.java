// https://wiki.vg/Query
package com.github.luglimaccaferri.qbic.data.net.query;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class QbicQuery {

    private final int port;
    private DatagramSocket socket = null;
    private final String address;

    public QbicQuery(String address, int port) throws SocketException, UnknownHostException {

        this.address = address;
        this.port = port;

    }

    private byte[] send(byte[] stuff) throws IOException {

        if(this.socket == null) this.socket = new DatagramSocket();
        this.socket.setSoTimeout(500);
        DatagramPacket p = new DatagramPacket(stuff, stuff.length, InetAddress.getByName(address), port);
        this.socket.send(p);

        byte[] rec = new byte[1024];
        DatagramPacket rec_p = new DatagramPacket(rec, rec.length);
        this.socket.receive(rec_p);

        return rec_p.getData();

    }

    public QbicQueryResponse basicStat() throws IOException{

        int token = generateChallengeToken();
        byte[] bytes = new QbicQueryRequest(QbicQueryRequest.Type.BASIC_STAT, token).toBytes();

        return new QbicQueryResponse(send(bytes), false);

    }

    public QbicQueryResponse fullStat() throws IOException{

        int token = generateChallengeToken();
        byte[] bytes = new QbicQueryRequest(QbicQueryRequest.Type.FULL_STAT, token).toBytes();

        return new QbicQueryResponse(send(bytes), true);

    }

    public int generateChallengeToken() throws IOException {

        byte[] bytes = new QbicQueryRequest(QbicQueryRequest.Type.HANDSHAKE).toBytes();
        byte[] response = send(bytes);

        return Integer.parseInt(new String(response).trim());

    }

}
