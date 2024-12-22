package com.ea.steps;

import com.ea.dto.HttpRequestData;
import com.ea.services.http.NwcService;
import com.ea.services.http.TosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Socket;

@Slf4j
@RequiredArgsConstructor
@Component
public class HttpProcessor {

    private final TosService tosService;
    private final NwcService nwcService;


    public void process(Socket socket, HttpRequestData request) {
        try {
            if(request.getMethod().equals("GET")
                    && request.getUri().startsWith("/legalapp")) {
                tosService.legalApp(socket);
            } else if (request.getMethod().equals("POST")
                    && request.getUri().startsWith("/ac")) {
                nwcService.ac(socket, request);
            }
        } catch (IOException e) {
            log.error("Error while processing HTTP request", e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                log.error("Error while closing socket", e);
            }
        }
    }

}
