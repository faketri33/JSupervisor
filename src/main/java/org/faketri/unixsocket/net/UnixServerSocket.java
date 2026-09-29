package org.faketri.unixsocket.net;

import org.faketri.infrastructure.exceptions.unixserver.BadRequestException;
import org.faketri.unixsocket.UnixSocketUtilities;
import org.faketri.unixsocket.dto.response.ErrorResponse;
import org.faketri.unixsocket.dto.Frame;
import org.faketri.unixsocket.dto.request.Request;
import org.faketri.unixsocket.dto.response.Response;
import org.faketri.unixsocket.handlers.CommandHandler;
import org.faketri.unixsocket.handlers.Dispatcher;
import org.faketri.unixsocket.io.FrameReader;
import org.faketri.unixsocket.io.FrameWriter;
import org.faketri.utils.JsonObjectParser;
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

    @Override
    public void run() {
        try (var channel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
             var executor = Executors.newVirtualThreadPerTaskExecutor()){

            log.info("Server start");

            UnixDomainSocketAddress address = UnixSocketUtilities.address();
            channel.bind(address);
            UnixSocketUtilities.installDirectoryPermissions(address.getPath());

            while (!Thread.currentThread().isInterrupted()) {
                log.info("Waiting client");
                SocketChannel client = channel.accept();
                executor.submit(() -> accept(client));
            }
        }catch (AsynchronousCloseException | InterruptedIOException ignored) {
            Thread.currentThread().interrupt();
        } catch (IOException ex){
            log.error(ex.toString());
        }
    }

    public <R extends Request, S extends Response> void registerHandler(Class<R> type, CommandHandler<R, S> handler) {
        dispatch.register(type, handler);
    }

    private void accept(SocketChannel channel) {
        try (channel){
            Frame frame = FrameReader.read(channel);
            Response response;
            try {
                Request request = parser.decode(frame);
                response = dispatch.dispatch(request);
            } catch (BadRequestException e) {
                response = new ErrorResponse("BAD_REQUEST", e.getMessage());
            } catch (Exception e) {
                log.error("Handler failed", e);
                response = new ErrorResponse("INTERNAL", "internal error");
            }

            FrameWriter.write(channel, parser.encode(response));
            log.info("{}", response);
        } catch (IOException ig){
            log.error(ig.getMessage());
        }
    }
}
