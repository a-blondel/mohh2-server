package com.ea.steps;

import com.ea.dto.SocketData;
import com.ea.dto.SocketWrapper;
import com.ea.services.SocketManager;
import com.ea.utils.BeanUtil;
import com.ea.utils.HexUtils;
import com.ea.utils.Props;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static com.ea.utils.SocketUtils.NEWLINE_CHAR;
import static java.util.stream.Collectors.joining;

@Slf4j
public class SocketWriter {

    private static Props props = BeanUtil.getBean(Props.class);


    public static void write(Socket socket, SocketData socketData) {
        write(socket, socketData, NEWLINE_CHAR);
    }

    /**
     * Builds the full output message based on the data id and content
     * Then sends it through the socket
     * @param socket the socket to write into
     * @param socketData the object to use to write the message
     * @throws IOException
     */
    public static void write(Socket socket, SocketData socketData, String joiner) {

        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             DataOutputStream writer = new DataOutputStream(buffer)) {

            writer.write(socketData.getIdMessage().getBytes(StandardCharsets.UTF_8));
            if(socketData.getIdMessage().length() == 4) {
                writer.writeInt(0);
            }
            int outputLength = 12;

            if (null != socketData.getOutputData()) {
                byte[] contentBytes = (socketData.getOutputData().entrySet()
                        .stream()
                        .map(param -> param.getKey() + "=" + param.getValue())
                        .collect(joining(joiner)) + "\0").getBytes(StandardCharsets.UTF_8);

                outputLength += contentBytes.length;
                writer.writeInt(outputLength);
                writer.write(contentBytes);
            } else {
                writer.writeInt(outputLength);
            }

            byte[] bufferBytes = buffer.toByteArray();
            SocketWrapper socketWrapper = SocketManager.getSocketWrapper(socket);
            String playerInfo = "";
            if (socketWrapper != null && socketWrapper.getAccountEntity() != null) {
                String account = socketWrapper.getAccountEntity().getName();
                String role = socketWrapper.isHost() ? "host" : "client";
                playerInfo = account + " (" + role + ")";
            }
            if (!props.getTcpDebugExclusions().contains(socketData.getIdMessage())) {
                log.info("--> {} {} {}", socket.getRemoteSocketAddress().toString(),
                        props.isTcpDebugEnabled() ? playerInfo : socketData.getIdMessage(),
                        props.isTcpDebugEnabled() ? "\n" + HexUtils.formatHexDump(bufferBytes) : playerInfo);
            }

            socket.getOutputStream().write(bufferBytes);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
