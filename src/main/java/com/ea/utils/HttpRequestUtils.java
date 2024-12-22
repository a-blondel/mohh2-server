package com.ea.utils;

import com.ea.dto.HttpRequestData;

import java.util.HashMap;
import java.util.Map;

public class HttpRequestUtils {


    /**
     * Check if the packet is an HTTP packet
     * @param buffer the request buffer
     * @return boolean - true if the packet is an HTTP packet
     */
    public static boolean isHttpPacket(byte[] buffer) {
        String packetStart = new String(buffer, 0, Math.min(buffer.length, 10)).toUpperCase();
        return packetStart.startsWith("GET ") || packetStart.startsWith("POST ")
                || packetStart.startsWith("PUT ") || packetStart.startsWith("DELETE ")
                || packetStart.startsWith("HEAD ") || packetStart.startsWith("OPTIONS ")
                || packetStart.startsWith("PATCH ") || packetStart.startsWith("CONNECT ");
    }

    /**
     * Extracts the HTTP request from the buffer
     * @param buffer the buffer to extract from
     * @return the extracted HTTP request
     */
    public static HttpRequestData parseHttpRequest(byte[] buffer) {
        String requestString = new String(buffer);
        int bodyStart = requestString.indexOf("\r\n\r\n") + 4;
        if(bodyStart < 4 || bodyStart >= requestString.length()) {
            return null;
        }
        String[] lines = requestString.substring(0, bodyStart - 4).split("\r\n");
        Map<String, String> headers = new HashMap<>();
        for (String line : lines) {
            if (line.contains(": ")) {
                String[] header = line.split(": ");
                headers.put(header[0].toLowerCase(), header[1]);
            }
        }
        int bodyEnd = requestString.indexOf("\0", bodyStart);
        String body = requestString.substring(bodyStart, bodyEnd == -1 ? requestString.length() : bodyEnd);

        HttpRequestData request = new HttpRequestData();
        request.setMethod(lines[0].split(" ")[0].toUpperCase());
        request.setUri(lines[0].split(" ")[1]);
        request.setHttpVersion(lines[0].split(" ")[2]);
        request.setHeaders(headers);
        request.setBody(body);

        return request;
    }
}
