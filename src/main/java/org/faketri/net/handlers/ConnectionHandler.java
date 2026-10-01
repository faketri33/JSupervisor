package org.faketri.net.handlers;

import org.faketri.net.io.dto.request.Request;
import org.faketri.net.io.dto.response.Response;

import java.nio.channels.SocketChannel;


public interface ConnectionHandler {
    void handle(SocketChannel channel);
    <REQ extends Request, RES extends Response> void addHandler(Class<REQ> type, CommandHandler<REQ, RES> handler);
}
