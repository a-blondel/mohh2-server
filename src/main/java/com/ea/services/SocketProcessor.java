package com.ea.services;

import com.ea.models.SocketData;
import lombok.extern.slf4j.Slf4j;

import java.net.Socket;

@Slf4j
public class SocketProcessor {

    public static final String NUL = "\0";
    public static final String LF = "\n";

    private static int currentPingValue = 22;

    /**
     * Prepares the response based on request type,
     * then calls the writer
     * @param socket the socket to give to the writer
     * @param socketData the object to process
     */
    public static void process(Socket socket, SocketData socketData) {
        StringBuffer content = new StringBuffer();
        switch (socketData.getId()) {
            case ("@tic"):
                break;
            case ("@dir"):
                content.append("ADDR=127.0.0.1" + LF);
                content.append("PORT=21172" + LF);
                content.append("SESS=1337420011" + LF);
                content.append("MASK=dbbcc81057aa718bbdafe887591112b4" + NUL);
                break;
            case ("addr"):
                content.append("ADDR=" + socket.getLocalAddress().getHostAddress() + NUL);
                break;
            case ("skey"):
                content.append("SKEY=$37940faf2a8d1381a3b7d0d2f570e6a7" + NUL);
                break;
            case ("news"):
                content.append("Official servers are down" + NUL);
                break;
            case ("sele"):
                content.append("MORE=1" + LF);
                content.append("STATS=0" + LF);
                content.append("SLOTS=10" + NUL);
                break;
            case ("auth"):
                content.append("NAME=Name" + LF);
                content.append("ADDR=" + socket.getLocalAddress().getHostAddress() + LF);
                content.append("PERSONAS=User" + LF);
                content.append("LOC=frFR" + LF);
                content.append("MAIL=user@domain.com" + LF);
                content.append("SPAM=NN" + NUL);
                break;
            case ("pers"):
                content.append("PERS=User" + LF);
                content.append("LKEY=3fcf27540c92935b0a66fd3b0000283c" + LF);
                content.append("LOC=frFR" + NUL);
                setTimeout(() -> startPing(socket), socket, 15000);
                break;
            case ("~png"):
                //content.append("TIME=" + time++ + LF);
                //content.append("REF=2023.05.13-21:22:34" + NUL);
                break;
            default:
                log.info("Unsupported operation: {}", socketData.getId());
                break;
        }
        if (content.length() > 0) {
            socketData.setResponse(content.toString());
            SocketWriter.write(socket, socketData);
        }
    }

    public static void startPing(Socket socket) {
        SocketData socketData = new SocketData("~png", null, null, currentPingValue++);
        SocketWriter.write(socket, socketData);
    }

    public static void setTimeout(Runnable runnable, Socket socket, int delay){
        new Thread(() -> {
            try {
                while(!socket.isClosed()) {
                    Thread.sleep(delay);
                    runnable.run();
                }
            }
            catch (Exception e){
                System.err.println(e);
            }
        }).start();
    }

}