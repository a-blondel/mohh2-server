package com.ea;

import com.ea.config.ServerConfig;
import com.ea.config.SslSocketThread;
import com.ea.config.TcpSocketThread;
import com.ea.enums.Certificates;
import com.ea.services.GameService;
import com.ea.services.PersonaService;
import com.ea.services.SocketManager;
import com.ea.steps.SocketReader;
import com.ea.steps.SocketWriter;
import com.ea.utils.Props;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class ServerApp implements CommandLineRunner {

    private final ScheduledExecutorService processExpiredGamesThread = Executors.newSingleThreadScheduledExecutor();
    private ExecutorService clientHandlingExecutor = Executors.newFixedThreadPool(100);

    private final Props props;
    private final ServerConfig serverConfig;
    private final SocketManager socketManager;
    private final SocketReader socketReader;
    private final SocketWriter socketWriter;
    private final PersonaService personaService;
    private final GameService gameService;

    public static void main(String[] args) {
        SpringApplication.run(ServerApp.class, args);
    }

    @Override
    public void run(String... args) {
        setupThreadPool();
        
        gameService.closeUnfinishedConnectionsAndGames();

        Security.setProperty("jdk.tls.disabledAlgorithms", "");
        System.setProperty("https.protocols", props.getSslProtocols());
        System.setProperty("https.cipherSuites", props.getSslCipherSuites());
        System.setProperty("jdk.tls.client.cipherSuites", props.getSslCipherSuites());
        System.setProperty("jdk.tls.server.cipherSuites", props.getSslCipherSuites());

        if (props.isSslDebugEnabled()) {
            System.setProperty("javax.net.debug", "all");
        }

        startTcpTunnelServer();
        try {
            if(props.getHostedGames().contains("mohh_psp_pal")) {
                ServerSocket mohhPspPalTcpServerSocket = serverConfig.createTcpServerSocket(11180);
                startServerThread(mohhPspPalTcpServerSocket, this::createTcpSocketThread);
                SSLServerSocket mohhPspPalSslServerSocket = serverConfig.createSslServerSocket(11181, Certificates.MOHH_PSP);
                startServerThread(mohhPspPalSslServerSocket, this::createSslSocketThread);
            }
            if(props.getHostedGames().contains("mohh_psp_ntsc")) {
                ServerSocket mohhPspNtscTcpServerSocket = serverConfig.createTcpServerSocket(11190);
                startServerThread(mohhPspNtscTcpServerSocket, this::createTcpSocketThread);
                SSLServerSocket mohhPspNtscSslServerSocket = serverConfig.createSslServerSocket(11191, Certificates.MOHH_PSP);
                startServerThread(mohhPspNtscSslServerSocket, this::createSslSocketThread);
            }
            if(props.getHostedGames().contains("mohh2_psp_pal")) {
                ServerSocket mohh2PspPalTcpServerSocket = serverConfig.createTcpServerSocket(21180);
                startServerThread(mohh2PspPalTcpServerSocket, this::createTcpSocketThread);
                SSLServerSocket mohh2PspPalSslServerSocket = serverConfig.createSslServerSocket(21181, Certificates.MOHH2_PSP);
                startServerThread(mohh2PspPalSslServerSocket, this::createSslSocketThread);
            }
            if(props.getHostedGames().contains("mohh2_psp_ntsc")) {
                ServerSocket mohh2PspNtscTcpServerSocket = serverConfig.createTcpServerSocket(21190);
                startServerThread(mohh2PspNtscTcpServerSocket, this::createTcpSocketThread);
                SSLServerSocket mohh2PspNtscSslServerSocket = serverConfig.createSslServerSocket(21191, Certificates.MOHH2_PSP);
                startServerThread(mohh2PspNtscSslServerSocket, this::createSslSocketThread);
            }
            if(props.getHostedGames().contains("mohh2_wii_pal")) {
                ServerSocket mohh2WiiPalTcpServerSocket = serverConfig.createTcpServerSocket(21170);
                startServerThread(mohh2WiiPalTcpServerSocket, this::createTcpSocketThread);
                SSLServerSocket mohh2WiiPalSslServerSocket = serverConfig.createSslServerSocket(21171, Certificates.MOHH2_WII);
                startServerThread(mohh2WiiPalSslServerSocket, this::createSslSocketThread);
            }
            if(props.getHostedGames().contains("mohh2_wii_ntsc")) {
                ServerSocket mohh2WiiNtscTcpServerSocket = serverConfig.createTcpServerSocket(21120);
                startServerThread(mohh2WiiNtscTcpServerSocket, this::createTcpSocketThread);
                SSLServerSocket mohh2WiiNtscSslServerSocket = serverConfig.createSslServerSocket(21121, Certificates.MOHH2_WII);
                startServerThread(mohh2WiiNtscSslServerSocket, this::createSslSocketThread);
            }
        } catch (Exception e) {
            log.error("Error starting servers", e);
        }
        processExpiredGames();
        gracefullyExit();
    }

    private void startServerThread(ServerSocket serverSocket, Function<Socket, Runnable> runnableFactory) {
        new Thread(() -> {
            try {
                log.info("Starting server thread for port: {}", serverSocket.getLocalPort());
                while (true) {
                    Socket socket = serverSocket.accept();
                    if (!(socket instanceof SSLSocket)) {
                        socketManager.addSocket(socket.getRemoteSocketAddress().toString(), socket);
                    }
                    clientHandlingExecutor.submit(runnableFactory.apply(socket));
                }
            } catch (IOException e) {
                log.error("Error accepting connections on port: {}", serverSocket.getLocalPort(), e);
            }
        }).start();
    }

    private void startTcpTunnelServer() {
        new Thread(() -> {
            log.info("Starting tunnel server on port 80");
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<>() {
                            @Override
                            protected void initChannel(Channel ch) {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(new HttpServerCodec());
                                pipeline.addLast(new TunnelHandler());
                            }
                        });
                ChannelFuture f = b.bind(80).sync();
                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }).start();
    }

    private Runnable createTcpSocketThread(Socket socket) {
        return new TcpSocketThread(socket, socketManager, socketReader, socketWriter, personaService, gameService);
    }

    private Runnable createSslSocketThread(Socket socket) {
        return new SslSocketThread((SSLSocket) socket, socketReader);
    }

    private void gracefullyExit() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down...");
            gameService.closeUnfinishedConnectionsAndGames();
            processExpiredGamesThread.shutdown();
            clientHandlingExecutor.shutdown();
            try {
                if (!processExpiredGamesThread.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    processExpiredGamesThread.shutdownNow();
                }
                if (!clientHandlingExecutor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    clientHandlingExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                processExpiredGamesThread.shutdownNow();
                clientHandlingExecutor.shutdownNow();
            }
            log.info("Shutdown complete.");
        }));
    }

    private void processExpiredGames() {
        processExpiredGamesThread.scheduleAtFixedRate(gameService::closeExpiredGames, 30, 60, TimeUnit.SECONDS);
    }

    private void setupThreadPool() {
        int poolSize = 200;
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(300);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();

        clientHandlingExecutor = new ThreadPoolExecutor(
                poolSize,
                poolSize,
                0L,
                TimeUnit.MILLISECONDS,
                queue,
                threadFactory,
                handler
        );
    }

    static class TunnelHandler extends ChannelInboundHandlerAdapter {
        private DefaultHttpRequest httpRequest;
        private final StringBuilder requestBody = new StringBuilder();

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
            System.out.println("msg: " + msg);
            if (msg instanceof DefaultHttpRequest request) {
                httpRequest = request;
            } else if (msg instanceof DefaultHttpContent content && !(msg instanceof LastHttpContent)) {
                ByteBuf buffer = content.content();
                requestBody.append(buffer.toString(CharsetUtil.UTF_8));
            } else if (msg instanceof LastHttpContent lastContent) {
                String uri = httpRequest.uri();
                HttpMethod method = httpRequest.method();
                HttpHeaders headers = httpRequest.headers();
                ByteBuf content = lastContent.content();
                requestBody.append(content.toString(CharsetUtil.UTF_8));

                System.out.println("uri: " + uri);
                System.out.println("method: " + method);
                System.out.println("headers: " + headers);
                System.out.println("requestBody: " + requestBody);

                URL restUrl = new URL("http://localhost:8080" + uri);
                HttpURLConnection connection = (HttpURLConnection) restUrl.openConnection();
                connection.setRequestMethod(method.name());
                for (Map.Entry<String, String> entry : headers) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
                if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT) || method.equals(HttpMethod.PATCH)) {
                    connection.setDoOutput(true);
                    BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream());
                    out.write(requestBody.toString().getBytes());
                    out.flush();
                }

                int responseCode = connection.getResponseCode();
                byte[] responseBytes;
                if (uri.startsWith("/images/")) {
                    InputStream inputStream = connection.getInputStream();
                    responseBytes = IOUtils.toByteArray(inputStream);
                } else {
                    StringBuilder responseBody;
                    try (StringWriter writer = new StringWriter()) {
                        IOUtils.copy(connection.getInputStream(), writer, StandardCharsets.UTF_8);
                        responseBody = new StringBuilder(writer.toString());
                    }
                    System.out.println("responseBody: " + responseBody);
                    responseBytes = responseBody.toString().getBytes();
                }

                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.valueOf(responseCode),
                        Unpooled.copiedBuffer(responseBytes));

                ctx.write(response);
                ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                ctx.close();
                requestBody.setLength(0);
            } else {
                ctx.fireChannelRead(msg);
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }

}
