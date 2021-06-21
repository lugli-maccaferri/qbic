package com.github.luglimaccaferri.qbic.data.net.query;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class QbicQueryRequest {

    public enum Type {
        HANDSHAKE((byte) 9),
        BASIC_STAT((byte) 0);

        public final byte type;

        private Type(byte t) {
            this.type = t;
        }
    }

    private final Type req_type;
    private ByteBuffer stuff;
    private final int SESSION_ID = 1; // può essere qualsiasi cosa, magari facciamolo randomico poi
    private final byte[] payload;
    private final ByteArrayOutputStream bs = new ByteArrayOutputStream();
    private final DataOutputStream data = new DataOutputStream(bs);

    private final byte[] MAGIC_NUMBER = { (byte) 0xfe, (byte) 0xfd};

    public QbicQueryRequest(Type t){
        req_type = t; payload = new byte[]{};
    }
    public QbicQueryRequest(Type t, int payload){
        req_type = t; this.payload = intToByte(payload);
    }
    public byte[] toBytes() throws IOException {

        bs.reset();

        data.write(MAGIC_NUMBER);
        data.write(req_type.type);
        data.writeInt(SESSION_ID);
        data.write(payload);

        if(req_type == Type.HANDSHAKE) data.write(new byte[]{0, 0, 0, 0});

        return bs.toByteArray();

    }

    private byte[] intToByte(int payload){

        return new byte[]{
                (byte) (payload >>> 24	& 0xFF),
                (byte) (payload >>> 16	& 0xFF),
                (byte) (payload >>> 8	& 0xFF),
                (byte) (payload         & 0xFF)
        };
    }

}