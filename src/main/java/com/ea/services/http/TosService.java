package com.ea.services.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class TosService {

    public void legalApp(Socket socket) throws IOException {
        log.info("Processing legalapp request");
        Resource resource = new ClassPathResource("tosa.en.txt");
        try {
            String data = Files.readString(Path.of(resource.getURI()));
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html;charset=UTF-8\r\n" +
                    "\r\n" +
                    data;
            socket.getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            String response = "HTTP/1.1 500 Internal Server Error\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "Server error";
            socket.getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}
