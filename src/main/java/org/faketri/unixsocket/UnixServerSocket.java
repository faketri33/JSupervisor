package org.faketri.unixsocket;

import org.faketri.utils.Constants;
import org.faketri.utils.UnixSocketUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class UnixServerSocket implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(UnixServerSocket.class);

    private final ChannelListener listener;

    public UnixServerSocket(ChannelListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try (var channel = ServerSocketChannel.open(StandardProtocolFamily.UNIX)){
            Path sock = UnixSocketUtilities.socketDir().resolve(Constants.UnixServerConfiguration.SOCK_NAME);
            Files.deleteIfExists(sock);
            channel.bind(UnixDomainSocketAddress.of(sock));

            while (!Thread.currentThread().isInterrupted()) {
                SocketChannel client = channel.accept();

                // TODO: Creating a Connection Handler
                // First Step: Handling the Connection
                // It decodes the user's message into a Java object
                // and a higher-level abstraction handler
                // works with Java objects.

                // As programmers, we like to work with high-level handlers.
                // And we don't think about what it does underneath the hood.
                // This is next step. high-level user request handler like as in spring

                Thread.ofVirtual().start(() -> {
                    try {
                        listener.listen(client, request -> log.debug(request.command()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }catch (IOException ignored){
            log.error(ignored.getMessage());
        }
    }
}
