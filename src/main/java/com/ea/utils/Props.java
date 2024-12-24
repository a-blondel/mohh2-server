package com.ea.utils;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
public class Props {

    @Value("${ssl.protocols}")
    private String sslProtocols;

    @Value("${ssl.cipher-suites}")
    private String sslCipherSuites;

    @Value("${ssl.certificate.cipher-algorithm}")
    private String sslCertificateCipherAlgorithm;

    @Value("${ssl.debug.enabled}")
    private boolean sslDebugEnabled;

    @Value("${tcp.host}")
    private String tcpHost;

    @Value("${tcp.debug.enabled}")
    private boolean tcpDebugEnabled;

    @Value("${tcp.debug.exclusions}")
    private List<String> tcpDebugExclusions;

    @Value("${tos.base_url}")
    private String tosBaseUrl;

    @Value("${ssc2.key}")
    private String ssc2Key;

    @Value("#{'${hosted-games}'.split(',')}")
    private List<String> hostedGames;

}
