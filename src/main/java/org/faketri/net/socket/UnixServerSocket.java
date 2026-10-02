package org.faketri.net.socket;

import org.faketri.net.RequestDispatcherCommand;
import org.faketri.net.ServerChannel;
import org.faketri.net.handlers.ConnectionHandler;
import org.faketri.net.handlers.StupidDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.Executors;

public class UnixServerSocket implements ServerChannel {

    private static final Logger log = LoggerFactory.getLogger(UnixServerSocket.class);
    private final ConnectionHandler handler;

    private UnixServerSocket(ConnectionHandler handler) {
        this.handler = Objects.requireNonNull(handler);
    }

    public static UnixServerSocket of() {
        return new UnixServerSocket(new UnixSocketConnectionHandler(new StupidDispatcher()));
    }

    public static UnixServerSocket of(RequestDispatcherCommand dispatcherCommand) {
        return new UnixServerSocket(new UnixSocketConnectionHandler(dispatcherCommand));
    }

    @Override
    public void start() {
        try (var channel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
             var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            log.debug("Server start");

            UnixDomainSocketAddress address = UnixSocketUtilities.address();
            channel.bind(address);
            UnixSocketUtilities.installDirectoryPermissions(address.getPath());

            while (!Thread.currentThread().isInterrupted()) {
                log.debug("Waiting client");
                SocketChannel client = channel.accept();
                executor.submit(() -> handler.handle(client));
            }
        } catch (AsynchronousCloseException | InterruptedIOException ignored) {
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            log.error(ex.toString());
        }
    }
}
