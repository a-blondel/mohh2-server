package com.ea.services.http;

import com.ea.dto.HttpRequestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class NwcService {

    private static final Logger log = LoggerFactory.getLogger(NwcService.class);

    public void ac(Socket socket, HttpRequestData request) throws IOException {
        String status = "";
        String body = "";

        if (request.getBody().isEmpty()) {
            status = "400 Bad Request";
            body = "Request body is missing";
            sendHttpResponse(socket, status, body);
            return;
        }

        try {
            Map<String, String> post = queryStringToMap(request.getBody());

            post.put("ipaddr", socket.getRemoteSocketAddress().toString());
            String action = post.get("action").toLowerCase();

            Map<String, String> ret;
            switch (action) {
                case "login":
                    String challenge = generateRandomStr(8);
                    String authtoken = generateAuthToken();
                    ret = new HashMap<>();
                    ret.put("retry", "0");
                    ret.put("returncd", "001");
                    ret.put("locator", "gamespy.com");
                    ret.put("challenge", challenge);
                    ret.put("token", authtoken);
                    break;
                case "svcloc":
                    String authtokenSvc = generateAuthToken();
                    ret = new HashMap<>();
                    ret.put("retry", "0");
                    ret.put("returncd", "007");
                    ret.put("statusdata", "Y");
                    if (post.containsKey("svc")) {
                        if (post.get("svc").equals("9000") || post.get("svc").equals("9001")) {
                            ret.put("svchost", request.getHeaders().get("host").split(",")[0]);
                            if (post.get("svc").equals("9000")) {
                                ret.put("token", authtokenSvc);
                            } else {
                                ret.put("servicetoken", authtokenSvc);
                            }
                        } else if (post.get("svc").equals("0000")) {
                            ret.put("servicetoken", authtokenSvc);
                            ret.put("svchost", "n/a");
                        } else {
                            ret.put("svchost", "n/a");
                            ret.put("servicetoken", authtokenSvc);
                        }
                    }
                    break;
                default:
                    status = "400 Bad Request";
                    body = "Invalid action";
                    sendHttpResponse(socket, status, body);
                    return;
            }
            ret.put("datetime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            body = mapToQueryString(ret);
            status = "200 OK";
        } catch (Exception e) {
            log.error("Exception occurred on POST request!", e);
        }
        sendHttpResponse(socket, status, body);
    }

    private void sendHttpResponse(Socket socket, String status, String body) throws IOException {
        String response = "HTTP/1.1 " + status + "\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "NODE: wifiappe1\r\n" +
                "\r\n" +
                body;
        socket.getOutputStream().write(response.getBytes());
    }

    public Map<String, String> queryStringToMap(String str) {
        Map<String, String> ret = new HashMap<>();
        String[] pairs = str.split("&");
        for (String pair : pairs) {
            String key;
            String value;
            String[] keyValue = pair.split("=");
            if (keyValue.length > 1) {
                key = keyValue[0];
                value = decodeUrlEncoded(keyValue[1]);
            } else {
                key = keyValue[0];
                value = "";
            }
            try {
                value = decodeUrlEncoded(value);
                value = value.replace("*", "=").replace("?", "/").replace(">", "+").replace("-", "/");
                byte[] bytes = Base64.getDecoder().decode(value);
                value = new String(bytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.error("Error decoding value: {}", value, e);
            }
            ret.put(key, value);
        }
        return ret;
    }

    private String decodeUrlEncoded(String str) {
        try {
            return URLDecoder.decode(str, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return str;
        }
    }

    private String mapToQueryString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String encoded = Base64.getEncoder().encodeToString(entry.getValue().getBytes());
            encoded = encoded.replace("=", "*");
            sb.append(entry.getKey()).append("=").append(encoded).append("&");
        }
        return sb.toString().replaceAll("&$", "") + "\r\n";
    }

    private String generateRandomStr(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    private String generateAuthToken() {
        return "NDS" + generateRandomStr(80);
    }

}
