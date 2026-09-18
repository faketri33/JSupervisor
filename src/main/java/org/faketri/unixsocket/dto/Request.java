package org.faketri.unixsocket.dto;

import java.io.Serializable;

public record Request(String command, String[] args) implements Serializable {}
