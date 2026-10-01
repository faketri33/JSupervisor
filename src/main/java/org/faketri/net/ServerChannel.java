package org.faketri.net;


import org.faketri.net.handlers.CommandHandler;
import org.faketri.net.io.dto.request.Request;
import org.faketri.net.io.dto.response.Response;

public interface ServerChannel {

    int start();

    <REQ extends Request, RES extends Response> void customHandler(Class<REQ> type, CommandHandler<REQ, RES> handler);
}
