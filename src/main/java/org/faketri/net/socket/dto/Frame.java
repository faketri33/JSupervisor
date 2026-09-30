package org.faketri.net.socket.dto;

import java.nio.ByteBuffer;

public record Frame(Header header, ByteBuffer payload) {
}
