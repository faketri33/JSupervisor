package org.faketri.unixsocket.dto;

import org.faketri.utils.Constants;

import java.net.ProtocolException;
import java.nio.ByteBuffer;

import static org.faketri.utils.Constants.HeaderConfiguration.MAGIC;
import static org.faketri.utils.Constants.HeaderConfiguration.MIN_SIZE;

public record Header(int version, int headerSize, int payloadSize) {

    public static Header parse(ByteBuffer buf) throws ProtocolException {
        if (buf.getShort() != MAGIC) throw new ProtocolException("bad magic");
        int version = buf.get() & 0xFF;
        int headerSize = buf.getShort() & 0xFFFF;
        int payloadSize = buf.getInt();
        if (headerSize < MIN_SIZE) throw new ProtocolException("bad headerSize");
        if (payloadSize < 0 || payloadSize > Constants.UnixServerConfiguration.MAX_PAYLOAD)
            throw new ProtocolException("bad payloadSize");
        return new Header(version, headerSize, payloadSize);
    }

    public ByteBuffer toBuffer() {
        return ByteBuffer.allocate(MIN_SIZE)
                .putShort(MAGIC).put((byte) version)
                .putShort((short) MIN_SIZE).putInt(payloadSize)
                .flip();
    }
}