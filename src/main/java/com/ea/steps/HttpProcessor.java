package com.ea.steps;

import com.ea.dto.HttpRequestData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class HttpProcessor {

    public static void process(Socket socket, HttpRequestData request) {
        try {
            if(request.getMethod().equals("GET")
                    && request.getUri().startsWith("/legalapp")) {
                log.info("Processing legalapp request");
                Resource resource = new ClassPathResource("tosa.en.txt");
                try {
                    String data = Files.readString(Path.of(resource.getURI()));
                    String response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/html;charset=UTF-8\r\n" +
                            "\r\n" +
                            data + "\r\n\0";
                    socket.getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    String response = "HTTP/1.1 500 Internal Server Error\r\n" +
                            "Content-Type: text/plain\r\n" +
                            "\r\n" +
                            "Server error";
                    socket.getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));
                }
            } else {
                log.info("unknown request");
            }
        } catch (IOException e) {
            log.error("Error while processing HTTP request", e);
        }
    }

}