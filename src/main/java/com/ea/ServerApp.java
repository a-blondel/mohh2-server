package com.ea;

import com.ea.config.ServerConfig;
import com.ea.config.SslSocketThread;
import com.ea.config.TcpSocketThread;
import com.ea.enums.Certificates;
import com.ea.services.GameService;
import com.ea.services.SocketManager;
import com.ea.utils.Props;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.core.env.Environment;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Security;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Entry point
 */
@Slf4j
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class ServerApp implements CommandLineRunner {

    private ScheduledExecutorService processExpiredGamesThread = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    private Props props;

    @Autowired
    private Environment env;

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private GameService gameService;

    public static void main(String[] args) {
        SpringApplication.run(ServerApp.class, args);
    }

    @Override
    public void run(String... args) {
        gameService.closeUnfinishedConnectionsAndGames();

        Security.setProperty("jdk.tls.disabledAlgorithms", "");
        System.setProperty("https.protocols", props.getSslProtocols());
        System.setProperty("https.cipherSuites", props.getSslCipherSuites());
        System.setProperty("jdk.tls.client.cipherSuites", props.getSslCipherSuites());
        System.setProperty("jdk.tls.server.cipherSuites", props.getSslCipherSuites());

        if (props.isSslDebugEnabled()) {
            System.setProperty("javax.net.debug", "all");
        }

        try {
            log.info("Starting servers...");

            if(props.getHostedGames().contains("mohh_psp_pal")) {
                SSLServerSocket mohh2PspPalSslServerSocket = serverConfig.createSslServerSocket(11181, Certificates.MOHH_PSP);
                startServerThread(mohh2PspPalSslServerSocket, (socket) -> new SslSocketThread((SSLSocket) socket));
                log.info("MOHH PSP PAL SSL server started.");
            }

            if(props.getHostedGames().contains("mohh_psp_ntsc")) {
                SSLServerSocket mohh2PspNtscSslServerSocket = serverConfig.createSslServerSocket(11191, Certificates.MOHH_PSP);
                startServerThread(mohh2PspNtscSslServerSocket, (socket) -> new SslSocketThread((SSLSocket) socket));
                log.info("MOHH PSP NTSC SSL server started.");
            }

            if(props.getHostedGames().contains("mohh2_psp_pal")) {
                SSLServerSocket mohh2PspPalSslServerSocket = serverConfig.createSslServerSocket(21181, Certificates.MOHH2_PSP);
                startServerThread(mohh2PspPalSslServerSocket, (socket) -> new SslSocketThread((SSLSocket) socket));
                log.info("MOHH2 PSP PAL SSL server started.");
            }

            if(props.getHostedGames().contains("mohh2_psp_ntsc")) {
                SSLServerSocket mohh2PspNtscSslServerSocket = serverConfig.createSslServerSocket(21191, Certificates.MOHH2_PSP);
                startServerThread(mohh2PspNtscSslServerSocket, (socket) -> new SslSocketThread((SSLSocket) socket));
                log.info("MOHH2 PSP NTSC SSL server started.");
            }

            if(props.getHostedGames().contains("mohh2_wii_pal")) {
                SSLServerSocket mohh2WiiPalSslServerSocket = serverConfig.createSslServerSocket(21171, Certificates.MOHH2_WII);
                startServerThread(mohh2WiiPalSslServerSocket, (socket) -> new SslSocketThread((SSLSocket) socket));
                log.info("MOHH2 WII PAL SSL server started.");
            }

            if(props.getHostedGames().contains("mohh2_wii_ntsc")) {
                SSLServerSocket mohh2WiiNtscSslServerSocket = serverConfig.createSslServerSocket(21121, Certificates.MOHH2_WII);
                startServerThread(mohh2WiiNtscSslServerSocket, (socket) -> new SslSocketThread((SSLSocket) socket));
                log.info("MOHH2 WII NTSC SSL server started.");
            }

            ServerSocket mohTcpServerSocket = serverConfig.createTcpServerSocket(props.getTcpPort());
            startServerThread(mohTcpServerSocket, TcpSocketThread::new);
            log.info("TCP server started.");

            if (props.isTosEnabled()) {
                ServerSocket tosTcpServerSocket = serverConfig.createTcpServerSocket(80);
                startServerThread(tosTcpServerSocket, TcpSocketThread::new);
                log.info("TOS TCP server started.");

                SSLServerSocket tosSslServerSocket = serverConfig.createSslServerSocket(443, Certificates.TOS);
                startServerThread(tosSslServerSocket, (socket) -> new SslSocketThread((SSLSocket) socket));
                log.info("TOS SSL server started.");
            }

            log.info("Servers started. Waiting for client connections...");
        } catch (Exception e) {
            log.error("Error starting servers", e);
        }
        processExpiredGames();
        gracefullyExit();
    }

    private void startServerThread(ServerSocket serverSocket, Function<Socket, Runnable> runnableFactory) {
        new Thread(() -> {
            try {
                while (true) {
                    Socket socket = serverSocket.accept();
                    if (!(socket instanceof SSLSocket)) {
                        SocketManager.addSocket(socket.getRemoteSocketAddress().toString(), socket);
                    }
                    new Thread(runnableFactory.apply(socket)).start();
                }
            } catch (IOException e) {
                log.error("Error accepting connections", e);
            }
        }).start();
    }

    private void gracefullyExit() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down...");
            gameService.closeUnfinishedConnectionsAndGames();
            processExpiredGamesThread.shutdown();
            try {
                if (!processExpiredGamesThread.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    processExpiredGamesThread.shutdownNow();
                }
            } catch (InterruptedException e) {
                processExpiredGamesThread.shutdownNow();
            }
            log.info("Shutdown complete.");
        }));
    }

    private void processExpiredGames() {
        processExpiredGamesThread.scheduleAtFixedRate(() -> {
            gameService.closeExpiredGames();
        }, 60, 120, TimeUnit.SECONDS);
    }

}
