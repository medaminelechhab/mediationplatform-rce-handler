package com.MyProject.mediationplatformrcehandler.network;

import com.MyProject.mediationplatform.common.model.InterfaceParameters;
import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
@Data
@ToString
public class EFilesParameters extends InterfaceParameters {
    @Value("${clinksplatform.interface.connection.base-url}")
    private String baseUrl;
    @Value("${clinksplatform.interface.connection.auth-url}")
    private String authUrl;
    @Value("${clinksplatform.interface.connection.client-id}")
    private String clientId;
    @Value("${clinksplatform.interface.connection.client-secret}")
    private String clientSecret;
    @Value("${clinksplatform.interface.connection.grant-type}")
    private String grantType;
    @Value("${clinksplatform.interface.connection.use-proxy}")
    private boolean useProxy;
    @Value("${clinksplatform.interface.connection.use-proxy-for-auth}")
    private boolean useProxyForAuth;
    @Value("${clinksplatform.interface.connection.use-ssl}")
    private boolean useSSL;

    private String cuid;

    @Override
    public void setAuthorisation(HttpHeaders httpHeaders, String accessToken) {
        httpHeaders.setBearerAuth(accessToken);
    }
}
