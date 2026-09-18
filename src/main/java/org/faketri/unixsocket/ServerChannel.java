package org.faketri.unixsocket;

import org.faketri.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerChannel implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ServerChannel.class);
    private final int PORT = Constants.UnixServerConfiguration.PORT;

    private final ChannelListener listener;
    private volatile boolean run = true;

    public ServerChannel(ChannelListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        run = true;
        try (var channel = ServerSocketChannel.open()){
            channel.bind(new InetSocketAddress(PORT));
            SocketChannel client = channel.accept();

            Thread.ofVirtual().start(() -> {
                try {
                    listener.listen(client, request -> log.debug(request.command()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        }catch (IOException ignored){}
    }

    public void stop(){
        run = false;
    }
}
