package org.faketri.net.socket;

import org.faketri.net.RequestDispatcherCommand;
import org.faketri.net.exceptions.request.BadRequestException;
import org.faketri.net.exceptions.request.ErrorRequest;
import org.faketri.net.handlers.ConnectionHandler;
import org.faketri.net.handlers.RequestProcessing;
import org.faketri.net.io.FrameReader;
import org.faketri.net.io.FrameWriter;
import org.faketri.net.io.dto.Frame;
import org.faketri.net.io.dto.request.Request;
import org.faketri.net.io.dto.response.ResponseEntry;
import org.faketri.net.io.json.JsonObjectMapper;
import org.faketri.net.io.json.exception.JsonProcessingException;
import org.faketri.net.socket.mapper.RequestExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;

final class UnixSocketConnectionHandler implements ConnectionHandler {
    private static final Logger log = LoggerFactory.getLogger(UnixSocketConnectionHandler.class);

    private static final JsonObjectMapper parser = new JsonObjectMapper();
    private final RequestProcessing processing;
    private final RequestDispatcherCommand dispatch;

    public UnixSocketConnectionHandler(RequestDispatcherCommand dispatch, RequestProcessing processing) {
        this.dispatch = Objects.requireNonNull(dispatch);
        this.processing = Objects.requireNonNull(processing);
    }

    private static byte[] bytes(ByteBuffer buf) {
        ByteBuffer d = buf.duplicate();
        byte[] out = new byte[d.remaining()];
        d.get(out);
        return out;
    }

    @Override
    public void handle(SocketChannel channel) {
        try (channel) {
            ResponseEntry<?> response = process(channel);
            FrameWriter.write(channel, encodeSafely(response));
            log.debug("{}", response);
        } catch (IOException e) {
            log.warn("Connection failed", e);
        }
    }

    private ResponseEntry<?> process(SocketChannel channel) {
        try {
            Frame frame = FrameReader.read(channel);
            Request request = parser.decode(bytes(frame.payload()), Request.class);
            return dispatch.dispatch(processing.process(request));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return RequestExceptionMapper.map(new BadRequestException("Cannot read request params"));
        } catch (ErrorRequest e) {
            return RequestExceptionMapper.map(e);
        } catch (Exception e) {
            log.error("Unexpected error while processing request", e);
            return RequestExceptionMapper.map(e);
        }
    }

    private byte[] encodeSafely(ResponseEntry<?> response) {
        try {
            return parser.encode(response);
        } catch (RuntimeException e) {
            log.error("Failed to encode response", e);
            return parser.encode(RequestExceptionMapper.map(e));
        }
    }
}
