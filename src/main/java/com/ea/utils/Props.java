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

    @Value("${tcp.port}")
    private int tcpPort;

    @Value("${tcp.host}")
    private String tcpHost;

    @Value("${uhs.ea-server-mode}")
    private boolean uhsEaServerMode;

    @Value("${uhs.auto-start}")
    private boolean uhsAutoStart;

    @Value("${tcp.debug.enabled}")
    private boolean tcpDebugEnabled;

    @Value("${tcp.debug.exclusions}")
    private List<String> tcpDebugExclusions;

    @Value("${tos.enabled}")
    private boolean tosEnabled;

    @Value("${ssc2.key}")
    private String ssc2Key;

    @Value("${lobby.close-expired}")
    private boolean closeExpiredLobbiesEnabled;

    @Value("#{'${hosted-games}'.split(',')}")
    private List<String> hostedGames;

}
