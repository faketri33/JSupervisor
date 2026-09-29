package org.faketri.unixsocket.dto;

import java.nio.ByteBuffer;

public record Frame(Header header, ByteBuffer payload) {}
