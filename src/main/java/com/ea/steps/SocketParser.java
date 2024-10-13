package com.ea.steps;

import com.ea.dto.HttpRequestData;
import com.ea.utils.*;
import com.ea.dto.SocketData;
import lombok.extern.slf4j.Slf4j;

import java.net.Socket;
import java.nio.ByteBuffer;

@Slf4j
public class SocketParser {

    private static Props props = BeanUtil.getBean(Props.class);
    private static ByteBuffer messageBuffer = ByteBuffer.allocate(1024);

    /**
     * Parses input messages based on current content of the stream
     * Loops until all complete messages are parsed
     * Sends complete messages to the processor
     * @param socket the socket to exchange with
     * @param buffer the buffer to read from
     * @param readLength the size of written content in buffer
     */
    public static void parse(Socket socket, byte[] buffer, int readLength) {
        if (HttpRequestUtils.isHttpPacket(buffer)) {
            handleHttpRequest(socket, buffer);
        } else {
            handleSocketData(socket, buffer, readLength);
        }
    }

    private static void handleHttpRequest(Socket socket, byte[] buffer) {
        HttpRequestData request = HttpRequestUtils.extractHttpRequest(buffer);
        HttpProcessor.process(socket, request);
    }

    private static void handleSocketData(Socket socket, byte[] buffer, int readLength) {
        if (messageBuffer.remaining() < readLength) {
            ByteBuffer newBuffer = ByteBuffer.allocate(messageBuffer.capacity() + readLength);
            messageBuffer.flip();
            newBuffer.put(messageBuffer);
            messageBuffer = newBuffer;
        }

        messageBuffer.put(buffer, 0, readLength);
        messageBuffer.flip();

        while (messageBuffer.remaining() >= 12) {
            int messageSize = SocketUtils.getlength(messageBuffer.array(), messageBuffer.position());
            if (messageBuffer.remaining() >= messageSize) {
                byte[] message = new byte[messageSize];
                messageBuffer.get(message);
                processCompleteMessage(socket, message, messageSize);
            } else {
                break;
            }
        }

        messageBuffer.compact();
    }

    private static void processCompleteMessage(Socket socket, byte[] message, int messageSize) {
        String id = new String(message, 0, 4);
        String content = new String(message, 12, messageSize - 12);
        SocketData socketData = new SocketData(id, content, null);

        if (props.isTcpDebugEnabled() && !props.getTcpDebugExclusions().contains(socketData.getIdMessage())) {
            log.info("Received from {}:{} :\n{}", socket.getInetAddress().getHostAddress(), socket.getPort(), HexUtils.formatHexDump(message));
        }

        SocketProcessor.process(socket, socketData);
    }

}
