package org.faketri.unixsocket;

import org.faketri.unixsocket.dto.Header;
import org.faketri.utils.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class FrameWriter {
    private FrameWriter() {}

    public static void write(SocketChannel ch, byte[] payload) throws IOException {
        ByteBuffer header = new Header(
                Constants.HeaderConfiguration.VERSION,
                Constants.HeaderConfiguration.MIN_SIZE,
                payload.length
        ).toBuffer();

        ByteBuffer body = ByteBuffer.wrap(payload);

        ByteBuffer[] parts = { header, body };
        while (header.hasRemaining() || body.hasRemaining()) {
            ch.write(parts);
        }
    }
}
