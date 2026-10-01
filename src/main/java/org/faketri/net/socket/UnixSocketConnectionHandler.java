package org.faketri.net.socket;

import org.faketri.infrastructure.exceptions.unixserver.ErrorRequest;
import org.faketri.infrastructure.parser.JsonObjectParser;
import org.faketri.net.handlers.CommandHandler;
import org.faketri.net.handlers.ConnectionHandler;
import org.faketri.net.handlers.Dispatcher;
import org.faketri.net.io.FrameReader;
import org.faketri.net.io.FrameWriter;
import org.faketri.net.io.dto.Frame;
import org.faketri.net.io.dto.request.Request;
import org.faketri.net.io.dto.response.Response;
import org.faketri.net.socket.mapper.RequestExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public final class UnixSocketConnectionHandler implements ConnectionHandler {
    private static final Logger log = LoggerFactory.getLogger(UnixSocketConnectionHandler.class);

    private static final JsonObjectParser parser = new JsonObjectParser();
    private final Dispatcher dispatch = new Dispatcher();

    @Override
    public void handle(SocketChannel channel) {
        try (channel) {
            Response response = safeProcessRequest(FrameReader.read(channel));
            FrameWriter.write(channel, parser.encode(response));
            log.debug("{}", response);
        } catch (IOException ig) {
            log.error(ig.getMessage());
        }
    }

    @Override
    public <REQ extends Request, RES extends Response> void addHandler(Class<REQ> type, CommandHandler<REQ, RES> handler) {
        dispatch.register(type, handler);
    }


    private Response safeProcessRequest(Frame frame) {
        Response response;
        try {
            Request request = parser.decode(frame.payload().array(), Request.class);
            response = dispatch.dispatch(request);
        } catch (ErrorRequest e) {
            response = RequestExceptionMapper.map(e);
        } catch (Exception e) {
            response = RequestExceptionMapper.map(e);
        }

        return response;
    }
}
