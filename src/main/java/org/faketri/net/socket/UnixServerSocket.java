package org.faketri.net.socket;

import org.faketri.infrastructure.exceptions.unixserver.BadRequestException;
import org.faketri.infrastructure.exceptions.unixserver.ErrorRequest;
import org.faketri.infrastructure.parser.JsonObjectParser;
import org.faketri.net.io.FrameReader;
import org.faketri.net.io.FrameWriter;
import org.faketri.net.socket.dto.Frame;
import org.faketri.net.socket.dto.request.Request;
import org.faketri.net.socket.dto.response.ErrorResponse;
import org.faketri.net.socket.dto.response.Response;
import org.faketri.net.socket.handlers.CommandHandler;
import org.faketri.net.socket.handlers.Dispatcher;
import org.faketri.net.socket.mapper.RequestExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;

public class UnixServerSocket implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(UnixServerSocket.class);
    private static final JsonObjectParser parser = new JsonObjectParser();
    private final Dispatcher dispatch = new Dispatcher();

    private UnixServerSocket() {
    }

    public UnixServerSocket of() {
        return new UnixServerSocket();
    }

    @Override
    public void run() {
        try (var channel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
             var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            log.info("Server start");

            UnixDomainSocketAddress address = UnixSocketUtilities.address();
            channel.bind(address);
            UnixSocketUtilities.installDirectoryPermissions(address.getPath());

            while (!Thread.currentThread().isInterrupted()) {
                log.info("Waiting client");
                SocketChannel client = channel.accept();
                executor.submit(() -> accept(client));
            }
        } catch (AsynchronousCloseException | InterruptedIOException ignored) {
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            log.error(ex.toString());
        }
    }

    public <R extends Request, S extends Response> void registerHandler(Class<R> type, CommandHandler<R, S> handler) {
        dispatch.register(type, handler);
    }

    private void accept(SocketChannel channel) {
        try (channel) {
            Response response = safeProcessRequest(FrameReader.read(channel));
            FrameWriter.write(channel, parser.encode(response));
            log.debug("{}", response);
        } catch (IOException ig) {
            log.error(ig.getMessage());
        }
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
