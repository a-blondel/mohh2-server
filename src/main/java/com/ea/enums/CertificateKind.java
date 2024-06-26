package com.ea.enums;

public enum CertificateKind {
    MOH_WII("MOH_WII",
            "CN=wiimoh08.ea.com, OU=Global Online Studio, O=Electronic Arts, Inc., ST=California, C=US",
            "OU=Online Technology Group, O=Electronic Arts, Inc., L=Redwood City, ST=California, C=US, CN=OTG3 Certificate Authority"),
    MOH_PSP("MOH_PSP",
            "CN=pspmoh08.ea.com, OU=Global Online Studio, O=Electronic Arts, Inc., ST=California, C=US",
            "OU=Online Technology Group, O=Electronic Arts, Inc., L=Redwood City, ST=California, C=US, CN=OTG3 Certificate Authority"),
    TOS("TOS",
            "CN=tos.ea.com, OU=Global Online Studio, O=Electronic Arts, Inc., ST=California, C=US",
            "OU=Online Technology Group, O=Electronic Arts, Inc., L=Redwood City, ST=California, C=US, CN=OTG3 Certificate Authority");

    private final String name;
    private final String subject;
    private final String issuer;

    CertificateKind(String name, String subject, String issuer) {
        this.name = name;
        this.subject = subject;
        this.issuer = issuer;
    }

    public String getName() {
        return name;
    }

    public String getSubject() {
        return subject;
    }

    public String getIssuer() {
        return issuer;
    }
}
