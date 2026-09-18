package org.faketri;

import org.faketri.mapper.ConfigMapper;
import org.faketri.process.reader.ConsoleOutputProcessReader;
import org.faketri.process.reader.InFileProcessReader;
import org.faketri.process.reader.ProcessReader;
import org.faketri.repository.ApplicationInMemoryRepository;
import org.faketri.unixsocket.ChannelListener;
import org.faketri.unixsocket.RequestHandler;
import org.faketri.unixsocket.ServerChannel;
import org.faketri.unixsocket.dto.Request;
import org.faketri.utils.Constants;
import org.faketri.utils.NotificationSystem;
import org.faketri.utils.YAMLConfigurationParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final ApplicationInMemoryRepository memoryRepository = new ApplicationInMemoryRepository();

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length == 0) return;

        ChannelListener listener = new ChannelListener() {
            SocketChannel client;
            @Override
            public void listen(SocketChannel channel, RequestHandler handler) throws IOException {
                log.debug("Client connected");
                client = channel;
                ByteBuffer bf = ByteBuffer.allocate(1024);
                if (channel.read(bf) != -1) {
                    bf.flip();

                    String request = StandardCharsets.UTF_8.decode(bf).toString();

                    handler.handle(new Request(request, new String[]{}));
                }
                close();
            }

            @Override
            public void close() throws IOException {
                log.debug("Close connection");
                client.close();
            }
        };

        ServerChannel serverChannel = new ServerChannel(listener);
        new Thread(serverChannel).start();

        String home = System.getProperty("user.home").concat("/");

        var cnf = new YAMLConfigurationParser().parse(Path.of(args[0]));
        cnf.getApp().forEach((k, v) -> memoryRepository.save(ConfigMapper.toDto(k, v)));

        var app = memoryRepository.getByName("test");

        ProcessReader toConsole = new ConsoleOutputProcessReader(System.out);
        ProcessReader toFile = new InFileProcessReader(Path.of(home + app.getName() + ".txt"));

        app.listen(toConsole);
        app.listen(toFile);
        // Stupid handler
        app.errHandle(ex -> NotificationSystem.notify("JSupervisor", ex.getMessage()));

        memoryRepository.startAllByProfile(Constants.ConfigurationConstants.DEFAULT_PROFILE);

        String format = "%-36s  %-15s  %-10s  %-10s";
        log.info(String.format(format, "ID", "NAME", "PID", "STATE"));

        memoryRepository.getAll().forEach(application ->
                log.info(String.format(format,
                        application.getAppId(),
                        application.getName(),
                        application.getPid(),
                        application.getState()))
        );

        Thread.currentThread().join();
    }
}