package org.faketri.net.io;

import org.faketri.net.socket.dto.Frame;
import org.faketri.net.socket.dto.Header;
import org.faketri.utils.Constants;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;

public class FrameReader {
    private FrameReader() {
        /* This utility class should not be instantiated */
    }

    static void readFully(ReadableByteChannel channel, ByteBuffer bf) throws IOException {
        while (bf.hasRemaining()) if (channel.read(bf) < 0) throw new EOFException();
    }

    public static Frame read(SocketChannel ch) throws IOException {
        ByteBuffer fixed = ByteBuffer.allocate(Constants.HeaderConfiguration.MIN_SIZE);
        readFully(ch, fixed);
        fixed.flip();
        Header header = Header.parse(fixed);

        int extra = header.headerSize() - Constants.HeaderConfiguration.MIN_SIZE;
        if (extra > 0) readFully(ch, ByteBuffer.allocate(extra));

        ByteBuffer payload = ByteBuffer.allocate(header.payloadSize());
        readFully(ch, payload);
        payload.flip();
        return new Frame(header, payload);
    }

}
