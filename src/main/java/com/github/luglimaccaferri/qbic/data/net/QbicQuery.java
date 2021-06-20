// https://wiki.vg/Query
package com.github.luglimaccaferri.qbic.data.net;

import java.io.IOException;
import java.net.*;

public class QbicQuery {

    private short port;
    private DatagramSocket socket = null;
    private String address;

    public QbicQuery(String address, short port) throws SocketException, UnknownHostException {

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

        return new QbicQueryResponse(send(bytes));

    }
    public int generateChallengeToken() throws IOException {

        byte[] bytes = new QbicQueryRequest(QbicQueryRequest.Type.HANDSHAKE).toBytes();
        byte[] response = send(bytes);

        return Integer.parseInt(new String(response).trim());

    }

}
