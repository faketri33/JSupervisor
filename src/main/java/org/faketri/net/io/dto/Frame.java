package org.faketri.net.io.dto;

import java.nio.ByteBuffer;

public record Frame(Header header, ByteBuffer payload) {
}
