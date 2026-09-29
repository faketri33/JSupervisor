package org.faketri;

import org.faketri.mapper.ConfigMapper;
import org.faketri.process.reader.ConsoleOutputProcessReader;
import org.faketri.process.reader.InFileProcessReader;
import org.faketri.process.reader.ProcessReader;
import org.faketri.repository.ApplicationInMemoryRepository;
import org.faketri.unixsocket.UnixServerSocket;
import org.faketri.unixsocket.dto.request.AllRequest;
import org.faketri.unixsocket.dto.request.RunRequest;
import org.faketri.unixsocket.dto.request.StopRequest;
import org.faketri.unixsocket.dto.response.AppInfo;
import org.faketri.unixsocket.dto.response.AppsResponse;
import org.faketri.unixsocket.dto.response.ErrorResponse;
import org.faketri.unixsocket.dto.response.OkResponse;
import org.faketri.utils.Constants;
import org.faketri.utils.NotificationSystem;
import org.faketri.utils.YAMLConfigurationParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final ApplicationInMemoryRepository memoryRepository = new ApplicationInMemoryRepository();

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length == 0) {
            log.error("No one argument present");
            return;
        }
        var server = new UnixServerSocket();

        server.registerHandler(AllRequest.class,  (req) -> {
            log.info("Handler client request {}", req);
            var appsInfo = memoryRepository.getAll().stream()
                    .map(a -> new AppInfo(a.getName(), a.getAppId().toString(), -1, a.getConfiguration().getCommands()))
                    .toList();
            return new AppsResponse(appsInfo);
        }) ;

        server.registerHandler(RunRequest.class, (req -> {
            log.info("Handler client request {}", req);
            var app = memoryRepository.getByName(req.app());
            app.start();
            return new OkResponse("Start");
        }));

        server.registerHandler(StopRequest.class, (req -> {
            log.info("Handler client request {}", req);
            try {
                throw new RuntimeException("test");
            } catch (Exception ex){
                return new ErrorResponse(ex.getClass().getSimpleName(), ex.getMessage());
            }
        }));

        new Thread(server).start();

        String home = System.getProperty("user.home").concat("/");

        var cnf = new YAMLConfigurationParser().parse(Path.of(args[0]));
        cnf.getApp().forEach((k, v) -> memoryRepository.save(ConfigMapper.toDto(k, v)));

        var app = memoryRepository.getByName("test");

        ProcessReader toConsole = new ConsoleOutputProcessReader(System.out);
        ProcessReader toFile = new InFileProcessReader(Path.of(home + app.getName() + ".txt"));

        app.listen(toConsole);
        app.listen(toFile);
        // Stupid handler
        app.errHandle(ex -> NotificationSystem.notify(Constants.Global.APP_NAME, ex.getMessage()));

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